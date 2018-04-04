package xfy.fakeview.library.text.drawer;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;

import xfy.fakeview.library.text.block.BlockMeasureParams;
import xfy.fakeview.library.text.block.IDrawableBlockList;
import xfy.fakeview.library.text.param.ImmutableParams;
import xfy.fakeview.library.text.param.SpecialStyleParams;
import xfy.fakeview.library.text.param.VariableParams;
import xfy.fakeview.library.text.utils.LineUtils;
import xfy.fakeview.library.text.utils.MeasureTextUtils;

/**
 * Created by XiongFangyu on 2018/2/9.
 */
public class TextDrawer {
    private static final String TAG = "Fake--TextDrawer";
    private static Paint.FontMetricsInt fontMetricsInt;
    private static boolean DEBUG = false;
    private static Paint debugPaint;

    public static final String ELLIPSIZE_TEXT = "...";

    public static void setDebug(boolean debug) {
        DEBUG = debug;
        if (debug) {
            if (debugPaint == null) {
                debugPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                debugPaint.setColor(Color.GREEN);
            }
        } else {
            debugPaint = null;
        }
    }

    private static void drawLine(Canvas canvas, float x0, float y0, float x1, float y1) {
        if (!DEBUG)
            return;
        canvas.drawLine(x0, y0, x1, y1, debugPaint);
    }

    private static Paint.FontMetricsInt getFontMetricsInt() {
        if (fontMetricsInt == null) {
            fontMetricsInt = new Paint.FontMetricsInt();
        }
        return fontMetricsInt;
    }

    /**
     * 绘制纯文本
     * @param canvas
     * @param text              纯文本
     * @param variableParams    可变参数
     *                          @see VariableParams
     * @param immutableParams   不可变参数
     *                          @see ImmutableParams
     */
    public static void drawText(Canvas canvas, CharSequence text,
                                @NonNull VariableParams variableParams, @NonNull ImmutableParams immutableParams,
                                @Nullable SpecialStyleParams styleParams) {
        final TextPaint textPaint = immutableParams.paint;
        final int oldFColor = textPaint.getColor();
        final boolean oldUnderline = textPaint.isUnderlineText();
        final Typeface oldType = textPaint.getTypeface();
        final boolean oldFakeBold = textPaint.isFakeBoldText();
        final float textSkewX = textPaint.getTextSkewX();
        final float oldTextSize = textPaint.getTextSize();

        TextPaint backPaint = null;
        if (styleParams != null) {
            setPaintByStyleParams(textPaint, styleParams);
            if (styleParams.hasBColor) {
                backPaint = textPaint;
                backPaint.bgColor = styleParams.backgroundColor;
            }
        }

        int textWidth = (int) Math.ceil(textPaint.measureText(text, 0, text.length()));
        int breakPoint;
        final int right = immutableParams.right;
        final TextUtils.TruncateAt ellipsize = immutableParams.truncateAt;
        boolean drawError = false;
        while (textWidth + variableParams.currentLeft > right) {
            final int maxWidth = getDrawMaxWidthFronNow(variableParams, immutableParams);
            final int rmw = maxWidth < 0 ? -maxWidth : maxWidth;
            breakPoint = textPaint.breakText(text, 0, text.length(), true,
                    rmw, null);

            if (backPaint != null) {
                drawBack(canvas, backPaint, rmw, variableParams, immutableParams);
            }

            canvas.drawText(text, 0, breakPoint, variableParams.currentLeft, variableParams.currentBaseline, textPaint);
            if (maxWidth < 0 && ellipsize == TextUtils.TruncateAt.END) {
                variableParams.currentLeft += (int) Math.ceil(textPaint.measureText(text, 0, breakPoint));

                if (backPaint != null) {
                    drawBack(canvas, backPaint, immutableParams.ellipsizeLength, variableParams, immutableParams);
                }

                drawEllipsize(canvas, variableParams, immutableParams);

                if (styleParams != null) {
                    restorePaint(textPaint, oldFColor, oldUnderline, oldFakeBold, textSkewX, oldTextSize, oldType);
                }
                return;
            }
            if (variableParams.currentDrawLine >= immutableParams.needDrawLine - 1) {
                drawError = true;
                variableParams.isDrawEndEllipsize = true;
                break;
            }
            toNewDrawLine(variableParams, immutableParams);
            text = text.subSequence(breakPoint, text.length());
            textWidth = (int) Math.ceil(textPaint.measureText(text, 0, text.length()));
        }

        if (!drawError) {
            if (backPaint != null) {
                drawBack(canvas, backPaint, textWidth, variableParams, immutableParams);
            }
            canvas.drawText(text, 0, text.length(), variableParams.currentLeft, variableParams.currentBaseline, textPaint);
            variableParams.currentLeft += textWidth;
        }

        if (styleParams != null) {
            restorePaint(textPaint, oldFColor, oldUnderline, oldFakeBold, textSkewX, oldTextSize, oldType);
        }
    }

