package xfy.fakeview.library.text.utils;

/**
 * 计算纯文本结果参数
 */
public class MeasureTextUtils {
    public static final int STATE_SUCCESS = 0;
    public static final int STATE_TIMEOUT = 1;
    public static final int STATE_ERROR = 2;

    private static final long WIDTH_FLAG = 0xfffl;              //最后12位表示 width  用12位可表示4096的长度，能表示一般屏幕像素个数
    private static final long LEFT_FLAG  = 0xfff000l;           //第13位到24位表示 left 用12位可表示4096的长度，能表示一般屏幕像素个数
    private static final int LEFT_OFFSET = 12;                  //left结果需右移12位
    private static final long HEIGHT_FLAG = 0xfff000000l;       //第25到36位表示 height 用12位可表示4096的长度，能表示一般屏幕像素个数
    private static final int HEIGHT_OFFSET = 24;
    private static final long LINES_FLAG = 0x1ffffff000000000l; //第37位到61位表示 lines 25位
    private static final int LINES_OFFSET = 36;                 //lines结果需右移36位
    private static final long WILL_DRAW_ON_FIRST_LINE_FLAG = 0x2000000000000000l;   //第62位表示 lines大于1时，是否在第一行会绘制文字
    private static final int STATE_OFFSET = 62;                 //直接右移62位得到state

    private static boolean libraryLoaded;
    static {
        try {
            System.loadLibrary("measure");
            libraryLoaded = true;
        } catch (Throwable e) {
            libraryLoaded = false;
        }
    }

    /**
     * 当lines大于1时，是否在第一行绘制文字
     * @param flag
     * @return
     */
    public static boolean willDrawOnFirstLine(long flag) {
        return (flag & WILL_DRAW_ON_FIRST_LINE_FLAG) != 0;
    }

    /**
     * 获取结果状态
     * @param flag
     * @return
     */
    public static int getState(long flag) {
        return (int) (flag >>> STATE_OFFSET);
    }

    /**
     * 获取当前绘制到的坐标
     * @param flag
     * @return
     */
    public static int getCurrentLeft(long flag) {
        return (int) ((flag & LEFT_FLAG) >>> LEFT_OFFSET);
    }

    /**
     * 获取当前绘制行数
     * @param flag
     * @return
     */
    public static int getLines(long flag) {
        return (int) ((flag & LINES_FLAG) >>> LINES_OFFSET);
    }

    /**
     * 获取当前绘制行的最长长度
     * @param flag
     * @return
     */
    public static int getMaxWidth(long flag) {
        return (int) (flag & WIDTH_FLAG);
    }

    /**
     * 获取当前行高度
     * @param flag
     * @return
     */
    public static int getMaxHeight(long flag) {
        return (int) ((flag & HEIGHT_FLAG) >>> HEIGHT_OFFSET);
    }

    public static long setState(long flag, int state) {
        long stateL = state;
        flag = (flag & ~(3l << STATE_OFFSET)) | (stateL << STATE_OFFSET);
        return flag;
    }

    public static long setCurrentLeft(long flag, int currentLeft) {
        long leftL = currentLeft;
        flag = (flag & ~(LEFT_FLAG)) | (leftL << LEFT_OFFSET);
        return flag;
    }

    public static long setLines(long flag, int lines) {
        long ll = lines;
        flag = (flag & ~(LINES_FLAG)) | (ll << LINES_OFFSET);
        return flag;
    }

    public static long setMaxWidth(long flag, int maxWidth) {
        flag = (flag & ~(WIDTH_FLAG)) | maxWidth;
        return flag;
    }

    public static long setMaxHeight(long flag, int maxHeight) {
        long hl = maxHeight;
        flag = (flag & ~(HEIGHT_FLAG)) | (hl << HEIGHT_OFFSET);
        return flag;
    }

    public static long setWillDrawOnFirstLine(long flag) {
        flag = flag | WILL_DRAW_ON_FIRST_LINE_FLAG;
        return flag;
    }

    public static long measureTextByNative(long flag, int left, int right, float[] widths) {
        if (libraryLoaded)
            return nativeMeasureText(flag, left, right, widths);
        return measureText(flag, left, right, widths, 0);
    }

    private static native long nativeMeasureText(long flag, int left, int right, float[] widths);
    
    public static long measureText(long flag, int left, int right, float[] widths, long timeout) {
        final long now = now();
        final int contentWidth = right - left;
        for (int i = 0, l = widths.length; i < l; i++) {
            if (contentWidth < widths[i]) {
                return setState(flag, STATE_ERROR);
            }
            if (timeout > 0 && now() - now >= timeout) {
                return setState(flag, STATE_TIMEOUT);
            }
            if (getCurrentLeft(flag) + widths[i] > right) {
                if (i != 0) {
                    flag = setWillDrawOnFirstLine(flag);
                }
                flag = gotoCalNextLine(flag, left);
            }
            flag = setCurrentLeft(flag, (int) (getCurrentLeft(flag) + Math.ceil(widths[i])));
        }
        flag = calContentMaxWidth(flag, left);
        return flag;
    }
    
    public static long calContentMaxWidth(long flag, int left) {
        int max = Math.max(getMaxWidth(flag), getCurrentLeft(flag) - left);
        return setMaxWidth(flag, max);
    }

    public static long gotoCalNextLine(long flag, int left) {
        flag = setLines(flag, getLines(flag) + 1);
        flag = calContentMaxWidth(flag, left);
        return setCurrentLeft(flag, left);
    }

    private static long now() {
        return System.currentTimeMillis();
    }
}
