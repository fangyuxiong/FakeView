package xfy.fakeview.library.text.param;

/**
 * 可变参数
 */
public class VariableParams {
    //当前绘制位置坐标
    public int currentLeft;
    //当前绘制文字位置的y坐标
    public int currentBaseline;
    //当前绘制图片的y坐标
    public int currentTop;
    //当前绘制行数
    public int currentDrawLine;
    //是否已绘制了最后的省略号
    public boolean isDrawEndEllipsize;
}
