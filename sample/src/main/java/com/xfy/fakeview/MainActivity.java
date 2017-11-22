package com.xfy.fakeview;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import xfy.fakeview.library.DebugInfo;
import xfy.fakeview.library.FViewCreater;
import xfy.fakeview.library.fview.IFView;
import xfy.fakeview.library.fview.IFViewGroup;
import xfy.fakeview.library.fview.normal.FFrameLayout;
import xfy.fakeview.library.fview.normal.FImageView;
import xfy.fakeview.library.fview.normal.FTextView;

public class MainActivity extends Activity implements IFView.OnClickListener, IFView.OnLongClickListener, View.OnClickListener{
    private ViewGroup container;

    private Handler handler;
    private Runnable invalidateRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DebugInfo.setDebug(true);

        container = (ViewGroup) findViewById(R.id.container);
        container.addView(buildFView());
        container.setOnClickListener(this);

        handler = new Handler();
        handler.postDelayed(invalidateRunnable, 1000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(invalidateRunnable);
    }

    private View buildFView() {
        FViewCreater creater = new FViewCreater(this);
        FFrameLayout layout = creater.buildRootFView(FFrameLayout.class);
        layout.setFLayoutParams(newMatchParentParams());
        layout.setId(1);
        layout.setOnClickListener(this);
        layout.setOnLongClickListener(this);

        final FImageView imageView = creater.newFView(FImageView.class);
        imageView.setPadding(10, 10, 10, 10);
        imageView.setImageResource(R.drawable.me);
        imageView.setOnClickListener(this);
        imageView.setOnLongClickListener(this);
        imageView.setId(2);
        imageView.setBackgroundColor(Color.BLACK);

        layout.addView(imageView, new FFrameLayout.LayoutParams(IFViewGroup.FLayoutParams.WRAP_CONTENT,
                IFViewGroup.FLayoutParams.WRAP_CONTENT, Gravity.CENTER));

        final FTextView textView = creater.newFView(FTextView.class);
        textView.setPadding(10, 10, 10, 10);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        textView.setTextColor(Color.RED);
        textView.setBackgroundColor(Color.YELLOW);
        textView.setText(SpannableTextUtils.getColorableText("哈哈哈哈哈哈哈哈哈", new int[] {0, 2}, new int[]{1, 4}, new int[]{Color.BLACK, Color.BLUE}));

        layout.addView(textView, new FFrameLayout.LayoutParams(IFViewGroup.FLayoutParams.WRAP_CONTENT,
                IFViewGroup.FLayoutParams.WRAP_CONTENT, Gravity.BOTTOM | Gravity.CENTER)
        .setMargins(0, 0, 0, 20));

        View root = creater.build();
        root.setLayoutParams(FViewCreater.newMatchParentLayoutParams());

        invalidateRunnable = new Runnable() {
            @Override
            public void run() {
                imageView.invalidate();
                handler.postDelayed(this, 1000);
            }
        };
        return root;
    }

    private IFViewGroup.FLayoutParams newMatchParentParams() {
        return new IFViewGroup.FLayoutParams(IFViewGroup.FLayoutParams.MATCH_PARENT, IFViewGroup.FLayoutParams.MATCH_PARENT);
    }

    private IFViewGroup.FLayoutParams newWrapContentParams() {
        return new IFViewGroup.FLayoutParams(IFViewGroup.FLayoutParams.WRAP_CONTENT, IFViewGroup.FLayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onClick(IFView view) {
        switch (view.getId()) {
            case 1:
                Toast.makeText(this, "container click", Toast.LENGTH_LONG).show();
                break;
            case 2:
                startActivity(new Intent(this, TestTranslateActivity.class));
                break;
        }
    }

    @Override
    public boolean onLongClick(IFView view) {
        switch (view.getId()) {
            case 1:
                startActivity(new Intent(this, TestMergeLayersActivity.class));
                return true;
            case 2:
                long start = System.currentTimeMillis();
                int[] pos = new int[2];
                view.getLocationInWindow(pos);
                Toast.makeText(this, "image loc: [" + pos[0] + ", " + pos[1] + "], cast: " + (System.currentTimeMillis() - start), Toast.LENGTH_LONG).show();

                return true;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        Log.d("tag", "onclick" + v);
    }
}
