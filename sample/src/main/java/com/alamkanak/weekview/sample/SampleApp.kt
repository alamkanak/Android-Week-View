package com.alamkanak.weekview.sample

import android.app.Application
import androidx.emoji.bundled.BundledEmojiCompatConfig
import androidx.emoji.text.EmojiCompat
import com.jakewharton.threetenabp.AndroidThreeTen

class SampleApp : Application() {

    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
        EmojiCompat.init(BundledEmojiCompatConfig(this))
    }
}
