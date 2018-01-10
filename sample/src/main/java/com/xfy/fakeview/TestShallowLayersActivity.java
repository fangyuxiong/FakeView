package com.xfy.fakeview;

import android.app.Activity;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import xfy.fakeview.library.shallowlayer.ShallowLayerManager;

/**
 * Created by XiongFangyu on 2018/1/10.
 */

public class TestShallowLayersActivity extends Activity {
    private static final String TAG = "SHALLOW";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_shallow_layers);
        final ViewGroup root = (ViewGroup) findViewById(android.R.id.content);

        findViewById(R.id.start_shallow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShallowLayerManager layerManager = new ShallowLayerManager(root);
                long now = now();
                layerManager.start();
                Log.d(TAG, "shallow done. cast " + (now() - now));

                Log.d(TAG, getLayoutStr(root));
            }
        });
        findViewById(R.id.has_click).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onclick");
            }
        });
    }

    private String getLayoutStr(ViewGroup vg) {
        StringBuilder sb = new StringBuilder(getViewStr(vg)).append(":[");
        for (int i = 0, l = vg.getChildCount(); i < l ;i ++) {
            View c = vg.getChildAt(i);
            if (c instanceof ViewGroup) {
                sb.append(getLayoutStr((ViewGroup)c));
            } else {
                sb.append(getViewStr(c));
            }
            if (i != l - 1) {
                sb.append(',');
            }
        }
        sb.append(']');
        return sb.toString();
    }

    private String getViewStr(View v) {
        return v.getClass().getName();
    }

    private long now() {
        return SystemClock.uptimeMillis();
    }
}
