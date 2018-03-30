package xfy.fakeview.library.text.utils;

import java.util.ArrayList;

/**
 * Created by XiongFangyu on 2018/3/30.
 */
public class NoCacheSpanRegister {
    private static final ArrayList<Class<? extends BaseSpan>> noCacheClz;

    static {
        noCacheClz = new ArrayList<>();
        register(FClickableSpan.class);
    }

    public static void register(Class<? extends BaseSpan> clz) {
        if (clz == null)
            return;
        if (!noCacheClz.contains(clz))
            noCacheClz.add(clz);
    }

    public static void unRegister(Class<? extends BaseSpan> clz) {
        if (clz == null)
            return;
        noCacheClz.remove(clz);
    }

    public static <T extends BaseSpan> boolean contain(T obj) {
        if (obj == null)
            return false;
        for (int i = 0, l = noCacheClz.size(); i < l;i ++) {
            Class<? extends BaseSpan> clz = noCacheClz.get(i);
            if (clz != null && clz.isInstance(obj))
                return true;
        }
        return false;
    }
}
