package xfy.fakeview.library.fview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.support.annotation.IntRange;
import android.view.MotionEvent;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by XiongFangyu on 2017/11/7.
 *
 * FView 接口
 * 有很多接口和{@link android.view.View}很像
 * 默认只有点击事件和长按事件
 */
public interface IFView extends IView{
    int NO_ID = -1;

    @IntDef({VISIBLE, INVISIBLE, GONE})
    @Retention(RetentionPolicy.SOURCE)
    @interface Visibility {}

    int VISIBLE         = 0x00000000;
    int INVISIBLE       = 0x00000004;
    int GONE            = 0x00000008;
    int VISIBILITY_MASK = 0x0000000C;

    int ENABLED         = 0x00000000;
    int DISABLED        = 0x00000020;
    int ENABLED_MASK    = 0x00000020;

    int ATTACHED_MASK   = 0x00000100;

    int LAYOUT_MASK     = 0x00000200;

    int CLICKABLE       = 0x00002000;
    int LONG_CLICKABLE  = 0x00004000;
    int PRESS_MASK      = 0x00008000;

    int MEASURED_STATE_MASK = 0xff000000;
    int MEASURED_HEIGHT_STATE_SHIFT = 16;

    /**
     * This is called when the view is attached to a window.  At this point it
     * has a Surface and will start drawing.  Note that this function is
     * guaranteed to be called before {@link #draw(Canvas)},
     * however it may be called any time before the first onDraw -- including
     * before or after {@link #measure(int, int)}.
     *
     * @see #onDetachedFromWindow()
     */
    void onAttachedToWindow();

    /**
     * This is called when the view is detached from a window.  At this point it
     * no longer has a surface for drawing.
     *
     * @see #onAttachedToWindow()
     */
    void onDetachedFromWindow();

    /**
     * Set the visibility state of this view.
     *
     * @param visibility One of {@link #VISIBLE}, {@link #INVISIBLE}, or {@link #GONE}.
     */
    void setVisibility(@Visibility int visibility);

    /**
     * Returns the visibility status for this view.
     *
     * @return One of {@link #VISIBLE}, {@link #INVISIBLE}, or {@link #GONE}.
     */
    @Visibility int getVisibility();

    /**
     * Set the enabled state of this view. The interpretation of the enabled
     * state varies by subclass.
     *
     * @param enabled True if this view is enabled, false otherwise.
     */
    void setEnabled(boolean enabled);

    /**
     * Returns the enabled status for this view. The interpretation of the
     * enabled state varies by subclass.
     *
     * @return True if this view is enabled, false otherwise.
     */
    boolean isEnabled();

    /**
     * <p>
     * This is called to find out how big a view should be. The parent
     * supplies constraint information in the width and height parameters.
     * </p>
     *
     * <p>
     * The actual measurement work of a view is performed in
     * {@link FView#onMeasure(int, int)}, called by this method. Therefore, only
     * {@link FView#onMeasure(int, int)} can and must be overridden by subclasses.
     * </p>
     *
     *
     * @param widthMeasureSpec Horizontal space requirements as imposed by the
     *        parent
     * @param heightMeasureSpec Vertical space requirements as imposed by the
     *        parent
     *
     * @see FView#onMeasure(int, int)
     */
    void measure(int widthMeasureSpec, int heightMeasureSpec);

    /**
     * Assign a size and position to a view and all of its
     * descendants
     *
     * <p>This is the second phase of the layout mechanism.
     * (The first is measuring). In this phase, each parent calls
     * layout on all of its children to position them.
     * This is typically done using the child measurements
     * that were stored in the measure pass().</p>
     *
     * <p>Derived classes should not override this method.
     * Derived classes with children should override
     * onLayout. In that method, they should
     * call layout on each of their children.</p>
     *
     * @param l Left position, relative to parent
     * @param t Top position, relative to parent
     * @param r Right position, relative to parent
     * @param b Bottom position, relative to parent
     */
    void layout(int l, int t, int r, int b);