    private static void drawBack(@NonNull Canvas canvas, @NonNull TextPaint backPaint, int width,
                                 @NonNull VariableParams variableParams, @NonNull ImmutableParams immutableParams) {
        final int[] linesHeight = immutableParams.lineInfos;
        int lh = 0;
        if (linesHeight != null) {
            lh = LineUtils.getLineHeight(linesHeight, variableParams.currentDrawLine);
        }
        lh = lh < 0 ? 0 : lh;
        final int oldColor = backPaint.getColor();
        backPaint.setColor(backPaint.bgColor);
        canvas.drawRect(variableParams.currentLeft, variableParams.currentTop,
                variableParams.currentLeft + width, variableParams.currentTop + lh, backPaint);
        backPaint.setColor(oldColor);
    }

    private static void setPaintByStyleParams(@NonNull TextPaint paint, @NonNull SpecialStyleParams params) {
        if (params.hasFColor)
            paint.setColor(params.foregroundColor);
        if (params.hasTextSize)
            paint.setTextSize(params.textSize);
        paint.setUnderlineText(params.underline);
        int type = Typeface.NORMAL;
        if (params.bold) {
            if (params.italic) {
                type = Typeface.BOLD_ITALIC;
            } else {
                type = Typeface.BOLD;
            }
        } else if (params.italic) {
            type = Typeface.ITALIC;
        }
        apply(paint, type);
    }

    public static void apply(Paint paint, int style) {
        int oldStyle;
        Typeface old = paint.getTypeface();
        if (old == null) {
            oldStyle = 0;
        } else {
            oldStyle = old.getStyle();
        }
        @SuppressLint("WrongConstant") int want = oldStyle | style;
        Typeface tf;
        if (old == null) {
            tf = Typeface.defaultFromStyle(want);
        } else {
            tf = Typeface.create(old, want);
        }
        int fake = want & ~tf.getStyle();
        if ((fake & Typeface.BOLD) != 0) {
            paint.setFakeBoldText(true);
        }
        if ((fake & Typeface.ITALIC) != 0) {
            paint.setTextSkewX(-0.25f);
        }
        paint.setTypeface(tf);
    }

    public static void clear(Paint paint, int style) {
        int oldStyle;

        Typeface old = paint.getTypeface();
        if (old == null) {
            oldStyle = 0;
        } else {
            oldStyle = old.getStyle();
        }
        @SuppressLint("WrongConstant") int want = oldStyle & (~style);
        Typeface tf;
        if (old == null) {
            tf = Typeface.defaultFromStyle(want);
        } else {
            tf = Typeface.create(old, want);
        }
        if ((style & Typeface.BOLD) != 0) {
            paint.setFakeBoldText(false);
        }
        if ((style & Typeface.ITALIC) != 0) {
            paint.setTextSkewX(0);
        }
        paint.setTypeface(tf);
    }

