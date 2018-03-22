package xfy.fakeview.library.text.block;

import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.text.TextPaint;
import android.view.MotionEvent;
import android.view.View;

import java.util.List;

import xfy.fakeview.library.text.param.ImmutableParams;
import xfy.fakeview.library.text.param.VariableParams;

/**
 * Created by XiongFangyu on 2018/3/2.
 */
public interface IDrawableBlockList<E extends IDrawableBlock> extends List<E> {
    long getFlag();

    int getStart();

    int getEnd();

    int getNewLineCount();

    int getDrawableCount();

    int getSpecialDrawableCount();

    boolean hasSpan();

    int[] getLinesHeight();

    int getFlagSize();

    long measure(TextPaint textPaint, int lineInfo, int drawableSize, int currentLeft, int currentTop, int left, int right);

    void use();

    void notUse();

    void doNotRecycle();

    void canRecycle();

    boolean onTouchEvent(@NonNull View v, MotionEvent event, @NonNull ImmutableParams immutableParams);

    void draw(Canvas canvas, @NonNull VariableParams variableParams, @NonNull ImmutableParams immutableParams);
}
