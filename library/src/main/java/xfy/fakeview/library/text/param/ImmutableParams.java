package xfy.fakeview.library.text.param;

import android.text.TextPaint;
import android.text.TextUtils;
import android.view.Gravity;

import java.util.ArrayList;

import xfy.fakeview.library.text.block.IDrawableBlock;
import xfy.fakeview.library.text.drawer.TextDrawer;

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
    //文字gravity
    public int gravity = Gravity.LEFT | Gravity.TOP;
    //强制行高
    public int forceLineHeight = -1;
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
    //整个block的flag
    public long blockFlag;
    //自动根据gravity布局
    public boolean translateByGravity = true;
    //ellipsize文案
    public String ellipsizeText = TextDrawer.ELLIPSIZE_TEXT;
    //一个blocklist里所有的可点击的block
    public ArrayList<ClickSpanBlockInfo> clickSpanBlockInfos = new ArrayList<>();

    public void addClickSpanBlockInfo(IDrawableBlock block, int left, int top, long flag) {
        if (!clickSpanBlockInfos.contains(block))
            clickSpanBlockInfos.add(new ClickSpanBlockInfo(block, left, top, flag));
    }

    public void clearClickBlockInfo() {
        clickSpanBlockInfos.clear();
    }
}
