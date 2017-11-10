package com.xfy.fakeview;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import xfy.fakeview.library.layermerge.LayersMergeManager;

public class TestMergeLayersActivity extends Activity implements View.OnClickListener{

    private FrameLayout mergeLayout1, mergeLayout2, mergeLayout3;

    private LayersMergeManager manager1, manager2, manager3;
    private boolean merged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_merge_layers);

        mergeLayout1 = (FrameLayout) findViewById(R.id.merge1);
        mergeLayout2 = (FrameLayout) findViewById(R.id.merge2);
        mergeLayout3 = (FrameLayout) findViewById(R.id.merge3);

        mergeLayout1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(TestMergeLayersActivity.this, TestMergeEngineActivity.class));
            }
        });

        findViewById(R.id.merge_btn).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (!merged)
            merge();
        else {
            check();
        }
    }

    private void merge() {
        long start = System.currentTimeMillis();
        if (manager1 == null) {
            manager1 = new LayersMergeManager(mergeLayout1);
            manager1.mergeChildrenLayers();
        }
        if (manager2 == null) {
            manager2 = new LayersMergeManager(mergeLayout2, true);
            manager2.mergeChildrenLayers();
        }
        if (manager3 == null) {
            manager3 = new LayersMergeManager(mergeLayout3);
            manager3.mergeChildrenLayers();
        }
        Toast.makeText(this, "merge success, cast: " + (System.currentTimeMillis() - start), Toast.LENGTH_SHORT).show();
        merged = true;
    }

    private void check() {
        int childCount = mergeLayout1.getChildCount();
        for (int i = 0; i < childCount; i ++) {
            View child = mergeLayout1.getChildAt(i);
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) child.getLayoutParams();
            Log.d("testmerge", "child: " + child + " \n"
                    + String.format("margin:[%d, %d, %d, %d]", params.leftMargin, params.topMargin, params.rightMargin, params.bottomMargin));
        }
    }
}
