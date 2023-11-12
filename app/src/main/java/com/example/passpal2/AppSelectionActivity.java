package com.example.passpal2;

import static android.app.ProgressDialog.show;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AppSelectionActivity extends AppCompatActivity {

    private AppSelectionViewModel viewModel;
    private RecyclerView recyclerView;
    private List<AppsObj.AppInfo> selectedAppsList = new ArrayList<>();
    private List<AppsObj.UserApp> userAppsList = new ArrayList<>(); // Λίστα για τις εφαρμογές του χρήστη

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_selection);
        getSupportActionBar().setTitle("App selection");

        recyclerView = findViewById(R.id.recyclerView);

        viewModel = new ViewModelProvider(this).get(AppSelectionViewModel.class);

        List<AppsObj.AppInfo> appInfoList = new ArrayList<>();
        appInfoList.addAll(AppsObj.COMMON_APPS);

        for (AppsObj.AppInfo appInfo : appInfoList) {
            if (selectedAppsList.contains(appInfo)) {
                updateCheckmarkVisibility(appInfoList.indexOf(appInfo), true);
            }
        }

        AppSelectionAdapter adapter = new AppSelectionAdapter(this, appInfoList, AppsObj.COMMON_APPS);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter.setOnItemClickListener(new AppSelectionAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                AppsObj.AppInfo clickedApp = appInfoList.get(position);

                if (selectedAppsList.contains(clickedApp)) {
                    selectedAppsList.remove(clickedApp);
                    updateCheckmarkVisibility(position, false);
                    viewModel.removeSelectedApp(clickedApp);
                    Toast.makeText(AppSelectionActivity.this, "Error1", Toast.LENGTH_SHORT).show();
                } else {
                    if (selectedAppsList.size() < 10) {
                        selectedAppsList.add(clickedApp);
                        updateCheckmarkVisibility(position, true);
                        viewModel.addSelectedApp(clickedApp);
                        Toast.makeText(AppSelectionActivity.this, "Error2", Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(AppSelectionActivity.this, "You've reached the limit!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }

    private void updateCheckmarkVisibility(int position, boolean isVisible) {
        RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(position);
        if (viewHolder != null) {
            ImageView checkMarkImageView = viewHolder.itemView.findViewById(R.id.checkMarkImageView);
            checkMarkImageView.setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            String appName = data.getStringExtra("appName");
            String appLink = data.getStringExtra("appLink");

            userAppsList.add(new AppsObj.UserApp(appName, appLink));
            refreshUserAppsRecyclerView();
        }
    }

    private void refreshUserAppsRecyclerView() {
        AppSelectionAdapter adapter = (AppSelectionAdapter) recyclerView.getAdapter();
        if (adapter != null) {
            adapter.updateUserApps(userAppsList);
        }
    }
}
