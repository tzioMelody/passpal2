package com.example.passpal2;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class AppSelectionViewModel extends ViewModel {
    private MutableLiveData<List<AppsObj.AppInfo>> selectedAppsLiveData = new MutableLiveData<>();
    private static final int MAX_SELECTED_APPS = 10;

    public LiveData<List<AppsObj.AppInfo>> getSelectedAppsLiveData() {
        return selectedAppsLiveData;
    }

    public boolean addSelectedApp(AppsObj.AppInfo appInfo) {
        List<AppsObj.AppInfo> selectedApps = selectedAppsLiveData.getValue();
        if (selectedApps == null) {
            selectedApps = new ArrayList<>();
        }

        if (selectedApps.size() < MAX_SELECTED_APPS && !selectedApps.contains(appInfo)) {
            selectedApps.add(appInfo);
            selectedAppsLiveData.setValue(selectedApps);
            return true;
        }

        return false;
    }

    public void removeSelectedApp(AppsObj.AppInfo appInfo) {
        List<AppsObj.AppInfo> selectedApps = selectedAppsLiveData.getValue();
        if (selectedApps != null) {
            selectedApps.remove(appInfo);
            selectedAppsLiveData.setValue(selectedApps);
        }
    }

    public boolean hasReachedMaxSelectedApps() {
        List<AppsObj.AppInfo> selectedApps = selectedAppsLiveData.getValue();
        return selectedApps != null && selectedApps.size() >= MAX_SELECTED_APPS;
    }
}
