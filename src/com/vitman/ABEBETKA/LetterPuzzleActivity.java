package com.vitman.ABEBETKA;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;

@SuppressWarnings("deprecation")
public class LetterPuzzleActivity extends Activity implements View.OnTouchListener {

    private static final int MAX_LETTERS_LIMBS = 1;

    private int mCountPartOfLetter;
    private int xDelta;
    private int yDelta;

    private ArrayList<View> mAllViews = new ArrayList<View>();
    private int mCountOfViews;
    private int mLayoutId;

    private RelativeLayout mMainLayout;
    private RelativeLayout mLetterLayout;

    private MediaPlayer mMediaPlayerBackground;
    private MediaPlayer mMediaPlayerSound;
    private WeakReference<Bitmap> mBackground;
    private Animation mAnimation;
    private Intent mIntent;
    private View correctLettersPartSpot;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent() != null) {
            mLayoutId = getIntent().getIntExtra(LettersTag.LETTER_LAYOUT, 0);
        }

        mAnimation = AnimationUtils.loadAnimation(LetterPuzzleActivity.this, R.anim.shake);

        setContentView(mLayoutId);
        setLayout(mLayoutId);

        initBackground();
        initMedia();

        mCountOfViews = mMainLayout.getChildCount();

        for (int i = 0; i < mCountOfViews; i++) {
            mAllViews.add(mMainLayout.getChildAt(i));
        }
        setListenerAndAnimationOnViewByTag(mAllViews);
    }

    private void setLayout(int layout) {
        switch (layout) {
            case R.layout.a_puzzle_letter_layout:
                mMainLayout = (RelativeLayout) findViewById(R.id.a_letter_layout);
                break;
            case R.layout.b_puzzle_letter_layout:
               mMainLayout = (RelativeLayout) findViewById(R.id.b_letter_layout);
                break;
            case R.layout.c_puzzle_letter_layout:
                mMainLayout = (RelativeLayout) findViewById(R.id.c_letter_layout);
            case R.layout.test :
                mMainLayout = (RelativeLayout) findViewById(R.id.b_letter_layout);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onPause() {
        clearActivity();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        clearActivity();
        super.onDestroy();
    }

    private void clearActivity() {
        mMediaPlayerBackground.stop();
        mMainLayout.setBackground(null);
        mMainLayout.removeAllViews();
    }

    @Override
    protected void onRestart() {
        initBackground();
        initMedia();
        super.onRestart();
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        final int X = (int) event.getRawX();
        final int Y = (int) event.getRawY();
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
        int[] rules = layoutParams.getRules();
        Log.d("DEV", Arrays.toString(rules));
        if(rules[10] == 0) {
            Log.d("DEV", "in if");
            int left = view.getLeft();
            int right = view.getRight();
            int top = view.getTop();
            int bottom = view.getBottom();
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
            layoutParams.addRule(RelativeLayout.RIGHT_OF, 0);
            layoutParams.addRule(RelativeLayout.LEFT_OF, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_BOTTOM, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
            layoutParams.leftMargin = left;
            layoutParams.topMargin = top;
            view.setLayoutParams(layoutParams);
        }

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                view.setScaleX(1.5f);
                view.setScaleY(1.5f);
                xDelta = X - layoutParams.leftMargin;
                yDelta = Y - layoutParams.topMargin;
                view.clearAnimation();
                break;

            case MotionEvent.ACTION_UP:
                view.setScaleX(1f);
                view.setScaleY(1f);
                boolean isCorrectPosition = checkViewPosition(view);
                Log.d("DEV", String.valueOf(isCorrectPosition));
                if (isCorrectPosition) {
                    mMediaPlayerSound.start();
                    view.clearAnimation();
                    view.setOnTouchListener(null);
                    correctLettersPartSpot.setVisibility(View.INVISIBLE);
                    mCountPartOfLetter++;
                    startLettersAnimation(checkCompletedLetter());
                    break;
                }
                view.startAnimation(mAnimation);
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                break;

            case MotionEvent.ACTION_POINTER_UP:
                break;

            case MotionEvent.ACTION_MOVE:
                layoutParams.leftMargin = X - xDelta;
                layoutParams.topMargin = Y - yDelta;

                layoutParams.rightMargin = -50;
                layoutParams.bottomMargin = -50;
                view.setLayoutParams(layoutParams);
                break;
        }
        mMainLayout.invalidate();
        return true;
    }

    // checks relatively separated part of letter to correct position
    public boolean checkViewPosition(View view) {
        float positionX = view.getX();
        float positionY = view.getY();

        Log.d("DEV", String.valueOf(positionX));
        Log.d("DEV", String.valueOf(positionY));

        correctLettersPartSpot = getCorrectLettersPartView(view.getTag());

//        correctLettersPartSpot.startAnimation(AnimationUtils.loadAnimation(LetterPuzzleActivity.this, R.anim.shake));
        float correctPositionX = correctLettersPartSpot.getX();
        float correctPositionY = correctLettersPartSpot.getY();

        float minBorderPositionX = correctPositionX - 100;
        float maxBorderPositionX = correctPositionX + 100;
        float minBorderPositionY = correctPositionY - 100;
        float maxBorderPositionY = correctPositionY + 100;

        Log.d("DEV", "correctPositionX: " + correctPositionX);
        Log.d("DEV", "correctPositionY: " + correctPositionY);

        return (positionX >= minBorderPositionX && positionX <= maxBorderPositionX) &&
                (positionY >= minBorderPositionY && positionY <= maxBorderPositionY);

    }

    //starts letters animation if all parts of letters body in correct place
    private void startLettersAnimation(boolean isLetterCompleted) {
        if (isLetterCompleted) {
            mMainLayout.removeAllViewsInLayout();
            mMediaPlayerBackground.stop();
            initIntent();
            playLettersAnimation(mIntent);
        }
    }

    private void initIntent() {
        mIntent = new Intent(LetterPuzzleActivity.this, LettersAnimationActivity.class);
        switch (mLayoutId) {
            case R.layout.a_puzzle_letter_layout:
                mIntent.putExtra(LettersTag.LETTER_LAYOUT, R.layout.a_letter_animation_layout);
                break;
            case R.layout.b_puzzle_letter_layout:
                mIntent.putExtra(LettersTag.LETTER_LAYOUT, R.layout.b_letter_animation_layout);
                break;
            case R.layout.c_puzzle_letter_layout:
                mIntent.putExtra(LettersTag.LETTER_LAYOUT, R.layout.c_letter_animation_layout);
                break;
        }
    }

    //starts new activity with letters animation
    private void playLettersAnimation(Intent intent) {
        if (intent != null) {
            startActivity(intent);
        }
    }

    //checks if letter is completed
    private boolean checkCompletedLetter() {
        return mCountPartOfLetter == MAX_LETTERS_LIMBS;
    }

    //setup OnTouchListener and Animation separated part of letters by tag
    private void setListenerAndAnimationOnViewByTag(ArrayList<View> views) {

        for (View view : views) {
            Object tag = view.getTag();
            if (tag != null) {
                if (tag.equals(LettersTag.TAG_LEFT_HAND)) {
                    setListenerAndAnimationOnView(view);
                }

                if (tag.equals(LettersTag.TAG_RIGHT_HAND)) {
                    setListenerAndAnimationOnView(view);
                }

                if (tag.equals(LettersTag.TAG_HAT)) {
                    setListenerAndAnimationOnView(view);
                }

                if (tag.equals(LettersTag.TAG_RIGHT_LEG)) {
                    setListenerAndAnimationOnView(view);
                }

                if (tag.equals(LettersTag.TAG_LEFT_LEG)) {
                    setListenerAndAnimationOnView(view);
                }
            }
        }
    }

    //setup OnTouchListener and Animation separated part of letters
    private void setListenerAndAnimationOnView(View view) {
        view.setOnTouchListener(LetterPuzzleActivity.this);
        view.startAnimation(mAnimation);
    }

    //gets correct view of letters part
    private View getCorrectLettersPartView(Object tag) {
        View view = null;

        if (tag.equals(LettersTag.TAG_LEFT_HAND)) {
            view = findViewById(R.id.position_left_hand);
        } else if (tag.equals(LettersTag.TAG_RIGHT_HAND)) {
            view = findViewById(R.id.position_right_hand);
        } else if (tag.equals(LettersTag.TAG_HAT)) {
            view = findViewById(R.id.position_hat);
        } else if (tag.equals(LettersTag.TAG_RIGHT_LEG)) {
            view = findViewById(R.id.position_right_leg);
        } else if (tag.equals(LettersTag.TAG_LEFT_LEG)) {
            view = findViewById(R.id.position_left_leg);
        }
        return view;
    }

    private void initBackground() {
        mBackground = new WeakReference<Bitmap>(BitmapFactory.decodeResource(getResources(), R.drawable.background_black));
        mMainLayout.setBackground(new BitmapDrawable(mBackground.get()));
    }

    private void initMedia() {
        mMediaPlayerSound = MediaPlayer.create(this, R.raw.match_sound);
        mMediaPlayerBackground = MediaPlayer.create(this, R.raw.letterpuzzle_background_music);
        mMediaPlayerBackground.start();
        mMediaPlayerBackground.setLooping(true);
    }
}