package xfy.fakeview.library.translator;

import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;

import xfy.fakeview.library.fview.FView;
import xfy.fakeview.library.fview.normal.FFrameLayout;
import xfy.fakeview.library.fview.normal.FImageView;
import xfy.fakeview.library.fview.normal.FLinearLayout;
import xfy.fakeview.library.fview.normal.FTextView;

/**
 * Created by XiongFangyu on 2017/11/8.
 *
 * Translate View into FView
 * If your custom View need to be translated,
 * the method {@link #registerTranslator(Class, Class)} must be invoked before.
 */
public class FViewTranslator {

    private static final HashMap<Class<? extends View>, Class<? extends FView>> TRANS;

    static {
        TRANS = new HashMap<>();
        TRANS.put(View.class, FView.class);
        TRANS.put(FrameLayout.class, FFrameLayout.class);
        TRANS.put(LinearLayout.class, FLinearLayout.class);
        TRANS.put(ImageView.class, FImageView.class);
        TRANS.put(ImageButton.class, FImageView.class);
        TRANS.put(TextView.class, FTextView.class);
        TRANS.put(Button.class, FTextView.class);
        TRANS.put(AppCompatImageView.class, FImageView.class);
        TRANS.put(AppCompatTextView.class, FTextView.class);
        TRANS.put(AppCompatButton.class, FTextView.class);
        TRANS.put(AppCompatImageButton.class, FImageView.class);
    }

    /**
     * Register the class of View can be translated into
     * other special class of FView
     * @param target the target class need to be translated
     * @param trans the translated class
     */
    public static void registerTranslator(Class<? extends View> target, Class<? extends FView> trans) {
        TRANS.put(target, trans);
    }

    public static Class<? extends FView> getTranslateClass(Class<? extends View> target) {
        return TRANS.get(target);
    }

    public static boolean canBeTranslated(Class<? extends View> target) {
        return TRANS.containsKey(target);
    }
}
