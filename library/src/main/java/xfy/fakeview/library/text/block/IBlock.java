package xfy.fakeview.library.text.block;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

import xfy.fakeview.library.text.param.ImmutableParams;
import xfy.fakeview.library.text.param.VariableParams;

/**
 * Created by XiongFangyu on 2018/3/29.
 */

public interface IBlock {
    long getFlag();

    long measure(BlockMeasureParams measureParams, @NonNull ImmutableParams immutableParams);

    boolean draw(Canvas canvas, @NonNull VariableParams variableParams, @NonNull ImmutableParams immutableParams);

    void addCallback(Drawable.Callback callback);

    void removeCallback(Drawable.Callback callback);
}
