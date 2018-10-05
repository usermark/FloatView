package com.github.usermark.floatview.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.github.usermark.floatview.FloatView;

public class MainActivity extends AppCompatActivity {

    private FloatView floatView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        floatView = new FloatView(this, R.mipmap.ic_launcher_round,
                WindowManager.LayoutParams.TYPE_APPLICATION);
        floatView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Click!", Toast.LENGTH_SHORT).show();
            }
        });
        floatView.show();
    }

    public void onSetGravity(View v) {
        floatView.setFloatGravity(Integer.parseInt((String) v.getTag()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        floatView.release();
    }
}
