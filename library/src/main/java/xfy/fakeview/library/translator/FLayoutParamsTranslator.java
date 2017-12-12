package xfy.fakeview.library.translator;

import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.util.HashMap;

import xfy.fakeview.library.fview.IFViewGroup;
import xfy.fakeview.library.fview.normal.FFrameLayout;
import xfy.fakeview.library.fview.normal.FLinearLayout;

/**
 * Created by XiongFangyu on 2017/11/8.
 *
 * Translate LayoutParams into FLayoutParams
 *
 * If your custom LayoutParams need to be translated,
 * the method {@link #registerTranslator(Class, Class)} must be invoked before.
 */
public class FLayoutParamsTranslator {
    private static final HashMap<Class<? extends ViewGroup.LayoutParams>, Class<? extends IFViewGroup.FLayoutParams>> TRANS;

    static {
        TRANS = new HashMap<>();
        TRANS.put(ViewGroup.LayoutParams.class, IFViewGroup.FLayoutParams.class);
        TRANS.put(ViewGroup.MarginLayoutParams.class, IFViewGroup.FLayoutParams.class);
        TRANS.put(FrameLayout.LayoutParams.class, FFrameLayout.LayoutParams.class);
        TRANS.put(LinearLayout.LayoutParams.class, FLinearLayout.LayoutParams.class);
    }

    /**
     * Register the class of LayoutParams whitch can be translated into
     * other special class of FLayoutParams
     * @param target the target class need to be translated
     * @param tran the translated class
     */
    public static void registerTranslator(Class<? extends ViewGroup.LayoutParams> target,
                                          Class<? extends IFViewGroup.FLayoutParams> tran) {
        TRANS.put(target, tran);
    }

    public static Class<? extends IFViewGroup.FLayoutParams> getTranslateClass(Class<? extends ViewGroup.LayoutParams> target) {
        return TRANS.get(target);
    }
}
