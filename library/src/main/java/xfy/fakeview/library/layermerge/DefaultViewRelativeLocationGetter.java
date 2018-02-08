package xfy.fakeview.library.layermerge;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

/**
 * Created by XiongFangyu on 2018/2/7.
 *
 * Get view relative location by parent's margin and padding
 */
public class DefaultViewRelativeLocationGetter implements ViewRelativeLocationGetter {
    @Override
    public int[] getViewRelativeLocation(View view, ViewGroup root) {
        int[] loc = new int[2];
        setViewMargin(view, loc);
        ViewGroup parent = getViewParent(view);
        while (parent != root && parent != null) {
            setMarginByParent(parent, loc);
            parent = getViewParent(parent);
        }
        return loc;
    }

    private static ViewGroup getViewParent(View v) {
        ViewParent vp = v.getParent();
        if (vp instanceof ViewGroup)
            return (ViewGroup) vp;
        return null;
    }

    private static void setMarginByParent(ViewGroup parent, int[] margins) {
        margins[0] += parent.getPaddingLeft();
        margins[1] += parent.getPaddingTop();
        setViewMargin(parent, margins);
    }

    private static void setViewMargin(View v, int[] margins) {
        ViewGroup.MarginLayoutParams params = null;
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
        }
        if (params != null) {
            margins[0] += params.leftMargin;
            margins[1] += params.topMargin;
        }
    }
}
