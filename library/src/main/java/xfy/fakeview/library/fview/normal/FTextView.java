package xfy.fakeview.library.fview.normal;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.DynamicLayout;
import android.text.Layout;
import android.text.Spannable;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;

import xfy.fakeview.library.fview.utils.FMeasureSpec;
import xfy.fakeview.library.fview.FView;
import xfy.fakeview.library.fview.IFViewGroup;
import xfy.fakeview.library.fview.IFViewRoot;

/**
 * Created by XiongFangyu on 2017/11/8.
 *
 * like {@link android.widget.TextView}
 */
public class FTextView extends FView {

    private int mTextColor = Color.BLACK;

    private CharSequence mText;

    private final TextPaint mTextPaint;
    private Layout mLayout;
    private TextUtils.TruncateAt mEllipsize;
    private float mSpacingMult = 1.0f;
    private float mSpacingAdd = 0.0f;
    private boolean mIncludePad = true;

    private int mGravity = Gravity.TOP | Gravity.START;

    public FTextView(Context context, IFViewRoot viewRoot) {
        super(context, viewRoot);
        mText = "";

        final Resources res = getResources();
        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.density = res.getDisplayMetrics().density;
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = FMeasureSpec.getMode(widthMeasureSpec);
        int heightMode = FMeasureSpec.getMode(heightMeasureSpec);
        int widthSize = FMeasureSpec.getSize(widthMeasureSpec);
        int heightSize = FMeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        int des = -1;
        if (widthMode == FMeasureSpec.EXACTLY) {
            // Parent has told us how big to be. So be it.
            width = widthSize;
        } else {
            if (mLayout != null && mEllipsize == null) {
                des = desired(mLayout);
            }
            if (des < 0) {
                des = (int) Math.ceil(Layout.getDesiredWidth(mText, mTextPaint));
            }
            width = des;
            width += getPaddingLeft() + getPaddingRight();
            width = Math.max(width, getSuggestedMinimumWidth());
            if (widthMode == FMeasureSpec.AT_MOST) {
                width = Math.min(widthSize, width);
            }
        }

        int want = width - getPaddingLeft() - getPaddingRight();
        if (mLayout == null) {
            makeNewLayout(want, want);
        } else {
            final boolean layoutChanged = (mLayout.getWidth() != want)
                    || (mLayout.getEllipsizedWidth() != want);
            if (layoutChanged)
                mLayout.increaseWidthTo(want);
        }

        if (heightMode == FMeasureSpec.EXACTLY) {
            // Parent has told us how big to be. So be it.
            height = heightSize;
        } else {
            int desired = getDesiredHeight(mLayout);
            height = desired;
            if (heightMode == FMeasureSpec.AT_MOST) {
                height = Math.min(desired, heightSize);
            }
        }
        setMeasuredDimension(width, height);
    }

    public void setTextColor(int color) {
        if (mTextColor != color) {
            mTextColor = color;
            invalidate();
        }
    }

    public int getTextColor() {
        return mTextColor;
    }

    public void setEllipsize(TextUtils.TruncateAt ellipsize) {
        if (mEllipsize != ellipsize) {
            mEllipsize = ellipsize;
            requestFViewTreeLayout();
            invalidate();
        }
    }

    public float getTextSize() {
        return mTextPaint.getTextSize();
    }

    public void setTextSize(float size) {
        setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
    }

    public void setTextSize(int unit, float size) {
        Context c = getContext();
        Resources r;

        if (c == null) {
            r = Resources.getSystem();
        } else {
            r = c.getResources();
        }

        float px = TypedValue.applyDimension(unit, size, r.getDisplayMetrics());
        if (px != mTextPaint.getTextSize()) {
            mTextPaint.setTextSize(px);
            if (mLayout != null) {
                mLayout = null;
                requestFViewTreeLayout();
                invalidate();
            }
        }
    }

    public void setText(CharSequence text) {
        setTextInnternal(text);
    }

