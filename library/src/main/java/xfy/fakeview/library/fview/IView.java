package xfy.fakeview.library.fview;

/**
 * Created by XiongFangyu on 2017/11/9.
 *
 * 默认所有View或FView带有id，可为-1
 */
public interface IView {
    /**
     * 获取View或FView的id
     * @return id [-1, +)
     */
    int getId();
}
