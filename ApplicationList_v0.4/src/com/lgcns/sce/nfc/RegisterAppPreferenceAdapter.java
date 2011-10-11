
package com.lgcns.sce.nfc;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * @class AppRegister DAO Class for information of quick launch applications **
 *        Example of Store structure **
 *        |-----(1st)-----|-----(2nd)-----|-----(3rd)-----|-----(4th)-----|----
 *        Calculator Calculator empty Calculator saved on
 *        com.lgcns.sce.nfc_preferences.xml <int name="Cabinet" value="11"/>
 *        <string name="Pkg1">com.android.calculator2</string> <string
 *        name="Pkg2">com.android.calculator2</string> <string
 *        name="Pkg8">com.android.calculator2</string>
 */
public class RegisterAppPreferenceAdapter extends RegisterApp {
    static final String TAG = "AppRegister";
    private static SharedPreferences mPrefs;

    private static final String CABINET = "Cabinet";
    private static final String PACKAGE = "Package";
    private static final String HASHCODE = "HashCode";

    public RegisterAppPreferenceAdapter(Context context) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);

    }

    // Register Application information for auto launch
    @Override
    public boolean registerApps(String hashCode, String pkg) {
        int Cabinet = mPrefs.getInt(CABINET, 0);

        if (Cabinet == 0xffffffff) return false;

        try {
            SharedPreferences.Editor editor = mPrefs.edit();
            int location = getEmptyLocation(Cabinet);
            editor.putInt(CABINET, location | Cabinet);
            editor.putString(PACKAGE + location, pkg);
            editor.putString(HASHCODE + location, hashCode);
            editor.commit();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    // Unregister auto launch information
    public boolean unregisterAppsPkg(String packageName) {
        int Cabinet = mPrefs.getInt(CABINET, 0);

        if (Cabinet == 0) {
            return false;
        } else {
            int iter = 1;
            for (int inx = 1; inx <= 15; inx++) {
                if (packageName.equals(mPrefs.getString(PACKAGE + inx, null))) {
                    int mask = 0xffffffff ^ inx;
                    SharedPreferences.Editor editor = mPrefs.edit();
                    editor.remove(PACKAGE + iter);
                    editor.remove(HASHCODE + iter);
                    editor.putInt(CABINET, mask & Cabinet);
                    editor.commit();
                    return true;
                }
                iter = iter << 1;
            }
        }
        return false;
    }

    @Override
    public boolean unregisterAppsHash(String hashcode) {
        int Cabinet = mPrefs.getInt(CABINET, 0);

        if (Cabinet == 0) {
            return false;
        } else {
            int iter = 1;
            for (int inx = 1; inx <= 15; inx++) {
                if (hashcode.equals(mPrefs.getString(HASHCODE + inx, null))) {
                    int mask = 0xffffffff ^ inx;
                    SharedPreferences.Editor editor = mPrefs.edit();
                    editor.remove(PACKAGE + iter);
                    editor.remove(HASHCODE + iter);
                    editor.putInt(CABINET, mask & Cabinet);
                    editor.commit();
                    return true;
                }
                iter = iter << 1;
            }
        }
        return false;
    }

    // Unregister All informations for auto launch
    public void unregisterAll() {
        SharedPreferences.Editor editor = mPrefs.edit();
        int Cabinet = mPrefs.getInt(CABINET, 0);
        int iter = 1;
        for (int inx = 1; inx <= 15; inx++) {
            if (iter == (iter & Cabinet)) {
                editor.remove(PACKAGE + iter);
                editor.remove(HASHCODE + iter);
                editor.commit();
            }
            iter = iter << 1;
        }
        editor.remove(CABINET);
        editor.commit();
    }

    // Retrieve registered apps information from storage
    public ArrayList<String> retrieveApps() {
        int Cabinet = mPrefs.getInt(CABINET, 0);

        if (Cabinet == 0) {
            return null;
        } else {
            int iter = 1;
            ArrayList<String> appList = new ArrayList<String>();
            while (iter != 0x80000000) {
                if (iter == (iter & Cabinet)) {

                    appList.add(mPrefs.getString(HASHCODE + iter, null));
                }
                iter = iter << 1;
            }
            return appList;
        }

    }

    // Get Empty space to save information.
    // Total usable space is 32.
    // Cabinet number is 0x00000001 ~ 0xffffffff, each bit checks each space.
    // So 1st cabinet is 0x00000001, and second is 0x00000002, and third is
    // 0x00000004 and so on.
    // Additionally, the value of "Cabinet" is sum of stored cabinet numbers.
    private int getEmptyLocation(int loc) {
        int emptyLocation = 1;

        while (emptyLocation == (loc & emptyLocation)) {
            emptyLocation = emptyLocation << 1;
        }

        return emptyLocation;
    }

    @Override
    public ArrayList<String[]> retrieveExecInfo() {
        int Cabinet = mPrefs.getInt(CABINET, 0);

        if (Cabinet == 0) {
            return null;
        } else {
            int iter = 1;
            ArrayList<String[]> appList = new ArrayList<String[]>();
            while (iter != 0x80000000) {
                if (iter == (iter & Cabinet)) {
                    appList.add(new String[] {
                            mPrefs.getString(HASHCODE + iter, null), 
                            getSerial(mPrefs.getString(PACKAGE + iter, null))
                    });
                }
                iter = iter << 1;
            }
            return appList;
        }
    }

}
