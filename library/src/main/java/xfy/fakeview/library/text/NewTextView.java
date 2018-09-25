package xfy.fakeview.library.text;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Typeface;
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
import xfy.fakeview.library.text.utils.MeasureTextUtils;

/**
 * Created by XiongFangyu on 2018/3/1.
 */
public class NewTextView extends View implements FTextDrawable.LayoutRequestListener {
    private static final String TAG = "Fake--NewTextView";
    private final FTextDrawable textDrawable;
    private int maxWidth;
    private int maxHeight;

    public NewTextView(Context context) {
        this(context, null);
    }

    public NewTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NewTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        StyleHelper styleHelper = new StyleHelper(context, attrs, defStyleAttr, 0);
        textDrawable = new FTextDrawable(styleHelper);
        init(styleHelper);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public NewTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        StyleHelper styleHelper = new StyleHelper(context, attrs, defStyleAttr, defStyleRes);
        textDrawable = new FTextDrawable(styleHelper);
        init(styleHelper);
    }

    private void init(StyleHelper helper) {
        TextDrawableDrawer.init(getContext());
        textDrawable.setCallback(this);
        textDrawable.setLayoutRequestListener(this);
        if (textDrawable.getCompiler() == null)
            textDrawable.setTextCompiler(getDefaultCompiler());
        if (helper == null)
            return;
        maxWidth = helper.maxWidth;
        maxHeight = helper.maxHeight;
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

    public void setTypeface(Typeface tf) {
        textDrawable.setTypeface(tf);
    }

    public void setText(CharSequence text) {
        textDrawable.setText(text);
    }

    public void setGravity(int gravity) {
        textDrawable.setGravity(gravity);
    }

    public void setIncludePad(boolean inlucdePad) {
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
        int maxWidth = (widthSize == 0 ? this.maxWidth : widthSize) - pl - pr;
        int maxHeight = (heightSize == 0 ? this.maxHeight : heightSize) - pl - pr;
        maxHeight = maxHeight == 0 ? MeasureTextUtils.HEIGHT_MAX_SIZE : maxHeight;
        textDrawable.justSetMaxSize(maxWidth, maxHeight);
//        textDrawable.setMaxWidth(maxWidth);
//        textDrawable.setMaxHeight(maxHeight);
        if (!textDrawable.isAutoMeasure()) {
            textDrawable.measure();
        }

        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else {
            width = Math.max(textDrawable.getIntrinsicWidth(), getSuggestedMinimumWidth());
            if (widthMode == MeasureSpec.AT_MOST) {
                width = Math.min(widthSize, width);
            }
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else {
            height = Math.max(textDrawable.getIntrinsicHeight(), getSuggestedMinimumHeight());
            if (heightMode == MeasureSpec.AT_MOST) {
                height = Math.min(heightSize, height);
            }
        }
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
