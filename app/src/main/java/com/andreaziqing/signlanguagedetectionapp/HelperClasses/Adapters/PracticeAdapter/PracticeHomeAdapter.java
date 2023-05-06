package com.andreaziqing.signlanguagedetectionapp.HelperClasses.Adapters.PracticeAdapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.andreaziqing.signlanguagedetectionapp.DetectionGames.Practice.SignTheLetterGame;
import com.andreaziqing.signlanguagedetectionapp.DetectionGames.Practice.MatchGame;
import com.andreaziqing.signlanguagedetectionapp.DetectionGames.Practice.SpellTheWordGame;
import com.andreaziqing.signlanguagedetectionapp.R;

import java.util.ArrayList;

public class PracticeHomeAdapter extends RecyclerView.Adapter<PracticeHomeAdapter.PracticeHomeViewHolder> {

    ArrayList<PracticeHelperClass> games;
    private Context context;

    public PracticeHomeAdapter(ArrayList<PracticeHelperClass> games) {
        this.games = games;
    }

    @NonNull
    @Override
    public PracticeHomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.practice_home_card_design, parent, false);
        PracticeHomeViewHolder practiceHomeViewHolder = new PracticeHomeViewHolder(view);
        context = parent.getContext();

        return practiceHomeViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull PracticeHomeViewHolder holder, int position) {

        PracticeHelperClass practiceHelperClass = games.get(position);
        holder.image.setImageResource(practiceHelperClass.getImage());
        holder.title.setText(practiceHelperClass.getTitle());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                switch (holder.getAdapterPosition()) {
                    case 0:
                        intent = new Intent(context, SignTheLetterGame.class);
                        break;
                    case 1:
                        intent = new Intent(context, SpellTheWordGame.class);
                        break;
                    case 2:
                        intent = new Intent(context, MatchGame.class);
                        break;
                    default:
                        break;
                }
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return games.size();
    }

    public static class PracticeHomeViewHolder extends RecyclerView.ViewHolder {

        ImageView image;
        TextView title;

        public PracticeHomeViewHolder(@NonNull View itemView) {
            super(itemView);

            // Hooks
            image = itemView.findViewById(R.id.practice_home_game_image);
            title = itemView.findViewById(R.id.practice_home_game_title);
        }
    }
}