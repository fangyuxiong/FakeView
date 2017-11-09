package xfy.fakeview.library.translator.data;

import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.AppCompatImageView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import xfy.fakeview.library.fview.normal.FImageView;

/**
 * Created by XiongFangyu on 2017/11/9.
 *
 * Translate data in ImageView.
 */
public class ImageDataTranslator implements IDataTranslator<FImageView, ImageView> {
    @Override
    public boolean translateData(FImageView fview, ImageView src) {
        fview.setImageDrawable(src.getDrawable());
        return true;
    }

    @Override
    public boolean checkSrc(View src) {
        Class clz = src.getClass();
        return clz.equals(ImageView.class)
                || clz.equals(ImageButton.class)
                || clz.equals(AppCompatImageView.class)
                || clz.equals(AppCompatImageButton.class);
    }
}
