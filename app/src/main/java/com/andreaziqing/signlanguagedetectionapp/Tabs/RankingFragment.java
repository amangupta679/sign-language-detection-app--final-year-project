package com.andreaziqing.signlanguagedetectionapp.Tabs;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.andreaziqing.signlanguagedetectionapp.Database.UserStatsDatabase;
import com.andreaziqing.signlanguagedetectionapp.HelperClasses.Adapters.RankingAdapter.RankingAdapter;
import com.andreaziqing.signlanguagedetectionapp.HelperClasses.Adapters.RankingAdapter.RankingHelperClass;
import com.andreaziqing.signlanguagedetectionapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Ranking fragment class.
 *
 * In charge of the "Ranking" section of the app. Handles showing to the user the stats of their
 * contribution or learning in the app, as well as loads a ranking table extracted from the application
 * database showing other users' progress (such as number of games completed, etc).
 */
public class RankingFragment extends Fragment {

    private static final String RANKING_FRAGMENT = "RankingFragment";

    RecyclerView rankingRecycler;
    RecyclerView.Adapter adapter;

    TextView username, ncLessons, ncGames, daysActive;

    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    UserStatsDatabase userStatsDB = new UserStatsDatabase();
    FirebaseUser firebaseUser;

    public RankingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(RANKING_FRAGMENT, "Starting Ranking Fragment");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ranking, container, false);

        firebaseUser = firebaseAuth.getCurrentUser();

        username = view.findViewById(R.id.ranking_username);
        ncLessons = view.findViewById(R.id.n_lessons_user);
        ncGames = view.findViewById(R.id.n_games_user);
        daysActive = view.findViewById(R.id.n_active_days_user);

        // Get user stats and update screen views
        userStatsDB.updateUserStatsViews(firebaseUser.getUid(), username, ncLessons, ncGames, daysActive);

        // Hooks
        rankingRecycler = view.findViewById(R.id.ranking_recycler);
        rankingRecycler();

        return view;
    }

    private void rankingRecycler() {
        rankingRecycler.setHasFixedSize(true);
        rankingRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        // Get users stats full ranking and display in screen each of them
        db.collection("userstats")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            ArrayList<RankingHelperClass> usersStats = new ArrayList<>();

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(RANKING_FRAGMENT, document.getId() + " => " + document.getData());

                                String username = document.getString("name");
                                long ncLessons = (long) document.getData().get("nclessons");
                                long ncGames = (long) document.getData().get("ncgames");

                                Timestamp timestamp_regdate = (Timestamp) document.getData().get("regdate");
                                Timestamp timestamp_lastlogin = (Timestamp) document.getData().get("lastlogin");

                                Date regdate = timestamp_regdate.toDate();
                                Date lastlogin = timestamp_lastlogin.toDate();

                                long diffInMillies = Math.abs(regdate.getTime() - lastlogin.getTime());
                                long daysActive = (int) TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);

                                usersStats.add(new RankingHelperClass(username, ncLessons, ncGames, daysActive));
                            }

                            adapter = new RankingAdapter(usersStats);
                            rankingRecycler.setAdapter(adapter);
                        }
                    }
                });
    }
}