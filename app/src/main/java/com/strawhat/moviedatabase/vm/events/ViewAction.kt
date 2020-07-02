package com.strawhat.moviedatabase.vm.events

sealed class ViewAction
data class LoadPageAction(val page: Int) : ViewAction()
data class SearchAction(val page: Int, val query: String) : ViewAction()
object SearchActivatedAction : ViewAction()
object SearchDeActivatedAction : ViewAction()