package com.andreaziqing.signlanguagedetectionapp.Database;

import android.widget.TextView;

import java.util.Map;

public interface UserStatsDatabaseInterface {

    public boolean insertNewUser(UserStatsDTO newUser);
    public UserStatsDTO getUserFromUID(String userUID);
    public void updateUserStats(String userUID, Map<String, Object> userUpdates);
    public void updateUsernameView(String userUID, TextView mUsername);
    public void updateUserStatsViews(String userUID, TextView username, TextView ncLessons, TextView ncGames, TextView daysActive);
}
