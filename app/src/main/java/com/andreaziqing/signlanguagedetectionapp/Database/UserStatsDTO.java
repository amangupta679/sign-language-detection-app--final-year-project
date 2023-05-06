package com.andreaziqing.signlanguagedetectionapp.Database;

import com.google.firebase.firestore.FieldValue;
import java.util.HashMap;
import java.util.Map;

public class UserStatsDTO {

    private String userName;
    private String userUID;
    private FieldValue lastLogin;
    private FieldValue regDate;
    private int nclessons;
    private int ncgames;
    private String progressl1;
    private String progressl2;
    private String progressl3;

    public UserStatsDTO(String userUID, String userName) {
        this.userUID = userUID;
        this.userName = userName;
        this.lastLogin = FieldValue.serverTimestamp();
        this.regDate = FieldValue.serverTimestamp();
        this.ncgames = 0;
        this.nclessons = 0;
        this.progressl1 = "0";
        this.progressl2 = "0";
        this.progressl3 = "0";
    }

    public UserStatsDTO(String userUID,
                        String userName,
                        FieldValue lastLogin,
                        FieldValue regDate,
                        int nclessons,
                        int ncgames,
                        String progressl1,
                        String progressl2,
                        String progressl3) {
        this.userName = userName;
        this.userUID = userUID;
        this.lastLogin = lastLogin;
        this.regDate = regDate;
        this.nclessons = nclessons;
        this.ncgames = ncgames;
        this.progressl1 = progressl1;
        this.progressl2 = progressl2;
        this.progressl3 = progressl3;
    }

    public Map<String, Object> toUserMap(){

        Map<String, Object> userMap = new HashMap<>();

        userMap.put("name", getUsername());
        userMap.put("regdate", getRegDate());
        userMap.put("lastlogin", getLastLogin());
        userMap.put("ncgames", getNcgames());
        userMap.put("nclessons", getNclessons());
        userMap.put("progressl1", getProgressl1());
        userMap.put("progressl2", getProgressl2());
        userMap.put("progressl3", getProgressl3());

        return userMap;
    }
    public String getUsername() {
        return userName;
    }

    public void setUsername(String username) {
        this.userName = username;
    }

    public String getUserUID() {
        return userUID;
    }

    public FieldValue getLastLogin() {
        return lastLogin;
    }

    public FieldValue getRegDate() {
        return regDate;
    }

    public int getNclessons() {
        return nclessons;
    }

    public int getNcgames() {
        return ncgames;
    }

    public String getProgressl1() {
        return progressl1;
    }

    public String getProgressl2() {
        return progressl2;
    }

    public String getProgressl3() {
        return progressl3;
    }
}