    private static void restorePaint(@NonNull TextPaint paint, int oldFColor, boolean oldUnderline,
                                     boolean oldFakeBold, float textSkewX, float textSize, Typeface oldType) {
        paint.setColor(oldFColor);
        paint.setUnderlineText(oldUnderline);
        paint.setTypeface(oldType);
        paint.setFakeBoldText(oldFakeBold);
        paint.setTextSkewX(textSkewX);
        paint.setTextSize(textSize);
        paint.bgColor = Color.TRANSPARENT;
    }

    /**
     * 绘制换行符
     * @param canvas
     * @param variableParams    可变参数
     *                          @see VariableParams
     * @param immutableParams   不可变参数
     *                          @see ImmutableParams
     */
    public static void drawNextLine(Canvas canvas,
                                    @NonNull VariableParams variableParams, @NonNull ImmutableParams immutableParams) {
        final int needDrawLines = immutableParams.needDrawLine;
        if (variableParams.currentDrawLine + 1 == needDrawLines) {
            drawEllipsize(canvas, variableParams, immutableParams);
        } else {
            toNewDrawLine(variableParams, immutableParams);
        }
    }

    /**
     * 还能绘制的最大宽度，需取绝对值
     * @param variableParams
     * @param immutableParams
     * @return  若值小于0，说明可能需要绘制省略号
     */
    public static int getDrawMaxWidthFronNow(@NonNull VariableParams variableParams, @NonNull ImmutableParams immutableParams) {
        final int right = immutableParams.right;
        final int needDrawLines = immutableParams.needDrawLine;
        final TextUtils.TruncateAt ellipsize = immutableParams.truncateAt;
        final int ellipsizeLength = immutableParams.ellipsizeLength;
        int maxWidth = right - variableParams.currentLeft;
        if (ellipsize != null && (variableParams.currentDrawLine + 1) == needDrawLines) {
            maxWidth -= ellipsizeLength;
            maxWidth = -maxWidth;
        }
        return maxWidth;
    }

    /**
     * 绘制省略号
     * @param canvas
     * @param variableParams
     * @param immutableParams
     */
    public static void drawEllipsize(Canvas canvas,
                                      @NonNull VariableParams variableParams, @NonNull ImmutableParams immutableParams) {
        final TextPaint textPaint = immutableParams.paint;
        canvas.drawText(immutableParams.ellipsizeText, variableParams.currentLeft, variableParams.currentBaseline, textPaint);
        variableParams.currentLeft += immutableParams.ellipsizeLength;
        variableParams.isDrawEndEllipsize = true;
    }

    /**
     * 控制换行
     */
    private static void toNewDrawLine(@NonNull VariableParams variableParams, @NonNull ImmutableParams immutableParams) {
        variableParams.currentDrawLine++;
        final TextUtils.TruncateAt ellipsize = immutableParams.truncateAt;
        final int[] lineInfos = immutableParams.lineInfos;
        int lh = 0;
        if (lineInfos != null) {
            int lastBaseLine = LineUtils.getBaseLine(lineInfos, variableParams.currentDrawLine - 1);
            int nextBaseLine = LineUtils.getBaseLine(lineInfos, variableParams.currentDrawLine);
            nextBaseLine = nextBaseLine < 0 ? 0 : nextBaseLine;
            int lastHeight = LineUtils.getLineHeight(lineInfos, variableParams.currentDrawLine - 1);
            lastHeight = lastHeight < 0 ? 0 : lastHeight;
            lh = lastHeight + nextBaseLine - lastBaseLine;
        }
        final int addOn = immutableParams.lineSpace;
        if (ellipsize != null) {
            variableParams.currentBaseline += lh + addOn;
        } else {
            variableParams.currentBaseline += lh + addOn;
        }
        variableParams.currentLeft = immutableParams.left;
        variableParams.currentTop += lh + addOn;
    }

