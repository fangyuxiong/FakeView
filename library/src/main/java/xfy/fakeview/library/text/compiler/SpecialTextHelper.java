package xfy.fakeview.library.text.compiler;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;

import xfy.fakeview.library.text.param.SpecialStyleParams;

import static xfy.fakeview.library.text.compiler.SpecialCompiler.SpecialStyleAdapter;

/**
 * Created by XiongFangyu on 2018/3/14.
 */
public class SpecialTextHelper {
    private static Context context;
    private static ColorAdapter colorAdapter;
    private static WeightAdapter weightAdapter;
    private static SizeAdapter sizeAdapter;
    private static BackgroundColorAdapter backgroundColorAdapter;

    public static void init(Context c) {
        context = c.getApplicationContext();
    }

    public static SpecialCompiler getSpecialCompiler(Context context) {
        init(context);
        SpecialCompiler compiler = SpecialCompiler.getCompiler();
        if (colorAdapter == null || weightAdapter == null || sizeAdapter == null || backgroundColorAdapter == null) {
            colorAdapter = new ColorAdapter();
            weightAdapter = new WeightAdapter();
            sizeAdapter = new SizeAdapter();
            backgroundColorAdapter = new BackgroundColorAdapter();
            compiler.register("color", colorAdapter);
            compiler.register("weight", weightAdapter);
            compiler.register("size", sizeAdapter);
            compiler.register("background", backgroundColorAdapter);
        }
        return compiler;
    }

    private static class ColorAdapter implements SpecialStyleAdapter {
        @Override
        public void setStyle(SpecialStyleParams params, String value) {
            params.withForegroundColor(Color.parseColor(value));
        }
    }
    private static class WeightAdapter implements SpecialStyleAdapter {
        @Override
        public void setStyle(SpecialStyleParams params, String value) {
            if (Integer.parseInt(value) > 700) {
                params.boldText();
            }
        }
    }
    private static class SizeAdapter implements SpecialStyleAdapter {
        @Override
        public void setStyle(SpecialStyleParams params, String value) {
            int unit = TypedValue.COMPLEX_UNIT_PX;
            int index = value.indexOf("px");
            if (index > 0) {
                value = value.substring(0, index);
            } else if ((index = value.indexOf("dp")) > 0) {
                value = value.substring(0, index);
                unit = TypedValue.COMPLEX_UNIT_DIP;
            } else if ((index = value.indexOf("sp")) > 0) {
                value = value.substring(0, index);
                unit = TypedValue.COMPLEX_UNIT_SP;
            }
            try {
                int v = Integer.parseInt(value);
                params.withTextSize(TypedValue.applyDimension(unit, v, context.getResources().getDisplayMetrics()));
            } catch (Throwable e) {}
        }
    }
    private static class BackgroundColorAdapter implements SpecialStyleAdapter {
        @Override
        public void setStyle(SpecialStyleParams params, String value) {
            params.withBackgroundColor(Color.parseColor(value));
        }
    }
}
