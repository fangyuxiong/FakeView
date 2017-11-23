package xfy.fakeview.library.layermerge;

import android.widget.FrameLayout;

/**
 * Created by XiongFangyu on 2017/11/23.
 *
 * Interface definition for a callback to be invoked when merging failed
 */
public interface OnMergeFailedListener {
    /**
     * Call when merging failed
     * @param layout layout whitch is failed to merge
     * @param tag
     * @param extractInfo custom extracing info
     * @param failTimes merging failed times
     */
    void onMergeFailed(FrameLayout layout, Object tag, int extractInfo, int failTimes);
}
