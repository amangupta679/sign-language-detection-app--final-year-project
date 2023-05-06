package com.andreaziqing.signlanguagedetectionapp.DetectionGames.Practice;

import androidx.appcompat.app.AppCompatActivity;

import com.andreaziqing.signlanguagedetectionapp.Database.UserStatsDatabase;
import com.andreaziqing.signlanguagedetectionapp.DetectionGames.BetweenGamesActivity;
import com.andreaziqing.signlanguagedetectionapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class MatchGame extends AppCompatActivity {

    private static final String MATCH_GAME = "MatchGame";

    private static final String LETTER_ID_RSC = "com.andreaziqing.signlanguagedetectionapp:id/letter_button";
    private static final String IMAGE_ID_RSC = "com.andreaziqing.signlanguagedetectionapp:id/image_button";

    Context context;

    TextView mFirstLetter, mSecondLetter, mThirdLetter, mFourthLetter,
            mFifthLetter, mSixthLetter, mSeventhLetter, mEighthLetter;

    ImageView mFirstImage, mSecondImage, mThirdImage, mFourthImage,
            mFifthImage, mSixthImage, mSeventhImage, mEighthImage;

    RelativeLayout mFirstCardLetter, mSecondCardLetter, mThirdCardLetter, mFourthCardLetter,
            mFifthCardLetter, mSixthCardLetter, mSeventhCardLetter, mEighthCardLetter;

    RelativeLayout mFirstCardImage, mSecondCardImage, mThirdCardImage, mFourthCardImage,
            mFifthCardImage, mSixthCardImage, mSeventhCardImage, mEighthCardImage;

    Button mFirstLetterButton, mSecondLetterButton, mThirdLetterButton, mFourthLetterButton,
            mFifthLetterButton, mSixthLetterButton, mSeventhLetterButton, mEighthLetterButton;

    Button mFirstImageButton, mSecondImageButton, mThirdImageButton, mFourthImageButton,
            mFifthImageButton, mSixthImageButton, mSeventhImageButton, mEighthImageButton;

    TextView[] arrLetter;
    ImageView[] arrLetterImage;
    RelativeLayout[] arrCardLetter;
    RelativeLayout[] arrCardImage;
    Button[] arrButtonLetter;
    Button[] arrButtonImage;

    Integer[] arrButtonLetterPositions; // 0: card position, 1: letter dictionary position
    Integer[] arrButtonImagePositions;

    String[] arrGroupOfLetters;

    List<String> abecedary = Arrays.asList("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K",
            "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z");
    List<Integer> abecedaryImage = Arrays.asList(R.drawable.a, R.drawable.b, R.drawable.c, R.drawable.d,
            R.drawable.e, R.drawable.f, R.drawable.g, R.drawable.h, R.drawable.i, R.drawable.j, R.drawable.k,
            R.drawable.l, R.drawable.m, R.drawable.n, R.drawable.o, R.drawable.p, R.drawable.q, R.drawable.r,
            R.drawable.s, R.drawable.t, R.drawable.u, R.drawable.v, R.drawable.w, R.drawable.x, R.drawable.y,
            R.drawable.z);

    Dictionary<String, Integer> signDictionary = new Hashtable<String, Integer>();

    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    UserStatsDatabase userStatsDB = new UserStatsDatabase();

    Map<String, String> letterMap = new HashMap<>();
    Map<String, String> imageMap = new HashMap<>();
    private static String clickedLetterButtonId, clickedImageButtonId;

    String letterToCompare, letterImageToCompare;
    int clickedNumLetter, clickedNumImage;
    int chances = 3;
    int counterMatches = 0;

    public MediaPlayer mpCorrect;
    public MediaPlayer mpWrong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_game);

        context = getApplicationContext();

        mpCorrect = MediaPlayer.create(context, R.raw.correct);
        mpWrong = MediaPlayer.create(context, R.raw.wrong);

        initViews();

        // First 8 letters
        // Randomly choose eight letters
        setEightRandomLetters(arrCardLetter, arrCardImage, false);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // On click letter
        for (Button button : arrButtonLetter) {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickedLetterButtonId = v.getResources().getResourceName(v.getId());

                    clickedNumLetter = Integer.parseInt(clickedLetterButtonId.replaceAll("[^0-9]",""))-1;
                    Log.d(MATCH_GAME, "clickedNumLetter: " + clickedNumLetter);

                    String equivalentLetter = letterMap.get(clickedLetterButtonId);
                    letterToCompare = equivalentLetter;

                    Log.d(MATCH_GAME, "clickedLetterButtonId: " + clickedLetterButtonId + "--> " + equivalentLetter);

                    if (letterImageToCompare != null) {
                        isAMatch(letterToCompare.equals(letterImageToCompare), clickedNumLetter, clickedNumImage);
                    }
                }
            });
        }

        // On click image
        for (Button button : arrButtonImage) {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickedImageButtonId = v.getResources().getResourceName(v.getId());

                    clickedNumImage = Integer.parseInt(clickedImageButtonId.replaceAll("[^0-9]",""))-1;
                    Log.d(MATCH_GAME, "clickedNumImage: " + clickedNumImage);

                    String equivalentLetter = imageMap.get(clickedImageButtonId);
                    letterImageToCompare = equivalentLetter;

                    Log.d(MATCH_GAME, "clickedImageButtonId: " + clickedImageButtonId + "--> " + equivalentLetter);

                    if (letterToCompare != null) {
                        isAMatch(letterToCompare.equals(letterImageToCompare), clickedNumLetter, clickedNumImage);
                    }

                }
            });
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        mpCorrect.release();
        mpCorrect = null;

        mpWrong.release();
        mpWrong = null;
    }

    private void isAMatch(boolean match, int cardLetterPosition, int cardImagePosition) {
        final Handler handler = new Handler();

        class UpdateCardColorRunnable implements Runnable {

            UpdateCardColorRunnable() {
            }

            public void run() {
                if (match) {
                    Log.d(MATCH_GAME, "[" + Thread.currentThread() + "]" + "Updating cards to green.");
                    arrCardLetter[cardLetterPosition].setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#8CF5C1")));
                    arrCardImage[cardImagePosition].setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#8CF5C1")));

                    mpCorrect.start();

                    counterMatches++;

                    if (counterMatches == 8) {
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        Map<String, Object> dataToUpdate = new HashMap<>();
                        dataToUpdate.put("ncgames", FieldValue.increment(1));
                        userStatsDB.updateUserStats(firebaseUser.getUid(), dataToUpdate);

                        Intent intent = new Intent(context, BetweenGamesActivity.class);
                        intent.putExtra("previousActivity", MATCH_GAME);
                        intent.putExtra("state", "SUCCESS");
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    }

                    // Reset variables
                } else {
                    Log.d(MATCH_GAME, "[" + Thread.currentThread() + "]" + "Updating cards to red.");
                    arrCardLetter[cardLetterPosition].setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#EC6E6E")));
                    arrCardImage[cardImagePosition].setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#EC6E6E")));

                    mpWrong.start();

                    chances--;
                    if (chances == 0) {
                        Intent intent = new Intent(context, BetweenGamesActivity.class);
                        intent.putExtra("previousActivity", MATCH_GAME);
                        intent.putExtra("state", "FAIL");
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MatchGame.this);
                        builder.setMessage(getString(R.string.you_have) + " " + chances + " " + getString(R.string.chances_left))
                                .setTitle(R.string.ups)
                                .setPositiveButton(R.string.got_it, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Log.d(MATCH_GAME, "[" + Thread.currentThread() + "]" + "Updating cards to initial default color.");
                                        arrCardLetter[cardLetterPosition].setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFFFFF")));
                                        arrCardImage[cardImagePosition].setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFFFFF")));

                                        // Reset variables
                                        resetValues();
                                    }
                                })
                                .show();
                    }

                    // Reset variables
                }
                resetValues();
            }
        }
        // We launch the Handler to update the interface with the declared internal runnable
        handler.post(new UpdateCardColorRunnable());
    }

    private void resetValues() {
        letterToCompare = null;
        letterImageToCompare = null;
    }

    private void initViews() {

        for (int i = 0; i < abecedary.toArray().length; i++) {
            signDictionary.put(abecedary.get(i), abecedaryImage.get(i));
        }

        mFirstLetter = findViewById(R.id.first_letter);
        mSecondLetter = findViewById(R.id.second_letter);
        mThirdLetter = findViewById(R.id.third_letter);
        mFourthLetter = findViewById(R.id.fourth_letter);
        mFifthLetter = findViewById(R.id.fifth_letter);
        mSixthLetter = findViewById(R.id.sixth_letter);
        mSeventhLetter = findViewById(R.id.seventh_letter);
        mEighthLetter = findViewById(R.id.eighth_letter);

        mFirstImage = findViewById(R.id.first_letter_image);
        mSecondImage = findViewById(R.id.second_letter_image);
        mThirdImage = findViewById(R.id.third_letter_image);
        mFourthImage = findViewById(R.id.fourth_letter_image);
        mFifthImage = findViewById(R.id.fifth_letter_image);
        mSixthImage = findViewById(R.id.sixth_letter_image);
        mSeventhImage = findViewById(R.id.seventh_letter_image);
        mEighthImage = findViewById(R.id.eighth_letter_image);

        mFirstCardLetter = findViewById(R.id.first_card_letter);
        mSecondCardLetter = findViewById(R.id.second_card_letter);
        mThirdCardLetter = findViewById(R.id.third_card_letter);
        mFourthCardLetter = findViewById(R.id.fourth_card_letter);
        mFifthCardLetter = findViewById(R.id.fifth_card_letter);
        mSixthCardLetter = findViewById(R.id.sixth_card_letter);
        mSeventhCardLetter = findViewById(R.id.seventh_card_letter);
        mEighthCardLetter = findViewById(R.id.eighth_card_letter);

        mFirstCardImage = findViewById(R.id.first_card_image);
        mSecondCardImage = findViewById(R.id.second_card_image);
        mThirdCardImage = findViewById(R.id.third_card_image);
        mFourthCardImage = findViewById(R.id.fourth_card_image);
        mFifthCardImage = findViewById(R.id.fifth_card_image);
        mSixthCardImage = findViewById(R.id.sixth_card_image);
        mSeventhCardImage = findViewById(R.id.seventh_card_image);
        mEighthCardImage = findViewById(R.id.eighth_card_image);

        mFirstLetterButton = findViewById(R.id.letter_button1);
        mSecondLetterButton = findViewById(R.id.letter_button2);
        mThirdLetterButton = findViewById(R.id.letter_button3);
        mFourthLetterButton = findViewById(R.id.letter_button4);
        mFifthLetterButton = findViewById(R.id.letter_button5);
        mSixthLetterButton = findViewById(R.id.letter_button6);
        mSeventhLetterButton = findViewById(R.id.letter_button7);
        mEighthLetterButton = findViewById(R.id.letter_button8);

        mFirstImageButton = findViewById(R.id.image_button1);
        mSecondImageButton = findViewById(R.id.image_button2);
        mThirdImageButton = findViewById(R.id.image_button3);
        mFourthImageButton = findViewById(R.id.image_button4);
        mFifthImageButton = findViewById(R.id.image_button5);
        mSixthImageButton = findViewById(R.id.image_button6);
        mSeventhImageButton = findViewById(R.id.image_button7);
        mEighthImageButton = findViewById(R.id.image_button8);

        arrLetter = new TextView[]{mFirstLetter, mSecondLetter, mThirdLetter, mFourthLetter,
                mFifthLetter, mSixthLetter, mSeventhLetter, mEighthLetter};
        arrLetterImage = new ImageView[]{mFirstImage, mSecondImage, mThirdImage, mFourthImage,
                mFifthImage, mSixthImage, mSeventhImage, mEighthImage};
        arrCardLetter = new RelativeLayout[]{mFirstCardLetter, mSecondCardLetter, mThirdCardLetter,
                mFourthCardLetter, mFifthCardLetter, mSixthCardLetter,
                mSeventhCardLetter, mEighthCardLetter};
        arrCardImage = new RelativeLayout[]{mFirstCardImage, mSecondCardImage, mThirdCardImage,
                mFourthCardImage, mFifthCardImage, mSixthCardImage,
                mSeventhCardImage, mEighthCardImage};
        arrButtonLetter = new Button[]{mFirstLetterButton, mSecondLetterButton, mThirdLetterButton,
                mFourthLetterButton, mFifthLetterButton, mSixthLetterButton,
                mSeventhLetterButton, mEighthLetterButton};
        arrButtonImage = new Button[]{mFirstImageButton, mSecondImageButton, mThirdImageButton,
                mFourthImageButton, mFifthImageButton, mSixthImageButton,
                mSeventhImageButton, mEighthImageButton};

        arrGroupOfLetters = new String[]{"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K",
                "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
    }

    private void setEightRandomLetters(RelativeLayout[] arrCardLetter, RelativeLayout[] arrCardImage, boolean refreshCard) {
        String positions = "";
        Integer[] arrPositions = new Integer[arrLetter.length];

        for (int i = 0; i < arrLetter.length; i++) {
            boolean retryLetter = true;
            while (retryLetter) {
                int letterPosition = (int) (Math.random() * (arrGroupOfLetters.length));
                if (!positions.contains(String.valueOf(letterPosition))) {
                    retryLetter = false;
                    arrPositions[i] = letterPosition;
                    positions = positions.concat(String.valueOf(letterPosition));

                    if (refreshCard) {
                        // Reset card color to white
                        arrCardLetter[i].setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFFFFF")));
                        arrCardImage[i].setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFFFFF")));
                    }
                } else {
                    retryLetter = true;
                }
            }
        }

        // Shuffle letters
        shuffleArrayPositions(arrPositions);
        arrButtonLetterPositions = arrPositions;
        Log.d(MATCH_GAME, "arrButtonLetterPositions: " + Arrays.toString(arrButtonLetterPositions));

        int i = 0;
        for (int position : arrButtonLetterPositions) {
            arrLetter[i].setText(arrGroupOfLetters[position]);
            i++;
        }

        int letterPosition;
        // We build the map that relates ids of letter buttons to their letter
        for (int k = 0; k < arrButtonLetterPositions.length ; k++) {
            letterPosition = arrButtonLetterPositions[k];
            letterMap.put(LETTER_ID_RSC + (k+1), arrGroupOfLetters[letterPosition]);
        }

        Log.d(MATCH_GAME, "letterMap: " + letterMap);

        // Shuffle images
        shuffleArrayPositions(arrPositions);
        arrButtonImagePositions = arrPositions;
        Log.d(MATCH_GAME, "arrButtonImagePositions: " + Arrays.toString(arrButtonImagePositions));
        int j = 0;
        for (int position : arrButtonImagePositions) {
            arrLetterImage[j].setImageResource(signDictionary.get(arrGroupOfLetters[position]));
            j++;
        }

        int imagePosition;
        // We build the map that relates ids of image buttons with their letters
        for (int n = 0; n < arrButtonImagePositions.length ; n++) {
            imagePosition = arrButtonImagePositions[n];
            imageMap.put(IMAGE_ID_RSC + (n+1), arrGroupOfLetters[imagePosition]);
        }

        Log.d(MATCH_GAME, "imageMap: " + imageMap);
    }

    private void shuffleArrayPositions(Integer[] arrPositions) {
        List<Integer> arrShuffleLettersPositions = Arrays.asList(arrPositions);
        Collections.shuffle(arrShuffleLettersPositions);
        arrShuffleLettersPositions.toArray(arrPositions);
    }

    @Override
    public void onBackPressed() {
        int count = getSupportFragmentManager().getBackStackEntryCount();

        if (count == 0) { super.onBackPressed();
        } else {
            getSupportFragmentManager().popBackStack();
        }
    }
}