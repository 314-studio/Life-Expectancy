package com.sunbvert.lifeexpectancy;

import android.content.Intent;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

public class SplitJsonData extends AsyncTask<LifeExpectancyValues, Integer, LifeExpValuesForPlotting> {

    @Override
    protected LifeExpValuesForPlotting doInBackground(LifeExpectancyValues... lifeExpectancyValues) {
        return splitValues(lifeExpectancyValues[0]);
    }

    @Override
    protected void onPostExecute(LifeExpValuesForPlotting result){
        //Intent intent = new Intent(this, DisplayData.class);
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
