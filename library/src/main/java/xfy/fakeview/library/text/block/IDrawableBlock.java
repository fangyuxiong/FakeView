package xfy.fakeview.library.text.block;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.view.View;

import xfy.fakeview.library.text.param.ClickSpanBlockInfo;
import xfy.fakeview.library.text.param.ImmutableParams;

/**
 * Created by XiongFangyu on 2018/3/2.
 */
public interface IDrawableBlock<T extends IDrawableBlockList> extends IBlock {
    int TEXT = 0;
    int DRAWABLE = 1;
    int SPECIAL_DRAWABLE = 2;
    int SPAN = 3;
    int NEXTLINE = 4;
    int NEED_SET_CALLBACK_DRAWABLE = 5;

    int getType();

    int getBaseLine();

    CharSequence getText();

    int getDrawableRes();

    Drawable getSpecialDrawable();

    T getChildren();

    boolean onTouchEvent(@NonNull View v, MotionEvent event, @NonNull ImmutableParams immutableParams, @NonNull ClickSpanBlockInfo blockInfo);
}
