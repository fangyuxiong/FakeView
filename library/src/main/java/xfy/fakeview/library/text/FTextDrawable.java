package xfy.fakeview.library.text;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.lang.ref.WeakReference;

import xfy.fakeview.library.text.block.IDrawableBlock;
import xfy.fakeview.library.text.block.IDrawableBlockList;
import xfy.fakeview.library.text.compiler.ITextCompiler;
import xfy.fakeview.library.text.drawer.TextDrawer;
import xfy.fakeview.library.text.param.ImmutableParams;
import xfy.fakeview.library.text.param.VariableParams;
import xfy.fakeview.library.text.utils.LineUtils;
import xfy.fakeview.library.text.utils.MeasureTextUtils;

/**
 * Created by XiongFangyu on 2018/3/1.
 */
public class FTextDrawable extends Drawable {
    private static final String TAG = "Fake--TextDrawable";

    private int lineSpace = 10;
    private int maxWidth;
    private int maxHeight;
    private CharSequence mText;
    private int maxLines;
    private TextUtils.TruncateAt ellipsize = TextUtils.TruncateAt.END;
    private WeakReference<LayoutRequestListener> listenerRef;
    private boolean autoMeasure = true;

    protected int textWidth;
    protected int lines;
    protected int needDrawLines;
    protected boolean isNeedEllipsize;
    protected int ellipsizeLength;
    protected int drawableSize;

    protected final VariableParams variableParams;
    protected final ImmutableParams immutableParams;

    protected final TextPaint mTextPaint;

    protected boolean needMeasureText = false;

    protected boolean needMeasureTextLines = true;

    protected IDrawableBlockList<IDrawableBlock> blockList;
    private ITextCompiler compiler;

    public FTextDrawable() {
        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        variableParams = new VariableParams();
        immutableParams = new ImmutableParams();
    }

    public FTextDrawable(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        variableParams = new VariableParams();
        immutableParams = new ImmutableParams();
        StyleHelper helper = new StyleHelper(context, attrs, defStyleAttr, defStyleRes);
        if (helper.textCompiler != null)
            setTextCompiler(helper.textCompiler);
        setAutoMeasure(helper.measureWhenSetText);
        if (helper.maxLines > 0) {
            setMaxLines(helper.maxLines);
        }
        if (helper.textSize > 0) {
            setTextSize(helper.textSize);
        }
        if (helper.lineSpace > 0) {
            setLineSpace(helper.lineSpace);
        }
        if (helper.textColor != 0) {
            setTextColor(helper.textColor);
        }
        if (helper.drawableScale != 1) {
            setDrawableScale(helper.drawableScale);
        }
        if (helper.drawableSize > 0) {
            setDrawableSize(helper.drawableSize);
        }
        if (!TextUtils.isEmpty(helper.text)) {
            setText(helper.text);
        }
    }

    //<editor-folder desc="public method">
    public void setAutoMeasure(boolean auto) {
        autoMeasure = auto;
    }

    public boolean isAutoMeasure(){
        return autoMeasure;
    }

    public void setTextCompiler(ITextCompiler compiler) {
        this.compiler = compiler;
    }

    public ITextCompiler getCompiler() {
        return this.compiler;
    }

    public void setText(CharSequence text) {
        if (mText == null && text == null)
            return;
        if (mText != null && mText.equals(text))
            return;
        mText = text;
        if (compiler != null) {
            if (blockList != null) {
                blockList.notUse();
            }
            if (text == null) {
                blockList = null;
            } else {
                blockList = compiler.compile(text);
            }
        } else {
            throw new NullPointerException("compiler is null, please set compiler before set text!");
        }
        needMeasureTextLines = true;
        if (autoMeasure)
            measure();
        requestLayout();
        invalidateSelf();
    }

    public void setLayoutRequestListener(LayoutRequestListener listener) {
        listenerRef = new WeakReference<LayoutRequestListener>(listener);
    }

    public void setDrawableSize(int drawableSize) {
        if (this.drawableSize != drawableSize) {
            this.drawableSize = drawableSize;
            needMeasureTextLines = true;
            if (!TextUtils.isEmpty(mText)) {
                if (autoMeasure)
                    measure();
                requestLayout();
                invalidateSelf();
            }
        }
    }

    public void setDrawableScale(float scale) {
        int size = (int) (scale * mTextPaint.getTextSize());
        setDrawableSize(size);
    }

    public void setTextColor(int textColor) {
        if (mTextPaint.getColor() != textColor) {
            mTextPaint.setColor(textColor);
            if (!TextUtils.isEmpty(mText))
                invalidateSelf();
        }
    }

    public void setLineSpace(int lineSpace) {
        if (this.lineSpace != lineSpace) {
            this.lineSpace = lineSpace;
            if (!TextUtils.isEmpty(mText)) {
                if (autoMeasure)
                    measure();
                requestLayout();
                invalidateSelf();
            }
        }
    }

    public void setMaxWidth(int maxWidth) {
        maxWidth = maxWidth < 0 ? 0 : maxWidth;
        if (this.maxWidth != maxWidth) {
            this.maxWidth = maxWidth;
            if (!TextUtils.isEmpty(mText)) {
                if (autoMeasure)
                    measure();
                requestLayout();
                invalidateSelf();
            }
        }
    }

    public void setMaxHeight(int maxHeight) {
        maxHeight = maxHeight < 0 ? 0 : maxHeight;
        if (this.maxHeight != maxHeight) {
            this.maxHeight = maxHeight;
            if (!TextUtils.isEmpty(mText)) {
                if (autoMeasure)
                    measure();
                requestLayout();
                invalidateSelf();
            }
        }
    }

