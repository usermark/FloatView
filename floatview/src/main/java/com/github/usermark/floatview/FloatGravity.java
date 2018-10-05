package com.github.usermark.floatview;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by usermark on 2018/10/4.
 */
public interface FloatGravity {
    int LEFT_TOP = 0;
    int LEFT_CENTER = 1;
    int LEFT_BOTTOM = 2;
    int RIGHT_TOP = 3;
    int RIGHT_CENTER = 4;
    int RIGHT_BOTTOM = 5;
    int CENTER_TOP = 6;
    int CENTER_BOTTOM = 7;

    @IntDef({
            LEFT_TOP,
            LEFT_CENTER,
            LEFT_BOTTOM,
            RIGHT_TOP,
            RIGHT_CENTER,
            RIGHT_BOTTOM,
            CENTER_TOP,
            CENTER_BOTTOM
    })
    @Retention(RetentionPolicy.SOURCE)
    @interface Option {}
}
