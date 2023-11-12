package com.example.passpal2;



import android.content.Context;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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


        // Έλεγχος για το checkmark
        if (commonApps.contains(appInfo)) {
            // Εάν η τρέχουσα εφαρμογή βρίσκεται στις κοινές εφαρμογές, εμφανίστε το checkmark
            // ή κάποιο άλλο σήμα που υποδεικνύει την επιλογή του χρήστη
            // Εδώ χρησιμοποιείται ένα απλό checkmark, αλλά μπορείτε να χρησιμοποιήσετε κάποιο εικονίδιο ή αντίστοιχο γραφικό στο σημείο αυτό
            holder.checkMarkImageView.setVisibility(View.VISIBLE);


        } else {
            holder.checkMarkImageView.setVisibility(View.INVISIBLE);

        }

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onItemClick(position);

                // Έλεγχος για το checkmark όταν γίνεται κλικ
                if (commonApps.contains(appInfo)) {
                    holder.checkMarkImageView.setVisibility(View.VISIBLE);
                } else {
                    holder.checkMarkImageView.setVisibility(View.INVISIBLE);
                }
            }
        });
    }
    public void updateUserApps(List<AppsObj.UserApp> userAppsList) {
        this.appList.clear(); // Καθαρίστε την υπάρχουσα λίστα
        for (AppsObj.UserApp userApp : userAppsList) {
            this.appList.add(new AppsObj.AppInfo(userApp.getAppName(), userApp.getAppLink(), R.drawable.default_app_icon));
        }
        notifyDataSetChanged(); // Ενημερώστε το RecyclerView
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

    static class AppViewHolder extends RecyclerView.ViewHolder {
        ImageView appIconImageView;
        TextView appNameTextView;
        ImageView checkMarkImageView;

        public AppViewHolder(View itemView) {
            super(itemView);
            appIconImageView = itemView.findViewById(R.id.addBtn);
            appNameTextView = itemView.findViewById(R.id.appNameTextView);
            checkMarkImageView = itemView.findViewById(R.id.checkMarkImageView);
        }

    }
}
