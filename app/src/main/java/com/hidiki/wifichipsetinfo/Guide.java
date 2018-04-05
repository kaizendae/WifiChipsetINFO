package com.hidiki.wifichipsetinfo;

import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class Guide extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.dark)));
        setContentView(R.layout.activity_guide);
    }
    public void clickuse(View v){
        this.finish();
    }
}
