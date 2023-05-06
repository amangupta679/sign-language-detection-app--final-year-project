package com.andreaziqing.signlanguagedetectionapp.Tabs;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.andreaziqing.signlanguagedetectionapp.Authentication.LoginActivity;
import com.andreaziqing.signlanguagedetectionapp.Database.UserStatsDatabase;
import com.andreaziqing.signlanguagedetectionapp.HelperClasses.Adapters.GlossaryAdapter.GlossaryAdapter;
import com.andreaziqing.signlanguagedetectionapp.HelperClasses.Adapters.GlossaryAdapter.GlossaryHelperClass;
import com.andreaziqing.signlanguagedetectionapp.HelperClasses.Adapters.HomeAdapter.LessonsAdapter;
import com.andreaziqing.signlanguagedetectionapp.HelperClasses.Adapters.HomeAdapter.LessonsHelperClass;
import com.andreaziqing.signlanguagedetectionapp.HelperClasses.Adapters.PracticeAdapter.PracticeHelperClass;
import com.andreaziqing.signlanguagedetectionapp.HelperClasses.Adapters.PracticeAdapter.PracticeHomeAdapter;
import com.andreaziqing.signlanguagedetectionapp.Navigation.NavigationTabsController;
import com.andreaziqing.signlanguagedetectionapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

/**
 * Home fragment class.
 *
 * In charge of the "main" or "home" section of the app.
 *
 * Shows the rest of the main lessons and practice games sections of the application and provides
 * functionality to routing to each of them easily from the home page of the app.
 */
public class HomeFragment extends Fragment {

    private static final String HOME_FRAGMENT = "HomeFragment";

    private static final int CAMERA_PERMISSION_CODE = 100;

    RecyclerView lessonsRecycler;
    RecyclerView practiceGamesRecycler;
    RecyclerView glossaryRecycler;
    RecyclerView.Adapter adapter;

    // * Firebase Auth *
    TextView mUsername;
    ImageButton mLogoutButton;
    Button viewAllPracticeButton, viewAllGlossaryButton;

    private FirebaseAuth firebaseAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    UserStatsDatabase userStatsDB = new UserStatsDatabase();
    FirebaseUser firebaseUser;

    String[] abecedary;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(HOME_FRAGMENT, "Starting Home Fragment");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Permissions
        checkPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE);

        // * Firebase Auth *
        mUsername = view.findViewById(R.id.hi_username);

        // Init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();

        // Hooks
        lessonsRecycler = view.findViewById(R.id.lessons_recycler);
        lessonsRecycler();

        practiceGamesRecycler = view.findViewById(R.id.practice_recycler_home);
        practiceGamesRecycler();

        abecedary = new String[]{"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K",
                "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};

        glossaryRecycler = view.findViewById(R.id.glossary_recycler_home);
        glossaryRecycler();

        // Logout user
        mLogoutButton = view.findViewById(R.id.logout_button);
        mLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                checkUser();
            }
        });

        viewAllPracticeButton = view.findViewById(R.id.view_all_practice_button);
        viewAllGlossaryButton = view.findViewById(R.id.view_all_glossary_button);

        viewAllPracticeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), NavigationTabsController.class);
                intent.putExtra("nextFragment", "PracticeFragment");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getContext().startActivity(intent);
            }
        });

        viewAllGlossaryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), NavigationTabsController.class);
                intent.putExtra("nextFragment", "GlossaryFragment");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getContext().startActivity(intent);
            }
        });

        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireActivity(), "Camera Permission Granted", Toast.LENGTH_SHORT) .show();
            }
            else {
                Toast.makeText(requireActivity(), "Camera Permission Denied", Toast.LENGTH_SHORT) .show();
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", requireActivity().getPackageName(), null));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }
    }

    private void checkPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            // Permission already granted
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            Toast.makeText(
                    requireContext(),
                    "Camera permission is required for this application",
                    Toast.LENGTH_LONG)
                    .show();

            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", requireActivity().getPackageName(), null));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {
            // Requesting the permission
            ActivityCompat.requestPermissions(requireActivity(), new String[] { permission }, requestCode);
        }
    }

    private void checkUser() {
        // Check if user is not logged in then move to login activity

        // Get current user
        firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null) {
            // User is not logged in, move to login screen
            startActivity(new Intent(getContext(), LoginActivity.class));
        } else {
            // Get user name and display in screen
            userStatsDB.updateUsernameView(firebaseUser.getUid(), mUsername);
        }
    }

    private void lessonsRecycler() {
        lessonsRecycler.setHasFixedSize(true);
        lessonsRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        // Get user from database and update lessons adapter view
        db.collection("userstats")
                .document(firebaseUser.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Log.d(HOME_FRAGMENT, "Progress lesson 1: " + document.getString("lesson1"));

                                ArrayList<LessonsHelperClass> lessons = new ArrayList<>();

                                int progressl1 = Integer.parseInt(document.getString("progressl1"));
                                int progressl2 = Integer.parseInt(document.getString("progressl2"));
                                int progressl3 = Integer.parseInt(document.getString("progressl3"));

                                lessons.add(new LessonsHelperClass(R.drawable.atoh, getString(R.string.lesson_1),
                                        getString(R.string.lesson1_desc),
                                        progressl1));
                                lessons.add(new LessonsHelperClass(R.drawable.itop, getString(R.string.lesson_2),
                                        getString(R.string.lesson2_desc),
                                        progressl2));
                                lessons.add(new LessonsHelperClass(R.drawable.qtoz, getString(R.string.lesson_3),
                                        getString(R.string.lesson3_desc),
                                        progressl3));

                                adapter = new LessonsAdapter(lessons);
                                lessonsRecycler.setAdapter(adapter);
                            } else {
                                Log.d(HOME_FRAGMENT, "No such document");
                            }
                        } else {
                            Log.d(HOME_FRAGMENT, "getFailedWith ", task.getException());
                        }
                    }
                });
    }

    private void practiceGamesRecycler() {
        practiceGamesRecycler.setHasFixedSize(true);
        practiceGamesRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        ArrayList<PracticeHelperClass> games = new ArrayList<>();
        games.add(new PracticeHelperClass(R.drawable.guesslettersign, getString(R.string.game1_title)));
        games.add(new PracticeHelperClass(R.drawable.spelltheword, getString(R.string.game2_title)));
        games.add(new PracticeHelperClass(R.drawable.makeamatch, getString(R.string.game3_title)));

        adapter = new PracticeHomeAdapter(games);
        practiceGamesRecycler.setAdapter(adapter);
    }

    private void glossaryRecycler() {
        glossaryRecycler.setHasFixedSize(true);
        glossaryRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        ArrayList<GlossaryHelperClass> letters = new ArrayList<>();

        for (String letter : abecedary) {
            letters.add(new GlossaryHelperClass(getContext().getResources().getIdentifier(letter, "string", getContext().getPackageName())));
        }

        adapter = new GlossaryAdapter(letters);
        glossaryRecycler.setAdapter(adapter);
    }
}