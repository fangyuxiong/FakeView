package xfy.fakeview.library.text.compiler;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

    private LruCache<CharSequence, DefaultDrawableBlockList> cache;
    protected ITextCompiler<DefaultDrawableBlockList> innerCompiler;

    protected DefaultTextCompiler() {
        if (cacheSize() > 0) {
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
    }

    public DefaultTextCompiler(ITextCompiler<DefaultDrawableBlockList> innerCompiler) {
        this();
        setInnerCompiler(innerCompiler);
    }

    protected int cacheSize() {
        return 30;
    }

    @Override
    public void setInnerCompiler(@Nullable ITextCompiler<DefaultDrawableBlockList> compiler) {
        innerCompiler = compiler;
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

        DefaultDrawableBlockList result = cache != null ? cache.get(text) : null;
        if (result != null && result.getStart() == start && result.getEnd() == end) {
            result.use();
            return result;
        }
        result = DefaultDrawableBlockList.obtain(true, start, end);
        compileInternal(result, text, start, end, null);
        result.use();
        if (cache != null && result.canSaveToCache()) {
            result.doNotRecycle();
            DefaultDrawableBlockList pre = cache.put(text, result);
            if (pre != null) {
                pre.canRecycle();
            }
        }
        return result;
    }

    @Override
    public void compileInternal(@NonNull DefaultDrawableBlockList list, @NonNull CharSequence text, int start, int end, @Nullable SpecialStyleParams specialStyleParams) {
        if (innerCompiler != null) {
            innerCompiler.compileInternal(list, text, start, end, specialStyleParams);
        } else {
            compileNewLines(list, text.subSequence(start, end), specialStyleParams);
        }
    }

    /**
     * compile special text, implement by child class
     * @param list parent list
     * @param t special text
     * @return true if compile at least one special block, false otherwise
     */
    protected boolean compileSpecialText(DefaultDrawableBlockList list, CharSequence t, SpecialStyleParams specialStyleParams) {
        return false;
    }

    private void compileNewLines(DefaultDrawableBlockList list, CharSequence t, SpecialStyleParams specialStyleParams) {
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