    /**
     * Called from layout when this view should
     * assign a size and position to each of its children.
     *
     * Derived classes with children should override
     * this method and call layout on each of
     * their children.
     * @param changed This is a new size or position for this view
     * @param l Left position, relative to parent
     * @param t Top position, relative to parent
     * @param r Right position, relative to parent
     * @param b Bottom position, relative to parent
     */
    void onLayout(boolean changed, int l, int t, int r, int b);

    /**
     * Manually render this view (and all of its children) to the given Canvas.
     * The view must have already done a full layout before this function is
     * called.  When implementing a view, implement
     * {@link FView#onDraw(android.graphics.Canvas)} instead of overriding this method.
     * If you do need to override this method, call the superclass version.
     *
     * @param canvas The Canvas to which the View is rendered.
     */
    void draw(Canvas canvas);

    /**
     * Sets the identifier for this view. The identifier does not have to be
     * unique in this view's hierarchy. The identifier should be a positive
     * number.
     *
     * @see #NO_ID
     * @see #getId()
     * @see #findFViewById(int)
     *
     * @param id a number used to identify the view
     */
    void setId(@IntRange(from = 1, to = Integer.MAX_VALUE) int id);

    /**
     * Finds the first descendant view with the given ID, the view itself if
     * the ID matches {@link #getId()}, or {@code null} if the ID is invalid
     * (< 0) or there is no matching view in the hierarchy.
     * <p>
     * <strong>Note:</strong> In most cases -- depending on compiler support --
     * the resulting view is automatically cast to the target class type. If
     * the target class type is unconstrained, an explicit cast may be
     * necessary.
     *
     * @param id the ID to search for
     * @return a view with given ID if found, or {@code null} otherwise
     */
    IFView findFViewById(int id);

    /**
     * Returns the context the view is running in, through which it can
     * access the current theme, resources, etc.
     *
     * @return The view's Context.
     */
    Context getContext();

    /**
     * Gets the parent of this view. Note that the parent is a
     * ViewParent and not necessarily a View.
     *
     * @return Parent of this view.
     */
    FViewParent getFParent();

    /**
     * Invalidate the whole view. If the view is visible,
     * {@link #draw(Canvas)} will be called at some point in
     * the future.
     * <p>
     * This must be called from a UI thread. To call from a non-UI thread, call
     */
    void invalidate();

    /**
     * Get the LayoutParams associated with this view. All views should have
     * layout parameters. These supply parameters to the <i>parent</i> of this
     * view specifying how it should be arranged. There are many subclasses of
     * ViewGroup.LayoutParams, and these correspond to the different subclasses
     * of ViewGroup that are responsible for arranging their children.
     *
     * This method may return null if this View is not attached to a parent
     * ViewGroup or {@link #setFLayoutParams(IFViewGroup.FLayoutParams)}
     * was not invoked successfully. When a View is attached to a parent
     * ViewGroup, this method must not return null.
     *
     * @return The LayoutParams associated with this view, or null if no
     *         parameters have been set yet
     */
    IFViewGroup.FLayoutParams getFLayoutParams();

    /**
     * Set the layout parameters associated with this view. These supply
     * parameters to the <i>parent</i> of this view specifying how it should be
     * arranged. There are many subclasses of ViewGroup.LayoutParams, and these
     * correspond to the different subclasses of ViewGroup that are responsible
     * for arranging their children.
     *
     * @param params The layout parameters for this view, cannot be null
     */
    void setFLayoutParams(IFViewGroup.FLayoutParams params);

//background
    /**
     * Sets the background color for this view.
     * @param color the color of the background
     */
    void setBackgroundColor(@ColorInt int color);

    /**
     * Set the background to a given resource. The resource should refer to
     * a Drawable object or 0 to remove the background.
     * @param resid The identifier of the resource.
     */
    void setBackgroundResource(@DrawableRes int resid);

