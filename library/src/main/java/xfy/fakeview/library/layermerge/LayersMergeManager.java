package xfy.fakeview.library.layermerge;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.Arrays;

import xfy.fakeview.library.DebugInfo;
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
    /**
     * Extracting background drawable from a view
     */
    private static ChildViewGroupBackgroundExtractor childViewGroupBackgroundExtractor;

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

    private OnExtractViewGroupListener onExtractViewGroupListener;

    private int zeroLocCount = 0;
    private int maxZeroLocCount = 3;

    public static void setChildViewGroupBackgroundExtractor(ChildViewGroupBackgroundExtractor childViewGroupBackgroundExtractor) {
        LayersMergeManager.childViewGroupBackgroundExtractor = childViewGroupBackgroundExtractor;
    }

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
     * Indicating the view is layout.
     * @param src view tree parent
     * @return true: ready, false otherwise
     */
    public static boolean isReadyToMerge(ViewGroup src) {
        return isReadyToMerge(src, 0, 3);
    }

    /**
     * Indicating the view is layout.
     * @param src view tree parent
     * @param zeroViewCount input 0.
     * @param notReadyCount min fail count
     * @return true: ready, false otherwise
     */
    public static boolean isReadyToMerge(ViewGroup src, int zeroViewCount, int notReadyCount) {
        final int childCount = src.getChildCount();
        for (int i = 0; i < childCount; i ++) {
            if (zeroViewCount >= notReadyCount)
                return false;
            View child = src.getChildAt(i);
            if (isZeroLocFromParent(child)) {
                zeroViewCount ++;
            }
            if (DebugInfo.DEBUG) {
                Log.d(LayersMergeManager.class.getSimpleName(),
                        String.format("isReadyToMerge(%s, %d, %d) child: %s",
                                    src.getClass().getSimpleName(), zeroViewCount, notReadyCount,
                                    child.getClass().getSimpleName()));
            }
            if (child instanceof ViewGroup && !isReadyToMerge((ViewGroup) child, zeroViewCount, notReadyCount)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isZeroLocFromParent(View v) {
        return v.getLeft() == 0 && v.getTop() == 0;
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
        this(parent, flag, null);
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
     * @param listener callback invoked when extracting a ViewGroup
     */
    public LayersMergeManager(FrameLayout parent, int flag, OnExtractViewGroupListener listener) {
        this(parent, flag, 3, listener);
    }

    /**
     * Constructor for this manager
     *
     * @param parent view tree parent.
     *               If parent is other ViewGroup(eg. LinearLayout),
     *               create a new FrameLayout wrapping that parent, and pass the
     *               FrameLayout.
     * @param flag flag for extracting info
     * @param maxZeroLocCount max count
     * @param listener callback invoked when extracting a ViewGroup
     */
    public LayersMergeManager(FrameLayout parent, int flag, int maxZeroLocCount, OnExtractViewGroupListener listener) {
        this.extractFlag = flag;
        rootLayout = parent;
        rootLoc = new Loc(getViewLocation(parent));
        rootWidth = parent.getWidth();
        rootHeight = parent.getHeight();
        childrens = new ArrayList<>();
        childrenLoc = new ArrayList<>();
        this.maxZeroLocCount = maxZeroLocCount;
        onExtractViewGroupListener = listener;
    }

    /**
     * Start merge layers
     * When done, this object is useless.
     * @return true if merge success, false otherwise
     */
    public boolean mergeChildrenLayers() {
        boolean result = extractViewFromParent(rootLayout);
        addChildrenByLoc();
        rootLayout = null;
        rootLoc = null;
        return result;
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
            if (child.getParent() != null)
                continue;
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
     * @return true if extract success, false otherwise
     */
    private boolean extractViewFromParent(ViewGroup parent) {
        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i ++) {
            View c = parent.getChildAt(i);
            if (c instanceof ViewGroup) {
                if (onExtractViewGroupListener != null) {
                    OnExtractViewGroupListener.Result result = onExtractViewGroupListener.onExtract((ViewGroup) c);
                    if (result != null) {
                        if (!result.valid())
                            throw new IllegalArgumentException("invalid result: " + result);
                        final View[] views = result.views;
                        final Loc[] locs = result.locs;
                        for (int j = 0, vl = views.length; j < vl; j ++) {
                            childrens.add(views[j]);
                            childrenLoc.add(locs[j]);
                        }
                        if (result.handle)
                            continue;
                    }
                }
                if (isZeroLocFromParent(c)) {
                    zeroLocCount ++;
                }
                if (DebugInfo.DEBUG) {
                    Log.d(LayersMergeManager.class.getSimpleName(), String.format("zero loc count: %d, max: %d", zeroLocCount, maxZeroLocCount));
                }
                if (zeroLocCount >= maxZeroLocCount)
                    return false;
                int[] loc = getViewLocation(c);
                if (DebugInfo.DEBUG) {
                    logViewAndLoc(c, loc);
                }
                View backgroundHolder = createViewByExtractingFlag((ViewGroup) c);
                if (backgroundHolder != null) {
                    childrens.add(backgroundHolder);
                    childrenLoc.add(new Loc(loc));
                }
                if (!extractViewFromParent((ViewGroup) c))
                    return false;
            } else {
                if (isZeroLocFromParent(c)) {
                    zeroLocCount ++;
                }
                if (DebugInfo.DEBUG) {
                    Log.d(LayersMergeManager.class.getSimpleName(), String.format("zero loc count: %d, max: %d", zeroLocCount, maxZeroLocCount));
                }
                if (zeroLocCount >= maxZeroLocCount)
                    return false;
                int[] loc = getViewLocation(c);
                if (DebugInfo.DEBUG) {
                    logViewAndLoc(c, loc);
                }
                childrens.add(c);
                childrenLoc.add(new Loc(loc));
            }
        }
        parent.removeAllViews();
        return true;
    }

    private void logViewAndLoc(View v, int[] loc) {
        Log.d(LayersMergeManager.class.getSimpleName(), String.format("view loc: %s, view: %s", Arrays.toString(loc), v.toString()));
    }

    private boolean isZeroLoc(int[] loc) {
        return loc[0] == 0 && loc[1] == 0;
    }

    private View createViewByExtractingFlag(ViewGroup src) {
        final int flag = extractFlag;
        if (flag == EXTRACT_NONE)
            return null;
        View result = null;
        if ((flag & EXTRACT_BACKGROUND) == EXTRACT_BACKGROUND) {
            Drawable background = null;
            if (childViewGroupBackgroundExtractor != null) {
                background = childViewGroupBackgroundExtractor.extractFrom(src);
            } else {
                background = src.getBackground();
            }
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
    public static class Loc {
        int left, top;

        public Loc(int[] loc) {
            left = loc[0];
            top = loc[1];
        }
    }
}
