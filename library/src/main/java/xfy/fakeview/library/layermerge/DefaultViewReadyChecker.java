package xfy.fakeview.library.layermerge;

import android.support.annotation.NonNull;
import android.view.View;

/**
 * Created by XiongFangyu on 2018/2/7.
 *
 * A default checker
 */
public class DefaultViewReadyChecker implements ViewReadyChecker {
    @Override
    public boolean check(@NonNull View v) {
        if (v.getLeft() == 0 && v.getTop() == 0 && v.getWidth() == 0 && v.getHeight() == 0)
            return false;
        return true;
    }
}
