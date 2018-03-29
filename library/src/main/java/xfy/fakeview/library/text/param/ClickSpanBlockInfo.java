package xfy.fakeview.library.text.param;

import xfy.fakeview.library.text.block.IDrawableBlock;

/**
 * Created by XiongFangyu on 2018/3/29.
 */
public class ClickSpanBlockInfo {
    public IDrawableBlock block;
    public int blockLeft;
    public int blockTop;
    public long blockFlag;

    public ClickSpanBlockInfo(IDrawableBlock block, int left, int top, long flag) {
        this.block = block;
        blockLeft = left;
        blockTop = top;
        blockFlag = flag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClickSpanBlockInfo that = (ClickSpanBlockInfo) o;

        return block != null ? block.equals(that.block) : that.block == null;
    }

    @Override
    public int hashCode() {
        return block != null ? block.hashCode() : 0;
    }
}
