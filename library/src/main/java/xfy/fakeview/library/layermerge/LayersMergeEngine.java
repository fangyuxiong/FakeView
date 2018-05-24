package xfy.fakeview.library.layermerge;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import xfy.fakeview.library.DebugInfo;

/**
 * Created by XiongFangyu on 2017/11/10.
 *
 * An Engine for merging layers, which can be paused.
 * Usually this is used in a list view(ListView or RecyclerView),
 * pause mergine when list view is scrolling.
 *
 * @see #pause()
 * @see #resume()
 * @see #addMergeAction(Object, FrameLayout)
 * @see #addMergeAction(Object, FrameLayout, int)
 * @see #removeMergeActionByTag(Object)
 * @see #removeAllAction()
 */
public class LayersMergeEngine implements OnExtractViewGroupListener, MergeStatusListener {
    private static final String TAG = "LayersMergeEngine";
    private static volatile LayersMergeEngine engine;
    private static final int DELAY = 16;

    private LayersMergeEngine() {
        mScheduler = new Scheduler("LayersMergeEngineScheduler");
        mScheduler.start();
        mergeActions = new HashMap<>();
        mainHandler = new Handler(Looper.getMainLooper());
        removeTags = new ArrayList<>();
    }

    public static LayersMergeEngine getEngine() {
        if (engine == null) {
            synchronized (LayersMergeEngine.class) {
                if (engine == null) {
                    engine = new LayersMergeEngine();
                }
            }
        }
        return engine;
    }

    private final Handler mainHandler;
    private final Scheduler mScheduler;
    private final HashMap<Object, ArrayList<LayoutData>> mergeActions;
    private boolean mPause = false;
    private int mergingLayoutHashcode = -1;
    private boolean merging = false;
    private final ArrayList<Object> removeTags;
    private ArrayList<OnExtractViewGroupListener> onExtractViewGroupListeners;
    private ArrayList<MergeStatusListener> mergeStatusListeners;

    public synchronized void pause() {
        mPause = true;
    }

    public synchronized void resume() {
        mPause = false;
        if (!merging)
            mScheduler.post(new NextAction());
    }

    public synchronized static void release() {
        if (engine != null) {
            synchronized (engine) {
                engine.pause();
                engine.removeAllAction();
                engine.mScheduler.quit();
                engine.mainHandler.removeCallbacksAndMessages(null);
            }
            engine = null;
        }
    }

    /**
     * Add a merge action with none extracing info.
     *
     * @see LayersMergeManager#extractFlag
     *
     * @param tag for searching this action
     *            @see #removeMergeActionByTag(Object)
     * @param layout needed to merging
     * @return true: add to scheduler, false otherwise
     */
    public boolean addMergeAction(Object tag, FrameLayout layout) {
        return addMergeAction(tag, layout, 0);
    }

    /**
     * Add a merge action with custom extracting info.
     *
     * @see LayersMergeManager#extractFlag
     *
     * @param tag for searching this action
     *            @see #removeMergeActionByTag(Object)
     * @param layout needed to merging
     * @param extractInfo custom extracing info
     * @return true: add to scheduler, false otherwise
     */
    public boolean addMergeAction(Object tag, FrameLayout layout, int extractInfo) {
        return addMergeAction(new LayoutData(tag, layout).withExtractInfo(extractInfo));
    }

    /**
     * Add a merge action
     *
     * @param layoutData contain layout, tag, extract info etc..
     *                   @see LayoutData
     * @return true: add to scheduler, false otherwise
     */
    public synchronized boolean addMergeAction(LayoutData layoutData) {
        final FrameLayout layout = layoutData.layout;
        if (!LayersMergeManager.needMerge(layout))
            return false;
        final Object tag = layoutData.tag;
        removeTags.remove(tag);
        ArrayList<LayoutData> list = mergeActions.get(tag);
        if (list == null) {
            list = new ArrayList<>();
            mergeActions.put(tag, list);
        }
        if (list.contains(layout) || layout.hashCode() == mergingLayoutHashcode)
            return true;
        list.add(layoutData);
        if (!merging)
            mScheduler.post(new NextAction());
        return true;
    }

    /**
     * Remove merge action by tag.
     * If action is processing, it cannot be removed.
     * @param tag for searching this action
     */
    public synchronized void removeMergeActionByTag(Object tag) {
        releaseLayoutDataList(mergeActions.remove(tag));
        removeTags.add(tag);
    }

    /**
     * Remove all unprocessing merge actions.
     */
    public synchronized void removeAllAction() {
        for (Object k : mergeActions.keySet()) {
            releaseLayoutDataList(mergeActions.get(k));
        }
        removeTags.addAll(mergeActions.keySet());
        mergeActions.clear();
    }

