package com.strawhat.moviedatabase.ui.search

import android.app.SearchManager
import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.media.Rating
import android.net.Uri
import android.provider.BaseColumns
import com.google.gson.Gson
import com.strawhat.moviedatabase.MyApplication
import com.strawhat.moviedatabase.services.MovieRepository
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.math.min

class CitySuggestionProvider : ContentProvider() {

    private var mUriMatcher: UriMatcher? = null

    private var cities: MutableList<String>? = null

    @Inject
    lateinit var movieRepository: MovieRepository

    override fun onCreate(): Boolean {
        try {
            val applicationContext = context!!.applicationContext
            val myApplication = applicationContext as MyApplication
            myApplication.appComponent.inject(this)
        } catch (e: Exception) {

        }

        mUriMatcher = UriMatcher(UriMatcher.NO_MATCH)
        mUriMatcher!!.addURI(
            AUTHORITY,
            "search_suggest_query/*",
            TYPE_ALL_SUGGESTIONS
        )
        return false
    }

    override fun query(
        uri: Uri, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor? {
        val cursor = MatrixCursor(
            arrayOf(
                BaseColumns._ID,
                SearchManager.SUGGEST_COLUMN_TEXT_1,
                SearchManager.SUGGEST_COLUMN_RATING_SCORE,
                SearchManager.SUGGEST_COLUMN_RATING_STYLE,
                SearchManager.SUGGEST_COLUMN_PRODUCTION_YEAR,
                SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID,
                SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA
            )
        )
        val query = uri.lastPathSegment!!.toUpperCase(Locale.US)
        val limit = uri.getQueryParameter(SearchManager.SUGGEST_PARAMETER_LIMIT)!!.toInt()
        movieRepository?.let {
            val result = it.searchForMovies(query, 1).blockingFirst().first
            for (i in 0 until (min(limit, result.size))) {
                var year: Int? = null
                try {
                    val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(result[i].firstAirDate)!!
                    val cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"))
                    cal.time = date
                    year = cal[Calendar.YEAR]
                } catch (e: java.lang.Exception) {
                }
                cursor.addRow(arrayOf(result[i].id, result[i].name, result[i].voteAverage, Rating.RATING_5_STARS, year, i, Gson().toJson(result[i])))
            }
        }
        return cursor
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun getType(uri: Uri): String? {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun update(
        uri: Uri, values: ContentValues?, selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        throw UnsupportedOperationException("Not yet implemented")
    }

    companion object {
        private const val AUTHORITY = "com.strawhat.moviedatabase.search"
        private const val TYPE_ALL_SUGGESTIONS = 1
    }
}