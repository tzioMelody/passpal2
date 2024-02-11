package com.example.passpal2;

import static com.example.passpal2.AppsInfoDB.TABLE_APP_INFO;

import android.content.Intent;
import android.database.Cursor;
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

public class AppSelectionActivity extends AppCompatActivity implements RecyclerViewInterface {

    ArrayList<AppsObj> appsObjs = new ArrayList<>();
    int[] appImages = {R.drawable.app_icon1,R.drawable.app_icon2,R.drawable.app_icon3,R.drawable.app_icon4,
            R.drawable.app_icon5,R.drawable.app_icon6,R.drawable.app_icon7,R.drawable.app_icon8,R.drawable.app_icon9,
            R.drawable.app_icon10,R.drawable.app_icon11,R.drawable.app_icon12,R.drawable.app_icon13,R.drawable.app_icon14,
            R.drawable.app_icon15,R.drawable.app_icon16,R.drawable.app_icon17,R.drawable.app_icon18,R.drawable.app_icon19,R.drawable.app_icon20,
            R.drawable.app_icon21,R.drawable.app_icon22};



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

        public List<AppsObj.UserApp> getAllUserApps() {
            List<AppsObj.UserApp> userApps = new ArrayList<>();
            String selectQuery = "SELECT  * FROM " + TABLE_APP_INFO;

            DataBaseHelper db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery(selectQuery, null);

            if (cursor.moveToFirst()) {
                do {
                    AppsObj.UserApp userApp = new AppsObj.UserApp(cursor.getString(1), cursor.getString(2));
                    userApps.add(userApp);
                } while (cursor.moveToNext());
            }

            cursor.close();
            db.close();
            return userApps;
        }

        // Δημιουργία του ViewModel
        AppSelectionViewModel viewModel = new ViewModelProvider(this).get(AppSelectionViewModel.class);
        viewModel.getAppListLiveData().observe(this, new Observer<List<AppInfo>>() {
            @Override
            public void onChanged(List<AppInfo> appInfos) {
                mAdapter.setAppList(appInfos);
            }
        });
    }

    /**
     * @param position
     */
    @Override
    public void onItemClick(int position) {

    }
}
