package com.example.passpal2;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MainAppsAdapter extends RecyclerView.Adapter<MainAppsAdapter.ViewHolder> {
    private Context context;
    private List<AppsObj> appsList;
    private List<AppsObj> selectedApps;

    public List<AppsObj> getAppsList() {
        return this.appsList;
    }
    public MainAppsAdapter(Context context, List<AppsObj> appsList) {
        this.context = context;
        this.appsList = appsList;
        this.selectedApps = selectedApps;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.app_item_main, parent, false);
        Log.d("FetchAppsTask", "view holder first " );

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppsObj app = appsList.get(position);
        holder.appName.setText(app.getAppNames());
        holder.appImage.setImageResource(app.getAppImages());
        Log.d("FetchAppsTask", "bind view holder");

    }

  /*  public void deleteApp(int position) {
        if (position >= 0 && position < appsList.size()) {
            selectedApps.remove(appsList.get(position)); // Αφαίρεση από τη selectedApps
            appsList.remove(position);
            notifyItemRemoved(position);
        }
    }
*/
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
        TextView appName;
        ImageView appImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            appName = itemView.findViewById(R.id.appname);
            appImage = itemView.findViewById(R.id.imageView);
        }
    }
}
