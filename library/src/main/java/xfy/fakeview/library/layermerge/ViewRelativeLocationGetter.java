package xfy.fakeview.library.layermerge;

import android.view.View;
import android.view.ViewGroup;

/**
 * Created by XiongFangyu on 2018/2/7.
 *
 * Interface indicated for getting view location.
 */
public interface ViewRelativeLocationGetter {
    /**
     * Get view location relate by root
     * @param view the view that need get loc
     * @param root the root that relate to
     * @return relateive location. length 2
     */
    int[] getViewRelativeLocation(View view, ViewGroup root);
}
