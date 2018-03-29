package xfy.fakeview.library.text.block;

import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.view.View;

import java.util.List;

import xfy.fakeview.library.text.param.ImmutableParams;

/**
 * Created by XiongFangyu on 2018/3/2.
 */
public interface IDrawableBlockList<E extends IDrawableBlock> extends List<E>, IBlock {

    int getStart();

    int getEnd();

//    int getNewLineCount();

//    int getDrawableCount();

//    int getSpecialDrawableCount();

    boolean hasSpan();

    int[] getLinesHeight();

    int getLineHeightSize();

    void use();

    void notUse();

    void doNotRecycle();

    void canRecycle();

    boolean onTouchEvent(@NonNull View v, MotionEvent event, @NonNull ImmutableParams immutableParams);
}