    /**
     * Set the background to a given Drawable, or remove the background. If the
     * background has padding, this View's padding is set to the background's
     * padding. However, when a background is removed, this View's padding isn't
     * touched. If setting the padding is desired, please use
     * {@link #setPadding(int, int, int, int)}.
     *
     * @param background The Drawable to use as the background, or null to remove the
     *        background
     */
    void setBackground(Drawable background);

    /**
     * Gets the background drawable
     *
     * @return The drawable used as the background for this view, if any.
     *
     * @see #setBackground(Drawable)
     */
    Drawable getBackground();

//post
    /**
     * <p>Causes the Runnable to be added to the message queue.
     * The runnable will be run on the user interface thread.</p>
     *
     * @param action The Runnable that will be executed.
     *
     * @return Returns true if the Runnable was successfully placed in to the
     *         message queue.  Returns false on failure, usually because the
     *         looper processing the message queue is exiting.
     *
     * @see #postDelayed
     * @see #removeCallbacks
     */
    boolean post(Runnable action);

    /**
     * <p>Causes the Runnable to be added to the message queue, to be run
     * after the specified amount of time elapses.
     * The runnable will be run on the user interface thread.</p>
     *
     * @param action The Runnable that will be executed.
     * @param delayMillis The delay (in milliseconds) until the Runnable
     *        will be executed.
     *
     * @return true if the Runnable was successfully placed in to the
     *         message queue.  Returns false on failure, usually because the
     *         looper processing the message queue is exiting.  Note that a
     *         result of true does not mean the Runnable will be processed --
     *         if the looper is quit before the delivery time of the message
     *         occurs then the message will be dropped.
     *
     * @see #post
     * @see #removeCallbacks
     */
    boolean postDelayed(Runnable action, long delayMillis);

    /**
     * <p>Removes the specified Runnable from the message queue.</p>
     *
     * @param action The Runnable to remove from the message handling queue
     *
     * @return true if this view could ask the Handler to remove the Runnable,
     *         false otherwise. When the returned value is true, the Runnable
     *         may or may not have been actually removed from the message queue
     *         (for instance, if the Runnable was not in the queue already.)
     *
     * @see #post
     * @see #postDelayed
     */
    boolean removeCallbacks(Runnable action);
//end

//layout

    /**
     * 请求布局整个view树
     */
    void requestLayoutAll();

    /**
     * 请求布局整个FView树
     */
    void requestFViewTreeLayout();

    /**
     * 请求布局此view及其子view树
     */
    void requestLayoutThisView();
//end

//位置信息
    /**
     * Return the full width measurement information for this view as computed
     * by the most recent call to {@link #measure(int, int)}.
     * This should be used during measurement and layout calculations only. Use
     * {@link #getWidth()} to see how wide a view is after layout.
     *
     * @return The measured width of this view as a bit mask.
     */
    int getMeasuredWidth();

    /**
     * Return the full height measurement information for this view as computed
     * by the most recent call to {@link #measure(int, int)}.
     * This should be used during measurement and layout calculations only. Use
     * {@link #getHeight()} to see how wide a view is after layout.
     *
     * @return The measured height of this view as a bit mask.
     */
    int getMeasuredHeight();

    /**
     * Return only the state bits of {@link #getMeasuredWidth()}
     * and {@link #getMeasuredHeight()}, combined into one integer.
     * The width component is in the regular bits {@link #MEASURED_STATE_MASK}
     * and the height component is at the shifted bits
     * {@link #MEASURED_HEIGHT_STATE_SHIFT}>>{@link #MEASURED_STATE_MASK}.
     */
    int getMeasuredState();

    /**
     * Return the width of the your view.
     *
     * @return The width of your view, in pixels.
     */
    int getWidth();

    /**
     * Return the height of your view.
     *
     * @return The height of your view, in pixels.
     */
    int getHeight();

    /**
     * Left position of this view relative to its parent.
     *
     * @return The left edge of this view, in pixels.
     */
    int getLeft();

