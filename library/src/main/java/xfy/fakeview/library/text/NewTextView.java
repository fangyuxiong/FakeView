package xfy.fakeview.library.text;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import xfy.fakeview.library.text.compiler.DefaultTextCompiler;
import xfy.fakeview.library.text.compiler.ITextCompiler;
import xfy.fakeview.library.text.drawer.TextDrawableDrawer;

/**
 * Created by XiongFangyu on 2018/3/1.
 */
public class NewTextView extends View implements FTextDrawable.LayoutRequestListener {
    private static final String TAG = "Fake--NewTextView";
    private final FTextDrawable textDrawable;

    public NewTextView(Context context) {
        this(context, null);
    }

    public NewTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NewTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        textDrawable = new FTextDrawable(context, attrs, defStyleAttr, 0);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public NewTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        textDrawable = new FTextDrawable(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        TextDrawableDrawer.init(getContext());
        textDrawable.setCallback(this);
        textDrawable.setLayoutRequestListener(this);
        if (textDrawable.getCompiler() == null)
            textDrawable.setTextCompiler(getDefaultCompiler());
    }

    public FTextDrawable getTextDrawable() {
        return textDrawable;
    }

    public void setCompiler(@NonNull ITextCompiler compiler) {
        textDrawable.setTextCompiler(compiler);
    }

    public void setTextSize(float size) {
        setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
    }

    public void setTextSize(int unit, float size) {
        textDrawable.setTextSize(getPx(size, unit));
    }

    public void setTextColor(int color) {
        textDrawable.setTextColor(color);
    }

    public void setMaxLines(int max) {
        textDrawable.setMaxLines(max);
    }

    public void setLineSpace(int px) {
        textDrawable.setLineSpace(px);
    }

    public void setDrawableSize(int px) {
        textDrawable.setDrawableSize(px);
    }

    public void setDrawableScale(float scale) {
        textDrawable.setDrawableScale(scale);
    }

    public void setUnderLineText(boolean underline) {
        textDrawable.setUnderLineText(underline);
    }

    public void setBoldText(boolean bold) {
        textDrawable.setBoldText(bold);
    }

    public void setItalicText(boolean italic) {
        textDrawable.setItalicText(italic);
    }

    public void setText(CharSequence text) {
        textDrawable.setText(text);
    }

    public void setGravity(int gravity) {
        textDrawable.setGravity(gravity);
    }

    public void setInlucdePad(boolean inlucdePad) {
        textDrawable.setIncludePad(inlucdePad);
    }

    public void setForceMeasureBlockList(boolean force) {
        textDrawable.setForceMeasureBlockList(force);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        textDrawable.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        textDrawable.onDetachedFromWindow();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (!textDrawable.isAutoMeasure()) {
            textDrawable.measure();
        }
        int widthMode  = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize  = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;
        int pl = getPaddingLeft();
        int pt = getPaddingTop();
        int pr = getPaddingRight();
        int pb = getPaddingBottom();
        int maxWidth = widthSize - pl - pr;
        int maxHeight = heightSize - pt - pb;

        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else {
            width = textDrawable.getIntrinsicWidth() + pl + pr;
            width = Math.max(width, getSuggestedMinimumWidth());
            if (widthMode == MeasureSpec.AT_MOST) {
                width = Math.min(widthSize, width);
            }
        }
        textDrawable.setMaxWidth(maxWidth);

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else {
            height = textDrawable.getIntrinsicHeight() + pt + pb;
            height = Math.max(height, getSuggestedMinimumHeight());
            if (heightMode == MeasureSpec.AT_MOST) {
                height = Math.min(heightSize, height);
            }
        }
        textDrawable.setMaxHeight(maxHeight);
        setMeasuredDimension(width, height);
        textDrawable.setBounds(pl, pt, width - pr, height - pb);
    }

    @Override
    public void needRequest(FTextDrawable drawable) {
        requestLayout();
    }

    @Override
    protected boolean verifyDrawable(@NonNull Drawable who) {
        return super.verifyDrawable(who) || textDrawable == who;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        boolean result = super.onTouchEvent(ev);
        result = textDrawable.onTouchEvent(this, ev) || result;
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        textDrawable.draw(canvas);
    }

    protected ITextCompiler getDefaultCompiler() {
        return DefaultTextCompiler.getCompiler();
    }

    private float getPxFromSp(float sp) {
        return getPx(sp, TypedValue.COMPLEX_UNIT_SP);
    }

    private float getPx(float s, int type) {
        Resources resources = getResources();
        return TypedValue.applyDimension(type, s, resources.getDisplayMetrics());
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
