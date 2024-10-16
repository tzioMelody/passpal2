package com.example.passpal2;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

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

        // Λήψη του user ID από το intent
        Intent intent = getIntent();
        userId = intent.getIntExtra("user_id", -1);

        // Επαλήθευση αν το user ID είναι έγκυρο
        if (userId == -1) {
            showToast("User ID is invalid");
            finish();
            return;
        }

        username = dbHelper.getUsernameByUserId(userId);
        getSupportActionBar().setTitle("Welcome, " + username + "!");

        Main_layout = findViewById(R.id.Main_layout);

        // AsyncTask για την ανάκτηση και εμφάνιση των εφαρμογών
        new FetchAppsTask().execute(userId);

        FloatingActionButton appsBtn = findViewById(R.id.appsBtn);
        appsBtn.setOnClickListener(view -> {
            Intent intentUserID = new Intent(MainActivity.this, AppSelectionActivity.class);
            intentUserID.putExtra("USER_ID", userId); // Αποστολή ως int
            startActivityForResult(intentUserID, 1);
        });

        appsRecyclerView = findViewById(R.id.appsRecyclerView);
        layoutManager = new LinearLayoutManager(this);
        appsRecyclerView.setLayoutManager(layoutManager);

        // Αρχικοποίηση του MainAppsAdapter
        mainAppsAdapter = new MainAppsAdapter(this, selectedApps);

        // Set adapter to RecyclerView
        appsRecyclerView.setAdapter(mainAppsAdapter);
    }



    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }



    private void showMasterPasswordActivity(int userId) {
        Intent intent = new Intent(this, SetMasterPasswordActivity.class);
        intent.putExtra("user_id", userId);
        startActivity(intent);
        finish();
    }


    // Called when returning from AppSelectionActivity with selected apps
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_APP_REQUEST && resultCode == RESULT_OK) {
            // Ανανέωση των δεδομένων
            new FetchAppsTask().execute(userId);
        }
    }


    // RecyclerViewInterface method implementation
    public void onItemClick(int position) {
        AppsObj selectedApp = selectedApps.get(position);
        // Να πηγαίνει στην αντίστοιχη ιστοσελίδα ή link
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
                // Options (bottomsheet)
                case R.id.menu_item1:
                    showDialog();
                    return true;
                // Log out
                case R.id.menu_item2:
                    performLogout();
                    return true;
                case R.id.menu_item3:
                    new AlertDialog.Builder(this)
                            .setTitle("About")
                            .setMessage("Ασφάλεια και οργάνωση στην παλάμη σας - αυτό είναι το όραμα του PassPal, της κορυφαίας εφαρμογής διαχείρισης στοιχείων πρόσβασης και εφαρμογών." +
                                    " Με την έκδοση 1.0, το PassPal προσφέρει έναν άρτιο συνδυασμό απλότητας και καινοτομίας, επιτρέποντας σας να εγγραφείτε, να αποθηκεύσετε και να διαχειριστείτε ασφαλώς " +
                                    "τις πληροφορίες πρόσβασης σε αγαπημένες σας εφαρμογές και ιστοσελίδες. Χάρη στην κρυπτογράφηση κορυφαίας τεχνολογίας, τα δεδομένα σας είναι προστατευμένα," +
                                    " ενώ η ενσωματωμένη διασύνδεση με το Hunter API εξασφαλίζει ότι οι διευθύνσεις email που καταχωρίζετε είναι πάντα έγκυρες. " +
                                    "Κατεβάστε το PassPal και βελτιώστε σήμερα τη διαχείριση των ψηφιακών σας προφίλ!")
                            .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                                // Αν ο χρήστης επιλέξει να συνεχίσει, καλούμε την super.onBackPressed()
                                dialog.dismiss();
                            })
                            .show();
                    return true;
                case R.id.menu_item4:
                    new AlertDialog.Builder(this)
                            .setTitle("Help")
                            .setMessage("Αγαπητέ χρήστη,\n" +
                                    "\n" +
                                    "Σας ευχαριστούμε που επιλέξατε την εφαρμογή PassPal! Αυτή η εφαρμογή σχεδιάστηκε για να κάνει τη διαχείριση των κωδικών πρόσβασης σας απλή και ασφαλή. Ακολουθούν οι βασικές λειτουργίες και πώς να τις χρησιμοποιήσετε:\n" +
                                    "\n" +
                                    "1. Εγγραφή/Σύνδεση: Ξεκινήστε δημιουργώντας έναν λογαριασμό χρήστη. Εάν έχετε ήδη λογαριασμό, συνδεθείτε με το όνομα χρήστη και τον κωδικό που έχετε ορίσει.\n" +
                                    "\n" +
                                    "2. Προσθήκη Εφαρμογών: Μόλις συνδεθείτε, μπορείτε να προσθέσετε εφαρμογές και ιστοσελίδες στη λίστα σας, καθώς και τους σχετικούς κωδικούς πρόσβασης.\n" +
                                    "\n" +
                                    "3. Διαχείριση κωδικών: Αποθηκεύστε και διαχειριστείτε ασφαλώς τους κωδικούς πρόσβασης, με τη δυνατότητα να τους επεξεργαστείτε ή να δημιουργήσετε νέους. Μπορείτε ακόμα να επιτρέψετε και σε εμάς να σας προτείνουμε νέους κωδικούς.\n" +
                                    "\n" +
                                    "4. Ασφάλεια: Οι κωδικοί πρόσβασης σας είναι προστατευμένοι με σύγχρονες τεχνικές κρυπτογράφησης για να εξασφαλίζεται η ασφάλεια των δεδομένων σας.\n" +
                                    "\n" +
                                    "5. Πρόσβαση από παντού: Με την εφαρμογή PassPal, έχετε πρόσβαση στους κωδικούς σας από οποιαδήποτε συσκευή, ανά πάσα στιγμή.\n" +
                                    "\n" +
                                    "Αν χρειάζεστε περισσότερη βοήθεια ή έχετε απορίες, μη διστάσετε να μας επικοινωνήσετε μέσω της ενότητας επικοινωνίας στην εφαρμογή.\n" +
                                    "\n" +
                                    "Ειλικρινά,\n" +
                                    "η ομάδα PassPal")
                            .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                                dialog.dismiss();
                            })
                            .show();
                    return true;
                case R.id.menu_item5:
                    int userId = DataBaseHelper.getUserId(this);
                    dbHelper.deleteUserData(userId);
                    Toast.makeText(this, "All user data deleted", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                    return true;

                default:
                    return false;
            }
        });

        popupMenu.show();
    }

    // For bottomSheet popup
    private void showDialog() {
        final BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(R.layout.bottomsheet_layout);

        Button ShareLy = dialog.findViewById(R.id.ShareLy);
        Button UpdateLy = dialog.findViewById(R.id.UpdateLy);
        Button LoginPswLy = dialog.findViewById(R.id.LoginPswLy);
        Button SettingsLy = dialog.findViewById(R.id.SettingsLy);

        if (ShareLy != null) {
            ShareLy.setOnClickListener(v -> dialog.dismiss());
        }

        if (UpdateLy != null) {
            UpdateLy.setOnClickListener(v -> {
                Toast.makeText(MainActivity.this, "Updating...", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
        }

        if (LoginPswLy != null) {
            LoginPswLy.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, EnterMasterPasswordActivity.class);
                intent.putExtra("user_id", userId);
                startActivity(intent);
                dialog.dismiss();
            });
        }

        if (SettingsLy != null) {
            SettingsLy.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }


    // Log out from popup menu LOGOUT FROM APP
    private void performLogout() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    private class FetchAppsTask extends AsyncTask<Integer, Void, List<AppsObj>> {
        @Override
        protected List<AppsObj> doInBackground(Integer... userIds) {
            List<AppsObj> apps = dbHelper.getAllSelectedApps(userIds[0]);
            Log.d("FetchAppsTask", "Επιστρεφόμενες εφαρμογές: " + apps.size());
            Log.d("FetchAppsTask", "Ποιες είναι οι εφαρμογές : " + apps);

            return apps;
        }

        @Override
        protected void onPostExecute(List<AppsObj> apps) {
            super.onPostExecute(apps);
            mainAppsAdapter.setSelectedApps(apps);
            attachSwipeToDeleteAndEditHelper();

            Log.d("FetchAppsTask", "Ενημέρωση adapter με " + apps.size() + " εφαρμογές.");
            for (AppsObj app : apps) {
                Log.d("FetchApps", "App: " + app.getAppNames());
            }
        }
    }


    // NEW SWIPE TZIO
    private void attachSwipeToDeleteAndEditHelper() {
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

                // Θέτουμε το όριο στο 30% του πλάτους της οθόνης για την επεξεργασία (δεξιά swipe)
                float limit = recyclerView.getWidth() * 0.3f;

                // Αν το swipe είναι προς τα δεξιά (επεξεργασία) και δεν έχει φτάσει το όριο
                if (dX > 0 && Math.abs(dX) < limit) {
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                } else if (dX > 0) {
                    // Περιορίζουμε το swipe μέχρι το όριο
                    dX = limit;
                }

                // Αν το swipe είναι προς τα αριστερά (διαγραφή), αφήνουμε να προχωρήσει κανονικά
                if (dX < 0) {
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                }

                // Εμφανίζουμε το background και τα εικονίδια
                View itemView = viewHolder.itemView;
                Paint p = new Paint();

                if (dX > 0) {
                    // Επεξεργασία - Πράσινο background
                    p.setColor(Color.parseColor("#388E3C"));
                    RectF background = new RectF(itemView.getLeft(), itemView.getTop(), dX, itemView.getBottom());
                    c.drawRect(background, p);
                } else if (dX < 0) {
                    // Διαγραφή - Κόκκινο background
                    p.setColor(Color.parseColor("#D32F2F"));
                    RectF background = new RectF(itemView.getRight() + dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                    c.drawRect(background, p);
                }

                Drawable icon;
                RectF iconDest;

                if (dX > 0) {
                    icon = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_edit); // Εικονίδιο επεξεργασίας
                    iconDest = new RectF(itemView.getLeft() + 50, itemView.getTop() + 50, itemView.getLeft() + 150, itemView.getBottom() - 50);
                } else {
                    icon = ContextCompat.getDrawable(getApplicationContext(), R.drawable.deleteappitem); // Εικονίδιο διαγραφής
                    iconDest = new RectF(itemView.getRight() - 150, itemView.getTop() + 50, itemView.getRight() - 50, itemView.getBottom() - 50);
                }

                icon.setBounds(Math.round(iconDest.left), Math.round(iconDest.top), Math.round(iconDest.right), Math.round(iconDest.bottom));
                icon.draw(c);
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();

                if (direction == ItemTouchHelper.LEFT) {
                    // Διαγραφή της εφαρμογής
                    AppsObj app = mainAppsAdapter.getAppsList().get(position);
                    dbHelper.deleteApp(app.getAppNames(), dbHelper.getUserIdByUsername(username));
                    mainAppsAdapter.getAppsList().remove(position);
                    mainAppsAdapter.notifyItemRemoved(position);

                    Snackbar.make(appsRecyclerView, "App deleted", Snackbar.LENGTH_LONG).show();

                } else if (direction == ItemTouchHelper.RIGHT) {
                    // Έλεγχος αν το swipe ξεπερνάει το 30% του πλάτους της οθόνης
                    float swipePercentage = Math.abs(viewHolder.itemView.getTranslationX()) / appsRecyclerView.getWidth();

                    if (swipePercentage >= 0.3f) {
                        // Αν ξεπεράσει το 30%, ανοίγουμε την επεξεργασία της εφαρμογής
                        AppsObj app = mainAppsAdapter.getAppsList().get(position);

                        Intent intent = new Intent(MainActivity.this, EditSelectedAppActivity.class);
                        intent.putExtra("APP_DATA", app);
                        intent.putExtra("APP_ID", app.getId());
                        intent.putExtra("USER_ID", userId);
                        intent.putExtra("POSITION", position);
                        startActivityForResult(intent, EDIT_APP_REQUEST);
                    } else {
                        // Αν δεν ξεπεράσει το 30%, κάνουμε bounce back
                        mainAppsAdapter.notifyItemChanged(position);
                    }
                }
            }
        };

        // Σύνδεση του ItemTouchHelper με το RecyclerView
        new ItemTouchHelper(simpleItemTouchCallback).attachToRecyclerView(appsRecyclerView);
    }

}