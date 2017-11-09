package xfy.fakeview.library;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;

/**
 * Created by XiongFangyu on 2017/11/9.
 */
public class TraceFrameLayout extends FrameLayout {
    public TraceFrameLayout(@NonNull Context context) {
        this(context, null);
    }

    public TraceFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TraceFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public TraceFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setWillNotDraw(false);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        long start = now();
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        cast("onmeasure:", start);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        long start = now();
        super.onLayout(changed, left, top, right, bottom);
        cast("onlayout:", start);
    }

    @Override
    public void draw(Canvas canvas) {
        long start = now();
        super.draw(canvas);
        cast("draw:", start);
    }

    private static void cast(String pre, long start) {
        Log.d("TraceFrameLayout", pre + " " + (now() - start));
    }

    private static long now() {
        return System.currentTimeMillis();
    }
}
