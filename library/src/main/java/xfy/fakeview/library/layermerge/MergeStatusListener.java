package xfy.fakeview.library.layermerge;

import android.widget.FrameLayout;

/**
 * Created by XiongFangyu on 2017/11/23.
 *
 * Interface definition for a callback to be invoked when merging failed
 */
public interface MergeStatusListener {
    int FAILED_TYPE_NOT_READY = -1;
    int FAILED_TYPE_TOO_MANY_ZERO_VIEWS = -2;
    /**
     * Call when merging failed
     * @param layout layout whitch is failed to merge
     * @param tag
     * @param extractInfo custom extracing info
     * @param failTimes merging failed times
     * @param type failed type
     *
     */
    void onMergeFailed(FrameLayout layout, Object tag, int extractInfo, int failTimes, int type);

    /**
     * Called when merging success
     * @param layout layout whitch is successfully merged
     * @param tag
     */
    void onMergeSuccess(FrameLayout layout, Object tag);

    /**
     * Called when merging action really start
     * @param layout layout whitch is successfully merged
     * @param tag
     */
    void onMergeStart(FrameLayout layout, Object tag);
}
