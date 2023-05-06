package com.andreaziqing.signlanguagedetectionapp.Authentication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.andreaziqing.signlanguagedetectionapp.Database.UserStatsDTO;
import com.andreaziqing.signlanguagedetectionapp.Database.UserStatsDatabase;
import com.andreaziqing.signlanguagedetectionapp.Navigation.NavigationTabsController;
import com.andreaziqing.signlanguagedetectionapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Fragment class in charge of the Sign Up Tab Screen logic.
 */
public class SignUpTabFragment extends Fragment {

    private static final String SIGNUP_TAB_FRAGMENT = "Sign Up Tab Fragment";

    // Variables
    Button signup_button;

    // * Firebase Auth *
    TextInputLayout mUsername, mEmail, mPhoneNumber, mPassword;
    private FirebaseAuth firebaseAuth;

    // progress dialog
    private ProgressDialog progressDialog;

    UserStatsDatabase userStatsDB = new UserStatsDatabase();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_signup_tab, container, false);

        // * Firebase Auth *
        // Init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        // Configure progress dialog
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle(getString(R.string.please_wait));
        progressDialog.setMessage(getString(R.string.creating_account));
        progressDialog.setCanceledOnTouchOutside(false);

        mEmail = view.findViewById(R.id.email_signup);
        mPassword = view.findViewById(R.id.password_login);
        mUsername = view.findViewById(R.id.username);
        mPhoneNumber = view.findViewById(R.id.phone_num);
        signup_button = view.findViewById(R.id.signup_button);

        // Save data in Firebase on button click
        signup_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!validateUsername() || !validateEmail() || !validatePhoneNumber() || !validatePassword()) {
                    return;
                }

                String username = mUsername.getEditText().getText().toString();
                String email = mEmail.getEditText().getText().toString();
                String phoneNumber = mPhoneNumber.getEditText().getText().toString();
                String password = mPassword.getEditText().getText().toString();

                // Data is valid, now continue firebase signup
                firebaseSignUp(username, email, phoneNumber, password);
            }
        });
        return view;
    }

    // * Utils functions *


    /**
     * Handles Firebase Authentication Sign Up process.
     * @param username Username chosen by the user upon sign up.
     * @param email User email
     * @param phoneNumber User phone number for validation
     * @param password User chosen password
     */
    private void firebaseSignUp(String username, String email, String phoneNumber, String password) {
        // Show progress
        progressDialog.show();
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                // Sign up success
                progressDialog.dismiss();

                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

                Toast.makeText(getContext(), getString(R.string.account_created) + email, Toast.LENGTH_SHORT).show();

                // Send email verification
                firebaseUser.sendEmailVerification()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d(SIGNUP_TAB_FRAGMENT, "Verification email sent.");
                                }
                            }
                        });

                UserStatsDTO newUser = new UserStatsDTO(firebaseUser.getUid(), username);

                // Update internal user stats database with the newly created user information.
                // The document ID is set to the firebase auth user ID; allowing for later identifying user data.
                userStatsDB.insertNewUser(newUser);

                // Open Home Activity
                startActivity(new Intent(getContext(), NavigationTabsController.class));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Sign up failed
                progressDialog.dismiss();
                Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Handles username validation logic.
     * @return True if user has been correctly validated, False otherwise.
     */
    private Boolean validateUsername() {
        String value = mUsername.getEditText().getText().toString();
        String noWhiteSpace = "\\A\\w{4,20}\\z";

        if (value.isEmpty()){
            mUsername.setError(getString(R.string.field_cannot_be_empty));
            return false;
        } else if (value.length() >= 15) {
            mUsername.setError(getString(R.string.username_too_long));
            return false;
        } else if (!value.matches(noWhiteSpace)) {
            mUsername.setError(getString(R.string.white_spaces));
            return false;
        } else {
            mUsername.setError(null);
            mUsername.setErrorEnabled(false);
            return true;
        }
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
     * Handles phone number validation logic.
     * @return True if phone number has been correctly validated, False otherwise.
     */
    private Boolean validatePhoneNumber() {
        String value = mPhoneNumber.getEditText().getText().toString();

        if (value.isEmpty()){
            mPhoneNumber.setError(getString(R.string.field_cannot_be_empty));
            return false;
        } else {
            mPhoneNumber.setError(null);
            mPhoneNumber.setErrorEnabled(false);
            return true;
        }
    }

    /**
     * Handles password validation logic.
     * @return True if password has been correctly validated, False otherwise.
     */
    private Boolean validatePassword() {
        String value = mPassword.getEditText().getText().toString();
        String passwordValue = "^" +
                "(?=.*[a-zA-Z])" +
                "(?=.*[@#$%^&+=*])" +
                "(?=\\S+$)" +
                ".{4,}" +
                "$";

        if (value.isEmpty()) {
            mPassword.setError(getString(R.string.field_cannot_be_empty));
            return false;
        } else if (!value.matches(passwordValue)) {
            mPassword.setError(getString(R.string.password_too_weak));
            return false;
        } else {
            mPassword.setError(null);
            mPassword.setErrorEnabled(false);
            return true;
        }
    }
}
