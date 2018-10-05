package com.github.usermark.floatview;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.DrawableRes;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

public class FloatView implements View.OnTouchListener {

    private static final int PART_DELAY = 2000;
    private static final int FRAME = 10;

    private static final int BACK_TOP = 0;
    private static final int BACK_BOTTOM = 1;
    private static final int BACK_LEFT = 2;
    private static final int BACK_RIGHT = 3;

    private static final int BACK_TOP_PART = 10;
    private static final int BACK_BOTTOM_PART = 11;
    private static final int BACK_LEFT_PART = 12;
    private static final int BACK_RIGHT_PART = 13;

    private View layout;
    private ImageView floatBall;
    private WindowManager windowManager;
    private WindowManager.LayoutParams params;

    private int size;
    private int screenWidth, screenHeight;
    private float lastRawX, lastRawY;
    private float downX, downY;
    private long downTime;
    private boolean isMoving;
    private int left, top, right, bottom;
    private int currentFrame;

    private Handler handler = new InnerHandler(this);

    public FloatView(Context context, @DrawableRes int resId, int type) {
        layout = LayoutInflater.from(context).inflate(R.layout.layout_float, null);

        floatBall = layout.findViewById(R.id.imageview_ball);
        floatBall.setImageResource(resId);
        floatBall.setOnTouchListener(this);

        params = new WindowManager.LayoutParams();
        params.type = type;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.format = PixelFormat.TRANSPARENT;
        params.gravity = Gravity.TOP | Gravity.START;

        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.addView(layout, params);

        updateRealMetrics();
        setSize(context.getResources().getDimensionPixelSize(R.dimen.float_view_size));
    }

    public void setSize(int size) {
        this.size = size;
        floatBall.getLayoutParams().width = size;
        floatBall.getLayoutParams().height = size;
    }

    public void setFloatGravity(@FloatGravity.Option int gravity) {
        updateRealMetrics();

        switch (gravity) {
            case FloatGravity.LEFT_BOTTOM:
                params.x = 0;
                params.y = screenHeight - size;
                break;

            case FloatGravity.LEFT_CENTER:
                params.x = 0;
                params.y = screenHeight / 2 - size;
                break;

            case FloatGravity.LEFT_TOP:
                params.x = 0;
                params.y = 0;
                break;

            case FloatGravity.RIGHT_BOTTOM:
                params.x = screenWidth - size;
                params.y = screenHeight - size;
                break;

            case FloatGravity.RIGHT_CENTER:
                params.x = screenWidth - size;
                params.y = screenHeight / 2 - size;
                break;

            case FloatGravity.RIGHT_TOP:
                params.x = screenWidth - size;
                params.y = 0;
                break;

            case FloatGravity.CENTER_BOTTOM:
                params.x = (screenWidth - size) / 2;
                params.y = screenHeight - size;
                break;

            case FloatGravity.CENTER_TOP:
                params.x = (screenWidth - size) / 2;
                params.y = 0;
                break;
        }
        windowManager.updateViewLayout(layout, params);
        reset();
    }

