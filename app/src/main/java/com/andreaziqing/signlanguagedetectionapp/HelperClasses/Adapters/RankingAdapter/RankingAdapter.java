package com.andreaziqing.signlanguagedetectionapp.HelperClasses.Adapters.RankingAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.andreaziqing.signlanguagedetectionapp.R;

import java.util.ArrayList;

public class RankingAdapter extends RecyclerView.Adapter<RankingAdapter.RankingViewHolder> {

    ArrayList<RankingHelperClass> usersStats;
    private Context context;

    public RankingAdapter(ArrayList<RankingHelperClass> usersStats) {
        this.usersStats = usersStats;
    }

    @NonNull
    @Override
    public RankingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ranking_card_design, parent, false);
        RankingAdapter.RankingViewHolder rankingViewHolder = new RankingAdapter.RankingViewHolder(view);
        context = parent.getContext();

        return rankingViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RankingAdapter.RankingViewHolder holder, int position) {

        RankingHelperClass rankingHelperClass = usersStats.get(position);

        holder.username.setText(rankingHelperClass.getUsername());
        holder.ncLessons.setText(String.valueOf(rankingHelperClass.getNcLessons()));
        holder.ncGames.setText(String.valueOf(rankingHelperClass.getNcGames()));
        holder.daysActive.setText(String.valueOf(rankingHelperClass.getDaysActive()));
    }

    @Override
    public int getItemCount() {
        return usersStats.size();
    }

    public static class RankingViewHolder extends RecyclerView.ViewHolder {

        TextView username, ncLessons, ncGames, daysActive;

        public RankingViewHolder(@NonNull View itemView) {
            super(itemView);

            // Hooks
            username = itemView.findViewById(R.id.ranking_card_username);
            ncLessons = itemView.findViewById(R.id.n_lessons_card_user);
            ncGames = itemView.findViewById(R.id.n_games_card_user);
            daysActive = itemView.findViewById(R.id.n_days_active_card_user);
        }
    }
}