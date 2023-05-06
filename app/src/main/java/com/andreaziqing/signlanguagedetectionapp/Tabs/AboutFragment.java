package com.andreaziqing.signlanguagedetectionapp.Tabs;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.andreaziqing.signlanguagedetectionapp.Authentication.LoginActivity;
import com.andreaziqing.signlanguagedetectionapp.Database.UserStatsDatabase;
import com.andreaziqing.signlanguagedetectionapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * About fragment class.
 * In charge of the "About" section of the application showing basic author information.
 */
public class AboutFragment extends Fragment {

    private static final String ABOUT_FRAGMENT = "AboutFragment";

    ImageButton mLogoutButton;

    private FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    public AboutFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(ABOUT_FRAGMENT, "Starting About Fragment");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, container, false);

        // Init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        // Logout user
        mLogoutButton = view.findViewById(R.id.logout_button);
        mLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                checkUser();
            }
        });

        return view;
    }

    private void checkUser() {
        // Check if user is not logged in then move to login activity

        // Get current user
        firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null) {
            // User is not logged in, move to login screen
            startActivity(new Intent(getContext(), LoginActivity.class));
        }
    }
}