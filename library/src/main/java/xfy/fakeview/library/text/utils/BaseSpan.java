package xfy.fakeview.library.text.utils;

/**
 * Created by XiongFangyu on 2018/3/16.
 */
public class BaseSpan {
    //是否为粗体
    private boolean bold;
    //是否有下划线
    private boolean underline;
    //是否为斜体
    private boolean italic;
    //是否有前景色
    private boolean hasFColor;
    //前景色
    private int foregroundColor;
    //是否有背景色
    private boolean hasBColor;
    //背景色
    private int backgroundColor;
    //强制文本高度
    public int forceLineHeight = -1;

    public BaseSpan boldText() {
        bold = true;
        return this;
    }

    public BaseSpan underlineText() {
        underline = true;
        return this;
    }

    public BaseSpan italicText() {
        italic = true;
        return this;
    }

    public BaseSpan withForegroundColor(int color) {
        foregroundColor = color;
        hasFColor = true;
        return this;
    }

    public BaseSpan withBackgroundColor(int color) {
        backgroundColor = color;
        hasBColor = true;
        return this;
    }

    public BaseSpan withForceLineHeight(int forceLineHeight) {
        this.forceLineHeight = forceLineHeight;
        return this;
    }

    public boolean isBold() {
        return bold;
    }

    public boolean isUnderline() {
        return underline;
    }

    public boolean isItalic() {
        return italic;
    }

    public boolean isHasFColor() {
        return hasFColor;
    }

    public int getForegroundColor() {
        return foregroundColor;
    }

    public boolean isHasBColor() {
        return hasBColor;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

}
