package com.andreaziqing.signlanguagedetectionapp.DetectionGames.Lessons;

import com.andreaziqing.signlanguagedetectionapp.Database.UserStatsDatabase;
import com.andreaziqing.signlanguagedetectionapp.Detector.DetectorActivity;
import com.andreaziqing.signlanguagedetectionapp.R;
import com.andreaziqing.signlanguagedetectionapp.Detector.TFLiteInterpreter.Detector;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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
 * Guessing the alphabet signs lesson class.
 *
 *     This class is in charge of the logic regarding the first lesson; by means of identifying the
 *     signs the user is doing to the camera and matching it to the randomly generated 3-letter groups.
 *     The match is done by checking the Detector model prediction on the letter the user is signalling
 *     to the camera and comparing that to the letters being shown in the screen.
 */
public class FirstLesson extends DetectorActivity {
    private static final String FIRST_LESSON = "FirstLesson";

    Context context;

    // Views and Layout for the letter card images shown to the user to guess their corresponding signs.
    TextView mFirstLetter, mSecondLetter, mThirdLetter;
    ImageView mFirstLetterImage, mSecondLetterImage, mThirdLetterImage;
    RelativeLayout mFirstCardLetter, mSecondCardLetter, mThirdCardLetter;

    TextView[] arrLetter;
    ImageView[] arrLetterImage;
    RelativeLayout[] arrCardLetter;

    public int mPositionGroup;
    String[] arrGroupOfLetters;

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

    // Thread in charge of detecting that the model predicted sign match the randomly shown letters.
    Thread cardDetectionThread;

    // To control the thread stop/interrupt logic.
    volatile boolean activityStopped = false;
    volatile boolean threadIsInterrupted = false;

    // DB instances for updating user stats
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    UserStatsDatabase userStatsDB = new UserStatsDatabase();

