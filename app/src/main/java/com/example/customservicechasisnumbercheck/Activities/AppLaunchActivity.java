package com.example.customservicechasisnumbercheck.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.example.customservicechasisnumbercheck.R;

public class AppLaunchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_launch_activity);


        //dismiss splash screen and launch main activity

        new Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        Intent intent =  new Intent(AppLaunchActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                },
                1000);

    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    @Override
    protected void onPause(){
        super.onPause();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();

    }

}
