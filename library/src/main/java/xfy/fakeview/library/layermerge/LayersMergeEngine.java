package xfy.fakeview.library.layermerge;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.HashMap;
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
public class LayersMergeEngine {
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
        if (!LayersMergeManager.needMerge(layout))
            return false;
        removeTags.remove(tag);
        ArrayList<LayoutData> list = mergeActions.get(tag);
        if (list == null) {
            list = new ArrayList<>();
            mergeActions.put(tag, list);
        }
        if (list.contains(layout) || layout.hashCode() == mergingLayoutHashcode)
            return true;
        list.add(new LayoutData(tag, layout, extractInfo));
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
        ArrayList<LayoutData> list = mergeActions.remove(tag);
        if (list == null)
            return;
        list.clear();
        removeTags.add(tag);
    }

    /**
     * Remove all unprocessing merge actions.
     */
    public synchronized void removeAllAction() {
        for (Object k : mergeActions.keySet()) {
            ArrayList<LayoutData> list = mergeActions.get(k);
            if (list != null)
                list.clear();
        }
        removeTags.addAll(mergeActions.keySet());
        mergeActions.clear();
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
                mergeActions.remove(tag);
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

    private class NextAction implements Runnable {
        @Override
        public void run() {
            scheduleNext();
        }
    }

    /**
     * Merge action
     */
    private class Action implements Runnable {
        Object lock;
        LayoutData layout;
        Action(LayoutData layout) {
            this.layout = layout;
            lock = new Object();
        }
        @Override
        public void run() {
            if (layout == null || layout.layout == null)
                return;
            //check the view has been through one layout
            final FrameLayout src = layout.layout;
            final Object tag = layout.tag;
            final int info = layout.extractInfo;
            boolean canMerge = src.getLeft() != 0 || src.getTop() != 0 || src.getRight() != 0 || src.getBottom() != 0;
            if (DebugInfo.DEBUG) {
                Log.d(TAG, "run action: " + layout + " canMerge: " + canMerge);
            }
            //if not, add merge action and schedule next
            if (!canMerge) {
                mergingLayoutHashcode = -1;
                merging = false;
                if (!removeTags.contains(tag)) {
                    addMergeAction(tag, src, info);
                } else {
                    removeTags.remove(tag);
                }
                mScheduler.postDelay(new NextAction(), DELAY);
                return;
            }
            //do merge action by LayersMergeManager in main thread.
            mainHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!removeTags.contains(tag)) {
                        LayersMergeManager manager = new LayersMergeManager(src, info);
                        manager.mergeChildrenLayers();
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
            mergingLayoutHashcode = -1;
            merging = false;
            mScheduler.postDelay(new NextAction(), DELAY);
        }
    }

    private static class LayoutData {
        FrameLayout layout;
        int extractInfo = 0;
        Object tag;

        LayoutData(Object tag, FrameLayout layout, int extractInfo) {
            this.tag = tag;
            this.layout = layout;
            this.extractInfo = extractInfo;
        }

        @Override
        public String toString() {
            return layout + " info: " + extractInfo + " " + tag;
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