    /**
     * 计算纯文本的所占长度和行数
     * @param textPaint
     * @param text  纯文本
     * @param currentLeft   目前计算的位置
     * @param left          可绘制区域的左边坐标
     * @param right         可绘制区域的右边坐标
     * @param timeout       最大时长，若设置0，则不会有超时；若不为0，此次计算时长若大于timeout，则直接返回
     *                      @see MeasureTextUtils#STATE_ERROR
     * @return  位置信息
     *          @see MeasureTextUtils
     */
    public static long measureText(TextPaint textPaint, CharSequence text, int currentLeft, int left, int right, long timeout) {
        long flag = MeasureTextUtils.setLines(
                            MeasureTextUtils.setCurrentLeft(0, currentLeft),
                        1);
        final float[] widths = new float[text.length()];
        textPaint.getTextWidths(text.toString(), widths);
        if (timeout > 0) {
            flag = MeasureTextUtils.measureText(flag, left, right, widths, timeout);
        } else {
            flag = MeasureTextUtils.measureTextByNative(flag, left, right, widths);
        }
        return flag;
    }

    public static long measureFixWidth(int width, int currentLeft, int left, int right) {
        long flag = MeasureTextUtils.setLines(MeasureTextUtils.setCurrentLeft(0, currentLeft), 1);
        if (currentLeft + width > right) {
            flag = MeasureTextUtils.gotoCalNextLine(flag, left);
        }
        return MeasureTextUtils.calContentMaxWidth(MeasureTextUtils.setCurrentLeft(flag, MeasureTextUtils.getCurrentLeft(flag) + width), left);
    }

    public static long measureText(@NonNull ImmutableParams immutableParams, IDrawableBlockList list, int drawableSize,
                                   int currentLeft, int left, int right, boolean includePad, boolean forceMeasure) {
        if (!list.hasDrawable()) {
            drawableSize = 0;
        }
        BlockMeasureParams params = BlockMeasureParams.obtain()
                .setLineInfo(getLineInfo(immutableParams.paint, drawableSize, includePad))
                .setDrawableSize(drawableSize)
                .setCurrentLeft(currentLeft)
                .setCurrentTop(0)
                .setLeft(left)
                .setRight(right)
                .setIncludePad(includePad)
                .setForceMeasure(forceMeasure);
        return list.measure(params, immutableParams);
    }

    /**
     * 计算一行文本高度及第一行文本baseline
     * @param paint
     * @param drawableSize
     * @param includePad include font padding
     * @return 第一行baseline
     */
    public static int getLineInfo(TextPaint paint, int drawableSize, boolean includePad) {
        Paint.FontMetricsInt fontMetricsInt = getFontMetricsInt();
        paint.getFontMetricsInt(fontMetricsInt);
        int top = getFontHeightCalTop(fontMetricsInt, includePad);
        int bot = getFontHeightCalBottom(fontMetricsInt, includePad);
        int fh = bot - top;
        int baseLine = -top;
        if (fh < drawableSize) {
            baseLine += (drawableSize - fh) >> 1;
            fh = drawableSize;
        }
        return LineUtils.combime(fh, baseLine);
    }

    public static int getFontHeight(TextPaint paint, boolean includePad) {
        Paint.FontMetricsInt fontMetricsInt = getFontMetricsInt();
        paint.getFontMetricsInt(fontMetricsInt);
        return getFontHeight(fontMetricsInt, includePad);
    }

    public static int getFontHeight(Paint.FontMetricsInt fontMetricsInt, boolean includePad) {
        return getFontHeightCalBottom(fontMetricsInt, includePad) - getFontHeightCalTop(fontMetricsInt, includePad);
    }

    public static int getFontHeightCalTop(Paint.FontMetricsInt fontMetricsInt, boolean includePad) {
        return includePad ? fontMetricsInt.top : fontMetricsInt.ascent;
    }

    public static int getFontHeightCalBottom(Paint.FontMetricsInt fontMetricsInt, boolean includePad) {
        return includePad ? fontMetricsInt.bottom : fontMetricsInt.descent;
    }

    public static int getEllipsizeLength(TextPaint paint, String ellipsizeText) {
        return (int) Math.ceil(paint.measureText(ellipsizeText));
    }

    private static long nano() {
        return System.nanoTime();
    }

    private static long logCast(String pre, long start) {
        long nano = nano();
        Log.d(TAG, pre + (nano - start));
        return nano;
    }
}