    public void addOnExtractViewGroupListener(OnExtractViewGroupListener listener) {
        if (onExtractViewGroupListeners == null) {
            onExtractViewGroupListeners = new ArrayList<>();
        }
        if (!onExtractViewGroupListeners.contains(listener)) {
            onExtractViewGroupListeners.add(listener);
        }
    }

    public void removeOnExtractViewGroupListener(OnExtractViewGroupListener listener) {
        if (onExtractViewGroupListeners != null) {
            onExtractViewGroupListeners.remove(listener);
        }
    }

    public void clearOnExtractViewGroupListener() {
        if (onExtractViewGroupListeners != null) {
            onExtractViewGroupListeners.clear();
        }
    }

    public void addOnMergeFailedListener(MergeStatusListener listener) {
        if (mergeStatusListeners == null) {
            mergeStatusListeners = new ArrayList<>();
        }
        if (!mergeStatusListeners.contains(listener)) {
            mergeStatusListeners.add(listener);
        }
    }

    public void removeOnMergeFailedListener(MergeStatusListener listener) {
        if (mergeStatusListeners != null) {
            mergeStatusListeners.remove(listener);
        }
    }

    public void clearOnMergeFailedListener() {
        if (mergeStatusListeners != null) {
            mergeStatusListeners.clear();
        }
    }

    private void releaseLayoutDataList(List<LayoutData> data) {
        if (data != null) {
            for (LayoutData d : data) {
                if (d != null) {
                    d.removeListener();
                }
            }
            data.clear();
        }
    }

    /**
     * Schedule next action.
     */
    private synchronized void scheduleNext() {
        if (mPause || merging || mergeActions.isEmpty()) {
            if (DebugInfo.DEBUG) {
                Log.d(TAG, "merging: " + merging + " pause: " + mPause + " or empty");
            }
            return;
        }
        Set<Object> keys = mergeActions.keySet();
        if (keys.isEmpty()) {
            if (DebugInfo.DEBUG)
                Log.d(TAG, "keys is empty");
            return;
        }
        if (!removeTags.isEmpty()) {
            for (Object tag : removeTags) {
                releaseLayoutDataList(mergeActions.remove(tag));
            }
            removeTags.clear();
        }
        LayoutData layoutData = null;
        ArrayList<LayoutData> list = null;
        Object key = null;
        for (Object k : keys) {
            list = mergeActions.get(k);
            if (list == null || list.isEmpty())
                continue;
            for (LayoutData l : list) {
                if (l == null)
                    continue;
                layoutData = l;
                break;
            }
            if (layoutData != null) {
                key = k;
                break;
            }
        }
        if (layoutData == null) {
            if (DebugInfo.DEBUG) {
                Log.d(TAG, "no layout to merge");
            }
            return;
        }
        if (mPause)
            return;
        if (mScheduler.post(new Action(layoutData))) {
            list.remove(layoutData);
            if (list.isEmpty())
                mergeActions.remove(key);
            mergingLayoutHashcode = layoutData.hashCode();
            merging = true;
        } else {
            if (DebugInfo.DEBUG) {
                Log.d(TAG, "have not created handler yet");
            }
            mScheduler.post(new NextAction());
        }
    }

    @Override
    public Result onExtract(ViewGroup src) {
        if (onExtractViewGroupListeners != null) {
            ArrayList<OnExtractViewGroupListener> temp = new ArrayList<>(onExtractViewGroupListeners);
            for (int i = 0, l = temp.size(); i < l ; i ++) {
                OnExtractViewGroupListener listener = temp.get(i);
                Result result = listener.onExtract(src);
                if (result != null && result.valid())
                    return result;
            }
        }
        return null;
    }

    @Override
    public void onMergeFailed(LayoutData layout, int failTimes, int type) {
        if (mergeStatusListeners != null) {
            ArrayList<MergeStatusListener> temp = new ArrayList<>(mergeStatusListeners);
            for (int i = 0, l = temp.size(); i < l;i ++) {
                temp.get(i).onMergeFailed(layout, failTimes, type);
            }
        }
    }

    @Override
    public void onMergeSuccess(LayoutData layout) {
        if (mergeStatusListeners != null) {
            ArrayList<MergeStatusListener> temp = new ArrayList<>(mergeStatusListeners);
            for (int i = 0, l = temp.size(); i < l;i ++) {
                temp.get(i).onMergeSuccess(layout);
            }
        }
    }

