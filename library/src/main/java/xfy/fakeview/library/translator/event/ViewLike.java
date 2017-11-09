package xfy.fakeview.library.translator.event;

import android.view.View;

import xfy.fakeview.library.fview.IView;

/**
 * Created by XiongFangyu on 2017/11/9.
 *
 * IView adapter or View
 */
class ViewLike implements IView {
    private int id = -1;

    static ViewLike create(View v) {
        ViewLike viewLike = new ViewLike();
        viewLike.id = v.getId();
        return viewLike;
    }

    @Override
    public int getId() {
        return id;
    }
}
