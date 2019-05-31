package com.example.customservicechasisnumbercheck.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.example.customservicechasisnumbercheck.R;
import com.example.customservicechasisnumbercheck.Utils;

public class NotInRange extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_tag_in_range);
        Utils.playSound(1);
        new Handler().postDelayed(
                new Runnable() {
                    public void run() {

                        Intent intent = new Intent(NotInRange.this, MainActivity.class);
                        startActivity(intent);
                    }
                },
                1500);


    }
}
