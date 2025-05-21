package com.example.healthmate;

public class Feature {
    public final int iconRes;
    public final String title;
    public final String subtitle;
    public final Class<?> activityClass;

    public Feature(int iconRes, String title, String subtitle, Class<?> activityClass) {
        this.iconRes = iconRes;
        this.title = title;
        this.subtitle = subtitle;
        this.activityClass = activityClass;
    }
}
