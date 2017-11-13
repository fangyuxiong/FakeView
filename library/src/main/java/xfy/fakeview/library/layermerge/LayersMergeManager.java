package xfy.fakeview.library.layermerge;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.ArrayList;

import xfy.fakeview.library.translator.EventExtractor;

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
    /**
     * Only extracting children view
     */
    public static final int EXTRACT_NONE                = 0;
    /**
     * Extracting ViewGroup contain background.
     */
    public static final int EXTRACT_BACKGROUND          = 0x00000001;
    /**
     * Extracting ViewGroup contain click event.
     */
    public static final int EXTRACT_CLICK_EVENT         = 0x00000010;
    /**
     * Extracting ViewGroup contain long click event.
     */
    public static final int EXTRACT_LONG_CLICK_EVENT    = 0x00000100;
    /**
     * Extracting ViewGroup contain click event or long click event
     */
    public static final int EXTRACT_ALL_EVENT           = EXTRACT_CLICK_EVENT | EXTRACT_LONG_CLICK_EVENT;
    /**
     * Extracting ViewGroup contain background or event
     */
    public static final int EXTRACT_ALL                 = EXTRACT_BACKGROUND | EXTRACT_ALL_EVENT;

    private FrameLayout rootLayout;
    private Loc rootLoc;
    private int rootWidth, rootHeight;

    private ArrayList<View> childrens;
    private ArrayList<Loc> childrenLoc;
    /**
     * Flag for extracting info.
     *
     * @see #EXTRACT_NONE
     * @see #EXTRACT_BACKGROUND
     * @see #EXTRACT_CLICK_EVENT
     * @see #EXTRACT_LONG_CLICK_EVENT
     */
    private int extractFlag = EXTRACT_NONE;

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
        this(parent, EXTRACT_NONE);
    }

    /**
     * Constructor for this manager
     *
     * @param parent view tree parent.
     *               If parent is other ViewGroup(eg. LinearLayout),
     *               create a new FrameLayout wrapping that parent, and pass the
     *               FrameLayout.
     *
     * @param flag flag for extracting info
     */
    public LayersMergeManager(FrameLayout parent, int flag) {
        this.extractFlag = flag;
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
                View backgroundHolder = createViewByExtractingFlag((ViewGroup) c);
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

    private View createViewByExtractingFlag(ViewGroup src) {
        final int flag = extractFlag;
        if (flag == EXTRACT_NONE)
            return null;
        View result = null;
        if ((flag & EXTRACT_BACKGROUND) == EXTRACT_BACKGROUND) {
            Drawable background = src.getBackground();
            if (background != null) {
                result = createHolderViewForViewGroup(src);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    result.setBackground(background);
                } else {
                    result.setBackgroundDrawable(background);
                }
            }
        }
        if ((flag & EXTRACT_CLICK_EVENT) == EXTRACT_CLICK_EVENT) {
            View.OnClickListener clickListener = EventExtractor.getViewOnClickListener(src);
            if (clickListener != null) {
                result = result == null ? createHolderViewForViewGroup(src) : result;
                result.setOnClickListener(clickListener);
            }
        }
        if ((flag & EXTRACT_LONG_CLICK_EVENT) == EXTRACT_LONG_CLICK_EVENT) {
            View.OnLongClickListener longClickListener = EventExtractor.getViewOnLongClickListener(src);
            if (longClickListener != null) {
                result = result == null ? createHolderViewForViewGroup(src) : result;
                result.setOnLongClickListener(longClickListener);
            }
        }
        return result;
    }

    private View createHolderViewForViewGroup(ViewGroup src) {
        View holder = new View(src.getContext());
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(src.getWidth(), src.getHeight());
        holder.setLayoutParams(params);
        holder.setId(src.getId());
        return holder;
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
