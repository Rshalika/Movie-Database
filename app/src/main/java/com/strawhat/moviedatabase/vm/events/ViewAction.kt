package com.strawhat.moviedatabase.vm.events

sealed class ViewAction
data class LoadPageAction(val page: Int) : ViewAction()