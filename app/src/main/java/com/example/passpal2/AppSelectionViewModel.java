package com.example.passpal2;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class AppSelectionViewModel extends ViewModel {
    private MutableLiveData<List<AppsObj.AppInfo>> selectedAppsLiveData = new MutableLiveData<>(new ArrayList<>());
    private static final int MAX_SELECTED_APPS = 10;

    public LiveData<List<AppsObj.AppInfo>> getSelectedAppsLiveData() {
        return selectedAppsLiveData;
    }

    public boolean addSelectedApp(AppsObj.AppInfo appInfo) {
        List<AppsObj.AppInfo> selectedApps = new ArrayList<>(selectedAppsLiveData.getValue());

        if (selectedApps.size() < MAX_SELECTED_APPS && !selectedApps.contains(appInfo)) {
            selectedApps.add(appInfo);
            selectedAppsLiveData.setValue(selectedApps); // Ενημερώστε με τη νέα λίστα
            return true;
        }

        return false;
    }

    public void removeSelectedApp(AppsObj.AppInfo appInfo) {
        List<AppsObj.AppInfo> selectedApps = new ArrayList<>(selectedAppsLiveData.getValue());

        if (selectedApps.remove(appInfo)) { // Επιστρέφει true αν η λίστα άλλαξε ως αποτέλεσμα αυτής της κλήσης
            selectedAppsLiveData.setValue(selectedApps); // Ενημερώστε με τη νέα λίστα
        }
    }

    public boolean hasReachedMaxSelectedApps() {
        return selectedAppsLiveData.getValue() != null && selectedAppsLiveData.getValue().size() >= MAX_SELECTED_APPS;
    }
}
