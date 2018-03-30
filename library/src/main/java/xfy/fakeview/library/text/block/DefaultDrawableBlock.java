package xfy.fakeview.library.text.block;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.view.MotionEvent;
import android.view.View;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import xfy.fakeview.library.text.drawer.TextDrawableDrawer;
import xfy.fakeview.library.text.drawer.TextDrawer;
import xfy.fakeview.library.text.param.ClickSpanBlockInfo;
import xfy.fakeview.library.text.param.ImmutableParams;
import xfy.fakeview.library.text.param.SpecialStyleParams;
import xfy.fakeview.library.text.param.VariableParams;
import xfy.fakeview.library.text.utils.BaseSpan;
import xfy.fakeview.library.text.utils.CallbackObserver;
import xfy.fakeview.library.text.utils.FClickableSpan;
import xfy.fakeview.library.text.utils.IllegalDrawableException;
import xfy.fakeview.library.text.utils.LineUtils;
import xfy.fakeview.library.text.utils.MeasureTextUtils;
import xfy.fakeview.library.text.utils.NoCacheSpanRegister;

/**
 * Created by XiongFangyu on 2018/3/2.
 */
public class DefaultDrawableBlock implements IDrawableBlock<DefaultDrawableBlockList>, Drawable.Callback {
    private int type;
    private CharSequence mText;
    private SpecialStyleParams textStyleParams;
    private int drawableRes;
    private Drawable specialDrawable;
    private DefaultDrawableBlockList children;
    private BaseSpan span;

    private final ArrayList<WeakReference<Drawable.Callback>> callbacks;
    private boolean hasCreateNewDrawableForSpecialDrawable;
    private boolean hasSetCallback;
    private long flag;
    private int baseLine;

    private DefaultDrawableBlock() {
        callbacks = new ArrayList<>();
    }

    private static final int DEFAULT_SIZE = 20;
    private static final List<DefaultDrawableBlock> cache;
    static {
        cache = new ArrayList<>(DEFAULT_SIZE);
        for (int i = 0; i < DEFAULT_SIZE; i ++) {
            cache.add(new DefaultDrawableBlock());
        }
    }

    private synchronized static DefaultDrawableBlock obtain() {
        if (cache.isEmpty())
            return new DefaultDrawableBlock();
        return cache.remove(0);
    }

    private synchronized static void putToCache(DefaultDrawableBlock block) {
        cache.add(block);
    }

    public void recycle() {
        callbacks.clear();
        hasCreateNewDrawableForSpecialDrawable = false;
        hasSetCallback = false;
        span = null;
        flag = 0;
        type = 0;
        baseLine = 0;
        mText = null;
        if (textStyleParams != null)
            textStyleParams.recycle();
        textStyleParams = null;
        drawableRes = 0;
        if (specialDrawable != null) {
            if (specialDrawable instanceof CallbackObserver) {
                ((CallbackObserver) specialDrawable).onCallbackSet(null);
            }
        }
        specialDrawable = null;
        if (children != null)
            children.notUse();
        children = null;
        putToCache(this);
    }

    @Override
    public boolean canSaveToCache() {
        return !NoCacheSpanRegister.contain(span);
    }

