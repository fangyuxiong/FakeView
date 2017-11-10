package xfy.fakeview.library.fview.normal;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;

import xfy.fakeview.library.fview.FView;
import xfy.fakeview.library.fview.IFViewRoot;

import static xfy.fakeview.library.fview.utils.FMeasureSpec.resolveSizeAndState;

/**
 * Created by XiongFangyu on 2017/11/7.
 *
 * like {@link android.widget.ImageView}
 */
public class FImageView extends FView {

    private Drawable mDrawable;
    private int mResource;

    private int mDrawableWidth;
    private int mDrawableHeight;

    private ScaleType mScaleType = ScaleType.FIT_CENTER;
    private Matrix mDrawMatrix;
    private Matrix mMatrix;

    private ColorFilter mColorFilter = null;

    private final RectF mTempSrc = new RectF();
    private final RectF mTempDst = new RectF();

    private boolean hasFrame = false;

    public FImageView(Context context, IFViewRoot viewRoot) {
        super(context, viewRoot);
        mMatrix = new Matrix();
    }

    public void setScaleType(ScaleType scaleType) {
        if (scaleType == null) {
            throw new NullPointerException();
        }
        if (mScaleType != scaleType) {
            mScaleType = scaleType;
            requestFViewTreeLayout();
            invalidate();
        }
    }

    public void setImageBitmap(Bitmap bitmap) {
        if (mDrawable instanceof BitmapDrawable) {
            if (((BitmapDrawable) mDrawable).getBitmap() == bitmap) {
                return;
            }
        }
        setImageDrawable(new BitmapDrawable(bitmap));
    }

    public void setImageResource(@DrawableRes int res) {
        final int oldWidth = mDrawableWidth;
        final int oldHeight = mDrawableHeight;

        updateDrawable(null);
        mResource = res;

        resolveRes();

        if (oldWidth != mDrawableWidth || oldHeight != mDrawableHeight) {
            requestFViewTreeLayout();
        }
        invalidate();
    }

    public void setImageDrawable(Drawable drawable) {
        if (mDrawable != drawable) {
            mResource = 0;
            final int oldWidth = mDrawableWidth;
            final int oldHeight = mDrawableHeight;
            updateDrawable(drawable);
            if (oldWidth != mDrawableWidth || oldHeight != mDrawableHeight) {
                requestFViewTreeLayout();
            }
            invalidate();
        }
    }

    private void resolveRes() {
        if (mDrawable != null) {
            return;
        }

        if (getResources() == null) {
            return;
        }

        Drawable d = null;

        if (mResource != 0) {
            try {
                d = getResources().getDrawable(mResource);
            } catch (Exception e) {
                // Don't try again.
            }
        } else {
            return;
        }
        updateDrawable(d);
    }

