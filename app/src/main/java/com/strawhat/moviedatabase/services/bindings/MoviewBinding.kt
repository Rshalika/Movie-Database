package com.strawhat.moviedatabase.services.bindings

import com.google.gson.annotations.SerializedName
import java.io.Serializable


data class MovieResponse(
    @SerializedName("page")
    var page: Int,
    @SerializedName("results")
    var movies: List<Movie>,
    @SerializedName("total_pages")
    var totalPages: Int,
    @SerializedName("total_results")
    var totalResults: Int
)

data class Movie(
    @SerializedName("backdrop_path")
    var backdropPath: String,
    @SerializedName("first_air_date")
    var firstAirDate: String,
    @SerializedName("genre_ids")
    var genreIds: List<Int>,
    @SerializedName("id")
    var id: Int,
    @SerializedName("name")
    var name: String,
    @SerializedName("origin_country")
    var originCountry: List<String>,
    @SerializedName("original_language")
    var originalLanguage: String,
    @SerializedName("original_name")
    var originalName: String,
    @SerializedName("overview")
    var overview: String,
    @SerializedName("popularity")
    var popularity: Double,
    @SerializedName("poster_path")
    var posterPath: String,
    @SerializedName("vote_average")
    var voteAverage: Double,
    @SerializedName("vote_count")
    var voteCount: Int
) : Serializable