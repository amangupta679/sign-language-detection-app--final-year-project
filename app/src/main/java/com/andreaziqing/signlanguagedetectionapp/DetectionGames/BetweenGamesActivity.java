package com.andreaziqing.signlanguagedetectionapp.DetectionGames;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.andreaziqing.signlanguagedetectionapp.DetectionGames.Lessons.FirstLesson;
import com.andreaziqing.signlanguagedetectionapp.DetectionGames.Lessons.SecondLesson;
import com.andreaziqing.signlanguagedetectionapp.DetectionGames.Practice.SignTheLetterGame;
import com.andreaziqing.signlanguagedetectionapp.DetectionGames.Practice.MatchGame;
import com.andreaziqing.signlanguagedetectionapp.DetectionGames.Practice.SpellTheWordGame;
import com.andreaziqing.signlanguagedetectionapp.R;
import com.andreaziqing.signlanguagedetectionapp.Navigation.NavigationTabsController;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Activity that is in charge of the screen in between games.
 *
 * Depending on the state from the game the user is coming (failure, or success), different
 * UI is shown and actions to be chosen are present to the user, E.g. whether if repeating the lesson
 * or game; go to the main menu, etc.
 */
public class BetweenGamesActivity extends AppCompatActivity {

    private static final String BETWEEN_GAMES = "BetweenGamesActivity";

    // For showing a small animation emoji / image
    LottieAnimationView lottieAnimationView;
    private static int SPLASH_TIMER = 5000;

    TextView mTitle, mDesc;
    Button mButtonNext, mButtonRepeat;

    String mPreviousActivity;
    public int mPositionGroup;
    String[] arrGroupOfLetters;
    String mState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_between_games);

        lottieAnimationView = findViewById(R.id.lottie_welldone);
        mTitle = findViewById(R.id.between_games_title);
        mDesc = findViewById(R.id.between_games_desc);
        mButtonNext = findViewById(R.id.next_button);
        mButtonRepeat = findViewById(R.id.repeat_button);

        mState = "SUCCESS";

        // We gather the activity we are coming from, to act in consequence
        // As well as the (if chosen) group of letters user had chosen in the lesson games.
        Bundle bundle = getIntent().getExtras();
        mPreviousActivity = bundle.getString("previousActivity");
        arrGroupOfLetters = bundle.getStringArray("arrGroupOfLetters");
        mPositionGroup = bundle.getInt("position");
        mState = bundle.getString("state");

        if (mState.equals("FAIL")) {
            mTitle.setText(R.string.oh_no);
            mDesc.setText(R.string.try_again_desc);
            mButtonNext.setText(R.string.try_again);
            mButtonRepeat.setVisibility(View.INVISIBLE);
            lottieAnimationView.setAnimation(R.raw.fail);
        }

        // Starts UI animation
        lottieAnimationView.animate().setDuration(600).setStartDelay(4000);

        // Controlling the logic for the next activity / intent to launch based on game control flow.
        new Handler().postDelayed(()-> {

            mButtonNext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    if (mState.equals("FAIL")) {
                        repeatActivity(intent);
                    } else {
                        switch (mPreviousActivity) {
                            case "SecondLesson":
                                intent = new Intent(getApplicationContext(), NavigationTabsController.class);
                                intent.putExtra("nextFragment", "HomeFragment");
                            case "SignTheLetterGame":
                            case "SpellTheWordGame":
                            case "MatchGame":
                                intent = new Intent(getApplicationContext(), NavigationTabsController.class);
                                intent.putExtra("nextFragment", "PracticeFragment");
                                break;
                            default:
                                break;
                        }
                        // Starts the activity based of the state machine selection logic.
                        startActivity(intent);
                        finish();
                    }
                }
            });
            // If user clicks button to repeact activity, relaunch it.
            mButtonRepeat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    repeatActivity(intent);
                }
            });

        }, SPLASH_TIMER);
    }

    /**
     * Function in charge of controlling the activity repeat action.
     *
     * Depending on the current previous activity, starts again the same activity as to repeat the lesson or game.
     * @param intent  intent object
     */
    private void repeatActivity(Intent intent) {
        switch (mPreviousActivity) {
            case "FirstLesson":
                intent = new Intent(getApplicationContext(), FirstLesson.class);
                intent.putExtra("position", mPositionGroup);
                break;
            case "SecondLesson":
                intent = new Intent(getApplicationContext(), SecondLesson.class);
                intent.putExtra("position", mPositionGroup);
                intent.putExtra("arrGroupOfLetters", arrGroupOfLetters);
                break;
            case "SpellTheWordGame":
                intent = new Intent(getApplicationContext(), SpellTheWordGame.class);
                break;
            case "SignTheLetterGame":
                intent = new Intent(getApplicationContext(), SignTheLetterGame.class);
                break;
            case "MatchGame":
                intent = new Intent(getApplicationContext(), MatchGame.class);
                break;
            default:
                break;
        }

        if (intent != null) {
            startActivity(intent);
        }
        finish();
    }

    /**
     * Handles the case when the user choses to close the game upon finishing, going back to Home or to the practice tab.
     * @param view current activity view
     */
    public void close(View view) {
        Intent intent;
        switch (mPreviousActivity) {
            case "SpellTheWordGame":
            case "SignTheLetterGame":
            case "MatchGame":
                intent = new Intent(getApplicationContext(), NavigationTabsController.class);
                intent.putExtra("nextFragment", "PracticeFragment");
                break;
            default:
                intent = new Intent(getApplicationContext(), NavigationTabsController.class);
                intent.putExtra("nextFragment", "HomeFragment");
                break;
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplicationContext().startActivity(intent);
    }

    /*
    Handles the "back" button control logic; going back to the Home tab if corresponds.
     */
    @Override
    public void onBackPressed() {
        int count = getSupportFragmentManager().getBackStackEntryCount();

        if (count == 0) {
            Intent intent = new Intent(getApplicationContext(), NavigationTabsController.class);
            intent.putExtra("nextFragment", "HomeFragment");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getApplicationContext().startActivity(intent);
        } else {
            getSupportFragmentManager().popBackStack();
        }
    }
}