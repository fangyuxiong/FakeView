package xfy.fakeview.library.layermerge;

import android.support.annotation.NonNull;
import android.view.View;

/**
 * Created by XiongFangyu on 2018/2/7.
 *
 * Interface indicated for checking whether view is ready
 */
public interface ViewReadyChecker {
    /**
     * Check view status
     * @param v view
     * @return true for view that is ready to merge, false otherwise
     */
    boolean check(@NonNull View v);
}
