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

package com.android.launcher3;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;

public class UninstallShortcutReceiver extends BroadcastReceiver {
    public static final String TAG = "UninstallShortcut";

    private static final String ACTION_UNINSTALL_SHORTCUT =
            "com.android.launcher.action.UNINSTALL_SHORTCUT";

    // The set of shortcuts that are pending uninstall
    private static ArrayList<PendingUninstallShortcutInfo> mUninstallQueue =
            new ArrayList<PendingUninstallShortcutInfo>();

    // Determines whether to defer uninstalling shortcuts immediately until
    // disableAndFlushUninstallQueue() is called.
    private static boolean mUseUninstallQueue = false;

    private static class PendingUninstallShortcutInfo {
        Intent data;

        public PendingUninstallShortcutInfo(Intent rawData) {
            data = rawData;
        }
    }

    public void onReceive(Context context, Intent data) {
        Log.d(TAG, "UninstallShortcutReceiver ");
        Intent intent  = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
        if (intent==null||!ACTION_UNINSTALL_SHORTCUT.equals(data.getAction()) || !isDualIntent(intent)) {
            return;
        }

        PendingUninstallShortcutInfo info = new PendingUninstallShortcutInfo(data);
        if (info != null) {
            mUninstallQueue.add(info);
        }
        mUninstallQueue.add(info);
        disableAndFlushUninstallQueue(context);
    }

    static void enableUninstallQueue() {
        mUseUninstallQueue = true;
    }

    static void disableAndFlushUninstallQueue(Context context) {
        mUseUninstallQueue = false;
        Iterator<PendingUninstallShortcutInfo> iter = mUninstallQueue.iterator();
        while (iter.hasNext()) {
            processUninstallShortcut(context, iter.next());
            iter.remove();
        }
    }

    private static void processUninstallShortcut(Context context,
                                                 PendingUninstallShortcutInfo pendingInfo) {
        SharedPreferences sharedPrefs = context.getSharedPreferences(LauncherFiles.SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);

        final Intent data = pendingInfo.data;

        LauncherAppState app = LauncherAppState.getInstance();
        synchronized (app) {
            removeShortcut(context, data, sharedPrefs);
        }
    }

    public static boolean isDualIntent(Intent intent) {
        if(intent ==null) return false;
        ComponentName componentName = intent.getComponent();
        if (componentName != null && "com.vapp.aide.intl".equals(componentName.getPackageName())
                && "com.lbe.parallel.ui.LaunchDelegateActivity".equals(componentName.getClassName())) {
            return true;
        }
        return false;

    }

    private static void removeShortcut(Context context, Intent data,
                                       final SharedPreferences sharedPrefs) {
        Intent intent = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
        String name = data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
        boolean duplicate = true;

        if (intent != null && name != null) {
            final ContentResolver cr = context.getContentResolver();
            Cursor c = cr.query(LauncherSettings.Favorites.CONTENT_URI,
                    new String[]{LauncherSettings.Favorites._ID, LauncherSettings.Favorites.INTENT},
                    LauncherSettings.Favorites.TITLE + "=?", new String[]{name}, null);

            final int intentIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.INTENT);
            final int idIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites._ID);

            boolean changed = false;
            final boolean isDaulIntent = isDualIntent(intent);
            try {
                while (c.moveToNext()) {
                    try {
                        boolean removeDualShortcut = false;
                        Intent it = Intent.parseUri(c.getString(intentIndex), 0);
                        if (isDaulIntent && isDualIntent(it)) {
                            String realPkg = intent.getStringExtra("EXTRA_LAUNCH_PACKAGE");
                            String realPkg1 = it.getStringExtra("EXTRA_LAUNCH_PACKAGE");
                            if (realPkg != null && realPkg.equals(realPkg1)) {
                                removeDualShortcut = true;
                            }
                        }
                        if (intent.filterEquals(Intent.parseUri(c.getString(intentIndex), 0)) || removeDualShortcut) {
                            final long id = c.getLong(idIndex);
                            final Uri uri = LauncherSettings.Favorites.getContentUri(id);
                            cr.delete(uri, null, null);
                            changed = true;
                            if (!duplicate) {
                                break;
                            }
                        }
                    } catch (URISyntaxException e) {
                        // Ignore
                        Log.e(TAG, "removeShortcut URISyntaxException : " + e.toString());
                    } catch (Exception e) {
                        Log.e(TAG, "removeShortcut Exception : " + e.toString());
                    }
                }
            } finally {
                c.close();
            }

            if (changed) {
                cr.notifyChange(LauncherSettings.Favorites.CONTENT_URI, null);
               LauncherAppState.getInstance().getModel().checkDualItem(intent);
                Toast.makeText(context, context.getString(R.string.shortcut_uninstalled, name),
                        Toast.LENGTH_SHORT).show();
            }

            // Remove any items due to be animated
//            boolean appRemoved;
//            Set<String> newApps = new HashSet<String>();
//            newApps = sharedPrefs.getStringSet(InstallShortcutReceiver.NEW_APPS_LIST_KEY, newApps);
//            synchronized (newApps) {
//                do {
//                    appRemoved = newApps.remove(intent.toUri(0).toString());
//                } while (appRemoved);
//            }
//            if (appRemoved) {
//                final Set<String> savedNewApps = newApps;
//                new Thread("setNewAppsThread-remove") {
//                    public void run() {
//                        synchronized (savedNewApps) {
//                            SharedPreferences.Editor editor = sharedPrefs.edit();
//                            editor.putStringSet(InstallShortcutReceiver.NEW_APPS_LIST_KEY,
//                                    savedNewApps);
//                            if (savedNewApps.isEmpty()) {
//                                // Reset the page index if there are no more items
//                                editor.putInt(InstallShortcutReceiver.NEW_APPS_PAGE_KEY, -1);
//                            }
//                            editor.commit();
//                        }
//                    }
//                }.start();
//            }
        }
    }
}
