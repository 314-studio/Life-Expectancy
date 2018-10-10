package com.sunbvert.lifeexpectancy;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.JsonReader;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class DisplayData extends AppCompatActivity {

    static String TAG = "数据展示界面";
    static String fileName = "life-expectancy.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.data_display_layout);

    }

    @Override
    protected void onStart(){
        super.onStart();

        try {
            InputStream in = getAssets().open(fileName);
            JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
            LoadJsonData asyncLoad = new LoadJsonData();
            asyncLoad.execute(reader);
        } catch (IOException e){
            Log.e(TAG, "无法读取到文件" + fileName);
            //todo: 添加警示框
        }
    }
}
