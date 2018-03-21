# text measure

LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES := text_measure.c
LOCAL_MODULE := libmeasure

#引入log
LOCAL_LDLIBS :=-llog

include $(BUILD_SHARED_LIBRARY)
