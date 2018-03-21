package xfy.fakeview.library.text.utils;

/**
 * {@link xfy.fakeview.library.text.block.DefaultDrawableBlockList#lineFlags} 中保存每行的三种信息
 * lineHeight   行高度int 12位
 * baseLine     此行的baseLine int 12位
 */
public class LineUtils {
    static final int LINE_HEIGHT_MASK = 0xfff;
    static final int BASE_LINE_MASK = 0xfff000;
    static final int BASE_LINE_OFFSET = 12;

    public static int getLineHeight(int flag) {
        return flag & LINE_HEIGHT_MASK;
    }

    public static int getBaseLine(int flag) {
        return (flag & BASE_LINE_MASK) >>> BASE_LINE_OFFSET;
    }

    public static int combime(int height, int baseLine) {
        return (baseLine << BASE_LINE_OFFSET) | height;
    }

    public static int getLineHeight(int[] flags, int lineIndex) {
        if (flags.length > lineIndex) {
            return getLineHeight(flags[lineIndex]);
        }
        return -1;
    }

    public static int getBaseLine(int[] flags, int lineIndex) {
        if (flags.length > lineIndex) {
            return getBaseLine(flags[lineIndex]);
        }
        return -1;
    }

    public static int getAllLineHeight(int[] flags, int size) {
        if (flags.length >= size) {
            int h = 0;
            for (int i = 0; i < size; i ++) {
                h += getLineHeight(flags[i]);
            }
            return h;
        }
        return -1;
    }
}
