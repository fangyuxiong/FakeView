#!/usr/bin/env bash

cd library/src/main/jni
ndk-build
cd ..
rm -dfr jniLibs/armeabi
mv -f libs/armeabi jniLibs/
rm -dfr libs
rm -dfr obj