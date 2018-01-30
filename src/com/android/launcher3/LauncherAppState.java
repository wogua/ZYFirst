/*
 * Copyright (C) 2013 The Android Open Source Project
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
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Process;
import android.util.Log;

import com.android.zylauncher.badge.BadgeController;
import com.android.launcher3.colors.ColorManager;
import com.android.launcher3.compat.LauncherAppsCompat;
import com.android.launcher3.compat.PackageInstallerCompat;
import com.android.launcher3.compat.UserManagerCompat;
import com.android.launcher3.config.ProviderConfig;
import com.android.launcher3.dynamicui.ExtractionUtils;
import com.android.launcher3.logging.FileLog;
import com.android.launcher3.shortcuts.DeepShortcutManager;
import com.android.launcher3.shortcuts.ShortcutCache;
import com.android.launcher3.util.ConfigMonitor;
import com.android.launcher3.util.TestingUtils;
import com.android.launcher3.util.Thunk;

import java.lang.ref.WeakReference;

import static com.android.launcher3.colors.ColorManager.CC_TEXT_BLACK_DEFAULT_COLOR;

public class LauncherAppState {

    public static final boolean PROFILE_STARTUP = ProviderConfig.IS_DOGFOOD_BUILD;

    private final AppFilter mAppFilter;
    @Thunk final LauncherModel mModel;
    private final IconCache mIconCache;
    private final WidgetPreviewLoader mWidgetCache;
    private final DeepShortcutManager mDeepShortcutManager;

    @Thunk boolean mWallpaperChangedSinceLastCheck;

    private static WeakReference<LauncherProvider> sLauncherProvider;
    private final BadgeController mBadgeController;//lijun add for unread
    private static Context sContext;

    private static LauncherAppState INSTANCE;

    private InvariantDeviceProfile mInvariantDeviceProfile;

    private static Object lock = new Object();

    public static LauncherAppState getInstance() {
        synchronized (lock) {
            if (INSTANCE == null) {
                Log.e("IconManager", "LauncherAppState Tid: " + Process.myTid());
                INSTANCE = new LauncherAppState();
            }
        }
        return INSTANCE;
    }

    public static LauncherAppState getInstanceNoCreate() {
        return INSTANCE;
    }

    public Context getContext() {
        return sContext;
    }

    static void setLauncherProvider(LauncherProvider provider) {
        if (sLauncherProvider != null) {
            Log.w(Launcher.TAG, "setLauncherProvider called twice! old=" +
                    sLauncherProvider.get() + " new=" + provider);
        }
        sLauncherProvider = new WeakReference<>(provider);

        // The content provider exists for the entire duration of the launcher main process and
        // is the first component to get created. Initializing application context here ensures
        // that LauncherAppState always exists in the main process.
        sContext = provider.getContext().getApplicationContext();
        FileLog.setDir(sContext.getFilesDir());
    }

    private LauncherAppState() {
        if (sContext == null) {
            throw new IllegalStateException("LauncherAppState inited before app context set");
        }

        Log.v(Launcher.TAG, "LauncherAppState inited");

        if (TestingUtils.MEMORY_DUMP_ENABLED) {
            TestingUtils.startTrackingMemory(sContext);
        }

        mInvariantDeviceProfile = new InvariantDeviceProfile(sContext);
        mIconCache = new IconCache(sContext, mInvariantDeviceProfile);
        mWidgetCache = new WidgetPreviewLoader(sContext, mIconCache);
        mDeepShortcutManager = new DeepShortcutManager(sContext, new ShortcutCache());

        mAppFilter = AppFilter.loadByName(sContext.getString(R.string.app_filter_class));
        mModel = new LauncherModel(this, mIconCache, mAppFilter, mDeepShortcutManager);

        LauncherAppsCompat.getInstance(sContext).addOnAppsChangedCallback(mModel);

        // Register intent receivers
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_LOCALE_CHANGED);
        // For handling managed profiles
        filter.addAction(Intent.ACTION_MANAGED_PROFILE_ADDED);
        filter.addAction(Intent.ACTION_MANAGED_PROFILE_REMOVED);
        filter.addAction(Intent.ACTION_MANAGED_PROFILE_AVAILABLE);
        filter.addAction(Intent.ACTION_MANAGED_PROFILE_UNAVAILABLE);
        filter.addAction(Intent.ACTION_MANAGED_PROFILE_UNLOCKED);
        filter.addAction("com.hb.theme.ACTION_THEME_CHANGE");
        filter.addAction("com.zylauncher.ACTION_LAYOUT_CHANGE");
        // For extracting colors from the wallpaper
        if (Utilities.isNycOrAbove()) {
            // TODO: add a broadcast entry to the manifest for pre-N.
            filter.addAction(Intent.ACTION_WALLPAPER_CHANGED);
        }

        sContext.registerReceiver(mModel, filter);

        //lijun add for unread start
        mBadgeController = new BadgeController();
        IntentFilter filter1 = new IntentFilter();
        filter1.addDataScheme("package");
        filter1.addAction(Intent.ACTION_PACKAGE_DATA_CLEARED);
        filter1.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter1.addAction("com.mediatek.intent.action.SETTINGS_PACKAGE_DATA_CLEARED");
        sContext.registerReceiver(mBadgeController, filter1);
        //lijun add for unread end

        UserManagerCompat.getInstance(sContext).enableAndResetCache();
        if (Utilities.ATLEAST_KITKAT) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Intent.ACTION_WALLPAPER_CHANGED);
            sContext.registerReceiver(new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent intent) {
                    mWallpaperChangedSinceLastCheck = true;
                    if(Intent.ACTION_WALLPAPER_CHANGED.equals(intent.getAction())){
                        ColorManager.getInstance().dealWallpaperForLauncher(sContext);//lijun add for wallpaper changed
                    }
                }
            }, intentFilter);
        }
        new ConfigMonitor(sContext).register();
        getColorFromSharedPreferences(sContext);

        ExtractionUtils.startColorExtractionServiceIfNecessary(sContext);
        ColorManager.getInstance().dealWallpaperForLauncher(sContext);//lijun add for wallpaper changed

        //lijun add for unistall shortcut
        ContentResolver resolver = sContext.getContentResolver();
        resolver.registerContentObserver(LauncherSettings.Favorites.CONTENT_URI, true,
                mFavoritesObserver);
    }

    private void getColorFromSharedPreferences(Context sContext) {
        int color= sContext.getSharedPreferences("com.android.launcher3.prefs",Context.MODE_PRIVATE).getInt("default_text_color",CC_TEXT_BLACK_DEFAULT_COLOR);
        ColorManager.getInstance().setColors(color);
    }

    /**
     * Call from Application.onTerminate(), which is not guaranteed to ever be called.
     */
    public void onTerminate() {
        sContext.unregisterReceiver(mModel);
        sContext.unregisterReceiver(mBadgeController);//lijun add for unread
        //lijun add for uninstall shortcut start
        ContentResolver resolver = sContext.getContentResolver();
        resolver.unregisterContentObserver(mFavoritesObserver);
        //lijun add for uninstall shortcut end
        final LauncherAppsCompat launcherApps = LauncherAppsCompat.getInstance(sContext);
        launcherApps.removeOnAppsChangedCallback(mModel);
        PackageInstallerCompat.getInstance(sContext).onStop();
    }

    /**
     * Reloads the workspace items from the DB and re-binds the workspace. This should generally
     * not be called as DB updates are automatically followed by UI update
     */
    public void reloadWorkspace() {
        mModel.resetLoadedState(false, true);
        mModel.startLoaderFromBackground();
    }

    LauncherModel setLauncher(Launcher launcher) {
        sLauncherProvider.get().setLauncherProviderChangeListener(launcher);
        mModel.initialize(launcher);
        return mModel;
    }

    /**
     * lijun add for unread
     */
    public BadgeController initBadgeProvider(Launcher launcher) {
        mBadgeController.initialize(launcher);
        return mBadgeController;
    }

    public IconCache getIconCache() {
        return mIconCache;
    }

    public LauncherModel getModel() {
        return mModel;
    }

    public WidgetPreviewLoader getWidgetCache() {
        return mWidgetCache;
    }

    public DeepShortcutManager getShortcutManager() {
        return mDeepShortcutManager;
    }

    public boolean hasWallpaperChangedSinceLastCheck() {
        boolean result = mWallpaperChangedSinceLastCheck;
        mWallpaperChangedSinceLastCheck = false;
        return result;
    }

    public InvariantDeviceProfile getInvariantDeviceProfile() {
        return mInvariantDeviceProfile;
    }

    /**
     * lijun add for clear icon database as theme thanged
     */
    public void clearIcons(){
        mIconCache.clearIcons();
        mWidgetCache.clearIcons();
    }

    //lijun add for uninstall shortcut
    private final ContentObserver mFavoritesObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            // If the database has ever changed, then we really need to force a reload of the
            // workspace on the next load
//            mModel.resetLoadedState(false, true);
//            mModel.startLoaderFromBackground();
        }
    };
}
