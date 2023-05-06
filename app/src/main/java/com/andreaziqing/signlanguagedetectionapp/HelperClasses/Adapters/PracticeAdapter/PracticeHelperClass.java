package com.andreaziqing.signlanguagedetectionapp.HelperClasses.Adapters.PracticeAdapter;

public class PracticeHelperClass {
    int image;
    String title, desc;

    public PracticeHelperClass(int image, String title, String desc) {
        this.image = image;
        this.title = title;
        this.desc = desc;
    }

    public PracticeHelperClass(int image, String title) {
        this.image = image;
        this.title = title;
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
}
