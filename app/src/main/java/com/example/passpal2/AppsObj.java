package com.example.passpal2;


import java.util.ArrayList;
import java.util.List;

public class AppsObj {
    public static final List<AppInfo> COMMON_APPS = new ArrayList<>();
    public static final List<UserApp> USER_APPS = new ArrayList<>();

    static {
        appList();
    }

    private static void appList() {

        COMMON_APPS.add(new AppInfo("Facebook", "https://www.facebook.com",R.drawable.app_icon1));
        COMMON_APPS.add(new AppInfo("Instagram", "https://www.instagram.com",R.drawable.app_icon2));
        COMMON_APPS.add(new AppInfo("Twitter", "https://www.twitter.com",R.drawable.app_icon3));
        COMMON_APPS.add(new AppInfo("Netflix", "https://www.netflix.com",R.drawable.app_icon4));
        COMMON_APPS.add(new AppInfo("Discord", "https://www.discord.com",R.drawable.app_icon5));
        COMMON_APPS.add(new AppInfo("Pinterest", "https://www.pinterest.com",R.drawable.app_icon6));
        COMMON_APPS.add(new AppInfo("GooglePay", "https://www.googlepay.com",R.drawable.app_icon7));
        COMMON_APPS.add(new AppInfo("YouTube", "https://www.youtube.com",R.drawable.app_icon8));
        COMMON_APPS.add(new AppInfo("WhatsApp", "https://www.whatsapp.com",R.drawable.app_icon9));
        COMMON_APPS.add(new AppInfo("Telegram", "https://www.telegram.com",R.drawable.app_icon10));

        COMMON_APPS.add(new AppInfo("Paypal", "https://www.paypal.com",R.drawable.app_icon11));
        COMMON_APPS.add(new AppInfo("Shazam", "https://www.shazam.com",R.drawable.app_icon12));
        COMMON_APPS.add(new AppInfo("Spotify", "https://www.spotify.com",R.drawable.app_icon13));
        COMMON_APPS.add(new AppInfo("Viber", "https://www.viber.com",R.drawable.app_icon14));
        COMMON_APPS.add(new AppInfo("Snapchat", "https://www.snapchat.com",R.drawable.app_icon15));
        COMMON_APPS.add(new AppInfo("BeReal", "https://www.bereal.com",R.drawable.app_icon16));
        COMMON_APPS.add(new AppInfo("TikTok", "https://www.tiktok.com",R.drawable.app_icon17));
        COMMON_APPS.add(new AppInfo("Amazon", "https://www.amazon.com",R.drawable.app_icon18));
        COMMON_APPS.add(new AppInfo("Gmail", "https://www.gmail.com",R.drawable.app_icon19));
        COMMON_APPS.add(new AppInfo("Shein", "https://www.shein.com",R.drawable.app_icon20));

        COMMON_APPS.add(new AppInfo("skroutz", "https://www.skroutz.com",R.drawable.app_icon21));
        COMMON_APPS.add(new AppInfo("Disney+", "https://www.disneyplus.com/",R.drawable.app_icon22));
    }

    public static class AppInfo  {
        private final String appName;
        private final String appLink;
        private final int appIconId; // Πεδίο για το ID της εικόνας

        public AppInfo(String appName, String appLink, int appIconId) {
            this.appName = appName;
            this.appLink = appLink;
            this.appIconId = appIconId; // Αποθήκευση το ID της εικόνας
        }

        public String getAppName() {
            return appName;
        }

        public String getAppLink() {
            return appLink;
        }

        public int getAppIconId() {
            return appIconId;
        }
    }


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

    public static void addUserApp(String appName, String appLink) {
        USER_APPS.add(new UserApp(appName, appLink));
    }
}
