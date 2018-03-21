package xfy.fakeview.library.text.block;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.text.TextPaint;
import android.view.MotionEvent;
import android.view.View;

import xfy.fakeview.library.text.param.ImmutableParams;
import xfy.fakeview.library.text.param.VariableParams;

/**
 * Created by XiongFangyu on 2018/3/2.
 */
public interface IDrawableBlock<T extends IDrawableBlockList> {
    int TEXT = 0;
    int DRAWABLE = 1;
    int SPECIAL_DRAWABLE = 2;
    int SPAN = 3;
    int NEXTLINE = 4;
    long getFlag();

    int getType();

    int getBaseLine();

    CharSequence getText();

    int getDrawableRes();

    Drawable getSpecialDrawable();

    T getChildren();

    long measure(TextPaint textPaint, int baseLine, int drawableSize, int currentLeft, int currentTop, int left, int right);

    boolean draw(Canvas canvas, @NonNull VariableParams variableParams, @NonNull ImmutableParams immutableParams);

    boolean onTouchEvent(@NonNull View v, MotionEvent event, @NonNull ImmutableParams immutableParams);
}
