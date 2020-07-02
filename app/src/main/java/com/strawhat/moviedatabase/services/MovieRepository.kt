package com.strawhat.moviedatabase.services

import com.strawhat.moviedatabase.services.bindings.Movie
import io.reactivex.rxjava3.annotations.NonNull
import io.reactivex.rxjava3.core.Observable

class MovieRepository(private val apiService: ApiService) {

    fun loadPage(page: Int): @NonNull Observable<Pair<List<Movie>, Boolean>> {
        return apiService.getPopularMovies(page).map { Pair(it.movies, it.page < it.totalPages) }
    }

    fun loadSimilarMovies(tvId: String, page: Int): @NonNull Observable<List<Movie>> {
        return apiService.getSimilarMovies(tvId, page).map { it.movies }
    }

    fun searchForMovies(query: String, page: Int): @NonNull Observable<Pair<List<Movie>, Boolean>> {
        return apiService.searchForMovies(query, page).map { Pair(it.movies, it.page < it.totalPages) }
    }
}