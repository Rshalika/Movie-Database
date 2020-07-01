package com.strawhat.moviedatabase.vm.events

import com.strawhat.moviedatabase.services.bindings.Movie

sealed class ViewResult

data class LoadPageSuccessResult(val pageNumber: Int, val items: List<Movie>) : ViewResult()
data class LoadPageFailResult(val throwable: Throwable) : ViewResult()
object LoadPageRequestedResult : ViewResult()
object LoadingResult : ViewResult()
