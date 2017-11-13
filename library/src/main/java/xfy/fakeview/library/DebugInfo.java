package xfy.fakeview.library;

/**
 * Created by XiongFangyu on 2017/11/13.
 */
public class DebugInfo {
    public static boolean DEBUG = false;
    public static void setDebug(boolean debug) {
        if (DEBUG != debug) {
            DEBUG = debug;
        }
    }
}
