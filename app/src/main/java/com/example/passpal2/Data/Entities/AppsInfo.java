package com.example.passpal2.Data.Entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "app_info_table")
public class AppsInfo {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String appName;
    private String appLink;
    private int imageResource;
    private boolean isSelected;

    // Προσθήκη πεδίου userId
    private int userId;

    public AppsInfo(String appName, String appLink, int imageResource) {
        this.appName = appName;
        this.appLink = appLink;
        this.imageResource = imageResource;
        this.isSelected = false;
    }

    // Προσθήκη άδειου κατασκευαστή
    public AppsInfo() {
        // Αφήστε τον άδειο κατασκευαστή για τη χρήση από τον CursorAdapter
    }


    // Προσθήκη getter και setter μεθόδων για τα πεδία της κλάσης
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    //ειναι η αντιστοιχιση του χρηστη με τις εφαρμογες που εχει επιλεξει
    //γιατι δεν εχει usage(?)
    public int getUserId() {return userId;}

    public void setUserId(int userId) {this.userId = userId;}
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