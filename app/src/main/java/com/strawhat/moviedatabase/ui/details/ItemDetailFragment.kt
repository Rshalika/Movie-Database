package com.strawhat.moviedatabase.ui.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.strawhat.moviedatabase.BuildConfig
import com.strawhat.moviedatabase.MyApplication
import com.strawhat.moviedatabase.R
import com.strawhat.moviedatabase.services.bindings.Movie
import com.strawhat.moviedatabase.vm.events.MainViewState
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.exceptions.OnErrorNotImplementedException
import io.reactivex.rxjava3.kotlin.subscribeBy
import kotlinx.android.synthetic.main.activity_item_detail.*
import kotlinx.android.synthetic.main.item_detail.*


private const val VISIBLE_THRESHOLD = 2

class ItemDetailFragment : Fragment() {

    private lateinit var movie: Movie

    private lateinit var layoutManager: LinearLayoutManager

    private lateinit var adapter: SimilarMoviesListAdapter

    private var twoPane: Boolean = false

    private val viewModel by viewModels<DetailsViewModel>()

    private val disposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = savedInstanceState ?: requireArguments()
        movie = bundle.getSerializable(ARG_ITEM) as Movie
        (requireActivity().application as MyApplication).appComponent.inject(viewModel)
        twoPane = activity?.findViewById<ImageView>(R.id.movie_image) != null
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(ARG_ITEM, movie)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.item_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.findViewById<CollapsingToolbarLayout>(R.id.toolbar_layout)?.title = movie.name
        val movieImage = activity?.findViewById<ImageView>(R.id.movie_image)
        if (movieImage == null) {
            val detailsImageView: ImageView = view.findViewById(R.id.details_image_view)
            detailsImageView.visibility = View.VISIBLE
            Glide
                .with(this)
                .load("${BuildConfig.IMAGES_URL_PREFIX}${movie.posterPath}")
                .centerCrop()
//            .placeholder(R.drawable.loader_image)
                .into(detailsImageView)
        } else {
            Glide
                .with(this)
                .load("${BuildConfig.IMAGES_URL_PREFIX}${movie.backdropPath}")
                .centerCrop()
//            .placeholder(R.drawable.loader_image)
                .into(movieImage)
        }
        description.text = movie.overview
        details_rating.text = movie.voteAverage.toString()
        setupRecyclerView(similar_movies_list)
        setUpLoadMoreListener(similar_movies_list)
        disposable.add(
            viewModel.viewStateRelay.subscribeBy(
                onNext = {
                    updateState(it)
                },
                onError = {
                    throw OnErrorNotImplementedException(it)
                }
            )
        )
        viewModel.afterInit(movie.id.toString())
    }

    private fun updateState(state: MainViewState) {
        adapter.setMovies(state.items.toMutableList())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposable.dispose()
    }

    private fun setupRecyclerView(recyclerView: RecyclerView) {
        layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        adapter = SimilarMoviesListAdapter(requireActivity(), twoPane)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
    }

    private fun setUpLoadMoreListener(recyclerView: RecyclerView) {
        recyclerView.setOnScrollChangeListener { _, _, _, _, _ ->
            val findLastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
            val totalItemCount = layoutManager.itemCount
            if (totalItemCount <= findLastVisibleItemPosition + VISIBLE_THRESHOLD) {
                viewModel.loadPageRequested()
            }
        }
    }

    companion object {
        const val ARG_ITEM = "movie"
    }
}
