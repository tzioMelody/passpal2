package com.example.passpal2;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
            if (selectedApps.size() > 10) {
                Toast.makeText(this, "You can select up to 10 apps.", Toast.LENGTH_SHORT).show();
                // Κάποια λογική για να επιστρέψει την τελευταία επιλογή ή να ενημερώσει τον χρήστη
            }
        });
    }
}
