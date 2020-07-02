package com.strawhat.moviedatabase.ui.details

import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomViewTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.strawhat.moviedatabase.BuildConfig
import com.strawhat.moviedatabase.MyApplication
import com.strawhat.moviedatabase.R
import com.strawhat.moviedatabase.services.bindings.Movie
import com.strawhat.moviedatabase.util.GenresUtil
import com.strawhat.moviedatabase.vm.events.MainViewState
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.exceptions.OnErrorNotImplementedException
import io.reactivex.rxjava3.kotlin.subscribeBy
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
//                .into(movieImage)
                .into(object : CustomViewTarget<ImageView, Drawable>(movieImage) {
                    override fun onLoadFailed(errorDrawable: Drawable?) {

                    }

                    override fun onResourceCleared(placeholder: Drawable?) {

                    }

                    override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                        movieImage.setImageDrawable(resource)
                        val toBitmap = resource.toBitmap()
                        val builder = Palette.Builder(toBitmap)
                        val palette = builder.generate()

                        val dominantSwatch = palette.dominantSwatch
                        val vibrantSwatch = palette.vibrantSwatch
                        val lightVibrantSwatch = palette.lightVibrantSwatch
                        val lightMutedSwatch = palette.lightMutedSwatch
                        val darkVibrantSwatch = palette.darkVibrantSwatch
                        val darkMutedSwatch = palette.darkMutedSwatch
                        val secondaryLightColor = resources.getColor(R.color.secondaryLightColor, requireContext().theme)
                        val _primaryColor = resources.getColor(R.color.primaryColor, requireContext().theme)
                        val primaryDarkColor = resources.getColor(R.color.primaryDarkColor, requireContext().theme)

                        val lightColor = arrayListOf(
                            lightVibrantSwatch?.rgb,
                            lightMutedSwatch?.rgb,
                            secondaryLightColor
                        ).find { it != null }!!

                        val primaryColor = arrayListOf(
                            vibrantSwatch?.rgb,
                            dominantSwatch?.rgb,
                            _primaryColor
                        ).find { it != null }!!

                        val darkColor = arrayListOf(
                            darkVibrantSwatch?.rgb,
                            darkMutedSwatch?.rgb,
                            primaryDarkColor
                        ).find { it != null }!!

                        (activity?.findViewById<CollapsingToolbarLayout>(R.id.toolbar_layout))?.setContentScrimColor(primaryColor)
                        activity?.run {
                            val window: Window = window
                            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                            window.statusBarColor = darkColor
                        }
                        rating_bar.backgroundTintList = ColorStateList.valueOf(lightColor)
                        details_rating.setTextColor(lightColor)
                    }

                })

        }
        description.text = movie.overview
        details_rating.text = movie.voteAverage.toString()
        setupRecyclerView(similar_movies_list)
        setUpLoadMoreListener(similar_movies_list)
        details_title.text = movie.name
        movie.genreIds.forEach {
            val genre = GenresUtil.genreMap[it]!!
            val textView =
                LayoutInflater.from(requireContext()).inflate(R.layout.genre_name_text_view, categories_list, false) as TextView
            textView.text = genre
            categories_list.addView(textView)
        }
        rating_bar.rating = movie.voteAverage.div(2).toFloat()


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
