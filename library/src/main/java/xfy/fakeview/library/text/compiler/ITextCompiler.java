package xfy.fakeview.library.text.compiler;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import xfy.fakeview.library.text.block.IDrawableBlockList;
import xfy.fakeview.library.text.param.SpecialStyleParams;

/**
 * Created by XiongFangyu on 2018/3/2.
 */
public interface ITextCompiler<T extends IDrawableBlockList> {
    char NEW_LINE_CHAR = '\n';

    void setInnerCompiler(@Nullable ITextCompiler<T> compiler);

    T compile(@NonNull CharSequence charSequence);

    T compile(@NonNull CharSequence text, int start, int end);

    /**
     * invoke by compiler
     * @param list
     * @param text
     * @param start
     * @param end
     * @param specialStyleParams
     */
    void compileInternal(@NonNull T list, @NonNull CharSequence text, int start, int end, @Nullable SpecialStyleParams specialStyleParams);
}
