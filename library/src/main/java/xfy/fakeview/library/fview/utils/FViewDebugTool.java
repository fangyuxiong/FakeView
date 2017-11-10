package xfy.fakeview.library.fview.utils;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;

/**
 * Created by XiongFangyu on 2017/11/8.
 *
 * debug drawing tool
 *
 * draw ontline of a FView
 */
public class FViewDebugTool {
    private Paint debugPaint;
    private Path debugPath;

    public FViewDebugTool() {
        debugPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        debugPaint.setStyle(Paint.Style.STROKE);
        debugPaint.setStrokeWidth(5);
        debugPaint.setColor(Color.RED);
        debugPath = new Path();
    }

    public FViewDebugTool withColor(int color) {
        debugPaint.setColor(color);
        return this;
    }

    public FViewDebugTool withStrokeWidth(float width) {
        debugPaint.setStrokeWidth(width);
        return this;
    }

    public FViewDebugTool withBounds(Rect bounds) {
        debugPath.addRect(bounds.left, bounds.top, bounds.right, bounds.bottom, Path.Direction.CCW);
        return this;
    }

    public FViewDebugTool withBounds(int l, int t, int r, int b) {
        debugPath.addRect(l, t, r, b, Path.Direction.CCW);
        return this;
    }

    public void draw(Canvas canvas) {
        canvas.drawPath(debugPath, debugPaint);
    }
}
