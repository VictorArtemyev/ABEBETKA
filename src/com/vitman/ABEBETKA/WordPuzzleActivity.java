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
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("deprecation")
public class WordPuzzleActivity extends Activity implements View.OnTouchListener {

    private static final String IMAGES_TAG = "image_black";
    private static final String BLACK_LETTERS_TAG = "_black";

    private int xDelta;
    private int yDelta;

    private int mAmountLettersOfWord = 1;
    private int mCountOfLettersMatches;
    private int mCountOfViews;

    private List<View> mAllViews = new ArrayList<View>();
    private List<View> mBlackLettersViews = new ArrayList<View>();

    private View mCorrectWordsLetterSpot;

    private RelativeLayout mMainLayout;
    private WeakReference<Bitmap> mBackground;

    private Animation mAnimationShake;
    private Animation mAnimationAppear;
    private MediaPlayer mMediaPlayerBackground;
    private MediaPlayer mMediaPlayerSound;
    private MediaPlayer mMediaPlayerNameOfWord;

    private int mLetterId;

    private List<Integer> mWordsLayouts;
    private RelativeLayout.LayoutParams mLayoutParams;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent() != null) {
            mLetterId = getIntent().getIntExtra(LettersTag.LETTER_LAYOUT, 0);
            Log.d("DEV", String.valueOf(mLetterId));
        }

        initWordsLayouts(mLetterId);
        initAnimation();
        showNewWord();
        initMedia();
    }

    private void initWordsLayouts(int id) {
        switch (id) {
            case R.layout.animation_a_letter_layout:
                mWordsLayouts = Words.LETTER_A_WORDS;
                break;
            case R.layout.animation_b_letter_layout:
                mWordsLayouts = Words.LETTER_B_WORDS;
                break;
            case R.layout.animation_v_letter_layout:
                mWordsLayouts = Words.LETTER_V_WORDS;
                break;
            default:
                break;
        }
        Collections.shuffle(mWordsLayouts);
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
    protected void onRestart() {
        initBackground();
        initMedia();
        super.onRestart();
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.home_button:
                startActivity(new Intent(WordPuzzleActivity.this, ChoiceOfLetterActivity.class));
                break;
            case R.id.next_button:
                showNewWord();
                break;
        }
    }

    @Override
    public void onBackPressed() {}

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        final int X = (int) event.getRawX();
        final int Y = (int) event.getRawY();
        mLayoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
        setLayoutParamsForView(view);

        switch (event.getAction() & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN:
                view.setScaleX(1.5f);
                view.setScaleY(1.5f);

                startAnimationOnBlackLetters(view);
                xDelta = X - mLayoutParams.leftMargin;
                yDelta = Y - mLayoutParams.topMargin;
                break;

            case MotionEvent.ACTION_UP:
                view.setScaleX(1f);
                view.setScaleY(1f);
                stopAnimationOnBlackLetters();
                boolean isCorrectPosition = checkLetterPosition(view);
                if (isCorrectPosition) {
                    mMediaPlayerSound.start();
                    view.setOnTouchListener(null);
                    mCorrectWordsLetterSpot.setVisibility(View.INVISIBLE);
                    mCountOfLettersMatches++;
                    if (checkCompletedWord()) {
                        mMediaPlayerNameOfWord.start();
                       pause();
                    }
                    break;
                }
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                break;

            case MotionEvent.ACTION_POINTER_UP:
                break;

            case MotionEvent.ACTION_MOVE:

                mLayoutParams.leftMargin = X - xDelta;
                mLayoutParams.topMargin = Y - yDelta;
                mLayoutParams.rightMargin = -50;
                mLayoutParams.bottomMargin = -50;

                view.setLayoutParams(mLayoutParams);
                break;
        }
        mMainLayout.invalidate();
        return true;
    }

    //sets layout params for view
    private void setLayoutParamsForView(View view) {
        int[] rules = mLayoutParams.getRules();
        if (rules[10] == 0) {
            int left = view.getLeft();
            int top = view.getTop();

            mLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
            mLayoutParams.addRule(RelativeLayout.RIGHT_OF, 0);
            mLayoutParams.addRule(RelativeLayout.LEFT_OF, 0);
            mLayoutParams.addRule(RelativeLayout.ALIGN_TOP, 0);
            mLayoutParams.addRule(RelativeLayout.ALIGN_BOTTOM, 0);
            mLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);

            mLayoutParams.leftMargin = left;
            mLayoutParams.topMargin = top;

            view.setLayoutParams(mLayoutParams);
        }
    }

    //starts animation on black letters
    private void startAnimationOnBlackLetters(View colorLetterView) {
        String viewsTag = String.valueOf(colorLetterView.getTag());
        String firstCharOfLettersTag = String.valueOf(viewsTag.charAt(0));
        for (View view : mBlackLettersViews) {
            if (String.valueOf(view.getTag()).startsWith(firstCharOfLettersTag)) {
                view.startAnimation(mAnimationShake);
            }
        }
    }

    //stops animation on black letters
    private void stopAnimationOnBlackLetters() {
        for (View view : mBlackLettersViews) {
                view.clearAnimation();
            }
    }

    private void setListenerOnViews(List<View> views) {
        for (View view : views) {
            if (!view.getTag().toString().endsWith(BLACK_LETTERS_TAG)) {
                view.setOnTouchListener(WordPuzzleActivity.this);
            }
        }
    }

    //gets correct related black letters view
    private View getRelatedLetterView(View colorLetter) {
        View correctView = null;
        String viewsTag = (String) colorLetter.getTag();
        String firstCharOfLettersTag = String.valueOf(viewsTag.charAt(0));

        for (View blackLetter : mBlackLettersViews) {
            if (blackLetter.getTag().toString().startsWith(firstCharOfLettersTag)) {
                if (checkRelativePosition(colorLetter, blackLetter)) {
                    correctView = blackLetter;
                }
            }
        }
        mBlackLettersViews.remove(correctView);
        return correctView;
    }

    //checks relative position of a color letter and a black letter
    private boolean checkRelativePosition(View colorLetter, View blackLetter) {
        float positionX = colorLetter.getX();
        float positionY = colorLetter.getY();

        float correctPositionX = blackLetter.getX();
        float correctPositionY = blackLetter.getY();

        float minBorderPositionX = correctPositionX - 50;
        float maxBorderPositionX = correctPositionX + 50;
        float minBorderPositionY = correctPositionY - 50;
        float maxBorderPositionY = correctPositionY + 50;

        return (positionX >= minBorderPositionX && positionX <= maxBorderPositionX) &&
                (positionY >= minBorderPositionY && positionY <= maxBorderPositionY);
    }

    private boolean checkLetterPosition(View view) {
        mCorrectWordsLetterSpot = getRelatedLetterView(view);
        return mCorrectWordsLetterSpot != null;
    }

    //checks if word is completed
    private boolean checkCompletedWord() {
        return mAmountLettersOfWord == mCountOfLettersMatches;
    }

    private void showNewWord() {
        if (allWordsAreCompleted()) {
            startActivity(new Intent(WordPuzzleActivity.this, ChoiceOfLetterActivity.class));
            return;
        }
        setContentView(mWordsLayouts.get(0));
        initLayouts(mWordsLayouts.get(0));
        initViews();
        mWordsLayouts.remove(0);
    }

    //TODO to simplify it
    private void initLayouts(int idLayout) {
        switch (idLayout) {
            case R.layout.word_pineapple_layout:
                setWordPuzzleLayouts(R.id.word_pineapple);
                startMediaPlayerNameOfWord(R.raw.pronunciation_word_pineapple);
                break;
            case R.layout.word_bus_layout:
                setWordPuzzleLayouts(R.id.word_bus);
                startMediaPlayerNameOfWord(R.raw.pronunciation_word_bus);
                break;
            case R.layout.word_shark_layout:
                setWordPuzzleLayouts(R.id.word_shark);
                startMediaPlayerNameOfWord(R.raw.pronunciation_word_shark);
                break;
            case R.layout.word_drum_layout:
                setWordPuzzleLayouts(R.id.word_drum);
                startMediaPlayerNameOfWord(R.raw.pronunciation_word_baraban);
                break;
            case R.layout.word_hippopotamus_layout:
                setWordPuzzleLayouts(R.id.word_hippopotamus);
                startMediaPlayerNameOfWord(R.raw.pronunciation_word_begemot);
                break;
            case R.layout.word_sheep_layout:
                setWordPuzzleLayouts(R.id.word_sheep);
                startMediaPlayerNameOfWord(R.raw.pronunciation_word_baran);
                break;
            case R.layout.word_crow_layout:
                setWordPuzzleLayouts(R.id.word_crow);
                startMediaPlayerNameOfWord(0);
                break;
            case R.layout.word_rainbow_layout:
                setWordPuzzleLayouts(R.id.word_rainbow);
                startMediaPlayerNameOfWord(R.raw.pronunciation_word_rainbow);
                break;
            case R.layout.word_wolf_layout:
                setWordPuzzleLayouts(R.id.word_wolf);
                startMediaPlayerNameOfWord(R.raw.pronunciation_word_wolf);
                break;

        }
        initBackground();
        startWordsImageAnimation();
    }

    private void setWordPuzzleLayouts(int main) {
        mMainLayout = (RelativeLayout) findViewById(main);
    }

    private void startMediaPlayerNameOfWord(int nameOfWord) {
        if(nameOfWord == 0) {
            mMediaPlayerNameOfWord.stop();
            return;
        }
        mMediaPlayerNameOfWord = MediaPlayer.create(WordPuzzleActivity.this, nameOfWord);
        mMediaPlayerNameOfWord.start();
    }

    private void initViews() {
        mCountOfViews = mMainLayout.getChildCount();
        mAmountLettersOfWord = (mCountOfViews - 2) / 2;
        mCountOfLettersMatches = 0;
        for (int i = 0; i < mCountOfViews; i++) {
            View view = mMainLayout.getChildAt(i);
            mAllViews.add(view);
            if (String.valueOf(view.getTag()).endsWith(BLACK_LETTERS_TAG)) {
                mBlackLettersViews.add(view);
            }
        }
        setListenerOnViews(mAllViews);
    }

    private void startWordsImageAnimation() {
        mMainLayout.findViewWithTag(IMAGES_TAG).startAnimation(mAnimationAppear);
    }

    private boolean allWordsAreCompleted() {
        return mWordsLayouts.isEmpty();
    }

    private void initBackground() {
        mBackground = new WeakReference<Bitmap>(BitmapFactory.decodeResource
                (getResources(), R.drawable.background_word_puzzle));
        mMainLayout.setBackground(new BitmapDrawable(mBackground.get()));
    }

    private void initMedia() {
        mMediaPlayerSound = MediaPlayer.create(WordPuzzleActivity.this, R.raw.match_sound);
        mMediaPlayerBackground = MediaPlayer.create(this, R.raw.wordpuzzle_background_music);
        mMediaPlayerBackground.start();
        mMediaPlayerBackground.setLooping(true);
    }

    private void initAnimation() {
        mAnimationShake = AnimationUtils.loadAnimation
                (WordPuzzleActivity.this, R.anim.shake_letter);
        mAnimationAppear = AnimationUtils.loadAnimation
                (WordPuzzleActivity.this, R.anim.appear_word_image);
    }

    private void clearActivity() {
        mMainLayout.setBackground(null);
        mMediaPlayerBackground.stop();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mMainLayout.removeAllViews();
            showNewWord();
        }
    };

    public void pause() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                handler.sendEmptyMessage(0);
            }
        }).start();
    }
}

