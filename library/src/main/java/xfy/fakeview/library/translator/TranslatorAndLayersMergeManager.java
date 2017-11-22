package xfy.fakeview.library.translator;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import xfy.fakeview.library.fview.IFViewRoot;
import xfy.fakeview.library.layermerge.LayersMergeManager;

/**
 * Created by XiongFangyu on 2017/11/21.
 */

public class TranslatorAndLayersMergeManager extends TranslatorManager {

    public TranslatorAndLayersMergeManager(Context context) {
        super(context);
    }

    @Override
    public IFViewRoot translateView(ViewGroup target) {
        if (target instanceof FrameLayout) {
            merge((FrameLayout) target);
        }
        return super.translateView(target);
    }

    private boolean merge(FrameLayout layout) {
        if (LayersMergeManager.needMerge(layout) && LayersMergeManager.isReadyToMerge(layout)) {
            LayersMergeManager manager = new LayersMergeManager(layout, LayersMergeManager.EXTRACT_ALL);
            manager.mergeChildrenLayers();
            return true;
        }
        return false;
    }
}
