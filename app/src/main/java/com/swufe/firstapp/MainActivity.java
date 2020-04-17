package com.swufe.firstapp;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    EditText inp;
    TextView out;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inp = (EditText) findViewById(R.id.inpText);
        String str = inp.getText().toString();
        inp.setText("请输入摄氏温度");
        out = (TextView) findViewById(R.id.showText);
        Log.i("main","input=" + str);

        Button but = (Button) findViewById(R.id.button);
        but.setText("转换为华氏温度");
        but.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Log.i("click","onClick.....");

        String str = inp.getText().toString();
        Double num= Double.valueOf(inp.getText().toString());
        Double result=num*1.8+32;
        out.setText("结果为：" + result);
    }
}

