package com.sunbvert.lifeexpectancy;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.JsonReader;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class MapChart extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    public static String GDP = "gdp";
    public static String AVERAGE_LIFE_TIME = "averageLifeTime";
    public static String POPULATION = "population";
    public static String COUNTRIES = "countries";
    public static String INVESTIGATE_YEAR = "investigate_year";
    public static String TIME_LINE = "time_line";

    static String TAG = "地图图表";
    static String FILE_NAME = "relative-pos.json";
    static String WORLD_MAP = "World Map";
    static int DURATION_ONE_YEAR = 30;

    LifeExpValuesForPlotting plottingData;
    LifeExpectancyValues rawData;

    Context context;

    Map<String, Bitmap> countriesBitmap;
    Map<String, double[]> countriesPos;
    Map<String, Bitmap> scaledCountriesBitmaps;
    Bitmap popuGraph;

    int[] gdpInColor;

    private SurfaceHolder mHolder;
    private Canvas mCanvas;
    private boolean mIsDrawing;

    int bgWidth;
    int bgHeight;

    int horizontalOffset = 0;

    float popuGraphScale = 0;

    public MapChart(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public MapChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public MapChart(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    public MapChart(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;
        init();
    }

    private void init(){
        mHolder = getHolder();
        mHolder.addCallback(this);
        setFocusable(true);
        setFocusableInTouchMode(true);
        this.setKeepScreenOn(true);

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
         wm.getDefaultDisplay().getMetrics(displayMetrics);

         //获取绘图所需的资源
        countriesBitmap = loadCountriesBitmap();
        countriesPos = loadRelativePos(getReader(FILE_NAME));
        popuGraph = BitmapFactory.decodeResource(getResources(), R.mipmap.popu_graph);

        Matrix scaler = new Matrix();
        //根据屏幕宽和高决定缩放比例填充屏幕
        if (displayMetrics.heightPixels - displayMetrics.widthPixels > 0){
            float scale = (float) displayMetrics.widthPixels / countriesBitmap.get(WORLD_MAP).getWidth();
            scaler.postScale(scale, scale);
            horizontalOffset = 0;
        }else {
            float scale = (float) (displayMetrics.heightPixels - 9 * TEXT_SIZE) / countriesBitmap.get(WORLD_MAP).getHeight();
            scaler.postScale(scale, scale);
        }

        //依据屏幕大小缩放图片
        //popuGraph = Bitmap.createBitmap(popuGraph, 0, 0, popuGraph.getWidth(), popuGraph.getHeight(), scaler, true);
        scaledCountriesBitmaps = scaleCountriesBitmap(countriesBitmap, scaler);
        Bitmap bg = scaledCountriesBitmaps.get(WORLD_MAP);
        this.bgWidth = bg.getWidth();
        this.bgHeight = bg.getHeight();

        //如果横屏的话，计算在屏幕中央显示所需的偏差值
        if (displayMetrics.heightPixels - displayMetrics.widthPixels < 0) {
            horizontalOffset = displayMetrics.widthPixels / 2 - bgWidth / 2;
        }
    }

    private Map<String, Bitmap> loadCountriesBitmap(){
        Resources res = getResources();
        Map<String, Bitmap> bitmaps = new HashMap<String, Bitmap>();

        Bitmap australia = BitmapFactory.decodeResource(res, R.mipmap.australia);
        Bitmap canada = BitmapFactory.decodeResource(res, R.mipmap.canada);
        Bitmap china = BitmapFactory.decodeResource(res, R.mipmap.china);
        Bitmap cuba = BitmapFactory.decodeResource(res, R.mipmap.cuba);
        Bitmap finland = BitmapFactory.decodeResource(res, R.mipmap.finland);
        Bitmap france = BitmapFactory.decodeResource(res, R.mipmap.france);
        Bitmap germany = BitmapFactory.decodeResource(res, R.mipmap.germany);
        Bitmap iceland = BitmapFactory.decodeResource(res, R.mipmap.iceland);
        Bitmap india = BitmapFactory.decodeResource(res, R.mipmap.india);
        Bitmap japan = BitmapFactory.decodeResource(res, R.mipmap.japan);
        Bitmap newZealand = BitmapFactory.decodeResource(res, R.mipmap.new_zealand);
        Bitmap northKorea = BitmapFactory.decodeResource(res, R.mipmap.north_korea);
        Bitmap norway = BitmapFactory.decodeResource(res, R.mipmap.norway);
        Bitmap poland = BitmapFactory.decodeResource(res, R.mipmap.poland);
        Bitmap russia = BitmapFactory.decodeResource(res, R.mipmap.russia);
        Bitmap southKorea = BitmapFactory.decodeResource(res, R.mipmap.south_korea);
        Bitmap turkey = BitmapFactory.decodeResource(res, R.mipmap.turkey);
        Bitmap unitedKingdom = BitmapFactory.decodeResource(res, R.mipmap.united_kingdom);
        Bitmap unitedStates = BitmapFactory.decodeResource(res, R.mipmap.united_states);
        Bitmap countries = BitmapFactory.decodeResource(res, R.mipmap.countries);

        bitmaps.put("Australia", australia);
        bitmaps.put("Canada", canada);
        bitmaps.put("China", china);
        bitmaps.put("Cuba", cuba);
        bitmaps.put("Finland", finland);
        bitmaps.put("France", france);
        bitmaps.put("Germany", germany);
        bitmaps.put("Iceland", iceland);
        bitmaps.put("India", india);
        bitmaps.put("Japan", japan);
        bitmaps.put("New Zealand", newZealand);
        bitmaps.put("North Korea", northKorea);
        bitmaps.put("Norway", norway);
        bitmaps.put("Poland", poland);
        bitmaps.put("Russia", russia);
        bitmaps.put("South Korea", southKorea);
        bitmaps.put("Turkey", turkey);
        bitmaps.put("United Kingdom", unitedKingdom);
        bitmaps.put("United States", unitedStates);
        bitmaps.put("World Map", countries);

        return bitmaps;
    }

    private Map<String, Bitmap> scaleCountriesBitmap(Map<String, Bitmap> bitmaps, Matrix scaler){
        Map<String, Bitmap> scaledBitmaps = new HashMap<String, Bitmap>();
        for (String key: bitmaps.keySet()){
            scaledBitmaps.put(key, scaleBitmap(bitmaps.get(key),scaler));
        }
        return scaledBitmaps;
    }

    private Bitmap scaleBitmap(Bitmap bitmap, Matrix scaler){
        return Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), scaler, true);
    }

    static int GENERALIZATION = 100000;
    public void setData(LifeExpValuesForPlotting plottingData) {
        this.plottingData = plottingData;
        this.gdpInColor = constructGdpColorArray(plottingData.gdp, Color.RED, Color.GREEN);

        long max = 0;
        for (int i = 0; i < plottingData.population.length; i++){
            if (plottingData.population[i] > max){
                max = plottingData.population[i];
            }
        }
        double f = (double)(max / GENERALIZATION);
        if (f <= 1){
            f = 2;
        }
        this.popuGraphScale = (float) bgHeight / (8 * (float)Math.log(f));
        Log.d(TAG, "Population Graph scale: " + popuGraphScale);
    }

    private int timeLineSize = 0;
    public void setRawData(LifeExpectancyValues rawData){
        this.rawData = rawData;
        this.timeLineSize = rawData.timeLine.size();

        for (int i = 0; i < yearsInRegulator.length; i++){
            yearsInRegulator[i] = rawData.timeLine.get(i);
        }
        currentSelectedYearIndex = 3;
    }

    private JsonReader getReader(String fileName) {
        JsonReader reader = null;
        try {
            InputStream in = context.getAssets().open(fileName);
            reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        } catch (IOException e){
            Log.e(TAG, "无法读取到文件" + fileName);
            //todo: 添加警示框
        }
        return reader;
    }

    private Map<String, double[]> loadRelativePos(JsonReader reader) {
        Map<String, double[]> relativePos = new HashMap<>();

        try {
            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();

                reader.beginArray();
                double[] pos = new double[2];
                int j = 0;
                while (reader.hasNext()) {
                    pos[j] = reader.nextDouble();
                    j++;
                }
                reader.endArray();

                relativePos.put(name, pos);
            }
            reader.endObject();
        } catch (IOException e){
            Log.e(TAG, "解析文件失败" + FILE_NAME);
        }

        return relativePos;
    }


    //定义一些经常用到的画笔
    Paint defaultPaint;
    Paint[] textPaint;
    Paint clearPaint;

    static int margin = 100;
    static int numOfYear = 5;   //同时在年份选择器上绘制的年份
    static int TEXT_SIZE = 20;
    int[] yearsInRegulator;
    int currentSelectedYearIndex = 0;
    int interval;

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        defaultPaint = new Paint();
        textPaint = new Paint[numOfYear];
        //初始化不同大小的字体画笔
        for (int i = 0; i < (numOfYear / 2) + 1; i++){
            Paint paint = new Paint();
            paint.setTextSize(i * 5 + TEXT_SIZE);
            textPaint[i] = paint;
        }
        for (int i = numOfYear - 1; i > numOfYear / 2; i--){
            Paint paint = new Paint();
            paint.setTextSize((-i + 4) * 5 + TEXT_SIZE);
            textPaint[i] = paint;
        }

        interval = (bgWidth - margin * 2) / (numOfYear - 1);
        yearsInRegulator = new int[numOfYear];

        clearPaint = new Paint();
        clearPaint.setColor(Color.WHITE);

        //将画布初始化为白色
        mCanvas = mHolder.lockCanvas();
        mCanvas.drawColor(Color.WHITE);
        mHolder.unlockCanvasAndPost(mCanvas);

        mIsDrawing = true;
        new Thread(this).start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mIsDrawing = false;
    }

    int lastMoveX = 0;
    int lastXDis = 0;   //存放每次X方向手指移动的距离
    int offset = 0;
    boolean rulerTouched = false;
    float rulerTouchEndSpeed = 0;
    long lastMoveTime = 0;
    int lastTimeDelay = 0;
    @Override
    public boolean onTouchEvent(MotionEvent event){
        int x = (int) event.getX();
        int y = (int) event.getY();

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                if (y > bgHeight - 3 * TEXT_SIZE && y < bgHeight + 5 * TEXT_SIZE){
                    rulerTouched = true;
                    lastMoveX = x;
                    lastMoveTime = System.currentTimeMillis();
                }
                Log.d(TAG, "开始响应触摸事件");
                break;
            case MotionEvent.ACTION_MOVE:
                if (rulerTouched){
                    offset += x - lastMoveX;
                }

                lastXDis = x - lastMoveX;   //记录每次移动的距离
                lastMoveX = x;
                lastTimeDelay = (int) (System.currentTimeMillis() - lastMoveTime);  //记录每次移动的时间
                lastMoveTime = System.currentTimeMillis();
                break;
            case MotionEvent.ACTION_UP:
                if (rulerTouched) {
                    rulerTouched = false;
                    rulerTouchEndSpeed = (float) lastXDis / lastTimeDelay;  //触摸结束后标尺的速度

                    //限制标尺的速度，防止产生空白画面
                    if (rulerTouchEndSpeed > 10){
                        rulerTouchEndSpeed = 10;
                    }else if (rulerTouchEndSpeed < -10){
                        rulerTouchEndSpeed = -10;
                    }

                    Log.d(TAG, "当前绘图年份：" + currentSelectedYearIndex
                            + " 结束速度：" + rulerTouchEndSpeed + "， 间隔时间：" + lastTimeDelay);
                    Log.d(TAG, " 与上次X的间距：" + lastXDis);

                    lastMoveX = 0;
                }

                detectTouchOnCountry(x, y);
                break;
        }
        return true;
    }

    private void detectTouchOnCountry(int x, int y){
        for (int i = 0; i < rawData.counties.size(); i++){
            String countryName = rawData.counties.get(i);
            int posX = (int)(bgWidth * countriesPos.get(countryName)[0]);
            int posY = (int)(bgHeight * countriesPos.get(countryName)[1]);

            Bitmap bitmap = scaledCountriesBitmaps.get(countryName);
            if (x > posX && x < posX + bitmap.getWidth()){
                if (y > posY && y < posY + bitmap.getHeight()){
                    if (bitmapHasColorAt(bitmap, x - posX, y - posY)){
                        Log.d(TAG, "country name: " + countryName + ", x: " + x + ", y: " + y + ", x - posX = " + (x - posX) + ", y - posY = " + (y - posY));

                        int[] timeLine = new int[rawData.timeLine.size()];
                        for (int j = 0; j < rawData.timeLine.size(); j++){
                            timeLine[i] = rawData.timeLine.get(i);
                        }

                        Intent intent = new Intent(getContext(), DisplayData.class);
                        intent.putExtra(MapActivity.COUNTRY_NAME, countryName);
                        intent.putExtra(GDP, this.plottingData.gdp);
                        intent.putExtra(TIME_LINE, timeLine);
                        intent.putExtra(INVESTIGATE_YEAR, plottingData.investigateYear);
                        intent.putExtra(AVERAGE_LIFE_TIME, plottingData.averageLifeTime);
                        intent.putExtra(POPULATION, plottingData.population);
                        intent.putExtra(COUNTRIES, plottingData.counties);

                        getContext().startActivity(intent);
                    }
                }
            }
        }
    }

    private boolean bitmapHasColorAt(Bitmap bitmap, int x, int y){
        int clr = bitmap.getPixel(x, y);
        if (clr != 0){
            return true;
        }
        return false;
    }

    @Override
    public void run() {

        while (mIsDrawing){
            long startTime = System.currentTimeMillis();

            //线程开启时还没有rawData数据，所以需要判断rawData是否为空
            if (rawData != null && plottingData != null) {
                //只有当前选定的年份在安全区域内才作画
                draw(rawData.timeLine.get(currentSelectedYearIndex));
                Log.d(TAG, "SelectedYear: " + currentSelectedYearIndex + ", offset: " +
                        offset + ", Speed: " + rulerTouchEndSpeed +  ", rulerTouched: " + rulerTouched);
            }

            long endTime = System.currentTimeMillis();
            int diffTime = (int)(endTime - startTime);

            //控制标尺的惯性移动
            if (!rulerTouched){
                if (rulerTouchEndSpeed != 0) {
                    offset += diffTime * rulerTouchEndSpeed;
                }
            }

            //控制画面刷新率
            while(diffTime <= DURATION_ONE_YEAR){
                diffTime = (int)(System.currentTimeMillis() - startTime);
                Thread.yield();
            }
        }
    }

    private void draw(int year){
        try {
            mCanvas = mHolder.lockCanvas();
            mCanvas.drawColor(Color.WHITE);
            drawSingleFrame(mCanvas, year);
            drawYearRegulator(mCanvas);
            drawPopulationGraph(mCanvas, year);
        }catch (Exception e){
            Log.e(TAG, "绘制线程出错：", e);
        }finally {
            if (mCanvas != null){
                mHolder.unlockCanvasAndPost(mCanvas);
            }
        }

    }

    private void drawPopulationGraph(Canvas canvas, int year){
        for(int i = 0; i < plottingData.population.length; i++){
            if (plottingData.investigateYear[i] == year){
                String countryName = plottingData.counties[i];
                long countryPopu = plottingData.population[i];
                Bitmap countryBitmap = scaledCountriesBitmaps.get(countryName);
                double f = countryPopu / GENERALIZATION;
                if (f <= 1){
                    f = 2;
                }
                float scale = (popuGraphScale *  (float) Math.log(f)) / popuGraph.getHeight();
                //Log.d(TAG, countryName + ", popuGraphScale: " + popuGraphScale + ", f: " + f);
                //Log.d(TAG, "scale: " + scale);
                Matrix matrix = new Matrix();
                matrix.postScale(scale, scale);
                Bitmap scaledBitmap = Bitmap.createBitmap(popuGraph, 0, 0,
                        popuGraph.getWidth(),
                        popuGraph.getHeight(),
                        matrix, true);

                if (countryName.equals("United States")){
                    canvas.drawBitmap(scaledBitmap,
                            (float) countriesPos.get(countryName)[0] * bgWidth + horizontalOffset - bgWidth / 4
                                    + countryBitmap.getWidth() / 2  - scaledBitmap.getWidth() / 2,
                            (float) countriesPos.get(countryName)[1] * bgHeight + 50
                                    + countryBitmap.getHeight() / 2 - scaledBitmap.getHeight() / 2,
                            new Paint());
                }else {
                    canvas.drawBitmap(scaledBitmap,
                            (float) countriesPos.get(countryName)[0] * bgWidth + horizontalOffset
                                    + countryBitmap.getWidth() / 2 - scaledBitmap.getWidth() / 2,
                            (float) countriesPos.get(countryName)[1] * bgHeight
                                    + countryBitmap.getHeight() / 2 - scaledBitmap.getHeight() / 2,
                            new Paint());
                }
            }
        }
    }

     //画出每一帧的国家颜色变化
    private void drawSingleFrame(Canvas canvas, int year){
        canvas.drawBitmap(scaledCountriesBitmaps.get(WORLD_MAP), horizontalOffset, 0, defaultPaint);
        for(int i = 0; i < plottingData.gdp.length; i++){
            if (plottingData.investigateYear[i] == year){
                String countryName = plottingData.counties[i];
                Bitmap countryBitmap = scaledCountriesBitmaps.get(countryName);
                Bitmap coloredBitmap = changeBitmapColor(countryBitmap, gdpInColor[i]);

                if (coloredBitmap != null) {
                    canvas.drawBitmap(coloredBitmap,
                            (float) countriesPos.get(countryName)[0] * bgWidth + horizontalOffset,
                            (float) countriesPos.get(countryName)[1] * bgHeight,
                            new Paint());
                }
            }
        }
    }

    static float resistance = 0.2f;
    //画出每一帧年份标尺的变化
    //注意该函数每秒运行30次
    private void drawYearRegulator(Canvas canvas){
        //如果超出年份边界的话，重置当前选择的年份，偏移量和速度
        if (currentSelectedYearIndex < 2) {
            currentSelectedYearIndex = 2;
            offset = 0;
            rulerTouchEndSpeed = 0;
        } else if (currentSelectedYearIndex > timeLineSize - 4){
            currentSelectedYearIndex = timeLineSize - 4;
            offset = 0;
            rulerTouchEndSpeed = 0;
        }

        //如果offset小于0，说明年份在增大，如果偏移量大于年份之间的间距，则重置offset，然后增加一年
        if(offset < 0){
            if (Math.abs(offset) >= interval){
                offset += interval;
                currentSelectedYearIndex++;
                for (int i = 0; i < yearsInRegulator.length; i++){
                    yearsInRegulator[i] = rawData.timeLine.get(i + currentSelectedYearIndex - 2);
                }
            }
        }else{
            if (offset >= interval){
                offset -= interval;
                currentSelectedYearIndex--;
                for (int i = 0; i < yearsInRegulator.length; i++){
                    yearsInRegulator[i] = rawData.timeLine.get(i + currentSelectedYearIndex - 2);
                }
            }
        }

        if (!rulerTouched && rulerTouchEndSpeed != 0){
            //如果速度大于0，说明年份在向前移动，则年份在减小
            if (rulerTouchEndSpeed >= 0){
                //为标尺添加阻力
                rulerTouchEndSpeed -= resistance;
                //如果标尺的速度足够小了，就停止移动
                if (rulerTouchEndSpeed < 0){
                    rulerTouchEndSpeed = 0;
                    offset = 0;
                }
            }else{   //如果速度小于0，说明年份在向后移动，则年份在增加
                rulerTouchEndSpeed += resistance;
                if (rulerTouchEndSpeed > 0){
                    rulerTouchEndSpeed = 0;
                    offset = 0;
                }
            }
        }

        //画出年份标尺中的上下两条线
        canvas.drawLine(margin + horizontalOffset, bgHeight,
                bgWidth - margin + horizontalOffset, bgHeight, defaultPaint);
        //每次从新画之前清空这一部分画布
        canvas.drawRect(0, bgHeight + TEXT_SIZE, bgWidth, bgHeight + TEXT_SIZE * 2, clearPaint);

        //把目前放在yearsInRegulator数组里的年份画出来
        for (int i = 0; i < yearsInRegulator.length; i++){
            canvas.drawText(String.valueOf(yearsInRegulator[i]),
                    margin + interval * i - TEXT_SIZE + offset + horizontalOffset,
                    bgHeight + TEXT_SIZE * 2 - Math.abs(i - 2) * 1.5f,
                    textPaint[i]);
        }

        canvas.drawLine(margin + horizontalOffset, bgHeight + TEXT_SIZE * 3,
                bgWidth - margin + horizontalOffset, bgHeight + TEXT_SIZE * 3, defaultPaint);

    }

    //将gdp数据分析为颜色，然后将颜色存放在一个和gdp一样大小的数组中并一一对应
    private int[] constructGdpColorArray(int[] gdp, @ColorInt int startColor, @ColorInt int endColor){
        int max = 0;
        int min = gdp[0];

        for (int i: gdp) {
            if (i > max){
                max = i;
            }
            if (i < min){
                min = i;
            }
        }

        int highestDiff = max - min;
        int[] gdpInColor = new int[gdp.length];

        for (int j = 0; j < gdp.length; j++) {
            float fraction = (float) gdp[j] / highestDiff;
            gdpInColor[j] = getColorGradient(fraction, startColor, endColor);
        }

        return gdpInColor;
    }

    //将给定的Bitmap的不透明区域填充为给定的颜色
    private Bitmap changeBitmapColor(Bitmap inBitmap, Integer dstColor){
        if (inBitmap == null) {
            return null;
        }

        Bitmap outBitmap = Bitmap.createBitmap (inBitmap.getWidth(), inBitmap.getHeight() , inBitmap.getConfig());
        Canvas canvas = new Canvas(outBitmap);

        Paint paint = new Paint();
        paint.setColorFilter( new PorterDuffColorFilter(dstColor, PorterDuff.Mode.SRC_ATOP));

        canvas.drawBitmap(inBitmap , 0, 0, paint) ;
        return outBitmap ;
    }

    //该函数从给定的起始和终止颜色中按百分比选择中间的一个过渡颜色
    private @ColorInt Integer getColorGradient(float fraction, Integer startColor, Integer endColor){
        int startInt = startColor;
        int startA = (startInt >> 24) & 0xff;
        int startR = (startInt >> 16) & 0xff;
        int startG = (startInt >> 8) & 0xff;
        int startB = startInt & 0xff;

        int endInt = endColor;
        int endA = (endInt >> 24) & 0xff;
        int endR = (endInt >> 16) & 0xff;
        int endG = (endInt >> 8) & 0xff;
        int endB = endInt & 0xff;

        return (int) ((startA + (int) (fraction * (endA - startA))) << 24)
                | (int) ((startR + (int) (fraction * (endR - startR))) << 16)
                | (int) ((startG + (int) (fraction * (endG - startG))) << 8)
                | (int) ((startB + (int) (fraction * (endB - startB))));
    }
}