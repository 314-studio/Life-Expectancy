package com.sunbvert.lifeexpectancy;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.support.annotation.ColorInt;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

public class CountryInfoView extends View {
    static String TAG = "Country info: ";

    static int paddingTop = 50;
    static int ONE_HUNDRED_MILLION = 100000000;
    static int POPU_SCALE = 20;

    Bitmap countryBitmap;
    Bitmap popuGraph;
    Bitmap scaledPopuGraph;
    LifeExpValuesForPlotting plottingData;
    int[] gdpInColor;
    String countryName;
    int year;
    int numOfPopuGraph;
    Paint defaultPaint;

    DisplayMetrics displayMetrics;
    int animatedNumOfGraph = 1;
    Bitmap animatedPopuGraphOne;

    ValueAnimator popuGraphScaleAnimOne;
    ValueAnimator popuGraphNumAnim;

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
        scaledPopuGraph = Bitmap.createBitmap(popuGraph, 0, 0,
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

        //创建补间动画
        popuGraphScaleAnimOne = ValueAnimator.ofFloat(scale * 0.5f, scale);
        popuGraphNumAnim = ValueAnimator.ofInt(1, numOfPopuGraph);
        Log.d(TAG, "initial scale = " + scale);

        setAnimation();
    }

    boolean animationBegin = false;
    //设置动画动作
    private void setAnimation(){
        popuGraphNumAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                animatedNumOfGraph = (int) animation.getAnimatedValue();
                invalidate();
            }
        });
        popuGraphNumAnim.setDuration(numOfPopuGraph * 300);

        popuGraphScaleAnimOne.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float animatedScale = (float) animation.getAnimatedValue();
                Log.d(TAG, "animated scale = " + animatedScale);

                animatedNumOfGraph = numOfPopuGraph;

                Matrix matrix = new Matrix();
                matrix.postScale(animatedScale, animatedScale);
                animatedPopuGraphOne = Bitmap.createBitmap(popuGraph, 0, 0, popuGraph.getWidth(),
                        popuGraph.getHeight(), matrix, true);
                invalidate();
            }
        });
        popuGraphScaleAnimOne.setDuration(300);
    }

    @Override
    protected void onDraw(Canvas canvas){
        if (!animationBegin){
            animationBegin = true;
            popuGraphScaleAnimOne.start();
        }

        if (!popuGraphScaleAnimOne.isRunning()){
            animationBegin = false;
        }

        if (countryBitmap != null){
            canvas.drawBitmap(countryBitmap, displayMetrics.widthPixels / 2 - countryBitmap.getWidth() / 2,
                    paddingTop, defaultPaint);
            if (animatedPopuGraphOne != null) {
                int firstLineGraphNum = (int) Math.sqrt(animatedNumOfGraph * 2);
                int count = 0;
                int lineNum = firstLineGraphNum;
                for (int i = 0; i < firstLineGraphNum; i++) {
                    for (int j = 0; j < lineNum; j++) {
                        canvas.drawBitmap(animatedPopuGraphOne, j * (animatedPopuGraphOne.getWidth() - 25),
                                i * animatedPopuGraphOne.getHeight(), defaultPaint);
                        count++;

                        if (count >= animatedNumOfGraph) {
                            count = 0;
                            break;
                        }
                    }
                    if (count == 0) {
                        break;
                    }
                    lineNum--;
                }
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
