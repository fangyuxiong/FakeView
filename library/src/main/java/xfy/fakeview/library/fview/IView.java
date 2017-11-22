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

    /**
     * Returns this view's tag.
     *
     * @return the Object stored in this view as a tag, or {@code null} if not
     *         set
     *
     * @see #setTag(Object)
     * @see #getTag(int)
     */
    Object getTag();

    /**
     * Sets the tag associated with this view. A tag can be used to mark
     * a view in its hierarchy and does not have to be unique within the
     * hierarchy. Tags can also be used to store data within a view without
     * resorting to another data structure.
     *
     * @param tag an Object to tag the view with
     *
     * @see #getTag()
     * @see #setTag(int, Object)
     */
    void setTag(Object tag);

    /**
     * Returns the tag associated with this view and the specified key.
     *
     * @param key The key identifying the tag
     *
     * @return the Object stored in this view as a tag, or {@code null} if not
     *         set
     *
     * @see #setTag(int, Object)
     * @see #getTag()
     */
    Object getTag(int key);

    /**
     * Sets a tag associated with this view and a key. A tag can be used
     * to mark a view in its hierarchy and does not have to be unique within
     * the hierarchy. Tags can also be used to store data within a view
     * without resorting to another data structure.
     *
     * The specified key should be an id declared in the resources of the
     * application to ensure it is unique (see the <a
     * href="{@docRoot}guide/topics/resources/more-resources.html#Id">ID resource type</a>).
     * Keys identified as belonging to
     * the Android framework or not associated with any package will cause
     * an {@link IllegalArgumentException} to be thrown.
     *
     * @param key The key identifying the tag
     * @param tag An Object to tag the view with
     *
     * @throws IllegalArgumentException If they specified key is not valid
     *
     * @see #setTag(Object)
     * @see #getTag(int)
     */
    void setTag(int key, final Object tag);
}
