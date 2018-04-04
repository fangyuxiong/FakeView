package xfy.fakeview.library.text.drawer;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;

import xfy.fakeview.library.text.param.ImmutableParams;
import xfy.fakeview.library.text.param.VariableParams;
import xfy.fakeview.library.text.utils.IllegalDrawableException;

/**
 * Created by XiongFangyu on 2018/3/2.
 */
public class TextDrawableDrawer {

    private volatile static TextDrawableDrawer drawableDrawer;

    private static Context context;
    public static void init(Context c) {
        context = c.getApplicationContext();
    }

    protected TextDrawableDrawer() {
    }

    public static TextDrawableDrawer getDrawableDrawer() {
        if (drawableDrawer == null) {
            synchronized (TextDrawableDrawer.class) {
                if (drawableDrawer == null) {
                    drawableDrawer = new TextDrawableDrawer();
                }
            }
        }
        return drawableDrawer;
    }

    /**
     * 绘制一个Drawable resource
     * @param canvas
     * @param res
     * @param variableParams
     * @param immutableParams
     * @return resource对应的drawable，若此drawable可复制，则返回复制的drawable；反之返回获取到的drawable
     */
    public Drawable drawResource(@NonNull Canvas canvas, @DrawableRes int res,
                                        @NonNull VariableParams variableParams, @NonNull ImmutableParams immutableParams) {
        Drawable drawable = getSpecialDrawable(res, immutableParams.drawableHeight);
        return drawDrawable(canvas, drawable, false, variableParams, immutableParams);
    }

    /**
     * 绘制一个drawable
     * @param canvas
     * @param drawable
     * @param variableParams
     * @param immutableParams
     * @return 若此drawable可复制，则返回复制的drawable；反之返回获取到的drawable
     */
    public Drawable drawDrawable(Canvas canvas, Drawable drawable, boolean forceNewDrawable,
                                    @NonNull VariableParams variableParams, @NonNull ImmutableParams immutableParams) {
        drawable = getSpecialDrawable(drawable, immutableParams.drawableHeight, forceNewDrawable);
        drawSpecialDrawable(canvas, drawable, variableParams, immutableParams);
        return drawable;
    }

    public Drawable getSpecialDrawable(@DrawableRes int res, int drawableHeight) {
        return getSpecialDrawable(getResources().getDrawable(res), drawableHeight, false);
    }

    public Drawable getSpecialDrawable(Drawable drawable, int drawableHeight, boolean forceNewDrawable) {
        if (forceNewDrawable) {
            Drawable.ConstantState state = drawable.getConstantState();
            if (state != null) {
                Drawable newDrawable = state.newDrawable();
                if (newDrawable != null && newDrawable != drawable) {
                    drawable = newDrawable;
                } else {
                    throw new IllegalDrawableException("drawable " + drawable.getClass().getName() + " state return a null or a same drawable.");
                }
            } else {
                throw new IllegalDrawableException("drawable " + drawable.getClass().getName() + " return null constant state");
            }
        }

        initDrawableBounds(drawable, drawableHeight);
        return drawable;
    }

    public void drawSpecialDrawable(Canvas canvas, Drawable drawable,
                                           @NonNull VariableParams variableParams, @NonNull ImmutableParams immutableParams) {
        int dh = immutableParams.drawableHeight;
        initDrawableBounds(drawable, dh);
        int dw = measureDrawableWidth(drawable, dh);

        int maxWidth = TextDrawer.getDrawMaxWidthFronNow(variableParams, immutableParams);
        if (maxWidth < 0) {
            if (-maxWidth < dw) {
                TextDrawer.drawEllipsize(canvas, variableParams, immutableParams);
                return;
            }
        } else if (maxWidth < dw) {
            TextDrawer.drawNextLine(canvas, variableParams, immutableParams);
        }

        canvas.save();
        float scale = measureDrawableScale(drawable, dh);
        canvas.translate(variableParams.currentLeft, variableParams.currentTop);
        canvas.scale(scale, scale);
        drawable.draw(canvas);
        canvas.restore();
        variableParams.currentLeft += dw;
    }

    private static void initDrawableBounds(Drawable d, int dh) {
        Rect bounds = d.getBounds();
        int bw = bounds.width();
        int bh = bounds.height();
        int iw = d.getIntrinsicWidth();
        int ih = d.getIntrinsicHeight();
        if ((bw == 0 || bh == 0) || (bw * ih != bh * iw)) {
            if (iw == 0 || ih == 0) {
                d.setBounds(0, 0, dh, dh);
            } else {
                d.setBounds(0, 0, iw, ih);
            }
        }
    }

    protected Resources getResources() {
        return context.getResources();
    }

    public static int measureDrawableWidth(Drawable d, int drawableSize) {
        int iw = d.getIntrinsicWidth();
        int ih = d.getIntrinsicHeight();
        if (ih == 0)
            return drawableSize;
        return drawableSize * iw / ih;
    }

    private static float measureDrawableScale(Drawable d, int dh) {
        Rect bounds = d.getBounds();
        float bh = bounds.height();
        return dh / bh;
    }
}
