package com.xfy.fakeview;

import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;

/**
 * Created by XiongFangyu on 2017/11/8.
 */

public class SpannableTextUtils {
    public static CharSequence getLinkStyle(CharSequence text) {
        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        builder.setSpan(new UnderlineSpan(), 0, text.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return builder;
    }

    public static CharSequence getColorableText(CharSequence text, int[] starts, int[] ends, int[] colors) {
        int len = starts.length;
        if (len != ends.length || len != colors.length)
            return text;
        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        for (int i = 0; i < len ; i ++) {
            int start = starts[i];
            int end = ends[i];
            int color = colors[i];
            builder.setSpan(new ForegroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return builder;
    }
}
