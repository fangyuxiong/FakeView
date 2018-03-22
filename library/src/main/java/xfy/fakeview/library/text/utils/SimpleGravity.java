package xfy.fakeview.library.text.utils;

import static android.view.Gravity.AXIS_CLIP;
import static android.view.Gravity.AXIS_PULL_AFTER;
import static android.view.Gravity.AXIS_PULL_BEFORE;
import static android.view.Gravity.AXIS_X_SHIFT;
import static android.view.Gravity.AXIS_Y_SHIFT;

/**
 * Created by XiongFangyu on 2018/3/22.
 */
public class SimpleGravity {

    public static long apply(int gravity, int cl, int ct, int cr, int cb, int w, int h) {
        int left = 0, top = 0;
        switch (gravity&((AXIS_PULL_BEFORE|AXIS_PULL_AFTER)<<AXIS_X_SHIFT)) {
            case 0:
                left = cl + ((cr - cl - w) >> 1);
                if ((gravity&(AXIS_CLIP<<AXIS_X_SHIFT))
                        == (AXIS_CLIP<<AXIS_X_SHIFT)) {
                    left = left < cl ? cl : left;
                }
                break;
            case AXIS_PULL_AFTER<<AXIS_X_SHIFT:
                left = cr - w;
                if ((gravity&(AXIS_CLIP<<AXIS_X_SHIFT))
                        == (AXIS_CLIP<<AXIS_X_SHIFT)) {
                    left = left < cl ? cl : left;
                }
                break;
            default:
                left = cl;
                break;
        }
        switch (gravity&((AXIS_PULL_BEFORE|AXIS_PULL_AFTER)<<AXIS_Y_SHIFT)) {
            case 0:
                top = ct + ((cb - ct - h) >> 1);
                if ((gravity&(AXIS_CLIP<<AXIS_Y_SHIFT))
                        == (AXIS_CLIP<<AXIS_Y_SHIFT)) {
                    top = top < ct ? ct : top;
                }
                break;
            case AXIS_PULL_AFTER<<AXIS_Y_SHIFT:
                top = cb - h;
                if ((gravity&(AXIS_CLIP<<AXIS_Y_SHIFT))
                        == (AXIS_CLIP<<AXIS_Y_SHIFT)) {
                    top = top < ct ? ct : top;
                }
                break;
            default:
                top = ct;
                break;
        }
        return combine(left, top);
    }

    public static long combine(int left, int top) {
        long tl = top;
        tl = tl << 32;
        return tl | left;
    }

    public static int getLeft(long flag) {
        return (int) flag;
    }

    public static int getTop(long flag) {
        flag = flag >>> 32;
        return (int) flag;
    }
}
