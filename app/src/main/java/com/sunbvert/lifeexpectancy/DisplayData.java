package com.sunbvert.lifeexpectancy;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.JsonReader;
import android.util.Log;

import com.github.mikephil.charting.charts.LineChart;
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



    private void drawLineChart(LifeExpValuesForPlotting valuesForPlotting){
        List<Entry> popuEntries = new ArrayList<Entry>();
        List<Entry> ageEntries = new ArrayList<Entry>();
        List<Entry> gdpEntries = new ArrayList<Entry>();

        //int entrySize = valuesForPlotting.averageLifeTime.length;
        int dataSize = valuesForPlotting.gdp.length;

        String selectedCountry = valuesForPlotting.counties[0];

        float j = 0;
        for(int i = 0; i < dataSize; i++){
            if (valuesForPlotting.counties[i].equals(selectedCountry)){
                popuEntries.add(new Entry(j, valuesForPlotting.population[i]));
                ageEntries.add(new Entry(j, (float)valuesForPlotting.averageLifeTime[i]));
                gdpEntries.add(new Entry(j, valuesForPlotting.gdp[i]));
            }
        }

        LineDataSet setPopu = new LineDataSet(popuEntries, "人口数");
        setPopu.setAxisDependency(YAxis.AxisDependency.LEFT);
        LineDataSet setAge = new LineDataSet(ageEntries, "平均年龄");
        setAge.setAxisDependency(YAxis.AxisDependency.LEFT);
        LineDataSet setGdp = new LineDataSet(gdpEntries, "GDP");
        setGdp.setAxisDependency(YAxis.AxisDependency.LEFT);

        List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(setPopu);
        dataSets.add(setAge);
        dataSets.add(setGdp);

        LineData data = new LineData(dataSets);

        chart.setData(data);
        chart.invalidate();
    }
}
