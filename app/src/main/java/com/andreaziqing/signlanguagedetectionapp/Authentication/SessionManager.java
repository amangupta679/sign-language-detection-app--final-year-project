package com.andreaziqing.signlanguagedetectionapp.Authentication;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;

/**
 * Class that handles the user session in the application.
 * Handles "Remember me" user session mappings.
 */
public class SessionManager {

    // Variables
    SharedPreferences usersSession;
    SharedPreferences.Editor editor;
    Context context;

    // Sessions names
    public static final String SESSION_REMEMBERME = "rememberMe";

    // Remember me variables
    private static final String IS_REMEMBERME = "IsRememberMe";
    public static final String KEY_USERNAME_REMEMBER = "username";
    public static final String KEY_PASSWORD_REMEMBER = "password";

    // Constructor
    public SessionManager(Context _context, String sessionName) {
        context = _context;
        usersSession = context.getSharedPreferences(sessionName, Context.MODE_PRIVATE);
        editor = usersSession.edit(); // to update the data
    }

    // Remember Me - Session Functions
    public void createRememberMeSession(String username, String password) {
        editor.putBoolean(IS_REMEMBERME, true);
        editor.putString(KEY_USERNAME_REMEMBER, username);
        editor.putString(KEY_PASSWORD_REMEMBER, password);

        editor.commit();
    }


    /**
     * Function that puts in the userData map the details from the remembered session.
     *
     * @return Map with the user-pass relationship
     */
    public HashMap<String, String> getRememberMeDetailFromSession() {
        HashMap<String, String> userData = new HashMap<String, String>();

        userData.put(KEY_USERNAME_REMEMBER, usersSession.getString(KEY_USERNAME_REMEMBER, null));
        userData.put(KEY_PASSWORD_REMEMBER, usersSession.getString(KEY_PASSWORD_REMEMBER, null));

        return userData;
    }

    /**
     * @return True if user session is set to be remembered, False elsewhere.
     */
    public boolean checkRememberMe() {
        if (usersSession.getBoolean(IS_REMEMBERME, false)) {
            return true;
        } else {
            return false;
        }
    }
}
