package com.sunbvert.lifeexpectancy;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.view.View;

public class PopuAnimationUnit {

    private static final int SCALE_ANIM_DURATION = 500;
    private static final int COLOR_ANIMA_DURATION = 1000;

    private Bitmap popuGraph;
    private Bitmap animatedPopuGraph;
    private float colorScale;

    private ValueAnimator scaleAnimator;
    private ValueAnimator colorAnimator;

    private int colorPopuGreen;
    private int colorPopuWhite;

    public boolean isEnd = false;

    public PopuAnimationUnit(View view, Bitmap popuGraph, float colorScale){
        this.popuGraph = popuGraph;
        this.colorScale = colorScale;
        Resources res = view.getResources();
        this.colorPopuGreen = res.getColor(R.color.popu_green);
        this.colorPopuWhite = res.getColor(R.color.popu_white);

        scaleAnimator = ValueAnimator.ofFloat(0.1f, 1);

        if (colorScale < 0.3){
            colorScale = 0.3f;
        }

        colorAnimator = ValueAnimator.ofInt(0, (int) (colorScale * popuGraph.getHeight()));

        setAnimationListener();
    }

    public void setAnimationStart(){
        this.scaleAnimator.start();
    }

    public Bitmap getAnimatedPopuGraph(){
        if (animatedPopuGraph != null){
            return animatedPopuGraph;
        }
        return popuGraph;
    }

    private void setAnimationListener(){
        scaleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float scale = (float) animation.getAnimatedValue();
                Matrix matrix = new Matrix();
                matrix.postScale(scale, scale);
                //改变Bitmap的大小
                animatedPopuGraph = Bitmap.createBitmap(popuGraph, 0, 0, popuGraph.getWidth(),
                        popuGraph.getHeight(), matrix, true);

                if (scale >= 1){
                    colorAnimator.start();
                }
            }
        });
        scaleAnimator.setDuration(SCALE_ANIM_DURATION);

        colorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int boundary = (int) animation.getAnimatedValue();
                for (int i = popuGraph.getHeight() - 2; i > popuGraph.getHeight() - boundary; i--){
                    for (int j = 20; j < popuGraph.getWidth() - 20; j++){
                        int pixelColor = popuGraph.getPixel(j, i);
                        if (pixelColor == colorPopuWhite){
                            popuGraph.setPixel(j, i, colorPopuGreen);
                        }
                    }
                }
                if(boundary >= (int) (colorScale * popuGraph.getHeight())){
                    isEnd = true;
                }
            }
        });
        colorAnimator.setDuration(COLOR_ANIMA_DURATION);
    }
}
