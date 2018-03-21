package com.xfy.fakeview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by XiongFangyu on 2018/3/13.
 */

public class LayoutTextView extends View {
    private static final String TAG = "Fake--LayoutText";
    public LayoutTextView(Context context) {
        super(context);
    }

    public LayoutTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public LayoutTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public LayoutTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private TextPaint mPaint;
    private CharSequence mText;
    private StaticLayout layout;
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        long now = now();
        if (layout != null) {
            int widthMode = MeasureSpec.getMode(widthMeasureSpec);
            int heightMode = MeasureSpec.getMode(heightMeasureSpec);
            int widthSize = MeasureSpec.getSize(widthMeasureSpec);
            int heightSize = MeasureSpec.getSize(heightMeasureSpec);

            int width, height;
            if (widthMode == MeasureSpec.EXACTLY) {
                width = widthSize;
            } else {
                width = getDesiredWidth();
                if (width < 0) {
                    width = layout.getWidth();
                }
                if (widthSize > 0) {
                    width = Math.min(width, widthSize);
                }
            }

            width += getPaddingLeft() + getPaddingRight();
            if (widthMode == MeasureSpec.AT_MOST) {
                width = Math.min(widthSize, width);
            }

            if (heightMode == MeasureSpec.EXACTLY) {
                height = heightSize;
            } else {
                height = getDesiredHeight();
                if (heightMode == MeasureSpec.AT_MOST) {
                    height = Math.min(heightSize, height);
                }
            }
            setMeasuredDimension(width, height);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
        logCast("measure cast: ", now);
    }

    private int getDesiredWidth() {
        final int lineCount = layout.getLineCount();
        final CharSequence text = layout.getText();

        // If any line was wrapped, we can't use it. but it's
        // ok for the last line not to have a newline.
        for (int i = 0; i < lineCount - 1; i++) {
            if (text.charAt(layout.getLineEnd(i) - 1) != '\n') {
                return -1;
            }
        }

        float maxWidth = 0;
        for (int i = 0; i < lineCount; i++) {
            maxWidth = Math.max(maxWidth, layout.getLineWidth(i));
        }
        return (int) Math.ceil(maxWidth);
    }

    private int getDesiredHeight() {
        if (layout == null) {
            return 0;
        }
        final int padding = getPaddingTop() + getPaddingBottom();
        final int refLine = Math.min(10, layout.getLineCount());
        return layout.getLineTop(refLine) + padding;
    }

    private void initPaint() {
        if (mPaint == null)
            mPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    }

    public void setTextColor(int color) {
        initPaint();
        if (mPaint.getColor() != color) {
            mPaint.setColor(color);
            invalidate();
        }
    }

    public void setTextSize(float size) {
        initPaint();
        if (mPaint.getTextSize() != size) {
            mPaint.setTextSize(size);
            initLayout();
            invalidate();
            requestLayout();
        }
    }

    public void setText(CharSequence text) {
        if (TextUtils.equals(text, mText))
            return;
        mText = text;
        initLayout();
        invalidate();
        requestLayout();
    }

    private void initLayout() {
        initPaint();
        if (mText != null) {
            layout = LayoutHelper.newLayout(mText, mPaint, getMeasuredWidth() - getPaddingLeft() - getPaddingRight());
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        long now = now();
        if (layout != null) {
            canvas.save();
            canvas.translate(getPaddingLeft(), getPaddingTop());
            layout.draw(canvas);
            canvas.restore();
        }
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
