package com.xfy.fakeview;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import xfy.fakeview.library.fview.IFView;
import xfy.fakeview.library.fview.IFViewRoot;
import xfy.fakeview.library.fview.IView;
import xfy.fakeview.library.translator.TranslatorAndLayersMergeManager;
import xfy.fakeview.library.translator.TranslatorManager;
import xfy.fakeview.library.translator.event.OnClickListener;
import xfy.fakeview.library.translator.event.OnLongClickListener;

public class TestTranslateActivity extends Activity implements OnClickListener.Trans, OnLongClickListener.Trans{

    private Toast toast;

    private Runnable action;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_translate);
        View i1 = findViewById(R.id.image1);
        View i2 = findViewById(R.id.image2);
        View t1 = findViewById(R.id.text1);
        View t2 = findViewById(R.id.text2);
        i2.setOnClickListener(onClickListener);
        i1.setOnClickListener(onClickListener);
        t1.setOnClickListener(onClickListener);
        t2.setOnClickListener(onClickListener);

        i1.setOnLongClickListener(onLongClickListener);
        i2.setOnLongClickListener(onLongClickListener);
        t1.setOnLongClickListener(onLongClickListener);
        t2.setOnLongClickListener(onLongClickListener);

        long mill = System.currentTimeMillis();
        TranslatorManager translatorManager = new TranslatorAndLayersMergeManager(this);
        final ViewGroup container = (ViewGroup) findViewById(R.id.container);
        final IFViewRoot root = translatorManager.translateView(container);
        Log.d("TestTranslateActivity", "cast: " + (System.currentTimeMillis() - mill));

        handler = new Handler();
        action = new Runnable() {
            @Override
            public void run() {
//                container.requestLayout();
//                container.invalidate();
                root.requestFViewTreeLayout();
                root.invalidate();
                handler.postDelayed(this, 1000);
            }
        };
        handler.postDelayed(action, 1000);
    }

    private OnClickListener onClickListener = new OnClickListener(this);
    private OnLongClickListener onLongClickListener = new OnLongClickListener(this);

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(action);
    }
    @Override
    public void onClick(IView view) {
        if (view == null) {
            showToast("null view");
        }
        switch (view.getId()) {
            case R.id.image1:
                showToast("click image1");
                break;
            case R.id.image2:
                showToast("click image2");
                break;
            case R.id.text1:
                showToast("click text1");
                break;
            case R.id.text2:
                showToast("click text2");
                break;
            default:
                showToast("click what? " + view.getId());
                break;
        }
    }

    private void showToast(String msg) {
        if (toast != null)
            toast.cancel();
        toast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        toast.show();
    }

    @Override
    public boolean onLongClick(IView view) {
        if (view == null)
            return false;
        long start = System.currentTimeMillis();
        int[] pos = new int[2];
        if (view instanceof IFView) {
            ((IFView) view).getLocationInWindow(pos);
        }
        long cast = System.currentTimeMillis() - start;
        String msg = "view pos: [" + pos[0] + ", " + pos[1] + "], cast: " + cast;
        switch (view.getId()) {
            case R.id.image1:
                showToast(msg);
                break;
            case R.id.image2:
                showToast(msg);
                break;
            case R.id.text1:
                showToast(msg);
                break;
            case R.id.text2:
                showToast(msg);
                break;
            default:
                showToast("long click what? " + view.getId());
                break;
        }
        return true;
    }
}
