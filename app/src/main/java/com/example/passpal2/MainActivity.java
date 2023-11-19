package com.example.passpal2;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Toast;
import com.example.passpal2.Data.Entities.User;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class MainActivity extends AppCompatActivity {

    private AppSelectionAdapter adapter;
    private List<AppsObj.AppInfo> selectedApps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setTitle("Pass Pal");


        // Εδώ προσθέτουμε τον έλεγχο σύνδεσης χρήστη
        if (!isLoggedIn()) {
            // Αν δεν είναι συνδεδεμένος ο χρήστης, μεταφέρετε τον στην οθόνη σύνδεσης
            Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(loginIntent);
            finish(); // Τερματίζουμε την τρέχουσα δραστηριότητα για να μην επιτρέπεται η επιστροφή πίσω.
            return;
        }

        FloatingActionButton appsBtn = findViewById(R.id.appsBtn);
        appsBtn.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, AppSelectionActivity.class);
            startActivity(intent);
        });

        RecyclerView appsRecyclerView = findViewById(R.id.appsRecyclerView);
        selectedApps = new ArrayList<>();
        adapter = new AppSelectionAdapter(this, selectedApps, selectedApps);
        appsRecyclerView.setAdapter(adapter);


        //Swipe items for Edit and Delete
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeToDeleteCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT));
        itemTouchHelper.attachToRecyclerView(appsRecyclerView);


        //Show selected apps from AppSelectionActivity
        ArrayList<Parcelable> parcelableList = getIntent().getParcelableArrayListExtra("selectedApps");
        if (parcelableList != null) {
            for (Parcelable parcelable : parcelableList) {
                if (parcelable instanceof AppsObj.AppInfo) {
                    selectedApps.add((AppsObj.AppInfo) parcelable);
                }
            }
            adapter.notifyDataSetChanged();
        }

