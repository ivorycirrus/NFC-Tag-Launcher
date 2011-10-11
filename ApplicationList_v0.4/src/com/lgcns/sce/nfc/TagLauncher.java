
package com.lgcns.sce.nfc;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

/**
 * @class TagLauncher
 * 
 * The First Activity for TagLauncher application.
 * The Aim is to show intro image when TagLauncher application is executed. 
 */
public class TagLauncher extends Activity {

    SharedPreferences mPrefs;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tag_launcher);        
        init();
        
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (mPrefs.getBoolean("pref_first_execute", true))
        {
            SharedPreferences.Editor editor = mPrefs.edit();
            editor.putBoolean("pref_auto_launch", true);
            editor.putBoolean("pref_first_execute", false);
            editor.commit();
        }
    }
    
    private void init()
    {
        new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(2000);
                    Intent i = new Intent(TagLauncher.this, AppList.class);
                    startActivity(i);
                    finish();
                } catch (InterruptedException e) {                    
                    e.printStackTrace();
                }
            }
        }).start();
    }

}
