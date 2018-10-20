package com.sunbvert.lifeexpectancy;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.JsonReader;
import android.util.Log;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.ImageView;

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
    static String UNITED_STATES = "United States";

    static Bitmap COUNTRY_BITMAP;

    static int TITLE_HEIGHT = 250;

    LineChart chart;
    CountryInfoView mapView;

    Intent data;
    LifeExpValuesForPlotting plottingData;
    int[] gdpInColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.data_display_layout);

        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(displayMetrics);

        chart = (LineChart) findViewById(R.id.line_chart);
        chart.getLayoutParams().height = displayMetrics.heightPixels - TITLE_HEIGHT;

        mapView = (CountryInfoView) findViewById(R.id.map_view);

        this.data = getIntent();
        this.plottingData = new LifeExpValuesForPlotting(
                data.getIntArrayExtra(MapChart.GDP),
                data.getDoubleArrayExtra(MapChart.AVERAGE_LIFE_TIME),
                data.getLongArrayExtra(MapChart.POPULATION),
                data.getStringArrayExtra(MapChart.COUNTRIES),
                data.getIntArrayExtra(MapChart.INVESTIGATE_YEAR)
        );
        this.gdpInColor = data.getIntArrayExtra(MapChart.GDP_IN_COLOR);
        String countryName = data.getStringExtra(MapChart.COUNTRY_NAME);

        setTitle(countryName);


        displayMap(countryName,
                data.getIntExtra(MapChart.SELECTED_YEAR, 2010),
                COUNTRY_BITMAP, displayMetrics);
        drawLineChart(plottingData, countryName);
    }

    @Override
    protected void onStart(){
        super.onStart();
    }

    private void displayMap(String countryName, int year, Bitmap bitmap, DisplayMetrics displayMetrics){
        float scale = 1;

        if (countryName.equals(UNITED_STATES)) {
            bitmap = Bitmap.createBitmap(bitmap, 0, 0,
                    bitmap.getWidth() / 3, bitmap.getHeight());
        }

        //如果是竖屏的话
        if (displayMetrics.heightPixels > displayMetrics.widthPixels){
            //如果地图的宽大于高的话
            if (bitmap.getWidth() > bitmap.getHeight()){
                scale = (float) displayMetrics.widthPixels / bitmap.getWidth();
            }else{
                scale = (float) displayMetrics.widthPixels / bitmap.getHeight();
            }
        }else{  //如果是横屏的话
            if (bitmap.getWidth() > bitmap.getHeight()){
                scale = (float) (displayMetrics.heightPixels - TITLE_HEIGHT) / bitmap.getHeight();
            }else{
                scale = (float) (displayMetrics.heightPixels -TITLE_HEIGHT) / bitmap.getHeight();
            }
        }

        Matrix scaler = new Matrix();
        scaler.postScale(scale, scale);

        for(int i = 0; i < plottingData.investigateYear.length; i++){
            if (plottingData.investigateYear[i] == year){
                if (plottingData.counties[i].equals(countryName)){
                    bitmap = MapChart.changeBitmapColor(bitmap, gdpInColor[i]);
                }
            }
        }


        bitmap = MapChart.scaleBitmap(bitmap, scaler);

        mapView.setCountryBitmap(bitmap, countryName, year);
        mapView.setPlottingData(plottingData, gdpInColor);
        //mapView.invalidate();
    }



    public void drawLineChart(LifeExpValuesForPlotting valuesForPlotting, String countryName){
        List<Entry> popuEntries = new ArrayList<Entry>();
        List<Entry> ageEntries = new ArrayList<Entry>();
        List<Entry> gdpEntries = new ArrayList<Entry>();

        //int entrySize = valuesForPlotting.averageLifeTime.length;
        int dataSize = valuesForPlotting.gdp.length;

        String selectedCountry = countryName;

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

        LineDataSet setPopu = new LineDataSet(popuEntries, "人口数(千万)");
        setPopu.setAxisDependency(YAxis.AxisDependency.LEFT);
        setPopu.setColor(Color.BLUE);
        LineDataSet setAge = new LineDataSet(ageEntries, "平均年龄(岁)");
        setAge.setAxisDependency(YAxis.AxisDependency.LEFT);
        setAge.setColor(Color.GREEN);
        LineDataSet setGdp = new LineDataSet(gdpEntries, "人均GDP(百美元)");
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

    private String countryNameToLowerCase(String countryName){
        String[] strArray = countryName.split(" ");
        StringBuilder builder = new StringBuilder();
        for (String s : strArray) {
            String lower = s.substring(0, 1).toLowerCase() + s.substring(1);
            builder.append(lower + "_");
        }
        return builder.toString();
    }
}