    /**
     * Top position of this view relative to its parent.
     *
     * @return The top of this view, in pixels.
     */
    int getTop();

    /**
     * Right position of this view relative to its parent.
     *
     * @return The right edge of this view, in pixels.
     */
    int getRight();

    /**
     * Bottom position of this view relative to its parent.
     *
     * @return The bottom of this view, in pixels.
     */
    int getBottom();

    /**
     * Returns the left padding of this view. If there are inset and enabled
     * scrollbars, this value may include the space required to display the
     * scrollbars as well.
     *
     * @return the left padding in pixels
     */
    int getPaddingLeft();

    /**
     * Returns the top padding of this view.
     *
     * @return the top padding in pixels
     */
    int getPaddingTop();

    /**
     * Returns the right padding of this view. If there are inset and enabled
     * scrollbars, this value may include the space required to display the
     * scrollbars as well.
     *
     * @return the right padding in pixels
     */
    int getPaddingRight();

    /**
     * Returns the bottom padding of this view. If there are inset and enabled
     * scrollbars, this value may include the space required to display the
     * scrollbars as well.
     *
     * @return the bottom padding in pixels
     */
    int getPaddingBottom();

    /**
     * Sets the padding. The view may add on the space required to display
     * the scrollbars, depending on the style and visibility of the scrollbars.
     * So the values returned from {@link #getPaddingLeft}, {@link #getPaddingTop},
     * {@link #getPaddingRight} and {@link #getPaddingBottom} may be different
     * from the values set in this call.
     *
     * @param l the left padding in pixels
     * @param t the top padding in pixels
     * @param r the right padding in pixels
     * @param b the bottom padding in pixels
     */
    void setPadding(int l, int t, int r, int b);
//end

//事件处理 start

    /**
     * 坐标是否在view内部
     * @param x 触摸事件的x坐标
     * @param y 触摸事件的y坐标
     * @return true: 在内部，可做点击或其他事件
     *          false: 不在，不处理
     */
    boolean isTouchInView(float x, float y);

    /**
     * Pass the touch screen motion event down to the target view, or this
     * view if it is the target.
     *
     * @param event The motion event to be dispatched.
     * @return True if the event was handled by the view, false otherwise.
     */
    boolean dispatchTouchEvent(MotionEvent event);

    /**
     * Implement this method to handle touch screen motion events.
     * <p>
     * If this method is used to detect click actions, it is recommended that
     * the actions be performed by implementing and calling
     * {@link FView#performClick()}. This will ensure consistent system behavior,
     * including:
     * <ul>
     * <li>dispatching OnClickListener calls
     * </ul>
     *
     * @param event The motion event.
     * @return True if the event was handled, false otherwise.
     */
    boolean onTouchEvent(MotionEvent event);
//end

    /**
     * Register a callback to be invoked when this view is clicked. If this view is not
     * clickable, it becomes clickable.
     *
     * @param listener The callback that will run
     *
     * @see FView#setClickable(boolean)
     */
    void setOnClickListener(OnClickListener listener);

    /**
     * Register a callback to be invoked when this view is clicked and held. If this view is not
     * long clickable, it becomes long clickable.
     *
     * @param listener The callback that will run
     *
     * @see FView#setLongClickable(boolean)
     */
    void setOnLongClickListener(OnLongClickListener listener);

    /**
     * Interface definition for a callback to be invoked when a view is clicked.
     */
    interface OnClickListener {
        /**
         * Called when a view has been clicked.
         *
         * @param view The view that was clicked.
         */
        void onClick(IFView view);
    }

    /**
     * Interface definition for a callback to be invoked when a view has been clicked and held.
     */
    interface OnLongClickListener {
        /**
         * Called when a view has been clicked and held.
         *
         * @param view The view that was clicked and held.
         *
         * @return true if the callback consumed the long click, false otherwise.
         */
        boolean onLongClick(IFView view);
    }
}
