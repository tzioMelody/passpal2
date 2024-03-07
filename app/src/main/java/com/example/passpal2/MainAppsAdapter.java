package com.example.passpal2;

import android.content.Context;
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

    public MainAppsAdapter(Context context, List<AppsObj> appsList) {
        this.context = context;
        this.appsList = appsList;
    }


    /**
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.app_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppsObj app = appsList.get(position);
        holder.appName.setText(app.getAppNames());
        holder.appImage.setImageResource(app.getAppImages());
        // Απενεργοποίηση του ToggleButton
/*
        holder.toggleButton.setChecked(false);
*/
    }
    public void setSelectedApps(List<AppsObj> apps) {
        this.appsList = apps; // Ενημέρωση της τρέχουσας λίστας με τη νέα λίστα εφαρμογών
        notifyDataSetChanged(); // Ειδοποίηση του adapter ότι τα δεδομένα έχουν αλλάξει
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
            appName = itemView.findViewById(R.id.appname); // Ενημερώστε ανάλογα με το ID του TextView στο layout σας
            appImage = itemView.findViewById(R.id.imageView); // Ενημερώστε ανάλογα με το ID του ImageView στο layout σας
        }
    }
}
