package com.andreaziqing.signlanguagedetectionapp.Database;

import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 * User stats database class.
 *
 * Implementation of a Database access object with several Insert, Get and Update methods
 * aimed towards transactions with UserStats objects (UserStatsDTO).
 *
 * Handles all DB interaction logic and wraps around a Firebase Firestore DB instance.
 */
public class UserStatsDatabase implements UserStatsDatabaseInterface{

    private static final String USER_STATS_DATABASE = "UserStatsDatabase";
    private static final String DB_NAME = "userstats";

    private FirebaseFirestore db;

    public UserStatsDatabase(){
        // Firestore Database instance
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Function handling the insert action of a new user object into the database..
     * @param newUser : new user DTO object to insert.
     * @return true if correct insertion, else false.
     */
    public boolean insertNewUser(UserStatsDTO newUser){

        Map<String, Object> newUserMap = newUser.toUserMap();
        final boolean[] isSuccessful = {false};

        db.collection(DB_NAME)
                .document(newUser.getUserUID())
                .set(newUserMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(USER_STATS_DATABASE, "DocumentSnapshot added with ID: " + newUser.getUserUID());
                        isSuccessful[0] = true;
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(USER_STATS_DATABASE, "Error adding document", e);
                    }
                });
        return isSuccessful[0];

    }

    /**
     * Function handling the retrieval action of a given user object from the database given its Firebase UID.
     * @param userUID : User Firebase Auth UID to look for in the database
     * @return UserStatsDTO object of the retrieved user.
     */
    public UserStatsDTO getUserFromUID(String userUID){
        final String[] username = new String[1];
        final int[] nclessons = new int[1];
        final int[] ncgames = new int[1];
        final FieldValue[] regdate = new FieldValue[1];
        final FieldValue[] lastlogin = new FieldValue[1];
        final String[] progressl1 = new String[1];
        final String[] progressl2 = new String[1];
        final String[] progressl3 = new String[1];

        db.collection(DB_NAME)
                .document(userUID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                username[0] = document.getString("name");
                                nclessons[0] = (int) document.getData().get("nclessons");
                                ncgames[0] = (int) document.getData().get("ncgames");
                                regdate[0] = (FieldValue) document.getData().get("regdate");
                                lastlogin[0] = (FieldValue) document.getData().get("lastlogin");
                                progressl1[0] = document.getString("progressl1");
                                progressl2[0] = document.getString("progressl2");
                                progressl3[0] = document.getString("progressl3");
                            } else {
                                Log.d(USER_STATS_DATABASE, "No such document");
                            }
                        } else {
                            Log.d(USER_STATS_DATABASE, "get failed with ", task.getException());
                        }
                    }
                });
        UserStatsDTO user = new UserStatsDTO(userUID,
                                       username[0],
                                       lastlogin[0],
                                       regdate[0],
                                       nclessons[0],
                                       ncgames[0],
                                       progressl1[0],
                                       progressl2[0],
                                       progressl3[0]);

        return user;
    }


    /**
     * Function handling the update action of a given user object field(s) into the database
     * @param userUID : User Firebase Auth UID to look for in the database
     * @return userUpdates map containing the desired fields and values to update.
     */
    public void updateUserStats(String userUID, Map<String, Object> userUpdates){

        db.collection(DB_NAME)
                .document(userUID)
                .set(userUpdates, SetOptions.merge());

    }

    /**
     * Function handling the DB retrieval and subsequent UI update of the username field retrieved.
     * @param userUID : User Firebase Auth UID to look for in the database
     * @param mUsername : Username text view to update.
     */
    public void updateUsernameView(String userUID, TextView mUsername){
        db.collection("userstats")
                .document(userUID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                mUsername.setText(document.getString("name"));
                            } else {
                                Log.d(USER_STATS_DATABASE, "No such document");
                            }
                        } else {
                            Log.d(USER_STATS_DATABASE, "get failed with ", task.getException());
                        }
                    }
                });
    }


    /**
     * Function handling the DB retrieval and subsequent UI updates of the Ranking TextViews
     * @param userUID : User Firebase Auth UID to look for in the database
     * @param username : Username text view to update.
     * @param ncLessons : Number of completed lessons text view to update.
     * @param ncGames : Number of completed games text view to update.
     * @param daysActive : Days active text view to update.
     */
    public void updateUserStatsViews(String userUID, TextView username, TextView ncLessons, TextView ncGames, TextView daysActive){
        db.collection(DB_NAME)
                .document(userUID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                username.setText(document.getString("name"));
                                ncLessons.setText(String.valueOf(document.getData().get("nclessons")));
                                ncGames.setText(String.valueOf(document.getData().get("ncgames")));

                                Timestamp timestamp_regdate = (Timestamp) document.getData().get("regdate");
                                Timestamp timestamp_lastlogin = (Timestamp) document.getData().get("lastlogin");

                                Date regdate = timestamp_regdate.toDate();
                                Date lastlogin = timestamp_lastlogin.toDate();

                                long diffInMillies = Math.abs(regdate.getTime() - lastlogin.getTime());
                                long daysBetween = (int) TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);

                                daysActive.setText(String.valueOf(daysBetween));
                            } else {
                                Log.d(USER_STATS_DATABASE, "No such document");
                            }
                        } else {
                            Log.d(USER_STATS_DATABASE, "get failed with ", task.getException());
                        }
                    }
                });

    }

}
