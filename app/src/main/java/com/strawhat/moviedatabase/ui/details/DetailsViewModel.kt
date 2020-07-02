package com.strawhat.moviedatabase.ui.details

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

class DetailsViewModel(application: Application) : AndroidViewModel(application) {

    private val disposable = CompositeDisposable()

    private val viewActionsRelay: PublishRelay<ViewAction> = PublishRelay.create<ViewAction>()

    private val viewResultsRelay: PublishRelay<ViewResult> = PublishRelay.create<ViewResult>()

    private var previousState = MainViewState()

    val viewStateRelay: BehaviorRelay<MainViewState> = BehaviorRelay.create<MainViewState>()

    @Inject
    lateinit var movieRepository: MovieRepository


    fun afterInit(movieId: String) {
        val loadPage = ObservableTransformer<LoadPageAction, ViewResult> { event ->
            return@ObservableTransformer event.flatMap { action ->
                return@flatMap movieRepository.loadSimilarMovies(movieId, action.page)
                    .subscribeOn(Schedulers.io())
                    .map(fun(it: List<Movie>): ViewResult {
                        return LoadPageSuccessResult(action.page, it,false)
                    })
                    .onErrorReturn {
                        LoadPageFailResult(it)
                    }
                    .observeOn(AndroidSchedulers.mainThread())
                    .startWithItem(LoadingResult)
            }
        }

        val UI = ObservableTransformer<ViewAction, ViewResult> { event ->
            return@ObservableTransformer event.publish { shared ->
                return@publish Observable.mergeArray(shared.ofType(LoadPageAction::class.java).compose(loadPage))

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
                if (state.loading.not()) {
                    viewActionsRelay.accept(LoadPageAction(state.lastPage + 1))
                }
                state.copy()
            }
            is SearchRequestResult -> {
                state.copy()
            }
            SearchActivatedResult -> {
                state.copy()
            }
            SearchDeActivatedResult -> {
                state.copy()
            }
            is SearchSuccessResult -> {
                state.copy()
            }
            is SearchPageFailResult -> {
                state.copy()
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

    override fun onCleared() {
        super.onCleared()
        disposable.dispose()
    }
}