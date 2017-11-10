package xfy.fakeview.library.fview.utils;

import android.annotation.SuppressLint;
import android.support.annotation.IntDef;
import android.support.annotation.IntRange;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import xfy.fakeview.library.fview.IFViewGroup;

/**
 * A FMeasureSpec encapsulates the layout requirements passed from parent to child.
 * Each FMeasureSpec represents a requirement for either the width or the height.
 * A FMeasureSpec is comprised of a size and a mode. There are three possible
 * modes:
 * <dl>
 * <dt>UNSPECIFIED</dt>
 * <dd>
 * The parent has not imposed any constraint on the child. It can be whatever size
 * it wants.
 * </dd>
 *
 * <dt>EXACTLY</dt>
 * <dd>
 * The parent has determined an exact size for the child. The child is going to be
 * given those bounds regardless of how big it wants to be.
 * </dd>
 *
 * <dt>AT_MOST</dt>
 * <dd>
 * The child can be as large as it wants up to the specified size.
 * </dd>
 * </dl>
 *
 * MeasureSpecs are implemented as ints to reduce object allocation. This class
 * is provided to pack and unpack the &lt;size, mode&gt; tuple into the int.
 */
public class FMeasureSpec {
    private static final int MODE_SHIFT = 30;
    private static final int MODE_MASK  = 0x3 << MODE_SHIFT;

    /** @hide */
    @IntDef({UNSPECIFIED, EXACTLY, AT_MOST})
    @Retention(RetentionPolicy.SOURCE)
    public @interface MeasureSpecMode {}

    /**
     * Measure specification mode: The parent has not imposed any constraint
     * on the child. It can be whatever size it wants.
     */
    public static final int UNSPECIFIED = 0 << MODE_SHIFT;

    /**
     * Measure specification mode: The parent has determined an exact size
     * for the child. The child is going to be given those bounds regardless
     * of how big it wants to be.
     */
    public static final int EXACTLY     = 1 << MODE_SHIFT;

    /**
     * Measure specification mode: The child can be as large as it wants up
     * to the specified size.
     */
    public static final int AT_MOST     = 2 << MODE_SHIFT;

    /**
     * Bit of {@link #getMeasuredWidthAndState()} and
     * {@link #getMeasuredWidthAndState()} that indicates the measured size
     * is smaller that the space the view would like to have.
     */
    public static final int MEASURED_STATE_TOO_SMALL = 0x01000000;

    /**
     * Bits of {@link #getMeasuredWidthAndState()} and
     * {@link #getMeasuredWidthAndState()} that provide the additional state bits.
     */
    public static final int MEASURED_STATE_MASK = 0xff000000;

    /**
     * Creates a measure specification based on the supplied size and mode.
     *
     * The mode must always be one of the following:
     * <ul>
     *  <li>{@link android.view.View.MeasureSpec#UNSPECIFIED}</li>
     *  <li>{@link android.view.View.MeasureSpec#EXACTLY}</li>
     *  <li>{@link android.view.View.MeasureSpec#AT_MOST}</li>
     * </ul>
     *
     * <p><strong>Note:</strong> On API level 17 and lower, makeMeasureSpec's
     * implementation was such that the order of arguments did not matter
     * and overflow in either value could impact the resulting FMeasureSpec.
     * {@link android.widget.RelativeLayout} was affected by this bug.
     * Apps targeting API levels greater than 17 will get the fixed, more strict
     * behavior.</p>
     *
     * @param size the size of the measure specification
     * @param mode the mode of the measure specification
     * @return the measure specification based on size and mode
     */
    public static int makeMeasureSpec(@IntRange(from = 0, to = (1 << FMeasureSpec.MODE_SHIFT) - 1) int size,
                                      @MeasureSpecMode int mode) {
        return (size & ~MODE_MASK) | (mode & MODE_MASK);
    }

    /**
     * Like {@link #makeMeasureSpec(int, int)}, but any spec with a mode of UNSPECIFIED
     * will automatically get a size of 0. Older apps expect this.
     *
     * @hide internal use only for compatibility with system widgets and older apps
     */
    public static int makeSafeMeasureSpec(int size, int mode) {
        return makeMeasureSpec(size, mode);
    }

    /**
     * Extracts the mode from the supplied measure specification.
     *
     * @param measureSpec the measure specification to extract the mode from
     * @return {@link android.view.View.MeasureSpec#UNSPECIFIED},
     *         {@link android.view.View.MeasureSpec#AT_MOST} or
     *         {@link android.view.View.MeasureSpec#EXACTLY}
     */
    @SuppressLint("WrongConstant")
    @MeasureSpecMode
    public static int getMode(int measureSpec) {
        //noinspection ResourceType
        return (measureSpec & MODE_MASK);
    }

    /**
     * Extracts the size from the supplied measure specification.
     *
     * @param measureSpec the measure specification to extract the size from
     * @return the size in pixels defined in the supplied measure specification
     */
    public static int getSize(int measureSpec) {
        return (measureSpec & ~MODE_MASK);
    }

    static int adjust(int measureSpec, int delta) {
        final int mode = getMode(measureSpec);
        int size = getSize(measureSpec);
        if (mode == UNSPECIFIED) {
            // No need to adjust size for UNSPECIFIED mode.
            return makeMeasureSpec(size, UNSPECIFIED);
        }
        size += delta;
        if (size < 0) {
            size = 0;
        }
        return makeMeasureSpec(size, mode);
    }

