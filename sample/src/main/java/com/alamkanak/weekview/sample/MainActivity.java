package com.alamkanak.weekview.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;


/**
 * The launcher activity of the sample app. It contains the links to visit all the example screens.
 * Created by Raquib-ul-Alam Kanak on 7/21/2014.
 * Website: http://alamkanak.github.io
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.buttonBasic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, BaseActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.buttonStatic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, StaticActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.buttonConstraint).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ConstraintActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.buttonAsynchronous).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AsyncActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.buttonLimited).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LimitedActivity.class);
                startActivity(intent);
            }
        });
    }

}
