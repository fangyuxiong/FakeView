package xfy.fakeview.library.shallowlayer;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import xfy.fakeview.library.translator.EventExtractor;

/**
 * Created by XiongFangyu on 2018/1/10.
 *
 * Shallow layer tool.
 *
 * Remove view when:
 *  1.view is {@link android.view.ViewGroup}, and the parent is {@link android.view.ViewGroup}, and the class are the same
 *  2.the only child of its parent
 *  3.not in {@link #notShallowClass}
 *  4.size of the view and its parent are the same
 *  5.not contain {@link android.view.View.OnClickListener} and {@link android.view.View.OnLongClickListener}
 *  6.not contain background
 *  7.check {@link #onShallowViewListener} result
 */
public class ShallowLayerManager {
    private final static List<Class<? extends ViewGroup>> notShallowClass = new ArrayList<>();

    public static void registerNotShallowClass(Class<? extends ViewGroup> clz) {
        if (clz != null)
            notShallowClass.add(clz);
    }

    private ViewGroup rootView;
    private OnShallowViewListener onShallowViewListener;

    public ShallowLayerManager(ViewGroup root) {
        this(root, null);
    }

    public ShallowLayerManager(ViewGroup root, OnShallowViewListener listener) {
        rootView = root;
        setOnShallowViewListener(listener);
    }

    public void setOnShallowViewListener(OnShallowViewListener listener) {
        onShallowViewListener = listener;
    }

    public void start() {
        if (rootView == null)
            return;
        shallow(rootView);
    }

    /**
     * Remove children in parent judged by conditions
     * @param parent
     */
    private void shallow(ViewGroup parent) {
        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i ++) {
            View child = parent.getChildAt(i);
            ViewGroup vg = null;
            if (child instanceof ViewGroup) {
                vg = (ViewGroup) child;
            } else {
                continue;
            }
            shallow(vg);
            if (childCount == 1) {
                if (!child.getClass().equals(parent.getClass()))
                    continue;
                if (isNotShallowClass(vg))
                    continue;
                if (!isSameSize(parent, vg))
                    continue;
                if (containEvent(vg))
                    continue;
                if (containBackground(vg))
                    continue;
                if (checkListener(vg))
                    continue;
                removeView(parent, vg);
            }
        }
    }

    private void removeView(ViewGroup parent, ViewGroup needRemoved) {
        parent.removeView(needRemoved);
        final int l = needRemoved.getChildCount();
        for (int i = 0; i < l; i ++) {
            View c = needRemoved.getChildAt(0);
            needRemoved.removeView(c);
            parent.addView(c);
        }
    }

    /**
     * Condition 3
     * @param view child
     * @return return true if view is in the list, false otherwise
     */
    private boolean isNotShallowClass(@NonNull ViewGroup view) {
        for (Class<? extends ViewGroup> c : notShallowClass) {
            if (c.isInstance(view)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Condition 4
     * @param parent parent view
     * @param child  view whitch needed to be removed
     * @return true if size of the 2 views are the same, false otherwise
     */
    private boolean isSameSize(@NonNull View parent, @NonNull View child) {
        int pw = parent.getMeasuredWidth();
        int ph = parent.getMeasuredHeight();
        int cw = child.getMeasuredWidth();
        int ch = child.getMeasuredHeight();
        if (pw > 0 && ph > 0 && cw > 0 && ch > 0) {
            if (pw == cw && ph == ch)
                return true;
        }
        ViewGroup.LayoutParams params = parent.getLayoutParams();
        if (params != null) {
            pw = params.width;
            ph = params.height;
        } else {
            pw = -3;
            ph = -3;
        }
        params = child.getLayoutParams();
        if (params != null) {
            cw = params.width;
            ch = params.height;
        } else {
            cw = -4;
            ch = -4;
        }
        return pw == cw && ph == ch;
    }

    /**
     * Condition 5
     * @param view  view whitch needed to be removed
     * @return true if view contain event, false otherwise.
     */
    private boolean containEvent(@NonNull View view) {
        if (EventExtractor.getViewOnClickListener(view) != null)
            return true;
        if (EventExtractor.getViewOnLongClickListener(view) != null)
            return true;
        return false;
    }

    /**
     * Condition 6
     * @param view view whitch needed to be removed
     * @return true if view contain background, false otherwise
     */
    private boolean containBackground(@NonNull View view) {
        return view.getBackground() != null;
    }

    /**
     * Condition 7
     * @param view view whitch needed to be removed
     * @return true if the view should not be removed, false otherwise.
     */
    private boolean checkListener(@NonNull View view) {
        if (onShallowViewListener != null)
            return onShallowViewListener.onShallowView(view);
        return false;
    }
}
