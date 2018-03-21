package com.xfy.fakeview.special;

import com.xfy.fakeview.DefaultResourceAdapter;

import xfy.fakeview.library.text.compiler.DrawableTextCompiler;

/**
 * Created by XiongFangyu on 2018/3/20.
 */
public class TestCompiler extends DrawableTextCompiler {

    private static volatile TestCompiler compiler;

    public static TestCompiler getCompiler() {
        if (compiler == null) {
            synchronized (TestCompiler.class) {
                if (compiler == null)
                    compiler = new TestCompiler();
            }
        }
        return compiler;
    }

    protected TestCompiler() {
        D_START = '(';
        D_END = ')';
        setResourceAdapter(new DefaultResourceAdapter());
    }
}
