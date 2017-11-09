package xfy.fakeview.library.translator.event;

import android.view.View;

import xfy.fakeview.library.fview.IView;
import xfy.fakeview.library.fview.FView;

/**
 * Created by XiongFangyu on 2017/11/9.
 *
 * Long click listener for {@link View} whitch could be translated
 * into {@link FView}
 *
 * A param implement {@link Trans} must be setted by constructor.
 *
 * <code>
 *     //old code:
 *     btn.setOnLongClickListener(new View.OnLongClickListener() {
 *         public boolean onLongClick(View v) {
 *             int id = v.getId();
 *             //do things
 *             return true;
 *         }
 *     });
 *
 *     //new code:
 *     btn.setOnLongClickListener(new OnLongClickListener(new OnLongClickListener.Trans(){
 *         public boolean onLongClick(IView view) {
 *             int id = view.getId();
 *             //do things
 *             return true;
 *         }
 *     }));
 * </code>
 */
public class OnLongClickListener implements View.OnLongClickListener{
    private Trans trans;

    public OnLongClickListener(Trans trans) {
        this.trans = trans;
    }
    @Override
    public boolean onLongClick(View v) {
        return trans != null ? trans.onLongClick(ViewLike.create(v)) : false;
    }

    public Trans getTrans() {
        return trans;
    }

    public interface Trans {
        boolean onLongClick(IView view);
    }
}
