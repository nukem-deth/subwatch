package com.subwatch

import android.app.Application
import com.subwatch.data.SubWatchDatabase
import com.subwatch.notify.NotifyScheduler

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        SubWatchDatabase.init(this)
        NotifyScheduler.scheduleDaily(this)
    }
}
