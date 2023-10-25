package com.example.passpal2;

import static com.example.passpal2.AppsObj.USER_APPS;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_selection);

        viewModel = new ViewModelProvider(this).get(AppSelectionViewModel.class);

        List<AppsObj.AppInfo> appInfoList = new ArrayList<>();
        appInfoList.addAll(AppsObj.COMMON_APPS);

        appInfoList.addAll(USER_APPS.stream()
                .map(userApp -> new AppsObj.AppInfo(userApp.getAppName(), userApp.getAppLink(), R.drawable.default_app_icon))
                .collect(Collectors.toList()));

        recyclerView = findViewById(R.id.recyclerView);
        AppSelectionAdapter adapter = new AppSelectionAdapter(this, appInfoList, AppsObj.COMMON_APPS);
        recyclerView.setAdapter(adapter);
        List<AppsObj.AppInfo> commonAndUserApps = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        commonAndUserApps.addAll(AppsObj.COMMON_APPS);
/*        commonAndUserApps.addAll(USER_APPS);*/

        adapter.setOnItemClickListener(new AppSelectionAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                AppsObj.AppInfo clickedApp = appInfoList.get(position);

                if (selectedAppsList.contains(clickedApp)) {
                    // Αφαιρεί
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
                startActivity(intent);
            }
        });
    }
}/*
package com.example.passpal2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AppSelectionActivity extends AppCompatActivity {

    private AppSelectionViewModel viewModel;
    private RecyclerView recyclerView;
    private List<AppsObj.AppInfo> selectedAppsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_selection);

        viewModel = new ViewModelProvider(this).get(AppSelectionViewModel.class);

        List<AppsObj.AppInfo> appInfoList = new ArrayList<>();
        appInfoList.addAll(AppsObj.COMMON_APPS);

        appInfoList.addAll(AppsObj.USER_APPS.stream()
                .map(userApp -> new AppsObj.AppInfo(userApp.getAppName(), userApp.getAppLink(), R.drawable.default_app_icon))
                .collect(Collectors.toList()));

        // Δημιουργία της λίστας commonAndUserApps
        List<AppsObj.AppInfo> commonAndUserApps = new ArrayList<>();
        commonAndUserApps.addAll(AppsObj.COMMON_APPS);
        commonAndUserApps.addAll(appInfoList);

        recyclerView = findViewById(R.id.recyclerView);
        AppSelectionAdapter adapter = new AppSelectionAdapter(this, commonAndUserApps, commonAndUserApps);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new AppSelectionAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                AppsObj.AppInfo clickedApp = commonAndUserApps.get(position);

                if (selectedAppsList.contains(clickedApp)) {
                    // Αφαιρεί
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
        final int[] selectionAppClickCount = {0};

        selectionApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectionAppClickCount[0]++;

                if (selectionAppClickCount[0] >= 2) {
                    // Εάν το κουμπί πατηθεί δύο φορές, επιστρέψτε στο MainActivity
                    Intent intent = new Intent(AppSelectionActivity.this, MainActivity.class);
                    intent.putExtra("selectedApps", new ArrayList<>(selectedAppsList));
                    startActivity(intent);
                } else if (selectedAppsList.size() == 0) {
                    // Εάν δεν έχουν επιλεγεί εφαρμογές, εμφανίστε το μήνυμα "You didn't select any apps!"
                    Toast.makeText(AppSelectionActivity.this, "You didn't select any apps!", Toast.LENGTH_SHORT).show();
                } else if (selectedAppsList.size() <= 10) {
                    Intent intent = new Intent(AppSelectionActivity.this, MainActivity.class);
                    intent.putExtra("selectedApps", new ArrayList<>(selectedAppsList));
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
                startActivity(intent);
            }
        });
    }
}
*/