// Εδώ προσθέτουμε τον κώδικα για την αποθήκευση των επιλεγμένων εφαρμογών στη βάση δεδομένων
        saveSelectedAppsToDatabase(selectedApps);
    }

    // Προσθέτουμε την εξής μέθοδο για τον έλεγχο σύνδεσης χρήστη
    private boolean isLoggedIn() {
        // Εδώ μπορείτε να επικοινωνήσετε με τη βάση δεδομένων ή να χρησιμοποιήσετε άλλον τρόπο
        // για να ελέγξετε αν ο χρήστης είναι συνδεδεμένος. Στο παράδειγμα, ελέγχουμε την ύπαρξη του email στη βάση χρηστών.
        // Επιστρέφουμε true αν ο χρήστης είναι συνδεδεμένος, αλλιώς false.
        UserDB userDB = new UserDB(this);
        User loggedInUser = userDB.getUserByEmail("email@example.com");
        return loggedInUser != null;
    }

    // Προσθέτουμε την εξής μέθοδο για την αποθήκευση των επιλεγμένων εφαρμογών στη βάση δεδομένων
    private void saveSelectedAppsToDatabase(List<AppsObj.AppInfo> selectedApps) {
        // Εδώ πρέπει να αποθηκεύσετε τις επιλεγμένες εφαρμογές (selectedApps) στη βάση δεδομένων.
        // Χρησιμοποιήστε την κατάλληλη κλάση για την επικοινωνία με τη βάση δεδομένων (π.χ., AppsInfoDB).
        // Στο παράδειγμα, χρησιμοποιούμε τη μέθοδο addUserIfNotExists για να προσθέσουμε κάθε επιλεγμένη εφαρμογή ως χρήστη.
        UserDB userDB = new UserDB(this);
        for (AppsObj.AppInfo selectedApp : selectedApps) {
            // Υποθέτουμε ότι το email του χρήστη είναι η μοναδική αναγνωριστική πληροφορία για τον έλεγχο.
            // Αν χρησιμοποιείτε άλλη μοναδική πληροφορία, προσαρμόστε αντίστοιχα.
            userDB.addUserIfNotExists(new User(0, "", "email@example.com", "", "", ""));
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
            showPopupMenu();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showPopupMenu() {
        PopupMenu popupMenu = new PopupMenu(this, findViewById(R.id.action_settings));
        popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                //options(bottomsheet)
                case R.id.menu_item1:
                    showDialog();
                    return true;
                //LOG OUT
                case R.id.menu_item2:
                    performLogout();
                    return true;
                    /*
                case R.id.menu_item3:
                    // Handle menu item 3
                    return true;
                case R.id.menu_item4:
                    Intent helpIntent = new Intent(MainActivity.this, HelpActivity.class);
                    startActivity(helpIntent);
                    return true;
                case R.id.menu_item5:
                    performLogout();
                    return true;*/
                default:
                    return false;
            }
        });

        popupMenu.show();
    }


    //For bottomSheet popup
    private void showDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(getWindow().FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottomsheet_layout);

        LinearLayout EditLy = dialog.findViewById(R.id.EditLy);
        LinearLayout ShareLy = dialog.findViewById(R.id.ShareLy);
        LinearLayout UpdateLy = dialog.findViewById(R.id.UpdateLy);
        LinearLayout LoginPswLy = dialog.findViewById(R.id.LoginPswLy);
        LinearLayout SettingsLy = dialog.findViewById(R.id.SettingsLy);


        EditLy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //EditActivity for apps activated
                dialog.dismiss();

            }
        });
        ShareLy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Log in app or website
                dialog.dismiss();

            }
        });
        UpdateLy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Update database
                Toast.makeText(MainActivity.this, "Updating...", Toast.LENGTH_SHORT).show();
                dialog.dismiss();

            }
        });
        LoginPswLy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Will recuire a master password so he can go to the login and passwords activity with all apps their usernames and their passwords
                dialog.dismiss();

            }
        });
        SettingsLy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Settings can have the changes to the color of the app the interior and the background.
                dialog.dismiss();

            }
        });

        // Υποθέτουμε ότι η μεταβλητή bottom_sheet αναφέρεται στο LinearLayout του Bottom Sheet
        LinearLayout bottomSheetLayout = dialog.findViewById(R.id.bottom_sheet);

        if (bottomSheetLayout != null) {
            BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout);
            // Κάνει το bottom sheet να είναι εμφανές
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

            // Καθορίζει τη συμπεριφορά κατά το κλείσιμο
            bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(@NonNull View bottomSheet, int newState) {
                    if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                        dialog.dismiss();
                    }
                }

                @Override
                public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                    // Επιπλέον λειτουργικότητα κατά τη συρτή του Bottom Sheet
                }
            });
        }

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

    }

    //Log out from popup menu LOGOUT FROM APP
    private void performLogout() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {

        SwipeToDeleteCallback(int dragDirs, int swipeDirs) {
            super(dragDirs, swipeDirs);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        //Undo if delete item and put it back at the same position
        //This is what is using for swipes
        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();
            if (!selectedApps.isEmpty() && position >= 0 && position < selectedApps.size()) {
                if (direction == ItemTouchHelper.LEFT) {
                    AppsObj.AppInfo deletedApp = selectedApps.get(position);
                    Snackbar.make(viewHolder.itemView, deletedApp.getAppName() + " deleted!", Snackbar.LENGTH_LONG)
                            .setAction("Undo", view -> {
                                selectedApps.add(position, deletedApp);
                                adapter.notifyItemInserted(position);
                            }).show();
                    deleteApp(position);
                } else if (direction == ItemTouchHelper.RIGHT) {
                    // BottomSheet
                    editApp(position);
                }
            }
        }

        @Override
        public void onChildDraw(@NonNull Canvas c,@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                                float dX, float dY, int actionState, boolean isCurrentlyActive){
            new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

                    //Adding color backgorund and icon for deleteSwipe
                    .addSwipeLeftBackgroundColor(ContextCompat.getColor(MainActivity.this,R.color.red))
                    .addSwipeLeftActionIcon(R.drawable.deleteappitem)

                    //Adding color background and icon for editSwipe
                    .addSwipeRightBackgroundColor(ContextCompat.getColor(MainActivity.this,R.color.appGreen))
                    .addSwipeRightActionIcon(R.drawable.editappitem)
                    .create()
                    .decorate();
            super.onChildDraw(c,recyclerView,viewHolder,dX,dY,actionState,isCurrentlyActive);
        }
    }


    //Works for the undo button so DO NOT delete
    private void deleteApp(int position) {
        selectedApps.remove(position);
        adapter.notifyItemRemoved(position);
    }

    //Edit app once swiped right
    private void editApp(int position) {
        if (position < selectedApps.size()) {
            AppsObj.AppInfo selectedApp = selectedApps.get(position);
            // Μεταφορά προς το EditSelectedAppActivity
            Intent editIntent = new Intent(MainActivity.this, EditSelectedAppActivity.class);
            editIntent.putExtra("selectedApp", selectedApp);
            startActivity(editIntent);
        }
    }
}


