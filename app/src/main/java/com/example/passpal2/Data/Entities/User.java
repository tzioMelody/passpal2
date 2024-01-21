package com.example.passpal2.Data.Entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String username;
    private String email;
    private String password;
    private String loginDateTime;


    // Κατασκευαστές (Constructors)
    public User(int id, String username, String email, String password, String loginDateTime) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        // Εδώ δεν ορίζεται η ημερομηνία και ώρα σύνδεσης
    }

    public User(String username, String email, String password, String loginDateTime) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.loginDateTime = loginDateTime;
    }


    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", loginDateTime='" + loginDateTime + '\'' +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLoginDateTime() {
        return loginDateTime;
    }

    public void setLoginDateTime(String loginDateTime) {
        this.loginDateTime = loginDateTime;
    }
}