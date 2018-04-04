package xfy.fakeview.library.text.utils;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

/**
 * Created by XiongFangyu on 2018/3/29.
 *
 * Interface implement by special Drawable
 */
public interface IDrawableStats {
    void onCallbackSet(Drawable.Callback callback);

    void setCountInText(int c);

    /**
     * if return true, the drawable must override {@link Drawable#getConstantState()} method and return a
     * {@link Drawable.ConstantState} object, whitch must override {@link Drawable.ConstantState#newDrawable()}
     * and return a nonnull drawable.
     *
     * @see Drawable#getConstantState()
     * @see Drawable.ConstantState
     * @see Drawable.ConstantState#newDrawable()
     * @return true to create a new Drawable, false otherwise
     */
    boolean needCreateNewDrawable();

    void recycle();

    @NonNull Drawable.ConstantState getConstantState();
}
