package com.xfy.fakeview.special;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import xfy.fakeview.library.text.utils.CallbackObserver;

/**
 * Created by XiongFangyu on 2018/3/29.
 */
public class SpecialDrawable extends Drawable implements CallbackObserver{
    private final Path path;
    private final Paint paint;

    private RectF rect;
    private float rx, ry;
    private float maxRx, maxRy;

    private Animator anim;

    private static volatile SpecialDrawable instance;

    public static SpecialDrawable getSingleInstance() {
        if (instance == null) {
            synchronized (SpecialDrawable.class) {
                if (instance == null)
                    instance = new SpecialDrawable();
            }
        }
        return instance;
    }

    public SpecialDrawable() {
        path = new Path();
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.GRAY);
        paint.setStyle(Paint.Style.FILL);
    }

    private SpecialDrawable(State state) {
        path = new Path();
        paint = state.paint;
    }

    private void setPath() {
        if (rect == null) {
            Rect r = getBounds();
            if (r.width() <= 10 || r.height() <= 10)
                return;
            rect = new RectF(r);
            rect.inset(5, 5);
        }
        path.reset();
        path.addRoundRect(rect, rx, ry, Path.Direction.CCW);
    }

    @Override
    public void onBoundsChange(Rect rect) {
        maxRx = rect.width() * 0.5f;
        maxRy = rect.height() * 0.5f;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        canvas.drawPath(path, paint);
    }

    @Override
    public void setAlpha(int alpha) {
        paint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        paint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return 0;
    }

    @Override
    public void onCallbackSet(Callback callback) {
        setCallback(callback);
        if (callback != null) {
            initAnim();
            if (!anim.isStarted()) {
                anim.start();
            } else if (!anim.isRunning()) {
                anim.start();
            }
        } else {
            if (anim != null)
                anim.cancel();
        }
    }

    private void initAnim() {
        if (anim == null) {
            ValueAnimator va = ValueAnimator.ofFloat(0, 1);
            va.setDuration(1000);
            va.setRepeatCount(ValueAnimator.INFINITE);
            va.setRepeatMode(ValueAnimator.REVERSE);
            va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float v = (float) animation.getAnimatedValue();
                    rx = maxRx * v;
                    ry = maxRy * v;
                    setPath();
                    invalidateSelf();
                }
            });
            anim = va;
        }
    }

    @Override
    public int getIntrinsicWidth() {
        return 10;
    }

    @Override
    public int getIntrinsicHeight() {
        return 10;
    }

    @Override
    public Drawable.ConstantState getConstantState() {
        return new State(paint);
    }

    private static class State extends Drawable.ConstantState {
        private final Paint paint;
        public State(Paint paint) {
            this.paint = paint;
        }
        @NonNull
        @Override
        public Drawable newDrawable() {
            return new SpecialDrawable(this);
        }

        @Override
        public int getChangingConfigurations() {
            return 0;
        }
    }
}
