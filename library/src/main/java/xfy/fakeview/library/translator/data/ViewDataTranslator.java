package xfy.fakeview.library.translator.data;

import android.view.View;

import xfy.fakeview.library.fview.FView;

/**
 * Created by XiongFangyu on 2017/11/9.
 *
 * Translate Data in View
 */
public class ViewDataTranslator implements IDataTranslator<FView, View> {
    @Override
    public boolean translateData(FView fview, View src) {
        return true;
    }

    @Override
    public boolean checkSrc(View src) {
        return src.getClass().equals(View.class);
    }
}
