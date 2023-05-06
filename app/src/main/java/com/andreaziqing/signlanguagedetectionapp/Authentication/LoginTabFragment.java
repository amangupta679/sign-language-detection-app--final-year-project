package com.andreaziqing.signlanguagedetectionapp.Authentication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.andreaziqing.signlanguagedetectionapp.Database.UserStatsDatabase;
import com.andreaziqing.signlanguagedetectionapp.Navigation.NavigationTabsController;
import com.andreaziqing.signlanguagedetectionapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Fragment class in charge of the Login Tab Screen logic.
 */
public class LoginTabFragment extends Fragment {

    private static final String LOGIN_TAB_FRAGMENT = "Login Tab Fragment";

    Button login_button;
    CheckBox rememberMe;
    TextInputEditText emailEditText, passwordEditText;
    float v = 0;

    // * Firebase Auth *
    TextInputLayout mEmail, mPassword;
    private FirebaseAuth firebaseAuth;

    // progress dialog
    private ProgressDialog progressDialog;

    UserStatsDatabase userStatsDB = new UserStatsDatabase();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_login_tab, container, false);

        // * Firebase Auth *
        // Init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();

        // Configure progress dialog
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle(getString(R.string.please_wait));
        progressDialog.setMessage(getString(R.string.logging_in));
        progressDialog.setCanceledOnTouchOutside(false);

        mEmail = view.findViewById(R.id.email_login);
        mPassword = view.findViewById(R.id.password_login);

        login_button = view.findViewById(R.id.login_button);
        rememberMe = view.findViewById(R.id.remember_me);
        emailEditText = view.findViewById(R.id.editEmailLogin);
        passwordEditText = view.findViewById(R.id.editPasswordLogin);

        // Check weather username and password is already saved in Shared Preferences or not
        SessionManager sessionManager = new SessionManager(getContext(), SessionManager.SESSION_REMEMBERME);
        if (sessionManager.checkRememberMe()) {
            HashMap<String, String> rememberMeDetails = sessionManager.getRememberMeDetailFromSession();
            emailEditText.setText(rememberMeDetails.get(SessionManager.KEY_USERNAME_REMEMBER));
            passwordEditText.setText(rememberMeDetails.get(SessionManager.KEY_PASSWORD_REMEMBER));
        }

        // Animation
        mEmail.setTranslationX(800);
        mPassword.setTranslationX(800);
        login_button.setTranslationX(800);

        mEmail.setAlpha(v);
        mPassword.setAlpha(v);
        login_button.setAlpha(v);

        mEmail.animate().translationX(0).alpha(1).setDuration(800).setStartDelay(300).start();
        mPassword.animate().translationX(0).alpha(1).setDuration(800).setStartDelay(500).start();
        login_button.animate().translationX(0).alpha(1).setDuration(800).setStartDelay(700).start();

        // Save data in Firebase on button click
        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Validate Login info
                if (!validateEmail() | !validatePassword()) {
                    return;
                } else {
                    // Get values from fields
                    final String email = mEmail.getEditText().getText().toString().trim();
                    final String password = mPassword.getEditText().getText().toString().trim();

                    firebaseLogin(email, password);

                    // If user checks the "Remember Me" section, a Session manager is instantiated recalling the session details.
                    if (rememberMe.isChecked()) {
                        SessionManager sessionManager = new SessionManager(getContext(), SessionManager.SESSION_REMEMBERME);
                        sessionManager.createRememberMeSession(email, password);
                    }
//                    isUser();
                }
            }
        });

        return view;
    }

    /**
     *  Checks if user is already logged in.
     *  If so, goes directly to the Home Activity.
     */
    private void checkUser() {
        // Get current user
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            // User is already logged in
            startActivity(new Intent(getContext(), NavigationTabsController.class));
        }
    }

    // * Utils functions *

    /**
     * Handles login through Firebase Authentication.
     *
     * @param email User email for login into the application
     * @param password User password.
     */
    private void firebaseLogin(String email, String password) {
        // Show progress
        progressDialog.show();
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                // Login success
                progressDialog.dismiss();
                // Get user info
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                String email = firebaseUser.getEmail();
                Toast.makeText(getContext(), getString(R.string.logged_in) + email, Toast.LENGTH_SHORT).show();

                Map<String, Object> dataToUpdate = new HashMap<>();
                dataToUpdate.put("lastlogin", FieldValue.serverTimestamp());
                userStatsDB.updateUserStats(firebaseUser.getUid(), dataToUpdate);

                // Open Home Activity
                startActivity(new Intent(getContext(), NavigationTabsController.class));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Login failed
                progressDialog.dismiss();
                Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Handles email validation logic.
     * @return True if email has been correctly validated, False otherwise.
     */
    private Boolean validateEmail() {
        String value = mEmail.getEditText().getText().toString();
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

        if (value.isEmpty()){
            mEmail.setError(getString(R.string.field_cannot_be_empty));
            return false;
        } else if (!value.matches(emailPattern)) {
            mEmail.setError(getString(R.string.invalid_email));
            return false;
        } else {
            mEmail.setError(null);
            mEmail.setErrorEnabled(false);
            return true;
        }
    }

    /**
     * Handles password security validation logic.
     * @return True if password security has been correctly validated, False otherwise.
     */
    private Boolean validatePassword() {
        String value = mPassword.getEditText().getText().toString();

        if (value.isEmpty()) {
            mPassword.setError(getString(R.string.field_cannot_be_empty));
            return false;
        } else {
            mPassword.setError(null);
            mPassword.setErrorEnabled(false);
            return true;
        }
    }
}
