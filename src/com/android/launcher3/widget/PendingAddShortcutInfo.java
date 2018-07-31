/*
 * Copyright (C) 2015 The Android Open Source Project
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
package com.android.launcher3.widget;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.pm.ActivityInfo;
import android.content.pm.ShortcutInfo;
import android.os.Build;

import com.android.launcher3.LauncherSettings;
import com.android.launcher3.PendingAddItemInfo;
import com.android.launcher3.compat.ShortcutConfigActivityInfo;

/**
 * Meta data used for late binding of the short cuts.
 *
 * @see {@link PendingAddItemInfo}
 */
public class PendingAddShortcutInfo extends PendingAddItemInfo {

    ActivityInfo activityInfo = null;
    public ShortcutConfigActivityInfo activityInfoSC;

    public PendingAddShortcutInfo(ActivityInfo activityInfo) {
        this.activityInfo = activityInfo;
        this.activityInfoSC = null;

        componentName = new ComponentName(activityInfo.packageName, activityInfo.name);
        itemType = LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT;
    }

    public PendingAddShortcutInfo(ShortcutConfigActivityInfo activityInfo) {
        this.activityInfo = null;
        this.activityInfoSC = activityInfo;
        componentName = activityInfo.getComponent();
        itemType = activityInfo.getItemType();
    }
}
