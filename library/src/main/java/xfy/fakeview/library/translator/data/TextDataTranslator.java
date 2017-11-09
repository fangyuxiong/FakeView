package xfy.fakeview.library.translator.data;

import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import xfy.fakeview.library.fview.normal.FTextView;

/**
 * Created by XiongFangyu on 2017/11/9.
 *
 * Translate data in TextView
 */
public class TextDataTranslator implements IDataTranslator<FTextView, TextView> {
    @Override
    public boolean translateData(FTextView fview, TextView src) {
        fview.setTextColor(src.getCurrentTextColor());
        fview.setTextSize(TypedValue.COMPLEX_UNIT_PX, src.getTextSize());
        fview.setText(src.getText());
        return true;
    }

    @Override
    public boolean checkSrc(View src) {
        Class clz = src.getClass();
        return clz.equals(TextView.class)
                || clz.equals(Button.class)
                || clz.equals(AppCompatTextView.class)
                || clz.equals(AppCompatButton.class);
    }
}
