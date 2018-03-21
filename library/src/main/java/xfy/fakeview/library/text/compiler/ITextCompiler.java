package xfy.fakeview.library.text.compiler;

import android.support.annotation.NonNull;

import xfy.fakeview.library.text.block.IDrawableBlockList;

/**
 * Created by XiongFangyu on 2018/3/2.
 */
public interface ITextCompiler<T extends IDrawableBlockList> {
    char NEW_LINE_CHAR = '\n';

    T compile(@NonNull CharSequence charSequence);

    T compile(@NonNull CharSequence text, int start, int end);
}
