package com.xfy.fakeview;

import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.style.AbsoluteSizeSpan;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by XiongFangyu on 2018/3/13.
 */
public class LayoutHelper {

    private static final HashMap<String, Integer> resourceMap = new HashMap<>();

    static {
        resourceMap.put("[me]", R.drawable.me);
        resourceMap.put("(me)", R.drawable.me);
    }

    public static StaticLayout newLayout(CharSequence text, TextPaint paint, int maxWidth) {
        return new StaticLayout(text, paint, maxWidth, Layout.Alignment.ALIGN_NORMAL, 1, 0, false);
    }

    public static CharSequence newSpan(String text, int ds) {
        SpannableStringBuilder builder = SpannableStringBuilder.valueOf(text);
        Matcher matcher = Pattern.compile("\\[me\\]").matcher(text);
        while (matcher.find()) {
            String key = matcher.group();
            int res = getRes(key);
            if (res > 0) {
                builder.setSpan(new ImageSpan(App.getContext(), res, ds), matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        builder.setSpan(new AbsoluteSizeSpan(100), text.length() / 4, text.length() * 3 / 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return builder;
    }

    public static int getRes(String key) {
        Integer integer = resourceMap.get(key);
        if (integer == null)
            return 0;
        return integer;
    }
}
