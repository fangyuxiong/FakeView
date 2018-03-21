package xfy.fakeview.library.text.compiler;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.LruCache;

import xfy.fakeview.library.text.block.DefaultDrawableBlock;
import xfy.fakeview.library.text.block.DefaultDrawableBlockList;
import xfy.fakeview.library.text.param.SpecialStyleParams;

/**
 * Created by XiongFangyu on 2018/3/8.
 */
public class DefaultTextCompiler implements ITextCompiler<DefaultDrawableBlockList> {

    private static volatile DefaultTextCompiler compiler;

    public static DefaultTextCompiler getCompiler() {
        if (compiler == null) {
            synchronized (DefaultTextCompiler.class) {
                if (compiler == null) {
                    compiler = new DefaultTextCompiler();
                }
            }
        }
        return compiler;
    }

    private final LruCache<CharSequence, DefaultDrawableBlockList> cache;

    protected DefaultTextCompiler() {
        cache = new LruCache<CharSequence, DefaultDrawableBlockList>(cacheSize()) {
            @Override
            protected void entryRemoved(boolean evicted, CharSequence key, DefaultDrawableBlockList oldValue, DefaultDrawableBlockList newValue) {
                if (oldValue != null) {
                    oldValue.canRecycle();
                }
                if (newValue != null) {
                    newValue.doNotRecycle();
                }
            }
        };
    }

    protected int cacheSize() {
        return 30;
    }

    @Override
    public DefaultDrawableBlockList compile(@NonNull CharSequence charSequence) {
        return compile(charSequence, 0, charSequence.length());
    }

    @Override
    public DefaultDrawableBlockList compile(@NonNull CharSequence text, int start, int end) {
        if (start < 0 || start >= text.length()) {
            throw new IllegalArgumentException("start must >= 0 and < text.length");
        }
        if (end <= start) {
            throw new IllegalArgumentException("end must > start");
        }
        int size = text.length();

        if (end > size) {
            end = size;
        }

        DefaultDrawableBlockList result = cache.get(text);
        if (result != null && result.getStart() == start && result.getEnd() == end) {
            result.use();
            return result;
        }
        result = realCompile(text, start, end);
        result.use();
        result.doNotRecycle();
        DefaultDrawableBlockList pre = cache.put(text, result);
        if (pre != null) {
            pre.canRecycle();
        }
        return result;
    }

    protected DefaultDrawableBlockList realCompile(@NonNull CharSequence text, int start, int end) {
        DefaultDrawableBlockList list = DefaultDrawableBlockList.obtain(start, end);
//        if (start > 0) {
//            list.add(DefaultDrawableBlock.createTextBlock(text.subSequence(0, start), null));
//        }
        compileNewLines(list, text.subSequence(start, end));
        return list;
    }

    protected void compileNewLines(DefaultDrawableBlockList list, CharSequence t) {
        compileNewLines(list, t, null);
    }

    protected void compileNewLines(DefaultDrawableBlockList list, CharSequence t, SpecialStyleParams specialStyleParams) {
        if (t.length() == 0)
            return;
        String text = t.toString();
        final int len = text.length();
        int newLineIndex = text.indexOf(NEW_LINE_CHAR);
        while (newLineIndex >= 0) {
            if (newLineIndex != 0) {
                list.add(DefaultDrawableBlock.createTextBlock(text.substring(0, newLineIndex), specialStyleParams));
            }
            list.add(DefaultDrawableBlock.createNextLineBlock());
            if (newLineIndex == len) {
                text = null;
                break;
            }
            text = text.substring(newLineIndex + 1);
            newLineIndex = text.indexOf(NEW_LINE_CHAR);
        }
        if (!TextUtils.isEmpty(text)) {
            list.add(DefaultDrawableBlock.createTextBlock(text, specialStyleParams));
        }
    }
}
