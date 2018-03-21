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

import xfy.fakeview.library.DebugInfo;
import xfy.fakeview.library.FViewCreater;
import xfy.fakeview.library.fview.FView;
import xfy.fakeview.library.fview.IFView;
import xfy.fakeview.library.fview.IFViewGroup;
import xfy.fakeview.library.fview.normal.FFrameLayout;
import xfy.fakeview.library.fview.normal.FImageView;
import xfy.fakeview.library.fview.normal.FLinearLayout;
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
//        handler.postDelayed(invalidateRunnable, 1000);
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

        layout.addView(newLinear(creater), new IFViewGroup.FLayoutParams(IFViewGroup.FLayoutParams.MATCH_PARENT, IFViewGroup.FLayoutParams.WRAP_CONTENT));
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

    private FView newLinear(FViewCreater creater) {
        FLinearLayout layout = creater.newFView(FLinearLayout.class);
        layout.setBackgroundColor(Color.GRAY);

        FImageView child1 = creater.newFView(FImageView.class);
        child1.setImageResource(R.drawable.me);
        layout.addView(child1, new FLinearLayout.LayoutParams(IFViewGroup.FLayoutParams.WRAP_CONTENT,
                IFViewGroup.FLayoutParams.WRAP_CONTENT, 0.5f));

        final FImageView child2 = creater.newFView(FImageView.class);
        child2.setImageResource(R.drawable.me);
        layout.addView(child2, new FLinearLayout.LayoutParams(IFViewGroup.FLayoutParams.WRAP_CONTENT,
                IFViewGroup.FLayoutParams.WRAP_CONTENT, 0.5f));
        child2.setVisibility(IFView.GONE);

        FImageView child3 = creater.newFView(FImageView.class);
        child3.setImageResource(R.drawable.me);
        layout.addView(child3, new FLinearLayout.LayoutParams(IFViewGroup.FLayoutParams.WRAP_CONTENT,
                IFViewGroup.FLayoutParams.WRAP_CONTENT, 0.5f));
        child3.setOnClickListener(new IFView.OnClickListener() {
            @Override
            public void onClick(IFView view) {
                Log.d("tag", "on click");
                if (child2.getVisibility() != IFView.VISIBLE) {
                    child2.setVisibility(IFView.VISIBLE);
                } else {
                    child2.setVisibility(IFView.GONE);
                }
            }
        });
        return layout;
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
//                Toast.makeText(this, "container click", Toast.LENGTH_LONG).show();
                startActivity(new Intent(this, TestNewTextActivity.class));
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
//                long start = System.currentTimeMillis();
//                int[] pos = new int[2];
//                view.getLocationInWindow(pos);
//                Toast.makeText(this, "image loc: [" + pos[0] + ", " + pos[1] + "], cast: " + (System.currentTimeMillis() - start), Toast.LENGTH_LONG).show();
                startActivity(new Intent(this, TestShallowLayersActivity.class));
                return true;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        Log.d("tag", "onclick" + v);
    }
}
