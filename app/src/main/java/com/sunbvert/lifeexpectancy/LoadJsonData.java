package com.sunbvert.lifeexpectancy;

import android.content.Context;
import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LoadJsonData extends AsyncTask<JsonReader, Integer, LifeExpectancyValues> {

    static String TAG = "线程：读取JOSN数据" ;

    private Context content;

    public LoadJsonData(){
        //this.content = content;
    }

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
