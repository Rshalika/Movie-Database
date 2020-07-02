package com.strawhat.moviedatabase.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.jakewharton.rxrelay3.BehaviorRelay
import com.jakewharton.rxrelay3.PublishRelay
import com.strawhat.moviedatabase.services.MovieRepository
import com.strawhat.moviedatabase.services.bindings.Movie
import com.strawhat.moviedatabase.vm.events.*
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableTransformer
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.exceptions.OnErrorNotImplementedException
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val disposable = CompositeDisposable()

    private val viewActionsRelay: PublishRelay<ViewAction> = PublishRelay.create<ViewAction>()

    private val viewResultsRelay: PublishRelay<ViewResult> = PublishRelay.create<ViewResult>()

    private var previousState = MainViewState()

    val viewStateRelay: BehaviorRelay<MainViewState> = BehaviorRelay.create<MainViewState>()

    @Inject
    lateinit var movieRepository: MovieRepository


    fun afterInit() {
        val loadPage = ObservableTransformer<LoadPageAction, ViewResult> { event ->
            return@ObservableTransformer event.flatMap { action ->
                return@flatMap movieRepository.loadPage(action.page)
                    .subscribeOn(Schedulers.io())
                    .map(fun(it: Pair<List<Movie>, Boolean>): ViewResult {
                        return LoadPageSuccessResult(action.page, it.first, it.second)
                    })
                    .onErrorReturn {
                        LoadPageFailResult(it)
                    }
                    .observeOn(AndroidSchedulers.mainThread())
                    .startWithItem(LoadingResult)
            }
        }

        val search = ObservableTransformer<SearchAction, ViewResult> { event ->
            return@ObservableTransformer event.flatMap { action ->
                return@flatMap movieRepository.searchForMovies(action.query, action.page)
                    .subscribeOn(Schedulers.io())
                    .map(fun(it: Pair<List<Movie>, Boolean>): ViewResult {
                        return SearchSuccessResult(action.page, it.first, it.second)
                    })
                    .onErrorReturn {
                        SearchPageFailResult(it)
                    }
                    .observeOn(AndroidSchedulers.mainThread())
                    .startWithItem(LoadingResult)
            }
        }

        val UI = ObservableTransformer<ViewAction, ViewResult> { event ->
            return@ObservableTransformer event.publish { shared ->
                return@publish Observable.mergeArray(
                    shared.ofType(LoadPageAction::class.java).compose(loadPage),
                    shared.ofType(SearchAction::class.java).compose(search),
                    shared.ofType(SearchActivatedAction::class.java).map { SearchActivatedResult },
                    shared.ofType(SearchDeActivatedAction::class.java).map { SearchDeActivatedResult }
                )

            }
        }

        disposable.add(
            viewActionsRelay
                .compose(UI)
                .mergeWith(viewResultsRelay)
                .observeOn(AndroidSchedulers.mainThread())
                .scan(previousState, { state, result ->
                    return@scan reduce(state, result)
                })
                .subscribeBy(
                    onNext = {
                        emmit(it)
                    },
                    onError = {
                        throw OnErrorNotImplementedException(it)
                    }
                )
        )
        loadPageRequested()
    }

    private fun reduce(state: MainViewState, result: ViewResult): MainViewState {
        return when (result) {
            is LoadPageSuccessResult -> {
                val items = state.items
                result.items.forEach {
                    if (items.contains(it).not()) {
                        items.add(it)
                    }
                }
                state.copy(
                    hasNext = result.hasNext,
                    lastPage = result.pageNumber,
                    items = items,
                    loading = false,
                    errorMessage = null
                )
            }
            is LoadPageFailResult -> {
                state.copy(
                    loading = false,
                    errorMessage = result.throwable.message
                )
            }
            LoadingResult -> {
                state.copy(loading = true)
            }
            LoadPageRequestedResult -> {
                if (state.loading.not() && state.hasNext && state.searchEnabled.not()) {
                    viewActionsRelay.accept(LoadPageAction(state.lastPage + 1))
                }
                if (state.loading.not() && state.hasNext && state.searchEnabled && state.searchQuery.isNullOrBlank().not()) {
                    viewActionsRelay.accept(SearchAction(state.lastPage + 1, state.searchQuery!!))
                }
                state.copy()
            }
            SearchActivatedResult -> {
                state.copy(searchEnabled = true, lastPage = 0, items = linkedSetOf(), hasNext = true)
            }
            SearchDeActivatedResult -> {
                loadPageRequested()
                state.copy(searchEnabled = false, lastPage = 0, items = linkedSetOf())
            }
            is SearchSuccessResult -> {
                val items = state.items
                result.items.forEach {
                    if (items.contains(it).not()) {
                        items.add(it)
                    }
                }
                state.copy(
                    hasNext = result.hasNext,
                    lastPage = result.pageNumber,
                    items = items,
                    loading = false,
                    errorMessage = null
                )
            }
            is SearchPageFailResult -> {
                state.copy(
                    loading = false,
                    errorMessage = result.throwable.message
                )
            }
            is SearchRequestResult -> {
                if (state.loading.not() && state.hasNext) {
                    viewActionsRelay.accept(SearchAction(state.lastPage + 1, result.query))
                }
                state.copy(searchQuery = result.query)
            }
        }
    }

    private fun emmit(state: MainViewState) {
        previousState = state
        viewStateRelay.accept(state)
    }

    fun loadPageRequested() {
        viewResultsRelay.accept(LoadPageRequestedResult)
    }

    fun searchActivated() {
        viewActionsRelay.accept(SearchActivatedAction)
    }

    fun searchDeActivated() {
        viewActionsRelay.accept(SearchDeActivatedAction)
    }

    fun search(query: String) {
        viewResultsRelay.accept(SearchRequestResult(query))
    }

    override fun onCleared() {
        super.onCleared()
        disposable.dispose()
    }

}