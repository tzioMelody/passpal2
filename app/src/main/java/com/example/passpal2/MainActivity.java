package com.example.passpal2;

import android.app.Dialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import com.example.passpal2.MainAppsAdapter;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class MainActivity extends AppCompatActivity implements RecyclerViewInterface {
    String username;
    private RecyclerView appsRecyclerView;
    DataBaseHelper dbHelper = new DataBaseHelper(this);
    private MainAppsAdapter mainAppsAdapter;
    private List<AppsObj> selectedApps = new ArrayList<>();
    private LinearLayoutManager layoutManager;
    private Context context;
    RelativeLayout Main_layout;
    private List<AppsObj> apps = new ArrayList<>();
    private static final int EDIT_APP_REQUEST = 2;
    int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ανάκτηση του username από το Intent ή SharedPreferences
        Intent intent = getIntent();
        username = intent.getStringExtra("username");
        if (username == null || username.isEmpty()) {
            // Αν το Intent δεν περιέχει το username, δοκιμάζουμε να το ανακτήσουμε από SharedPreferences
            SharedPreferences preferences = getSharedPreferences("user_credentials", MODE_PRIVATE);
            username = preferences.getString("username", "");
        }
        getSupportActionBar().setTitle("Welcome, " + username + "!");

        // Ανάκτηση του userId χρησιμοποιώντας το username
         userId = dbHelper.getUserIdByUsername(username);

        Main_layout = findViewById(R.id.Main_layout);

        // AsyncTask για την ανάκτηση και εμφάνιση των εφαρμογών
        new FetchAppsTask().execute(userId);

        FloatingActionButton appsBtn = findViewById(R.id.appsBtn);
        appsBtn.setOnClickListener(view -> {
            Intent intentUserID = new Intent(MainActivity.this, AppSelectionActivity.class);
            intentUserID.putExtra("USER_ID", userId);
            startActivityForResult(intentUserID, 1);
        });

        appsRecyclerView = findViewById(R.id.appsRecyclerView);
        layoutManager = new LinearLayoutManager(this);
        appsRecyclerView.setLayoutManager(layoutManager);

        // Αρχικοποίηση του MainAppsAdapter
        mainAppsAdapter = new MainAppsAdapter(this, selectedApps);

        // Set adapter to RecyclerView
        appsRecyclerView.setAdapter(mainAppsAdapter);
        appsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

    }


    // Called when returning from AppSelectionActivity with selected apps
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            ArrayList<Parcelable> parcelables = data.getParcelableArrayListExtra("selected_apps");
            if (parcelables != null) {
                List<AppsObj> apps = new ArrayList<>();
                for (Parcelable parcelable : parcelables) {
                    if (parcelable instanceof AppsObj) {
                        apps.add((AppsObj) parcelable);
                    }
                }
                selectedApps.clear();
                selectedApps.addAll(apps);
                mainAppsAdapter.notifyDataSetChanged();
            }
        }
    }

    // RecyclerViewInterface method implementation
    public void onItemClick(int position) {
        AppsObj selectedApp = selectedApps.get(position);
        //να πηγαινει στην αντισοτιχη ιστοσελιδα ή λινκ
        Toast.makeText(MainActivity.this, "Clicked on app: " + selectedApp.getAppNames(), Toast.LENGTH_SHORT).show();
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

                case R.id.menu_item3:
                    new  AlertDialog.Builder(this)
                        .setTitle("About")
                        .setMessage("Ασφάλεια και οργάνωση στην παλάμη σας - αυτό είναι το όραμα του PassPal, της κορυφαίας εφαρμογής διαχείρισης στοιχείων πρόσβασης και εφαρμογών." +
                                " Με την έκδοση 1.0, το PassPal προσφέρει έναν άρτιο συνδυασμό απλότητας και καινοτομίας, επιτρέποντας σας να εγγραφείτε, να αποθηκεύσετε και να διαχειριστείτε ασφαλώς " +
                                "τις πληροφορίες πρόσβασης σε αγαπημένες σας εφαρμογές και ιστοσελίδες. Χάρη στην κρυπτογράφηση κορυφαίας τεχνολογίας, τα δεδομένα σας είναι προστατευμένα," +
                                " ενώ η ενσωματωμένη διασύνδεση με το Hunter API εξασφαλίζει ότι οι διευθύνσεις email που καταχωρίζετε είναι πάντα έγκυρες. " +
                                "Κατεβάστε το PassPal και βελτιώστε σήμερα τη διαχείριση των ψηφιακών σας προφίλ!")
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                            // Αν ο χρήστης επιλέξει να συνεχίσει, καλούμε την super.onBackPressed()
                            super.onBackPressed();
                        })
                        .show();
                    return true;


                case R.id.menu_item4:
                    new  AlertDialog.Builder(this)
                            .setTitle("Help")
                            .setMessage("Αγαπητέ χρήστη,\n" +
                                    "\n" +
                                    "Σας ευχαριστούμε που επιλέξατε την εφαρμογή PassPal! Αυτή η εφαρμογή σχεδιάστηκε για να κάνει τη διαχείριση των κωδικών πρόσβασης σας απλή και ασφαλή. Ακολουθούν οι βασικές λειτουργίες και πώς να τις χρησιμοποιήσετε:\n" +
                                    "\n" +
                                    "1.Εγγραφή/Σύνδεση: Ξεκινήστε δημιουργώντας έναν λογαριασμό χρήστη. Εάν έχετε ήδη λογαριασμό, συνδεθείτε με το όνομα χρήστη και τον κωδικό που έχετε ορίσει.\n" +
                                    "\n" +
                                    "2.Προσθήκη Εφαρμογών: Μόλις συνδεθείτε, μπορείτε να προσθέσετε εφαρμογές και ιστοσελίδες στη λίστα σας, καθώς και τους σχετικούς κωδικούς πρόσβασης.\n" +
                                    "\n" +
                                    "3.Διαχείριση κωδικών: Αποθηκεύστε και διαχειριστείτε ασφαλώς τους κωδικούς πρόσβασης, με τη δυνατότητα να τους επεξεργαστείτε ή να δημιουργήσετε νέους, μπορείτε ακόμα να επιτρέψετ και σε εμάς να σας προτείνουμε νέους κωδικούς.\n" +
                                    "\n" +
                                    "4.Ασφάλεια: Οι κωδικοί πρόσβασης σας είναι προστατευμένοι με σύγχρονες τεχνικές κρυπτογράφησης για να εξασφαλίζεται η ασφάλεια των δεδομένων σας.\n" +
                                    "\n" +
                                    "5.Πρόσβαση από παντού: Με την εφαρμογή PassPal, έχετε πρόσβαση στους κωδικούς σας από οποιαδήποτε συσκευή, ανά πάσα στιγμή.\n" +
                                    "\n" +
                                    "Αν χρειάζεστε περισσότερη βοήθεια ή έχετε απορίες, μη διστάσετε να μας επικοινωνήσετε μέσω της ενότητας επικοινωνίας στην εφαρμογή.\n" +
                                    "\n" +
                                    "Ειλικρινά,\n" +
                                    "η ομάδα PassPal")
                            .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                                // Αν ο χρήστης επιλέξει να συνεχίσει, καλούμε την super.onBackPressed()
                                super.onBackPressed();
                            })
                            .show();
                    return true;
                    /*
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
                //Will require a master password so he can go to the login and passwords activity with all apps their usernames and their passwords
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


        LinearLayout bottomSheetLayout = dialog.findViewById(R.id.bottom_sheet);

        if (bottomSheetLayout != null) {
            BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout);
            // Το κανει ορατο το bottomsheet
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);


            bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(@NonNull View bottomSheet, int newState) {

                    dialog.dismiss();

                }

                @Override
                public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                    // Additional functionality during bottom sheet slide
                }
            });

        }

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

    }

    //Log out from popup menu LOGOUT FROM APP
    private void performLogout() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
    }



    private class FetchAppsTask extends AsyncTask<Integer, Void, List<AppsObj>> {
        @Override
        protected List<AppsObj> doInBackground(Integer... userIds) {
            List<AppsObj> apps = dbHelper.getAllSelectedApps(userIds[0]);
            Log.d("FetchAppsTask", "Επιστρεφόμενες εφαρμογές: " + apps.size());
            Log.d("FetchAppsTask", "Ποιες ειναι οι εφαρμογες :  " + apps);

            return apps;
        }

        @Override
        protected void onPostExecute(List<AppsObj> apps) {
            super.onPostExecute(apps);
            // Ενημέρωση του RecyclerView νέα λίστα εφαρμογών
            mainAppsAdapter.setSelectedApps(apps);
            attachSwipeToDeleteAndEditHelper();

            Log.d("FetchAppsTask", "Ενημέρωση adapter με " + apps.size() + " εφαρμογές.");
            for (AppsObj app : apps) {
                Log.d("FetchApps", "App: " + app.getAppNames() );
            }
        }


    }
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                            float dX, float dY, int actionState, boolean isCurrentlyActive) {
        new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

                //Adding color background and icon for deleteSwipe
                .addSwipeLeftBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.red))
                .addSwipeLeftActionIcon(R.drawable.deleteappitem)

                //Adding color background and icon for editSwipe
                .addSwipeRightBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.appGreen))
                .addSwipeRightActionIcon(R.drawable.editappitem)
                .create()
                .decorate();
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }
    //NEW SWIPE TZIO
       private void attachSwipeToDeleteAndEditHelper() {
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                if (direction == ItemTouchHelper.LEFT) {
                    // Διαγραφή της εφαρμογής
                    AppsObj app = mainAppsAdapter.getAppsList().get(position);
                    // Αποθήκευση της εφαρμογής τοπικα για το undo
                    AppsObj deletedApp = app;
                    int deletedIndex = position;

                    dbHelper.deleteApp(app.getAppNames(), dbHelper.getUserIdByUsername(username));
                    mainAppsAdapter.getAppsList().remove(position);
                    mainAppsAdapter.notifyItemRemoved(position);

                    // Εμφανίζει το Snackbar με την επιλογή Undo
                    Snackbar.make(appsRecyclerView, "App deleted", Snackbar.LENGTH_LONG)
                            .setAction("Undo", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    // Επαναφορά της διαγραφείσας εφαρμογής
                                    mainAppsAdapter.getAppsList().add(deletedIndex, deletedApp);
                                    mainAppsAdapter.notifyItemInserted(deletedIndex);
                                }
                            }).show();
                } else if (direction == ItemTouchHelper.RIGHT) {
                        // Επεξεργασία της εφαρμογής
                        AppsObj app = mainAppsAdapter.getAppsList().get(position);
                        // παιρνει τα δεδομενα της εφαρμογης και τα φορτωνει στην editapp
                        Intent intent = new Intent(MainActivity.this, EditSelectedAppActivity.class);
                        intent.putExtra("APP_DATA", app);
                        intent.putExtra("APP_ID", app.getId());
                        intent.putExtra("USER_ID",userId);
                        intent.putExtra("POSITION", position);
                        startActivityForResult(intent, EDIT_APP_REQUEST);

                }


                }
    };
        new ItemTouchHelper(simpleItemTouchCallback).attachToRecyclerView(appsRecyclerView);

    }
}

