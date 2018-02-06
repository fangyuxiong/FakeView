package xfy.fakeview.library.layermerge;

import android.graphics.drawable.Drawable;
import android.view.ViewGroup;

/**
 * Created by XiongFangyu on 2018/2/6.
 *
 * Extractor
 */
public interface ChildViewGroupBackgroundExtractor {
    /**
     * Extract background drawable from a ViewGroup
     * @param child child view
     * @return null for not create holder for the view
     */
    Drawable extractFrom(ViewGroup child);
}
