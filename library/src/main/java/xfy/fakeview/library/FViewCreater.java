package xfy.fakeview.library;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.lang.reflect.Constructor;

import xfy.fakeview.library.fview.FView;
import xfy.fakeview.library.fview.FViewRootImpl;
import xfy.fakeview.library.fview.IFViewRoot;

/**
 * Created by XiongFangyu on 2017/11/7.
 *
 * FView creater
 */
public class FViewCreater {
    private FViewRootImpl viewRoot;

    public FViewCreater(Context context) {
        viewRoot = new FViewRootImpl(context);
    }

    /**
     * build root view by special FView class
     * @param clz FView or its children class
     * @param <T> special FView class
     * @return null or special FView
     */
    public <T extends FView> T buildRootFView(@NonNull Class<T> clz) {
        T view = newFView(clz);
        viewRoot.setTargetChild(view);
        return view;
    }

    /**
     * create new FView by special FView class
     * @param clz FView or its children class
     * @param <T> special FView class
     * @return null or special FView
     */
    public <T extends FView> T newFView(@NonNull Class<T> clz) {
        try {
            Constructor<? extends FView> constructor = clz.getConstructor(Context.class, IFViewRoot.class);
            FView root = constructor.newInstance(viewRoot.getContext(), viewRoot);
            return (T) root;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * build view which contain all FView
     * @return root view which contain all FView
     */
    public View build() {
        return viewRoot;
    }

    public static ViewGroup.LayoutParams newMatchParentLayoutParams() {
        return new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    public static ViewGroup.LayoutParams newWrapContentLayoutParams() {
        return new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }
}
