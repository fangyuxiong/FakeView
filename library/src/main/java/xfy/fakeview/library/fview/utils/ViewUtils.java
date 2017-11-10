package xfy.fakeview.library.fview.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Size;
import android.view.View;

import java.lang.reflect.Field;

/**
 * Created by XiongFangyu on 2017/11/10.
 */
public class ViewUtils {
    private static Field mAttachInfo;
    private static Field mWindowLeft;
    private static Field mWindowTop;

    static {
        try {
            mAttachInfo = View.class.getDeclaredField("mAttachInfo");
            mAttachInfo.setAccessible(true);
        } catch (Throwable e) {

        }
    }

    /**
     * Get Window left and top by View.
     * see View.AttachInfo
     * @param view any view attached to window
     * @param pos int array
     */
    public static void getWindowPosition(@NonNull View view, @Size(2) int[] pos) {
        if (mAttachInfo == null)
            return;
        try {
            Object info = mAttachInfo.get(view);
            if (info == null)
                return;
            if (mWindowLeft == null || mWindowTop == null) {
                Class clz = info.getClass();
                mWindowLeft = clz.getDeclaredField("mWindowLeft");
                mWindowLeft.setAccessible(true);
                mWindowTop = clz.getDeclaredField("mWindowTop");
                mWindowTop.setAccessible(true);
            }
            pos[0] = (int) mWindowLeft.get(info);
            pos[1] = (int) mWindowTop.get(info);
        } catch (Throwable e) {}
    }
}
