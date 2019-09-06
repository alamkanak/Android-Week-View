package com.alamkanak.weekview.sample

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen

class SampleApp : Application() {

    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
    }
}