    // MediaPlayer used for playing sound effect on valid detections.
    public MediaPlayer mpCorrect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_lesson);

        context = getApplicationContext();

        mpCorrect = MediaPlayer.create(context, R.raw.correct);

        //We build the signDictionary map with their corresponding image locations.
        for (int i = 0; i < abecedary.toArray().length; i++) {
            signDictionary.put(abecedary.get(i), abecedaryImage.get(i));
        }

        //Views setup for the letters shown to display

        mFirstLetter = findViewById(R.id.first_letter);
        mSecondLetter = findViewById(R.id.second_letter);
        mThirdLetter = findViewById(R.id.third_letter);

        mFirstLetterImage = findViewById(R.id.first_letter_image);
        mSecondLetterImage = findViewById(R.id.second_letter_image);
        mThirdLetterImage = findViewById(R.id.third_letter_image);

        mFirstCardLetter = findViewById(R.id.first_card_letter);
        mSecondCardLetter = findViewById(R.id.second_card_letter);
        mThirdCardLetter = findViewById(R.id.third_card_letter);

        arrLetter = new TextView[]{mFirstLetter, mSecondLetter, mThirdLetter};
        arrLetterImage = new ImageView[]{mFirstLetterImage, mSecondLetterImage, mThirdLetterImage};
        arrCardLetter = new RelativeLayout[]{mFirstCardLetter, mSecondCardLetter, mThirdCardLetter};

        // Getting the position of the letter groups the user has chosen prior to launching the lesson
        Bundle bundle = getIntent().getExtras();
        mPositionGroup = bundle.getInt("position");
        setGroupOfLetters(mPositionGroup);

        // Randomly sampling 3 letters from that letter group chosen.
        setThreeRandomLetters(arrCardLetter,false);
    }

    @Override
    public synchronized void onResume() {
        super.onResume();

        // We will need these handlers to manage the UI changes.
        final Handler handler = new Handler();
        final Handler handler2 = new Handler();

        // The CardDectectionThread will be in charge of running this runnable
        // Runnable that performs the iterative checking of the detected signs <--> showed letter in screen.
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Log.d(FIRST_LESSON, "Thread in background: "+ Thread.currentThread().getName());
                synchronized (this) {
                    for (int cycle = 0; cycle < 4; cycle++) {
                        Log.d(FIRST_LESSON, "["+ Thread.currentThread()+ "]" + "Cycle #"+ cycle + 1);
                        int letterIdx = 0;
                        // For this group of 3 letters, we go 1 by 1 checking the guesses.
                        for (TextView letter: arrLetter) {
                            // Wait for letter validation
                            while (!checkLetter(letter, activityStopped)) { }

                            if (activityStopped) {
                                return;
                            }
                            // Once letter has been correctly guessed by the user; we need to show in screen the green card
                            // 1. Runnable to be executed by the ui update thread that will update the card color to green
                            class UpdateCardColorRunnable implements Runnable {
                                int letterIndex;
                                UpdateCardColorRunnable(int idx) { letterIndex = idx; }
                                public void run() {
                                    Log.d(FIRST_LESSON, "["+ Thread.currentThread()+ "]" + "Updating letter card to green.");
                                    arrCardLetter[letterIndex].setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#8CF5C1")));
                                }
                            }

                            mpCorrect.start(); // Playing the "correct" sound effect

                            // 2. UI update thread runs the color update runnable on that specific guessed letter card.
                            handler.post(new UpdateCardColorRunnable(letterIdx));
                            // 3. We go for the next card letter to check
                            letterIdx++;
                        }
                        Log.d(FIRST_LESSON, "["+ Thread.currentThread()+ "]" + "Guessed group of 3 letters; generating next...");

                        try {
                            wait(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        // Now we can randomly sample 3 new letters to show in screen for the user to guess. Next cycle starts.
                        class UpdateLetterCardsRunnable implements Runnable {
                            UpdateLetterCardsRunnable() {}
                            public void run() {
                                Log.d(FIRST_LESSON, "["+ Thread.currentThread()+ "]" + "Regenerating group of 3 letters.");
                                setThreeRandomLetters(arrCardLetter, true);
                            }
                        }
                        if(cycle <= 2) {
                            handler2.post(new UpdateLetterCardsRunnable());
                        } // cycle == 3 would be the last cycle so we would not need to update more cards.
                    }
                }
                Log.i(FIRST_LESSON, "Sub-process completed");
            }
        };

        // Initiate the main card detection thread
        cardDetectionThread = new Thread(runnable);
        cardDetectionThread.start();

        // We have a running task for checking for game completion and if so, goes to the next lesson game.
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.d(FIRST_LESSON, "State: " + cardDetectionThread.getState() + "isAlive: " + cardDetectionThread.isAlive());
                if (!cardDetectionThread.isAlive()) {
                    if (!threadIsInterrupted) {
                        // Case where user finished the first game (thread ended by itself)
                        Log.d(FIRST_LESSON, "Thread finished, moving on to the next activity.");

                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        Map<String, Object> dataToUpdate = new HashMap<>();
                        // Update lesson progress in user database.
                        switch (mPositionGroup) {
                            case 0:
                                dataToUpdate.put("progressl1", "1");
                                break;
                            case 1:
                                dataToUpdate.put("progressl2", "1");
                                break;
                            case 2:
                                dataToUpdate.put("progressl3", "1");
                                break;
                            default:
                                break;
                        }

                        userStatsDB.updateUserStats(firebaseUser.getUid(), dataToUpdate);

                        // We go to the next lesson upon completion. Passing necesary information in the bundle.
                        Intent intent = new Intent(context, SecondLesson.class);
                        intent.putExtra("arrGroupOfLetters", arrGroupOfLetters);
                        intent.putExtra("position", mPositionGroup);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);

                        timer.cancel();
                    } else {
                        // Case when thread was interrupted due to external reasons.
                        Log.d(FIRST_LESSON, "Thread interrupted, closing activity.");

                        timer.cancel();
                    }
                } else {
                    Log.d(FIRST_LESSON, "I'm still waiting for the thread to end.");
                }
            }
        }, 500, 500);  // first is delay, second is period
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        // Setting flag for thread signalling
        activityStopped = true;
    }

    @Override
    public synchronized void onStop() {
        super.onStop();
        // We release the MP for graceful exit
        mpCorrect.release();
        mpCorrect = null;
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
            // Log.d(FIRST_GAME, "Cargado Mapped Recognition: " + mappedRecognitions);
            for (Detector.Recognition result : mappedRecognitions) {
                if (result.getTitle().contentEquals(letter.getText())) {
                    // Detected letter matches the one shown in screen. User is correct.
                    Log.d(FIRST_LESSON, "Recognized the letter: " + result.getTitle());
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Function in charge of setting the arrGroupOfLetters array containing the relevant letters that
     * will be randomly sampled from for the user to guess.
     * @param position: 0 1 or 2 depending of the group of letters the user has selected for the lesson.
     */
    private void setGroupOfLetters(int position) {
        switch (position) {
            case 0:
                arrGroupOfLetters = new String[]{"A", "B", "C", "D", "E", "F", "G", "H"};
                break;
            case 1:
                arrGroupOfLetters = new String[]{"I", "J", "K", "L", "M", "N", "O", "P"};
                break;
            case 2:
                arrGroupOfLetters = new String[]{"Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
                break;

        }
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
            while(retryLetter) {
                int letterPosition = (int) (Math.random() * (arrGroupOfLetters.length));
                if (!positions.contains(String.valueOf(letterPosition))) {
                    // Randomly samples the letter position from the arrGroupOfLetters list, and sets image and letter accordingly.
                    retryLetter = false;
                    arrLetter[i].setText(arrGroupOfLetters[letterPosition]);
                    arrLetterImage[i].setImageResource(signDictionary.get(arrGroupOfLetters[letterPosition]));
                    positions = positions.concat(String.valueOf(letterPosition));

                    if (refreshCard) {
                        // Card color reset back to white.
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

        if (count == 0) {
            super.onBackPressed();
            threadIsInterrupted = true;
        } else {
            getSupportFragmentManager().popBackStack();
        }
    }
}