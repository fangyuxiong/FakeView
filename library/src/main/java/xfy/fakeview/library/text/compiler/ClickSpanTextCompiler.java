package xfy.fakeview.library.text.compiler;

import android.support.annotation.NonNull;
import android.text.Spanned;

import xfy.fakeview.library.text.block.DefaultDrawableBlock;
import xfy.fakeview.library.text.block.DefaultDrawableBlockList;
import xfy.fakeview.library.text.utils.FClickableSpan;

/**
 * Created by XiongFangyu on 2018/3/16.
 */
public class ClickSpanTextCompiler extends DefaultTextCompiler {
    private static volatile ClickSpanTextCompiler compiler;

    public static ClickSpanTextCompiler getCompiler() {
        if (compiler == null) {
            synchronized (ClickSpanTextCompiler.class) {
                if (compiler == null)
                    compiler = new ClickSpanTextCompiler();
            }
        }
        return compiler;
    }

    protected ClickSpanTextCompiler() {}

    @Override
    protected DefaultDrawableBlockList realCompile(@NonNull CharSequence text, int start, int end) {
        if (text instanceof Spanned) {
            Spanned spanned = (Spanned) text;
            FClickableSpan[] spans = spanned.getSpans(start, end, FClickableSpan.class);
            int len = spans == null ? 0 : spans.length;
            if (len > 0) {
                DefaultDrawableBlockList list = DefaultDrawableBlockList.obtain(start, end);
                FClickableSpan span = null;
                int spanStart = 0;
                int lastSpanEnd = start;
                for (int i = 0; i < len; i ++) {
                    span = spans[i];
                    spanStart = spanned.getSpanStart(span);
                    if (lastSpanEnd < spanStart)
                        compileNewLines(list, text.subSequence(lastSpanEnd, spanStart));
                    lastSpanEnd = spanned.getSpanEnd(span);
                    compileSpan(list, span, text, spanStart, lastSpanEnd);
                }
                if (lastSpanEnd < end)
                    compileNewLines(list, text.subSequence(lastSpanEnd, end));
                return list;
            }
        }
        return super.realCompile(text, start, end);
    }

    protected void compileSpan(DefaultDrawableBlockList list, FClickableSpan span, CharSequence text, int start, int end) {
        DefaultDrawableBlockList children = super.realCompile(text, start, end);
        list.add(DefaultDrawableBlock.createSpanBlock(text.subSequence(start, end), span, children));
    }
}
