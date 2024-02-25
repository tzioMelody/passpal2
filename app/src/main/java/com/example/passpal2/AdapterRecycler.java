package com.example.passpal2;

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
    ArrayList<AppsObj> selectedApps = new ArrayList<>();
    public AdapterRecycler(Context context, ArrayList<AppsObj> appsObjs, RecyclerViewInterface recyclerViewInterface){
        this.context = context;
        this.appsObjs = appsObjs;
        this.recyclerViewInterface = recyclerViewInterface;
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
        Log.d("MyApp", "Application selected: " + appName + ", Position: " + position);

        // Προσθήκη της εφαρμογής στη λίστα selectedApps
        if (app.isSelected()) {
            selectedApps.add(app);
        } else {
            selectedApps.remove(app);
        }

        // Εκτύπωση της λίστας selectedApps στο Logcat
        StringBuilder selectedAppsNames = new StringBuilder();
        for (AppsObj selectedApp : selectedApps) {
            selectedAppsNames.append(selectedApp.getAppNames()).append(", ");
        }
        if (selectedAppsNames.length() > 0) {
            selectedAppsNames.setLength(selectedAppsNames.length() - 2); // Αφαιρούμε το τελευταίο κόμμα και το κενό
        }
        Log.d("MyApp", "Selected Apps: " + selectedAppsNames);
    }

    // Μέθοδος για να ενημερώνει τη λίστα των επιλεγμένων εφαρμογών
    public void setSelectedApps(List<AppsObj> selectedApps) {
        this.selectedApps.clear();
        this.selectedApps.addAll(selectedApps);
        notifyDataSetChanged();
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
