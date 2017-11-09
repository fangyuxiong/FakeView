package xfy.fakeview.library.translator.event;

import android.view.View;

import xfy.fakeview.library.fview.IView;
import xfy.fakeview.library.fview.FView;

/**
 * Created by XiongFangyu on 2017/11/9.
 *
 * Click listener for {@link View} whitch could be translated
 * into {@link FView}
 *
 * A param implement {@link Trans} must be setted by constructor.
 *
 * <code>
 *     //old code:
 *     btn.setOnClickListener(new View.OnClickListener() {
 *         public void onClick(View v) {
 *             int id = v.getId();
 *             //do things
 *         }
 *     });
 *
 *     //new code:
 *     btn.setOnClickListener(new OnClickListener(new OnClickListener.Trans(){
 *         public void onClick(IView view) {
 *             int id = view.getId();
 *             //do things
 *         }
 *     }));
 * </code>
 */
public class OnClickListener implements View.OnClickListener{
    private Trans trans;
    public OnClickListener(Trans trans) {
        this.trans = trans;
    }

    @Override
    public void onClick(View v) {
        if (trans != null && v != null) {
            trans.onClick(ViewLike.create(v));
        }
    }

    public Trans getTrans() {
        return trans;
    }

    public static interface Trans {
        void onClick(IView view);
    }

}
