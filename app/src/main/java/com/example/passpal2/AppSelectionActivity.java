package com.example.passpal2;

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

        viewModel = new ViewModelProvider(this).get(AppSelectionViewModel.class);

        List<AppsObj.AppInfo> appInfoList = new ArrayList<>();
        appInfoList.addAll(AppsObj.COMMON_APPS);

        appInfoList.addAll(userAppsList.stream()
                .map(userApp -> new AppsObj.AppInfo(userApp.getAppName(), userApp.getAppLink(), R.drawable.default_app_icon))
                .collect(Collectors.toList()));

        recyclerView = findViewById(R.id.recyclerView);
        AppSelectionAdapter adapter = new AppSelectionAdapter(this, AppsObj.COMMON_APPS, userAppsList); // Αλλαγή εδώ
        recyclerView.setAdapter(adapter);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter.setOnItemClickListener(new AppSelectionAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                AppsObj.AppInfo clickedApp = appInfoList.get(position);

                if (selectedAppsList.contains(clickedApp)) {
                    // Αφαιρεί την εφαρμογή
                    selectedAppsList.remove(clickedApp);
                    updateCheckmarkVisibility(position, false);
                    viewModel.removeSelectedApp(clickedApp);
                } else {
                    if (selectedAppsList.size() < 10) {
                        // Προσθέτει την εφαρμογή στη λίστα
                        selectedAppsList.add(clickedApp);
                        updateCheckmarkVisibility(position, true);
                        viewModel.addSelectedApp(clickedApp);
                    } else {
                        Toast.makeText(AppSelectionActivity.this, "You've reached the limit!", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            private void updateCheckmarkVisibility(int position, boolean isVisible) {
                RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(position);
                if (viewHolder != null) {
                    ImageView checkMarkImageView = viewHolder.itemView.findViewById(R.id.checkMarkImageView);
                    checkMarkImageView.setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
                }
            }
        });

        Button selectionApp = findViewById(R.id.selectionApp);
        selectionApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedAppsList.size() == 0) {
                    Toast.makeText(AppSelectionActivity.this, "Please choose at least one app", Toast.LENGTH_SHORT).show();
                } else if (selectedAppsList.size() <= 10) {
                    Intent intent = new Intent(AppSelectionActivity.this, MainActivity.class);
                    intent.putExtra("selectedApps", new ArrayList<>(selectedAppsList));
                    intent.putExtra("userApps", new ArrayList<>(userAppsList)); // Προσθέστε τις εφαρμογές του χρήστη
                    startActivity(intent);
                } else {
                    Toast.makeText(AppSelectionActivity.this, "You can select up to 10 apps", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button AddUserApps = findViewById(R.id.AddUserApps);
        AddUserApps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AppSelectionActivity.this, AddAppUserActivity.class);
                startActivityForResult(intent, 1);
            }
        });
    }

    // Αυτή η μέθοδος καλείται όταν ολοκληρώνετε τη δημιουργία μιας εφαρμογής από τον χρήστη
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            String appName = data.getStringExtra("appName");
            String appLink = data.getStringExtra("appLink");

            // Προσθέστε τις εφαρμογές του χρήστη στη λίστα
            userAppsList.add(new AppsObj.UserApp(appName, appLink));
            // Ανανεώστε τον αντίστοιχο RecyclerView για να εμφανιστούν οι νέες εφαρμογές
            refreshUserAppsRecyclerView();
        }
    }

    // Ανανέωση του RecyclerView που εμφανίζει τις εφαρμογές του χρήστη
    private void refreshUserAppsRecyclerView() {
        AppSelectionAdapter adapter = (AppSelectionAdapter) recyclerView.getAdapter();
        if (adapter != null) {
            adapter.updateUserApps(userAppsList);
        }
    }
}
