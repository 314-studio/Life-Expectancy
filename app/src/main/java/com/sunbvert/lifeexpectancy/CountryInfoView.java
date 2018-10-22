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

import java.util.ArrayList;
import java.util.List;

public class CountryInfoView extends View {
    static String TAG = "Country info: ";

    private static final int paddingTop = 50;
    private static final int ONE_HUNDRED_MILLION = 100000000;
    private static final int POPU_SCALE = 20;
    private static final int GLOBAL_ANIMATION_CTRL = 1000;
    private static final int GLOBAL_ANIMATION_DURE = 100000;

    Bitmap countryBitmap;
    Bitmap popuGraph;
    LifeExpValuesForPlotting plottingData;
    int[] gdpInColor;
    String countryName;
    int year;
    int numOfPopuGraph;
    Paint defaultPaint;

    DisplayMetrics displayMetrics;
    int animatedNumOfGraph = 1;
    float colorScale = 1;

    List<PopuAnimationUnit> popuAnimUnits;

    ValueAnimator globalAnimator;
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

        popuAnimUnits = new ArrayList<PopuAnimationUnit>();
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

        colorScale = 1 / (float) numOfPopuGraph;

        //创建补间动画
        globalAnimator = ValueAnimator.ofInt(0, GLOBAL_ANIMATION_CTRL);
        popuGraphNumAnim = ValueAnimator.ofInt(1, numOfPopuGraph);
        setAnimation();
    }

    boolean animationBegin = false;
    //设置动画动作
    private void setAnimation(){
        globalAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (popuAnimUnits.size() < animatedNumOfGraph) {
                    PopuAnimationUnit unit = new PopuAnimationUnit(popuGraph, animatedNumOfGraph * colorScale);
                    unit.setAnimationStart();
                    popuAnimUnits.add(unit);
                }
                Log.d(TAG, "全局动画运行返回值：" + animation.getAnimatedValue());
                invalidate();
            }
        });
        globalAnimator.setDuration(GLOBAL_ANIMATION_DURE);

        popuGraphNumAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                animatedNumOfGraph = (int) animation.getAnimatedValue();
                Log.d(TAG, "小人数量动画返回值：" + animatedNumOfGraph);
            }
        });
        popuGraphNumAnim.setDuration(numOfPopuGraph * 10000);
    }

    @Override
    protected void onDraw(Canvas canvas){
        //第一次运行时设置动画开始
        if (!animationBegin){
            globalAnimator.start();
            popuGraphNumAnim.start();
        }

        if (countryBitmap != null) {
            canvas.drawBitmap(countryBitmap, displayMetrics.widthPixels / 2 - countryBitmap.getWidth() / 2,
                    paddingTop, defaultPaint);
            int firstLineGraphNum = (int) Math.sqrt(animatedNumOfGraph * 2);
            int count = 0;
            int lineNum = firstLineGraphNum;
            for (int i = 0; i < firstLineGraphNum; i++) {
                for (int j = 0; j < lineNum; j++) {
                    PopuAnimationUnit unit = popuAnimUnits.get(count);
                    canvas.drawBitmap(unit.getAnimatedPopuGraph(), j * (unit.getAnimatedPopuGraph().getWidth() - 25),
                            i * unit.getAnimatedPopuGraph().getHeight(), defaultPaint);
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
