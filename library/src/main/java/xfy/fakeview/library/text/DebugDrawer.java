package xfy.fakeview.library.text;


import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * Created by XiongFangyu on 2018/6/20.
 */
public class DebugDrawer {

    private static boolean debug = false;

    public static boolean isDebug() {
        return debug;
    }

    public static void setDebug(boolean debug) {
        DebugDrawer.debug = debug;
    }

    private static Paint debugPaint;
    private static final int DEFAULT_COLOR = Color.argb(20, 244, 30, 30);

    public static void draw(Canvas canvas, int tx, int ty) {
        if (!debug)
            return;
        if (tx <= 0 && ty <= 0)
            return;
        if (tx <= 0)
            tx = 1;
        else if (ty <= 0)
            ty = 1;
        draw(canvas, 0, 0, tx, ty);
    }

    public static void draw(Canvas canvas, int l, int t, int r, int b) {
        if (!debug)
            return;
        if (debugPaint == null) {
            debugPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            debugPaint.setColor(DEFAULT_COLOR);
        }
        canvas.drawRect(l, t, r, b, debugPaint);
    }

    public static void draw(Canvas canvas, Rect rect) {
        if (!debug)
            return;
        if (debugPaint == null) {
            debugPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            debugPaint.setColor(DEFAULT_COLOR);
        }
        canvas.drawRect(rect, debugPaint);
    }

    public static void draw(Canvas canvas, int tx, int ty, int color) {
        if (!debug)
            return;
        if (tx <= 0 && ty <= 0)
            return;
        if (tx <= 0)
            tx = 1;
        else if (ty <= 0)
            ty = 1;
        draw(canvas, 0, 0, tx, ty, color);
    }

    public static void draw(Canvas canvas, int l, int t, int r, int b, int color) {
        if (!debug)
            return;
        if (debugPaint == null) {
            debugPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        }
        debugPaint.setColor(color);
        canvas.drawRect(l, t, r, b, debugPaint);
        debugPaint.setColor(DEFAULT_COLOR);
    }

    public static void draw(Canvas canvas, Rect rect, int color) {
        if (!debug)
            return;
        if (debugPaint == null) {
            debugPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        }
        debugPaint.setColor(color);
        canvas.drawRect(rect, debugPaint);
        debugPaint.setColor(DEFAULT_COLOR);
    }
}
