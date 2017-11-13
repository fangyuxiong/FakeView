package xfy.fakeview.library.layermerge;

import android.view.View;
import android.view.ViewGroup;

/**
 * Created by XiongFangyu on 2017/11/13.
 *
 * Interface definition for a callback to be invoked when extracting a ViewGroup
 */
public interface OnExtractViewGroupListener {
    /**
     * Called when extracting viewgroup
     * @param src target ViewGroup
     * @return result for extracting info, or null.
     *          @see Result
     */
    Result onExtract(ViewGroup src);

    /**
     * Class for result
     */
    public class Result {
        /**
         * Views whitch need to be added to the new view tree.
         */
        public View[] views;
        /**
         * Views locations
         * the length must be the same of views
         */
        public LayersMergeManager.Loc[] locs;
        /**
         * True if the listener has consumed the event, false otherwise.
         */
        public boolean handle = false;

        boolean valid() {
            if (views == null && locs == null)
                return true;
            if ((views == null && locs != null)
                 || (views != null && locs == null))
                return false;
            if (views.length != locs.length)
                return false;
            return true;
        }

        @Override
        public String toString() {
            return String.format("views length: %d\nlocs length: %d\nhandle: %b",
                    views != null ? views.length : 0,
                    locs != null ? locs.length : 0,
                    handle);
        }
    }
}
