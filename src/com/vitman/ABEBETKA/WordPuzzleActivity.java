package com.vitman.ABEBETKA;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
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

    private List<Integer> mWordsLayouts = new ArrayList<Integer>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initWordsLayouts();
        initAnimation();
        showNewWord();
        initMedia();
    }

    private void initWordsLayouts() {
        mWordsLayouts.add(R.layout.word_bus_layout);
        mWordsLayouts.add(R.layout.word_pineapple_layout);
        mWordsLayouts.add(R.layout.word_shark_layout);
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
        }
    }

    @Override
    public void onBackPressed() {}

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        final int X = (int) event.getRawX();
        final int Y = (int) event.getRawY();
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
        int[] rules = layoutParams.getRules();

        if(rules[10] == 0) {
            int left = view.getLeft();
            int top = view.getTop();

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

                startAnimationOnBlackLetters(view);
                xDelta = X - layoutParams.leftMargin;
                yDelta = Y - layoutParams.topMargin;
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
                        mMainLayout.removeAllViews();
                        showNewWord();
                    }
                    break;
                }
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

        float minBorderPositionX = correctPositionX - 100;
        float maxBorderPositionX = correctPositionX + 100;
        float minBorderPositionY = correctPositionY - 100;
        float maxBorderPositionY = correctPositionY + 100;

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

    private void initLayouts(int idLayout) {
        switch (idLayout) {
            case R.layout.word_pineapple_layout:
                setWordPuzzleLayouts(R.id.word_pineapple);
                startMediaPlayerNameOfWord(R.raw.pineapple);
                break;
            case R.layout.word_bus_layout:
                setWordPuzzleLayouts(R.id.word_bus);
                startMediaPlayerNameOfWord(R.raw.bus);
                break;
            case R.layout.word_shark_layout:
                setWordPuzzleLayouts(R.id.word_shark);
                startMediaPlayerNameOfWord(R.raw.shark);
                break;
        }
        initBackground();
        startWordsImageAnimation();
    }

    private void setWordPuzzleLayouts(int main) {
        mMainLayout = (RelativeLayout) findViewById(main);
    }

    private void startMediaPlayerNameOfWord(int nameOfWord) {
        mMediaPlayerNameOfWord = MediaPlayer.create(WordPuzzleActivity.this, nameOfWord);
        mMediaPlayerNameOfWord.start();
    }

    private void initViews() {
        mCountOfViews = mMainLayout.getChildCount();
        mAmountLettersOfWord = (mCountOfViews - 1) / 2;
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
}

