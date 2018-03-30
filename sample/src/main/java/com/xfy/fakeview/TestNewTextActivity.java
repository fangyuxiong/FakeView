package com.xfy.fakeview;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import xfy.fakeview.library.text.NewTextView;
import xfy.fakeview.library.text.compiler.ClickSpanTextCompiler;
import xfy.fakeview.library.text.compiler.DrawableTextCompiler;
import xfy.fakeview.library.text.utils.FClickableSpan;

/**
 * Created by 
 */
public class TestNewTextActivity extends Activity {
    private static final String TAG = "Fake--TestActivity";
    private enum Type {
        color {
            @Override
            void done(String data, NewTextView ntv, TextView tv, LayoutTextView ltv) throws Throwable{
                int color = Color.parseColor(data);
                ntv.getTextDrawable().setTextColor(color);
                tv.setTextColor(color);
            }
        },
        lineSpace {
            @Override
            void done(String data, NewTextView ntv, TextView tv, LayoutTextView ltv) throws Throwable {
                int l = Integer.parseInt(data);
                ntv.getTextDrawable().setLineSpace(l);
                tv.setLineSpacing(l, 1);
            }
        },
        textSize {
            @Override
            void done(String data, NewTextView ntv, TextView tv, LayoutTextView ltv) throws Throwable {
                float ts = Float.parseFloat(data);
                ntv.getTextDrawable().setTextSize(ts);
                tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, ts);
            }
        },
        maxLines {
            @Override
            void done(String data, NewTextView ntv, TextView tv, LayoutTextView ltv) throws Throwable {
                int ml = Integer.parseInt(data);
                ntv.getTextDrawable().setMaxLines(ml);
                tv.setMaxLines(ml);
            }
        }
        ;
        Type() {}
        abstract void done(String data, NewTextView ntv, TextView tv, LayoutTextView ltv) throws Throwable;
    }
    private NewTextView newTextView;
    private NewTextView newTextView2;
    private TextView normal_text;
    private LayoutTextView layoutTextView;
    private EditText editText;
    private TextView switcher;
    private EditText data;
    private Type type = Type.color;
    private int typeIndex = 0;
    private boolean bold = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_test_new_text);
        initData(getIntent());
        initView();
        initEvent();
    }

    private void initData(Intent data) {

    }

    private void initView() {
        newTextView = findViewByResId(R.id.new_text);
        newTextView2 = findViewByResId(R.id.new_text2);
        normal_text = findViewByResId(R.id.normal_text);
        layoutTextView = findViewByResId(R.id.layout_text);
        editText = findViewByResId(R.id.edit);
        switcher = findViewByResId(R.id.switcher);
        data = findViewByResId(R.id.data);
        layoutTextView.setTextSize(50);
        newTextView2.setInlucdePad(false);
        layoutTextView.setVisibility(View.GONE);
        normal_text.setVisibility(View.GONE);
//        View v = findViewById(R.id.test);
//        SpecialDrawable.getSingleInstance().onCallbackSet(v);
//        v.setBackgroundDrawable(SpecialDrawable.getSingleInstance());

        DrawableTextCompiler.getCompiler().setResourceAdapter(new DefaultResourceAdapter());
        ClickSpanTextCompiler.getCompiler().setInnerCompiler(DrawableTextCompiler.getCompiler());

        SpannableStringBuilder sb = new SpannableStringBuilder("[me]ha[me]slkdjflsakggasd[me]");
        sb.setSpan(new FClickableSpan() {
            @Override
            public void onClick(View v) {
                showToast("onclickspan" + v.hashCode());
            }
        }.underlineText().italicText(), 4, 12, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        newTextView.setText(sb);
        newTextView2.setText(sb);
//        newTextView.setText("h1(font size='30px' color='white' weight='600' background='blue')今天天气真好，晚上来家坐坐呀(/font)h2(font size='100px' color='red' weight='800')@王先asfsadfsdfsadfds生(/font)h3");
    }

    private void initEvent() {
        findViewById(R.id.container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newTextView.invalidate();
                newTextView2.invalidate();
                showToast("click container");
            }
        });
        findViewById(R.id.click).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s = editText.getText().toString();
                long now = now();
                newTextView.setText(s);
                long end = now();
                Log.d(TAG, "set newtext: " + (end - now));
                now = now();
                newTextView2.setText(s);
                end = now();
                Log.d(TAG, "set newtext2: " + (end - now));
                now = now();
//                normal_text.setText(LayoutHelper.newSpan(s, (int) normal_text.getTextSize()));
                normal_text.setText(LayoutHelper.getRichText(s));
                end = now();
                Log.d(TAG, "set text: " + (end - now));
                now = now();
                layoutTextView.setText(LayoutHelper.newSpan(s, 50));
                end = now();
                Log.d(TAG, "set layouttext: " + (end - now));
            }
        });
        findViewById(R.id.done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    type.done(data.getText().toString(), newTextView, normal_text, layoutTextView);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                    showToast("错误");
                }
            }
        });
        switcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                typeIndex ++;
                Type[] types = Type.values();
                if (typeIndex == types.length) {
                    typeIndex = 0;
                }
                type = types[typeIndex];
                switcher.setText(type.name());
            }
        });
//        newTextView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                bold = !bold;
//                newTextView.setBoldText(bold);
//            }
//        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private <T extends View> T findViewByResId(int id) {
        return (T) findViewById(id);
    }

    private Toast toast;
    private void showToast(String text) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(this, text, Toast.LENGTH_LONG);
        toast.show();
    }

    private long now() {
        return System.nanoTime();
    }
}
