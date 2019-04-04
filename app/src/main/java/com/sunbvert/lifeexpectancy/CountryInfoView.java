package com.sunbvert.lifeexpectancy;

import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.support.annotation.ColorInt;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class CountryInfoView extends View {
    static String TAG = "Country info: ";

    private static final int paddingTop = 50;
    private static final int ONE_HUNDRED_MILLION = 100000000;
    private static final int POPU_SCALE = 20;
    private static final int GLOBAL_ANIMATION_CTRL = 100;
    private static final int GLOBAL_ANIMATION_DURE = 10000;
    private static final float TEXT_SIZE = 50f;

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

    Paint textPaint;
    private Paint colorRulerPaint;
    private Paint rulerLinePaint;
    private Paint rulerTextPaint;
    private Paint rulerTipPaint;


    String[] timeLine;
    AlertDialog alertDialog;
    ListView yearListView;
    ArrayAdapter<String> adapter;

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

        //设置年份字体画笔
        textPaint = new Paint();
        textPaint.setTextSize(50f);
    }

    public void setTimeLine(final int[] timeLine, View dialogView){
        this.timeLine = new String[timeLine.length];
        for (int i = 0; i < timeLine.length; i++) {
            this.timeLine[i] = String.valueOf(timeLine[i]);
        }

        adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, this.timeLine);
        alertDialog = new AlertDialog.Builder(this.getContext()).setTitle("选择年份")
                .setView(dialogView).create();

        yearListView = (ListView) dialogView.findViewById(R.id.year_list);
        yearListView.setAdapter(adapter);
        yearListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                year = timeLine[position];
                alertDialog.dismiss();
                redraw();
                //Toast.makeText(getContext(),"year: " + timeLine[position], Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void setPlottingData(LifeExpValuesForPlotting plottingData, int[] gdpInColor) {
        this.plottingData = plottingData;
        this.gdpInColor = gdpInColor;

        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        displayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(displayMetrics);

        this.defaultPaint = new Paint();

        //获取小人图片并缩放到合适的大小
        this.popuGraph = BitmapFactory.decodeResource(getResources(), R.mipmap.popu_graph_white);
        Matrix matrix = new Matrix();
        float scale = (float) displayMetrics.widthPixels / (POPU_SCALE * popuGraph.getHeight());
        matrix.postScale(scale, scale);
        popuGraph = Bitmap.createBitmap(popuGraph, 0, 0,
                popuGraph.getWidth(), popuGraph.getHeight(), matrix, true);

        redraw();

        setAnimation();
        /*
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
        */
    }

    //用于记录当前年份的数据
    private int gdp = 0;
    private long population = 0;
    private double lifeTime = 0;

    private void redraw(){
        popuAnimUnits.clear();

        if (countryName != null){
            //遍历到当前选择的数据条
            for(int i = 0; i < plottingData.investigateYear.length; i++){
                if (plottingData.investigateYear[i] == year){
                    if (plottingData.counties[i].equals(countryName)){
                        //更改国家颜色
                        countryBitmap = MapChart.changeBitmapColor(countryBitmap, gdpInColor[i]);
                        //计算需要画小人的数量
                        this.numOfPopuGraph = (int) (plottingData.population[i] / ONE_HUNDRED_MILLION + 1);
                        gdp = plottingData.gdp[i];
                        population = plottingData.population[i];
                        lifeTime = plottingData.averageLifeTime[i];
                    }
                }
            }
        }

        colorScale = 1 / (float) numOfPopuGraph;

        //创建补间动画
        globalAnimator = ValueAnimator.ofInt(0, GLOBAL_ANIMATION_CTRL);
        popuGraphNumAnim = ValueAnimator.ofInt(1, numOfPopuGraph);
        animationBegin = false;
        animatedNumOfGraph = 1;
        invalidate();
        setAnimation();
    }

    boolean animationBegin = false;
    //设置动画动作
    private void setAnimation(){
        globalAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                //Log.d(TAG, "全局动画运行返回值：" + animation.getAnimatedValue());
                invalidate();
            }
        });
        globalAnimator.setDuration(GLOBAL_ANIMATION_DURE);

        popuGraphNumAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                animatedNumOfGraph = (int) animation.getAnimatedValue();
                if (popuAnimUnits.size() < animatedNumOfGraph) {
                    PopuAnimationUnit unit = new PopuAnimationUnit(CountryInfoView.this,
                            popuGraph, animatedNumOfGraph * colorScale);
                    Log.d(TAG, "colorScale: " + colorScale + ", num * scale: " + animatedNumOfGraph * colorScale);
                    unit.setAnimationStart();
                    popuAnimUnits.add(unit);
                }
                //Log.d(TAG, "小人数量动画返回值：" + animatedNumOfGraph);
            }
        });
        popuGraphNumAnim.setDuration(numOfPopuGraph * 250);
    }

    @Override
    protected void onDraw(Canvas canvas){
        //第一次运行时设置动画开始
        if (!animationBegin){
            globalAnimator.start();
            popuGraphNumAnim.start();
            animationBegin = true;
        }

        if (countryBitmap != null) {
            canvas.drawBitmap(countryBitmap, displayMetrics.widthPixels / 2 - countryBitmap.getWidth() / 2,
                    paddingTop, defaultPaint);
            int firstLineGraphNum = (int) Math.sqrt(numOfPopuGraph * 2);
            int count = 0;
            int lineNum = firstLineGraphNum;
            for (int i = 0; i < firstLineGraphNum; i++) {
                for (int j = 0; j < lineNum; j++) {
                    if (popuAnimUnits.size() != 0) {
                        if (popuAnimUnits.size() <= animatedNumOfGraph) {
                            PopuAnimationUnit unit = popuAnimUnits.get(count);
                            canvas.drawBitmap(unit.getAnimatedPopuGraph(), j * (unit.getAnimatedPopuGraph().getWidth() - 25),
                                    i * unit.getAnimatedPopuGraph().getHeight(), defaultPaint);
                            count++;

                            if (count >= animatedNumOfGraph) {
                                count = 0;
                                break;
                            }
                        }
                    }
                }
                if (count == 0) {
                    break;
                }
                lineNum--;
            }

            //画当前年份
            canvas.drawText(String.valueOf(year) + "年", displayMetrics.widthPixels - 200, 50, textPaint);
        }

        int bottom = countryBitmap.getHeight() + paddingTop * 2 - 25;
        canvas.drawText(countryName, displayMetrics.widthPixels / 2 - 50, displayMetrics.heightPixels / 2 + paddingTop, textPaint);
        canvas.drawText("人均GDP：" + gdp + "美元", 0, bottom - 125, textPaint);
        canvas.drawText("人口数量：" + population + "人", 0, bottom - 75, textPaint);
        canvas.drawText("平均年龄：" + lifeTime + "岁", 0, bottom - 25, textPaint);
        canvas.drawLine(0, bottom, displayMetrics.widthPixels, bottom, defaultPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        int x = (int) event.getX();
        int y = (int) event.getY();
        if (event.getAction() == MotionEvent.ACTION_UP){
            if (x > displayMetrics.widthPixels - 200 && x < displayMetrics.widthPixels
                    && y > 0 && y < 50){
                alertDialog.show();
            }
        }
        return true;
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
