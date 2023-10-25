package com.example.passpal2;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.PopupMenu;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // FloatingActionButton με βάση το ID του
        FloatingActionButton appsBtn = findViewById(R.id.appsBtn);

        // OnClickListener για τον FloatingActionButton
        appsBtn.setOnClickListener(view -> {
            // Μετάβαση στην σελίδα με λίστα εφαρμογών
            Intent intent = new Intent(MainActivity.this, AppSelectionActivity.class);
            startActivity(intent);
        });

        // Εμφανίστε τις επιλεγμένες εφαρμογές από το Intent στο RecyclerView
        RecyclerView appsRecyclerView = findViewById(R.id.appsRecyclerView);
        ArrayList<Parcelable> parcelableList = getIntent().getParcelableArrayListExtra("selectedApps");
        List<AppsObj.AppInfo> selectedApps = new ArrayList<>();

        if (parcelableList != null) {
            for (Parcelable parcelable : parcelableList) {
                if (parcelable instanceof AppsObj.AppInfo) {
                    selectedApps.add((AppsObj.AppInfo) parcelable);
                }
            }

            // Αντάπτορας με τις επιλεγμένες εφαρμογές
            AppSelectionAdapter adapter = new AppSelectionAdapter(this, selectedApps, selectedApps);
            appsRecyclerView.setAdapter(adapter);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            // Εδώ μπορείτε να εμφανίσετε το Popup Menu
            showPopupMenu();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showPopupMenu() {
        PopupMenu popupMenu = new PopupMenu(this, findViewById(R.id.action_settings));
        popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // Εδώ ορίζετε τη συμπεριφορά για τα αντικείμενα του Popup Menu
                switch (item.getItemId()) {
                    case R.id.menu_item1:
                        // Εκτελέστε την επιλεγμένη ενέργεια για το αντικείμενο 1
                        return true;
                    case R.id.menu_item2:
                        // Εκτελέστε την επιλεγμένη ενέργεια για το αντικείμενο 2
                        return true;
                    case R.id.menu_item3:
                        // Εκτελέστε την επιλεγμένη ενέργεια για το αντικείμενο 3
                        return true;
                    default:
                        return false;
                }
            }
        });

        popupMenu.show();
    }
}
