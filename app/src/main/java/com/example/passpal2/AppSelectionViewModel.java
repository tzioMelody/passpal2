package com.example.passpal2;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class AppSelectionViewModel extends ViewModel {
    private MutableLiveData<List<AppsObj.AppInfo>> selectedApps = new MutableLiveData<>(new ArrayList<>());

    public LiveData<List<AppsObj.AppInfo>> getSelectedAppsLiveData() {
        return selectedApps;
    }

    public void toggleAppSelection(AppsObj.AppInfo app) {
        List<AppsObj.AppInfo> currentSelectedApps = selectedApps.getValue();
        if (currentSelectedApps.contains(app)) {
            currentSelectedApps.remove(app);
        } else {
            currentSelectedApps.add(app);
        }
        selectedApps.setValue(new ArrayList<>(currentSelectedApps));
    }
}
