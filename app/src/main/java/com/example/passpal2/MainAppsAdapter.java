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

import java.util.List;

public class MainAppsAdapter extends RecyclerView.Adapter<MainAppsAdapter.ViewHolder> {
    private Context context;
    private List<AppsObj> appsList;
    private RecyclerViewInterface recyclerViewInterface;
    public List<AppsObj> getAppsList() {
        return this.appsList;
    }

    public MainAppsAdapter(Context context, List<AppsObj> appsList, RecyclerViewInterface recyclerViewInterface) {
        this.context = context;
        this.appsList = appsList;
        this.recyclerViewInterface = recyclerViewInterface;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.app_item_main, parent, false);
        Log.d("FetchAppsTask", "view holder first " );

        return new ViewHolder(view, recyclerViewInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppsObj app = appsList.get(position);
        holder.appName.setText(app.getAppNames());
        holder.appImage.setImageResource(app.getAppImages());
        holder.itemView.setOnClickListener(v -> {
            if (recyclerViewInterface != null) {
                recyclerViewInterface.onItemClick(position);
            }
        });
        Log.d("FetchAppsTask", "In bind View Holder in main apps adapter");
    }

    public void setSelectedApps(List<AppsObj> apps) {
        this.appsList = apps;
        Log.d("FetchAppsTask", "setselectedapp");

        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return appsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView appImage;
        TextView appName;

        public ViewHolder(@NonNull View itemView, RecyclerViewInterface recyclerViewInterface) {
            super(itemView);
            appName = itemView.findViewById(R.id.appname);
            appImage = itemView.findViewById(R.id.imageView);

            // Κλικ στο item (εκτός από το ToggleButton)
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
