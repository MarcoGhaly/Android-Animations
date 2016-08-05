package com.marco.animations;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

public class Animator implements Runnable {

    // Animator Callback
    public interface AnimatorCallback {
        void animationCompleted();
    }


    public static final int ANIMATION_TYPE_MOVE_UP = 0;
    public static final int ANIMATION_TYPE_MOVE_DOWN = 1;
    public static final int ANIMATION_TYPE_MOVE_LEFT = 2;
    public static final int ANIMATION_TYPE_MOVE_RIGHT = 3;
    public static final int ANIMATION_TYPE_DISSOLVE_VERTICALLY = 4;
    public static final int ANIMATION_TYPE_DISSOLVE_HORIZONTALLY = 5;

    private static final int TIME_SLEEP_MILLISECONDS = 25;


    private Handler handler = new Handler(Looper.getMainLooper());

    private View view;
    private int animationType;
    private ViewGroup.MarginLayoutParams layoutParams;

    private int displacement;
    private int step;

    private ViewGroup.MarginLayoutParams originalLayoutParams;
    private ViewGroup.MarginLayoutParams startLayoutParams;

    private AnimatorCallback animatorCallback;


    // Constructor
    public Animator(final View view) {
        this.view = view;

        layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        originalLayoutParams = new ViewGroup.MarginLayoutParams(layoutParams);

        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                layoutParams.width = originalLayoutParams.width = view.getWidth();
                layoutParams.height = originalLayoutParams.height = view.getHeight();

                ViewTreeObserver viewTreeObserver = view.getViewTreeObserver();
                if (Build.VERSION.SDK_INT >= 16) {
                    viewTreeObserver.removeOnGlobalLayoutListener(this);
                } else {
                    viewTreeObserver.removeGlobalOnLayoutListener(this);
                }
            }
        });

        new Thread(this).start();
    }


    // Set Animator Callback
    public void setAnimatorCallback(AnimatorCallback animatorCallback) {
        this.animatorCallback = animatorCallback;
    }


    // Reset
    public void reset() {
        layoutParams.topMargin = originalLayoutParams.topMargin;
        layoutParams.bottomMargin = originalLayoutParams.bottomMargin;
        layoutParams.leftMargin = originalLayoutParams.leftMargin;
        layoutParams.rightMargin = originalLayoutParams.rightMargin;

        layoutParams.width = originalLayoutParams.width;
        layoutParams.height = originalLayoutParams.height;

        view.requestLayout();
    }


    // Animate

    public void animate(int animationType, double durationInSeconds) {
        int displacement = 0;

        if (animationType == ANIMATION_TYPE_MOVE_UP || animationType == ANIMATION_TYPE_MOVE_DOWN
                || animationType == ANIMATION_TYPE_DISSOLVE_VERTICALLY) {
            displacement = view.getHeight();
        } else if (animationType == ANIMATION_TYPE_MOVE_LEFT || animationType == ANIMATION_TYPE_MOVE_RIGHT
                || animationType == ANIMATION_TYPE_DISSOLVE_HORIZONTALLY) {
            displacement = view.getWidth();
        }

        animate(animationType, displacement, durationInSeconds);
    }

    public void animate(int animationType, int displacement, double durationInSeconds) {
        this.animationType = animationType;
        this.displacement = displacement;

        startLayoutParams = new ViewGroup.MarginLayoutParams(layoutParams);

        int stepsCount = (int) (durationInSeconds * DateUtils.SECOND_IN_MILLIS / TIME_SLEEP_MILLISECONDS);
        step = displacement / stepsCount;
        if (step == 0) {
            step = 1;
        }

        synchronized (this) {
            this.notify();
        }
    }


    @Override
    public void run() {
        while (true) {
            synchronized (this) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            while (true) {
                if (animationType <= ANIMATION_TYPE_MOVE_RIGHT) {
                    int layoutMargin = 0, margin = 0;
                    int direction = 1;

                    if (animationType == ANIMATION_TYPE_MOVE_UP || animationType == ANIMATION_TYPE_MOVE_DOWN) {
                        layoutMargin = layoutParams.topMargin;
                        margin = startLayoutParams.topMargin;

                        if (animationType == ANIMATION_TYPE_MOVE_UP) {
                            direction = -1;
                        }
                    } else if (animationType == ANIMATION_TYPE_MOVE_LEFT || animationType == ANIMATION_TYPE_MOVE_RIGHT) {
                        layoutMargin = layoutParams.leftMargin;
                        margin = startLayoutParams.leftMargin;

                        if (animationType == ANIMATION_TYPE_MOVE_LEFT) {
                            direction = -1;
                        }
                    }

                    if (Math.abs(layoutMargin - margin) < displacement) {
                        layoutMargin += step * direction;
                        if (Math.abs(layoutMargin - margin) > displacement) {
                            layoutMargin = margin + displacement * direction;
                        }
                    } else {
                        break;
                    }

                    if (animationType == ANIMATION_TYPE_MOVE_UP || animationType == ANIMATION_TYPE_MOVE_DOWN) {
                        layoutParams.topMargin = layoutMargin;
                    } else if (animationType == ANIMATION_TYPE_MOVE_LEFT || animationType == ANIMATION_TYPE_MOVE_RIGHT) {
                        layoutParams.leftMargin = layoutMargin;
                    }
                } else {
                    int layoutDimension = 0, dimension = 0;

                    if (animationType == ANIMATION_TYPE_DISSOLVE_VERTICALLY) {
                        layoutDimension = layoutParams.height;
                        dimension = view.getHeight();
                    } else if (animationType == ANIMATION_TYPE_DISSOLVE_HORIZONTALLY) {
                        layoutDimension = layoutParams.width;
                        dimension = view.getWidth();
                    }

                    if (layoutDimension > 0) {
                        layoutDimension -= step;
                        if (layoutDimension < 0) {
                            layoutDimension = 0;
                        }
                    } else {
                        break;
                    }

                    if (animationType == ANIMATION_TYPE_DISSOLVE_VERTICALLY) {
                        layoutParams.height = layoutDimension;
                    } else if (animationType == ANIMATION_TYPE_DISSOLVE_HORIZONTALLY) {
                        layoutParams.width = layoutDimension;
                    }
                }

                handler.post(viewUpdater);

                try {
                    Thread.sleep(TIME_SLEEP_MILLISECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if (animatorCallback != null) {
                        animatorCallback.animationCompleted();
                    }
                }
            });
        }
    }


    // View Updater
    private Runnable viewUpdater = new Runnable() {
        @Override
        public void run() {
            view.requestLayout();
        }
    };

}
