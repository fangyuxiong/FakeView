package xfy.fakeview.library.fview;

import android.graphics.Rect;

/**
 * Created by XiongFangyu on 2017/11/8.
 */

public interface FViewParent {

    /**
     * invalidate child rect
     * @param child view for invalidate
     * @param rect  rect for invalidate
     */
    void invalidateChild(FView child, Rect rect);

    /**
     * 布局view及其子view树
     * @param view
     */
    void layoutChild(FView view);

    /**
     * Ask one of the children of this view to measure itself, taking into
     * account both the MeasureSpec requirements for this view and its padding
     * and margins. The child must have MarginLayoutParams The heavy lifting is
     * done in getChildMeasureSpec.
     *
     * @param child The child to measure
     * @param parentWidthMeasureSpec The width requirements for this view
     * @param widthUsed Extra space that has been used up by the parent
     *        horizontally (possibly by other children of the parent)
     * @param parentHeightMeasureSpec The height requirements for this view
     * @param heightUsed Extra space that has been used up by the parent
     *        vertically (possibly by other children of the parent)
     */
    void measureChildWithMargins(FView child,
                                 int parentWidthMeasureSpec, int widthUsed,
                                 int parentHeightMeasureSpec, int heightUsed);

    /**
     * 请求布局整个view树
     */
    void requestLayoutAll();

    /**
     * 请求布局整个FView树
     */
    void requestFViewTreeLayout();
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

    void onSetLayoutParams(FView child, IFViewGroup.FLayoutParams params);

    /**
     * Get last measured widthMeasureSpec param
     * @return last widthMeasureSpec
     */
    int getOldWidthMeasureSpec();

    /**
     * Get last measured heightMeasureSpec param
     * @return last heightMeasureSpec
     */
    int getOldHeightMeasureSpec();
}
