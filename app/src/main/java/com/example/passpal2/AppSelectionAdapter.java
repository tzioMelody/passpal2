package com.example.passpal2;



import android.content.Context;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import java.util.ArrayList;
import java.util.List;

public class AppSelectionAdapter extends RecyclerView.Adapter<AppSelectionAdapter.AppViewHolder> {
    private Context context;
    private List<AppsObj.AppInfo> appList;
    private OnItemClickListener clickListener;
    private List<AppsObj.AppInfo> commonApps; // Νέο πεδίο για τις κοινές εφαρμογές

    public AppSelectionAdapter(Context context, List<AppsObj.AppInfo> appList, List<AppsObj.AppInfo> commonApps) {
        this.context = context;
        this.appList = appList;
        this.commonApps = commonApps; // Αναθέστε τη λίστα commonApps
    }



    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.clickListener = listener;
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
        ImageView appIconImageView = holder.itemView.findViewById(R.id.addBtn);
        appIconImageView.setImageResource(appInfo.getAppIconId());

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onItemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

    static class AppViewHolder extends RecyclerView.ViewHolder {
        ImageView appIconImageView;
        TextView appNameTextView;

        public AppViewHolder(View itemView) {
            super(itemView);
            appIconImageView = itemView.findViewById(R.id.addBtn);
            appNameTextView = itemView.findViewById(R.id.appNameTextView);
        }
    }
}
