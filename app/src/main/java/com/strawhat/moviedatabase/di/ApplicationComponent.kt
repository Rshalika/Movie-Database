package com.strawhat.moviedatabase.di

import com.strawhat.moviedatabase.vm.MainViewModel
import dagger.Component

@Component(modules = [ApplicationModule::class])
interface ApplicationComponent {

    fun inject(viewModel: MainViewModel)
}