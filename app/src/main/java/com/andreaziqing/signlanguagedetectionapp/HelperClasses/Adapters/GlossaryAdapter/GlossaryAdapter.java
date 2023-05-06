package com.andreaziqing.signlanguagedetectionapp.HelperClasses.Adapters.GlossaryAdapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.andreaziqing.signlanguagedetectionapp.Tabs.GlossarySlider;
import com.andreaziqing.signlanguagedetectionapp.R;

import java.util.ArrayList;

public class GlossaryAdapter extends RecyclerView.Adapter<GlossaryAdapter.GlossaryViewHolder> {
    ArrayList<GlossaryHelperClass> letters;
    private Context context;

    public GlossaryAdapter(ArrayList<GlossaryHelperClass> letters) {
        this.letters = letters;
    }

    @NonNull
    @Override
    public GlossaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.glossary_card_design, parent, false);
        GlossaryViewHolder glossaryViewHolder = new GlossaryViewHolder(view);
        context = parent.getContext();

        return glossaryViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull GlossaryViewHolder holder, int position) {

        GlossaryHelperClass practiceHelperClass = letters.get(position);
        holder.title.setText(practiceHelperClass.getTitle());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, GlossarySlider.class);
                intent.putExtra("position", holder.getAdapterPosition());
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return letters.size();
    }

    public static class GlossaryViewHolder extends RecyclerView.ViewHolder {

        TextView title, desc;

        public GlossaryViewHolder(@NonNull View itemView) {
            super(itemView);

            // Hooks
            title = itemView.findViewById(R.id.letter_title);
        }
    }

}
