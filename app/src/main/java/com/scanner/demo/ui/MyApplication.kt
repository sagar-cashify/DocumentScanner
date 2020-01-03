package com.scanner.demo.ui

import com.scanner.demo.core.di.AppComponent
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication

class MyApplication : DaggerApplication() {
    lateinit var appComponent: AppComponent
    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
//        appComponent = DaggerAppComponent.builder().application(this).build();
//        appComponent.inject(this);
        return appComponent
    }



}