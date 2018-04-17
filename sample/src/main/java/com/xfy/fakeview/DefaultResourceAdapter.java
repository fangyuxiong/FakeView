package com.xfy.fakeview;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

import com.xfy.fakeview.special.SpecialDrawable;

import xfy.fakeview.library.text.compiler.DrawableTextCompiler;

/**
 * Created by XiongFangyu on 2018/3/13.
 */
public class DefaultResourceAdapter implements DrawableTextCompiler.ResourceAdapter {
    @Override
    public void beforeCompile() {

    }

    @Override
    public int parseRes(@NonNull CharSequence text) {
        return LayoutHelper.getRes(text.toString());
    }

    @Override
    public Drawable parseDrawable(@NonNull CharSequence text) {
        if (text.equals("(spcial)"))
        return SpecialDrawable.getSingleInstance();
        return null;
    }
}
