
package com.lgcns.sce.nfc;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

/**
 * @class AppExec Application Launcher from NFC tag reading.
 */
public class AppExec extends Activity {

    static final String TAG = "APPList";

    private PackageManager mPm;
    private RegisterApp mAR;
    private SharedPreferences mPrefs;

    private String mDevId;
    private String mPackage;
    private String mHashCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Check Launch app automatically.
        if (!checkAutoLaunch()) {
            finish();
            return;
        }

        mPm = getPackageManager();
        // mAR = new RegisterAppPreferenceAdapter(this);
        mAR = new RegisterAppDatabaseAdapter(this);

        // Get informations from ndef messages.
        NdefMessage msg = (NdefMessage) this.getIntent().getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)[0];
        NdefRecord rec = msg.getRecords()[0];
        byte[] data = rec.getPayload();

        // parse data from payload.
        byte[][] split_data = ByteUtils.byteSplit(data, 8, 4, data.length - 12);
        mDevId = "" + ByteUtils.byteArrayToLong(split_data[0]);
        mHashCode = "" + ByteUtils.byteArrayToInt(split_data[1]);
        mPackage = parseText(split_data[2]);

        Log.d(TAG, "-----------<Tag Read>-----------");
        Log.d(TAG, "Device Id : " + mDevId + "\n");
        Log.d(TAG, "HashCode : " + mHashCode + "\n");
        Log.d(TAG, "Package : " + mPackage + "\n");
        Log.d(TAG, "--------------------------------");

        // Check Private launch mode
        if (!checkDevice()) {
            finish();
            return;
        }

        // Launch application.
        execute();
    }

    // Get data from payload.
    private String parseText(byte[] bytes) {
        try {
            String textEncoding = "US_ASCII";
            String text = new String(bytes, 0, bytes.length, textEncoding);
            return text;
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    // Launch application
    //
    // 1. hash code & serial of package name are matched
    //    ==> get launch intent by hash code
    // 2. only serial of package name is matched
    //    ==> get launch intent by package name which 1st searched
    //
    // This algorithm is to avoid the situation when same application has
    // different hash code between different devices or when a package has
    // 2 or more launch intent.
    private void execute() {
        if (mHashCode == null || mHashCode.equals("")) {
            executeFailure(R.string.appexec_sorry_app_info_not_found, false);
            return;
        }

        Intent intent = null;
        ArrayList<String[]> list = mAR.retrieveExecInfo();
        if (list == null || list.size() == 0) {
            executeFailure(R.string.appexec_sorry_no_registered_apps, false);
            return;
        } else {
            for (String[] info : list) {
                if (mHashCode.equals(info[0]) && mPackage.equals(info[1])) {
                    intent = getLaunchIntentHash(mHashCode);
                    intent.setFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
                    startActivity(intent);
                    finish();
                    return;
                } else if (mPackage.equals(info[1])) {
                    intent = getLaunchIntentPackage(mPackage);
                    intent.setFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
                    startActivity(intent);
                    finish();
                    return;
                }
            }
            executeFailure(R.string.appexec_sorry_no_matched_apps, false);
        }
    }

    // Show failure message
    private void executeFailure(int string, boolean b) {
        if (!b) Toast.makeText(this, string, Toast.LENGTH_LONG).show();
        finish();
    }

    // Check Auto Launch
    private boolean checkAutoLaunch() {
        if (!mPrefs.getBoolean("pref_auto_launch", false)) {
            Toast.makeText(this, R.string.appexec_launch_option_disabled, Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    // Check Private launch mode
    private boolean checkDevice() {
        TelephonyManager manager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String devId = Long.toHexString(Long.parseLong(manager.getDeviceId()));
        if (mPrefs.getBoolean("pref_my_device_only", false) && !devId.equals(mDevId)) {
            Toast.makeText(this, R.string.appexec_launch_protect_from_other_device, Toast.LENGTH_LONG)
                    .show();
            return false;
        }
        return true;
    }

    // Return executable intent using hash code
    private Intent getLaunchIntentHash(String hashCode) {
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> activityList = mPm.queryIntentActivities(intent, 0 );
        for (ResolveInfo info : activityList) {
            AppsInfo appInfo = new AppsInfo(mPm, info);
            if (hashCode != null && hashCode.equals(appInfo.hashcode)) return appInfo.intent;
        }
        return null;
    }

    // Return executable intent using Serial value of package name
    private Intent getLaunchIntentPackage(String pkg) {
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> activityList = mPm.queryIntentActivities(intent, 0 );
        for (ResolveInfo info : activityList) {
            AppsInfo appInfo = new AppsInfo(mPm, info);
            if (pkg != null && pkg.equals(mAR.getSerial(appInfo.PackageName))) return appInfo.intent;
        }
        return null;
    }
}
