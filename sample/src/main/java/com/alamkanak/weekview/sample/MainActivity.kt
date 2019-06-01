package com.alamkanak.weekview.sample

import android.content.Intent
import android.os.Bundle
import android.view.View

import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<View>(R.id.buttonBasic).setOnClickListener {
            val intent = Intent(this@MainActivity, BaseActivity::class.java)
            startActivity(intent)
        }

        findViewById<View>(R.id.buttonStatic).setOnClickListener {
            val intent = Intent(this@MainActivity, StaticActivity::class.java)
            startActivity(intent)
        }

        findViewById<View>(R.id.buttonConstraint).setOnClickListener {
            val intent = Intent(this@MainActivity, ConstraintActivity::class.java)
            startActivity(intent)
        }

        findViewById<View>(R.id.buttonAsynchronous).setOnClickListener {
            val intent = Intent(this@MainActivity, AsyncActivity::class.java)
            startActivity(intent)
        }

        findViewById<View>(R.id.buttonLimited).setOnClickListener {
            val intent = Intent(this@MainActivity, LimitedActivity::class.java)
            startActivity(intent)
        }

        findViewById<View>(R.id.buttonCustomFont).setOnClickListener {
            val intent = Intent(this@MainActivity, CustomFontActivity::class.java)
            startActivity(intent)
        }
    }

}
