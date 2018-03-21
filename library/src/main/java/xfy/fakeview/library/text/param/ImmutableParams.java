package xfy.fakeview.library.text.param;

import android.text.TextPaint;
import android.text.TextUtils;

/**
 * 不可变参数
 */
public class ImmutableParams {
    public TextPaint paint;
    //可绘制区域左边坐标
    public int left;
    //可绘制区域右边坐标
    public int right;
    //可绘制区域上边坐标
    public int top;
    //可绘制区域的下边坐标
    public int bottom;
    //图片高度
    public int drawableHeight;
    //省略号的位置
    public TextUtils.TruncateAt truncateAt;
    //行间距
    public int lineSpace;
    //文本行数
    public int lines;
    //可绘制文本行数
    public int needDrawLine;
    //省略号长度
    public int ellipsizeLength;
    //所有行高
    public int[] lineInfos;
}
