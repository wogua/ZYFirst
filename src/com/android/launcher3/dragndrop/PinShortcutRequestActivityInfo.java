/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.android.launcher3.dragndrop;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.LauncherApps;
import android.content.pm.LauncherApps.PinItemRequest;
import android.content.pm.ShortcutInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Process;
import android.support.annotation.Nullable;

import com.android.launcher3.FastBitmapDrawable;
import com.android.launcher3.IconCache;
import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.LauncherModel;
import com.android.launcher3.LauncherSettings;
import com.android.launcher3.R;
import com.android.launcher3.compat.ShortcutConfigActivityInfo;
import com.android.launcher3.compat.UserHandleCompat;
import com.android.launcher3.graphics.LauncherIcons;
import com.android.launcher3.shortcuts.ShortcutInfoCompat;
import com.android.launcher3.util.LooperExecutor;

/**
 * Extension of ShortcutConfigActivityInfo to be used in the confirmation prompt for pin item
 * request.
 */
@TargetApi(Build.VERSION_CODES.O)
class PinShortcutRequestActivityInfo extends ShortcutConfigActivityInfo {

    // Class name used in the target component, such that it will never represent an
    // actual existing class.
    private static final String DUMMY_COMPONENT_CLASS = "pinned-shortcut";

    private final PinItemRequest mRequest;
    private final ShortcutInfo mInfo;
    private final Context mContext;

    public PinShortcutRequestActivityInfo(PinItemRequest request, Context context) {
        super(new ComponentName(request.getShortcutInfo().getPackage(), DUMMY_COMPONENT_CLASS),
                request.getShortcutInfo().getUserHandle());
        mRequest = request;
        mInfo = request.getShortcutInfo();
        mContext = context;
    }

    @Override
    public int getItemType() {
        return LauncherSettings.Favorites.ITEM_TYPE_DEEP_SHORTCUT;
    }

    @Override
    public CharSequence getLabel() {
        return mInfo.getShortLabel();
    }

    @Override
    public Drawable getFullResIcon(IconCache cache) {
        Drawable d = mContext.getSystemService(LauncherApps.class)
                .getShortcutIconDrawable(mInfo, LauncherAppState.getInstance().getInvariantDeviceProfile().fillResIconDpi);
        if (d == null) {
            d = new FastBitmapDrawable(cache.getDefaultIcon(UserHandleCompat.myUserHandle()));
        }
        return d;
    }

    @Override
    public com.android.launcher3.ShortcutInfo createShortcutInfo() {
        // Total duration for the drop animation to complete.
        long duration = mContext.getResources().getInteger(R.integer.config_dropAnimMaxDuration) +
                Launcher.EXIT_SPRINGLOADED_MODE_SHORT_TIMEOUT +
                mContext.getResources().getInteger(R.integer.config_overlayTransitionTime) / 2;
        // Delay the actual accept() call until the drop animation is complete.
        return createShortcutInfoFromPinItemRequest(
                mContext, mRequest, duration);
    }

    @Override
    public boolean startConfigActivity(Activity activity, int requestCode) {
        return false;
    }

    @Override
    public boolean isPersistable() {
        return false;
    }

    public static com.android.launcher3.ShortcutInfo createShortcutInfoFromPinItemRequest(
            Context context, final PinItemRequest request, final long acceptDelay) {
        if (request != null &&
                request.getRequestType() == PinItemRequest.REQUEST_TYPE_SHORTCUT &&
                request.isValid()) {

            if (acceptDelay <= 0) {
                if (!request.accept()) {
                    return null;
                }
            } else {
                // Block the worker thread until the accept() is called.
                new LooperExecutor(LauncherModel.getWorkerLooper()).execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(acceptDelay);
                        } catch (InterruptedException e) {
                            // Ignore
                        }
                        if (request.isValid()) {
                            request.accept();
                        }
                    }
                });
            }

            ShortcutInfoCompat compat = new ShortcutInfoCompat(request.getShortcutInfo());
            com.android.launcher3.ShortcutInfo info = new com.android.launcher3.ShortcutInfo(compat, context);
            // Apply the unbadged icon and fetch the actual icon asynchronously.
            info.iconBitmap = LauncherIcons
                    .createShortcutIcon(compat, context, false /* badged */);
            LauncherAppState.getInstance().getModel()
                    .updateShortcutInfo(compat, info);
            return info;
        } else {
            return null;
        }
    }
}
