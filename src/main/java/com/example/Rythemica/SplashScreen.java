package com.example.Rythemica;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.myapplication.R;

public class SplashScreen extends AppCompatActivity {
    public static int splashTimer = 3000 ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        getSupportActionBar().hide(); //hide the title bar

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent splashScreen = new Intent(SplashScreen.this , MainActivity.class) ;
                startActivity(splashScreen);
                finish();
            }
        } , splashTimer);
    }
}
