package com.strawhat.moviedatabase

import android.app.Application
import com.strawhat.moviedatabase.di.DaggerApplicationComponent

class MyApplication: Application() {

    val appComponent = DaggerApplicationComponent.create()
}