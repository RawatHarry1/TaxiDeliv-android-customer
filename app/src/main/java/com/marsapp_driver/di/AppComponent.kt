package com.marsapp_driver.di

import dagger.Component
import javax.inject.Singleton

// Dagger Graph
@Singleton
@Component(modules = [ApiModule::class])
interface AppComponent {
//    fun inject(fragment: RidesFragment)

}