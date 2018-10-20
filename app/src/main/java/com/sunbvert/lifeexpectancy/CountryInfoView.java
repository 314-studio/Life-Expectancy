package com.sunbvert.lifeexpectancy;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.support.annotation.ColorInt;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

public class CountryInfoView extends View {
    static int paddingTop = 50;
    static int ONE_HUNDRED_MILLION = 100000000;
    static int POPU_SCALE = 20;

    Bitmap countryBitmap;
    Bitmap popuGraph;
    LifeExpValuesForPlotting plottingData;
    int[] gdpInColor;
    String countryName;
    int year;
    int numOfPopuGraph;
    Paint defaultPaint;

    DisplayMetrics displayMetrics;

    public CountryInfoView(Context context) {
        super(context);
    }

    public CountryInfoView(Context context,  AttributeSet attrs) {
        super(context, attrs);
    }

    public CountryInfoView(Context context,  AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CountryInfoView(Context context,  AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setCountryBitmap(Bitmap countryBitmap, String countryName, int year) {
        this.countryBitmap = countryBitmap;
        this.countryName = countryName;
        this.year = year;
        ViewGroup.LayoutParams lp = this.getLayoutParams();
        lp.height = countryBitmap.getHeight() + paddingTop * 2;
        this.setLayoutParams(lp);
    }

    public void setPlottingData(LifeExpValuesForPlotting plottingData, int[] gdpInColor) {
        this.plottingData = plottingData;
        this.gdpInColor = gdpInColor;

        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        displayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(displayMetrics);

        this.defaultPaint = new Paint();

        //获取小人图片并缩放到合适的大小
        this.popuGraph = BitmapFactory.decodeResource(getResources(), R.mipmap.popu_graph);
        Matrix matrix = new Matrix();
        float scale = (float) displayMetrics.widthPixels / (POPU_SCALE * popuGraph.getHeight());
        matrix.postScale(scale, scale);
        popuGraph = Bitmap.createBitmap(popuGraph, 0, 0,
                popuGraph.getWidth(), popuGraph.getHeight(), matrix, true);

        if (countryName != null){
            //遍历到当前选择的数据条
            for(int i = 0; i < plottingData.investigateYear.length; i++){
                if (plottingData.investigateYear[i] == year){
                    if (plottingData.counties[i].equals(countryName)){
                        //更改国家颜色
                        countryBitmap = MapChart.changeBitmapColor(countryBitmap, gdpInColor[i]);
                        //计算需要画小人的数量
                        this.numOfPopuGraph = (int) (plottingData.population[i] / ONE_HUNDRED_MILLION + 1);
                    }
                }
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas){
        if (countryBitmap != null){
            canvas.drawBitmap(countryBitmap, displayMetrics.widthPixels / 2 - countryBitmap.getWidth() / 2,
                    paddingTop, defaultPaint);
            int firstLineGraphNum = (int) Math.sqrt(numOfPopuGraph * 2);
            int count = 0;
            int lineNum = firstLineGraphNum;
            for (int i = 0; i < firstLineGraphNum; i++){
                for (int j = 0; j < lineNum; j++) {
                    canvas.drawBitmap(popuGraph, j * (popuGraph.getWidth() - 25),
                            i * popuGraph.getHeight(), defaultPaint);
                    count++;
                    if (count >= numOfPopuGraph){
                        count = 0;
                        break;
                    }
                }
                if (count == 0){
                    break;
                }
                lineNum--;
            }
            int bottom = countryBitmap.getHeight() + paddingTop * 2 - 25;
            canvas.drawLine(0, bottom, displayMetrics.widthPixels, bottom, defaultPaint);
        }
    }

    private @ColorInt int getColor(String countryName, int year){
        int color = 0;
        //依据年份改变国家颜色
        for(int i = 0; i < plottingData.investigateYear.length; i++){
            if (plottingData.investigateYear[i] == year){
                if (plottingData.counties[i].equals(countryName)){
                   color = gdpInColor[i];
                }
            }
        }
        return color;
    }
}
