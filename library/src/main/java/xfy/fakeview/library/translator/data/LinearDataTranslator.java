package xfy.fakeview.library.translator.data;

import android.view.View;
import android.widget.LinearLayout;

import xfy.fakeview.library.fview.normal.FLinearLayout;

/**
 * Created by XiongFangyu on 2017/12/12.
 *
 * Translate data in linear layout
 */
public class LinearDataTranslator implements IDataTranslator<FLinearLayout, LinearLayout> {
    @Override
    public boolean translateData(FLinearLayout fview, LinearLayout src) {
        fview.setOrientation(src.getOrientation());
        return true;
    }

    @Override
    public boolean checkSrc(View src) {
        return src.getClass().equals(LinearLayout.class);
    }
}
