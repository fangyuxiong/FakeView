package xfy.fakeview.library.layermerge;

import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

/**
 * Created by XiongFangyu on 2018/5/24.
 */
public class LayoutData implements ViewTreeObserver.OnPreDrawListener {
    FrameLayout layout;
    int extractInfo = LayersMergeManager.EXTRACT_ALL;
    Object tag;
    int maxFailTimes = 0;
    int maxNotReadyCount = 3;
    int maxZeroLocCountWhenExtracting = 3;
    OnExtractViewGroupListener onExtractViewGroupListener;
    MergeStatusListener mergeStatusListener;

    int failTimes = 0;
    boolean canMerge = false;
    long waitMill = 0;

    public LayoutData(Object tag, FrameLayout layout) {
        this.tag = tag;
        this.layout = layout;
        layout.getViewTreeObserver().addOnPreDrawListener(this);
        checkValid();
    }

    public LayoutData withExtractInfo(int extractInfo) {
        this.extractInfo = extractInfo;
        return this;
    }

    public LayoutData withMaxFailTimes(int maxFailTimes) {
        this.maxFailTimes = maxFailTimes;
        return this;
    }

    public LayoutData withOnExtractViewGroupListener(OnExtractViewGroupListener listener) {
        this.onExtractViewGroupListener = listener;
        return this;
    }

    public LayoutData withMaxNotReadyCount(int maxNotReadyCount) {
        this.maxNotReadyCount = maxNotReadyCount;
        return this;
    }

    public LayoutData withMaxZeroLocCountWhenExtracting(int maxZeroLocCountWhenExtracting) {
        this.maxZeroLocCountWhenExtracting = maxZeroLocCountWhenExtracting;
        return this;
    }

    public LayoutData withMergeFailedListener(MergeStatusListener listener) {
        mergeStatusListener = listener;
        return this;
    }

    public LayoutData withExtractViewGroupListener(OnExtractViewGroupListener listener) {
        onExtractViewGroupListener = listener;
        return this;
    }

    public LayoutData withWaitMillTime(long millTime) {
        this.waitMill = millTime;
        return this;
    }

    public LayoutData canMergeNow() {
        this.canMerge = true;
        removeListener();
        return this;
    }

    public LayoutData checkValid() {
        if (layout == null)
            throw new NullPointerException("layout must not be null!");
        if (tag == null)
            throw new NullPointerException("tag must not be null!");
        if (extractInfo < 0)
            throw new IllegalArgumentException("extract info is invalid!");
        return this;
    }

    @Override
    public String toString() {
        return layout + " info: " + extractInfo + " " + tag;
    }

    @Override
    public boolean onPreDraw() {
        canMerge = true;
        synchronized (this) {
            notifyAll();
        }
        removeListener();
        return false;
    }

    public void removeListener() {
        layout.getViewTreeObserver().removeOnPreDrawListener(this);
    }

    synchronized boolean canMerge() {
        if (canMerge) {
            return true;
        }
        try {
            if (waitMill > 0) {
                wait(waitMill);
            } else {
                wait();
            }
        } catch (Throwable e) {

        }
        return canMerge;
    }
}
