package com.marco.animations;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

public class AnimationsActivity extends AppCompatActivity implements View.OnClickListener, Animator.AnimatorCallback {

    private ImageView imageView_android;

    private ImageButton button_up;
    private ImageButton button_left;
    private ImageButton button_down;
    private ImageButton button_right;

    private Animator animator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animations);

        initViews();

        animator = new Animator(imageView_android);
        animator.setAnimatorCallback(this);
    }


    // Initialize Views
    private void initViews() {
        imageView_android = (ImageView) findViewById(R.id.imageView_android);
        button_up = (ImageButton) findViewById(R.id.button_up);
        button_left = (ImageButton) findViewById(R.id.button_left);
        button_down = (ImageButton) findViewById(R.id.button_down);
        button_right = (ImageButton) findViewById(R.id.button_right);

        button_up.setOnClickListener(this);
        button_left.setOnClickListener(this);
        button_down.setOnClickListener(this);
        button_right.setOnClickListener(this);
    }


    // Set Buttons Enabled
    private void setButtonsEnabled(boolean enabled) {
        button_up.setEnabled(enabled);
        button_left.setEnabled(enabled);
        button_down.setEnabled(enabled);
        button_right.setEnabled(enabled);
    }


    @Override
    public void onClick(View view) {
        int animationType = Animator.ANIMATION_TYPE_MOVE_UP;
        if (view == button_up) {
            animationType = Animator.ANIMATION_TYPE_MOVE_UP;
        } else if (view == button_left) {
            animationType = Animator.ANIMATION_TYPE_MOVE_LEFT;
        } else if (view == button_down) {
            animationType = Animator.ANIMATION_TYPE_MOVE_DOWN;
        } else if (view == button_right) {
            animationType = Animator.ANIMATION_TYPE_MOVE_RIGHT;
        }

        setButtonsEnabled(false);

        animator.animate(animationType, 1.5);
    }


    @Override
    public void animationCompleted() {
        animator.reset();
        setButtonsEnabled(true);
    }

}
