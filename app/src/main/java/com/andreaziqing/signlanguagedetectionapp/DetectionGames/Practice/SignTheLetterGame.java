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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Guessing the alphabet signs second lesson class.
 *
 *     This class is in charge of the logic regarding the practice game with timing
 *     Users will have to correctly guess the appearing sign letters and will have certain time to
 *     correctly do the signs, otherwise if they have 3 consecutive fails they will have to start again.
 */
public class SignTheLetterGame extends DetectorActivity implements View.OnClickListener {

    private static final String SIGN_THE_LETTER_GAME = "SignTheLetterGame";

    Context context;

    // Views and Layout for the letter card images shown to the user to guess their corresponding signs.
    TextView mFirstLetter, mSecondLetter, mThirdLetter;
    ImageView mFirstLetterImage, mSecondLetterImage, mThirdLetterImage;
    RelativeLayout mFirstCardLetter, mSecondCardLetter, mThirdCardLetter;

    Button mFirsLettertHint, mSecondLetterHint, mThirdLetterHint;

    TextView[] arrLetter;
    ImageView[] arrLetterImage;
    RelativeLayout[] arrCardLetter;
    Button[] arrButtonLetterHint;

    String[] arrGroupOfLetters;

    // Thread in charge of detecting that the model predicted sign match the randomly shown letters and do the main game logic
    Thread cardDetectionThread;
    // To control the thread stop/interrupt logic.
    volatile boolean activityStopped = false;
    volatile boolean threadIsInterrupted = false;

    // Lists from which we are going to randomly sample 3 of them to show.
    List<String> abecedary = Arrays.asList("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K",
            "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z");
    List<Integer> abecedaryImage = Arrays.asList(R.drawable.a, R.drawable.b, R.drawable.c, R.drawable.d,
            R.drawable.e, R.drawable.f, R.drawable.g, R.drawable.h, R.drawable.i, R.drawable.j, R.drawable.k,
            R.drawable.l, R.drawable.m, R.drawable.n, R.drawable.o, R.drawable.p, R.drawable.q, R.drawable.r,
            R.drawable.s, R.drawable.t, R.drawable.u, R.drawable.v, R.drawable.w, R.drawable.x, R.drawable.y,
            R.drawable.z);

    // This map contains the relationship between the randomly drawn letters and their ground truth.
    Dictionary<String, Integer> signDictionary = new Hashtable<>();

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
        setContentView(R.layout.activity_sign_the_letter_game);

        counter = 0;
        context = getApplicationContext();

        mpCorrect = MediaPlayer.create(context, R.raw.correct);
        mpWrong = MediaPlayer.create(context, R.raw.wrong);

        initViews();

