package xfy.fakeview.library.fview.normal;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.widget.FrameLayout;

import java.util.ArrayList;

import xfy.fakeview.library.fview.FView;
import xfy.fakeview.library.fview.FViewGroup;
import xfy.fakeview.library.fview.IFViewGroup;
import xfy.fakeview.library.fview.IFViewRoot;
import xfy.fakeview.library.fview.utils.FMeasureSpec;

/**
 * Created by XiongFangyu on 2017/11/7.
 *
 * like {@link FrameLayout}
 */
public class FFrameLayout extends FViewGroup {
    private static final int DEFAULT_CHILD_GRAVITY = Gravity.TOP | Gravity.START;

    private final ArrayList<FView> mMatchParentChildren = new ArrayList<>(1);

    public FFrameLayout(Context context, IFViewRoot viewRoot) {
        super(context, viewRoot);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int count = getChildCount();
        final boolean measureMatchParentChildren =
                FMeasureSpec.getMode(widthMeasureSpec) != FMeasureSpec.EXACTLY ||
                        FMeasureSpec.getMode(heightMeasureSpec) != FMeasureSpec.EXACTLY;
        mMatchParentChildren.clear();

        int maxHeight = 0;
        int maxWidth = 0;
        int childState = 0;

        for (int i = 0; i < count; i++) {
            final FView child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
                final LayoutParams lp = (LayoutParams) child.getFLayoutParams();
                maxWidth = Math.max(maxWidth,
                        child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin);
                maxHeight = Math.max(maxHeight,
                        child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin);
                childState = combineMeasuredStates(childState, child.getMeasuredState());
                if (measureMatchParentChildren) {
                    if (lp.width == LayoutParams.MATCH_PARENT ||
                            lp.height == LayoutParams.MATCH_PARENT) {
                        mMatchParentChildren.add(child);
                    }
                }
            }
        }

        // Account for padding too
        maxWidth += getPaddingLeft() + getPaddingRight();
        maxHeight += getPaddingTop() + getPaddingBottom();
        maxHeight = Math.max(maxHeight, getSuggestedMinimumHeight());
        maxWidth = Math.max(maxWidth, getSuggestedMinimumWidth());

        setMeasuredDimension(FMeasureSpec.resolveSizeAndState(maxWidth, widthMeasureSpec, childState),
                FMeasureSpec.resolveSizeAndState(maxHeight, heightMeasureSpec,
                        childState << MEASURED_HEIGHT_STATE_SHIFT));

        count = mMatchParentChildren.size();
        if (count > 1) {
            for (int i = 0; i < count; i++) {
                final FView child = mMatchParentChildren.get(i);
                final LayoutParams lp = (LayoutParams) child.getFLayoutParams();

                final int childWidthMeasureSpec;
                if (lp.width == LayoutParams.MATCH_PARENT) {
                    final int width = Math.max(0, getMeasuredWidth()
                            - getPaddingLeft() - getPaddingRight()
                            - lp.leftMargin - lp.rightMargin);
                    childWidthMeasureSpec = FMeasureSpec.makeMeasureSpec(
                            width, FMeasureSpec.EXACTLY);
                } else {
                    childWidthMeasureSpec = FMeasureSpec.getChildMeasureSpec(widthMeasureSpec,
                            getPaddingLeft() + getPaddingRight() +
                                    lp.leftMargin + lp.rightMargin,
                            lp.width);
                }

                final int childHeightMeasureSpec;
                if (lp.height == LayoutParams.MATCH_PARENT) {
                    final int height = Math.max(0, getMeasuredHeight()
                            - getPaddingTop() - getPaddingBottom()
                            - lp.topMargin - lp.bottomMargin);
                    childHeightMeasureSpec = FMeasureSpec.makeMeasureSpec(
                            height, FMeasureSpec.EXACTLY);
                } else {
                    childHeightMeasureSpec = FMeasureSpec.getChildMeasureSpec(heightMeasureSpec,
                            getPaddingTop() + getPaddingBottom() +
                                    lp.topMargin + lp.bottomMargin,
                            lp.height);
                }

                child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
            }
        }
    }

    @Override
    public void layoutChild(FView child) {
        final int parentLeft = getPaddingLeft();
        final int parentRight = bounds.right - bounds.left - getPaddingRight();

        final int parentTop = getPaddingTop();
        final int parentBottom = bounds.bottom - bounds.top - getPaddingBottom();
        layoutChild(child, parentLeft, parentRight, parentTop, parentBottom);
    }

    private void layoutChild(FView child, int parentLeft, int parentRight, int parentTop, int parentBottom) {
        if (child.getVisibility() != GONE) {
            final LayoutParams lp = (LayoutParams) child.getFLayoutParams();

            final int width = child.getMeasuredWidth();
            final int height = child.getMeasuredHeight();

            int childLeft;
            int childTop;

            int gravity = lp.gravity;
            if (gravity == -1) {
                gravity = DEFAULT_CHILD_GRAVITY;
            }

            final int layoutDirection = 0;
            final int absoluteGravity = Gravity.getAbsoluteGravity(gravity, layoutDirection);
            final int verticalGravity = gravity & Gravity.VERTICAL_GRAVITY_MASK;

            switch (absoluteGravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
                case Gravity.CENTER_HORIZONTAL:
                    childLeft = parentLeft + (parentRight - parentLeft - width) / 2 +
                            lp.leftMargin - lp.rightMargin;
                    break;
                case Gravity.RIGHT:
                    childLeft = parentRight - width - lp.rightMargin;
                    break;
                case Gravity.LEFT:
                default:
                    childLeft = parentLeft + lp.leftMargin;
            }

            switch (verticalGravity) {
                case Gravity.TOP:
                    childTop = parentTop + lp.topMargin;
                    break;
                case Gravity.CENTER_VERTICAL:
                    childTop = parentTop + (parentBottom - parentTop - height) / 2 +
                            lp.topMargin - lp.bottomMargin;
                    break;
                case Gravity.BOTTOM:
                    childTop = parentBottom - height - lp.bottomMargin;
                    break;
                default:
                    childTop = parentTop + lp.topMargin;
            }

            child.layout(childLeft, childTop, childLeft + width, childTop + height);
        }
    }

    @Override
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        final int count = getChildCount();

        final int parentLeft = getPaddingLeft();
        final int parentRight = bounds.right - bounds.left - getPaddingRight();

        final int parentTop = getPaddingTop();
        final int parentBottom = bounds.bottom - bounds.top - getPaddingBottom();

        for (int i = 0; i < count; i++) {
            final FView child = getChildAt(i);
            layoutChild(child, parentLeft, parentRight, parentTop, parentBottom);
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
        public static final int UNSPECIFIED_GRAVITY = -1;

        public int gravity = UNSPECIFIED_GRAVITY;

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(int width, int height, int gravity) {
            super(width, height);
            this.gravity = gravity;
        }

        public LayoutParams(FLayoutParams source) {
            super(source);
        }

        public LayoutParams(@NonNull LayoutParams source) {
            super(source);
            this.gravity = source.gravity;
        }

        public LayoutParams(@NonNull FrameLayout.LayoutParams source) {
            super(source);
            this.gravity = source.gravity;
        }
    }
}
