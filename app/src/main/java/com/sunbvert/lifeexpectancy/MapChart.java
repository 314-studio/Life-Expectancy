package com.sunbvert.lifeexpectancy;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.JsonReader;
import android.util.Log;
import android.view.Surface;
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

    LifeExpValuesForPlotting plottingData;

    Context context;

    Map<String, Bitmap> countriesBitmap;
    Map<String, double[]> countriesPos;
    Map<String, Bitmap> scaledCountriesBitmaps;

    private SurfaceHolder mHolder;
    private Canvas mCanvas;
    private boolean mIsDrawing;


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

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mCanvas = holder.lockCanvas();
        drawBackground(mCanvas);
        holder.unlockCanvasAndPost(mCanvas);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void run() {

    }
}