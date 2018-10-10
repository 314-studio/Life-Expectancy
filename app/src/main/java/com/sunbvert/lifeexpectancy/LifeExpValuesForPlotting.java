package com.sunbvert.lifeexpectancy;

public class LifeExpValuesForPlotting {
    public static int valueSize = 5;
    //static String valueFormat = "[815,34.05,351014,\"Australia\",1800]";

    public int[] gdp;
    public double[] averageLifeTime;
    public long[] population;
    public String[] counties;
    public int[] investigateYear;

    public LifeExpValuesForPlotting(int size){
        gdp = new int[size];
        averageLifeTime = new double[size];
        population = new long[size];
        counties = new String[size];
        investigateYear = new int[size];
    }

    public LifeExpValuesForPlotting(
            int[] gdp,
            double[] averageLifeTime,
            long[] population,
            String[] counties,
            int[] investigateYear){
        this.gdp = gdp;
        this.averageLifeTime = averageLifeTime;
        this.population = population;
        this.investigateYear = investigateYear;
        this.counties = counties;
    }
}
