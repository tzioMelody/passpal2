package com.example.passpal2;

import java.util.ArrayList;
import java.util.List;

public class AppsObj {
    private int id; // Προσθέστε αυτή την μεταβλητή
    private String AppNames;
    private String AppLinks;
    private int AppImages;
    private boolean isSelected;

    public AppsObj(String appNames, String appLinks, int appImages) {
        AppNames = appNames;
        AppLinks = appLinks;
        AppImages = appImages;
        isSelected = false;
    }

    public String getAppNames() {
        return AppNames;
    }

    public String getAppLinks() {
        return AppLinks;
    }

    public int getAppImages() {
        return AppImages;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public int getId() { // Προσθέστε αυτή την μέθοδο
        return id;
    }

    public void setId(int id) { // Προσθέστε αυτή την μέθοδο
        this.id = id;
    }

    @Override
    public String toString() {
        return "AppsObj{" +
                "id=" + id +
                ", AppNames='" + AppNames + '\'' +
                '}';
    }

    // Εσωτερική κλάση UserApp
    public static class UserApp {
        private final String appName;
        private final String appLink;

        public UserApp(String appName, String appLink) {
            this.appName = appName;
            this.appLink = appLink;
        }

        public String getAppName() {
            return appName;
        }

        public String getAppLink() {
            return appLink;
        }


    }

    // Στατική λίστα USER_APPS
    public static List<UserApp> USER_APPS = new ArrayList<>();
}
