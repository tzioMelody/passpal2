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

public class AppSelectionAdapter extends RecyclerView.Adapter<AppSelectionAdapter.AppViewHolder> {
    private Context context;
    private List<AppsObj.AppInfo> appList;
    private AppSelectionViewModel viewModel;

    public AppSelectionAdapter(Context context, List<AppsObj.AppInfo> appList, AppSelectionViewModel viewModel) {
        this.context = context;
        this.appList = appList;
        this.viewModel = viewModel;
    }

    @NonNull
    @Override
    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.app_item, parent, false);
        return new AppViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
        AppsObj.AppInfo appInfo = appList.get(position);
        holder.appNameTextView.setText(appInfo.getAppName());
        holder.appIconImageView.setImageResource(appInfo.getAppIconId());

        boolean isSelected = viewModel.getSelectedAppsLiveData().getValue().contains(appInfo);
        holder.toggleButton.setChecked(isSelected);

        holder.toggleButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                viewModel.addSelectedApp(appInfo);
            } else {
                viewModel.removeSelectedApp(appInfo);
            }
        });
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

    public void updateAppList(List<AppsObj.AppInfo> newAppList) {
        this.appList = newAppList;
        // Ειδοποιεί τον adapter για την αλλαγή δεδομένων
        notifyDataSetChanged();
    }

    static class AppViewHolder extends RecyclerView.ViewHolder {
        ImageView appIconImageView;
        TextView appNameTextView;
        ToggleButton toggleButton;

        public AppViewHolder(View itemView) {
            super(itemView);
            appIconImageView = itemView.findViewById(R.id.appIconImageView);
            appNameTextView = itemView.findViewById(R.id.appNameTextView);
            toggleButton = itemView.findViewById(R.id.toggleButton);
        }
    }
}
