package com.example.passpal2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.passpal2.AppsObj.AppInfo;
import com.example.passpal2.databinding.ActivityAppSelectionBinding;

import java.util.ArrayList;
import java.util.List;

public class AppSelectionActivity extends AppCompatActivity {

    private AppSelectionAdapter mAdapter;
    private List<AppInfo> mSelectedApps = new ArrayList<>();
    private ActivityAppSelectionBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityAppSelectionBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        RecyclerView recyclerView = mBinding.recyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new AppSelectionAdapter(this, new ArrayList<>(), mSelectedApps, new AppSelectionAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(AppInfo appInfo) {
                // Προσθήκη ή αφαίρεση της εφαρμογής από τη λίστα επιλεγμένων εφαρμογών
                if (mSelectedApps.contains(appInfo)) {
                    mSelectedApps.remove(appInfo);
                } else {
                    if (mSelectedApps.size() < 10) {
                        mSelectedApps.add(appInfo);
                    } else {
                        // Εμφάνιση μηνύματος ειδοποίησης αν έχουν επιλεγεί ήδη 10 εφαρμογές
                        Toast.makeText(AppSelectionActivity.this, "Μπορείτε να επιλέξετε μόνο μέχρι 10 εφαρμογές", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        recyclerView.setAdapter(mAdapter);

        Button addUserAppsButton = mBinding.AddUserApps;
        addUserAppsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Πήγαινε στο AddAppUserActivity για προσθήκη νέας εφαρμογής
                Intent intent = new Intent(AppSelectionActivity.this, AddAppUserActivity.class);
                startActivity(intent);
            }
        });

        Button selectionAppButton = mBinding.selectionApp;
        selectionAppButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ελέγχουμε αν η λίστα με τις επιλεγμένες εφαρμογές είναι άδεια
                if (mSelectedApps.isEmpty()) {
                    // Εμφανίζουμε μήνυμα προειδοποίησης
                    Toast.makeText(AppSelectionActivity.this, "Παρακαλώ επιλέξτε τουλάχιστον μία εφαρμογή", Toast.LENGTH_SHORT).show();
                } else {
                    // Επιστροφή στο MainActivity με τις επιλεγμένες εφαρμογές
                    Intent intent = new Intent();
                    intent.putParcelableArrayListExtra("selected_apps", new ArrayList<>(mSelectedApps));
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });

        // Δημιουργία του ViewModel
        AppSelectionViewModel viewModel = new ViewModelProvider(this).get(AppSelectionViewModel.class);
        viewModel.getAppListLiveData().observe(this, new Observer<List<AppInfo>>() {
            @Override
            public void onChanged(List<AppInfo> appInfos) {
                mAdapter.setAppList(appInfos);
            }
        });
    }
}
