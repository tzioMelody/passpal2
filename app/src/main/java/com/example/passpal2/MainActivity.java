package com.example.passpal2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.content.Intent;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Parcelable;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private AppSelectionAdapter adapter;
    private List<AppsObj.AppInfo> selectedApps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setTitle("Pass Pal");

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
            }
        });
        ShareLy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Log in app or website
            }
        });
        UpdateLy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Update database
                Toast.makeText(MainActivity.this, "Updating...", Toast.LENGTH_SHORT).show();
            }
        });
        LoginPswLy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Will recuire a master password so he can go to the login and
                // passwords activity with all apps their usernames and their passwords
            }
        });
        SettingsLy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Settings can have the changes to the color of the app the interior
                //and the background.
            }
        });
    dialog.show();
    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
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
        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();
            if (direction == ItemTouchHelper.LEFT) {
                deletedApp = String.valueOf(selectedApps.get(position));
                Snackbar.make(((RecyclerView.ViewHolder) viewHolder).itemView, deletedApp, Snackbar.LENGTH_LONG)
                .setAction("Undo", new View.OnClickListener(){
                    @Override
                    public void onClick(View view){
                        selectedApps.add(position, selectedApps.get(position));
                        adapter.notifyItemInserted(position);
                    }
                        }).show();
                deleteApp(position);
            } else if (direction == ItemTouchHelper.RIGHT) {
                //BottomSheet
                editApp(position);
            }
        }
    }

    String deletedApp = null;
    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {


        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        //Delete app once swiped left
        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();
            switch(direction){
                //Delete
                case ItemTouchHelper.LEFT:
                    selectedApps.remove(position);
                    adapter.notifyItemRemoved(position);
                    break;
                    //Edit
                case  ItemTouchHelper.RIGHT:

                    break;
            }
        }
    };

    //Works for the undo button so DO NOT delete
    private void deleteApp(int position) {
        selectedApps.remove(position);
        adapter.notifyItemRemoved(position);
        Toast.makeText(this, "App deleted", Toast.LENGTH_SHORT).show();
    }

    //Edit app once swiped right
    private void editApp(int position) {
        // Edit app functionality here
        Toast.makeText(this, "App edited", Toast.LENGTH_SHORT).show();
    }
}

