
package com.lgcns.sce.nfc;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

/**
 * @class AppList Show Installed Application list, and manage application list
 *        to quick launch by tag read.
 */
public class AppList extends Activity {
    static final String TAG = "APPListActivity";
    private PackageManager mPm;
    private RegisterApp mAR;

    private ArrayList<AppsInfo> registeredApps = new ArrayList<AppsInfo>();
    private ArrayList<AppsInfo> defaultApps = new ArrayList<AppsInfo>();

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPm = getPackageManager();

        // Initialize some window features.
        requestWindowFeature(Window.FEATURE_RIGHT_ICON);
        requestWindowFeature(Window.FEATURE_PROGRESS);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.applist);

        // Show registered applications
        // mAR = new RegisterAppPreferenceAdapter(this);
        mAR = new RegisterAppDatabaseAdapter(this);

        // Show application informations.
        initAppList();
        initRegisteredApps();

    }

    // ArrayAdapter for showing application informations.
    @SuppressWarnings("hiding")
    class AllAppListAdapter<AppsInfo> extends ArrayAdapter<AppsInfo> {
        List<AppsInfo> appList;
        int textViewResourceId;
        Context c;

        public AllAppListAdapter(Context context, int textViewResourceId, List<AppsInfo> itemList) {
            super(context, textViewResourceId, itemList);
            this.appList = itemList;
            this.textViewResourceId = textViewResourceId;
            this.c = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;

            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.app_item_list, null);
            }

            com.lgcns.sce.nfc.AppsInfo app = (com.lgcns.sce.nfc.AppsInfo) appList.get(position);

            if (app != null) {
                ImageView icon = (ImageView) v.findViewById(R.id.app_icon);
                TextView name = (TextView) v.findViewById(R.id.app_name);
                TextView pkg = (TextView) v.findViewById(R.id.package_name);
                if (icon != null) {
                    icon.setImageDrawable(app.icon);
                    name.setText(app.title.toString());
                    pkg.setText(app.PackageName);
                }
            }

            return v;
        }

    }

    // Initialize registered application informations.
    private void initRegisteredApps() {
        ArrayList<String> regApps = mAR.retrieveApps();
        ListView lv_reg_app_list = (ListView) findViewById(R.id.lv_reg_app_list);
        TextView tv_reg = (TextView) findViewById(R.id.reg_title);
        if (regApps != null) {
            lv_reg_app_list.setVisibility(View.VISIBLE);
            tv_reg.setVisibility(View.VISIBLE);
            registeredApps.clear();
            final int length = defaultApps.size();
            for (int k = 0; k < length; ++k) {
                AppsInfo info = defaultApps.get(k);
                for (String hashCode : regApps) {
                    if (hashCode.equals(info.hashcode)) {
                        registeredApps.add(info);                        
                    }
                }

            }
            AllAppListAdapter<AppsInfo> appRegListAdapter = new AllAppListAdapter<AppsInfo>(this,
                    R.layout.app_item_list, registeredApps);
            lv_reg_app_list.setAdapter(appRegListAdapter);
            lv_reg_app_list.setOnItemLongClickListener(mRegisteredAppListClickListener);
            lv_reg_app_list.setOnItemClickListener(mUnregisterListener);
            appRegListAdapter.notifyDataSetChanged();
        } else {
            lv_reg_app_list.setVisibility(View.GONE);
            tv_reg.setVisibility(View.GONE);
        }
    }

    // Initialize application informations.
    private void initAppList() {
        // Retrieve application list.

        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> activityList = mPm.queryIntentActivities(intent, 0);
        for (ResolveInfo info : activityList) {
            final ActivityInfo activityInfo = info.activityInfo;
            final ComponentName infoComponent = new ComponentName(activityInfo.packageName, activityInfo.name);
            AppsInfo appInfo = new AppsInfo(mPm, info);
            if (!findActivity(defaultApps, infoComponent)) {
                defaultApps.add(appInfo);
            }
        }
        Collections.sort(defaultApps, APP_NAME_COMPARATOR);

        AllAppListAdapter<AppsInfo> appListAdapter = new AllAppListAdapter<AppsInfo>(this, R.layout.app_item_list,
                defaultApps);
        ListView lv_app_list = (ListView) findViewById(R.id.lv_app_list);
        lv_app_list.setAdapter(appListAdapter);
        lv_app_list.setOnItemClickListener(mAppListClickListener);
    }

    private static boolean findActivity(ArrayList<AppsInfo> apps, ComponentName component) {
        final int N = apps.size();
        for (int i = 0; i < N; i++) {
            final AppsInfo info = apps.get(i);
            if (info.componentName.equals(component)) {
                return true;
            }
        }
        return false;
    }

    // Show information dialog when registered application list clicked.
    private OnItemLongClickListener mRegisteredAppListClickListener = new OnItemLongClickListener() {
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            final com.lgcns.sce.nfc.AppsInfo app = (com.lgcns.sce.nfc.AppsInfo) registeredApps.get(position);            
            ShowWriteToTagDialog(R.string.applist_write_tag, app); 
            return true;
        }
    };

    // Show information dialog when application list clicked.
    private OnItemClickListener mAppListClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
            final com.lgcns.sce.nfc.AppsInfo app = (com.lgcns.sce.nfc.AppsInfo) defaultApps.get(position);
            AlertDialog.Builder dialog = new AlertDialog.Builder(arg1.getContext());
            dialog.setTitle(app.title);
            dialog.setIcon(app.icon);
            dialog.setMessage(R.string.applist_register_app);
            dialog.setPositiveButton(R.string.applist_register, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (app.intent == null) {
                        sorry();
                    } else {
                        mAR.registerApps(app.hashcode, app.PackageName);
                        initRegisteredApps();
                        dialog.dismiss();
                        ShowWriteToTagDialog(R.string.applist_register_and_write, app);
                    }
                }
            });
            dialog.setNegativeButton(R.string.applist_cancel, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            dialog.show();
        }
    };

    // Show unregiater app. dialog when registered application list long
    // clicked.
    private OnItemClickListener mUnregisterListener = new OnItemClickListener() {

        public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
            final com.lgcns.sce.nfc.AppsInfo app = (com.lgcns.sce.nfc.AppsInfo) registeredApps.get(position);
            AlertDialog.Builder dialog = new AlertDialog.Builder(arg1.getContext());
            dialog.setTitle(app.title);
            dialog.setIcon(app.icon);
            dialog.setMessage(R.string.applist_unregister_app);
            dialog.setPositiveButton(R.string.applist_unregister, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (app.hashcode != null) {
                        mAR.unregisterAppsHash(app.hashcode);
                    } else if (app.PackageName != null) {
                        mAR.unregisterAppsPkg(app.PackageName);
                    }
                    initRegisteredApps();
                    dialog.dismiss();
                }
            });
            dialog.setNegativeButton(R.string.applist_cancel, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            dialog.show();
        }

    };
    
    //to Show 'confirm to Write Tag' dialog
    private void ShowWriteToTagDialog(int message,final com.lgcns.sce.nfc.AppsInfo app)
    {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(app.title);
        dialog.setIcon(app.icon);
        dialog.setMessage(message);
        dialog.setPositiveButton(R.string.applist_write_tag_button, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (app.intent == null) {
                    sorry();
                } else {
                    writeToTag(app.hashcode, app.PackageName);
                    dialog.dismiss();
                }
            }
        });
        dialog.setNegativeButton(R.string.applist_cancel, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    // Create Option Menu
    public boolean onCreateOptionsMenu(android.view.Menu menu) {

        menu.add(0, 1, 0, R.string.applist_menu_remove_all).setIcon(android.R.drawable.ic_menu_delete);
        menu.add(0, 2, 0, R.string.applist_menu_help).setIcon(android.R.drawable.ic_menu_help);
        menu.add(0, 3, 0,  R.string.applist_menu_configuration).setIcon(android.R.drawable.ic_menu_preferences);

        return true;
    };

    // Event Processing on Option item selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                dialog.setTitle(R.string.applist_menu_remove_all);
                dialog.setMessage(R.string.applist_remove_all_apps);
                dialog.setPositiveButton(R.string.applist_remove_all, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mAR.unregisterAll();
                        initRegisteredApps();
                    }
                });
                dialog.setNegativeButton(R.string.applist_cancel, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                dialog.show();
                break;
            case 2:
                startActivity(new Intent(this, Help.class));
                break;
            case 3:
                configuration();
                break;
            default:
        }

        return true;
    }

    // Show alert message when wrong app. information read.
    private void sorry() {
        Toast.makeText(this, R.string.applist_no_intent, Toast.LENGTH_SHORT);
    }

    // Write registered app. information to tag.
    private void writeToTag(String hashcode ,String pkg) {
        Intent intent = new Intent(this, WriteToTag.class);        
        intent.putExtra(WriteToTag.HASHCODE, hashcode);
        intent.putExtra(WriteToTag.PACKAGE_NAME, mAR.getSerial(pkg));
        startActivity(intent);
    }

    // Go to Configuration Menu
    private void configuration() {
        Intent intent = new Intent(this, Configure.class);
        startActivity(intent);
    }

    // Comparators to Compare Application informations.
    private static final Collator sCollator = Collator.getInstance();
    public static final Comparator<AppsInfo> APP_NAME_COMPARATOR = new Comparator<AppsInfo>() {
        public final int compare(AppsInfo a, AppsInfo b) {
            return sCollator.compare(a.title.toString(), b.title.toString());
        }
    };    
    public static final Comparator<AppsInfo> APP_INSTALL_TIME_COMPARATOR = new Comparator<AppsInfo>() {
        public final int compare(AppsInfo a, AppsInfo b) {
            if (a.firstInstallTime < b.firstInstallTime) return 1;
            if (a.firstInstallTime > b.firstInstallTime) return -1;
            return 0;
        }
    };

}
