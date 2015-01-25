package com.vitman.ABEBETKA;

import java.util.ArrayList;
import java.util.List;

public final class Words {

    public static final List<Integer> LETTER_A_WORDS = new ArrayList<Integer>();
    public static final List<Integer> LETTER_B_WORDS = new ArrayList<Integer>();
    public static final List<Integer> LETTER_V_WORDS = new ArrayList<Integer>();

    //init Letter a words
    static {
        LETTER_A_WORDS.add(R.layout.word_bus_layout);
        LETTER_A_WORDS.add(R.layout.word_pineapple_layout);
        LETTER_A_WORDS.add(R.layout.word_shark_layout);
    }

    //init Letter b words
    static {
        LETTER_B_WORDS.add(R.layout.word_drum_layout);
        LETTER_B_WORDS.add(R.layout.word_hippopotamus_layout);
        LETTER_B_WORDS.add(R.layout.word_sheep_layout);
    }

    //init Letter v words
    static {
        LETTER_V_WORDS.add(R.layout.word_crow_layout);
        LETTER_V_WORDS.add(R.layout.word_wolf_layout);
        LETTER_V_WORDS.add(R.layout.word_rainbow_layout);
    }
}
