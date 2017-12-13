package xfy.fakeview.library.fview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.MotionEvent;

import java.util.ArrayList;

import xfy.fakeview.library.fview.utils.FMeasureSpec;

/**
 * Created by XiongFangyu on 2017/11/7.
 *
 * like {@link android.view.ViewGroup}
 */
public abstract class FViewGroup extends FView implements IFViewGroup {
    protected static final String TAG = "FViewGroup----";
    protected final ArrayList<FView> mChildren = new ArrayList<>(12);

    public FViewGroup(Context context, IFViewRoot viewRoot) {
        super(context, viewRoot);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        for (int i = 0, l = mChildren.size(); i < l; i ++) {
            FView child = mChildren.get(i);
            child.onAttachedToWindow();
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        for (int i = 0, l = mChildren.size(); i < l; i ++) {
            FView child = mChildren.get(i);
            child.onDetachedFromWindow();
        }
    }

    @Override
    public void measureChildWithMargins(FView child,
                                           int parentWidthMeasureSpec, int widthUsed,
                                           int parentHeightMeasureSpec, int heightUsed) {
        final FLayoutParams lp = child.getFLayoutParams();

        final int childWidthMeasureSpec = FMeasureSpec.getChildMeasureSpec(parentWidthMeasureSpec,
                padding.left + padding.right + lp.leftMargin + lp.rightMargin
                        + widthUsed, lp.width);
        final int childHeightMeasureSpec = FMeasureSpec.getChildMeasureSpec(parentHeightMeasureSpec,
                padding.top + padding.bottom + lp.topMargin + lp.bottomMargin
                        + heightUsed, lp.height);

        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }

    @Override
    public abstract void onLayout(boolean changed, int l, int t, int r, int b);

    @Override
    public void invalidateChild(FView child, Rect rect) {
        int l = rect.left + child.getLeft();
        int t = rect.top + child.getTop();
        Rect childBound = new Rect(
                l, t, l + rect.width(), t + rect.height());
        if (parent != null) {
            parent.invalidateChild(this, childBound);
        }
    }

    @Override
    public void addView(FView child) {
        addView(child, -1);
    }

    @Override
    public void addView(FView child, int index) {
        if (child == null) {
            throw new IllegalArgumentException("Cannot add a null child view to a ViewGroup");
        }
        FLayoutParams params = child.getFLayoutParams();
        if (params == null) {
            params = generateDefaultLayoutParams();
            if (params == null) {
                throw new IllegalArgumentException("generateDefaultLayoutParams() cannot return null");
            }
        }
        addView(child, index, params);
    }

    @Override
    public void addView(FView child, FLayoutParams params) {
        addView(child, -1, params);
    }

    @Override
    public void addView(FView child, int index, FLayoutParams params) {
        if (child == null) {
            throw new IllegalArgumentException("Cannot add a null child view to a ViewGroup");
        }
        requestFViewTreeLayout();
        invalidate();
        addViewInner(child, index, params);
    }

    protected void addViewInner(FView child, int index, FLayoutParams params) {
        if (child.getFParent() != null) {
            throw new IllegalStateException("The specified child already has a parent. " +
                    "You must call removeView() on the child's parent first.");
        }
        if (!checkLayoutParams(params)) {
            params = generateLayoutParams(params);
        }
        child.setFLayoutParams(params);
        if (index < 0)
            index = getChildCount();
        mChildren.add(index, child);
        child.parent = this;
    }

    @Override
    public FLayoutParams generateDefaultLayoutParams() {
        return new FLayoutParams(FLayoutParams.WRAP_CONTENT, FLayoutParams.WRAP_CONTENT);
    }

    protected boolean checkLayoutParams(FLayoutParams p) {
        return  p != null;
    }

    /**
     * Returns a safe set of layout parameters based on the supplied layout params.
     * When a ViewGroup is passed a View whose layout params do not pass the test of
     * {@link #checkLayoutParams(FLayoutParams)}, this method
     * is invoked. This method should return a new set of layout params suitable for
     * this ViewGroup, possibly by copying the appropriate attributes from the
     * specified set of layout params.
     *
     * @param p The layout parameters to convert into a suitable set of layout parameters
     *          for this ViewGroup.
     *
     * @return an instance of {@link FLayoutParams} or one
     *         of its descendants
     */
    protected FLayoutParams generateLayoutParams(FLayoutParams p) {
        return p;
    }

    @Override
    public int getChildCount() {
        return mChildren.size();
    }

    @Override
    public FView getChildAt(int index) {
        if (index < 0 || index >= getChildCount())
            return null;
        return mChildren.get(index);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        final int childCount = getChildCount();
        for (int i = 0; i < childCount ; i ++) {
            FView view = mChildren.get(i);
            canvas.save();
            canvas.translate(view.bounds.left, view.bounds.top);
            canvas.clipRect(0, 0, view.bounds.width(), view.bounds.height());
            view.draw(canvas);
            canvas.restore();
        }
    }

    @Override
    public FView findViewTraversal(int id) {
        if (id == mId) {
            return this;
        }
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i ++) {
            FView child = mChildren.get(i);
            child = child.findFViewById(id);
            if (child != null)
                return child;
        }
        return null;
    }

    @Override
    public boolean isTouchInView(float x, float y) {
        if (!isEnabled() || getVisibility() == GONE)
            return false;
        return bounds.contains((int) x, (int) y);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        if (!isTouchInView(x, y))
            return false;
        final int childCount = getChildCount();
        boolean handle = false;
        for (int i = childCount - 1; i >= 0 ; i --) {
            FView child = mChildren.get(i);
            handle = child.dispatchTouchEvent(event);
            if (handle)
                break;
        }
        if (!handle)
            handle = onTouchEvent(event);
        return handle;
    }

    @Override
    public void onSetLayoutParams(FView child, FLayoutParams params) {}

    @Override
    public int getOldWidthMeasureSpec() {
        return oldWidthMeasureSpec;
    }

    @Override
    public int getOldHeightMeasureSpec() {
        return oldHeightMeasureSpec;
    }

    public int indexOfChild(FView child) {
        return mChildren.indexOf(child);
    }

    public void removeView(FView view) {
        removeViewAt(mChildren.indexOf(view));
    }

    public void removeViewAt(int index) {
        removeViews(index, 1);
    }

    public void removeViews(int start, int count) {
        removeViewsInternal(start, count);
        requestFViewTreeLayout();
        invalidate();
    }

    public void removeAllViews() {
        removeViews(0, getChildCount());
    }

    private void removeViewsInternal(int start, int count) {
        final int end = start + count;
        final int lastChildCount = getChildCount();
        if (start < 0 || count < 0 || end > lastChildCount) {
            throw new IndexOutOfBoundsException();
        }
        for (int i = start; i < end; i ++) {
            FView child = mChildren.remove(start);
            child.parent = null;
        }
    }
}
