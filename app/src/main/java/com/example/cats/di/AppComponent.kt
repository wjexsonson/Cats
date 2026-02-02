package com.example.cats.di

import com.example.cats.ui.FavoriteFragment
import com.example.cats.ui.HomeFragment
import com.example.cats.MainActivity
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [
    NetworkModule::class,
    DatabaseModule::class,
    ViewModelModule::class
])
interface AppComponent {
    fun inject(activity: MainActivity)
    fun inject(fragment: HomeFragment)
    fun inject(fragment: FavoriteFragment)
}