        // First 3 letters randomly sampled from the vocabulary
        setThreeRandomLetters(arrCardLetter, false);
    }

    private void initViews() {
        //We build the signDictionary map with their corresponding image locations.
        for (int i = 0; i < abecedary.toArray().length; i++) {
            signDictionary.put(abecedary.get(i), abecedaryImage.get(i));
        }

        // Countdown timer UI
        time = findViewById(R.id.countdown_timer);

        //Views setup for the letters shown to display
        mFirstLetter = findViewById(R.id.first_letter);
        mSecondLetter = findViewById(R.id.second_letter);
        mThirdLetter = findViewById(R.id.third_letter);

        mFirstLetterImage = findViewById(R.id.first_letter_image);
        mSecondLetterImage = findViewById(R.id.second_letter_image);
        mThirdLetterImage = findViewById(R.id.third_letter_image);

        mFirsLettertHint = findViewById(R.id.first_letter_hint);
        mSecondLetterHint = findViewById(R.id.second_letter_hint);
        mThirdLetterHint = findViewById(R.id.third_letter_hint);

        mFirstCardLetter = findViewById(R.id.first_card_letter);
        mSecondCardLetter = findViewById(R.id.second_card_letter);
        mThirdCardLetter = findViewById(R.id.third_card_letter);

        arrLetter = new TextView[]{mFirstLetter, mSecondLetter, mThirdLetter};
        arrLetterImage = new ImageView[]{mFirstLetterImage, mSecondLetterImage, mThirdLetterImage};
        arrCardLetter = new RelativeLayout[]{mFirstCardLetter, mSecondCardLetter, mThirdCardLetter};
        arrButtonLetterHint = new Button[]{mFirsLettertHint, mSecondLetterHint, mThirdLetterHint};

        arrGroupOfLetters = new String[]{"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K",
                "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
    }

    @Override
    public synchronized void onResume() {
        super.onResume();

        // We will need these handlers to manage the UI changes.
        final Handler handler = new Handler();
        final Handler handler2 = new Handler();
        final Handler handler3 = new Handler();

        // The CardDectectionThread will be in charge of running this runnable
        // Runnable that performs the iterative checking of the detected signs <--> showed letter in screen
        // as well as the main game control logic
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Log.d(SIGN_THE_LETTER_GAME, "Thread in background: " + Thread.currentThread().getName());
                synchronized (this) {
                    for (int cycle = 0; cycle < 4; cycle++) {
                        Log.d(SIGN_THE_LETTER_GAME, "[" + Thread.currentThread() + "]" + "Cycle #" + cycle + 1);
                        int letterIdx = 0;
                        // Runnable in charge of the CountDown timer logic. Controls logic for time passing and time out alarm.
                        class InitTimerRunnable implements Runnable {
                            CountDownTimer countDownTimer;

                            InitTimerRunnable() {}
                            public void run() {
                                Log.d(SIGN_THE_LETTER_GAME, "["+ Thread.currentThread()+ "]" + "Starting timer.");
                                countDownTimer = new CountDownTimer(10000, 1000) {
                                    @Override
                                    public void onTick(long millisUntilFinished) {
                                        time.setText((int) (millisUntilFinished / 1000) + "");
                                        counter++;
                                    }

                                    @Override
                                    public void onFinish() {
                                        isTimesOut = true;
                                    }
                                };
                                countDownTimer.start();
                            }
                        }

                        InitTimerRunnable initTimerRunnable = null;

                        // For this group of 3 letters, we go 1 by 1 checking the guesses.
                        for (TextView letter : arrLetter) {
                            // Timer starts
                            initTimerRunnable = new InitTimerRunnable();
                            handler3.postDelayed(initTimerRunnable, 100);

                            // Only showing the sign image (for 3 seconds) if the user clicks on "Hint" button
                            onClickButtonHint(mFirsLettertHint, 0);
                            onClickButtonHint(mSecondLetterHint, 1);
                            onClickButtonHint(mThirdLetterHint, 2);

                            // Wait for letter validation
                            while (!checkLetter(letter, activityStopped)) {
                            }

                            if (activityStopped) {
                                return;
                            }
                            // Once letter has been correctly guessed by the user; we need to show in screen the green card
                            // 1. Runnable to be executed by the ui update thread that will update the card color to green
                            class UpdateCardColorRunnable implements Runnable {
                                int letterIndex;

                                UpdateCardColorRunnable(int idx) {
                                    letterIndex = idx;
                                }

                                public void run() {
                                    Log.d(SIGN_THE_LETTER_GAME, "[" + Thread.currentThread() + "]" + "Updating letter card to green.");
                                    // Card color updated to green
                                    arrCardLetter[letterIndex].setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#8CF5C1")));
                                }
                            }

                            mpCorrect.start(); // Play the "correct" sound effect as user correctly guessed the sign.

                            // Stopping timer as user did a correct guess.
                            initTimerRunnable.countDownTimer.cancel();
                            handler3.removeCallbacksAndMessages(initTimerRunnable);
                            // 2. UI update thread runs the color update runnable on that specific guessed letter card.
                            handler.post(new UpdateCardColorRunnable(letterIdx));
                            // 3. We go for the next card letter to check
                            letterIdx++;
                        }
                        Log.d(SIGN_THE_LETTER_GAME, "[" + Thread.currentThread() + "]" + "Guessed group of 3 letters; generating next...");

                        try {
                            wait(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        // Now we can randomly sample 3 new letters to show in screen for the user to guess. Next cycle starts.
                        class UpdateLetterCardsRunnable implements Runnable {
                            UpdateLetterCardsRunnable() {}
                            public void run() {
                                Log.d(SIGN_THE_LETTER_GAME, "[" + Thread.currentThread() + "]" + "Regenerating group of 3 letters.");
                                setThreeRandomLetters(arrCardLetter, true);
                            }
                        }
                        if (cycle <= 2) {
                            // Handler performs update of the cards in UI
                            handler2.post(new UpdateLetterCardsRunnable());
                        } // cycle == 3 would be the last cycle so we would not need to update more cards.

                    }
                }
                Log.i(SIGN_THE_LETTER_GAME, "Sub-process completed");
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
                Log.d(SIGN_THE_LETTER_GAME, "State: " + cardDetectionThread.getState() + "isAlive: " + cardDetectionThread.isAlive());
                if (!cardDetectionThread.isAlive()) {
                    if (!threadIsInterrupted) {
                        // Case where user finished the first game (thread ended by itself)
                        Log.d(SIGN_THE_LETTER_GAME, "Thread finished, moving on to the next activity.");

                        // Update lesson progress in user database.
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        Map<String, Object> dataToUpdate = new HashMap<>();
                        dataToUpdate.put("ncgames", FieldValue.increment(1));
                        userStatsDB.updateUserStats(firebaseUser.getUid(), dataToUpdate);

                        // We go to the next stage screen of the lesson (the between games activity), passing necessary information.
                        Intent intent = new Intent(SignTheLetterGame.this, BetweenGamesActivity.class);
                        intent.putExtra("previousActivity", SIGN_THE_LETTER_GAME);
                        intent.putExtra("state", "SUCCESS");
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        SignTheLetterGame.this.startActivity(intent);

                        timer.cancel();
                    } else {
                        Log.d(SIGN_THE_LETTER_GAME, "Thread interrupted, closing activity.");
                        timer.cancel();
                    }
                } else if (isTimesOut) {
                    // Case when user ran out of time for guessing the letter.
                    Log.d(SIGN_THE_LETTER_GAME, "Time's out.");
                    mpWrong.start();

                    Intent intent = new Intent(SignTheLetterGame.this, BetweenGamesActivity.class);
                    intent.putExtra("previousActivity", SIGN_THE_LETTER_GAME);
                    intent.putExtra("state", "FAIL");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    SignTheLetterGame.this.startActivity(intent);

                    timer.cancel();
                } else {
                    Log.d(SIGN_THE_LETTER_GAME, "I'm still waiting for the thread to end.");
                }
            }
        }, 500, 500);  // first is delay, second is period
    }

    /**
     * Function handling the click action when user clicks in the "hint" button.
     * The sign image corresponding to the selected letter will be shown for 3 seconds, then dissapear.
     * @param buttonHint : button of the Hint card
     * @param finalLetterIdx : letter id that the user wants the hint for.
     */
    private void onClickButtonHint(Button buttonHint, int finalLetterIdx) {
        buttonHint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Hint sign image will be shown as user clicked on the Hint button.
                arrLetter[finalLetterIdx].setVisibility(View.INVISIBLE);
                arrButtonLetterHint[finalLetterIdx].setVisibility(View.INVISIBLE);
                arrLetterImage[finalLetterIdx].setVisibility(View.VISIBLE);

                // UI update runnable for updating the image to the screen.
                class ShowHintImageRunnable implements Runnable {
                    int letterIndex;

                    ShowHintImageRunnable(int idx) {
                        letterIndex = idx;
                    }

                    public void run() {
                        Log.d(SIGN_THE_LETTER_GAME, "["+ Thread.currentThread()+ "]" + "Showing the hint image.");
                        // Showing in screen
                        arrLetter[finalLetterIdx].setVisibility(View.VISIBLE);
                        buttonHint.setVisibility(View.VISIBLE);
                        arrLetterImage[finalLetterIdx].setVisibility(View.GONE);
                    }
                }
                new Handler().postDelayed(new ShowHintImageRunnable(finalLetterIdx), 3000); // 3 second delay
            }
        });
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

        if (json.equals("A")) { // No detection at all.
            return false;
        }

        List<Detector.Recognition> mappedRecognitions = gson.fromJson(json, type);

        if (mappedRecognitions == null) {
            // if the array list is empty, creating a new array list.
            mappedRecognitions = new ArrayList<>();
            return false;
        } else {
            // Log.d(FULL_SECOND_GAME, "Cargado Mapped Recognition: " + mappedRecognitions);
            for (Detector.Recognition result : mappedRecognitions) {
                if (result.getTitle().contentEquals(letter.getText())) {
                    // Detected letter matches the one shown in screen. User is correct.
                    Log.d(SIGN_THE_LETTER_GAME, "Recognized the letter: " + result.getTitle());
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Function in charge of randomly sampling 3 letters out of the arrGroupOfLetters array as well
     * as updating the cards shown in screen to the user for guessing.
     * @param arrCardLetter array containing the letters from which to sample
     * @param refreshCard if True, changes the card color back to white when a new cycle begins.
     */
    private void setThreeRandomLetters(RelativeLayout[] arrCardLetter, boolean refreshCard) {
        String positions = "";
        for (int i = 0; i < arrLetter.length; i++) {
            boolean retryLetter = true;
            while (retryLetter) {
                int letterPosition = (int) (Math.random() * (arrGroupOfLetters.length));
                if (!positions.contains(String.valueOf(letterPosition))) {
                    retryLetter = false;
                    arrLetter[i].setText(arrGroupOfLetters[letterPosition]);
                    arrLetterImage[i].setImageResource(signDictionary.get(arrGroupOfLetters[letterPosition]));
                    positions = positions.concat(String.valueOf(letterPosition));

                    if (refreshCard) {
                        // Card color reset back to white as new cycle with 3 new letters will appear
                        arrCardLetter[i].setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFFFFF")));
                    }
                } else {
                    retryLetter = true;
                }
            }

        }
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
