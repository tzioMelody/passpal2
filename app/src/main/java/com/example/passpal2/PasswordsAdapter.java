package com.example.passpal2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PasswordsAdapter extends RecyclerView.Adapter<PasswordsAdapter.PasswordViewHolder> {

    private final List<DataBaseHelper.AppCredentials> credentialsList;

    public PasswordsAdapter(List<DataBaseHelper.AppCredentials> credentialsList) {
        this.credentialsList = credentialsList;
    }

    @NonNull
    @Override
    public PasswordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Φόρτωση του layout για κάθε item
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_passwordtable_item, parent, false);
        return new PasswordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PasswordViewHolder holder, int position) {
        // Λήψη των δεδομένων του item για τη συγκεκριμένη θέση
        DataBaseHelper.AppCredentials credentials = credentialsList.get(position);

        // Ανάθεση τιμών στα στοιχεία του layout
        holder.appNameTextView.setText(credentials.getAppName());
        holder.usernameTextView.setText(credentials.getUsername());
        holder.passwordTextView.setText("••••••••");

        // Εμφάνιση/Απόκρυψη του κωδικού όταν πατηθεί το κουμπί Show/Hide
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
        // Επιστροφή του πλήθους των credentials
        return credentialsList.size();
    }

    // ViewHolder για κάθε item της λίστας
    public static class PasswordViewHolder extends RecyclerView.ViewHolder {
        TextView appNameTextView;
        TextView usernameTextView;
        TextView passwordTextView;
        TextView showHideTextView;

        public PasswordViewHolder(@NonNull View itemView) {
            super(itemView);
            // Σύνδεση των στοιχείων του layout με τα Views
            appNameTextView = itemView.findViewById(R.id.appNameTextView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView);
            passwordTextView = itemView.findViewById(R.id.passwordTextView);
            showHideTextView = itemView.findViewById(R.id.showHideTextView);
        }
    }
}
