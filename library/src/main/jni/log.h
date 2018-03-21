// Android jni log 工具类
// Created by tangyuchun on 8/4/15.
//


#define LOG_TAG "Fake--"

#include <android/log.h>

#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

