package com.sunbvert.lifeexpectancy;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
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

    int[] gdpInColor;

    private SurfaceHolder mHolder;
    private Canvas mCanvas;
    private boolean mIsDrawing;

    int bgWidth;
    int bgHeight;

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

        countriesBitmap = loadCountriesBitmap();
        countriesPos = loadRelativePos(getReader(FILE_NAME));

        float scale = (float) displayMetrics.widthPixels / countriesBitmap.get(WORLD_MAP).getWidth();
        Matrix scaler = new Matrix();
        scaler.postScale(scale, scale);
        scaledCountriesBitmaps = scaleCountriesBitmap(countriesBitmap, scaler);
        Bitmap bg = scaledCountriesBitmaps.get(WORLD_MAP);
        this.bgWidth = bg.getWidth();
        this.bgHeight = bg.getHeight();
    }

    private void drawBackground(Canvas canvas){
        canvas.drawBitmap(scaledCountriesBitmaps.get(WORLD_MAP), 0, 0, new Paint());

        int worldMapWith = scaledCountriesBitmaps.get(WORLD_MAP).getWidth();
        int worldMapHeight = scaledCountriesBitmaps.get(WORLD_MAP).getHeight();

        for (String key: scaledCountriesBitmaps.keySet()){
            if (!key.equals(WORLD_MAP)) {
                canvas.drawBitmap(scaledCountriesBitmaps.get(key),
                        (float) countriesPos.get(key)[0] * worldMapWith,
                        (float) countriesPos.get(key)[1] * worldMapHeight,
                        new Paint());
            }
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

    public void setData(LifeExpValuesForPlotting plottingData) {
        this.plottingData = plottingData;
        this.gdpInColor = constructGdpColorArray(plottingData.gdp, Color.RED, Color.GREEN);
    }

    public void setRawData(LifeExpectancyValues rawData){
        this.rawData = rawData;
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
    Paint textPaint;
    Paint clearPaint;

    static int margin = 100;
    static int numOfYear = 5;   //同时在年份选择器上绘制的年份
    static int TEXT_SIZE = 20;
    int currentSeleteYearIndex = 0;
    int interval;
    boolean isTouched = false;

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        defaultPaint = new Paint();
        textPaint = new Paint();
        textPaint.setTextSize(TEXT_SIZE);
        interval = (bgWidth - margin * 2) / (numOfYear - 1);

        clearPaint = new Paint();
        clearPaint.setColor(Color.WHITE);

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

    int rulerTouchBeginX = 0;
    int rulerTouchBeginY = 0;
    int lastMoveX = 0;
    int lastMoveY = 0;
    int offset = 0;
    long rulerTouchBeginTime = 0;
    boolean rulerTouched = false;
    float rulerTouchEndSpeed = 0;
    @Override
    public boolean onTouchEvent(MotionEvent event){
        int x = (int) event.getX();
        int y = (int) event.getY();

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                if (y > bgHeight && y < bgHeight + 3 * TEXT_SIZE){
                    rulerTouched = true;
                    rulerTouchBeginX = x;
                    rulerTouchBeginY = y;
                    rulerTouchBeginTime = System.currentTimeMillis();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (rulerTouched){
                    if (lastMoveX != 0){
                        offset += x - lastMoveX;
                        //Log.d(TAG, "触摸offset: " + offset);
                    }
                }

                lastMoveX = x;
                lastMoveY = y;
                break;
            case MotionEvent.ACTION_UP:
                if (rulerTouched){
                    rulerTouched = false;
                    rulerTouchEndSpeed = (float) (y - rulerTouchBeginY) * 1000 /
                            (rulerTouchBeginTime - System.currentTimeMillis());
                    //offset = 0;

                    lastMoveX = 0;
                    lastMoveY = 0;
                }
                break;
        }
        return true;
    }

    int t = 0;   //当前选中国家在timeline中的索引
    @Override
    public void run() {

        while (mIsDrawing){
            long startTime = System.currentTimeMillis();

            //线程开启时还没有rawData数据，所以需要判断rawData是否为空
            if (rawData != null) {
                //todo：如果点击播放的话

                //如果控制用户选择年份t的话
                if (t > rawData.timeLine.size() - 1) {
                    t = rawData.timeLine.size() - 1;
                }
                draw(rawData.timeLine.get(t));

            }

            long endTime = System.currentTimeMillis();
            int diffTime = (int)(endTime - startTime);

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
            //测试先画某一年的情况
            drawSingleFrame(mCanvas, year);
            drawYearRegulator(mCanvas);
        }catch (Exception e){
            Log.e(TAG, "绘制线程出错：", e);
        }finally {
            if (mCanvas != null){
                mHolder.unlockCanvasAndPost(mCanvas);
            }
        }

    }

     //画出每一帧的国家颜色变化
    private void drawSingleFrame(Canvas canvas, int year){
        canvas.drawBitmap(scaledCountriesBitmaps.get(WORLD_MAP), 0, 0, defaultPaint);
        for(int i = 0; i < plottingData.gdp.length; i++){
            if (plottingData.investigateYear[i] == year){
                String countryName = plottingData.counties[i];
                Bitmap countryBitmap = scaledCountriesBitmaps.get(countryName);
                Bitmap coloredBitmap = changeBitmapColor(countryBitmap, gdpInColor[i]);

                if (coloredBitmap != null) {
                    canvas.drawBitmap(coloredBitmap,
                            (float) countriesPos.get(countryName)[0] * bgWidth,
                            (float) countriesPos.get(countryName)[1] * bgHeight,
                            new Paint());
                }
            }
        }
    }

    int numOfYearToDisplay = numOfYear;
    //画出每一帧年份标尺的变化
    private void drawYearRegulator(Canvas canvas){
        canvas.drawLine(margin, bgHeight, bgWidth - margin, bgHeight, defaultPaint);
        canvas.drawRect(0, bgHeight + TEXT_SIZE, bgWidth, bgHeight + TEXT_SIZE * 2, clearPaint);

        //t是当前timeLine中选中过国家的索引，t-2>=0可以确保标尺在前两格没有数据时也可以显示
        if (t - 2 >= 0) {
            int j = t - 2;
            for (int i = 0; i < numOfYearToDisplay; i++) {
                canvas.drawText(String.valueOf(rawData.timeLine.get(j)),
                        margin + interval * i - TEXT_SIZE + animateRulerOffset(), bgHeight + TEXT_SIZE * 2, textPaint);
                j++;
            }
        }else{
            for (int i = 0; i < numOfYearToDisplay; i++) {
                canvas.drawText(String.valueOf(rawData.timeLine.get(i)),
                        margin + interval * i - TEXT_SIZE + animateRulerOffset(), bgHeight + TEXT_SIZE * 2, textPaint);
            }
        }
        canvas.drawLine(margin, bgHeight + TEXT_SIZE * 3,
                bgWidth - margin, bgHeight + TEXT_SIZE * 3, new Paint());
    }

    //todo: put these values into xml
    static float accelerationClose = 1;
    static float friction = 1;
    float velocity = 0;
    boolean beginAnimation = false;

    private int animateRulerOffset(){
        if (rulerTouched){
            if (offset > 0){
                if (offset > interval){
                    //实时根据offset计算需要显示年份的数量
                    numOfYearToDisplay = numOfYear + offset / interval;
                    currentSeleteYearIndex = t + offset / interval;
                }
            }else {
                if (-offset > interval){
                    numOfYearToDisplay = numOfYear - offset / interval;
                    currentSeleteYearIndex = t + offset / interval;
                }
            }
            return offset;
        }else {
            if (offset != 0) {
                offset = 0;
            }
        }
        return 0;
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