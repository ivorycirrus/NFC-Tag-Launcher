
package com.lgcns.sce.nfc;

import java.util.ArrayList;

public abstract class RegisterApp {

    // Register Application information for auto launch
    public abstract boolean registerApps(String hashCode, String pkg);

    // Unregister auto launch information
    public abstract boolean unregisterAppsPkg(String Package);

    public abstract boolean unregisterAppsHash(String hashcode);

    // Unregister All informations for auto launch
    public abstract void unregisterAll();

    // Retrieve registered apps information from storage
    public abstract ArrayList<String> retrieveApps();
    
    public abstract ArrayList<String[]> retrieveExecInfo();

    // Make serial number using package names
    // Some NFC tags has very small storage space, and it can't store string of
    // full package name. So this method provides abbreviation of long package
    // name.
    public String getSerial(String pkg) {
        int serial = 0;
        int lastPoint = pkg.lastIndexOf(".");
        String pref = pkg.substring(0, lastPoint);
        String suf = pkg.substring(lastPoint);
        for (int inx = 0; inx < pref.length(); inx++) {
            serial += pref.charAt(inx);
        }
        return Integer.toHexString(serial) + suf;
    }

}