    public void setTextSize(float textSize) {
        if (mTextPaint.getTextSize() != textSize) {
            mTextPaint.setTextSize(textSize);
            needMeasureTextLines = true;
            if (drawableSize == 0)
                drawableSize = (int) textSize;
            ellipsizeLength = TextDrawer.getEllipsizeLength(mTextPaint);
            if (!TextUtils.isEmpty(mText)) {
                if (autoMeasure)
                    measure();
                requestLayout();
                invalidateSelf();
            }
        }
    }

    public void setMaxLines(int maxLines) {
        maxLines = maxLines < 1 ? 1 : maxLines;
        if (this.maxLines != maxLines) {
            this.maxLines = maxLines;
            if (!TextUtils.isEmpty(mText)) {
                if (autoMeasure)
                    measure();
                requestLayout();
                invalidateSelf();
            }
        }
    }

    public boolean onTouchEvent(@NonNull View v, MotionEvent event) {
        if (blockList != null)
            return blockList.onTouchEvent(v, event, immutableParams);
        return false;
    }
    //</editor-folder>

    //<editor-folder desc="drawable method">
    @Override
    public void draw(@NonNull Canvas canvas) {
        if (needMeasureText) {
            requestLayout();
            invalidateSelf();
            return;
        }
        if (mText == null || lines == 0)
            return;
        final Rect bounds = getBounds();
        final int left = bounds.left;
        final int right = bounds.right;
        if (left == right || bounds.height() == 0)
            return;
        initParamsBeforeDraw();
        if (needDrawLines <= 0)
            return;
        drawBlockList(canvas);
//        TextDrawer.drawText(canvas, mText, variableParams, immutableParams, null);
    }

    @Override
    public void setAlpha(int alpha) {
        mTextPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        mTextPaint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.UNKNOWN;
    }

    @Override
    public int getIntrinsicWidth() {
        return textWidth;
    }

    @Override
    public int getIntrinsicHeight() {
        int flags[] = blockList != null ? blockList.getLinesHeight() : null;
        return flags != null ? LineUtils.getAllLineHeight(flags, needDrawLines) : 0;
    }
    //</editor-folder>

    //<editor-folder desc="private method">
    private void initParamsBeforeDraw() {
        final Rect bounds = getBounds();
        final int left = bounds.left;
        final int right = bounds.right;
        final int top = bounds.top;

        int flags[] = blockList != null ? blockList.getLinesHeight() : null;
        if (flags != null) {
            variableParams.currentBaseline = LineUtils.getBaseLine(flags, 0) + bounds.top;
        }

        variableParams.currentLeft = left;
        variableParams.currentTop = top;
        variableParams.currentDrawLine = 0;
        variableParams.isDrawEndEllipsize = false;
//        variableParams.middleEllipsizeWidthRecord = 0;
//        variableParams.isExecutedMiddleEllipsize = false;

        immutableParams.top = top;
        immutableParams.bottom = bounds.bottom;
        immutableParams.left = left;
        immutableParams.right = right;
        immutableParams.drawableHeight = drawableSize;
        immutableParams.lineSpace = lineSpace;
        immutableParams.lines = lines;
        immutableParams.needDrawLine = needDrawLines;
        immutableParams.truncateAt = isNeedEllipsize ? ellipsize : null;
        immutableParams.ellipsizeLength = ellipsizeLength;
        immutableParams.paint = mTextPaint;
        immutableParams.lineInfos = flags;
    }

    private void drawBlockList(Canvas canvas) {
        int len = blockList.size();
        for (int i = 0; i < len; i ++) {
            blockList.get(i).draw(canvas, variableParams, immutableParams);
        }
    }

    public void measure() {
        if (maxWidth == 0 || maxHeight == 0) {
            needMeasureText = true;
            return;
        }
        calTextLinesAndContentWidth();
        calNeedDrawLines();
    }

    private void calTextLinesAndContentWidth() {
        if (TextUtils.isEmpty(mText) || blockList == null) {
            textWidth = 0;
            return;
        }
        if (!needMeasureTextLines) {
            return;
        }
        needMeasureTextLines = false;
        final Rect bounds = getBounds();
        final int left = bounds.left;
        int maxWidth = this.maxWidth;
        if (maxWidth == 0) {
            maxWidth = bounds.width();
        }
        long flag = TextDrawer.measureText(mTextPaint, blockList, drawableSize, left, left, left + maxWidth, true);
        if (MeasureTextUtils.getState(flag) == MeasureTextUtils.STATE_SUCCESS) {
            textWidth = MeasureTextUtils.getMaxWidth(flag);
            lines = MeasureTextUtils.getLines(flag);
            needMeasureText = false;
        } else {
            needMeasureText = true;
        }
    }

    private void calNeedDrawLines() {
        needDrawLines = lines;
        if (maxLines < lines && maxLines > 0) {
            needDrawLines = maxLines;
        }
        isNeedEllipsize = lines > needDrawLines;
    }

    private void requestLayout() {
        LayoutRequestListener listener = listenerRef != null ? listenerRef.get() : null;
        if (listener != null) {
            listener.needRequest(this);
        }
    }

    private long now() {
        return System.nanoTime();
    }

    private long logCast(String pre, long start) {
        long now = now();
        Log.d(TAG, pre + (now - start));
        return now;
    }
    //</editor-folder>

    public interface LayoutRequestListener {
        void needRequest(FTextDrawable drawable);
    }
}