    @Override
    public long getFlag() {
        return flag;
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public int getBaseLine() {
        return baseLine;
    }

    @Override
    public CharSequence getText() {
        return mText;
    }

    @Override
    public int getDrawableRes() {
        return drawableRes;
    }

    @Override
    public Drawable getSpecialDrawable() {
        return specialDrawable;
    }

    @Override
    public DefaultDrawableBlockList getChildren() {
        return children;
    }

    @Override
    public long measure(BlockMeasureParams measureParams, @NonNull ImmutableParams immutableParams) {
        final TextPaint textPaint = immutableParams.paint;
        final int drawableSize = measureParams.drawableSize;
        final int left = measureParams.left;
        final int currentLeft = measureParams.currentLeft;
        final int right = measureParams.right;
        final boolean includePad = measureParams.includePad;
        int lineInfo = measureParams.lineInfo;
        int top = measureParams.currentTop;
        int fontHeight = LineUtils.getLineHeight(lineInfo);
        this.baseLine = LineUtils.getBaseLine(lineInfo);
        switch (type) {
            case TEXT:
                float oldTextSize = textPaint.getTextSize();
                if (textStyleParams != null && textStyleParams.hasTextSize) {
                    textPaint.setTextSize(textStyleParams.textSize);
                    if (textStyleParams.textSize > oldTextSize) {
                        int flag = TextDrawer.getLineInfo(textPaint, drawableSize, includePad);
                        fontHeight = LineUtils.getLineHeight(flag);
                        baseLine = LineUtils.getBaseLine(flag);
                        measureParams.lineInfo = LineUtils.combime(fontHeight, baseLine);
                    }
                }
                flag = TextDrawer.measureText(textPaint, mText, currentLeft, left, right, 0);
                flag = MeasureTextUtils.setMaxHeight(flag, fontHeight);
                textPaint.setTextSize(oldTextSize);
                break;
            case NEXTLINE:
                flag = MeasureTextUtils.setLines(0, 2);
                break;
            case DRAWABLE:
                if (specialDrawable == null && drawableRes > 0)
                    specialDrawable = TextDrawableDrawer.getDrawableDrawer().getSpecialDrawable(drawableRes, drawableSize);
                if (specialDrawable == null) {
                    flag = TextDrawer.measureText(textPaint, mText, currentLeft, left, right, 0);
                }
                flag = TextDrawer.measureFixWidth(TextDrawableDrawer.measureDrawableWidth(specialDrawable, drawableSize), currentLeft, left, right);
                break;
            case SPECIAL_DRAWABLE:
                if (specialDrawable == null) {
                    flag = TextDrawer.measureText(textPaint, mText, currentLeft, left, right, 0);
                }
                flag = TextDrawer.measureFixWidth(TextDrawableDrawer.measureDrawableWidth(specialDrawable, drawableSize), currentLeft, left, right);
                break;
            case NEED_SET_CALLBACK_DRAWABLE:
                if (!hasCreateNewDrawableForSpecialDrawable) {
                    specialDrawable = TextDrawableDrawer.getDrawableDrawer().getSpecialDrawable(specialDrawable, drawableSize, true);
                    hasCreateNewDrawableForSpecialDrawable = true;
                    if (!hasSetCallback) {
                        ((CallbackObserver) specialDrawable).onCallbackSet(this);
                        hasSetCallback = true;
                    }
                }
                flag = TextDrawer.measureFixWidth(TextDrawableDrawer.measureDrawableWidth(specialDrawable, drawableSize), currentLeft, left, right);
                break;
            case SPAN:
                DefaultDrawableBlockList children = getChildren();
                if (children == null) {
                    flag = 0;
                } else {
                    flag = children.measure(measureParams, immutableParams);
                }
                if (span instanceof FClickableSpan) {
                    immutableParams.addClickSpanBlockInfo(this, currentLeft, top, flag);
                }
                break;
        }
        measureParams.currentLeft = MeasureTextUtils.getCurrentLeft(flag);
        int lines = MeasureTextUtils.getLines(flag);
        if (lines > 1) {
            top += (lines - 1) * MeasureTextUtils.getMaxHeight(flag);
            measureParams.currentTop = top;
        }
        return flag;
    }

    @Override
    public boolean draw(Canvas canvas, @NonNull VariableParams variableParams, @NonNull ImmutableParams immutableParams) {
        if (variableParams.isDrawEndEllipsize)
            return true;
        switch (type) {
            case TEXT:
                TextDrawer.drawText(canvas, mText, variableParams, immutableParams, textStyleParams);
                break;
            case NEXTLINE:
                TextDrawer.drawNextLine(canvas, variableParams, immutableParams);
                break;
            case DRAWABLE:
                if (drawableRes <= 0) {
                    TextDrawer.drawText(canvas, mText, variableParams, immutableParams, textStyleParams);
                    break;
                }
                if (specialDrawable != null) {
                    TextDrawableDrawer.getDrawableDrawer().drawSpecialDrawable(canvas, specialDrawable, variableParams, immutableParams);
                } else {
                    specialDrawable = TextDrawableDrawer.getDrawableDrawer().drawResource(canvas, drawableRes, variableParams, immutableParams);
                }
                break;
            case SPECIAL_DRAWABLE:
                if (specialDrawable == null) {
                    TextDrawer.drawText(canvas, mText, variableParams, immutableParams, textStyleParams);
                    break;
                }
                TextDrawableDrawer.getDrawableDrawer().drawSpecialDrawable(canvas, specialDrawable, variableParams, immutableParams);
                break;
            case NEED_SET_CALLBACK_DRAWABLE:
                if (specialDrawable == null) {
                    TextDrawer.drawText(canvas, mText, variableParams, immutableParams, textStyleParams);
                    break;
                }
                TextDrawableDrawer.getDrawableDrawer().drawSpecialDrawable(canvas, specialDrawable, variableParams, immutableParams);
                break;
            case SPAN:
                DefaultDrawableBlockList children = getChildren();
                if (children == null)
                    return false;
                boolean result = true;
                for (int i = 0, l = children.size(); i < l; i ++) {
                    if (!children.get(i).draw(canvas, variableParams, immutableParams)) {
                        result = false;
                    }
                }
                return result;
            default:
                return false;
        }
        return true;
    }

    @Override
    public boolean onTouchEvent(@NonNull View v, MotionEvent event, @NonNull ImmutableParams immutableParams, @NonNull ClickSpanBlockInfo blockInfo) {
        if (type != SPAN)
            return false;
        if (!(span instanceof FClickableSpan)) {
            return false;
        }
        int action = event.getAction();
        boolean handleEvent = false;
        if (action == MotionEvent.ACTION_UP ||
                action == MotionEvent.ACTION_DOWN) {
            handleEvent = false;
            if (isPointInThisBlock(event.getX(), event.getY(), immutableParams, blockInfo)) {
                if (action == MotionEvent.ACTION_UP) {
                    ((FClickableSpan) span).onClick(v);
                    return true;
                } else {
                    handleEvent = true;
                }
            }
        }
        return handleEvent;
    }

    @Override
    public void addCallback(Drawable.Callback callback) {
        if (type != NEED_SET_CALLBACK_DRAWABLE)
            return;
        boolean needAdd = true;
        for (int i = callbacks.size() - 1; i >= 0 ;i --) {
            WeakReference<Drawable.Callback> ref = callbacks.get(i);
            Drawable.Callback c = ref != null ? ref.get() : null;
            if (c == null) {
                callbacks.remove(i);
                continue;
            }
            if (c == callback) {
                needAdd = false;
                continue;
            }
        }
        if (needAdd)
            callbacks.add(new WeakReference<Drawable.Callback>(callback));
        if (!hasSetCallback && hasCreateNewDrawableForSpecialDrawable) {
            ((CallbackObserver)specialDrawable).onCallbackSet(this);
            hasSetCallback = true;
        }
    }

    @Override
    public void removeCallback(Drawable.Callback callback) {
        for (int i = callbacks.size() - 1; i >= 0 ;i --) {
            WeakReference<Drawable.Callback> ref = callbacks.get(i);
            Drawable.Callback c = ref != null ? ref.get() : null;
            if (c == null || c == callback) {
                callbacks.remove(i);
                continue;
            }
        }
    }

    private boolean isPointInThisBlock(float x, float y, @NonNull ImmutableParams immutableParams, @NonNull ClickSpanBlockInfo blockInfo) {
        long flag = blockInfo.blockFlag;
        final int h = MeasureTextUtils.getMaxHeight(flag);
        if (h <= 0)
            return false;
        final int cl = MeasureTextUtils.getCurrentLeft(flag);
        if (cl == 0)
            return false;
        final int lines = MeasureTextUtils.getLines(flag);
        if (lines == 0)
            return false;
        x -= immutableParams.left;
        y -= immutableParams.top;
        if (x < 0 || y < 0)
            return false;
        final int left = blockInfo.blockLeft;
        final int top = blockInfo.blockTop;
        final int b = h + top;
        if (lines == 1) {
            return x >= left && x <= cl && y >= top && y <= b;
        }
        final int vr = immutableParams.right;
        if (x > vr || y > b)
            return false;
        float lh = (float)h / lines;
        if (left < cl) {
            if (isPointInRect(x, y, left, top, vr, b))
                return true;
            if (isPointInRect(x, y, 0, (int) (top + lh), left, b))
                return true;
            if (isPointInRect(x, y, cl, top, vr, (int) (b - lh)))
                return true;
            return false;
        } else {
            if (isPointInRect(x, y, 0, (int) (top + lh), cl, b))
                return true;
            if (isPointInRect(x, y, cl, top, left, (int) (b - lh)))
                return true;
            if (isPointInRect(x, y, left, top, vr, (int) (b - lh)))
                return true;
            return false;
        }
    }

    private boolean isPointInRect(float x, float y, int l, int t, int r, int b) {
        return x >= l && x <= r && y >= t && y <= b;
    }

    public static DefaultDrawableBlock createTextBlock(CharSequence text, @Nullable SpecialStyleParams params) {
        DefaultDrawableBlock block = DefaultDrawableBlock.obtain();
        block.mText = text;
        block.type = TEXT;
        block.textStyleParams = params;
        return block;
    }

    public static DefaultDrawableBlock createDrawableBlock(CharSequence text, int res) {
        DefaultDrawableBlock block = DefaultDrawableBlock.obtain();
        block.mText = text;
        block.type = DRAWABLE;
        block.drawableRes = res;
        return block;
    }

    public static DefaultDrawableBlock createSpecialDrawableBlock(CharSequence text, Drawable d) {
        DefaultDrawableBlock block = DefaultDrawableBlock.obtain();
        block.mText = text;
        block.type = SPECIAL_DRAWABLE;
        block.specialDrawable = d;
        return block;
    }

    public static DefaultDrawableBlock createNeedSetCallbackDrawableBlock(CharSequence text, Drawable d) {
        if (!(d instanceof CallbackObserver)) {
            throw new IllegalDrawableException("drawble " + d.getClass().getName() + " is not a CallbackObserver");
        }
        DefaultDrawableBlock block = DefaultDrawableBlock.obtain();
        block.mText = text;
        block.type = NEED_SET_CALLBACK_DRAWABLE;
        block.specialDrawable = d;
        return block;
    }

    public static DefaultDrawableBlock createSpanBlock(CharSequence text, BaseSpan span, DefaultDrawableBlockList children) {
        DefaultDrawableBlock block = DefaultDrawableBlock.obtain();
        block.mText = text;
        block.type = SPAN;
        block.children = children;
        block.span = span;
        block.textStyleParams = SpecialStyleParams.obtain(span);
        for (int i = 0, l = children.size(); i < l;i ++) {
            DefaultDrawableBlock b = children.get(i);
            if (b != null && b.textStyleParams == null) {
                b.textStyleParams = block.textStyleParams;
            }
        }
        return block;
    }

    public static DefaultDrawableBlock createNextLineBlock() {
        DefaultDrawableBlock block = DefaultDrawableBlock.obtain();
        block.mText = "\n";
        block.type = NEXTLINE;
        return block;
    }

    @Override
    public void invalidateDrawable(@NonNull Drawable who) {
        for (int i = 0, l = callbacks.size(); i < l; i ++) {
            WeakReference<Drawable.Callback> ref = callbacks.get(i);
            Drawable.Callback callback = ref != null ? ref.get() : null;
            if (callback != null)
                callback.invalidateDrawable(who);
        }
    }

    @Override
    public void scheduleDrawable(@NonNull Drawable who, @NonNull Runnable what, long when) {
        int i = 0;
        int len = callbacks.size();
        if (len <= i)
            return;
        WeakReference<Drawable.Callback> ref = callbacks.get(i++);
        Drawable.Callback callback = ref != null ? ref.get() : null;
        while (callback == null && len > i) {
            ref = callbacks.get(i);
            callback = ref != null ? ref.get() : null;
        }
        if (callback != null)
            callback.scheduleDrawable(who, what, when);
    }

    @Override
    public void unscheduleDrawable(@NonNull Drawable who, @NonNull Runnable what) {
        for (int i = 0, l = callbacks.size(); i < l; i ++) {
            WeakReference<Drawable.Callback> ref = callbacks.get(i);
            Drawable.Callback callback = ref != null ? ref.get() : null;
            if (callback != null)
                callback.unscheduleDrawable(who, what);
        }
    }
}
