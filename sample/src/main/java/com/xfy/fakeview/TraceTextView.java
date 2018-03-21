package com.xfy.fakeview;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

/**
 * Created by XiongFangyu on 2018/3/13.
 */

public class TraceTextView extends TextView {
    private static final String TAG = "Fake--Normal";
    public TraceTextView(Context context) {
        super(context);
    }

    public TraceTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TraceTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public TraceTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        long now = now();
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        logCast("measure cast: ", now);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        long now = now();
        super.onDraw(canvas);
        logCast("draw cast: ", now);
    }

    private static long now() {
        return System.nanoTime();
    }

    private static long logCast(String pre, long now) {
        long end = now();
        Log.d(TAG, pre + (end - now));
        return end;
    }
}
