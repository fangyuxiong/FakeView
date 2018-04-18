package xfy.fakeview.library.text.param;

import android.graphics.Typeface;
import android.text.TextPaint;

import java.util.ArrayList;

import xfy.fakeview.library.text.utils.BaseSpan;

/**
 * Created by XiongFangyu on 2018/3/2.
 */
public class SpecialStyleParams {
    //是否有前景色
    public boolean hasFColor;
    //前景色
    public int foregroundColor;
    //是否有背景色
    public boolean hasBColor;
    //背景色
    public int backgroundColor;
    //是否为粗体
    public boolean bold;
    //是否有下划线
    public boolean underline;
    //是否为斜体
    public boolean italic;
    //是否更改文字大小
    public boolean hasTextSize;
    //文字大小
    public float textSize;
    //强制文本高度
    public int forceLineHeight = -1;

    private static final ArrayList<SpecialStyleParams> cache;
    private static final int DEFAULT_SIZE = 30;

    static {
        cache = new ArrayList<>();
        for (int i = 0; i < DEFAULT_SIZE; i ++) {
            cache.add(new SpecialStyleParams());
        }
    }

    private SpecialStyleParams() {

    }

    private static synchronized void putToCache(SpecialStyleParams params) {
        cache.add(params);
    }

    public static synchronized SpecialStyleParams obtain() {
        if (cache.isEmpty())
            return new SpecialStyleParams();
        return cache.remove(0);
    }

    public static synchronized SpecialStyleParams obtain(BaseSpan span) {
        SpecialStyleParams params = null;
        if (cache.isEmpty()) {
            params = new SpecialStyleParams();
        } else {
            params = cache.remove(0);
        }
        return params.initBySpan(span);
    }

    public void recycle() {
        hasFColor = false;
        foregroundColor = 0;
        hasBColor = false;
        backgroundColor = 0;
        bold = underline = italic = false;
        hasTextSize = false;
        textSize = 0;
        forceLineHeight = -1;
        putToCache(this);
    }

    public SpecialStyleParams initByPaint(TextPaint paint) {
        underline = paint.isUnderlineText();
        Typeface typeface = paint.getTypeface();
        if (typeface != null) {
            bold = typeface.isBold() || paint.isFakeBoldText();
            italic = typeface.isItalic();
        }
        return this;
    }

    public SpecialStyleParams initBySpan(BaseSpan span) {
        hasFColor = span.isHasFColor();
        foregroundColor = span.getForegroundColor();
        hasBColor = span.isHasBColor();
        backgroundColor = span.getBackgroundColor();
        bold = span.isBold();
        underline = span.isUnderline();
        italic = span.isItalic();
        forceLineHeight = span.forceLineHeight;
        return this;
    }

    public SpecialStyleParams withForegroundColor(int color) {
        hasFColor = true;
        foregroundColor = color;
        return this;
    }
    public SpecialStyleParams withBackgroundColor(int color) {
        hasBColor = true;
        backgroundColor = color;
        return this;
    }
    public SpecialStyleParams boldText() {
        bold = true;
        return this;
    }
    public SpecialStyleParams underlineText() {
        underline = true;
        return this;
    }
    public SpecialStyleParams italicText() {
        italic = true;
        return this;
    }

    public SpecialStyleParams withTextSize(float px) {
        hasTextSize = true;
        textSize = px;
        return this;
    }

    public SpecialStyleParams withForceLineHeight(int forceLineHeight) {
        this.forceLineHeight = forceLineHeight;
        return this;
    }
}
