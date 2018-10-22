package com.sunbvert.lifeexpectancy;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.Matrix;

public class PopuAnimationUnit {

    private static final int SCALE_ANIM_DURATION = 500;
    private static final int COLOR_ANIMA_DURATION = 500;

    private Bitmap popuGraph;
    private Bitmap animatedPopuGraph;
    private float colorScale;

    private ValueAnimator scaleAnimator;
    private ValueAnimator colorAnimator;

    public boolean isEnd = false;

    public PopuAnimationUnit(Bitmap popuGraph, float colorScale){
        this.popuGraph = popuGraph;
        this.colorScale = colorScale;

        scaleAnimator = ValueAnimator.ofFloat(0.1f, 1);
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

                if(boundary >= (int) (colorScale * popuGraph.getHeight())){
                    isEnd = true;
                }
            }
        });
        colorAnimator.setDuration(COLOR_ANIMA_DURATION);
    }
}
