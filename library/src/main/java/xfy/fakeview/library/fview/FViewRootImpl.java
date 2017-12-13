package xfy.fakeview.library.fview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.HashSet;
import java.util.Set;

import xfy.fakeview.library.DebugInfo;
import xfy.fakeview.library.fview.utils.FMeasureSpec;

/**
 * Created by XiongFangyu on 2017/11/7.
 *
 * special view whitch contain all FView
 *
 */
public class FViewRootImpl extends View implements IFViewRoot {
    private static final String TAG = "FViewRootImpl";

    private FView mTargetView;
    private boolean attached = false;
    private int oldWidthMeasureSpec;
    private int oldHeightMeasureSpec;
    /**
     * runnable set, added by {@link #post(Runnable)} method
     * remove by {@link #removeCallbacks(Runnable)}, {@link #removeAllCallbacks()},
     * or action done.
     */
    private Set<WrapperRunnable> runnables = new HashSet<>();

    public FViewRootImpl(Context context) {
        this(context, null);
    }

    public FViewRootImpl(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FViewRootImpl(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setWillNotDraw(false);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mTargetView != null)
            mTargetView.onAttachedToWindow();
        attached = true;
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mTargetView != null)
            mTargetView.onDetachedFromWindow();
        attached = false;
    }

    @Override
    public void layoutChild(FView view) {
        if (mTargetView == null)
            return;
        long start = System.currentTimeMillis();
        if (view == mTargetView) {
            int pl = getPaddingLeft();
            int pt = getPaddingTop();
            int pr = getPaddingRight();
            int pb = getPaddingBottom();
            int w = getWidth();
            int h = getHeight();
            view.layout(pl, pt, w - pl - pr, h - pt - pb);
        }
        d(TAG, "layout fView cast: " + (System.currentTimeMillis() - start));
    }

    @Override
    public boolean post(Runnable action) {
        boolean result = super.post(action);
        if (result)
            runnables.add(new WrapperRunnable(action));
        return result;
    }

    @Override
    public boolean postDelayed(Runnable action, long delayMillis) {
        boolean result = super.postDelayed(action, delayMillis);
        if (result)
            runnables.add(new WrapperRunnable(action));
        return result;
    }

    public boolean removeCallbacks(Runnable action) {
        boolean result = super.removeCallbacks(action);
        runnables.remove(action);
        return result;
    }

    @Override
    public void onSetLayoutParams(FView child, IFViewGroup.FLayoutParams params) {

    }

    @Override
    public int getOldWidthMeasureSpec() {
        return oldWidthMeasureSpec;
    }

    @Override
    public int getOldHeightMeasureSpec() {
        return oldHeightMeasureSpec;
    }

    @Override
    public void removeAllCallbacks() {
        for (Runnable a : runnables) {
            if (a != null)
                removeCallbacks(a);
        }
        runnables.clear();
    }

    @Override
    public void requestLayoutAll() {
        requestLayout();
    }

    @Override
    public void invalidateChild(FView child, Rect rect) {
        invalidate(rect);
    }

    @Override
    public void setTargetChild(FView view) {
        final FView old = mTargetView;
        if (old != view && old != null) {
            old.isRootView = false;
            old.onDetachedFromWindow();
            old.parent = null;
        }
        mTargetView = view;
        if (view != null) {
            view.isRootView = true;
            view.parent = this;
            if (attached)
                view.onAttachedToWindow();
        }
    }

    @Override
    public FView getTargetChild() {
        return mTargetView;
    }

    @Override
    public FView findFViewById(int id) {
        if (id <= 0 || mTargetView == null)
            return null;
        return mTargetView.findFViewById(id);
    }

    @Override
    public void requestFViewTreeLayout() {
        if (mTargetView == null) {
            Log.e(TAG, "requestFViewTreeLayout no target view");
            return;
        }
        post(new Runnable() {
            @Override
            public void run() {
                if (mTargetView != null) {
                    measureChildWithMargins(mTargetView, oldWidthMeasureSpec, 0, oldHeightMeasureSpec, 0);
                    layoutChild(mTargetView);
                } else {
                    Log.e(TAG, "requestFViewTreeLayout no target view in action");
                }
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mTargetView != null) {
            measureChildWithMargins(mTargetView, widthMeasureSpec, 0, heightMeasureSpec, 0);
            final IFViewGroup.FLayoutParams lp = mTargetView.getFLayoutParams();
            final int maxWidth = mTargetView.getMeasuredWidth() + lp.leftMargin + lp.rightMargin
                    + getPaddingLeft() + getPaddingRight();
            final int maxHeight = mTargetView.getMeasuredHeight() + lp.topMargin + lp.bottomMargin
                    + getPaddingTop() + getPaddingBottom();
            setMeasuredDimension(
                    FMeasureSpec.resolveSizeAndState(maxWidth, widthMeasureSpec, mTargetView.getMeasuredState()),
                    FMeasureSpec.resolveSizeAndState(maxHeight, heightMeasureSpec,
                            mTargetView.getMeasuredState() << MEASURED_HEIGHT_STATE_SHIFT));
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
        oldWidthMeasureSpec = widthMeasureSpec;
        oldHeightMeasureSpec = heightMeasureSpec;
    }

    @Override
    public void measureChildWithMargins(FView child,
                                        int parentWidthMeasureSpec, int widthUsed,
                                        int parentHeightMeasureSpec, int heightUsed) {
        final IFViewGroup.FLayoutParams lp = child.getFLayoutParams();

        final int childWidthMeasureSpec = FMeasureSpec.getChildMeasureSpec(parentWidthMeasureSpec,
                getPaddingLeft() + getPaddingRight() + lp.leftMargin + lp.rightMargin
                        + widthUsed, lp.width);
        final int childHeightMeasureSpec = FMeasureSpec.getChildMeasureSpec(parentHeightMeasureSpec,
                getPaddingTop() + getPaddingBottom() + lp.topMargin + lp.bottomMargin
                        + heightUsed, lp.height);

        long start = System.currentTimeMillis();
        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
        d(TAG, "measure fView cast: " + (System.currentTimeMillis() - start));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mTargetView != null) {
            long start = System.currentTimeMillis();
            mTargetView.draw(canvas);
            d(TAG, "draw fView cast: " + (System.currentTimeMillis() - start));
        }
    }

    /**
     * 考虑一下放在 {@link #dispatchTouchEvent(MotionEvent)}中
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mTargetView != null) {
            return mTargetView.dispatchTouchEvent(event);
        }
        return super.onTouchEvent(event);
    }

    private void d(String tag, String msg) {
        if (DebugInfo.DEBUG) {
            Log.d(tag, msg);
        }
    }

    class WrapperRunnable implements Runnable {
        Runnable action;

        WrapperRunnable(Runnable action) {
            this.action = action;
        }

        @Override
        public void run() {
            if (action != null)
                action.run();
            runnables.remove(this);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this)
                return true;
            if (obj == action)
                return true;
            if (obj instanceof WrapperRunnable) {
                return ((WrapperRunnable) obj).action == action;
            }
            return false;
        }
    }
}
