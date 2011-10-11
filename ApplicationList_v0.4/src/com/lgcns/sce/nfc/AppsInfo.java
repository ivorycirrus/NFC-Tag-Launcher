/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lgcns.sce.nfc;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.util.Log;

class AppsInfo   {
    private static final String TAG = "AppsInfo";

    /**
     * The application name.
     */
    CharSequence title;
    
    Intent intent;

    Drawable icon;
 
    long firstInstallTime;

    ComponentName componentName;

    String PackageName;

    String hashcode;

    AppsInfo() {

    }



    public AppsInfo(AppsInfo info) {
  
        componentName = info.componentName;
        title = info.title.toString();
        PackageName = info.PackageName;
        icon=info.icon; 
        firstInstallTime = info.firstInstallTime;
        hashcode=info.hashcode; 
 
    }

    /**
     * Creates the application intent based on a component name and various launch flags.
     * Sets {@link #itemType} to {@link LauncherSettings.BaseLauncherColumns#ITEM_TYPE_APPLICATION}.
     *
     * @param className the class name of the component representing the intent
     * @param launchFlags the launch flags
     */
    final void setActivity(ComponentName className, int launchFlags) {
        intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setComponent(className);
        intent.setFlags(launchFlags);
 
    }

    public AppsInfo(PackageManager pm, ResolveInfo info) {
 
        final ActivityInfo activityInfo = info.activityInfo;
        final String packageName = activityInfo.applicationInfo.packageName;
        final CharSequence titlelabel = activityInfo.loadLabel(pm);
        final Drawable dicon=info.activityInfo.loadIcon(pm);
        
        this.componentName = new ComponentName(packageName, activityInfo.name);
        this.setActivity(componentName,
                Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);


        try {
            title = titlelabel;
            PackageName = packageName;
            firstInstallTime = pm.getPackageInfo(packageName, 0).firstInstallTime;
            icon=dicon;
            hashcode=String.valueOf(componentName.hashCode());
            
        } catch (NameNotFoundException e) {
            Log.d(TAG, "PackageManager.getApplicationInfo failed for " + packageName);
        }
    }


}
