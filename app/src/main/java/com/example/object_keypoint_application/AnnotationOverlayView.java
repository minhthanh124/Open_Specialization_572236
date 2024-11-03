package com.example.object_keypoint_application;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AnnotationOverlayView extends AppCompatImageView {

    private Bitmap bitmap;
    private Canvas canvas;
    private Paint paint;
    public final List<float[]> circles = new ArrayList<>();

    public AnnotationOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @SuppressLint("CutPasteId")
    private void init() {
        paint = new Paint();
        paint.setColor(ContextCompat.getColor(this.getContext(), android.R.color.holo_red_dark));
        paint.setStyle(Paint.Style.FILL);
    }

    public void setKeypoints(Bitmap bm) {
        circles.clear();
        bitmap = bm;
        canvas = new Canvas(bitmap);
    }

    public void drawPoint(float x, float y) {
        if (canvas != null) {
            circles.add(new float[]{x, y});
            canvas.drawBitmap(bitmap, 0, 0, paint);
            invalidate();
        }
    }

    public void clearPoints() {
        circles.clear();
        invalidate();
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (bitmap != null) {
            for (float[] circle : circles) {
                canvas.drawCircle(circle[0], circle[1], 10, paint);
            }
        }
    }
}