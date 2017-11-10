package xfy.fakeview.library.translator;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import xfy.fakeview.library.fview.FView;
import xfy.fakeview.library.fview.FViewParent;
import xfy.fakeview.library.fview.FViewRootImpl;
import xfy.fakeview.library.fview.IFView;
import xfy.fakeview.library.fview.IFViewGroup;
import xfy.fakeview.library.fview.IFViewRoot;
import xfy.fakeview.library.translator.data.DataTranslatorManager;
import xfy.fakeview.library.translator.event.OnClickListener;
import xfy.fakeview.library.translator.event.OnLongClickListener;

/**
 * Created by XiongFangyu on 2017/11/8.
 *
 * Translator whitch can translate a View Tree into a FView Tree.
 *
 * Translate View:
 * @see FViewTranslator
 * Translate LayoutParam:
 * @see FLayoutParamsTranslator
 * Translate View data:
 * @see DataTranslatorManager
 */
public class TranslatorManager {
    private static final String TAG = "TranslatorManager";
    /**
     * debug mode
     * true:
     *  when translating failed, throw Exception.
     * @see #setDebug(boolean)
     */
    private static boolean DEBUG = false;
    public static void setDebug(boolean debug) {
        DEBUG = debug;
    }
    private static Field mListenerInfoField;
    private static Field mOnClickListenerField, mOnLongClickListenerField;
    private Context mContext;
    private FViewRootImpl viewRoot;

    static {
        try {
            mListenerInfoField = View.class.getDeclaredField("mListenerInfo");
            mListenerInfoField.setAccessible(true);
        } catch (Throwable e) {
            mListenerInfoField = null;
        }
    }

    public TranslatorManager(Context context) {
        mContext = context;
    }

    /**
     * 将target view翻译成用{@link FView}构建的view树
     * 如果不是ViewGroup，没必要翻译
     *
     * Translate view tree from target into view tree build by FView
     * @param target view group is require, otherwise not necessary
     * @return IFViewRoot implement by FViewRootImpl, null for translating faild.
     */
    public IFViewRoot translateView(ViewGroup target) {
        if (target == null)
            throw new NullPointerException("targe cannot be null");
        ViewParent parent = target.getParent();
        if (parent == null || !(parent instanceof ViewGroup)) {
            throw new IllegalArgumentException("view must be added to view tree");
        }
        if (!FViewTranslator.canBeTranslated(target.getClass())) {
            if (DEBUG) {
                throw new IllegalArgumentException("view " + target.getClass().getName() +
                        " cannot be translated. please register by invoke FViewTranslator.registerTranslator()");
            }
            return null;
        }
        viewRoot = new FViewRootImpl(mContext);
        viewRoot.setLayoutParams(target.getLayoutParams());
        translateViewTree(target, viewRoot);

        ViewGroup p = (ViewGroup) parent;
        int index = -1;
        final int childCount = p.getChildCount();
        for (int i = 0; i < childCount ; i ++) {
            View v = p.getChildAt(i);
            if (v == target) {
                index = i;
                break;
            }
        }
        p.removeView(target);
        p.addView(viewRoot, index);

        return viewRoot;
    }

    /**
     * Translate view tree from target, and fill data.
     * if targe is ViewGroup, translate its children.
     * @param target target view tree root.
     * @param parent view tree parent, may be IFViewRoot or IFViewGroup
     *
     * @see FViewTranslator
     * @see DataTranslatorManager
     */
    private void translateViewTree(View target, FViewParent parent) {
        Class<? extends FView> tran = FViewTranslator.getTranslateClass(target.getClass());
        if (tran == null) {
            if (DEBUG) {
                throw new IllegalArgumentException("view " + target.getClass().getName() +
                    " cannot be translated. please register by invoke FViewTranslator.registerTranslator()");
            }
            return;
        }
        FView fView = newFView(tran);
        if (fView == null) {
            if (DEBUG) {
                throw new IllegalArgumentException("class " + tran.getName() +
                    " does not have the public constructor with 2 params (Context, IFViewRoot).");
            }
            return;
        }
        if (parent instanceof IFViewRoot) {
            ((IFViewRoot) parent).setTargetChild(fView);
            fView.setFLayoutParams(newMatchParentParams());
        } else {
            IFViewGroup p = (IFViewGroup) parent;
            fView.setFLayoutParams(translateLayoutParams(target.getLayoutParams()));
            p.addView(fView);
        }

        fView.setId(target.getId());
        fView.setPadding(target.getPaddingLeft(), target.getPaddingTop(), target.getPaddingRight(), target.getPaddingBottom());
        fView.setBackground(target.getBackground());
        translateEvent(fView, target);

        if (target instanceof ViewGroup) {
            ViewGroup layout = (ViewGroup) target;
            final int childCount = layout.getChildCount();
            for (int i = 0; i < childCount; i ++) {
                translateViewTree(layout.getChildAt(i), (FViewParent) fView);
            }
        } else {
            if (!DataTranslatorManager.translateData(fView, target)) {
                if (DEBUG) {
                    throw new IllegalArgumentException("view " + target.getClass().getName() +
                            " cannot be translated to " + tran.getName());
                }
                return;
            }
        }
    }

