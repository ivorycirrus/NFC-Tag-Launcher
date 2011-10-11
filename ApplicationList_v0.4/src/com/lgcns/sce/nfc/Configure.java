package com.lgcns.sce.nfc;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;

/**
 * @class AppRegister
 * 
 * Configuration for Launch apppplications  
 */
public class Configure extends PreferenceActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_config);
        
        final CheckBoxPreference auto_launch = (CheckBoxPreference) findPreference("pref_auto_launch");
        final CheckBoxPreference private_mode = (CheckBoxPreference) findPreference("pref_my_device_only");
        auto_launch.setOnPreferenceClickListener(new OnPreferenceClickListener() {            
            public boolean onPreferenceClick(Preference preference) {
                if(auto_launch.isChecked()){
                    private_mode.setEnabled(true);
                }
                else
                {
                    private_mode.setEnabled(false);
                }
                return false;
            }
        });
    }

}
