package com.sunbvert.lifeexpectancy;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MapActivity extends AppCompatActivity {

    MapChart mapChart;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_chart);

        mapChart = (MapChart) findViewById(R.id.map_chart);
    }
}
