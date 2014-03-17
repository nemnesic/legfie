package com.ndnlogic.legs;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Call super :
        super.onCreate(savedInstanceState);

        // Set the activity's fragment :
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();


    }

    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);
        }
    }


}
