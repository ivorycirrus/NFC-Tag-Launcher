
package com.lgcns.sce.nfc;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class RegisterAppDatabaseAdapter extends RegisterApp {

    private static final String TAG = "RegisterAppDatabaseAdapter";

    private SQLiteDatabase mDb;
    private DatabaseHelper mDbHelper;
    private final Context context;

    private static final String DATABASE_NAME = "AppList";
    private static final String TABLE_NAME = "RegisteredApps";
    private static final String[] COLUMNS = {
            "_id", "hashcode", "package"
    };
    private static final int DATABASE_VERSION = 2;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        private static final String DATABASE_CREATE = "create table " + TABLE_NAME + " ( " + COLUMNS[0]
                + " integer primary key autoincrement , " + COLUMNS[1] + " text not null , " + COLUMNS[2] + " text );";

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_NAME);
            onCreate(db);
        }
    }

    public RegisterAppDatabaseAdapter(Context _context) {
        this.context = _context;
        mDbHelper = null;
        mDb = null;
    }

    public void open() throws SQLException {
        open(false);
    }

    public void open(boolean readOnly) throws SQLException {
        if (mDbHelper == null) {
            mDbHelper = new DatabaseHelper(context);
            if (readOnly) {
                mDb = mDbHelper.getReadableDatabase();
            } else {
                mDb = mDbHelper.getWritableDatabase();
            }
        }
    }

    public void close() {
        if (mDbHelper != null) {
            mDbHelper.close();
            mDbHelper = null;
        }
    }

    public boolean registerApps(String hashcode) {
        open(true);
        Log.i(TAG, "Register App ##  HashCode : " + hashcode);
        ContentValues content = new ContentValues();
        content.put(COLUMNS[1], hashcode);
        try {
            if (mDb.insert(TABLE_NAME, " ", content) == -1) {
                return false;
            } else {
                return true;
            }
        } finally {
            close();
        }
    }

    @Override
    public boolean registerApps(String hashCode, String pkg) {
        open(true);
        Log.i(TAG, "Register App ##  HashCode : " + hashCode);
        ContentValues content = new ContentValues();
        content.put(COLUMNS[1], hashCode);
        content.put(COLUMNS[2], pkg);
        try {
            if (mDb.insert(TABLE_NAME, " ", content) == -1) {
                return false;
            } else {
                return true;
            }
        } finally {
            close();
        }
    }

    public boolean unregisterAppsHash(String hashcode) {
        open(true);
        Log.i(TAG, "Unregister App ## HashCode : " + hashcode);
        try {
            if (mDb.delete(TABLE_NAME, COLUMNS[1] + " = " + hashcode, null) == 0) {
                return false;
            } else {
                return true;
            }
        } finally {
            close();
        }
    }

    @Override
    public boolean unregisterAppsPkg(String Package) {
        open(true);
        Log.i(TAG, "Unregister App ## HashCode : " + Package);
        try {
            if (mDb.delete(TABLE_NAME, COLUMNS[2] + " = " + Package, null) == 0) {
                return false;
            } else {
                return true;
            }
        } finally {
            close();
        }
    }

    public void unregisterAll() {
        open(true);
        Log.i(TAG, "Unregister All Apps");
        try {
            mDb.delete(TABLE_NAME, COLUMNS[0] + " is not null ", null);
        } finally {
            close();
        }
    }

    public ArrayList<String> retrieveApps() {
        open();
        try {
            ArrayList<String> hashcodes = new ArrayList<String>();
            Cursor c = mDb.query(TABLE_NAME, COLUMNS, null, null, null, null, null);
            if (c == null || c.getCount() == 0) return null;
            c.moveToFirst();
            while (!c.isAfterLast()) {
                hashcodes.add(c.getString(1));
                c.moveToNext();
            }
            return hashcodes;
        } finally {
            close();
        }
    }

    @Override
    public ArrayList<String[]> retrieveExecInfo() {
        // TODO Auto-generated method stub
        open();
        try {
            ArrayList<String[]> ret = new ArrayList<String[]>();
            Cursor c = mDb.query(TABLE_NAME, COLUMNS, null, null, null, null, null);
            if (c == null || c.getCount() == 0) return null;
            c.moveToFirst();
            while (!c.isAfterLast()) {
                ret.add(new String[]{c.getString(1),getSerial(c.getString(2))});
                c.moveToNext();
            }
            return ret;
        } finally {
            close();
        }
    }
}
