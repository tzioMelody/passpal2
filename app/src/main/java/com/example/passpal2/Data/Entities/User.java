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
    private String loginDate;
    private String loginTime;


    //Constructors
    public User(int id,String username, String email, String password, String loginDate, String loginTime) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.loginDate = loginDate;
        this.loginTime = loginTime;
    }
    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
        // Μπορείτε να αρχικοποιήσετε άλλα πεδία του χρήστη εδώ, όπως την ημερομηνία και την ώρα σύνδεσης.
    }

    public User(int id,String username, String email, String password, String loginDate) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.loginDate = loginDate;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", loginDate='" + loginDate + '\'' +
                ", loginTime='" + loginTime + '\'' +
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

    public String getLoginDate() {
        return loginDate;
    }

    public void setLoginDate(String loginDate) {
        this.loginDate = loginDate;
    }

    public String getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(String loginTime) {
        this.loginTime = loginTime;
    }
}