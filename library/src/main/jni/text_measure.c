//
// Created by XiongFangyu on 2018/3/9.
//

#include "text_measure.h"
#include "log.h"

uint64 setState(uint64 flag, uint32 state) {
    uint64 stateL = state;
    flag = (flag & ~(3LL << STATE_OFFSET)) | (stateL << STATE_OFFSET);
    return flag;
}

uint64 setCurrentLeft(uint64 flag, uint32 currentLeft) {
    uint64 leftL = currentLeft;
    flag = (flag & ~(LEFT_FLAG)) | (leftL << LEFT_OFFSET);
    return flag;
}

uint64 setLines(uint64 flag, uint32 lines) {
    uint64 ll = lines;
    flag = (flag & ~(LINES_FLAG)) | (ll << LINES_OFFSET);
    return flag;
}

uint64 setMaxWidth(uint64 flag, uint32 maxWidth) {
    flag = (flag & ~(WIDTH_FLAG)) | maxWidth;
    return flag;
}

uint64 setWillDrawOnFirstLine(uint64 flag) {
    flag = flag | WILL_DRAW_ON_FIRST_LINE_FLAG;
    return flag;
}

uint32 getState(uint64 flag) {
    return (uint32) (flag >> STATE_OFFSET);
}

uint32 getCurrentLeft(uint64 flag) {
    return (uint32) ((flag & LEFT_FLAG) >> LEFT_OFFSET);
}

uint32 getLines(uint64 flag) {
    return (flag & LINES_FLAG) >> LINES_OFFSET;
}

uint32 getMaxWidth(uint64 flag) {
    return (uint32) (flag & WIDTH_FLAG);
}

uint32 maxNum(uint32 a, uint32 b) {
    return a > b ? a : b;
}

uint64 calContentMaxWidth(uint64 flag, uint32 left) {
    uint32 m = maxNum(getMaxWidth(flag), getCurrentLeft(flag) - left);
    return setMaxWidth(flag, m);
}

uint64 gotoCalNextLine(uint64 flag, uint32 left) {
    flag = setLines(flag, getLines(flag) + 1);
    flag = calContentMaxWidth(flag, left);
    return setCurrentLeft(flag, left);
}

uint64 measure(uint64 flag, uint32 left, uint32 right, jfloat *widths, uint32 len)
{
    uint32 contentWdith = right - left;
    uint32 i;
    //printf("array len: %d\n", len);
    for (i = 0; i < len; i ++) {
        if (contentWdith < widths[i]) {
            return setState(0, STATE_ERROR);
        }
        uint32 cl = getCurrentLeft(flag);
        //printf("currentleft :%d\n", cl);
        if (cl + widths[i] > right) {
            if (i != 0) {
                flag = setWillDrawOnFirstLine(flag);
            }
            flag = gotoCalNextLine(flag, left);
        }
        flag = setCurrentLeft(flag, getCurrentLeft(flag) + ceil(widths[i]));
    }
    return calContentMaxWidth(flag, left);
}

JNIEXPORT jlong JNICALL Java_xfy_fakeview_library_text_utils_MeasureTextUtils_nativeMeasureText
  (JNIEnv *env, jclass obj, jlong flag, jint left, jint right, jfloatArray widths)
{
    jint len = (*env)->GetArrayLength(env, widths);
    jfloat *widthArray = (*env)->GetFloatArrayElements(env, widths, 0);
    flag = measure(flag, left, right, widthArray, len);
    return flag;
}