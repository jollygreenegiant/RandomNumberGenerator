package com.jggdevelopment.randomnumbergenerator;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.preference.PreferenceManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends PreferenceFragmentCompat {
    public static final int RESULT_CODE_THEME_UPDATED = 1;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        findPreference("theme").setOnPreferenceChangeListener(
                new RefreshActivityOnPreferenceChangeListener(RESULT_CODE_THEME_UPDATED));
    }

    private class RefreshActivityOnPreferenceChangeListener implements Preference.OnPreferenceChangeListener {
        private final int resultCode;

        public RefreshActivityOnPreferenceChangeListener (int resultCode) {
            this.resultCode = resultCode;
        }

        @Override
        public boolean onPreferenceChange(Preference p, Object newValue) {
            Activity parent = getActivity();
            parent.setResult(resultCode);
            parent.finish();
            parent.startActivity(parent.getIntent());
            return true;
        }
    }

}
