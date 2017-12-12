package xfy.fakeview.library.fview.normal;

import android.content.Context;
import android.os.Build;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.widget.LinearLayout;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import xfy.fakeview.library.fview.FView;
import xfy.fakeview.library.fview.FViewGroup;
import xfy.fakeview.library.fview.IFViewGroup;
import xfy.fakeview.library.fview.IFViewRoot;
import xfy.fakeview.library.fview.utils.FMeasureSpec;

/**
 * Created by XiongFangyu on 2017/12/12.
 *
 * like {@link LinearLayout}
 */
public class FLinearLayout extends FViewGroup {
    @IntDef({HORIZONTAL, VERTICAL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface OrientationMode {}
    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;

    private int mOrientation;
    private final boolean mAllowInconsistentMeasurement;

    public FLinearLayout(Context context, IFViewRoot viewRoot) {
        super(context, viewRoot);
        final int version = context.getApplicationInfo().targetSdkVersion;
        mAllowInconsistentMeasurement = version <= Build.VERSION_CODES.M;
    }

    /**
     * Should the layout be a column or a row.
     * @param orientation Pass {@link #HORIZONTAL} or {@link #VERTICAL}. Default
     * value is {@link #HORIZONTAL}.
     *
     * @attr ref android.R.styleable#LinearLayout_orientation
     */
    public void setOrientation(@OrientationMode int orientation) {
        if (mOrientation != orientation) {
            mOrientation = orientation;
            requestFViewTreeLayout();
        }
    }

    /**
     * Returns the current orientation.
     *
     * @return either {@link #HORIZONTAL} or {@link #VERTICAL}
     */
    @OrientationMode
    public int getOrientation() {
        return mOrientation;
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mOrientation == VERTICAL) {
            measureVertical(widthMeasureSpec, heightMeasureSpec);
        } else {
            measureHorizontal(widthMeasureSpec, heightMeasureSpec);
        }
    }

    /**
     * Measures the children when the orientation of this LinearLayout is set
     * to {@link #VERTICAL}.
     *
     * @param widthMeasureSpec Horizontal space requirements as imposed by the parent.
     * @param heightMeasureSpec Vertical space requirements as imposed by the parent.
     *
     * @see #getOrientation()
     * @see #setOrientation(int)
     * @see #onMeasure(int, int)
     */
    private void measureVertical(int widthMeasureSpec, int heightMeasureSpec) {
        int mTotalLength = 0;
        int maxWidth = 0;
        int childState = 0;
        int alternativeMaxWidth = 0;
        int weightedMaxWidth = 0;
        boolean allFillParent = true;
        float totalWeight = 0;

        final int count = getChildCount();

        final int widthMode = FMeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = FMeasureSpec.getMode(heightMeasureSpec);

        boolean matchWidth = false;
        boolean skippedMeasure = false;

        int largestChildHeight = Integer.MIN_VALUE;
        int consumedExcessSpace = 0;

        // See how tall everyone is. Also remember max width.
        for (int i = 0; i < count; ++i) {
            final FView child = getChildAt(i);
            if (child == null) {
                continue;
            }

            if (child.getVisibility() == FView.GONE) {
                continue;
            }

            final LayoutParams lp = (LayoutParams) child.getFLayoutParams();

            totalWeight += lp.weight;

            final boolean useExcessSpace = lp.height == 0 && lp.weight > 0;
            if (heightMode == FMeasureSpec.EXACTLY && useExcessSpace) {
                // Optimization: don't bother measuring children who are only
                // laid out using excess space. These views will get measured
                // later if we have space to distribute.
                final int totalLength = mTotalLength;
                mTotalLength = Math.max(totalLength, totalLength + lp.topMargin + lp.bottomMargin);
                skippedMeasure = true;
            } else {
                if (useExcessSpace) {
                    // The heightMode is either UNSPECIFIED or AT_MOST, and
                    // this child is only laid out using excess space. Measure
                    // using WRAP_CONTENT so that we can find out the view's
                    // optimal height. We'll restore the original height of 0
                    // after measurement.
                    lp.height = LayoutParams.WRAP_CONTENT;
                }

                // Determine how big this child would like to be. If this or
                // previous children have given a weight, then we allow it to
                // use all available space (and we will shrink things later
                // if needed).
                final int usedHeight = totalWeight == 0 ? mTotalLength : 0;
                measureChildBeforeLayout(child, i, widthMeasureSpec, 0,
                        heightMeasureSpec, usedHeight);

                final int childHeight = child.getMeasuredHeight();
                if (useExcessSpace) {
                    // Restore the original height and record how much space
                    // we've allocated to excess-only children so that we can
                    // match the behavior of EXACTLY measurement.
                    lp.height = 0;
                    consumedExcessSpace += childHeight;
                }

                final int totalLength = mTotalLength;
                mTotalLength = Math.max(totalLength, totalLength + childHeight + lp.topMargin +
                        lp.bottomMargin);
            }

            boolean matchWidthLocally = false;
            if (widthMode != FMeasureSpec.EXACTLY && lp.width == LayoutParams.MATCH_PARENT) {
                // The width of the linear layout will scale, and at least one
                // child said it wanted to match our width. Set a flag
                // indicating that we need to remeasure at least that view when
                // we know our width.
                matchWidth = true;
                matchWidthLocally = true;
            }

            final int margin = lp.leftMargin + lp.rightMargin;
            final int measuredWidth = child.getMeasuredWidth() + margin;
            maxWidth = Math.max(maxWidth, measuredWidth);
            childState = combineMeasuredStates(childState, child.getMeasuredState());

            allFillParent = allFillParent && lp.width == LayoutParams.MATCH_PARENT;
            if (lp.weight > 0) {
                /*
                 * Widths of weighted Views are bogus if we end up
                 * remeasuring, so keep them separate.
                 */
                weightedMaxWidth = Math.max(weightedMaxWidth,
                        matchWidthLocally ? margin : measuredWidth);
            } else {
                alternativeMaxWidth = Math.max(alternativeMaxWidth,
                        matchWidthLocally ? margin : measuredWidth);
            }
        }

        // Add in our padding
        mTotalLength += padding.top + padding.bottom;

        int heightSize = mTotalLength;

        // Check against our minimum height
        heightSize = Math.max(heightSize, getSuggestedMinimumHeight());

        // Reconcile our calculated size with the heightMeasureSpec
        int heightSizeAndState = FMeasureSpec.resolveSizeAndState(heightSize, heightMeasureSpec, 0);
        heightSize = heightSizeAndState & FMeasureSpec.MEASURED_SIZE_MASK;

        // Either expand children with weight to take up available space or
        // shrink them if they extend beyond our current bounds. If we skipped
        // measurement on any children, we need to measure them now.
        int remainingExcess = heightSize - mTotalLength
                + (mAllowInconsistentMeasurement ? 0 : consumedExcessSpace);
        if (skippedMeasure || remainingExcess != 0 && totalWeight > 0.0f) {
            float remainingWeightSum = totalWeight;

            mTotalLength = 0;

            for (int i = 0; i < count; ++i) {
                final FView child = getChildAt(i);
                if (child == null || child.getVisibility() == FView.GONE) {
                    continue;
                }

                final LayoutParams lp = (LayoutParams) child.getFLayoutParams();
                final float childWeight = lp.weight;
                if (childWeight > 0) {
                    final int share = (int) (childWeight * remainingExcess / remainingWeightSum);
                    remainingExcess -= share;
                    remainingWeightSum -= childWeight;

                    final int childHeight;
                    if (lp.height == 0 && (!mAllowInconsistentMeasurement
                            || heightMode == FMeasureSpec.EXACTLY)) {
                        // This child needs to be laid out from scratch using
                        // only its share of excess space.
                        childHeight = share;
                    } else {
                        // This child had some intrinsic height to which we
                        // need to add its share of excess space.
                        childHeight = child.getMeasuredHeight() + share;
                    }

                    final int childHeightMeasureSpec = FMeasureSpec.makeMeasureSpec(
                            Math.max(0, childHeight), FMeasureSpec.EXACTLY);
                    final int childWidthMeasureSpec = FMeasureSpec.getChildMeasureSpec(widthMeasureSpec,
                            padding.left + padding.right + lp.leftMargin + lp.rightMargin,
                            lp.width);
                    child.measure(childWidthMeasureSpec, childHeightMeasureSpec);

                    // Child may now not fit in vertical dimension.
                    childState = combineMeasuredStates(childState, child.getMeasuredState()
                            & (MEASURED_STATE_MASK>>MEASURED_HEIGHT_STATE_SHIFT));
                }

                final int margin =  lp.leftMargin + lp.rightMargin;
                final int measuredWidth = child.getMeasuredWidth() + margin;
                maxWidth = Math.max(maxWidth, measuredWidth);

                boolean matchWidthLocally = widthMode != FMeasureSpec.EXACTLY &&
                        lp.width == LayoutParams.MATCH_PARENT;

                alternativeMaxWidth = Math.max(alternativeMaxWidth,
                        matchWidthLocally ? margin : measuredWidth);

                allFillParent = allFillParent && lp.width == LayoutParams.MATCH_PARENT;

                final int totalLength = mTotalLength;
                mTotalLength = Math.max(totalLength, totalLength + child.getMeasuredHeight() +
                        lp.topMargin + lp.bottomMargin);
            }

            // Add in our padding
            mTotalLength += padding.top + padding.bottom;
        } else {
            alternativeMaxWidth = Math.max(alternativeMaxWidth,
                    weightedMaxWidth);
        }

        if (!allFillParent && widthMode != FMeasureSpec.EXACTLY) {
            maxWidth = alternativeMaxWidth;
        }

        maxWidth += padding.left + padding.right;

        // Check against our minimum width
        maxWidth = Math.max(maxWidth, getSuggestedMinimumWidth());

        setMeasuredDimension(FMeasureSpec.resolveSizeAndState(maxWidth, widthMeasureSpec, childState),
                heightSizeAndState);

        if (matchWidth) {
            forceUniformWidth(count, heightMeasureSpec);
        }
    }
    
    /**
     * Measures the children when the orientation of this LinearLayout is set
     * to {@link #HORIZONTAL}.
     *
     * @param widthMeasureSpec Horizontal space requirements as imposed by the parent.
     * @param heightMeasureSpec Vertical space requirements as imposed by the parent.
     *
     * @see #getOrientation()
     * @see #setOrientation(int)
     * @see #onMeasure(int, int)
     */
    private void measureHorizontal(int widthMeasureSpec, int heightMeasureSpec) {
        int mTotalLength = 0;
        int maxHeight = 0;
        int childState = 0;
        int alternativeMaxHeight = 0;
        int weightedMaxHeight = 0;
        boolean allFillParent = true;
        float totalWeight = 0;

        final int count = getChildCount();

        final int widthMode = FMeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = FMeasureSpec.getMode(heightMeasureSpec);

        boolean matchHeight = false;
        boolean skippedMeasure = false;

        final boolean isExactly = widthMode == FMeasureSpec.EXACTLY;

        int largestChildWidth = Integer.MIN_VALUE;
        int usedExcessSpace = 0;

        // See how wide everyone is. Also remember max height.
        for (int i = 0; i < count; ++i) {
            final FView child = getChildAt(i);
            if (child == null) {
                continue;
            }

            if (child.getVisibility() == GONE) {
                continue;
            }

            final LayoutParams lp = (LayoutParams) child.getFLayoutParams();

            totalWeight += lp.weight;

            final boolean useExcessSpace = lp.width == 0 && lp.weight > 0;
            if (widthMode == FMeasureSpec.EXACTLY && useExcessSpace) {
                // Optimization: don't bother measuring children who are only
                // laid out using excess space. These views will get measured
                // later if we have space to distribute.
                if (isExactly) {
                    mTotalLength += lp.leftMargin + lp.rightMargin;
                } else {
                    final int totalLength = mTotalLength;
                    mTotalLength = Math.max(totalLength, totalLength +
                            lp.leftMargin + lp.rightMargin);
                }
                skippedMeasure = true;
            } else {
                if (useExcessSpace) {
                    // The widthMode is either UNSPECIFIED or AT_MOST, and
                    // this child is only laid out using excess space. Measure
                    // using WRAP_CONTENT so that we can find out the view's
                    // optimal width. We'll restore the original width of 0
                    // after measurement.
                    lp.width = LayoutParams.WRAP_CONTENT;
                }

                // Determine how big this child would like to be. If this or
                // previous children have given a weight, then we allow it to
                // use all available space (and we will shrink things later
                // if needed).
                final int usedWidth = totalWeight == 0 ? mTotalLength : 0;
                measureChildBeforeLayout(child, i, widthMeasureSpec, usedWidth,
                        heightMeasureSpec, 0);

                final int childWidth = child.getMeasuredWidth();
                if (useExcessSpace) {
                    // Restore the original width and record how much space
                    // we've allocated to excess-only children so that we can
                    // match the behavior of EXACTLY measurement.
                    lp.width = 0;
                    usedExcessSpace += childWidth;
                }

                if (isExactly) {
                    mTotalLength += childWidth + lp.leftMargin + lp.rightMargin;
                } else {
                    final int totalLength = mTotalLength;
                    mTotalLength = Math.max(totalLength, totalLength + childWidth + lp.leftMargin
                            + lp.rightMargin);
                }
            }

            boolean matchHeightLocally = false;
            if (heightMode != FMeasureSpec.EXACTLY && lp.height == LayoutParams.MATCH_PARENT) {
                // The height of the linear layout will scale, and at least one
                // child said it wanted to match our height. Set a flag indicating that
                // we need to remeasure at least that view when we know our height.
                matchHeight = true;
                matchHeightLocally = true;
            }

            final int margin = lp.topMargin + lp.bottomMargin;
            final int childHeight = child.getMeasuredHeight() + margin;
            childState = combineMeasuredStates(childState, child.getMeasuredState());

            maxHeight = Math.max(maxHeight, childHeight);

            allFillParent = allFillParent && lp.height == LayoutParams.MATCH_PARENT;
            if (lp.weight > 0) {
                /*
                 * Heights of weighted Views are bogus if we end up
                 * remeasuring, so keep them separate.
                 */
                weightedMaxHeight = Math.max(weightedMaxHeight,
                        matchHeightLocally ? margin : childHeight);
            } else {
                alternativeMaxHeight = Math.max(alternativeMaxHeight,
                        matchHeightLocally ? margin : childHeight);
            }
        }

        // Add in our padding
        mTotalLength += padding.left + padding.right;

        int widthSize = mTotalLength;

        // Check against our minimum width
        widthSize = Math.max(widthSize, getSuggestedMinimumWidth());

        // Reconcile our calculated size with the widthMeasureSpec
        int widthSizeAndState = FMeasureSpec.resolveSizeAndState(widthSize, widthMeasureSpec, 0);
        widthSize = widthSizeAndState & FMeasureSpec.MEASURED_SIZE_MASK;

        // Either expand children with weight to take up available space or
        // shrink them if they extend beyond our current bounds. If we skipped
        // measurement on any children, we need to measure them now.
        int remainingExcess = widthSize - mTotalLength
                + (mAllowInconsistentMeasurement ? 0 : usedExcessSpace);
        if (skippedMeasure || remainingExcess != 0 && totalWeight > 0.0f) {
            float remainingWeightSum = totalWeight;

            maxHeight = -1;

            mTotalLength = 0;

            for (int i = 0; i < count; ++i) {
                final FView child = getChildAt(i);
                if (child == null || child.getVisibility() == FView.GONE) {
                    continue;
                }

                final LayoutParams lp = (LayoutParams) child.getFLayoutParams();
                final float childWeight = lp.weight;
                if (childWeight > 0) {
                    final int share = (int) (childWeight * remainingExcess / remainingWeightSum);
                    remainingExcess -= share;
                    remainingWeightSum -= childWeight;

                    final int childWidth;
                    if (lp.width == 0 && (!mAllowInconsistentMeasurement
                            || widthMode == FMeasureSpec.EXACTLY)) {
                        // This child needs to be laid out from scratch using
                        // only its share of excess space.
                        childWidth = share;
                    } else {
                        // This child had some intrinsic width to which we
                        // need to add its share of excess space.
                        childWidth = child.getMeasuredWidth() + share;
                    }

                    final int childWidthMeasureSpec = FMeasureSpec.makeMeasureSpec(
                            Math.max(0, childWidth), FMeasureSpec.EXACTLY);
                    final int childHeightMeasureSpec = FMeasureSpec.getChildMeasureSpec(heightMeasureSpec,
                            padding.top + padding.bottom + lp.topMargin + lp.bottomMargin,
                            lp.height);
                    child.measure(childWidthMeasureSpec, childHeightMeasureSpec);

                    // Child may now not fit in horizontal dimension.
                    childState = combineMeasuredStates(childState,
                            child.getMeasuredState() & MEASURED_STATE_MASK);
                }

                if (isExactly) {
                    mTotalLength += child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
                } else {
                    final int totalLength = mTotalLength;
                    mTotalLength = Math.max(totalLength, totalLength + child.getMeasuredWidth() +
                            lp.leftMargin + lp.rightMargin);
                }

                boolean matchHeightLocally = heightMode != FMeasureSpec.EXACTLY &&
                        lp.height == LayoutParams.MATCH_PARENT;

                final int margin = lp.topMargin + lp .bottomMargin;
                int childHeight = child.getMeasuredHeight() + margin;
                maxHeight = Math.max(maxHeight, childHeight);
                alternativeMaxHeight = Math.max(alternativeMaxHeight,
                        matchHeightLocally ? margin : childHeight);

                allFillParent = allFillParent && lp.height == LayoutParams.MATCH_PARENT;
            }

            // Add in our padding
            mTotalLength += padding.left + padding.right;
        } else {
            alternativeMaxHeight = Math.max(alternativeMaxHeight, weightedMaxHeight);
        }

        if (!allFillParent && heightMode != FMeasureSpec.EXACTLY) {
            maxHeight = alternativeMaxHeight;
        }

        maxHeight += padding.top + padding.bottom;

        // Check against our minimum height
        maxHeight = Math.max(maxHeight, getSuggestedMinimumHeight());

        setMeasuredDimension(widthSizeAndState | (childState&MEASURED_STATE_MASK),
                FMeasureSpec.resolveSizeAndState(maxHeight, heightMeasureSpec,
                        (childState<<MEASURED_HEIGHT_STATE_SHIFT)));

        if (matchHeight) {
            forceUniformHeight(count, widthMeasureSpec);
        }
    }
    
    private void forceUniformWidth(int count, int heightMeasureSpec) {
        // Pretend that the linear layout has an exact size.
        int uniformMeasureSpec = FMeasureSpec.makeMeasureSpec(getMeasuredWidth(),
                FMeasureSpec.EXACTLY);
        for (int i = 0; i< count; ++i) {
            final FView child = getChildAt(i);
            if (child != null && child.getVisibility() != GONE) {
                LayoutParams lp = ((LayoutParams)child.getFLayoutParams());

                if (lp.width == LayoutParams.MATCH_PARENT) {
                    // Temporarily force children to reuse their old measured height
                    int oldHeight = lp.height;
                    lp.height = child.getMeasuredHeight();

                    // Remeasue with new dimensions
                    measureChildWithMargins(child, uniformMeasureSpec, 0, heightMeasureSpec, 0);
                    lp.height = oldHeight;
                }
            }
        }
    }

    private void forceUniformHeight(int count, int widthMeasureSpec) {
        // Pretend that the linear layout has an exact size. This is the measured height of
        // ourselves. The measured height should be the max height of the children, changed
        // to accommodate the heightMeasureSpec from the parent
        int uniformMeasureSpec = FMeasureSpec.makeMeasureSpec(getMeasuredHeight(),
                FMeasureSpec.EXACTLY);
        for (int i = 0; i < count; ++i) {
            final FView child = getChildAt(i);
            if (child != null && child.getVisibility() != GONE) {
                LayoutParams lp = (LayoutParams) child.getFLayoutParams();

                if (lp.height == LayoutParams.MATCH_PARENT) {
                    // Temporarily force children to reuse their old measured width
                    // FIXME: this may not be right for something like wrapping text?
                    int oldWidth = lp.width;
                    lp.width = child.getMeasuredWidth();

                    // Remeasure with new dimensions
                    measureChildWithMargins(child, widthMeasureSpec, 0, uniformMeasureSpec, 0);
                    lp.width = oldWidth;
                }
            }
        }
    }

    /**
     * <p>Measure the child according to the parent's measure specs. This
     * method should be overriden by subclasses to force the sizing of
     * children. This method is called by {@link #measureVertical(int, int)} and
     * {@link #measureHorizontal(int, int)}.</p>
     *
     * @param child the child to measure
     * @param childIndex the index of the child in this view
     * @param widthMeasureSpec horizontal space requirements as imposed by the parent
     * @param totalWidth extra space that has been used up by the parent horizontally
     * @param heightMeasureSpec vertical space requirements as imposed by the parent
     * @param totalHeight extra space that has been used up by the parent vertically
     */
    void measureChildBeforeLayout(FView child, int childIndex,
                                  int widthMeasureSpec, int totalWidth, int heightMeasureSpec,
                                  int totalHeight) {
        measureChildWithMargins(child, widthMeasureSpec, totalWidth,
                heightMeasureSpec, totalHeight);
    }

    @Override
    public void layoutChild(FView view) {
        if (mOrientation == VERTICAL) {
            layoutVertical();
        } else {
            layoutHorizontal();
        }
    }

    @Override
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        if (mOrientation == VERTICAL) {
            layoutVertical();
        } else {
            layoutHorizontal();
        }
    }

