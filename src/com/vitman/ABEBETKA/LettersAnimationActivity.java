package com.vitman.ABEBETKA;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public class LettersAnimationActivity extends Activity {

    private WeakReference<Bitmap> mBackGround;

    private RelativeLayout mLettersMoveLayout;
    private RelativeLayout mLayoutForBackground;

    private List<View> mAllViews = new ArrayList<View>();
    private MediaPlayer mMediaPlayerBackground;
    private MediaPlayer mMediaPlayerLettersSong;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int layout = 0;
        if (getIntent() != null) {
            layout = getIntent().getIntExtra(LettersTag.LETTER_LAYOUT, 0);
        }

        setContentView(layout);
        initLayouts(layout);

        int mCountOfViews = mLettersMoveLayout.getChildCount();

        for (int i = 0; i < mCountOfViews; i++) {
            mAllViews.add(mLettersMoveLayout.getChildAt(i));
        }

        startMediaPlayerBackground();
        setAnimationOnView(mAllViews);
        isMediaPlaying();
    }


    private void startMediaPlayerBackground() {
        mMediaPlayerBackground = MediaPlayer.create
                (LettersAnimationActivity.this, R.raw.letters_animation_background_music);
        mMediaPlayerBackground.start();
        mMediaPlayerBackground.setLooping(true);
        mMediaPlayerBackground.setVolume(0.3f, 0.3f);
    }

    //initializes current layouts
    private void initLayouts(int layout) {
        switch (layout) {
            case R.layout.a_letter_animation_layout :
                setLettersAnimationLayouts(R.id.a_background, R.id.a_move);
                startLettersSong(R.raw.song_a);
                break;
            case R.layout.b_letter_animation_layout :
                setLettersAnimationLayouts(R.id.b_background, R.id.b_move);
                startLettersSong(R.raw.song_b);
                break;
            case R.layout.v_letter_animation_layout:
                setLettersAnimationLayouts(R.id.c_background, R.id.c_move);
                startLettersSong(R.raw.song_c);
                break;
            default:
                break;
        }
     }

    private void setLettersAnimationLayouts(int background, int move) {
        mLayoutForBackground = (RelativeLayout) findViewById(background);
        mLettersMoveLayout = (RelativeLayout) findViewById(move);
    }

    private void startLettersSong(int lettersSong) {
        mMediaPlayerLettersSong = MediaPlayer.create(LettersAnimationActivity.this, lettersSong);
        mMediaPlayerLettersSong.start();
    }

    //setup animation on letters limbs
    private void setAnimationOnView(List<View> views) {
        for (View view : views) {
            Object tag = view.getTag();
            if (tag != null) {
                if (tag.equals(LettersTag.TAG_LEFT_HAND)) {
                    view.startAnimation(AnimationUtils.loadAnimation
                            (LettersAnimationActivity.this, R.anim.shake_hand));
                }

                if (tag.equals(LettersTag.TAG_RIGHT_HAND)) {
                    view.startAnimation(AnimationUtils.loadAnimation
                            (LettersAnimationActivity.this, R.anim.shake_hand));
                }

                if (tag.equals(LettersTag.TAG_HAT)) {
                    view.startAnimation(AnimationUtils.loadAnimation
                            (LettersAnimationActivity.this, R.anim.move_hat));
                }

                if (tag.equals(LettersTag.TAG_RIGHT_LEG)) {
                    view.startAnimation(AnimationUtils.loadAnimation
                            (LettersAnimationActivity.this, R.anim.move_leg));
                }

                if (tag.equals(LettersTag.TAG_LEFT_LEG)) {
                    view.startAnimation(AnimationUtils.loadAnimation
                            (LettersAnimationActivity.this, R.anim.move_leg));
                }

                if (tag.equals(LettersTag.TAG_MOUTH)) {
                    view.startAnimation(AnimationUtils.loadAnimation
                            (LettersAnimationActivity.this, R.anim.move_mouth));
                }

                if(tag.equals(LettersTag.TAG_EYES)) {
                    view.startAnimation(AnimationUtils.loadAnimation
                            (LettersAnimationActivity.this, R.anim.close_eyes));
                }
            }
        }
    }

    @Override
    protected void onStop() {
        clearActivity();
        super.onStop();
    }

    @Override
    protected void onRestart() {
        initBackground();
        super.onRestart();
    }

    @Override
    protected void onResume() {
        initBackground();
        super.onResume();
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

    @Override
    public void onBackPressed() {
//        startActivity(new Intent(LettersAnimationActivity.this, ChoiceOfLetterActivity.class));
//        super.onBackPressed();
    }

    public void onClick(View view) {
        switch (view.getId()) {
//            case R.id.Next_Button :
//                startActivity(new Intent(LettersAnimationActivity.this, WordPuzzleActivity.class));
//                break;
            case R.id.home_button :
                startActivity(new Intent(LettersAnimationActivity.this, ChoiceOfLetterActivity.class));
                break;
        }
    }

    //checks if media still plays
    private synchronized boolean checkMediaPlaying() {
        return mMediaPlayerLettersSong.isPlaying();
    }

    //if media is stopped setup image button with animation
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
//            mMediaPlayerBackground.setVolume(1f, 1f);
//            Animation animation = AnimationUtils.loadAnimation(LettersAnimationActivity.this, R.anim.appear_button);
//            super.handleMessage(msg);
//            mNextButton.setVisibility(View.VISIBLE);
//            mNextButton.startAnimation(animation);
//
//            mHomeButton.setVisibility(View.VISIBLE);
//            mHomeButton.startAnimation(animation);
            startActivity(new Intent(LettersAnimationActivity.this, WordPuzzleActivity.class));
        }
    };

    //checks in another thread if media still plays
    public void isMediaPlaying() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                  if(!checkMediaPlaying()) {
                      break;
                  }
                }
                handler.sendEmptyMessage(0);
            }
        }).start();
    }

    private void initBackground() {
        mBackGround = new WeakReference<Bitmap>(BitmapFactory.decodeResource(getResources(), R.drawable.background_color));
        mLayoutForBackground.setBackground(new BitmapDrawable(mBackGround.get()));
    }

    private void clearActivity() {
        mLayoutForBackground.setBackground(null);
        mMediaPlayerBackground.stop();
    }
}