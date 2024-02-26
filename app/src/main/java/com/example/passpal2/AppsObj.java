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

    public AppsObj(String appNames, String appLinks, int appImages) {
        AppNames = appNames;
        AppLinks = appLinks;
        AppImages = appImages;
        isSelected = false;
    }

    protected AppsObj(Parcel in) {
        id = in.readInt();
        AppNames = in.readString();
        AppLinks = in.readString();
        AppImages = in.readInt();
        isSelected = in.readByte() != 0;
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "AppsObj{" +
                "id=" + id +
                ", AppNames='" + AppNames + '\'' +
                '}';
    }

    /**
     * @return
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * @param parcel
     * @param i
     */
    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {

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
