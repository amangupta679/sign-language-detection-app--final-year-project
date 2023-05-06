package com.andreaziqing.signlanguagedetectionapp.HelperClasses.Adapters.HomeAdapter;

import android.widget.ProgressBar;

public class LessonsHelperClass {
    int image;
    String title, desc;
    int progress;

    public LessonsHelperClass(int image, String title, String desc, int progress) {
        this.image = image;
        this.title = title;
        this.desc = desc;
        this.progress = progress;
    }

    public int getImage() {
        return image;
    }

    public String getTitle() {
        return title;
    }

    public String getDesc() {
        return desc;
    }

    public int getProgress() {
        return progress;
    }
}
