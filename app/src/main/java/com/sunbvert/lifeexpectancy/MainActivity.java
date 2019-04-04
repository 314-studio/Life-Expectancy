package com.sunbvert.lifeexpectancy;

import android.content.Intent;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class MainActivity extends AppCompatActivity {

    static final String TAG = "应用程序主界面";
    static final String fileName = "life-expectancy.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    private void init(){
        ArrayAdapter<String> fileListAdapter = new ArrayAdapter<String>(
                MainActivity.this, android.R.layout.simple_list_item_1, new String[]{fileName});
        ListView fileList = (ListView) findViewById(R.id.file_list_view);
        fileList.setAdapter(fileListAdapter);

        fileList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //只有一个文件，所以无需判断到底选取了哪个文件
                Intent showMapChart = new Intent(MainActivity.this, MapActivity.class);
                startActivity(showMapChart);
            }
        });
    }
}
