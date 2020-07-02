package com.strawhat.moviedatabase.services

import com.strawhat.moviedatabase.BuildConfig
import com.strawhat.moviedatabase.services.bindings.MovieResponse
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("3/tv/popular")
    fun getPopularMovies(
        @Query("page") page: Int,
        @Query("api_key") apiKey: String = BuildConfig.API_KEY,
        @Query("language") language: String = "en-US"
    ): Observable<MovieResponse>

    @GET("3/tv/{tvID}/similar")
    fun getSimilarMovies(
        @Path("tvID") tvId: String,
        @Query("page") page: Int,
        @Query("api_key") apiKey: String = BuildConfig.API_KEY,
        @Query("language") language: String = "en-US"
    ): Observable<MovieResponse>

    @GET("/3/search/tv")
    fun searchForMovies(
        @Query("query") query: String,
        @Query("page") page: Int,
        @Query("api_key") apiKey: String = BuildConfig.API_KEY,
        @Query("language") language: String = "en-US"
    ): Observable<MovieResponse>
}