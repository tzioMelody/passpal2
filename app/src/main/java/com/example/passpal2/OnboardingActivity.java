package com.example.passpal2;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;

import java.util.ArrayList;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        ViewPager2 viewPager = findViewById(R.id.viewPager);
        DotsIndicator dotsIndicator = findViewById(R.id.dotsIndicator);

        List<OnboardingItem> items = new ArrayList<>();
        items.add(new OnboardingItem("Ασφάλεια", "Προστατέψτε τους κωδικούς σας με κρυπτογράφηση.", R.drawable.security_image));
        items.add(new OnboardingItem("Προσθήκη Κωδικών", "Αποθηκεύστε κωδικούς από αγαπημένες εφαρμογές.", R.drawable.add_passwords_image));
        items.add(new OnboardingItem("Άμεση Πρόσβαση", "Δείτε τους αποθηκευμένους κωδικούς σας ανά πάσα στιγμή.", R.drawable.access_image));

        OnboardingAdapter adapter = new OnboardingAdapter(items);
        viewPager.setAdapter(adapter);
        dotsIndicator.attachTo(viewPager);
    }
}
