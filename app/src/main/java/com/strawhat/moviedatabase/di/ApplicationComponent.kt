package com.strawhat.moviedatabase.di

import com.strawhat.moviedatabase.ui.details.DetailsViewModel
import com.strawhat.moviedatabase.ui.search.CitySuggestionProvider
import com.strawhat.moviedatabase.vm.MainViewModel
import dagger.Component

@Component(modules = [ApplicationModule::class])
interface ApplicationComponent {

    fun inject(viewModel: MainViewModel)

    fun inject(viewModel: DetailsViewModel)

    fun inject(viewModel: CitySuggestionProvider)
}