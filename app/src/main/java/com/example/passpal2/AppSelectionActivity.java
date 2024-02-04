package com.example.passpal2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AppSelectionActivity extends AppCompatActivity {

    private AppSelectionViewModel viewModel;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_selection);

        viewModel = new ViewModelProvider(this).get(AppSelectionViewModel.class);

        recyclerView = findViewById(R.id.recyclerView);
        AppSelectionAdapter adapter = new AppSelectionAdapter(this, AppsObj.COMMON_APPS, viewModel);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        viewModel.getSelectedAppsLiveData().observe(this, selectedApps -> {
            adapter.updateAppList(selectedApps);
        });

        Button selectionApp = findViewById(R.id.selectionApp);
        selectionApp.setOnClickListener(v -> processSelection());

        Button addUserApps = findViewById(R.id.AddUserApps);
        addUserApps.setOnClickListener(v -> {
            Intent intent = new Intent(AppSelectionActivity.this, AddAppUserActivity.class);
            startActivity(intent);
        });
    }

    private void processSelection() {
        ArrayList<AppsObj.AppInfo> selectedApps = new ArrayList<>(viewModel.getSelectedAppsLiveData().getValue());
        if (selectedApps.isEmpty()) {
            Toast.makeText(this, "Please select at least one app.", Toast.LENGTH_SHORT).show();
        } else {
            // Για να περαστουν οι επιλεγμενες εφαρμογες στην Main
            Intent intent = new Intent(AppSelectionActivity.this, MainActivity.class);
            intent.putParcelableArrayListExtra("selectedApps", selectedApps);
            startActivity(intent);
        }
    }
}
