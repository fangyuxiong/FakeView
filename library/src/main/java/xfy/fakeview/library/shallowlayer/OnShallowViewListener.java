package xfy.fakeview.library.shallowlayer;

import android.support.annotation.NonNull;
import android.view.View;

/**
 * Created by XiongFangyu on 2018/1/10.
 *
 * Interface definition for a callback to be invoked when shallow a view
 */
public interface OnShallowViewListener {
    /**
     * Called when shallow view
     * @param view witch will be removed from view tree
     * @return true if the view should not be removed, false otherwise.
     */
    boolean onShallowView(@NonNull View view);
}
