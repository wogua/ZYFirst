package com.android.zylauncher.badge;

import com.android.launcher3.notification.PackageUserKey;

/**
 * Created by lijun on 17-9-20.
 */

public class BadgeInfo {
    public int id;
    public String pkgName;
    public String shortcutCustomId;
    public int badgeCount;
    public int creator;
    public String lastModifyTime;

    public BadgeInfo(PackageUserKey postedPackageUserKey) {

    }

    @Override
    public String toString() {
        return "id:" + id + ", pkgName:" + pkgName + ", shortcutCustomId:" + shortcutCustomId + ", badgeCount:" + badgeCount + ", creator:" + creator + ", lastModifyTime:" + lastModifyTime;
    }


}
