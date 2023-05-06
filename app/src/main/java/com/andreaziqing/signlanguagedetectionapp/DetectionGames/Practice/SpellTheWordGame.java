package com.andreaziqing.signlanguagedetectionapp.DetectionGames.Practice;

import com.andreaziqing.signlanguagedetectionapp.Database.UserStatsDatabase;
import com.andreaziqing.signlanguagedetectionapp.DetectionGames.BetweenGamesActivity;
import com.andreaziqing.signlanguagedetectionapp.Detector.DetectorActivity;
import com.andreaziqing.signlanguagedetectionapp.R;
import com.andreaziqing.signlanguagedetectionapp.Detector.TFLiteInterpreter.Detector;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


/**
 *     Guessing the words game.
 *
 *     This class is in charge of the logic regarding the practice game with timing where users will
 *     have to correctly guess the appearing words and doing one by one their spelling letters the word
 *     is composed of. If they fail and countdown timer is set off for 3 times, game will be lost.
 */
public class SpellTheWordGame extends DetectorActivity {

    private static final String SPELL_THE_WORD_GAME = "SpellTheWordGame";

    Context context;

    // View and Layout for the word composed of several letters.
    TextView mWord;
    RelativeLayout mCardWord;
    LinearLayout mLetterSpelling;

    volatile TextView[] arrLetter;

    // Thread in charge of controlling main game logic
    Thread cardDetectionThread;
    // To control the thread stop/interrupt logic.
    volatile boolean activityStopped = false;
    volatile boolean threadIsInterrupted = false;

    // Countdown timer variables
    public int counter;
    TextView time;
    volatile boolean isTimesOut = false;

    // DB instances for updating user stats
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    UserStatsDatabase userStatsDB = new UserStatsDatabase();

