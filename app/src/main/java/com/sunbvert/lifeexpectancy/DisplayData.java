package com.sunbvert.lifeexpectancy;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.JsonReader;
import android.util.Log;
import android.view.animation.Animation;

import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class DisplayData extends AppCompatActivity {

    static String TAG = "数据展示界面";
    static String TAG2 = "线程：读取JOSN数据" ;
    static String fileName = "life-expectancy.json";

    LineChart chart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.data_display_layout);

        chart = (LineChart) findViewById(R.id.line_chart);
    }

    @Override
    protected void onStart(){
        super.onStart();

        Intent data = getIntent();
    }



    public void drawLineChart(LifeExpValuesForPlotting valuesForPlotting){
        List<Entry> popuEntries = new ArrayList<Entry>();
        List<Entry> ageEntries = new ArrayList<Entry>();
        List<Entry> gdpEntries = new ArrayList<Entry>();

        //int entrySize = valuesForPlotting.averageLifeTime.length;
        int dataSize = valuesForPlotting.gdp.length;

        String selectedCountry = valuesForPlotting.counties[2];

        for(int i = 0; i < dataSize; i++){
            if (valuesForPlotting.counties[i].equals(selectedCountry)){
                popuEntries.add(new Entry(valuesForPlotting.investigateYear[i],
                        valuesForPlotting.population[i] / 10000000));
                ageEntries.add(new Entry(valuesForPlotting.investigateYear[i],
                        (float)valuesForPlotting.averageLifeTime[i]));
                gdpEntries.add(new Entry(valuesForPlotting.investigateYear[i],
                        valuesForPlotting.gdp[i] / 100));
            }
        }

        LineDataSet setPopu = new LineDataSet(popuEntries, "人口数");
        setPopu.setAxisDependency(YAxis.AxisDependency.LEFT);
        setPopu.setColor(Color.BLUE);
        LineDataSet setAge = new LineDataSet(ageEntries, "平均年龄");
        setAge.setAxisDependency(YAxis.AxisDependency.LEFT);
        setAge.setColor(Color.GREEN);
        LineDataSet setGdp = new LineDataSet(gdpEntries, "GDP");
        setGdp.setAxisDependency(YAxis.AxisDependency.LEFT);
        setGdp.setColor(Color.RED);

        List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(setPopu);
        dataSets.add(setAge);
        dataSets.add(setGdp);

        LineData data = new LineData(dataSets);

        Description description = new Description();
        description.setText(selectedCountry);

        chart.setData(data);
        chart.setDescription(description);
        chart.animateXY(3000, 3000);
        chart.invalidate();
    }
}
