package com.example.passpal2;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PasswordsAdapter extends RecyclerView.Adapter<PasswordsAdapter.PasswordViewHolder> {

    private final List<DataBaseHelper.AppCredentials> credentialsList;
    private final Context context;
    private DataBaseHelper dbHelper;



    public PasswordsAdapter(List<DataBaseHelper.AppCredentials> credentialsList, Context context) {
        this.credentialsList = credentialsList;
        this.context = context;
    }

    @NonNull
    @Override
    public PasswordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_passwordtable_item, parent, false);
        return new PasswordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PasswordViewHolder holder, int position) {
        DataBaseHelper.AppCredentials credentials = credentialsList.get(position);

        holder.appNameTextView.setText(credentials.getAppName());
        holder.usernameTextView.setText(credentials.getUsername());
        holder.passwordTextView.setText("••••••••");

        holder.showHideTextView.setOnClickListener(v -> {
            if (holder.showHideTextView.getText().toString().equals("Show")) {
                holder.passwordTextView.setText(credentials.getPassword());
                holder.showHideTextView.setText("Hide");
            } else {
                holder.passwordTextView.setText("••••••••");
                holder.showHideTextView.setText("Show");
            }
        });
    }

    @Override
    public int getItemCount() {
        return credentialsList.size();
    }

    public static class PasswordViewHolder extends RecyclerView.ViewHolder {
        TextView appNameTextView;
        TextView usernameTextView;
        TextView passwordTextView;
        TextView showHideTextView;
        TextView copyTextView;
        TextView deleteTextView;

        public PasswordViewHolder(@NonNull View itemView) {
            super(itemView);
            appNameTextView = itemView.findViewById(R.id.appNameTextView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView);
            passwordTextView = itemView.findViewById(R.id.passwordTextView);
            showHideTextView = itemView.findViewById(R.id.showHideTextView);
            copyTextView = itemView.findViewById(R.id.copyTextView);
            deleteTextView = itemView.findViewById(R.id.deleteTextView);
        }
    }
}