package com.xfy.fakeview;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.TypedValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by XiongFangyu on 2018/3/13.
 */
public class LayoutHelper {
    private static final String RICH_REG = "(?<=\\([Ff][Oo][Nn][Tt])[\\s\\S]*?(?=\\(/[Ff][Oo][Nn][Tt]\\))";

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

    public static CharSequence getRichText(CharSequence src) {
        if (src == null)
            return null;
        List<MWSTextBean> list = match(src.toString());
        if (list == null || list.isEmpty())
            return src;
        return getBuilder(list);
    }

    public static SpannableStringBuilder getBuilder(List<MWSTextBean> list){
        SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
        MWSTextBean bean = null;
        SpannableString spannableString = null;
        ForegroundColorSpan colorSpan = null;
        AbsoluteSizeSpan sizeSpan = null;
        int contentSize = 0;
        for (int i = 0, length = list.size(); i < length; i++) {
            bean = list.get(i);
            contentSize = bean.content.length();

            spannableString = new SpannableString(bean.content);
            colorSpan = new ForegroundColorSpan(Color.parseColor(bean.color));
            sizeSpan = new AbsoluteSizeSpan(bean.size);
            if (bean.isBold()) {
                spannableString.setSpan(new StyleSpan(Typeface.BOLD), 0, contentSize, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                spannableString.setSpan(new StyleSpan(Typeface.NORMAL), 0, contentSize, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            spannableString.setSpan(colorSpan, 0, contentSize, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableString.setSpan(sizeSpan, 0, contentSize, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            stringBuilder.append(spannableString);
        }
        return stringBuilder;
    }

    public static List<MWSTextBean> match(String source) {
        List<MWSTextBean> result = new ArrayList<MWSTextBean>();
        Matcher m = Pattern.compile(RICH_REG).matcher(source);
        while (m.find()) {
            String temp = m.group(0);
            if (temp == null) {
                return result;
            }
            result.add(parseNewText(temp));
        }
        return result;
    }

    public static MWSTextBean parseNewText(@NonNull String text){

        MWSTextBean textBean = new MWSTextBean(20, "#000000", "default", "");

        int index = text.indexOf(')');
        if (-1 == index){
            return textBean;
        }
        int length = text.length();
        String params = text.substring(0, index);
        textBean.content = text.substring(index+1,length);

        params = params.trim();
        String[] paramArray = params.split("\\s");
        for (int i = 0, size = paramArray.length; i < size; i++){
            String tempString = paramArray[i];
            String[] tempArray = tempString.split("=");
            if (tempArray.length != 2){
                return textBean;
            }

            if (tempArray[0].contains("\'")){
                tempArray[0] = tempArray[0].substring(1, tempArray[0].length());
            }
            if (tempArray[1].contains("\'")) {
                tempArray[1] = tempArray[1].substring(1, tempArray[1].length()-1);
            }
            switch (tempArray[0]){
                case "size":{
                    textBean.size = parseSize(tempArray[1]);
                    break;
                }
                case "color":{
                    textBean.color = tempArray[1];
                    break;
                }

                case "face":{
                    textBean.face = tempArray[1];
                    break;
                }

                case "weight":{
                    textBean.weight = Integer.parseInt(tempArray[1]);
                    break;
                }

                default:
                    break;
            }

        }

        return textBean;
    }

    private static int parseSize(String s) {
        if (TextUtils.isEmpty(s))
            return 0;
        int unit = TypedValue.COMPLEX_UNIT_PX;
        int index = s.indexOf("px");
        if (index > 0) {
            s = s.substring(0, index);
        } else if ((index = s.indexOf("dp")) > 0) {
            s = s.substring(0, index);
            unit = TypedValue.COMPLEX_UNIT_DIP;
        } else if ((index = s.indexOf("sp")) > 0) {
            s = s.substring(0, index);
            unit = TypedValue.COMPLEX_UNIT_SP;
        }
        try {
            int v = Integer.parseInt(s);
            return (int) TypedValue.applyDimension(unit, v, Resources.getSystem().getDisplayMetrics());
        } catch (Throwable e) {}
        return 0;
    }

    public static class MWSTextBean {
        public int size;
        public String color;
        public String face;
        public String content;
        public int weight;//>700是粗体

        public MWSTextBean(int size, String color, String face, String content) {
            this.size = size;
            this.color = color;
            this.face = face;
            this.content = content;
        }

        public boolean isBold() {
            return weight >= 700;
        }

        @Override
        public String toString() {
            return "size = " + size + "\n" +
                    "color = " + color + "\n" +
                    "face = " + face + "\n" +
                    "weight = " + weight + "\n" +
                    "content = " + content;
        }
    }
}
