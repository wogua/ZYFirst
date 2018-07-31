package com.android.zylauncher.badge;

import android.graphics.Shader;

import java.util.ArrayList;
import java.util.List;

import notification.NotificationKeyData;
import notification.PackageUserKey;

/**
 * Created by  on 17-9-20.
 */

public class BadgeInfo {
    public int id;
    public String pkgName;
    public String shortcutCustomId;
    public int badgeCount;
    public int creator;
    public String lastModifyTime;

    public static final int MAX_COUNT = 999;

    private PackageUserKey mPackageUserKey;
    private List<NotificationKeyData> mNotificationKeys;
    private int mTotalCount;

    public BadgeInfo() {

    }

    public BadgeInfo(PackageUserKey packageUserKey) {
        mPackageUserKey = packageUserKey;
        mNotificationKeys = new ArrayList<>();
    }

    public PackageUserKey getmPackageUserKey() {
        return mPackageUserKey;
    }

    @Override
    public String toString() {
        return "id:" + id + ", pkgName:" + pkgName + ", shortcutCustomId:" + shortcutCustomId + ", badgeCount:" + badgeCount + ", creator:" + creator + ", lastModifyTime:" + lastModifyTime;
    }

    public boolean addOrUpdateNotificationKey(NotificationKeyData notificationKey) {
        int indexOfPrevKey = mNotificationKeys.indexOf(notificationKey);
        NotificationKeyData prevKey = indexOfPrevKey == -1 ? null
                : mNotificationKeys.get(indexOfPrevKey);
        if (prevKey != null) {
            if (prevKey.count == notificationKey.count) {
                return false;
            }
            // Notification was updated with a new count.
            mTotalCount -= prevKey.count;
            mTotalCount += notificationKey.count;
            prevKey.count = notificationKey.count;
            return true;
        }
        boolean added = mNotificationKeys.add(notificationKey);
        if (added) {
            mTotalCount += notificationKey.count;
        }
        return added;
    }

    /**
     * Returns whether the notification was removed (false if it didn't exist).
     */
    public boolean removeNotificationKey(NotificationKeyData notificationKey) {
        boolean removed = mNotificationKeys.remove(notificationKey);
        if (removed) {
            mTotalCount -= notificationKey.count;
        }
        return removed;
    }

    public List<NotificationKeyData> getNotificationKeys() {
        return mNotificationKeys;
    }

    public int getNotificationCount() {
        return Math.min(mTotalCount, MAX_COUNT);
    }

    public boolean shouldBeInvalidated(BadgeInfo newBadge) {
        return mPackageUserKey.equals(newBadge.mPackageUserKey)
                && getNotificationCount() != newBadge.getNotificationCount();
    }

    public void checkValue() {
        if (pkgName == null && mPackageUserKey != null) {
            pkgName = mPackageUserKey.mPackageName;
        }
        if(shortcutCustomId == null){
            shortcutCustomId = Badge.DEFAULT_SHORTCUT_CUSTOM_ID;
        }
        badgeCount = getNotificationCount();
    }
}