    /**
     * Returns a String representation of the specified measure
     * specification.
     *
     * @param measureSpec the measure specification to convert to a String
     * @return a String with the following format: "FMeasureSpec: MODE SIZE"
     */
    public static String toString(int measureSpec) {
        int mode = getMode(measureSpec);
        int size = getSize(measureSpec);

        StringBuilder sb = new StringBuilder("FMeasureSpec: ");

        if (mode == UNSPECIFIED)
            sb.append("UNSPECIFIED ");
        else if (mode == EXACTLY)
            sb.append("EXACTLY ");
        else if (mode == AT_MOST)
            sb.append("AT_MOST ");
        else
            sb.append(mode).append(" ");

        sb.append(size);
        return sb.toString();
    }

    /**
     * Utility to return a default size. Uses the supplied size if the
     * FMeasureSpec imposed no constraints. Will get larger if allowed
     * by the FMeasureSpec.
     *
     * @param size Default size for this view
     * @param measureSpec Constraints imposed by the parent
     * @return The size this view should be.
     */
    public static int getDefaultSize(int size, int measureSpec) {
        int result = size;
        int specMode = getMode(measureSpec);
        int specSize = getSize(measureSpec);

        switch (specMode) {
            case UNSPECIFIED:
                result = size;
                break;
            case AT_MOST:
            case EXACTLY:
                result = specSize;
                break;
        }
        return result;
    }

    public static int getChildMeasureSpec(int spec, int padding, int childDimension) {
        int specMode = FMeasureSpec.getMode(spec);
        int specSize = FMeasureSpec.getSize(spec);

        int size = Math.max(0, specSize - padding);

        int resultSize = 0;
        int resultMode = 0;

        switch (specMode) {
            // Parent has imposed an exact size on us
            case FMeasureSpec.EXACTLY:
                if (childDimension >= 0) {
                    resultSize = childDimension;
                    resultMode = FMeasureSpec.EXACTLY;
                } else if (childDimension == IFViewGroup.FLayoutParams.MATCH_PARENT) {
                    // Child wants to be our size. So be it.
                    resultSize = size;
                    resultMode = FMeasureSpec.EXACTLY;
                } else if (childDimension == IFViewGroup.FLayoutParams.WRAP_CONTENT) {
                    // Child wants to determine its own size. It can't be
                    // bigger than us.
                    resultSize = size;
                    resultMode = FMeasureSpec.AT_MOST;
                }
                break;

            // Parent has imposed a maximum size on us
            case FMeasureSpec.AT_MOST:
                if (childDimension >= 0) {
                    // Child wants a specific size... so be it
                    resultSize = childDimension;
                    resultMode = FMeasureSpec.EXACTLY;
                } else if (childDimension == IFViewGroup.FLayoutParams.MATCH_PARENT) {
                    // Child wants to be our size, but our size is not fixed.
                    // Constrain child to not be bigger than us.
                    resultSize = size;
                    resultMode = FMeasureSpec.AT_MOST;
                } else if (childDimension == IFViewGroup.FLayoutParams.WRAP_CONTENT) {
                    // Child wants to determine its own size. It can't be
                    // bigger than us.
                    resultSize = size;
                    resultMode = FMeasureSpec.AT_MOST;
                }
                break;

            // Parent asked to see how big we want to be
            case FMeasureSpec.UNSPECIFIED:
                if (childDimension >= 0) {
                    // Child wants a specific size... let him have it
                    resultSize = childDimension;
                    resultMode = FMeasureSpec.EXACTLY;
                } else if (childDimension == IFViewGroup.FLayoutParams.MATCH_PARENT) {
                    // Child wants to be our size... find out how big it should
                    // be
                    resultSize = size;
                    resultMode = FMeasureSpec.UNSPECIFIED;
                } else if (childDimension == IFViewGroup.FLayoutParams.WRAP_CONTENT) {
                    // Child wants to determine its own size.... find out how
                    // big it should be
                    resultSize = size;
                    resultMode = FMeasureSpec.UNSPECIFIED;
                }
                break;
        }
        //noinspection ResourceType
        return FMeasureSpec.makeMeasureSpec(resultSize, resultMode);
    }

    /**
     * Utility to reconcile a desired size and state, with constraints imposed
     * by a FMeasureSpec. Will take the desired size, unless a different size
     * is imposed by the constraints. The returned value is a compound integer,
     * with the resolved size in the {@link #MEASURED_SIZE_MASK} bits and
     * optionally the bit {@link #MEASURED_STATE_TOO_SMALL} set if the
     * resulting size is smaller than the size the view wants to be.
     *
     * @param size How big the view wants to be.
     * @param measureSpec Constraints imposed by the parent.
     * @param childMeasuredState Size information bit mask for the view's
     *                           children.
     * @return Size information bit mask as defined by
     *         {@link #MEASURED_SIZE_MASK} and
     *         {@link #MEASURED_STATE_TOO_SMALL}.
     */
    public static int resolveSizeAndState(int size, int measureSpec, int childMeasuredState) {
        final int specMode = FMeasureSpec.getMode(measureSpec);
        final int specSize = FMeasureSpec.getSize(measureSpec);
        final int result;
        switch (specMode) {
            case FMeasureSpec.AT_MOST:
                if (specSize < size) {
                    result = specSize | MEASURED_STATE_TOO_SMALL;
                } else {
                    result = size;
                }
                break;
            case FMeasureSpec.EXACTLY:
                result = specSize;
                break;
            case FMeasureSpec.UNSPECIFIED:
            default:
                result = size;
        }
        return result | (childMeasuredState & MEASURED_STATE_MASK);
    }
}