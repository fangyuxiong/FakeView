package xfy.fakeview.library;

import org.junit.Assert;
import org.junit.Test;

import xfy.fakeview.library.text.utils.SimpleGravity;

/**
 * Created by XiongFangyu on 2018/3/22.
 */
public class GravityTest {

    @Test
    public void testGravity() {
        int left = 324;
        int top = 425;
        long flag = SimpleGravity.combine(left, top);
        Assert.assertEquals(left, SimpleGravity.getLeft(flag));
        Assert.assertEquals(top, SimpleGravity.getTop(flag));
    }
}