    /**
     * Translate old event(sp: click event, long click event) into FView event.
     * @param fView target FView
     * @param src source view
     */
    private void translateEvent(FView fView, View src) {
        fView.setOnClickListener(FViewOnClickListener.craete(getViewOnClickListener(src)));
        fView.setOnLongClickListener(FViewOnLongClickListener.create(getViewOnLongClickListener(src)));
    }

    /**
     * create new FView by special FView class
     * @param clz FView class
     * @param <T>
     * @return FView object, null for create error
     */
    private <T extends FView> T newFView(@NonNull Class<T> clz) {
        try {
            Constructor<T> constructor = clz.getConstructor(Context.class, IFViewRoot.class);
            FView root = constructor.newInstance(mContext, viewRoot);
            return (T) root;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    private IFViewGroup.FLayoutParams newMatchParentParams() {
        return new IFViewGroup.FLayoutParams(IFViewGroup.FLayoutParams.MATCH_PARENT, IFViewGroup.FLayoutParams.MATCH_PARENT);
    }

    /**
     * Translate LayoutParams into FLayoutParams
     *
     * @see FLayoutParamsTranslator
     *
     * @param params source params
     * @return FLayoutParams object, if params is null, return null.
     */
    private IFViewGroup.FLayoutParams translateLayoutParams(ViewGroup.LayoutParams params) {
        if (params == null)
            return null;
        Class<? extends IFViewGroup.FLayoutParams> tran = FLayoutParamsTranslator.getTranslateClass(params.getClass());
        IFViewGroup.FLayoutParams result = null;
        if (tran != null) {
            result = newLayoutParams(tran, params);
        }
        if (result == null) {
            if (params instanceof ViewGroup.MarginLayoutParams)
                return new IFViewGroup.FLayoutParams((ViewGroup.MarginLayoutParams)params);
            else
                return new IFViewGroup.FLayoutParams(params);
        }
        return result;
    }

    private <T extends IFViewGroup.FLayoutParams> T newLayoutParams(@NonNull Class<T> tClass,
                                                                    ViewGroup.LayoutParams source) {
        try {
            Constructor<T> constructor = tClass.getConstructor(source.getClass());
            return constructor.newInstance(source);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get View's OnClickListener by reflect
     * @param target target View
     * @return null if target View has no OnClickListener or error occur,
     *          othrewise OnClickListener object will be return.
     */
    public static View.OnClickListener getViewOnClickListener(View target) {
        try {
            Object mListenerInfo = mListenerInfoField.get(target);
            if (mListenerInfo == null)
                return null;
            if (mOnClickListenerField == null) {
                Class clz = mListenerInfo.getClass();
                mOnClickListenerField = clz.getDeclaredField("mOnClickListener");
                mOnClickListenerField.setAccessible(true);
            }
            return (View.OnClickListener) mOnClickListenerField.get(mListenerInfo);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get View's OnLongClickListener by reflect
     * @param target target View
     * @return null if target View has no OnLongClickListener or error occur,
     *          otherwise OnLongClickListener object will be return.
     */
    public static View.OnLongClickListener getViewOnLongClickListener(View target) {
        try {
            Object mListenerInfo = mListenerInfoField.get(target);
            if (mListenerInfo == null)
                return null;
            if (mOnLongClickListenerField == null) {
                Class clz = mListenerInfo.getClass();
                mOnLongClickListenerField = clz.getDeclaredField("mOnLongClickListener");
                mOnLongClickListenerField.setAccessible(true);
            }
            return (View.OnLongClickListener) mOnLongClickListenerField.get(mListenerInfo);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Implement of FView OnClickListener.
     *
     * When click event callback, callback old View click listener,
     * or if old View click listener is {@link OnClickListener},
     * callback listener's trans.
     */
    private static class FViewOnClickListener implements IFView.OnClickListener {
        static FViewOnClickListener craete(View.OnClickListener l) {
            if (l == null) {
                if (DEBUG) {
                    Log.d(TAG, "onclicklistener is null!");
                }
                return null;
            }
            FViewOnClickListener listener = new FViewOnClickListener();
            listener.listener = l;
            return listener;
        }
        View.OnClickListener listener;
        @Override
        public void onClick(IFView view) {
            if (listener instanceof OnClickListener) {
                OnClickListener.Trans trans = ((OnClickListener) listener).getTrans();
                if (trans != null) {
                    trans.onClick(view);
                    return;
                }
            }
            listener.onClick(null);
        }
    }

    /**
     * Implement of FView OnLongClickListener.
     *
     * When long click event callback, callback old View long click listener,
     * or if old View long click listener is {@link OnLongClickListener},
     * callback listener's trans.
     */
    private static class FViewOnLongClickListener implements IFView.OnLongClickListener {
        static FViewOnLongClickListener create(View.OnLongClickListener l) {
            if (l == null) {
                if (DEBUG) {
                    Log.d(TAG, "OnLongClickListener is null!");
                }
                return null;
            }
            FViewOnLongClickListener listener = new FViewOnLongClickListener();
            listener.listener = l;
            return listener;
        }
        View.OnLongClickListener listener;
        @Override
        public boolean onLongClick(IFView view) {
            if (listener instanceof OnLongClickListener) {
                OnLongClickListener.Trans trans = ((OnLongClickListener) listener).getTrans();
                if (trans != null) {
                    return trans.onLongClick(view);
                }
            }
            return listener.onLongClick(null);
        }
    }

    private static void d(String msg) {
        if (DEBUG) {
            Log.d(TAG, msg);
        }
    }
}
