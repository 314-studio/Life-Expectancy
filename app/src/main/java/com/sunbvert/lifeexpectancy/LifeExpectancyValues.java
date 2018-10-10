package com.sunbvert.lifeexpectancy;

import java.util.ArrayList;
import java.util.List;

public class LifeExpectancyValues {
    static String valueFormat = "[815,34.05,351014,\"Australia\",1800]";

    public List<String> counties;
    public List<Integer> timeLine;
    public List<List<String>> lifeExpectancyValues;

    public LifeExpectancyValues() {
        counties = new ArrayList<>();
        timeLine = new ArrayList<>();
        lifeExpectancyValues = new ArrayList<>();
    }

    public LifeExpectancyValues(List<String> counties, List<Integer> timeLine, List<List<String>> lifeExpectancyValues){
        this.counties = counties;
        this.timeLine = timeLine;
        this.lifeExpectancyValues = lifeExpectancyValues;
    }

    public List<Integer> getTimeLineList() {
        return timeLine;
    }

    public List<String> getCountieslist() {
        return counties;
    }
}
