LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := ooura
LOCAL_SRC_FILES := ooura/ooura.cpp

LOCAL_CFLAGS    += -O3
LOCAL_CPPFLAGS  += -O3

include $(BUILD_SHARED_LIBRARY)
