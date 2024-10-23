package com.pak.linphoneapp

import android.app.Application

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize LinphoneManager
        LinphoneManager.initialize(this)
    }
}