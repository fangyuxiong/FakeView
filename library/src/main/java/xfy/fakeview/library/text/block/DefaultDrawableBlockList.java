package xfy.fakeview.library.text.block;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import xfy.fakeview.library.text.param.ClickSpanBlockInfo;
import xfy.fakeview.library.text.param.ImmutableParams;
import xfy.fakeview.library.text.param.VariableParams;
import xfy.fakeview.library.text.utils.LineUtils;
import xfy.fakeview.library.text.utils.MeasureTextUtils;
import xfy.fakeview.library.text.utils.SimpleGravity;

/**
 * Created by XiongFangyu on 2018/3/2.
 */
public class DefaultDrawableBlockList extends ArrayList<DefaultDrawableBlock> implements IDrawableBlockList<DefaultDrawableBlock> {
    private static final String TAG = "Fake--BlockList";

    private int mStart;
    private int mEnd;
//    private int mNewLineCount;
//    private int mDrawableCount;
//    private int mSpecialDrawableCount;
    private boolean hasSpan = false;
    private boolean hasNeedSetCallbackBlock;

    private long lastFlag = 0;
    private int lastDrawableSize = 0;
    private int lastCLeft = 0;
    private int lastLeft = 0;
    private int lastRight = 0;
    private float lastTextSize;
    private boolean canSaveToCache = true;

    private static final int DEFAULT_EXPAND_SIZE = 10;
    private int[] lineFlags;
    private int lines = 0;

    private volatile int useCount = 0;

    private static final int DEFAULT_SIZE = 10;
    private static final List<DefaultDrawableBlockList> cache;
    private static final int NOT_RECYCLE_COUNT = -1;
    static {
        cache = new ArrayList<>(DEFAULT_SIZE);
        for (int i = 0; i < DEFAULT_SIZE; i ++) {
            cache.add(new DefaultDrawableBlockList());
        }
    }

    public synchronized static DefaultDrawableBlockList obtain(int start, int end) {
        if (cache.isEmpty())
            return new DefaultDrawableBlockList(start, end);
        DefaultDrawableBlockList list = cache.remove(0);
        list.mStart = start;
        list.mEnd = end;
        return list;
    }

    private synchronized static void putToCache(DefaultDrawableBlockList list) {
        cache.add(list);
    }

    private void recycle() {
        lines = 0;
        lineFlags = null;
        hasSpan = false;
        hasNeedSetCallbackBlock = false;
//        mNewLineCount = 0;
//        mDrawableCount = 0;
//        mSpecialDrawableCount = 0;
        lastFlag = 0;
        lastDrawableSize = 0;
        lastCLeft = 0;
        lastLeft = 0;
        lastRight = 0;
        for (int i = 0, l = size(); i < l;i ++) {
            DefaultDrawableBlock block = get(i);
            if (block == null)
                continue;
            block.recycle();
        }
        clear();
        putToCache(this);
    }

    @Override
    public synchronized void notUse() {
        if (useCount < 0) {
            useCount ++;
        } else {
            useCount --;
        }
        if (useCount == 0)
            recycle();
    }

    @Override
    public synchronized void doNotRecycle() {
        if (useCount >= 0) {
            useCount = NOT_RECYCLE_COUNT - useCount;
        }
    }

    @Override
    public synchronized void canRecycle() {
        if (useCount <= NOT_RECYCLE_COUNT) {
            useCount = NOT_RECYCLE_COUNT - useCount;
        }
        if (useCount == 0)
            recycle();
    }