    @Override
    public void onMergeStart(LayoutData layout) {
        if (mergeStatusListeners != null) {
            ArrayList<MergeStatusListener> temp = new ArrayList<>(mergeStatusListeners);
            for (int i = 0, l = temp.size(); i < l;i ++) {
                temp.get(i).onMergeStart(layout);
            }
        }
    }

    private class NextAction implements Runnable {
        @Override
        public void run() {
            mScheduler.clearActions();
            scheduleNext();
        }
    }

    /**
     * Merge action
     * run in thread
     */
    private class Action implements Runnable {
        Object lock;
        LayoutData layout;
        Action(LayoutData layout) {
            this.layout = layout;
            lock = new Object();
            if (layout.onExtractViewGroupListener == null) {
                layout.onExtractViewGroupListener = LayersMergeEngine.this;
            }
            if (layout.mergeStatusListener == null) {
                layout.mergeStatusListener = LayersMergeEngine.this;
            }
        }
        @Override
        public void run() {
            if (layout == null || layout.layout == null) {
                postNext();
                return;
            }
            //check the view has been through one layout
            boolean canMerge = checkCanMerge();
            if (DebugInfo.DEBUG) {
                Log.d(TAG, "run action: " + layout + " canMerge: " + canMerge);
            }
            //if not, add merge action and schedule next
            if (!canMerge) {
                onCannotMerge(true, MergeStatusListener.FAILED_TYPE_NOT_READY);
                return;
            }
            final FrameLayout src = layout.layout;
            final Object tag = layout.tag;
            final int info = layout.extractInfo;
            final OnExtractViewGroupListener listener = layout.onExtractViewGroupListener;
            final int mzl = layout.maxZeroLocCountWhenExtracting;
            final MergeStatusListener statusListener = layout.mergeStatusListener;

            //do merge action by LayersMergeManager in main thread.
            mainHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!removeTags.contains(tag)) {
                        if (statusListener != null)
                            statusListener.onMergeStart(layout);
                        LayersMergeManager manager = new LayersMergeManager(src, info, mzl, listener);
                        if (!manager.mergeChildrenLayers()) {
                            layout.failTimes++;
                            onCannotMerge(false, MergeStatusListener.FAILED_TYPE_TOO_MANY_ZERO_VIEWS);
                        } else if (statusListener != null) {
                            statusListener.onMergeSuccess(layout);
                        }
                    }
                    synchronized (lock) {
                        lock.notifyAll();
                    }
                }
            }, DELAY);
            //wait for merge action
            synchronized (lock) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    if (DebugInfo.DEBUG)
                        e.printStackTrace();
                }
            }
            if (DebugInfo.DEBUG) {
                Log.d(TAG, "action done: " + layout);
            }
            postNext();
        }

        private void postNext() {
            mergingLayoutHashcode = -1;
            merging = false;
            mScheduler.postDelay(new NextAction(), DELAY);
        }

        private void onCannotMerge(boolean postNext, int type) {
            if (layout.mergeStatusListener != null) {
                layout.mergeStatusListener.onMergeFailed(layout, layout.failTimes, type);
            }
            final Object tag = layout.tag;
            if (!removeTags.contains(tag)) {
                if (layout.failTimes < layout.maxFailTimes) {
                    addMergeAction(layout);
                } else {
                    layout.removeListener();
                }
            } else {
                removeTags.remove(tag);
            }
            if (postNext)
                postNext();
        }

        private boolean checkCanMerge() {
            boolean laidout = checkViewIsLaidOut();
            if (!laidout) {
                layout.failTimes++;
            }
            return laidout;
        }

        private boolean checkViewIsLaidOut() {
//            return LayersMergeManager.isReadyToMerge(layout.layout, 0, layout.maxNotReadyCount);
            return layout.canMerge();
        }
    }

    private class Scheduler extends HandlerThread {
        private Handler mHandler;
        private Looper myLooper;
        public Scheduler(String name) {
            super(name);
        }

        @Override
        protected void onLooperPrepared() {
            mHandler = new Handler();
            myLooper = Looper.myLooper();
        }

        boolean post(Runnable action) {
            if (mHandler != null) {
                return mHandler.post(action);
            }
            return false;
        }

        boolean postDelay(Runnable action, long delay) {
            if (mHandler != null) {
                return mHandler.postDelayed(action, delay);
            }
            return false;
        }

        void clearActions() {
            if (mHandler != null)
                mHandler.removeCallbacksAndMessages(null);
        }

        boolean removeAction(Runnable action) {
            if (mHandler != null) {
                mHandler.removeCallbacks(action);
                return true;
            }
            return false;
        }

        boolean checkLooper() {
            return Looper.myLooper() == myLooper;
        }
    }
}
