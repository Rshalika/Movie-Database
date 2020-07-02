package com.strawhat.moviedatabase.vm.events

import com.strawhat.moviedatabase.services.bindings.Movie

sealed class ViewResult

data class LoadPageSuccessResult(val pageNumber: Int, val items: List<Movie>, val hasNext: Boolean) : ViewResult()
data class LoadPageFailResult(val throwable: Throwable) : ViewResult()
object LoadPageRequestedResult : ViewResult()
data class SearchRequestResult(val query: String) : ViewResult()
object LoadingResult : ViewResult()
object SearchActivatedResult : ViewResult()
object SearchDeActivatedResult : ViewResult()
data class SearchSuccessResult(val pageNumber: Int, val items: List<Movie>, val hasNext: Boolean) : ViewResult()
data class SearchPageFailResult(val throwable: Throwable) : ViewResult()