    @Override
    public boolean onTouchEvent(@NonNull View v, MotionEvent event, @NonNull ImmutableParams immutableParams) {
        if (!hasSpan() || immutableParams.clickSpanBlockInfos == null || immutableParams.clickSpanBlockInfos.size() == 0)
            return false;
        for (int i = 0, l = immutableParams.clickSpanBlockInfos.size(); i < l;i ++) {
            ClickSpanBlockInfo blockInfo = immutableParams.clickSpanBlockInfos.get(i);
            if (blockInfo != null && blockInfo.block != null) {
                if (blockInfo.block.onTouchEvent(v, event, immutableParams, blockInfo)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean draw(Canvas canvas, @NonNull VariableParams variableParams, @NonNull ImmutableParams immutableParams) {
        boolean result = true;
        canvas.save();
        traslateCanvas(canvas, immutableParams);
        for (int i = 0, l = size(); i < l; i ++) {
            result |= get(i).draw(canvas, variableParams, immutableParams);
        }
        canvas.restore();
        return result;
    }

    @Override
    public void addCallback(Drawable.Callback callback) {
        if (!hasNeedSetCallbackBlock)
            return;
        for (int i = 0, l = size(); i < l; i ++) {
            IDrawableBlock block = get(i);
            if (block.getType() == IDrawableBlock.NEED_SET_CALLBACK_DRAWABLE) {
                block.addCallback(callback);
            } else if (block.getType() == IDrawableBlock.SPAN) {
                block.getChildren().addCallback(callback);
            }
        }
    }

    @Override
    public void removeCallback(Drawable.Callback callback) {
        if (!hasNeedSetCallbackBlock)
            return;
        for (int i = 0, l = size(); i < l; i ++) {
            IDrawableBlock block = get(i);
            if (block.getType() == IDrawableBlock.NEED_SET_CALLBACK_DRAWABLE) {
                block.removeCallback(callback);
            } else if (block.getType() == IDrawableBlock.SPAN) {
                block.getChildren().removeCallback(callback);
            }
        }
    }

    private void traslateCanvas(Canvas canvas, ImmutableParams params) {
        final int textWidth = MeasureTextUtils.getMaxWidth(params.blockFlag);
        final int textHeight = getAllLineHeight(params);
        if (textHeight == 0)
            return;
        final int gravity = params.gravity;
        long flag = SimpleGravity.apply(gravity, params.left, params.top, params.right, params.bottom, textWidth, textHeight);
        int tx = SimpleGravity.getLeft(flag) - params.left;
        int ty = SimpleGravity.getTop(flag) - params.top;
        if (tx == 0 && ty == 0)
            return;
        canvas.translate(tx, ty);
    }

    private int getAllLineHeight(ImmutableParams params) {
        final int[] lineInfo = params.lineInfos;
        if (lineInfo == null)
            return 0;
        final int needDrawLine = params.needDrawLine;
        final int space = params.lineSpace;
        return LineUtils.getAllLineHeight(lineInfo, needDrawLine) + (needDrawLine - 1) * space;
    }

    @Override
    public synchronized void use() {
        if (useCount < 0) {
            useCount --;
        } else {
            useCount ++;
        }
    }

    private DefaultDrawableBlockList(int start, int end) {
        mStart = start;
        mEnd = end;
    }

    private DefaultDrawableBlockList() {}

    @Override
    public boolean canSaveToCache() {
        return canSaveToCache;
    }

    @Override
    public long getFlag() {
        return lastFlag;
    }

    @Override
    public int getStart() {
        return mStart;
    }

    @Override
    public int getEnd() {
        return mEnd;
    }

//    @Override
//    public int getNewLineCount() {
//        return mNewLineCount;
//    }

//    @Override
//    public int getDrawableCount() {
//        return mDrawableCount;
//    }
//
//    @Override
//    public int getSpecialDrawableCount() {
//        return mSpecialDrawableCount;
//    }

    @Override
    public boolean hasSpan() {
        return hasSpan;
    }

    @Override
    public int[] getLinesHeight() {
        return lineFlags;
    }

    @Override
    public int getLineHeightSize() {
        return lines;
    }

    @Override
    public long measure(BlockMeasureParams measureParams, @NonNull ImmutableParams immutableParams) {
        final int lineInfo = measureParams.lineInfo;
        final int drawableSize = measureParams.drawableSize;
        int currentLeft = measureParams.currentLeft;
        int currentTop = measureParams.currentTop;
        final int left = measureParams.left;
        final int right = measureParams.right;
        if (!checkNeedMeasure(measureParams, immutableParams)) {
            return lastFlag;
        }
        long flag = MeasureTextUtils.setLines(0, 1);
        int len = size();
        for (int i = 0; i < len; i ++) {
            IDrawableBlock block = get(i);
            if (block == null)
                continue;
            measureParams.currentLeft = currentLeft;
            measureParams.currentTop = currentTop;
            long bf = block.measure(measureParams, immutableParams);
            int state = bf == 0 ? MeasureTextUtils.STATE_ERROR : MeasureTextUtils.getState(bf);
            if (state == MeasureTextUtils.STATE_SUCCESS) {
                int cl = MeasureTextUtils.getLines(flag);
                currentLeft = MeasureTextUtils.getCurrentLeft(bf);
                int blines = MeasureTextUtils.getLines(bf);
                boolean drawOnFirstLine = MeasureTextUtils.willDrawOnFirstLine(bf) || blines == 1;
                int lineHeight = MeasureTextUtils.getMaxHeight(bf);
                lineHeight = lineHeight == 0 ? LineUtils.getLineHeight(lineInfo) : lineHeight;
                //相当于index + 1,所以不减
                int lineStart = cl;
                int lh = getLastLineHeight();
                if (drawOnFirstLine) {
                    //需要覆盖上一行的高度，所以要减
                    if (lineHeight > lh) {
                        lineStart --;
                        saveLineHeight(lineHeight, block.getBaseLine(), lineStart, lineStart + blines - 1);
                    } else if (blines > 1){
                        saveLineHeight(lineHeight, block.getBaseLine(), lineStart, lineStart + blines - 2);
                    }
                } else if (blines > 1) {
                    saveLineHeight(lineHeight, block.getBaseLine(), lineStart, lineStart + blines - 2);
                }
                if (blines > 1) {
                    currentTop += (blines - 1) * lh;
                }
                flag = MeasureTextUtils.setLines(flag, cl + blines - 1);
                flag = MeasureTextUtils.setMaxWidth(flag, Math.max(MeasureTextUtils.getMaxWidth(flag), MeasureTextUtils.getMaxWidth(bf)));
                flag = MeasureTextUtils.setCurrentLeft(flag, currentLeft);
                flag = MeasureTextUtils.setMaxHeight(flag, Math.max(MeasureTextUtils.getMaxHeight(flag), lineHeight));
            } else {
                MeasureTextUtils.setState(flag, state);
            }
        }
        lastFlag = flag;
        lastDrawableSize = drawableSize;
        lastCLeft = currentLeft;
        lastLeft = left;
        lastRight = right;
        lastTextSize = immutableParams.paint.getTextSize();
        return flag;
    }

    private boolean checkNeedMeasure(BlockMeasureParams measureParams, @NonNull ImmutableParams immutableParams) {
        if (measureParams.drawableSize == lastDrawableSize
                && lastCLeft == measureParams.currentLeft
                && lastLeft == immutableParams.left
                && lastRight == immutableParams.right
                && lastTextSize == immutableParams.paint.getTextSize()
                && lastFlag != 0)
            return false;
        return true;
    }

    private int getLastLineHeight() {
        final int len = lineFlags != null ? lines : 0;
        if (len == 0 || lines >= lineFlags.length)
            return 0;
        return LineUtils.getLineHeight(lineFlags[len - 1]);
    }

    private void saveLineHeight(int height, int baseLine, int start, int end) {
        initLineHeight(end);
        for (int i = start; i <= end; i ++) {
            lineFlags[i] = LineUtils.combime(height, baseLine);
        }
        lines = end + 1;
    }

    private void initLineHeight(int endIndex) {
        if (lines <= endIndex) {
            int needLen = lines + DEFAULT_EXPAND_SIZE;
            while (needLen <= endIndex) {
                needLen += DEFAULT_EXPAND_SIZE;
            }
            int[] temp = new int[needLen];
            if (lines > 0)
                System.arraycopy(lineFlags, 0, temp, 0, lines);
            lineFlags = temp;
        }
    }

    @Override
    public boolean add(DefaultDrawableBlock block) {
        if (!super.add(block))
            return false;
        switch (block.getType()) {
            case IDrawableBlock.NEED_SET_CALLBACK_DRAWABLE:
                hasNeedSetCallbackBlock = true;
                break;
//            case IDrawableBlock.DRAWABLE:
//                mDrawableCount ++;
//                break;
//            case IDrawableBlock.NEXTLINE:
//                mNewLineCount ++;
//                break;
//            case IDrawableBlock.SPECIAL_DRAWABLE:
//                mSpecialDrawableCount ++;
//                break;
            case IDrawableBlock.SPAN:
                hasSpan = true;
                if (!hasNeedSetCallbackBlock) {
                    hasNeedSetCallbackBlock = block.getChildren().hasNeedSetCallbackBlock;
                }
                if (canSaveToCache) {
                    canSaveToCache = block.canSaveToCache();
                }
//                mDrawableCount += block.getChildren().getNewLineCount();
//                mNewLineCount += block.getChildren().getNewLineCount();
//                mSpecialDrawableCount += block.getChildren().getSpecialDrawableCount();
                break;
        }
        return true;
    }

    private long now() {
        return System.nanoTime();
    }

    private long logCast(String pre, long start) {
        long now = now();
        Log.d(TAG, pre + (now - start));
        return now;
    }

}
