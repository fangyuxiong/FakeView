package xfy.fakeview.library.text.compiler;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Spanned;

import xfy.fakeview.library.text.block.DefaultDrawableBlock;
import xfy.fakeview.library.text.block.DefaultDrawableBlockList;
import xfy.fakeview.library.text.param.SpecialStyleParams;
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

    public ClickSpanTextCompiler(ITextCompiler<DefaultDrawableBlockList> innerCompiler) {
        super(innerCompiler);
    }

    @Override
    public void compileInternal(@NonNull DefaultDrawableBlockList list, @NonNull CharSequence text, int start, int end, @Nullable SpecialStyleParams specialStyleParams) {
        if (text instanceof Spanned) {
            Spanned spanned = (Spanned) text;
            FClickableSpan[] spans = spanned.getSpans(start, end, FClickableSpan.class);
            int len = spans == null ? 0 : spans.length;
            if (len > 0) {
                FClickableSpan span = null;
                int spanStart = 0;
                int lastSpanEnd = start;
                for (int i = 0; i < len; i ++) {
                    span = spans[i];
                    spanStart = spanned.getSpanStart(span);
                    if (lastSpanEnd < spanStart)
                        super.compileInternal(list, text, lastSpanEnd, spanStart, specialStyleParams);
                    lastSpanEnd = spanned.getSpanEnd(span);
                    compileSpan(list, span, text, spanStart, lastSpanEnd, specialStyleParams);
                }
                if (lastSpanEnd < end)
                    super.compileInternal(list, text, lastSpanEnd, end, specialStyleParams);
                return;
            }
        }
        super.compileInternal(list, text, start, end, specialStyleParams);
    }

    private void compileSpan(DefaultDrawableBlockList list, FClickableSpan span, CharSequence text, int start, int end, @Nullable SpecialStyleParams specialStyleParams) {
        DefaultDrawableBlockList children = DefaultDrawableBlockList.obtain(start, end);
        super.compileInternal(children, text, start, end, specialStyleParams);
        list.add(DefaultDrawableBlock.createSpanBlock(text.subSequence(start, end), span, children));
    }
}