    // MediaPlayer used for playing sound effect on valid detections and on times-out incorrect guess.
    public MediaPlayer mpCorrect;
    public MediaPlayer mpWrong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spell_the_word_game);

        context = getApplicationContext();

        mpCorrect = MediaPlayer.create(context, R.raw.correct);
        mpWrong = MediaPlayer.create(context, R.raw.wrong);

        // Countdown timer UI
        time = findViewById(R.id.countdown_timer);

        mWord = findViewById(R.id.word);
        mCardWord = findViewById(R.id.card_word);
        mLetterSpelling = findViewById(R.id.letter_spelling);

        // A full word is randomly chosen from the word list.
        arrLetter = setRandomWord(mCardWord,false);

        // TODO: Escoger palabra según temática
    }

    @Override
    public synchronized void onResume() {
        super.onResume();

        // We will need these handlers to manage the UI changes.
        final Handler handler = new Handler();
        final Handler handler2 = new Handler();
        final Handler handler3 = new Handler();
        final Handler handler4 = new Handler();

        // The CardDectectionThread will be in charge of running this runnable that performs the iterative
        // checking of the detected signs and showed words in screen as well as the main game control logic
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Log.d(SPELL_THE_WORD_GAME, "Thread in background: "+ Thread.currentThread().getName());
                synchronized (this) {
                    for (int cycle = 0; cycle < 4; cycle++) {
                        try { // we give time for arrLetter to update
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Log.d(SPELL_THE_WORD_GAME, "["+ Thread.currentThread()+ "]" + "Cycle #"+ cycle + " Word : "+ arrLetter[0].getText().toString());
                        int letterIdx = 0;
                        final int[] chances = {3};
                        // Runnable in charge of the CountDown timer logic. Controls logic for time passing and time out alarm.
                        class InitTimerRunnable implements Runnable {
                            CountDownTimer countDownTimer;

                            InitTimerRunnable() {}
                            public void run() {
                                Log.d(SPELL_THE_WORD_GAME, "["+ Thread.currentThread()+ "]" + "Starting timer.");
                                countDownTimer = new CountDownTimer(30000, 1000) {
                                    @Override
                                    public void onTick(long millisUntilFinished) {
                                        time.setText((int) (millisUntilFinished / 1000) + "");
                                        counter++;
                                    }

                                    @Override
                                    public void onFinish() {
                                        // Logic handling for when timer runs out of time
                                        chances[0]--;
                                        if (chances[0] == 0) {
                                            isTimesOut = true;
                                        } else {
                                            try {
                                                mpWrong.start();
                                            } catch (Exception e){
                                                Log.d(SPELL_THE_WORD_GAME, "Mediaplayer not initiated");
                                            }

                                            AlertDialog.Builder builder = new AlertDialog.Builder(SpellTheWordGame.this);
                                            builder.setMessage(getString(R.string.you_have) + " " + chances[0] + " " + getString(R.string.chances_left))
                                                    .setTitle(R.string.ups)
                                                    .setPositiveButton(R.string.got_it, new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            countDownTimer.start();
                                                        }
                                                    })
                                                    .show();
                                        }
                                    }
                                };
                                countDownTimer.start();
                            }
                        }

                        InitTimerRunnable initTimerRunnable = new InitTimerRunnable();
                        // Timer starts
                        handler4.postDelayed(initTimerRunnable, 100);

                        // For this word, we go 1 by 1 checking the word signs
                        for (TextView letter: arrLetter) {

                            // Wait for letter validation
                            while (!checkLetter(letter, activityStopped)) { }

                            if (activityStopped) {
                                return;
                            }
                            // Once the letter has been correctly detected, its color will be updated (green)
                            class UpdateSpellingLetterRunnable implements Runnable {
                                UpdateSpellingLetterRunnable() {}
                                public void run() {
                                    Log.d(SPELL_THE_WORD_GAME, "["+ Thread.currentThread()+ "]" + "Painting the guessed letter.");
                                    // The guessed letter composing the word will be shown in screen
                                    letter.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.MATCH_PARENT));
                                    letter.setGravity(Gravity.CENTER);
                                    letter.setTextColor(Color.parseColor("#8CF5C1"));
                                    letter.setTextSize(40);
                                    letter.setTypeface(Typeface.create("sans-serif-condensed", Typeface.NORMAL));
                                    mLetterSpelling.addView(letter);
                                }
                            }
                            handler.post(new UpdateSpellingLetterRunnable());
                            mpCorrect.start();
                            // 1. Avanzamos a la siguiente iteración (letra)
                            letterIdx++;

                            // Comprobamos si es la última letra de la palabra
                            if (letterIdx == arrLetter.length) {
                                // 2. UI update thread runs the color update runnable on that specific guessed letter card id
                                class UpdateCardColorRunnable implements Runnable {
                                    final int letterIndex;
                                    UpdateCardColorRunnable(int idx) { letterIndex = idx; }
                                    public void run() {
                                        Log.d(SPELL_THE_WORD_GAME, "["+ Thread.currentThread()+ "]" + "Updating letter card to green.");
                                        // Card color updated to green
                                        mCardWord.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#8CF5C1")));
                                    }
                                }
                                mpCorrect.start(); // correct sound effect plays
                                handler2.post(new UpdateCardColorRunnable(letterIdx));
                            }
                        }
                        Log.d(SPELL_THE_WORD_GAME, "["+ Thread.currentThread()+ "]" + "Word guessed; generating next...");

                        try {
                            wait(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        // We show a new sampled word as the user has correctly guessed all letters from the prior word.
                        class UpdateLetterCardsRunnable implements Runnable {
                            UpdateLetterCardsRunnable() {}
                            public void run() {
                                Log.d(SPELL_THE_WORD_GAME, "["+ Thread.currentThread()+ "]" + "Generating new word.");
                                arrLetter = setRandomWord(mCardWord, true);
                            }
                        }
                        if(cycle <= 2) {
                            handler3.post(new UpdateLetterCardsRunnable());
                        } // cycle == 3 would be the last cycle so we would not need to update more cards.

                        // Stopping timer as user did a correct guess.
                        initTimerRunnable.countDownTimer.cancel();
                        handler4.removeCallbacksAndMessages(initTimerRunnable);
                    }
                }
                Log.i(SPELL_THE_WORD_GAME, "Sub-process completed");
            }
        };

        // Initiate the main card detection game logic thread
        cardDetectionThread = new Thread(runnable);
        cardDetectionThread.start();

        // We have a running task for checking for game completion and if so, goes to the next lesson game.
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.d(SPELL_THE_WORD_GAME, "State: " + cardDetectionThread.getState() + "isAlive: " + cardDetectionThread.isAlive());
                if (!cardDetectionThread.isAlive()) {
                    if (!threadIsInterrupted) {
                        // Case where user finished the first game (thread ended by itself)
                        Log.d(SPELL_THE_WORD_GAME, "Thread finished, moving on to the next activity.");

                        // Update lesson progress in user database.
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        Map<String, Object> dataToUpdate = new HashMap<>();
                        dataToUpdate.put("ncgames", FieldValue.increment(1));
                        userStatsDB.updateUserStats(firebaseUser.getUid(), dataToUpdate);

                        // We go to the next stage screen of the lesson (the between games activity), passing necessary information.
                        Intent intent = new Intent(context, BetweenGamesActivity.class);
                        intent.putExtra("previousActivity", SPELL_THE_WORD_GAME);
                        intent.putExtra("state", "SUCCESS");
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);

                        timer.cancel();
                    } else {
                        Log.d(SPELL_THE_WORD_GAME, "Thread interrupted, closing activity.");

                        timer.cancel();
                    }
                } else if (isTimesOut) {
                    // Case when user ran out of time for guessing the letter.
                    Log.d(SPELL_THE_WORD_GAME, "Time's out.");
                    mpWrong.start();

                    Intent intent = new Intent(context, BetweenGamesActivity.class);
                    intent.putExtra("previousActivity", SPELL_THE_WORD_GAME);
                    intent.putExtra("state", "FAIL");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);

                    timer.cancel();
                } else {
                    Log.d(SPELL_THE_WORD_GAME, "I'm still waiting for the thread to end.");
                }
            }
        }, 500, 500);  // first is delay, second is period
    }

    @Override
    public synchronized void onPause() {
        // Setting flag for thread signalling
        super.onPause();
        activityStopped = true;
    }

    @Override
    public synchronized void onStop() {
        super.onStop();
        // We release the MPs for graceful exit
        mpCorrect.release();
        mpCorrect = null;

        mpWrong.release();
        mpWrong = null;
    }

    /**
     * Function in charge of comparison between the letter shown in screen and the one detected
     * by the sign language detection model.
     * @param letter: textView of the letter showing in screen that we are interested on comparing
     * @param activityStopped: control logic boolean that will stop comparison in case activity stops.
     * @return True if detected letter matches the one being asked to the user to guess.
     */
    private boolean checkLetter(TextView letter, boolean activityStopped) {
        if (activityStopped) {
            return true;
        }
        // We will get from here the model detected sign
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE);
        Gson gson = new Gson();

        // Obtaining sign language detection model predictions (recognition)
        String json = sharedPreferences.getString("RESULTS", "A");
        Type type = new TypeToken<List<Detector.Recognition>>() {}.getType();

        if(json.equals("A")){ // No detection at all.
            return false;
        }

        List<Detector.Recognition> mappedRecognitions = gson.fromJson(json, type);

        if (mappedRecognitions == null) {
            // if the array list is empty, creating a new array list.
            mappedRecognitions = new ArrayList<>();
            return false;
        } else {
            for (Detector.Recognition result : mappedRecognitions) {
                if (result.getTitle().contentEquals(letter.getText())) {
                    // Detected letter matches the one shown in screen. User is correct.
                    Log.d(SPELL_THE_WORD_GAME, "Recognized the letter: " + result.getTitle());
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Function in charge of selecting a new word randomly from a list of predefined words.
     * Performs the sampling as well as the UI updates
     *
     * @param cardWord :layout containing the card where the word is present
     * @param refreshCard : if True, changes the card color back to white when a new cycle begins.
     * @return updated array of letters composing the randomly chosen word.
     */
    private TextView[] setRandomWord(RelativeLayout cardWord, boolean refreshCard) {
        String[] words = new String[]{"SIGNOS", "LENGUAJE", "APRENDER", "ANDROID", "VERANO",
                "PLAYA", "PISCINA", "MOJACAR"};

        int wordPosition = (int) (Math.random() * (words.length));

        mWord.setText(words[wordPosition] + "");

        // Word is turn to a list of letters
        arrLetter = new TextView[words[wordPosition].length()];

        // For each of the letters composing the word, we update the text views.
        for (int j = 0; j < words[wordPosition].length(); j ++) {
            TextView newLetter = new TextView(this);
            Log.d(SPELL_THE_WORD_GAME, "New letter: " + words[wordPosition].charAt(j));
            newLetter.setText(words[wordPosition].charAt(j) + "");
            arrLetter[j] = newLetter;
        }

        if (refreshCard) {
            // Card color reset back to wait
            cardWord.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFFFFF")));
            mLetterSpelling.removeAllViews();
        }
        return arrLetter;
    }

    /**
     * Graceful termination of thread and activity when user performs the "back" action.
     */
    @Override
    public void onBackPressed() {
        int count = getSupportFragmentManager().getBackStackEntryCount();

        if (count == 0) { super.onBackPressed();
            threadIsInterrupted = true;
        } else {
            getSupportFragmentManager().popBackStack();
        }
    }
}