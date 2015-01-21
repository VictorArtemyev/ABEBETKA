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

public class ChoiceOfLetterActivity extends Activity {
    private int mCountOfViews;
    private ArrayList<View> mAllViews = new ArrayList<View>();

    private Intent mIntent;
    private WeakReference<Bitmap> mBackground;
    private RelativeLayout mLayout;
    private MediaPlayer mMediaPlayerBackground;
    private MediaPlayer mMediaPlayerGreeting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choice_of_letter_layout);
        initLayout();
        setAnimationOnViews(mAllViews);
        initBackground();
        initMedia();
        isMediaPlaying();
    }

    private void initLayout() {
        mLayout = (RelativeLayout) findViewById(R.id.choice_letters);

        mCountOfViews = mLayout.getChildCount();

        for (int i = 0; i < mCountOfViews; i++) {
            mAllViews.add(mLayout.getChildAt(i));
        }
    }

    public void onClick(View view) {
        mIntent = new Intent(ChoiceOfLetterActivity.this, LetterPuzzleActivity.class);
        switch (view.getId()) {
            case R.id.A_Button :
                startMediaPlayerGreeting(R.raw.greeting_letter_a);
                mIntent.putExtra(LettersTag.LETTER_LAYOUT, R.layout.puzzle_a_letter_layout);
                startActivity(mIntent);
                break;
            case R.id.B_Button :
                startMediaPlayerGreeting(R.raw.greeting_letter_b);
                mIntent.putExtra(LettersTag.LETTER_LAYOUT, R.layout.puzzle_b_letter_layout);
                startActivity(mIntent);
                break;
            case R.id.C_Button :
                startMediaPlayerGreeting(R.raw.greeting_letter_c);
                mIntent.putExtra(LettersTag.LETTER_LAYOUT, R.layout.puzzle_v_letter_layout);
                startActivity(mIntent);
                break;
            default :
                break;
        }
    }

    @Override
    protected void onStop() {
        clearActivity();
        super.onStop();
    }

    @Override
    protected void onPause() {
        clearActivity();
        super.onPause();
    }

    @Override
    protected void onRestart() {
        initBackground();
        initMedia();
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        clearActivity();
        super.onDestroy();
    }

    private void initMedia() {
        mMediaPlayerBackground = MediaPlayer.create(this, R.raw.choiceofletter_background_music);
        mMediaPlayerBackground.start();
        mMediaPlayerBackground.setLooping(true);
        startMediaPlayerGreeting(R.raw.hello_);
    }

    private void initBackground() {
        mBackground = new WeakReference<Bitmap>(BitmapFactory.decodeResource(getResources(), R.drawable.background_choice_letters));
        mLayout.setBackground(new BitmapDrawable(mBackground.get()));
    }

    private void clearActivity() {
        mMediaPlayerBackground.stop();
        mLayout.setBackground(null);
        mLayout.removeAllViews();
    }

    public void setAnimationOnViews(ArrayList<View> views) {
        for(View view : views) {
            view.startAnimation(AnimationUtils.loadAnimation(ChoiceOfLetterActivity.this, R.anim.shake));
        }
    }


    private void startMediaPlayerGreeting(int lettersGreeting) {
        mMediaPlayerGreeting = MediaPlayer.create(ChoiceOfLetterActivity.this, lettersGreeting);
        mMediaPlayerGreeting.start();
    }

    //checks if media still plays
    private synchronized boolean checkMediaPlaying() {
        return mMediaPlayerGreeting.isPlaying();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            startMediaPlayerGreeting(R.raw.lets_start);
        }
    };

    //checks in another thread if media still plays
    public void isMediaPlaying() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if(!checkMediaPlaying()) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }
                handler.sendEmptyMessage(0);
            }
        }).start();
    }
}
