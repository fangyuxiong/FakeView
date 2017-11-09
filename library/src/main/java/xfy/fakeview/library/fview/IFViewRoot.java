package xfy.fakeview.library.fview;

import android.graphics.Canvas;

/**
 * Created by XiongFangyu on 2017/11/7.
 */
public interface IFViewRoot extends FViewParent{
    /**
     * Invalidate the whole view. If the view is visible,
     * {@link IFView#draw(Canvas)} will be called at some point in
     * the future.
     * <p>
     * This must be called from a UI thread. To call from a non-UI thread, call
     */
    void invalidate();

    /**
     * Removes all Runnables from the message queue.
     */
    void removeAllCallbacks();

    /**
     * Set root child for target
     * @param view root child
     */
    void setTargetChild(FView view);

    /**
     * Get root child
     * @return
     */
    FView getTargetChild();

    /**
     * Finds the first descendant view with the given ID, the view itself if
     * the ID matches {@link IFView#getId()}, or {@code null} if the ID is invalid
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
    FView findFViewById(int id);
}