    private void updateDrawable(Drawable d) {
        if (mDrawable != null) {
            mDrawable.setCallback(null);
            unscheduleDrawable(mDrawable);
            if (isAttachedToWindow()) {
                mDrawable.setVisible(false, false);
            }
        }

        mDrawable = d;

        if (d != null) {
            d.setCallback(this);
            if (isAttachedToWindow()) {
                d.setVisible(getVisibility() == VISIBLE, true);
            }
            mDrawableWidth = d.getIntrinsicWidth();
            mDrawableHeight = d.getIntrinsicHeight();
            applyColorMod();

            configureBounds();
        } else {
            mDrawableWidth = mDrawableHeight = -1;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        resolveRes();
        int w;
        int h;

        if (mDrawable == null) {
            // If no drawable, its intrinsic size is 0.
            mDrawableWidth = -1;
            mDrawableHeight = -1;
            w = h = 0;
        } else {
            w = mDrawableWidth;
            h = mDrawableHeight;
            if (w <= 0) w = 1;
            if (h <= 0) h = 1;
        }

        final int pleft = padding.left;
        final int pright = padding.right;
        final int ptop = padding.top;
        final int pbottom = padding.bottom;

        int widthSize;
        int heightSize;

        w += pleft + pright;
        h += ptop + pbottom;
        w = Math.max(w, getSuggestedMinimumWidth());
        h = Math.max(h, getSuggestedMinimumHeight());

        widthSize = resolveSizeAndState(w, widthMeasureSpec, 0);
        heightSize = resolveSizeAndState(h, heightMeasureSpec, 0);

        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void setFrame(int l, int t, int r, int b) {
        super.setFrame(l, t, r, b);
        hasFrame = true;
        configureBounds();
    }

    private void configureBounds() {
        if (mDrawable == null || !hasFrame) {
            return;
        }

        final int dwidth = mDrawableWidth;
        final int dheight = mDrawableHeight;

        final int vwidth = getWidth() - padding.left - padding.right;
        final int vheight = getHeight() - padding.top - padding.bottom;

        final boolean fits = (dwidth < 0 || vwidth == dwidth)
                && (dheight < 0 || vheight == dheight);

        if (dwidth <= 0 || dheight <= 0 || ScaleType.FIT_XY == mScaleType) {
            /* If the drawable has no intrinsic size, or we're told to
                scaletofit, then we just fill our entire view.
            */
            mDrawable.setBounds(0, 0, vwidth, vheight);
            mDrawMatrix = null;
        } else {
            // We need to do the scaling ourself, so have the drawable
            // use its native size.
            mDrawable.setBounds(0, 0, dwidth, dheight);

            if (ScaleType.MATRIX == mScaleType) {
                // Use the specified matrix as-is.
                if (mMatrix.isIdentity()) {
                    mDrawMatrix = null;
                } else {
                    mDrawMatrix = mMatrix;
                }
            } else if (fits) {
                // The bitmap fits exactly, no transform needed.
                mDrawMatrix = null;
            } else if (ScaleType.CENTER == mScaleType) {
                // Center bitmap in view, no scaling.
                mDrawMatrix = mMatrix;
                mDrawMatrix.setTranslate(Math.round((vwidth - dwidth) * 0.5f),
                        Math.round((vheight - dheight) * 0.5f));
            } else if (ScaleType.CENTER_CROP == mScaleType) {
                mDrawMatrix = mMatrix;

                float scale;
                float dx = 0, dy = 0;

                if (dwidth * vheight > vwidth * dheight) {
                    scale = (float) vheight / (float) dheight;
                    dx = (vwidth - dwidth * scale) * 0.5f;
                } else {
                    scale = (float) vwidth / (float) dwidth;
                    dy = (vheight - dheight * scale) * 0.5f;
                }

                mDrawMatrix.setScale(scale, scale);
                mDrawMatrix.postTranslate(Math.round(dx), Math.round(dy));
            } else if (ScaleType.CENTER_INSIDE == mScaleType) {
                mDrawMatrix = mMatrix;
                float scale;
                float dx;
                float dy;

                if (dwidth <= vwidth && dheight <= vheight) {
                    scale = 1.0f;
                } else {
                    scale = Math.min((float) vwidth / (float) dwidth,
                            (float) vheight / (float) dheight);
                }

                dx = Math.round((vwidth - dwidth * scale) * 0.5f);
                dy = Math.round((vheight - dheight * scale) * 0.5f);

                mDrawMatrix.setScale(scale, scale);
                mDrawMatrix.postTranslate(dx, dy);
            } else {
                // Generate the required transform.
                mTempSrc.set(0, 0, dwidth, dheight);
                mTempDst.set(0, 0, vwidth, vheight);

                mDrawMatrix = mMatrix;
                mDrawMatrix.setRectToRect(mTempSrc, mTempDst, scaleTypeToScaleToFit(mScaleType));
            }
        }
    }

    private void applyColorMod() {
        if (mDrawable != null) {
            mDrawable = mDrawable.mutate();
            if (mColorFilter != null) {
                mDrawable.setColorFilter(mColorFilter);
            }
        }
    }

    @Override
    public void invalidateDrawable(@NonNull Drawable dr) {
        if (dr == mDrawable) {
            if (dr != null) {
                // update cached drawable dimensions if they've changed
                final int w = dr.getIntrinsicWidth();
                final int h = dr.getIntrinsicHeight();
                if (w != mDrawableWidth || h != mDrawableHeight) {
                    mDrawableWidth = w;
                    mDrawableHeight = h;
                    // updates the matrix, which is dependent on the bounds
                    configureBounds();
                }
            }
            invalidate();
        }
    }

    protected boolean verifyDrawable(Drawable who) {
        return who == mDrawable || super.verifyDrawable(who);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mDrawable == null) {
            return; // couldn't resolve the URI
        }

        if (mDrawableWidth == 0 || mDrawableHeight == 0) {
            return;     // nothing to draw (empty bounds)
        }

        if (mDrawMatrix == null && padding.top == 0 && padding.left == 0) {
            mDrawable.draw(canvas);
        } else {
            final int saveCount = canvas.getSaveCount();
            canvas.save();
            canvas.translate(padding.left, padding.top);

            if (mDrawMatrix != null) {
                canvas.concat(mDrawMatrix);
            }
            mDrawable.draw(canvas);
            canvas.restoreToCount(saveCount);
        }
    }

    private static final Matrix.ScaleToFit[] sS2FArray = {
            Matrix.ScaleToFit.FILL,
            Matrix.ScaleToFit.START,
            Matrix.ScaleToFit.CENTER,
            Matrix.ScaleToFit.END
    };

    private static Matrix.ScaleToFit scaleTypeToScaleToFit(ScaleType st) {
        // ScaleToFit enum to their corresponding Matrix.ScaleToFit values
        return sS2FArray[st.nativeInt - 1];
    }

    public enum ScaleType {
        MATRIX(0),
        FIT_XY(1),
        FIT_START(2),
        FIT_CENTER(3),
        FIT_END(4),
        CENTER(5),
        CENTER_CROP(6),
        CENTER_INSIDE(7);

        ScaleType(int ni) {
            nativeInt = ni;
        }

        final int nativeInt;
    }
}
