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
import com.example.passpal2.AppsObj;
import com.example.passpal2.R;
import java.util.List;

public class AppSelectionAdapter extends RecyclerView.Adapter<AppSelectionAdapter.AppViewHolder> {

    private final Context mContext;
    private final List<AppsObj.AppInfo> mAppList;
    private final List<AppsObj.AppInfo> mSelectedApps;
    private final OnItemClickListener mListener;


    public interface OnItemClickListener {
        void onItemClick(AppsObj.AppInfo appInfo);
    }

    public AppSelectionAdapter(Context context, List<AppsObj.AppInfo> appList, List<AppsObj.AppInfo> selectedApps, OnItemClickListener listener) {
        mContext = context;
        mAppList = appList;
        mSelectedApps = selectedApps;
        mListener = listener;
    }

    @NonNull
    @Override
    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.app_item, parent, false);
        return new AppViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
        AppsObj.AppInfo appInfo = mAppList.get(position);
        holder.appNameTextView.setText(appInfo.getAppName());
        holder.appIconImageView.setImageResource(appInfo.getAppIconId());
        holder.toggleButton.setChecked(mSelectedApps.contains(appInfo));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onItemClick(appInfo);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mAppList.size();
    }
    // Μέθοδος για να ενημερώσετε τη λίστα των εφαρμογών
    public void setAppList(List<AppsObj.AppInfo> appList) {
        mAppList.clear();
        mAppList.addAll(appList);
        notifyDataSetChanged();
    }
    static class AppViewHolder extends RecyclerView.ViewHolder {
        ImageView appIconImageView;
        TextView appNameTextView;
        ToggleButton toggleButton;

        public AppViewHolder(@NonNull View itemView) {
            super(itemView);
            appIconImageView = itemView.findViewById(R.id.appIconImageView);
            appNameTextView = itemView.findViewById(R.id.appNameTextView);
            toggleButton = itemView.findViewById(R.id.toggleButton);
        }
    }
}
