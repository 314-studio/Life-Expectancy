package com.sunbvert.lifeexpectancy;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.JsonReader;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity {

    static final String TAG = "地图图表界面";
    static final String FILE_NAME = "life-expectancy.json";


    MapChart mapChart;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_chart);

        mapChart = (MapChart) findViewById(R.id.map_chart);
    }

    @Override
    protected void onStart(){
        super.onStart();

        try {
            InputStream in = getAssets().open(FILE_NAME);
            JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
            LoadJsonData asyncLoad = new LoadJsonData();
            asyncLoad.execute(reader);
        } catch (IOException e){
            Log.e(TAG, "无法读取到文件" + FILE_NAME);
        }
    }

    //读取JSON的线程类
    private class LoadJsonData extends AsyncTask<JsonReader, Integer, LifeExpectancyValues> {

        @Override
        protected LifeExpectancyValues doInBackground(JsonReader[] reader) {
            LifeExpectancyValues values = new LifeExpectancyValues();
            try{
                values = readObjects(reader[0]);
            } catch (IOException e){
                Log.e(TAG, "错误信息：", e);
            }
            return values;
        }

        @Override
        protected void onPostExecute(LifeExpectancyValues result) {
            //test code
            SplitJsonData asyncSplitData = new SplitJsonData();
            asyncSplitData.execute(result);
            mapChart.setRawData(result);
        }

        private LifeExpectancyValues readObjects(JsonReader reader) throws IOException {
            List<String> counties = null;
            List<Integer> timeLine = null;
            List<List<String>> lifeExpectancyValues = null;

            reader.beginObject();
            while(reader.hasNext()){
                String name = reader.nextName();
                switch (name) {
                    case "counties":
                        counties = readStringArray(reader);
                        break;
                    case "timeline":
                        timeLine = readIntegerArray(reader);
                        break;
                    case "series":
                        lifeExpectancyValues = readThreeDimArray(reader);
                        break;
                }
            }
            reader.endObject();

            return new LifeExpectancyValues(counties, timeLine, lifeExpectancyValues);
        }

        private List<String> readStringArray(JsonReader reader) throws IOException {
            List<String> strings = new ArrayList<String>();

            reader.beginArray();
            while(reader.hasNext()){
                strings.add(reader.nextString());
            }
            reader.endArray();
            return strings;
        }

        private List<Integer> readIntegerArray(JsonReader reader) throws IOException {
            List<Integer> integers = new ArrayList<Integer>();

            reader.beginArray();
            while(reader.hasNext()){
                integers.add(reader.nextInt());
            }
            reader.endArray();
            return integers;
        }

        private List<List<String>> readThreeDimArray(JsonReader reader) throws IOException {
            List<List<String>> lifeExpectancyValues = new ArrayList<List<String>>();

            reader.beginArray();
            while(reader.hasNext()){
                reader.beginArray();
                while(reader.hasNext()){
                    List<String> values = new ArrayList<String>();
                    reader.beginArray();
                    while(reader.hasNext()){
                        values.add(reader.nextString());
                    }
                    reader.endArray();
                    lifeExpectancyValues.add(values);
                }
                reader.endArray();
            }
            reader.endArray();

            return lifeExpectancyValues;
        }
    }


    //分割并二次处理数据的线程类
    private class SplitJsonData extends AsyncTask<LifeExpectancyValues, Integer, LifeExpValuesForPlotting> {

        @Override
        protected LifeExpValuesForPlotting doInBackground(LifeExpectancyValues... lifeExpectancyValues) {
            return splitValues(lifeExpectancyValues[0]);
        }

        @Override
        protected void onPostExecute(LifeExpValuesForPlotting result){
            mapChart.setData(result);
        }

        private LifeExpValuesForPlotting splitValues(LifeExpectancyValues values){
            int dataSize = values.lifeExpectancyValues.size();
            //int valueSize = LifeExpValuesForPlotting.valueSize;  //valueSize = 5

            int[] gdp = new int[dataSize];
            double[] averageLifeTime = new double[dataSize];
            long[] population = new long[dataSize];
            String[] counties = new String[dataSize];
            int[] investigateYear = new int[dataSize];

            int j = 0;
            for (List<String> valueList: values.lifeExpectancyValues) {
                for (int i = 0; i < valueList.size(); i++) {
                    switch (i){
                        case 0:
                            gdp[j] = Integer.parseInt(valueList.get(i));
                            break;
                        case 1:
                            averageLifeTime[j] = Double.parseDouble(valueList.get(i));
                            break;
                        case 2:
                            population[j] = Long.parseLong(valueList.get(i));
                            break;
                        case 3:
                            counties[j] = valueList.get(i);
                            break;
                        case 4:
                            investigateYear[j] = Integer.parseInt(valueList.get(i));
                            break;
                    }
                }
                j++;
            }

            return new LifeExpValuesForPlotting(gdp, averageLifeTime, population, counties, investigateYear);
        }
    }
}
