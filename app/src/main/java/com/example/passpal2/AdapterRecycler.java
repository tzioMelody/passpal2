package com.example.passpal2;

import static java.lang.String.valueOf;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AdapterRecycler extends RecyclerView.Adapter<AdapterRecycler.MyViewHolder>{
    private final RecyclerViewInterface recyclerViewInterface;
    Context context;
    ArrayList<AppsObj> appsObjs;
    private ArrayList<AppsObj> appsObjList;
    ArrayList<AppsObj> selectedApps = new ArrayList<>();
    public AdapterRecycler(Context context, ArrayList<AppsObj> appsObjs, RecyclerViewInterface recyclerViewInterface){
        this.context = context;
        this.appsObjs = appsObjs;
        this.recyclerViewInterface = recyclerViewInterface;
    }

    public void setfilteredList(ArrayList<AppsObj> filteredList){
        this.appsObjList = filteredList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.app_item, parent, false);
        return new MyViewHolder(view, recyclerViewInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        AppsObj app = appsObjs.get(position);

        holder.appName.setText(appsObjs.get(position).getAppNames());
        holder.imageView.setImageResource(appsObjs.get(position).getAppImages());

        // Ενημέρωση του ToggleButton βάσει της κατάστασης επιλογής
        holder.toggleButton.setChecked(app.isSelected());

        // Ορισμός click listener για το ToggleButton
        holder.toggleButton.setOnClickListener(view -> {
            // Αλλαγή της κατάστασης επιλογής και ενημέρωση του adapter
            toggleItemSelection(position);
        });
    }
    public void addApp(AppsObj newApp) {
        appsObjs.add(newApp);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return appsObjs.size();
    }

    public void toggleItemSelection(int position) {
        AppsObj app = appsObjs.get(position);
        // Αλλάζει την κατάσταση επιλογής
        app.setSelected(!app.isSelected());
        // Ενημερώνει το συγκεκριμένο στοιχείο
        notifyItemChanged(position);
        // Εκτύπωση πληροφοριών στο Logcat
        String appName = app.getAppNames();
        Log.d("MyApp", "Application selected: " + appName );

        // Προσθήκη της εφαρμογής στη λίστα selectedApps
        if (app.isSelected()) {
            selectedApps.add(app);
        } else {
            selectedApps.remove(app);
        }

    }



    public int getSelectedAppsCount() {
        return selectedApps.size();
    }

    public ArrayList<AppsObj> getSelectedApps() {
        ArrayList<AppsObj> selectedApps = new ArrayList<>();
        for (AppsObj selectedApp : this.selectedApps) {
            if (selectedApp.isSelected()) {
                selectedApps.add(selectedApp);
                Log.d("MyApp", "Selected Apps in AdapterRecycler :  " + selectedApps.toString());

            }
        }
        return selectedApps;
    }




    public static class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView appName;
        ToggleButton toggleButton;

        public MyViewHolder(@NonNull View itemView, RecyclerViewInterface recyclerViewInterface) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            appName = itemView.findViewById(R.id.appname);
            toggleButton = itemView.findViewById(R.id.toggleButton);

            itemView.setOnClickListener(view -> {
                if (recyclerViewInterface != null) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        recyclerViewInterface.onItemClick(pos);
                    }
                }
            });
        }
    }
}
