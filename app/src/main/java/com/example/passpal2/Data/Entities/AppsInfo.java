package com.example.passpal2.Data.Entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "app_info_table")
public class AppsInfo {
    @PrimaryKey(autoGenerate = true)
    private int id; // Πρωτεύον κλειδί, αυτόματα αυξανόμενο

    private String appName;
    private String appLink;
    private int imageResource;
    private boolean isSelected;

    public AppsInfo(String appName, String appLink, int imageResource) {
        this.appName = appName;
        this.appLink = appLink;
        this.imageResource = imageResource;
        this.isSelected = false;
    }

    // Προσθήκη getter και setter μεθόδων για τα πεδία της κλάσης
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppLink() {
        return appLink;
    }

    public void setAppLink(String appLink) {
        this.appLink = appLink;
    }

    public int getImageResource() {
        return imageResource;
    }

    public void setImageResource(int imageResource) {
        this.imageResource = imageResource;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

}