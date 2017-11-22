package xfy.fakeview.library.fview;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Size;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import java.util.ArrayList;
import java.util.HashMap;

import xfy.fakeview.library.DebugInfo;
import xfy.fakeview.library.fview.utils.FMeasureSpec;
import xfy.fakeview.library.fview.utils.FViewDebugTool;
import xfy.fakeview.library.fview.utils.ViewUtils;

/**
 * Created by XiongFangyu on 2017/11/7.
 *
 * 基础View
 *
 * FView只有一种构造方法，传入{@link Context}和{@link IFViewRoot}
 */
public class FView implements IFView, Drawable.Callback{
    protected static final String TAG = "FView-----";

    protected Context mContext;
    /**
     * view root
     * @see FViewRootImpl
     */
    protected IFViewRoot viewRoot;
    /**
     * parent
     * @see FViewGroup
     * @see FViewRootImpl
     */
    FViewParent parent;
    boolean isRootView = false;

    protected IFViewGroup.FLayoutParams mFLayoutParams;

    protected int mId = NO_ID;
    /**
     * private flag for:
     *  visibility
     *  enabled
     *  attached
     *  layout
     *  clickable
     *  long clickable
     *  pressed
     *  measure
     */
    private int flag;

    protected int measureWidth;
    protected int measureHeight;

    protected int oldWidthMeasureSpec;
    protected int oldHeightMeasureSpec;

    protected final Rect bounds = new Rect();
    protected final Rect padding = new Rect();

    private Drawable mBackground;
    private boolean mBackgroundSizeChanged;
    private int mBackgroundResource;
    /**
     * 保存由Drawable发出的事件
     * @see #scheduleDrawable(Drawable, Runnable, long)
     * @see #unscheduleDrawable(Drawable, Runnable)
     * @see #unscheduleDrawable(Drawable)
     */
    private final HashMap<Drawable, ArrayList<Runnable>> drawableActions;

    protected final ListenerInfo listenerInfo = new ListenerInfo();

    private boolean mHasPerformedLongPress = false;
    private CheckForLongPress mPendingCheckForLongPress;
    private UnsetPressedState mUnsetPressedState;
    /**
     * debug模式下绘制边框工具
     * @see DebugInfo#DEBUG
     */
    private FViewDebugTool debugTool;

    /**
     * The view's tag.
     * {@hide}
     *
     * @see #setTag(Object)
     * @see #getTag()
     */
    protected Object mTag = null;
    /**
     * Map used to store views' tags.
     */
    private SparseArray<Object> mKeyedTags;

    /**
     * FView
     * @param context
     * @param viewRoot
     */
    public FView(Context context, IFViewRoot viewRoot) {
        mContext = context;
        this.viewRoot = viewRoot;
        drawableActions = new HashMap<>();
    }

    @Override
    public void onAttachedToWindow() {
        addFlag(ATTACHED_MASK);
    }

    @Override
    public void onDetachedFromWindow() {
        removeFlag(ATTACHED_MASK);

        removeLongPressCallback();
        removeUnsetPressCallback();
        unscheduleDrawable(mBackground);
    }

    @Override
    public void setVisibility(@Visibility int visibility) {
        checkVisibilityMask(visibility);
        removeFlag(VISIBILITY_MASK);
        addFlag(visibility);
    }

    @Override
    public int getVisibility() {
        return getFlag(VISIBILITY_MASK);
    }

    protected static void checkVisibilityMask(int visibility) {
        if (visibility != VISIBLE || visibility != INVISIBLE || visibility != GONE)
            throw new IllegalArgumentException("param visibility can only use : [VISIBLE, INVISIBLE, GONE]");
    }

    @Override
    public void setEnabled(boolean enabled) {
        int flag = enabled ? ENABLED : DISABLED;
        removeFlag(ENABLED_MASK);
        addFlag(flag);
    }

    @Override
    public boolean isEnabled() {
        return getFlag(ENABLED_MASK) == ENABLED;
    }

