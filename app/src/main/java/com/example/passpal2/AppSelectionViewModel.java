package com.example.passpal2;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class AppSelectionViewModel extends ViewModel {
    private MutableLiveData<List<AppsObj>> selectedAppsLiveData;

    public LiveData<List<AppsObj>> getSelectedAppsLiveData() {
        if (selectedAppsLiveData == null) {
            selectedAppsLiveData = new MutableLiveData<>();
            selectedAppsLiveData.setValue(new ArrayList<>());
        }
        return selectedAppsLiveData;
    }

    public void addSelectedApp(AppsObj app) {
        List<AppsObj> apps = selectedAppsLiveData.getValue();
        if (apps != null) {
            apps.add(app);
            selectedAppsLiveData.setValue(apps);
        }
    }

    public void removeSelectedApp(AppsObj app) {
        List<AppsObj> apps = selectedAppsLiveData.getValue();
        if (apps != null) {
            apps.remove(app);
            selectedAppsLiveData.setValue(apps);
        }
    }
}

