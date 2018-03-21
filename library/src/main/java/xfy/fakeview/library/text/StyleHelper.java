package xfy.fakeview.library.text;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;

import java.lang.reflect.Method;

import xfy.fakeview.library.R;
import xfy.fakeview.library.text.compiler.ClickSpanTextCompiler;
import xfy.fakeview.library.text.compiler.DefaultTextCompiler;
import xfy.fakeview.library.text.compiler.DrawableTextCompiler;
import xfy.fakeview.library.text.compiler.ITextCompiler;
import xfy.fakeview.library.text.compiler.SpecialTextHelper;

/**
 * Created by XiongFangyu on 2018/3/20.
 */
public class StyleHelper {
    static enum Compiler {
        text_only {
            @Override
            ITextCompiler getCompiler(Context context) {
                return DefaultTextCompiler.getCompiler();
            }
        },
        contain_image {
            @Override
            ITextCompiler getCompiler(Context context) {
                return DrawableTextCompiler.getCompiler();
            }
        },
        click_span {
            @Override
            ITextCompiler getCompiler(Context context) {
                return ClickSpanTextCompiler.getCompiler();
            }
        },
        spcial_text {
            @Override
            ITextCompiler getCompiler(Context context) {
                return SpecialTextHelper.getSpecialCompiler(context);
            }
        },
        ;
        abstract ITextCompiler getCompiler(Context context);
    }
    public int maxLines = -1;
    public int textSize = -1;
    public int lineSpace = -1;
    public int textColor = 0;
    public float drawableScale = 1;
    public int drawableSize = -1;
    public CharSequence text;
    public boolean measureWhenSetText = false;
    public ITextCompiler textCompiler;

    public StyleHelper(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        if (context == null || attrs == null)
            return;
        final Resources.Theme theme = context.getTheme();
        if (theme == null)
            return;
        TypedArray a = theme.obtainStyledAttributes(attrs,
                R.styleable.FNewTextView, defStyleAttr, defStyleRes);
        TypedArray appearance = null;
        int ap = a.getResourceId(
                R.styleable.FNewTextView_fntv_style, -1);
        if (ap != -1) {
            appearance = theme.obtainStyledAttributes(
                    ap, R.styleable.FNewTextView);
        }
        initStyle(context, appearance);
        initStyle(context, a);
    }

    private void initStyle(Context context, TypedArray appearance){
        if (appearance != null) {
            final int len = appearance.getIndexCount();
            for (int i = 0 ; i < len ; i ++) {
                int attr = appearance.getIndex(i);
                if (attr == R.styleable.FNewTextView_android_textSize) {
                    textSize = appearance.getDimensionPixelSize(attr, textSize);
                } else if (attr == R.styleable.FNewTextView_android_text) {
                    text = appearance.getString(attr);
                } else if (attr == R.styleable.FNewTextView_android_maxLines) {
                    maxLines = appearance.getInt(attr, maxLines);
                } else if (attr == R.styleable.FNewTextView_android_textColor) {
                    textColor = appearance.getColor(attr, textColor);
                } else if (attr == R.styleable.FNewTextView_fntv_drawable_scale) {
                    drawableScale = appearance.getFloat(attr, drawableScale);
                } else if (attr == R.styleable.FNewTextView_fntv_drawable_size) {
                    drawableSize = appearance.getDimensionPixelSize(attr, drawableSize);
                } else if (attr == R.styleable.FNewTextView_android_lineSpacingExtra) {
                    lineSpace = appearance.getDimensionPixelOffset(attr, lineSpace);
                } else if (attr == R.styleable.FNewTextView_fntv_measure_when_set_text) {
                    measureWhenSetText = appearance.getBoolean(attr, measureWhenSetText);
                } else if (attr == R.styleable.FNewTextView_fntv_text_compiler) {
                    setTextCompiler(context, appearance, attr);
                }
            }
            appearance.recycle();
        }
    }

    private void setTextCompiler(Context context, TypedArray a, int attr) {
        try {
            int e = a.getInt(attr, -1);
            if (e >= 0) {
                Compiler[] compilers = Compiler.values();
                if (e < compilers.length) {
                    textCompiler = compilers[e].getCompiler(context);
                    return;
                }
            }
        } catch (Throwable e) {}
        String clz = a.getString(attr);
        if (!TextUtils.isEmpty(clz)) {
            try {
                Class<ITextCompiler> c = (Class<ITextCompiler>) Class.forName(clz);
                Method m = c.getDeclaredMethod("getCompiler");
                m.setAccessible(true);
                textCompiler = (ITextCompiler) m.invoke(null);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }
}
