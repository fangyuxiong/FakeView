package xfy.fakeview.library.text.compiler;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

import xfy.fakeview.library.text.block.DefaultDrawableBlock;
import xfy.fakeview.library.text.block.DefaultDrawableBlockList;

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

    public void setResourceAdapter(ResourceAdapter adapter) {
        this.adapter = adapter;
    }

    public ResourceAdapter getResourceAdapter() {
        return adapter;
    }

    @Override
    protected DefaultDrawableBlockList realCompile(@NonNull CharSequence text, int start, int end) {
        if (adapter == null)
            return super.realCompile(text, start, end);
        final DefaultDrawableBlockList list = DefaultDrawableBlockList.obtain(start, end);
        compileDrawbleText(list, text, start, end);
        return list;
    }

    protected void compileDrawbleText(DefaultDrawableBlockList list, @NonNull CharSequence text, int start, int end) {
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
                        compileNewLines(list, text.subSequence(lastEndIndex, lastStartIndex));
                    }
                    CharSequence parseText = text.subSequence(lastStartIndex, index + 1);
                    int res = adapter.parseRes(parseText);
                    if (res <= 0) {
                        Drawable d = adapter.parseDrawable(parseText);
                        if (d == null) {
                            compileNewLines(list, parseText);
                        } else {
                            list.add(DefaultDrawableBlock.createSpecialDrawableBlock(parseText, d));
                        }
                    } else {
                        list.add(DefaultDrawableBlock.createDrawableBlock(parseText, res));
                    }
                    lastEndIndex = index + 1;
                } else {
                    //do nothing
                }
                haveStart = false;
            }
            index ++;
        }
        if (lastEndIndex != index) {
            compileNewLines(list, text.subSequence(lastEndIndex, index));
        }
    }

    public interface ResourceAdapter {
        int parseRes(@NonNull CharSequence text);
        Drawable parseDrawable(@NonNull CharSequence text);
    }
}