    /**
     * Position the children during a layout pass if the orientation of this
     * LinearLayout is set to {@link #VERTICAL}.
     *
     * @see #getOrientation()
     * @see #setOrientation(int)
     * @see #onLayout(boolean, int, int, int, int)
     */
    void layoutVertical() {
        final int paddingLeft = padding.left;

        int childTop = 0;
        int childLeft = 0;

        final int count = getChildCount();

        for (int i = 0; i < count; i++) {
            final FView child = getChildAt(i);
            if (child == null) {
            } else if (child.getVisibility() != GONE) {
                final int childWidth = child.getMeasuredWidth();
                final int childHeight = child.getMeasuredHeight();

                final LayoutParams lp =
                        (LayoutParams) child.getFLayoutParams();

                childLeft = paddingLeft + lp.leftMargin;

                childTop += lp.topMargin;
                setChildFrame(child, childLeft, childTop,
                        childWidth, childHeight);
                childTop += childHeight + lp.bottomMargin;
            }
        }
    }

    private void setChildFrame(FView child, int left, int top, int width, int height) {
        child.layout(left, top, left + width, top + height);
    }

    /**
     * Position the children during a layout pass if the orientation of this
     * LinearLayout is set to {@link #HORIZONTAL}.
     *
     * @see #getOrientation()
     * @see #setOrientation(int)
     * @see #onLayout(boolean, int, int, int, int)
     */
    void layoutHorizontal() {
        final int paddingTop = padding.top;

        int childTop = 0;
        int childLeft = 0;

        final int count = getChildCount();

        int start = 0;
        int dir = 1;

        for (int i = 0; i < count; i++) {
            final int childIndex = start + dir * i;
            final FView child = getChildAt(childIndex);
            if (child == null) {
            } else if (child.getVisibility() != GONE) {
                final int childWidth = child.getMeasuredWidth();
                final int childHeight = child.getMeasuredHeight();

                final LayoutParams lp =
                        (LayoutParams) child.getFLayoutParams();

                childTop = paddingTop + lp.topMargin;

                childLeft += lp.leftMargin;
                setChildFrame(child, childLeft, childTop,
                        childWidth, childHeight);
                childLeft += childWidth + lp.rightMargin;
            }
        }
    }

    @Override
    public LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    @Override
    protected boolean checkLayoutParams(IFViewGroup.FLayoutParams p) {
        return p instanceof LayoutParams;
    }

    @Override
    protected IFViewGroup.FLayoutParams generateLayoutParams(IFViewGroup.FLayoutParams lp) {
        if (lp instanceof LayoutParams) {
            return new LayoutParams((LayoutParams) lp);
        } else {
            return new LayoutParams(lp);
        }
    }

    public static class LayoutParams extends IFViewGroup.FLayoutParams {
        public float weight = 0;
        public LayoutParams(int width, int height) {
            this(width, height, 0);
        }

        public LayoutParams(int width, int height, float weight) {
            super(width, height);
            this.weight = weight;
        }

        public LayoutParams(FLayoutParams source) {
            super(source);
        }

        public LayoutParams(@NonNull LayoutParams source) {
            super(source);
            this.weight = source.weight;
        }

        public LayoutParams(LinearLayout.LayoutParams source) {
            super(source);
            this.weight = source.weight;
        }
    }
}
