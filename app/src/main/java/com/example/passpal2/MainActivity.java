package com.example.passpal2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeToDeleteCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT));
        itemTouchHelper.attachToRecyclerView(appsRecyclerView);

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
                case R.id.menu_item1:
                    // Handle menu item 1
                    return true;
                case R.id.menu_item2:
                    // Handle menu item 2
                    return true;
                case R.id.menu_item3:
                    // Handle menu item 3
                    return true;
                case R.id.menu_item4:
                    Intent helpIntent = new Intent(MainActivity.this, HelpActivity.class);
                    startActivity(helpIntent);
                    return true;
                case R.id.menu_item5:
                    performLogout();
                    return true;
                default:
                    return false;
            }
        });

        popupMenu.show();
    }

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
    private void deleteApp(int position) {
        selectedApps.remove(position);
        adapter.notifyItemRemoved(position);
        Toast.makeText(this, "App deleted", Toast.LENGTH_SHORT).show();
    }

    private void editApp(int position) {
        // Edit app functionality here
        Toast.makeText(this, "App edited", Toast.LENGTH_SHORT).show();
    }
}

