package xfy.fakeview.library.text.compiler;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import xfy.fakeview.library.text.block.DefaultDrawableBlock;
import xfy.fakeview.library.text.block.DefaultDrawableBlockList;
import xfy.fakeview.library.text.param.SpecialStyleParams;
import xfy.fakeview.library.text.utils.IDrawableStats;

/**
 * Created by XiongFangyu on 2018/3/13.
 */
public class DrawableTextCompiler extends DefaultTextCompiler {

    private static volatile DrawableTextCompiler compiler;

    public static DrawableTextCompiler getCompiler() {
        if (compiler == null) {
            synchronized (DrawableTextCompiler.class) {
                if (compiler == null)
                    compiler = new DrawableTextCompiler();
            }
        }
        return compiler;
    }

    protected char D_START = '[';
    protected char D_END   = ']';
    private ResourceAdapter adapter;

    protected DrawableTextCompiler() {
    }

    public DrawableTextCompiler(ITextCompiler<DefaultDrawableBlockList> innerCompiler) {
        super(innerCompiler);
    }

    public void setResourceAdapter(ResourceAdapter adapter) {
        this.adapter = adapter;
    }

    public ResourceAdapter getResourceAdapter() {
        return adapter;
    }

    @Override
    public void compileInternal(@NonNull DefaultDrawableBlockList list, @NonNull CharSequence text, int start, int end, @Nullable SpecialStyleParams specialStyleParams) {
        if (adapter == null) {
            super.compileInternal(list, text, start, end, specialStyleParams);
            return;
        }
        adapter.beforeCompile();
        compileDrawbleText(list, text, start, end, specialStyleParams);
    }

    @Override
    protected boolean compileSpecialText(DefaultDrawableBlockList list, CharSequence t, @Nullable SpecialStyleParams specialStyleParams) {
        int res = adapter.parseRes(t);
        if (res <= 0) {
            Drawable d = adapter.parseDrawable(t);
            if (d == null) {
//                            compileNewLines(list, parseText);
            } else {
                if (d instanceof IDrawableStats) {
                    list.add(DefaultDrawableBlock.createNeedSetCallbackDrawableBlock(t, d));
                } else {
                    list.add(DefaultDrawableBlock.createSpecialDrawableBlock(t, d));
                }
                return true;
            }
        } else {
            list.add(DefaultDrawableBlock.createDrawableBlock(t, res));
            return true;
        }
        return false;
    }

    private void compileDrawbleText(DefaultDrawableBlockList list, @NonNull CharSequence text, int start, int end, @Nullable SpecialStyleParams specialStyleParams) {
        int index = start;
        boolean haveStart = false;
        int lastStartIndex = -1;
        int lastEndIndex = start;
        while (index < end) {
            final char c = text.charAt(index);
            if (c == D_START) {
                lastStartIndex = index;
                haveStart = true;
            } else if (c == D_END) {
                if (haveStart) {
                    if (lastEndIndex != lastStartIndex) {
                        super.compileInternal(list, text, lastEndIndex, lastStartIndex, specialStyleParams);
                        lastEndIndex = lastStartIndex;
                    }
                    CharSequence parseText = text.subSequence(lastStartIndex, index + 1);
                    if (compileSpecialText(list, parseText, specialStyleParams)) {
                        lastEndIndex = index + 1;
                    }
                } else {
                    //do nothing
                }
                haveStart = false;
            }
            index ++;
        }
        if (lastEndIndex != index) {
            super.compileInternal(list, text, lastEndIndex, index, specialStyleParams);
        }
    }

    public interface ResourceAdapter {
        void beforeCompile();
        int parseRes(@NonNull CharSequence text);
        Drawable parseDrawable(@NonNull CharSequence text);
    }
}
