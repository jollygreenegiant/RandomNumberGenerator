package com.jggdevelopment.randomnumbergenerator;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String themeName = pref.getString("theme", "Default");
        if (themeName.equals("Default")) {
            setTheme(R.style.DefaultTheme);
        } else if (themeName.equals("Dark")) {
            setTheme(R.style.Dark);
        } else if (themeName.equals("Red/Green")) {
            setTheme(R.style.RedGreen);
        } else if (themeName.equals("Orange/Blue")) {
            setTheme(R.style.OrangeBlue);
        } else if (themeName.equals("Red/Blue")) {
            setTheme(R.style.RedBlue);
        }

        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}