    private void setTextInnternal(CharSequence text) {
        text = TextUtils.stringOrSpannedString(text);
        mText = text;
        if (mLayout != null) {
            checkForRelayout();
        }
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mLayout == null) {
            assumeLayout();
        }
        mTextPaint.setColor(mTextColor);
        if (padding.left == 0 && padding.top == 0) {
            mLayout.draw(canvas);
        } else {
            canvas.save();
            canvas.translate(padding.left, padding.top);
            mLayout.draw(canvas);
            canvas.restore();
        }
    }

    private void assumeLayout() {
        int width = bounds.right - bounds.left - padding.left - padding.right;

        if (width < 1) {
            width = 0;
        }

        makeNewLayout(width, width);
    }

    private Layout.Alignment getLayoutAlignment() {
        Layout.Alignment alignment;
        switch (mGravity & Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK) {
            case Gravity.START:
                alignment = Layout.Alignment.ALIGN_NORMAL;
                break;
            case Gravity.END:
                alignment = Layout.Alignment.ALIGN_OPPOSITE;
                break;
            case Gravity.LEFT:
                alignment = Layout.Alignment.ALIGN_NORMAL;
                break;
            case Gravity.RIGHT:
                alignment = Layout.Alignment.ALIGN_OPPOSITE;
                break;
            case Gravity.CENTER_HORIZONTAL:
                alignment = Layout.Alignment.ALIGN_CENTER;
                break;
            default:
                alignment = Layout.Alignment.ALIGN_NORMAL;
                break;
        }
        return alignment;
    }

    private void checkForRelayout() {
        // If we have a fixed width, we can just swap in a new text layout
        // if the text height stays the same or if the view height is fixed.

        if (mFLayoutParams.width != IFViewGroup.FLayoutParams.WRAP_CONTENT
                && (bounds.right - bounds.left - padding.left - padding.right > 0)) {
            // Static width, so try making a new text layout.

            int oldht = mLayout.getHeight();
            int want = mLayout.getWidth();

            /*
             * No need to bring the text into view, since the size is not
             * changing (unless we do the requestLayout(), in which case it
             * will happen at measure).
             */
            makeNewLayout(want, bounds.right - bounds.left - padding.left - padding.right);

            if (mEllipsize != TextUtils.TruncateAt.MARQUEE) {
                // In a fixed-height view, so use our new text layout.
                if (mFLayoutParams.height != IFViewGroup.FLayoutParams.WRAP_CONTENT
                        && mFLayoutParams.height != IFViewGroup.FLayoutParams.MATCH_PARENT) {
                    invalidate();
                    return;
                }

                // Dynamic height, but height has stayed the same,
                // so use our new text layout.
                if (mLayout.getHeight() == oldht) {
                    invalidate();
                    return;
                }
            }

            // We lose: the height has changed and we have a dynamic height.
            // Request a new view layout using our new text layout.
            requestFViewTreeLayout();
            invalidate();
        } else {
            mLayout = null;
            requestFViewTreeLayout();
            invalidate();
        }
    }

    private void makeNewLayout(int wantWidth, int ellipsisWidth) {
        if (wantWidth < 0) {
            wantWidth = 0;
        }
        Layout.Alignment alignment = getLayoutAlignment();
        boolean shouldEllipsize = mEllipsize != null;

        mLayout = makeSingleLayout(wantWidth, ellipsisWidth, alignment, shouldEllipsize,
                mEllipsize, mSpacingMult, mSpacingAdd, mIncludePad);
    }

    private Layout makeSingleLayout(int wantWidth, int ellipsisWidth,
                                    Layout.Alignment alignment, boolean shouldEllipsize,
                                    TextUtils.TruncateAt effectiveEllipsize,
                                    float spacingMult, float spacingAdd, boolean includePad) {
        Layout result = null;
        if (mText instanceof Spannable) {
            result = new DynamicLayout(mText, mText, mTextPaint, wantWidth,
                    alignment, spacingMult, spacingAdd, includePad,
                    effectiveEllipsize, ellipsisWidth);
        }
        if (result == null) {
            if (shouldEllipsize) {
                result = new StaticLayout(mText, 0, mText.length(),
                        mTextPaint, wantWidth, alignment, spacingMult, spacingAdd, includePad,
                        effectiveEllipsize, ellipsisWidth);
            } else {
                result = new StaticLayout(mText, 0, mText.length(),
                        mTextPaint, wantWidth, alignment, spacingMult, spacingAdd, includePad);
            }
        }
        return result;
    }

    private static int desired(Layout layout) {
        int n = layout.getLineCount();
        CharSequence text = layout.getText();
        float max = 0;

        // if any line was wrapped, we can't use it.
        // but it's ok for the last line not to have a newline

        for (int i = 0; i < n - 1; i++) {
            if (text.charAt(layout.getLineEnd(i) - 1) != '\n') {
                return -1;
            }
        }

        for (int i = 0; i < n; i++) {
            max = Math.max(max, layout.getLineWidth(i));
        }

        return (int) Math.ceil(max);
    }

    private int getDesiredHeight(Layout layout) {
        if (layout == null) {
            return 0;
        }
        int desired = layout.getHeight();
        final int padding = getPaddingTop() + getPaddingBottom();
        desired += padding;
        desired = Math.max(desired, getSuggestedMinimumHeight());
        return desired;
    }
}
