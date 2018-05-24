package xfy.fakeview.library.layermerge;

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
     * @param layoutData
     * @param failTimes merging failed times
     * @param type failed type
     *
     */
    void onMergeFailed(LayoutData layoutData, int failTimes, int type);

    /**
     * Called when merging success
     * @param layoutData
     */
    void onMergeSuccess(LayoutData layoutData);

    /**
     * Called when merging action really start
     * @param layoutData
     */
    void onMergeStart(LayoutData layoutData);
}