    @Override
    public final void measure(int widthMeasureSpec, int heightMeasureSpec) {
        onMeasure(widthMeasureSpec, heightMeasureSpec);
        oldWidthMeasureSpec = widthMeasureSpec;
        oldHeightMeasureSpec = heightMeasureSpec;
    }

    /**
     * <p>
     * Measure the view and its content to determine the measured width and the
     * measured height. This method is invoked by {@link #measure(int, int)} and
     * should be overridden by subclasses to provide accurate and efficient
     * measurement of their contents.
     * </p>
     *
     * <p>
     * <strong>CONTRACT:</strong> When overriding this method, you
     * <em>must</em> call {@link #setMeasuredDimension(int, int)} to store the
     * measured width and height of this view. Failure to do so will trigger an
     * <code>IllegalStateException</code>, thrown by
     * {@link #measure(int, int)}. Calling the superclass'
     * {@link #onMeasure(int, int)} is a valid use.
     * </p>
     *
     * <p>
     * The base class implementation of measure defaults to the background size,
     * unless a larger size is allowed by the MeasureSpec. Subclasses should
     * override {@link #onMeasure(int, int)} to provide better measurements of
     * their content.
     * </p>
     *
     * <p>
     * If this method is overridden, it is the subclass's responsibility to make
     * sure the measured height and width are at least the view's minimum height
     * and width ({@link #getSuggestedMinimumHeight()} and
     * {@link #getSuggestedMinimumWidth()}).
     * </p>
     *
     * @param widthMeasureSpec horizontal space requirements as imposed by the parent.
     *                         The requirements are encoded with
     *                         {@link FMeasureSpec}.
     * @param heightMeasureSpec vertical space requirements as imposed by the parent.
     *                         The requirements are encoded with
     *                         {@link FMeasureSpec}.
     *
     * @see #getMeasuredWidth()
     * @see #getMeasuredHeight()
     * @see #setMeasuredDimension(int, int)
     * @see #getSuggestedMinimumHeight()
     * @see #getSuggestedMinimumWidth()
     * @see FMeasureSpec#getMode(int)
     * @see FMeasureSpec#getSize(int)
     */
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(FMeasureSpec.getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec),
                FMeasureSpec.getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec));
    }

    /**
     * <p>This method must be called by {@link #onMeasure(int, int)} to store the
     * measured width and measured height. Failing to do so will trigger an
     * exception at measurement time.</p>
     *
     * @param measuredWidth The measured width of this view.
     * @param measuredHeight The measured height of this view.
     */
    protected final void setMeasuredDimension(int measuredWidth, int measuredHeight) {
        this.measureWidth = measuredWidth;
        this.measureHeight = measuredHeight;
    }

    @Override
    public final int getMeasuredState() {
        return (measureWidth&MEASURED_STATE_MASK)
                | ((measureHeight>>MEASURED_HEIGHT_STATE_SHIFT)
                & (MEASURED_STATE_MASK>>MEASURED_HEIGHT_STATE_SHIFT));
    }

    @Override
    public void layout(int l, int t, int r, int b) {
        boolean changed = bounds.left != l
                || bounds.top != t
                || bounds.right != r
                || bounds.bottom != b;
        setFrame(l, t, r, b);
        onLayout(changed, l, t, r, b);
        addFlag(LAYOUT_MASK);
    }

    /**
     * check whether this view has bean layouted
     * @return true: layout, false otherwise
     */
    protected boolean isLayout() {
        return getFlag(LAYOUT_MASK) == LAYOUT_MASK;
    }

    /**
     * Assign a size and position to this view.
     *
     * This is called from layout.
     *
     * @param l Left position, relative to parent
     * @param t Top position, relative to parent
     * @param r Right position, relative to parent
     * @param b Bottom position, relative to parent
     * @return true if the new size and position are different than the
     *         previous ones
     */
    protected void setFrame(int l, int t, int r, int b) {
        bounds.set(l, t, r, b);
        mBackgroundSizeChanged = true;
    }

    @Override
    public void onLayout(boolean changed, int l, int t, int r, int b) {
    }

    @Override
    public void draw(Canvas canvas) {
        if (!isAttachedToWindow() || !isLayout() || getVisibility() == GONE)
            return;
        if (mBackground != null) {
            setBackgroundBounds();
            mBackground.draw(canvas);
        }
        onDraw(canvas);
        dispatchDraw(canvas);
        if (DebugInfo.DEBUG)
            debugDraw(canvas);
    }

    /**
     * draw this view's outline
     * @param canvas
     */
    private void debugDraw(Canvas canvas) {
        if (debugTool == null)
            debugTool = new FViewDebugTool();
        debugTool.withBounds(0, 0, bounds.width(), bounds.height());
        debugTool.draw(canvas);
    }

    /**
     * Sets the correct background bounds and rebuilds the outline, if needed.
     * <p/>
     * This is called by LayoutLib.
     */
    void setBackgroundBounds() {
        if (mBackgroundSizeChanged && mBackground != null) {
            mBackground.setBounds(0, 0, bounds.width(), bounds.height());
            mBackgroundSizeChanged = false;
        }
    }

    /**
     * Called by draw to draw the child views. This may be overridden
     * by derived classes to gain control just before its children are drawn
     * (but after its own view has been drawn).
     * @param canvas the canvas on which to draw the view
     */
    protected void dispatchDraw(Canvas canvas) {
    }

    /**
     * Implement this to do your drawing.
     *
     * @param canvas the canvas on which the background will be drawn
     */
    protected void onDraw(Canvas canvas) {
    }

    @Override
    public void setId(int id) {
        mId = id;
    }

    @Override
    public int getId() {
        return mId;
    }

    @Override
    public Object getTag() {
        return mTag;
    }

    @Override
    public void setTag(Object tag) {
        mTag = tag;
    }

    @Override
    public Object getTag(int key) {
        return mKeyedTags != null ? mKeyedTags.get(key) : null;
    }

    @Override
    public void setTag(int key, Object tag) {
        if (mKeyedTags == null)
            mKeyedTags = new SparseArray<>(2);
        mKeyedTags.put(key, tag);
    }

    @Override
    public FView findFViewById(int id) {
        if (id <= 0)
            return null;
        return findViewTraversal(id);
    }

    /**
     * @param id the id of the view to be found
     * @return the view of the specified id, null if cannot be found
     */
    protected FView findViewTraversal(int id) {
        if (id == mId)
            return this;
        return null;
    }

    @Override
    public Context getContext() {
        return mContext;
    }

    /**
     * Returns the resources associated with this view.
     *
     * @return Resources object.
     */
    protected Resources getResources() {
        return mContext != null ? mContext.getResources() : null;
    }

    @Override
    public FViewParent getFParent() {
        return parent;
    }

    @Override
    public void invalidate() {
        Rect dirty = new Rect(0, 0, bounds.width(), bounds.height());
        invalidate(dirty);
    }

    /**
     * Mark the area defined by dirty as needing to be drawn. If the view is
     * visible, {@link #draw(Canvas)} will be called at some
     * point in the future.
     * <p>
     * This must be called from a UI thread.
     * <p>
     * <b>WARNING:</b> In API 19 and below, this method may be destructive to
     * {@code dirty}.
     *
     * @param dirty the rectangle representing the bounds of the dirty region
     */
    protected void invalidate(Rect dirty) {
        if (parent != null)
            parent.invalidateChild(this, dirty);
    }

    @Override
    public IFViewGroup.FLayoutParams getFLayoutParams() {
        return mFLayoutParams;
    }

    @Override
    public void setFLayoutParams(IFViewGroup.FLayoutParams params) {
        if (params == null) {
            throw new NullPointerException("Layout parameters cannot be null");
        }
        mFLayoutParams = params;
        if (parent != null)
            parent.onSetLayoutParams(this, params);
        requestFViewTreeLayout();
    }

    @Override
    public void setBackgroundColor(int color) {
        if (mBackground instanceof ColorDrawable) {
            ((ColorDrawable) mBackground.mutate()).setColor(color);
            mBackgroundResource = 0;
        } else {
            setBackground(new ColorDrawable(color));
        }
    }

    @Override
    public void setBackgroundResource(int resid) {
        if (resid != 0 && resid == mBackgroundResource) {
            return;
        }

        Drawable d = null;
        if (resid != 0) {
            d = mContext.getResources().getDrawable(resid);
        }
        setBackground(d);

        mBackgroundResource = resid;
    }

    @Override
    public void setBackground(Drawable background) {
        if (background == mBackground) {
            return;
        }
        boolean requestLayout = false;

        mBackgroundResource = 0;

        /*
         * Regardless of whether we're setting a new background or not, we want
         * to clear the previous drawable. setVisible first while we still have the callback set.
         */
        if (mBackground != null) {
            if (isAttachedToWindow()) {
                mBackground.setVisible(false, false);
            }
            mBackground.setCallback(null);
            unscheduleDrawable(mBackground);
        }
        if (background != null) {
            Rect padding = new Rect();
            if (background.getPadding(padding)) {
                internalSetPadding(padding.left, padding.top, padding.right, padding.bottom);
            }
            if (mBackground == null
                    || mBackground.getMinimumHeight() != background.getMinimumHeight()
                    || mBackground.getMinimumWidth() != background.getMinimumWidth()) {
                requestLayout = true;
            }
            mBackground = background;
            if (isAttachedToWindow()) {
                background.setVisible(getVisibility() == VISIBLE, false);
            }
            background.setCallback(this);
        } else {
            mBackground = null;
            requestLayout = true;
        }
        if (requestLayout) {
            requestFViewTreeLayout();
        }
        invalidate();
        mBackgroundSizeChanged = true;
    }

    @Override
    public Drawable getBackground() {
        return mBackground;
    }

    @Override
    public boolean post(Runnable action) {
        return viewRoot.post(action);
    }

    @Override
    public boolean postDelayed(Runnable action, long delayMillis) {
        return viewRoot.postDelayed(action, delayMillis);
    }

    @Override
    public boolean removeCallbacks(Runnable action) {
        return viewRoot.removeCallbacks(action);
    }

    @Override
    public void requestLayoutAll() {
        viewRoot.requestLayoutAll();
    }

    @Override
    public void requestFViewTreeLayout() {
        viewRoot.requestFViewTreeLayout();
    }

    @Override
    public void requestLayoutThisView() {
        post(new Runnable() {
            @Override
            public void run() {
                if (parent != null) {
                    parent.measureChildWithMargins(FView.this, parent.getOldWidthMeasureSpec(),
                            0, parent.getOldHeightMeasureSpec(), 0);
                    parent.layoutChild(FView.this);
                }
            }
        });
    }

    @Override
    public int getMeasuredWidth() {
        return measureWidth;
    }

    @Override
    public int getMeasuredHeight() {
        return measureHeight;
    }

    @Override
    public int getWidth() {
        return bounds.width();
    }

    @Override
    public int getHeight() {
        return bounds.height();
    }

    @Override
    public int getLeft() {
        return bounds.left;
    }

    @Override
    public int getTop() {
        return bounds.top;
    }

    @Override
    public int getRight() {
        return bounds.right;
    }

    @Override
    public int getBottom() {
        return bounds.bottom;
    }

    @Override
    public int getPaddingLeft() {
        return padding.left;
    }

    @Override
    public int getPaddingTop() {
        return padding.top;
    }

    @Override
    public int getPaddingRight() {
        return padding.right;
    }

    @Override
    public int getPaddingBottom() {
        return padding.bottom;
    }

    @Override
    public void setPadding(int l, int t, int r, int b) {
        internalSetPadding(l, t, r, b);
    }

    @Override
    public void getLocationOnScreen(int[] outLocation) {
        getLocationInWindow(outLocation);
        if (isAttachedToWindow()) {
            int[] windowPos = new int[2];
            ViewUtils.getWindowPosition((View) viewRoot, windowPos);
            outLocation[0] += windowPos[0];
            outLocation[1] += windowPos[1];
        }
    }

    @Override
    public void getLocationInWindow(int[] outLocation) {
        transformFromViewToWindowSpace(outLocation);
    }

    protected void transformFromViewToWindowSpace(@Size(2) int[] inOutLocation) {
        if (inOutLocation == null || inOutLocation.length < 2) {
            throw new IllegalArgumentException("inOutLocation must be an array of two integers");
        }
        if (!isAttachedToWindow()) {
            inOutLocation[0] = inOutLocation[1] = 0;
            return;
        }
        float position[] = new float[] {
                bounds.left,
                bounds.top
        };
        FViewParent p = parent;
        while (p instanceof FView) {
            final FView view = (FView) p;
            position[0] += view.bounds.left;
            position[1] += view.bounds.top;
            p = view.parent;
        }
        if (p instanceof FViewRootImpl) {
            FViewRootImpl root = (FViewRootImpl) p;
            int[] rootPos = new int[2];
            root.getLocationInWindow(rootPos);
            position[0] += rootPos[0];
            position[1] += rootPos[1];
        }
        inOutLocation[0] = Math.round(position[0]);
        inOutLocation[1] = Math.round(position[1]);
    }

    private void internalSetPadding(int left, int top, int right, int bottom) {
        boolean changed = padding.left != left
                || padding.top != top
                || padding.right != right
                || padding.bottom != bottom;
        padding.set(left, top, right, bottom);

        if (changed) {
            requestFViewTreeLayout();
        }
    }

    /**
     * Indicates whether this view reacts to click events or not.
     *
     * @return true if the view is clickable, false otherwise
     *
     * @see #setClickable(boolean)
     * @attr ref android.R.styleable#View_clickable
     */
    protected boolean isClickable() {
        return getFlag(CLICKABLE) == CLICKABLE;
    }

    @Override
    public boolean isTouchInView(float x, float y) {
        if (!isClickable() || !isEnabled() || getVisibility() == GONE)
            return false;
        return bounds.contains((int) x, (int) y);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        if (!isTouchInView(x, y))
            return false;
        return onTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final float x = event.getX();
        final float y = event.getY();
        final int action = event.getAction();
        if (!isClickable() || !isEnabled() || getVisibility() == GONE)
            return false;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                setPressed(true);
                mHasPerformedLongPress = false;
                checkForLongClick(0);
                break;
            case MotionEvent.ACTION_MOVE:
                if (!isTouchInView(x, y)) {
                    removeLongPressCallback();
                    setPressed(false);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                setPressed(false);
                removeLongPressCallback();
                mHasPerformedLongPress = false;
                break;
            case MotionEvent.ACTION_UP:
                if (!mHasPerformedLongPress) {
                    removeLongPressCallback();
                    performClick();
                }
                if (mUnsetPressedState == null) {
                    mUnsetPressedState = new UnsetPressedState();
                }
                if (!post(mUnsetPressedState)) {
                    mUnsetPressedState.run();
                }
                break;
        }
        return true;
    }

    private void checkForLongClick(int delayOffset) {
        if (getFlag(LONG_CLICKABLE) == LONG_CLICKABLE) {
            mHasPerformedLongPress = false;

            if (mPendingCheckForLongPress == null) {
                mPendingCheckForLongPress = new CheckForLongPress();
            }
            postDelayed(mPendingCheckForLongPress,
                    ViewConfiguration.getLongPressTimeout() - delayOffset);
        }
    }

    /**
     * Sets the pressed state for this view.
     *
     * @see #isClickable()
     * @see #setClickable(boolean)
     *
     * @param pressed Pass true to set the View's internal state to "pressed", or false to reverts
     *        the View's internal state from a previously set "pressed" state.
     */
    protected void setPressed(boolean pressed) {
        removeFlag(PRESS_MASK);
        if (pressed)
            addFlag(PRESS_MASK);
    }

    /**
     * Indicates whether the view is currently in pressed state. Unless
     * {@link #setPressed(boolean)} is explicitly called, only clickable views can enter
     * the pressed state.
     *
     * @see #setPressed(boolean)
     * @see #isClickable()
     * @see #setClickable(boolean)
     *
     * @return true if the view is currently pressed, false otherwise
     */
    protected boolean isPressed() {
        return getFlag(PRESS_MASK) == PRESS_MASK;
    }

    /**
     * Enables or disables click events for this view. When a view
     * is clickable it will change its state to "pressed" on every click.
     * Subclasses should set the view clickable to visually react to
     * user's clicks.
     *
     * @param clickable true to make the view clickable, false otherwise
     *
     * @see #isClickable()
     */
    public void setClickable(boolean clickable) {
        removeFlag(CLICKABLE);
        if (clickable)
            addFlag(CLICKABLE);
    }

    @Override
    public void setOnClickListener(OnClickListener listener) {
        listenerInfo.onClickListener = listener;
        if (listener != null)
            setClickable(true);
    }

    /**
     * Enables or disables long click events for this view. When a view is long
     * clickable it reacts to the user holding down the button for a longer
     * duration than a tap. This event can either launch the listener or a
     * context menu.
     *
     * @param clickable true to make the view long clickable, false otherwise
     */
    public void setLongClickable(boolean clickable) {
        removeFlag(LONG_CLICKABLE);
        if (clickable) {
            addFlag(LONG_CLICKABLE);
            setClickable(true);
        }
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener listener) {
        listenerInfo.onLongClickListener = listener;
        if (listener != null)
            setLongClickable(true);
    }

    /**
     * Returns true if this view is currently attached to a window.
     */
    protected boolean isAttachedToWindow() {
        return getFlag(ATTACHED_MASK) == ATTACHED_MASK;
    }

    /**
     * Add flags controlling behavior of this view.
     *
     * @param mask Constant indicating the bit range that should be changed
     */
    private void addFlag(int mask) {
        flag = flag | mask;
    }

    /**
     * Get flags controlling behavior of this view.
     * @param mask Constant indicating the bit range that should be changed
     * @return flag
     */
    private int getFlag(int mask) {
        return flag & mask;
    }

    /**
     * Remove flags controlling behavior of this view.
     * @param mask Constant indicating the bit range that should be changed
     */
    private void removeFlag(int mask) {
        flag = flag & (~mask);
    }

    /**
     * Call this view's OnClickListener, if it is defined.  Performs all normal
     * actions associated with clicking: reporting accessibility event, playing
     * a sound, etc.
     *
     * @return True there was an assigned OnClickListener that was called, false
     *         otherwise is returned.
     */
    public boolean performClick() {
        if (listenerInfo.onClickListener != null) {
            listenerInfo.onClickListener.onClick(this);
            return true;
        }
        return false;
    }

    /**
     * Calls this view's OnLongClickListener, if it is defined. Invokes the
     * context menu if the OnLongClickListener did not consume the event.
     *
     * @return {@code true} if one of the above receivers consumed the event,
     *         {@code false} otherwise
     */
    public boolean performLongClick() {
        if (listenerInfo.onLongClickListener != null)
            return listenerInfo.onLongClickListener.onLongClick(this);
        return false;
    }

    private void removeLongPressCallback() {
        if (mPendingCheckForLongPress != null) {
            removeCallbacks(mPendingCheckForLongPress);
        }
    }

    private void removeUnsetPressCallback() {
        if (mUnsetPressedState != null) {
            removeCallbacks(mUnsetPressedState);
            setPressed(false);
        }
    }

    @Override
    public String toString() {
        return getClass().getName() + hashCode();
    }

    protected void d(String tag, String log) {
        if (DebugInfo.DEBUG)
            Log.d(tag, log);
    }

    @CallSuper
    protected boolean verifyDrawable(@NonNull Drawable who) {
        return who == mBackground;
    }

    @Override
    public void invalidateDrawable(@NonNull Drawable who) {
        if (verifyDrawable(who)) {
            final Rect dirty;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                dirty = who.getDirtyBounds();
            } else {
                dirty = who.getBounds();
            }
            invalidate(dirty);
        }
    }

    @Override
    public void scheduleDrawable(@NonNull Drawable who, @NonNull Runnable what, long when) {
        if (verifyDrawable(who) && what != null) {
            final long delay = when - SystemClock.uptimeMillis();
            if (postDelayed(new WrapperRunnable(who, what), delay)) {
                addAction(who, what);
            }
        }
    }

    @Override
    public void unscheduleDrawable(@NonNull Drawable who, @NonNull Runnable what) {
        if (verifyDrawable(who) && what != null) {
            removeCallbacks(what);
            if (what instanceof WrapperRunnable) {
                removeCallbacks(((WrapperRunnable) what).action);
            }
            removeAction(who, what);
        }
    }

    /**
     * Unschedule actions previously
     * scheduled with {@link #scheduleDrawable} by one of Drawable.
     * @param who The drawable being unscheduled.
     */
    protected void unscheduleDrawable(Drawable who) {
        if (who != null) {
            ArrayList<Runnable> actions = drawableActions.remove(who);
            if (actions != null) {
                for (int i = 0, l = actions.size(); i < l; i ++) {
                    Runnable a = actions.get(i);
                    removeCallbacks(a);
                }
            }
        }
    }

    protected void addAction(@NonNull Drawable who, @NonNull Runnable what) {
        ArrayList<Runnable> actions = drawableActions.get(who);
        if (actions == null) {
            actions = new ArrayList<>();
            drawableActions.put(who, actions);
        }
        actions.add(what);
    }

    protected void removeAction(@NonNull Drawable who, @NonNull Runnable what) {
        ArrayList<Runnable> actions = drawableActions.get(who);
        if (actions != null) {
            actions.remove(what);
            if (actions.isEmpty()) {
                drawableActions.remove(who);
            }
        }
    }

    /**
     * Returns the suggested minimum height that the view should use. This
     * returns the maximum of the view's minimum height
     * and the background's minimum height
     * ({@link Drawable#getMinimumHeight()}).
     * <p>
     * When being used in {@link #onMeasure(int, int)}, the caller should still
     * ensure the returned height is within the requirements of the parent.
     *
     * @return The suggested minimum height of the view.
     */
    protected int getSuggestedMinimumHeight() {
        return (mBackground == null) ? 0 : mBackground.getMinimumHeight();
    }

    /**
     * Returns the suggested minimum width that the view should use. This
     * returns the maximum of the view's minimum width
     * and the background's minimum width
     *  ({@link Drawable#getMinimumWidth()}).
     * <p>
     * When being used in {@link #onMeasure(int, int)}, the caller should still
     * ensure the returned width is within the requirements of the parent.
     *
     * @return The suggested minimum width of the view.
     */
    protected int getSuggestedMinimumWidth() {
        return (mBackground == null) ? 0 : mBackground.getMinimumWidth();
    }

    private final class CheckForLongPress implements Runnable {

        @Override
        public void run() {
            if (isPressed() && (parent != null)) {
                if (performLongClick()) {
                    mHasPerformedLongPress = true;
                }
            }
        }
    }

    private final class UnsetPressedState implements Runnable {
        @Override
        public void run() {
            setPressed(false);
        }
    }

    static class ListenerInfo {
        OnClickListener onClickListener;

        OnLongClickListener onLongClickListener;
    }

    class WrapperRunnable implements Runnable {
        Runnable action;
        Drawable who;
        WrapperRunnable(Drawable who, Runnable action) {
            this.action = action;
            this.who = who;
        }

        @Override
        public void run() {
            if (action != null)
                action.run();
            removeAction(who, action);
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
