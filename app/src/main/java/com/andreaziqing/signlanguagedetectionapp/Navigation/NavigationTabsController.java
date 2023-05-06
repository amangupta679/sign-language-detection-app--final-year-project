package com.andreaziqing.signlanguagedetectionapp.Navigation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.andreaziqing.signlanguagedetectionapp.Tabs.AboutFragment;
import com.andreaziqing.signlanguagedetectionapp.Tabs.SettingsFragment;
import com.andreaziqing.signlanguagedetectionapp.R;
import com.andreaziqing.signlanguagedetectionapp.Tabs.GlossaryFragment;
import com.andreaziqing.signlanguagedetectionapp.Tabs.HomeFragment;
import com.andreaziqing.signlanguagedetectionapp.Tabs.PracticeFragment;
import com.andreaziqing.signlanguagedetectionapp.Tabs.RankingFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import java.util.Objects;

/**
 * Navigation Tabs Controller Class.
 *
 * In charge of handling the routing logic of the bottom navigation tab UI of the application.
 * Mainly, routes the "Home", "Practice", "Ranking", "Glossary", "Settings" and "About" fragments.
 */
public class NavigationTabsController extends AppCompatActivity {

    private static final String NAVIGATION_TABS_CONTROLLER = "NavigationTabsController";

    private BottomNavigationView bottomNavigationView;
    private Toolbar toolbar;

    HomeFragment homeFragment = new HomeFragment();
    PracticeFragment practiceFragment = new PracticeFragment();
    RankingFragment rankingFragment = new RankingFragment();
    GlossaryFragment glossaryFragment = new GlossaryFragment();
    SettingsFragment settingsFragment = new SettingsFragment();
    AboutFragment aboutFragment = new AboutFragment();

    String nextFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navigation_tabs_controller);
        Log.i(NAVIGATION_TABS_CONTROLLER, "Starting Navigation Tabs Controller");

        setToolbar();
        initViews(savedInstanceState);
        initComponentsNavHeader();
    }

    /**
     * Sets up the top-left toolbar menu of the application.
     */
    private void setToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(0);
    }

    private void initViews(Bundle savedInstanceState) {
        /**
         * Menu Bottom Navigation
         */

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        setNextFragment();

        /**
         * Menu Navigation Drawer
         */

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.setDrawerIndicatorEnabled(false);
        toggle.setToolbarNavigationClickListener(view -> drawerLayout.openDrawer(GravityCompat.START));
        toggle.setHomeAsUpIndicator(R.drawable.ic_menu_48px);
        toggle.syncState();
    }

    /**
     * Initializes the Components Navigation Header UI containing the "Settings" and "About" sections.
     * Routes, based on navigation item selection, towards the desired fragment load.
     */
    private void initComponentsNavHeader() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.settings:
                        toolbar.setBackgroundColor(Color.parseColor("#FFFFFF"));
                        loadFragment(settingsFragment);
                        break;
                    case R.id.about:
                        toolbar.setBackgroundColor(Color.parseColor("#FFFFFF"));
                        loadFragment(aboutFragment);
                        break;
                    default:
                        break;
                }

                DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
    }

    /**
     * Based of the intent extras bundle value, routes to the "nextFragment" target.
     * In charge of the flow control logic for routing between the navigation tabs.
     */
    private void setNextFragment() {
        try {
            Bundle bundle = getIntent().getExtras();
            nextFragment = bundle.getString("nextFragment");
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d(NAVIGATION_TABS_CONTROLLER, "Next fragment: " + nextFragment);

        if (nextFragment == null) {
            loadFragment(homeFragment);
        } else {
            switch (nextFragment) {
                case "PracticeFragment":
                    loadFragment(practiceFragment);
                    bottomNavigationView.getMenu().getItem(1).setChecked(true);
                    break;
                case "RankingFragment":
                    loadFragment(rankingFragment);
                    bottomNavigationView.getMenu().getItem(2).setChecked(true);
                    break;
                case "GlossaryFragment":
                    loadFragment(glossaryFragment);
                    bottomNavigationView.getMenu().getItem(3).setChecked(true);
                    break;
                default:
                    loadFragment(homeFragment);
                    bottomNavigationView.getMenu().getItem(0).setChecked(true);
                    break;
            }
        }
    }

    private final BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.homeFragment:
                    toolbar.setBackgroundColor(Color.parseColor("#FFFFFF"));
                    loadFragment(homeFragment);
                    return true;
                case R.id.practiceFragment:
                    toolbar.setBackgroundColor(Color.parseColor("#283149"));
                    loadFragment(practiceFragment);
                    return true;
                case R.id.rankingFragment:
                    toolbar.setBackgroundColor(Color.parseColor("#283149"));
                    loadFragment(rankingFragment);
                    return true;
                case R.id.glossaryFragment:
                    toolbar.setBackgroundColor(Color.parseColor("#FFFFFF"));
                    loadFragment(glossaryFragment);
                    return true;
                default:
                    return false;
            }
        }
    };

    /**
     * Load fragment.
     * Handles the fragment manager transaction commits to the selected target fragment.
     * @param fragment the fragment
     */
    public void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_container, fragment);
        transaction.commit();
    }

    /**
     * Handles "back" button press logic. Closes drawer if is already open, else heads to home screen.
     */
    @Override
    public void onBackPressed() {
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            loadFragment(homeFragment);
        }
    }
}