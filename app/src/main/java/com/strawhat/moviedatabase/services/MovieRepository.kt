package com.strawhat.moviedatabase.services

import com.strawhat.moviedatabase.services.bindings.Movie
import io.reactivex.rxjava3.annotations.NonNull
import io.reactivex.rxjava3.core.Observable

class MovieRepository(private val apiService: ApiService) {

    fun loadPage(page: Int): @NonNull Observable<List<Movie>> {
        return apiService.getPopularMovies(page).map { it.movies }
    }
}