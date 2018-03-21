package xfy.fakeview.library;

import org.junit.Test;

import xfy.fakeview.library.text.utils.MeasureTextUtils;

import static org.junit.Assert.assertEquals;

/**
 * Created by XiongFangyu on 2018/3/9.
 */

public class MeasureTextTest {
    @Test
    public void testMeasureTextUtils() throws Exception {
        long flag = 0;
        flag = MeasureTextUtils.setState(flag, MeasureTextUtils.STATE_TIMEOUT);
        flag = MeasureTextUtils.setCurrentLeft(flag, 235);
        flag = MeasureTextUtils.setMaxWidth(flag, 233);
        flag = MeasureTextUtils.setLines(flag, 10000);
        flag = MeasureTextUtils.setState(flag, MeasureTextUtils.STATE_ERROR);
        flag = MeasureTextUtils.setLines(flag, 1024550);
        flag = MeasureTextUtils.setCurrentLeft(flag, 1111);
        flag = MeasureTextUtils.setMaxWidth(flag, 2588);
        assertEquals(2, MeasureTextUtils.getState(flag));
        assertEquals(1111, MeasureTextUtils.getCurrentLeft(flag));
        assertEquals(2588, MeasureTextUtils.getMaxWidth(flag));
        assertEquals(1024550, MeasureTextUtils.getLines(flag));
    }

    private String getLongByteString(long num) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 16; i ++) {
            byte b = (byte) (num & 0xF);
            sb.append(Integer.toHexString(b));
            if (i % 4 == 3) {
                sb.append(" ");
            }
            num = num >>> 4;
        }
        return sb.reverse().toString();
    }

    private static void log(String log) {
        log("test", log);
    }

    private static void log(String tag, String log) {
        System.out.println(tag + ": " + log);
    }
}
