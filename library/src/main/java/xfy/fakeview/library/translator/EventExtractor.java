package xfy.fakeview.library.translator;

import android.view.View;

import java.lang.reflect.Field;

import xfy.fakeview.library.DebugInfo;

/**
 * Created by XiongFangyu on 2017/11/13.
 *
 * Extracting event(click and long click) from a View
 */
public class EventExtractor {
    static Field mListenerInfoField;
    private static Field mOnClickListenerField;
    private static Field mOnLongClickListenerField;

    static {
        try {
            mListenerInfoField = View.class.getDeclaredField("mListenerInfo");
            mListenerInfoField.setAccessible(true);
        } catch (Throwable e) {
            mListenerInfoField = null;
        }
    }

    /**
     * Get View's OnClickListener by reflect
     * @param target target View
     * @return null if target View has no OnClickListener or error occur,
     *          othrewise OnClickListener object will be return.
     */
    public static View.OnClickListener getViewOnClickListener(View target) {
        try {
            Object mListenerInfo = mListenerInfoField.get(target);
            if (mListenerInfo == null)
                return null;
            if (mOnClickListenerField == null) {
                Class clz = mListenerInfo.getClass();
                mOnClickListenerField = clz.getDeclaredField("mOnClickListener");
                mOnClickListenerField.setAccessible(true);
            }
            return (View.OnClickListener) mOnClickListenerField.get(mListenerInfo);
        } catch (Throwable e) {
            if (DebugInfo.DEBUG)
                e.printStackTrace();
        }
        return null;
    }

    /**
     * Get View's OnLongClickListener by reflect
     * @param target target View
     * @return null if target View has no OnLongClickListener or error occur,
     *          otherwise OnLongClickListener object will be return.
     */
    public static View.OnLongClickListener getViewOnLongClickListener(View target) {
        try {
            Object mListenerInfo = mListenerInfoField.get(target);
            if (mListenerInfo == null)
                return null;
            if (mOnLongClickListenerField == null) {
                Class clz = mListenerInfo.getClass();
                mOnLongClickListenerField = clz.getDeclaredField("mOnLongClickListener");
                mOnLongClickListenerField.setAccessible(true);
            }
            return (View.OnLongClickListener) mOnLongClickListenerField.get(mListenerInfo);
        } catch (Throwable e) {
            if (DebugInfo.DEBUG)
                e.printStackTrace();
        }
        return null;
    }
}
