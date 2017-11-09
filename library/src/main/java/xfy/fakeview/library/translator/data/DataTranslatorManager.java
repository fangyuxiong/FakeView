package xfy.fakeview.library.translator.data;

import android.view.View;

import java.util.HashMap;

import xfy.fakeview.library.fview.FView;
import xfy.fakeview.library.fview.normal.FImageView;
import xfy.fakeview.library.fview.normal.FTextView;

/**
 * Created by XiongFangyu on 2017/11/9.
 *
 * Data translater manager.
 *
 * @see IDataTranslator
 */
public class DataTranslatorManager {
    private static final HashMap<Class<? extends FView>, IDataTranslator> dataTranslatorMap;

    static {
        dataTranslatorMap = new HashMap<>();
        dataTranslatorMap.put(FImageView.class, new ImageDataTranslator());
        dataTranslatorMap.put(FTextView.class, new TextDataTranslator());
        dataTranslatorMap.put(FView.class, new ViewDataTranslator());
    }

    public static void registerTranslator(Class<? extends FView> clz, IDataTranslator translator) {
        dataTranslatorMap.put(clz, translator);
    }

    public static boolean translateData(FView fView, View src) {
        IDataTranslator translator = dataTranslatorMap.get(fView.getClass());
        if (translator == null)
            return false;
        if (!translator.checkSrc(src))
            return false;
        return translator.translateData(fView, src);
    }
}
