package com.lgcns.sce.nfc;

import java.io.IOException;
import java.nio.charset.Charset;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

/**
 * @class WriteToTag
 * 
 * Write quick launch information to NFC tag.
 *
 *  ** payload Structure ** 
 *   |--------------|-----------------|-------------|
 *      Device ID        Hash Code         Serial             
 *     (Fix 8byte)      (Fix 4byte)     (Min. 3byte)     
 *     
 *  ** Minimum requirement of storage **
 *     Device ID(8byte) + Hash Code(4byte) + Serial(3byte)      
 *       = payload size is more than 15byte
 */
public class WriteToTag extends Activity {
    
    public static final String HASHCODE = "HashCode";
    public static final String PACKAGE_NAME = "PackageName";
    
    private static final String TAG = "APPList";
    private static final String mimeType = "a/l";
    
    private NfcAdapter mAdapter;
    private PendingIntent mPendingIntent;
    private NdefMessage mMessage;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.write_tag);
        
        mAdapter = NfcAdapter.getDefaultAdapter(this);
        if(mAdapter==null)
        {
            Toast.makeText(this, "Sorry. This device is not support NFC.", 0).show();
            finish();
        }
        
        Bundle extras = getIntent().getExtras();
        
        TelephonyManager manager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        
        
        if (extras == null) {
            setStatus(R.string.writetotag_no_data, true);
            return;
        }
        else
        {
            // Get data from intent.
            long devId = Long.parseLong(manager.getDeviceId());
            int hashCode = Integer.parseInt(extras.getString(HASHCODE));
            String pkg = extras.getString(PACKAGE_NAME);
            
            Log.d(TAG, "Message : "+extras.getString(HASHCODE)+" | "+extras.getString(PACKAGE_NAME));
            Log.d(TAG, "Message : "+devId+" | "+hashCode);
            
            // Prepare data to write tag
            byte[][] data = new byte[3][];
            data[0] = ByteUtils.longToByteArray(devId);
            data[1] = ByteUtils.intToByteArray(hashCode);
            data[2] = pkg.getBytes(Charset.forName("US_ASCII"));
            
            // Make NDEF data
            byte[] payload = ByteUtils.byteMerge(data);
            byte[] type = mimeType.getBytes(Charset.forName("US_ASCII"));            
            NdefRecord rec = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,type,new byte[0],payload);
            mMessage = new NdefMessage(new NdefRecord[]{rec});
        }
        
        mPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        mAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
    }

    @Override
    public void onNewIntent(Intent intent) {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);        
        if (mMessage != null) {
            writeTag(tag);
        } else {
            setStatus(R.string.writetotag_not_ready, false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mAdapter.disableForegroundDispatch(this);
    }

    // Show a message of a result of writing tag
    private void setStatus(int string, boolean isSuccess) {
        Toast.makeText(this, string, Toast.LENGTH_SHORT).show();
        if(isSuccess) 
        {
            Log.d(TAG, "Write complete.");
            finish();        
        }
    }
    
    // Write NDEF message to tag 
    boolean writeTag(Tag tag) {
        try {
            Ndef ndef = Ndef.get(tag);
            Log.d(TAG, "Tag Capacity : "+ndef.getMaxSize());
            if (ndef != null) {
                ndef.connect();

                if (!ndef.isWritable()) {
                    setStatus(R.string.writetotag_readonly, false);
                    return false;
                }                
                ndef.writeNdefMessage(mMessage);
                setStatus(R.string.writetotag_write_complete, true);
                return true;
            } else {
                NdefFormatable format = NdefFormatable.get(tag);
                if (format != null) {
                    try {
                        format.connect();
                        format.format(mMessage);
                        setStatus(R.string.writetotag_write_complete, true);
                        return true;
                    } catch (IOException e) {
                        setStatus(R.string.writetotag_format_fail, false);
                        return false;
                    }
                } else {
                    setStatus(R.string.writetotag_ndef_not_supported, false);
                    return false;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to write tag", e);
        }

        setStatus(R.string.writetotag_write_failure, false);
        return false;
    }

}
