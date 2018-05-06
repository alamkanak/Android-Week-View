package com.alamkanak.weekview.sample

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View


/**
 * The launcher activity of the sample app. It contains the links to visit all the example screens.
 * Created by Raquib-ul-Alam Kanak on 7/21/2014.
 * Website: http://alamkanak.github.io
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        val intent = Intent(this@MainActivity, BasicActivity::class.java)
//        startActivity(intent)
//        finish()
        findViewById<View>(R.id.buttonBasic).setOnClickListener {
            val intent = Intent(this@MainActivity, BasicActivity::class.java)
            startActivity(intent)
        }

        findViewById<View>(R.id.buttonAsynchronous).setOnClickListener {
            val intent = Intent(this@MainActivity, AsynchronousActivity::class.java)
            startActivity(intent)
        }
    }

}
