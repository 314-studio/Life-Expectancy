package com.sunbvert.lifeexpectancy;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

public class MapChart extends View {

    DisplayMetrics displayMetrics = new DisplayMetrics();


    public MapChart(Context context) {
        super(context);
    }

    public MapChart(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MapChart(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MapChart(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        Resources res = getResources();
        Bitmap bg = BitmapFactory.decodeResource(res, R.mipmap.countries);
        canvas.drawBitmap(bg, 100, 100, new Paint());

    }
}