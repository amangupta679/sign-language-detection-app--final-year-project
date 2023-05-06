package com.andreaziqing.signlanguagedetectionapp.HelperClasses.Adapters.RankingAdapter;

public class RankingHelperClass {
    String username;
    long ncLessons, ncGames, daysActive;

    public RankingHelperClass(String username, long ncLessons, long ncGames, long daysActive) {
        this.username = username;
        this.ncLessons = ncLessons;
        this.ncGames = ncGames;
        this.daysActive = daysActive;
    }

    public String getUsername() {
        return username;
    }

    public long getNcLessons() {
        return ncLessons;
    }

    public long getNcGames() {
        return ncGames;
    }

    public long getDaysActive() {
        return daysActive;
    }
}