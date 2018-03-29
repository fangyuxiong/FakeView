package xfy.fakeview.library.text.utils;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

/**
 * Created by XiongFangyu on 2018/3/29.
 */
public interface CallbackObserver {
    void onCallbackSet(Drawable.Callback callback);

    @NonNull Drawable.ConstantState getConstantState();
}
