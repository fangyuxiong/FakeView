package xfy.fakeview.library.text.compiler;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import xfy.fakeview.library.text.block.DefaultDrawableBlockList;
import xfy.fakeview.library.text.param.SpecialStyleParams;

/**
 * Created by XiongFangyu on 2018/3/14.
 */
public class SpecialCompiler extends DefaultTextCompiler {
    private static volatile SpecialCompiler compiler;

    public static SpecialCompiler getCompiler() {
        if (compiler == null) {
            synchronized (SpecialCompiler.class) {
                if (compiler == null)
                    compiler = new SpecialCompiler();
            }
        }
        return compiler;
    }

    private static final String RICH_REG = "(?<=\\([Ff][Oo][Nn][Tt])[\\s\\S]*?(?=\\(/[Ff][Oo][Nn][Tt]\\))";
    private static final String STYLE_SPLIT = "\\s";
    private static final String STYLE_PARAMS_SPLIT = "=";
    private static final String PARAMS_WRAPPER = "'";
    private static final String STYLE_END = ")";

    protected SpecialCompiler() {
        adapterMap = new HashMap<>();
    }

    public SpecialCompiler(ITextCompiler<DefaultDrawableBlockList> innerCompiler) {
        super(innerCompiler);
        adapterMap = new HashMap<>();
    }

    protected @NonNull String getReg() {
        return RICH_REG;
    }

    protected @NonNull Pattern getPattern() {
        if (pattern == null) {
            pattern = Pattern.compile(getReg());
        }
        return pattern;
    }

    protected @NonNull String getStyleSplit() {
        return STYLE_SPLIT;
    }

    protected @NonNull String getStyleParamsSplit() {
        return STYLE_PARAMS_SPLIT;
    }

    protected @NonNull String getParamsWrapper() {
        return PARAMS_WRAPPER;
    }

    protected @NonNull String getStyleEnd() {
        return STYLE_END;
    }

    private Pattern pattern;

    @Override
    public void compileInternal(@NonNull DefaultDrawableBlockList list, @NonNull CharSequence text, int start, int end, @Nullable SpecialStyleParams specialStyleParams) {
        CharSequence sub = text.subSequence(start, end);
        Matcher m = getPattern().matcher(sub);
        int lastEnd = start;
        while (m.find()) {
            String t = m.group().trim();
            int gs = m.start();
            int ge = m.end();
            int ss = getStyleStart(gs);
            if (lastEnd < ss) {
                super.compileInternal(list, sub, lastEnd, ss, specialStyleParams);
            }
            compileSpecialText(list, t, specialStyleParams);
            lastEnd = getStyleEnd(ge);
        }
        if (lastEnd < end) {
            super.compileInternal(list, sub, lastEnd, end, specialStyleParams);
        }
    }

    protected int getStyleStart(int start) {
        return start - 5;
    }

    protected int getStyleEnd(int end) {
        return end + 7;
    }

    protected int getTextStyleEndIndex(String text) {
        return text.indexOf(getStyleEnd());
    }

    private void compileSpecialText(DefaultDrawableBlockList list, String text, @Nullable SpecialStyleParams specialStyleParams) {
        int index = getTextStyleEndIndex(text);
        if (index < 0) {
            super.compileInternal(list, text, 0, text.length(), specialStyleParams);
            return;
        }
        final String styles = text.substring(0, index);
        final String content = text.substring(index + 1);
        if (TextUtils.isEmpty(styles)) {
            super.compileInternal(list, content, 0, content.length(), specialStyleParams);
            return;
        }
        final String[] ss = styles.split(getStyleSplit());
        if (ss == null || ss.length == 0) {
            super.compileInternal(list, content, 0, content.length(), specialStyleParams);
            return;
        }
        SpecialStyleParams inner = SpecialStyleParams.obtain();
        for (int i = 0, l = ss.length; i < l; i ++) {
            String style = ss[i];
            String[] param = style.split(getStyleParamsSplit());
            if (param == null || param.length != 2) {
                continue;
            }
            SpecialStyleAdapter adapter = adapterMap.get(param[0]);
            if (adapter == null) {
                continue;
            }
            String value = param[1];
            String pw = getParamsWrapper();
            if (value.startsWith(pw)) {
                value = value.substring(1);
            }
            if (value.endsWith(pw)) {
                value = value.substring(0, value.length() - 1);
            }
            if (value.length() == 0)
                continue;
            adapter.setStyle(inner, value);
        }
        super.compileInternal(list, content, 0, content.length(), inner);
    }

    private final HashMap<String, SpecialStyleAdapter> adapterMap;

    public void register(String key, SpecialStyleAdapter adapter) {
        adapterMap.put(key, adapter);
    }

    public void unRegister(String key) {
        adapterMap.remove(key);
    }

    public interface SpecialStyleAdapter {
        void setStyle(SpecialStyleParams params, String value);
    }
}
