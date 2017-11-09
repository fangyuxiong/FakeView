package xfy.fakeview.library.translator.data;

import android.view.View;

import xfy.fakeview.library.fview.FView;

/**
 * Created by XiongFangyu on 2017/11/9.
 *
 * Translate data in View into data in FView.
 *
 * Implement this interface and register it by invoke
 * {@link DataTranslatorManager#registerTranslator(Class, IDataTranslator)}
 * to translate your custom View data.
 */
public interface IDataTranslator<F extends FView, R extends View> {
    /**
     * Implement this method to do translate
     * @param fview target FView
     * @param src source View
     * @return true: translated success, false othrewise
     */
    boolean translateData(F fview, R src);

    /**
     * Indicate whether data in the source View can be translated.
     * @param src source View
     * @return  true: can be translated, false otherwise
     */
    boolean checkSrc(View src);
}
