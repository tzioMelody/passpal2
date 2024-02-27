package com.example.passpal2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainAppsAdapter extends RecyclerView.Adapter<MainAppsAdapter.AppViewHolder> {

    private Context context;
    private ArrayList<AppsObj> appsList;
    private final RecyclerViewInterface recyclerViewInterface;

    public interface RecyclerViewInterface {
        void onItemClick(int position);
    }

    public MainAppsAdapter(Context context, ArrayList<AppsObj> appsList, RecyclerViewInterface recyclerViewInterface) {
        this.context = context;
        this.appsList = appsList;
        this.recyclerViewInterface = recyclerViewInterface;
    }

    @NonNull
    @Override
    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.app_item, parent, false);
        return new AppViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
        AppsObj app = appsList.get(position);
        holder.appName.setText(app.getAppNames());
        holder.imageView.setImageResource(app.getAppImages());
    }

    @Override
    public int getItemCount() {
        return appsList.size();
    }

    public void setSelectedApps(List<AppsObj> appsList) {
        this.appsList.clear();
        this.appsList.addAll(appsList);
        notifyDataSetChanged();
    }

    public class AppViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView imageView;
        TextView appName;

        public AppViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            appName = itemView.findViewById(R.id.appname);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                recyclerViewInterface.onItemClick(position);
            }
        }
    }
}
