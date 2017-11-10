package xfy.fakeview.library.layermerge;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.ArrayList;

/**
 * Created by XiongFangyu on 2017/11/10.
 *
 * Merge Layers Tools
 *
 * To merge layers, add code:
 * <code>
 *     //check if view tree need merge layers
 *     if (!LayersMergeManager.needMerge(parent))
 *          return;
 *     //all views must be layout before
 *     LayersMergeManager manager = LayersMergeManager(parent);
 *     manager.mergeChildrenLayers();
 *     //done
 * </code>
 */
public class LayersMergeManager {
    private FrameLayout rootLayout;
    private Loc rootLoc;
    private int rootWidth, rootHeight;

    private ArrayList<View> childrens;
    private ArrayList<Loc> childrenLoc;

    private boolean extractBackgroundView = false;

    /**
     * Indicate the view tree need merge
     * @param parent view tree parent
     * @return true: need merge, false otherwise
     */
    public static boolean needMerge(ViewGroup parent) {
        int childCount = parent.getChildCount();
        for (int i = 0 ; i < childCount; i ++) {
            if (parent.getChildAt(i) instanceof ViewGroup)
                return true;
        }
        return false;
    }

    /**
     * Constructor for this manager
     *
     * @param parent view tree parent.
     *               If parent is other ViewGroup(eg. LinearLayout),
     *               create a new FrameLayout wrapping that parent, and pass the
     *               FrameLayout.
     */
    public LayersMergeManager(FrameLayout parent) {
        this(parent, false);
    }

    /**
     * Constructor for this manager
     *
     * @param parent view tree parent.
     *               If parent is other ViewGroup(eg. LinearLayout),
     *               create a new FrameLayout wrapping that parent, and pass the
     *               FrameLayout.
     *
     * @param createBackgroundView when a viewgroup has background, create a new view for it.
     */
    public LayersMergeManager(FrameLayout parent, boolean createBackgroundView) {
        this.extractBackgroundView = createBackgroundView;
        rootLayout = parent;
        rootLoc = new Loc(getViewLocation(parent));
        rootWidth = parent.getWidth();
        rootHeight = parent.getHeight();
        childrens = new ArrayList<>();
        childrenLoc = new ArrayList<>();
    }

    /**
     * Start merge layers
     * When done, this object is useless.
     */
    public void mergeChildrenLayers() {
        extractViewFromParent(rootLayout);
        addChildrenByLoc();
        rootLayout = null;
        rootLoc = null;
    }

    /**
     * Add all children(View) into rootLayout with correct LayoutParams
     */
    private void addChildrenByLoc() {
        int childCount = childrens.size();
        int pl = rootLayout.getPaddingLeft();
        int pt = rootLayout.getPaddingTop();
        for (int i = 0 ; i < childCount ;i ++ ) {
            View child = childrens.get(i);
            Loc childLoc = childrenLoc.get(i);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(child.getLayoutParams());
            params.setMargins(childLoc.left - rootLoc.left - pl,
                    childLoc.top - rootLoc.top - pt,0, 0);
            rootLayout.addView(child, params);
        }
        ViewGroup.LayoutParams params = rootLayout.getLayoutParams();
        params.width = rootWidth;
        params.height = rootHeight;
        rootLayout.setLayoutParams(params);
        childrens.clear();
        childrenLoc.clear();
    }

    /**
     * Extracting all view(not ViewGroup) and saving in array.
     * @param parent extract children in parent
     */
    private void extractViewFromParent(ViewGroup parent) {
        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i ++) {
            View c = parent.getChildAt(i);
            if (c instanceof ViewGroup) {
                View backgroundHolder = createViewForBackground((ViewGroup) c);
                if (backgroundHolder != null) {
                    childrens.add(backgroundHolder);
                    childrenLoc.add(new Loc(getViewLocation(c)));
                }
                extractViewFromParent((ViewGroup) c);
            } else {
                childrens.add(c);
                childrenLoc.add(new Loc(getViewLocation(c)));
            }
        }
        parent.removeAllViews();
    }

    /**
     * If extractBackgroundView is true, and view contain a background,
     * create a new View for holding the background.
     * @param src src view
     * @return view holding background or src; or null
     */
    private View createViewForBackground(ViewGroup src) {
        if (extractBackgroundView) {
            Drawable background = src.getBackground();
            if (background == null)
                return null;
            View backgroundHolder = new View(src.getContext());
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(src.getWidth(), src.getHeight());
            backgroundHolder.setLayoutParams(params);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                backgroundHolder.setBackground(background);
            } else {
                backgroundHolder.setBackgroundDrawable(background);
            }
            return backgroundHolder;
        }
        return null;
    }

    private static int[] getViewLocation(View view) {
        int[] loc = new int[2];
        view.getLocationInWindow(loc);
        return loc;
    }

    /**
     * Save view location
     */
    static class Loc {
        int left, top;
        Loc() {}

        Loc(int[] loc) {
            left = loc[0];
            top = loc[1];
        }
    }
}