    private void updateRealMetrics() {
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            display.getRealMetrics(metrics);
            screenWidth = metrics.widthPixels;
            screenHeight = metrics.heightPixels;

        } else {
            try {
                screenWidth = (Integer) Display.class.getMethod("getRawWidth").invoke(display);
                screenHeight = (Integer) Display.class.getMethod("getRawHeight").invoke(display);

            } catch (Exception e) {
                display.getMetrics(metrics);
                screenWidth = metrics.widthPixels;
                screenHeight = metrics.heightPixels;
            }
        }
    }

    public void show() {
        layout.setVisibility(View.VISIBLE);
        reset();
    }

    private void reset() {
        removeMessages();
        floatBall.clearAnimation();
        if (params.x == 0) {
            handler.sendEmptyMessageDelayed(BACK_LEFT_PART, PART_DELAY);

        } else if (params.x == screenWidth - size) {
            handler.sendEmptyMessageDelayed(BACK_RIGHT_PART, PART_DELAY);

        } else if (params.y == 0) {
            handler.sendEmptyMessageDelayed(BACK_TOP_PART, PART_DELAY);

        } else if (params.y == screenHeight - size) {
            handler.sendEmptyMessageDelayed(BACK_BOTTOM_PART, PART_DELAY);
        }
    }

    private void removeMessages() {
        handler.removeMessages(BACK_LEFT_PART);
        handler.removeMessages(BACK_RIGHT_PART);
        handler.removeMessages(BACK_TOP_PART);
        handler.removeMessages(BACK_BOTTOM_PART);
    }

    public void hide() {
        layout.setVisibility(View.GONE);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        updateRealMetrics();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastRawX = event.getRawX();
                lastRawY = event.getRawY();
                downX = params.x;
                downY = params.y;
                downTime = System.currentTimeMillis();
                removeMessages();
                floatBall.clearAnimation();
                break;

            case MotionEvent.ACTION_MOVE:
                float deltaX = event.getRawX() - lastRawX;
                float deltaY = event.getRawY() - lastRawY;
                params.x = (int) (downX + deltaX);
                params.y = (int) (downY + deltaY);

                if (Math.abs(deltaX) > 10 || Math.abs(deltaY) > 10) {
                    isMoving = true;
                }

                normalizePosition();
                windowManager.updateViewLayout(layout, params);
                break;

            case MotionEvent.ACTION_UP:
                if (!isMoving && System.currentTimeMillis() - downTime < 500) {
                    floatBall.performClick();

                } else {
                    updateViewLocation(event);
                }
                isMoving = false;
                break;
        }
        return true;
    }

    private void normalizePosition() {
        if (params.x < 0) {
            params.x = 0;
        }
        if (params.x > screenWidth - size) {
            params.x = screenWidth - size;
        }
        if (params.y < 0) {
            params.y = 0;
        }
        if (params.y > screenHeight - size) {
            params.y = screenHeight - size;
        }
    }

    private void updateViewLocation(MotionEvent event) {
        left = (int) (event.getRawX() - event.getX());
        right = screenWidth - left - size;
        top = (int) (event.getRawY() - event.getY());
        bottom = screenHeight - top - size;

        currentFrame = 0;

        // align minimal border
        if (Math.min(left, right) < Math.min(top, bottom)) {
            if (left < right) {
                handler.sendEmptyMessage(BACK_LEFT);

            } else {
                handler.sendEmptyMessage(BACK_RIGHT);
            }

        } else {
            if (top < bottom) {
                handler.sendEmptyMessage(BACK_TOP);

            } else {
                handler.sendEmptyMessage(BACK_BOTTOM);
            }
        }
    }

    public void setOnClickListener(View.OnClickListener listener) {
        floatBall.setOnClickListener(listener);
    }

    public void release() {
        windowManager.removeViewImmediate(layout); // need remove immediately
    }

    private void playAnim(int type) {
        if (currentFrame >= FRAME) {
            return;
        }
        currentFrame++;

        switch (type) {
            case BACK_TOP:
                params.y -= top / FRAME;
                if (currentFrame == FRAME) {
                    params.y = 0;
                    removeMessages();
                    handler.sendEmptyMessageDelayed(BACK_TOP_PART, PART_DELAY);
                }
                break;

            case BACK_BOTTOM:
                params.y += bottom / FRAME;
                if (currentFrame == FRAME) {
                    params.y = screenHeight - size;
                    removeMessages();
                    handler.sendEmptyMessageDelayed(BACK_BOTTOM_PART, PART_DELAY);
                }
                break;

            case BACK_LEFT:
                params.x -= left / FRAME;
                if (currentFrame == FRAME) {
                    params.x = 0;
                    removeMessages();
                    handler.sendEmptyMessageDelayed(BACK_LEFT_PART, PART_DELAY);
                }
                break;

            case BACK_RIGHT:
                params.x += right / FRAME;
                if (currentFrame == FRAME) {
                    params.x = screenWidth - size;
                    removeMessages();
                    handler.sendEmptyMessageDelayed(BACK_RIGHT_PART, PART_DELAY);
                }
                break;
        }

        windowManager.updateViewLayout(layout, params);
        handler.sendEmptyMessage(type);
    }

    private void playPartAnim(int type) {
        if (isMoving) {
            return;
        }
        float toXDelta = 0;
        float toYDelta = 0;

        switch (type) {
            case BACK_TOP_PART:
                toYDelta = -size / 2;
                break;

            case BACK_BOTTOM_PART:
                toYDelta = size / 2;
                break;

            case BACK_LEFT_PART:
                toXDelta = -size / 2;
                break;

            case BACK_RIGHT_PART:
                toXDelta = size / 2;
                break;

            default:
                return;
        }
        TranslateAnimation partAnim =
                new TranslateAnimation(0, toXDelta, 0, toYDelta);
        partAnim.setDuration(300);
        partAnim.setFillAfter(true);
        floatBall.startAnimation(partAnim);
    }

    private static class InnerHandler extends Handler {

        private FloatView callback;

        InnerHandler(FloatView callback) {
            this.callback = callback;
        }

        @Override
        public void handleMessage(Message msg) {
            callback.playAnim(msg.what);
            callback.playPartAnim(msg.what);
        }
    }
}
