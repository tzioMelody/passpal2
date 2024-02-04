package com.example.passpal2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AppSelectionAdapter extends RecyclerView.Adapter<AppSelectionAdapter.ViewHolder> {
    private Context context;
    private List<AppsObj.AppInfo> apps;
    private AppSelectionViewModel viewModel;

    public AppSelectionAdapter(Context context, List<AppsObj.AppInfo> apps, AppSelectionViewModel viewModel) {
        this.context = context;
        this.apps = apps;
        this.viewModel = viewModel;
    }

    public AppSelectionAdapter(MainActivity context, List<AppsObj.AppInfo> selectedApps) {
        this.context = context;
        this.apps = apps;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.app_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppsObj.AppInfo app = apps.get(position);
        holder.appName.setText(app.getAppName());
        holder.appIcon.setImageResource(app.getAppIconId());
        holder.toggleButton.setOnCheckedChangeListener(null);
        holder.toggleButton.setChecked(viewModel.getSelectedAppsLiveData().getValue().contains(app));
        holder.toggleButton.setOnCheckedChangeListener((buttonView, isChecked) -> viewModel.toggleAppSelection(app));
    }

    @Override
    public int getItemCount() {
        return apps.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView appName;
        ImageView appIcon;
        ToggleButton toggleButton;

        public ViewHolder(View itemView) {
            super(itemView);
            appName = itemView.findViewById(R.id.appNameTextView);
            appIcon = itemView.findViewById(R.id.appIconImageView);
            toggleButton = itemView.findViewById(R.id.toggleButton);
        }
    }
}
