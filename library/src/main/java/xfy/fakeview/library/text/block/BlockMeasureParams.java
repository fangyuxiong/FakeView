package xfy.fakeview.library.text.block;

/**
 * Created by XiongFangyu on 2018/3/29.
 */
public class BlockMeasureParams {
    public int lineInfo;
    public int drawableSize;
    public int currentLeft;
    public int currentTop;
    public int left;
    public int right;
    public boolean includePad;

    private BlockMeasureParams() {}

    public static BlockMeasureParams obtain() {
        return new BlockMeasureParams();
    }

    public BlockMeasureParams setLineInfo(int lineInfo) {
        this.lineInfo = lineInfo;
        return this;
    }

    public BlockMeasureParams setDrawableSize(int drawableSize) {
        this.drawableSize = drawableSize;
        return this;
    }

    public BlockMeasureParams setCurrentLeft(int currentLeft) {
        this.currentLeft = currentLeft;
        return this;
    }

    public BlockMeasureParams setCurrentTop(int currentTop) {
        this.currentTop = currentTop;
        return this;
    }

    public BlockMeasureParams setLeft(int left) {
        this.left = left;
        return this;
    }

    public BlockMeasureParams setRight(int right) {
        this.right = right;
        return this;
    }

    public BlockMeasureParams setIncludePad(boolean includePad) {
        this.includePad = includePad;
        return this;
    }
}
