package com.xfy.fakeview;

import android.app.Application;
import android.content.Context;

/**
 * Created by XiongFangyu on 2018/3/13.
 */

public class App extends Application {
    private static Context context;
    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }

    public static Context getContext() {
        return context;
    }
}
