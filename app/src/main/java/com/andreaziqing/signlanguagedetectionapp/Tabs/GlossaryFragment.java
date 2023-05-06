package com.andreaziqing.signlanguagedetectionapp.Tabs;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.andreaziqing.signlanguagedetectionapp.HelperClasses.Adapters.GlossaryAdapter.GlossaryAdapter;
import com.andreaziqing.signlanguagedetectionapp.HelperClasses.Adapters.GlossaryAdapter.GlossaryHelperClass;
import com.andreaziqing.signlanguagedetectionapp.R;

import java.util.ArrayList;

/**
 * Glossary fragment class.
 * In charge of the "Glossary" section of the application showing the different A-Z sign letters.
 */
public class GlossaryFragment extends Fragment {

    private static final String GLOSSARY_FRAGMENT = "GlossaryFragment";

    RecyclerView glossaryRecycler;
    RecyclerView.Adapter adapter;

    String[] abecedary;

    public GlossaryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(GLOSSARY_FRAGMENT, "Starting Glossary Fragment");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_glossary, container, false);

        abecedary = new String[]{"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K",
                "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};

        // Hooks
        glossaryRecycler = view.findViewById(R.id.glossary_recycler);
        glossaryRecycler();

        return view;
    }

    private void glossaryRecycler() {
        glossaryRecycler.setHasFixedSize(true);
        glossaryRecycler.setLayoutManager(new GridLayoutManager(getContext(), 5));

        ArrayList<GlossaryHelperClass> letters = new ArrayList<>();

        for (String letter : abecedary) {
            letters.add(new GlossaryHelperClass(getContext().getResources().getIdentifier(letter, "string", getContext().getPackageName())));
        }

        adapter = new GlossaryAdapter(letters);
        glossaryRecycler.setAdapter(adapter);
    }
}