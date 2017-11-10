package xfy.fakeview.library.layermerge;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by XiongFangyu on 2017/11/10.
 */
public class LayersMergeEngine {
    private static final String TAG = "LayersMergeEngine";
    private static boolean DEBUG = false;
    public static void setDebug(boolean debug) {
        DEBUG = debug;
    }
    private static volatile LayersMergeEngine engine;

    private LayersMergeEngine() {
        mScheduler = new Scheduler("LayersMergeEngineScheduler");
        mScheduler.start();
        mergeActions = new HashMap<>();
        mainHandler = new Handler(Looper.getMainLooper());
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

    public synchronized void pause() {
        mPause = true;
    }

    public synchronized void resume() {
        mPause = false;
        if (!merging)
            mScheduler.post(new NextAction());
    }

    public boolean addMergeAction(Object tag, FrameLayout layout) {
        return addMergeAction(tag, layout, false);
    }

    public boolean addMergeAction(Object tag, FrameLayout layout, boolean createBackgroundView) {
        if (!LayersMergeManager.needMerge(layout))
            return false;
        ArrayList<LayoutData> list = mergeActions.get(tag);
        if (list == null) {
            list = new ArrayList<>();
            mergeActions.put(tag, list);
        }
        if (list.contains(layout) || layout.hashCode() == mergingLayoutHashcode)
            return true;
        list.add(new LayoutData(layout, createBackgroundView));
        if (!merging)
            mScheduler.post(new NextAction());
        return true;
    }

    public synchronized void removeMergeActionByTag(Object tag) {
        ArrayList<LayoutData> list = mergeActions.remove(tag);
        if (list == null)
            return;
        list.clear();
    }

    public synchronized void removeAllAction() {
        for (Object k : mergeActions.keySet()) {
            ArrayList<LayoutData> list = mergeActions.get(k);
            if (list != null)
                list.clear();
        }
        mergeActions.clear();
    }

    private synchronized void scheduleNext() {
        if (mPause || merging || mergeActions.isEmpty()) {
            if (DEBUG) {
                Log.d(TAG, "merging: " + merging + " pause: " + mPause + " or empty");
            }
            return;
        }
        Set<Object> keys = mergeActions.keySet();
        if (keys.isEmpty()) {
            if (DEBUG)
                Log.d(TAG, "keys is empty");
            return;
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
            if (DEBUG) {
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
            if (DEBUG) {
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
            if (DEBUG) {
                Log.d(TAG, "run action: " + layout);
            }
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (DEBUG) {
                        Log.d(TAG, "real run action: " + layout);
                    }
                    LayersMergeManager manager = new LayersMergeManager(layout.layout, layout.extracting);
                    manager.mergeChildrenLayers();
                    synchronized (lock) {
                        lock.notifyAll();
                    }
                }
            });
            synchronized (lock) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (DEBUG) {
                Log.d(TAG, "action done: " + layout);
            }
            mergingLayoutHashcode = -1;
            merging = false;
            mScheduler.postDelay(new NextAction(), 1);
        }
    }

    private static class LayoutData {
        FrameLayout layout;
        boolean extracting = false;
        LayoutData(FrameLayout layout, boolean extracting) {
            this.layout = layout;
            this.extracting = extracting;
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
