package xfy.fakeview.library.fview;

import android.view.ViewGroup;

/**
 * Created by XiongFangyu on 2017/11/7.
 */
public interface IFViewGroup extends IFView, FViewParent{

    /**
     * <p>Adds a child view. If no layout parameters are already set on the child, the
     * default parameters for this ViewGroup are set on the child.</p>
     *
     * <p><strong>Note:</strong> do not invoke this method from
     * {@link #draw(android.graphics.Canvas)}, {@link FView#onDraw(android.graphics.Canvas)},
     * {@link FView#dispatchDraw(android.graphics.Canvas)} or any related method.</p>
     *
     * @param child the child view to add
     *
     * @see #generateDefaultLayoutParams()
     */
    void addView(FView child);

    /**
     * Adds a child view. If no layout parameters are already set on the child, the
     * default parameters for this ViewGroup are set on the child.
     *
     * <p><strong>Note:</strong> do not invoke this method from
     * {@link #draw(android.graphics.Canvas)}, {@link FView#onDraw(android.graphics.Canvas)},
     * {@link FView#dispatchDraw(android.graphics.Canvas)} or any related method.</p>
     *
     * @param child the child view to add
     * @param index the position at which to add the child
     *
     * @see #generateDefaultLayoutParams()
     */
    void addView(FView child, int index);

    /**
     * Adds a child view with the specified layout parameters.
     *
     * <p><strong>Note:</strong> do not invoke this method from
     * {@link #draw(android.graphics.Canvas)}, {@link FView#onDraw(android.graphics.Canvas)},
     * {@link FView#dispatchDraw(android.graphics.Canvas)} or any related method.</p>
     *
     * @param child the child view to add
     * @param params the layout parameters to set on the child
     */
    void addView(FView child, FLayoutParams params);

    /**
     * Adds a child view with the specified layout parameters.
     *
     * <p><strong>Note:</strong> do not invoke this method from
     * {@link #draw(android.graphics.Canvas)}, {@link FView#onDraw(android.graphics.Canvas)},
     * {@link FView#dispatchDraw(android.graphics.Canvas)} or any related method.</p>
     *
     * @param child the child view to add
     * @param index the position at which to add the child or -1 to add last
     * @param params the layout parameters to set on the child
     */
    void addView(FView child, int index, FLayoutParams params);

    /**
     * Returns the number of children in the group.
     *
     * @return a positive integer representing the number of children in
     *         the group
     */
    int getChildCount();

    /**
     * Returns the view at the specified position in the group.
     *
     * @param index the position at which to get the view from
     * @return the view at the specified position or null if the position
     *         does not exist within the group
     */
    FView getChildAt(int index);

    /**
     * Returns a set of default layout parameters. These parameters are requested
     * when the View passed to {@link #addView(FView)} has no layout parameters
     * already set. If null is returned, an exception is thrown from addView.
     *
     * @return a set of default layout parameters or null
     */
    FLayoutParams generateDefaultLayoutParams();

    class FLayoutParams {
        /**
         * Special value for the height or width requested by a View.
         * MATCH_PARENT means that the view wants to be as big as its parent,
         * minus the parent's padding, if any. Introduced in API Level 8.
         */
        public static final int MATCH_PARENT = -1;

        /**
         * Special value for the height or width requested by a View.
         * WRAP_CONTENT means that the view wants to be just large enough to fit
         * its own internal content, taking its own padding into account.
         */
        public static final int WRAP_CONTENT = -2;

        /**
         * Information about how wide the view wants to be. Can be one of the
         * constants FILL_PARENT (replaced by MATCH_PARENT
         * in API Level 8) or WRAP_CONTENT, or an exact size.
         */
        public int width;

        /**
         * Information about how tall the view wants to be. Can be one of the
         * constants FILL_PARENT (replaced by MATCH_PARENT
         * in API Level 8) or WRAP_CONTENT, or an exact size.
         */
        public int height;

        /**
         * The left margin in pixels of the child. Margin values should be positive.
         * Call {@link #setFLayoutParams(FLayoutParams)} after reassigning a new value
         * to this field.
         */
        public int leftMargin;

        /**
         * The top margin in pixels of the child. Margin values should be positive.
         * Call {@link #setFLayoutParams(FLayoutParams)} after reassigning a new value
         * to this field.
         */
        public int topMargin;

        /**
         * The right margin in pixels of the child. Margin values should be positive.
         * Call {@link #setFLayoutParams(FLayoutParams)} after reassigning a new value
         * to this field.
         */
        public int rightMargin;

        /**
         * The bottom margin in pixels of the child. Margin values should be positive.
         * Call {@link #setFLayoutParams(FLayoutParams)} after reassigning a new value
         * to this field.
         */
        public int bottomMargin;

        public FLayoutParams(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public FLayoutParams(FLayoutParams source) {
            this.width = source.width;
            this.height = source.height;
            this.leftMargin = source.leftMargin;
            this.topMargin = source.topMargin;
            this.rightMargin = source.rightMargin;
            this.bottomMargin = source.bottomMargin;
        }

        public FLayoutParams(ViewGroup.LayoutParams source) {
            this.width = source.width;
            this.height = source.height;
        }

        public FLayoutParams(ViewGroup.MarginLayoutParams source) {
            this.width = source.width;
            this.height = source.height;
            this.leftMargin = source.leftMargin;
            this.topMargin = source.topMargin;
            this.rightMargin = source.rightMargin;
            this.bottomMargin = source.bottomMargin;
        }

        public FLayoutParams setMargins(int left, int top, int right, int bottom) {
            leftMargin = left;
            topMargin = top;
            rightMargin = right;
            bottomMargin = bottom;
            return this;
        }
    }
}
