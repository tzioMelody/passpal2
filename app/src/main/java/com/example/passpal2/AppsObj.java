package com.example.passpal2;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class AppsObj implements Parcelable {
    private int id;
    private String AppNames;
    private String AppLinks;
    private int AppImages;
    private boolean isSelected;
    // Νέα πεδία
    private String username;
    private String email;
    private String password;


    public AppsObj(String appNames, String appLinks, int appImages) {
        AppNames = appNames;
        AppLinks = appLinks;
        AppImages = appImages;
        isSelected = false;
    }
    public AppsObj(String appNames, String appLinks, int appImages, String username, String email, String password) {
        this.AppNames = appNames;
        this.AppLinks = appLinks;
        this.AppImages = appImages;
        this.isSelected = false;
        this.username = username;
        this.email = email;
        this.password = password;
    }
    protected AppsObj(Parcel in) {
        id = in.readInt();
        AppNames = in.readString();
        AppLinks = in.readString();
        AppImages = in.readInt();
        isSelected = in.readByte() != 0;
        username = in.readString();
        email = in.readString();
        password = in.readString();
    }

    public static final Creator<AppsObj> CREATOR = new Creator<AppsObj>() {
        @Override
        public AppsObj createFromParcel(Parcel in) {
            return new AppsObj(in);
        }

        @Override
        public AppsObj[] newArray(int size) {
            return new AppsObj[size];
        }
    };

    public int getId() {
        return id;
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

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // Getters και Setters για τα νέα πεδία
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(AppNames);
        parcel.writeString(AppLinks);
        parcel.writeInt(AppImages);
        parcel.writeByte((byte) (isSelected ? 1 : 0));
        parcel.writeString(username);
        parcel.writeString(email);
        parcel.writeString(password);
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
