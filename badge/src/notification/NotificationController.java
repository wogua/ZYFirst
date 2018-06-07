package notification;

import android.content.Context;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.android.launcher3.Launcher;
import com.android.launcher3.Utilities;
import com.android.launcher3.util.ComponentKey;
import com.android.launcher3.util.MultiHashMap;
import com.android.zylauncher.badge.BadgeController;
import com.android.zylauncher.badge.BadgeInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by  on 2018/4/16.
 */

public class NotificationController implements NotificationListener.NotificationsChangedListener {
    private static final boolean LOGD = false;
    private static final String TAG = "NotificationController";

    private final Launcher mLauncher;

    private Map<Integer, BadgeInfo> mPackageUserToBadgeInfos = new HashMap<>();

    public NotificationController(Launcher launcher) {
        mLauncher = launcher;
    }

    @Override
    public void onNotificationPosted(PackageUserKey postedPackageUserKey,
                                     NotificationKeyData notificationKey, boolean shouldBeFilteredOut) {
        if (isSystemApp(postedPackageUserKey, mLauncher.getApplicationContext())) {
            return;
        }
        BadgeInfo badgeInfo = mPackageUserToBadgeInfos.get(postedPackageUserKey.hashCode());
        boolean badgeShouldBeRefreshed;
        if (badgeInfo == null) {
            if (!shouldBeFilteredOut) {
                BadgeInfo newBadgeInfo = new BadgeInfo(postedPackageUserKey);
                newBadgeInfo.addOrUpdateNotificationKey(notificationKey);
                mPackageUserToBadgeInfos.put(postedPackageUserKey.hashCode(), newBadgeInfo);
                badgeShouldBeRefreshed = true;
            } else {
                badgeShouldBeRefreshed = false;
            }
        } else {
            badgeShouldBeRefreshed = shouldBeFilteredOut
                    ? badgeInfo.removeNotificationKey(notificationKey)
                    : badgeInfo.addOrUpdateNotificationKey(notificationKey);
            if (badgeInfo.getNotificationKeys().size() == 0) {
                mPackageUserToBadgeInfos.remove(postedPackageUserKey.hashCode());
            }
        }
        updateLauncherIconBadges(Utilities.singletonHashSet(postedPackageUserKey.hashCode()),
                badgeShouldBeRefreshed);
    }

    @Override
    public void onNotificationRemoved(PackageUserKey removedPackageUserKey,
                                      NotificationKeyData notificationKey) {
        if (isSystemApp(removedPackageUserKey, mLauncher.getApplicationContext())) {
            return;
        }
        BadgeInfo oldBadgeInfo = mPackageUserToBadgeInfos.get(removedPackageUserKey.hashCode());
        if (oldBadgeInfo != null && oldBadgeInfo.removeNotificationKey(notificationKey)) {
            if (oldBadgeInfo.getNotificationKeys().size() == 0) {
                mPackageUserToBadgeInfos.remove(removedPackageUserKey);
            }
            updateLauncherIconBadges(Utilities.singletonHashSet(removedPackageUserKey.hashCode()));
        }
    }

    @Override
    public void onNotificationFullRefresh(List<StatusBarNotification> activeNotifications) {
        if (activeNotifications == null) return;
        // This will contain the PackageUserKeys which have updated badges.
        HashMap<Integer, BadgeInfo> updatedBadges = new HashMap<>(mPackageUserToBadgeInfos);
        mPackageUserToBadgeInfos.clear();
        for (StatusBarNotification notification : activeNotifications) {
            PackageUserKey packageUserKey = PackageUserKey.fromNotification(notification);
            if (isSystemApp(packageUserKey, mLauncher.getApplicationContext())) {
                continue;
            }
            BadgeInfo badgeInfo = mPackageUserToBadgeInfos.get(packageUserKey.hashCode());
            if (badgeInfo == null) {
                badgeInfo = new BadgeInfo(packageUserKey);
                mPackageUserToBadgeInfos.put(packageUserKey.hashCode(), badgeInfo);
            }
            badgeInfo.addOrUpdateNotificationKey(NotificationKeyData
                    .fromNotification(notification));
        }

        // Add and remove from updatedBadges so it contains the PackageUserKeys of updated badges.
        for (Integer packageUserKeyHash : mPackageUserToBadgeInfos.keySet()) {
            BadgeInfo prevBadge = updatedBadges.get(packageUserKeyHash);
            BadgeInfo newBadge = mPackageUserToBadgeInfos.get(packageUserKeyHash);
            if (prevBadge == null) {
                updatedBadges.put(packageUserKeyHash, newBadge);
            } else {
                if (prevBadge.shouldBeInvalidated(newBadge)) {
                    updatedBadges.remove(packageUserKeyHash);
                }
            }
        }

        if (!updatedBadges.isEmpty()) {
            updateLauncherIconBadges(updatedBadges.keySet());
        }
    }

    private void updateLauncherIconBadges(Set<Integer> updatedBadges) {
        updateLauncherIconBadges(updatedBadges, true);
    }

    /**
     * Updates the icons on launcher (workspace, folders, all apps) to refresh their badges.
     *
     * @param updatedBadges The packages whose badges should be refreshed (either a notification was
     *                      added or removed, or the badge should show the notification icon).
     * @param shouldRefresh An optional parameter that will allow us to only refresh badges that
     *                      have actually changed. If a notification updated its content but not
     *                      its count or icon, then the badge doesn't change.
     */
    private void updateLauncherIconBadges(Set<Integer> updatedBadges,
                                          boolean shouldRefresh) {
        Iterator<Integer> iterator = updatedBadges.iterator();
        while (iterator.hasNext()) {
            Integer packageUserKey = iterator.next();
            BadgeInfo badgeInfo = mPackageUserToBadgeInfos.get(packageUserKey);
            if (badgeInfo != null && !shouldRefresh) {
                // The notification icon isn't used, and the badge hasn't changed
                // so there is no update to be made.
                iterator.remove();
            }
        }
        if (!updatedBadges.isEmpty()) {
            updateBadgesForThirdPartApps(updatedBadges);
        }
    }

    public void cancelNotification(String notificationKey) {
        NotificationListener notificationListener = NotificationListener.getInstanceIfConnected();
        if (notificationListener == null) {
            return;
        }
        notificationListener.cancelNotification(notificationKey);
    }

    private void updateBadgesForThirdPartApps(Set<Integer> updatedBadges) {
        ArrayList<BadgeInfo> badgeInfos = new ArrayList<>();
        Iterator<Integer> iterator = updatedBadges.iterator();
        while (iterator.hasNext()) {
            Integer packageUserKey = iterator.next();
            BadgeInfo badgeInfo = mPackageUserToBadgeInfos.get(packageUserKey);
            if (badgeInfo != null) {
                badgeInfo.checkValue();
                badgeInfos.add(badgeInfo);
            }
        }
        mLauncher.bindWorkspaceUnreadInfo(badgeInfos,false);
    }


    private static boolean isSystemApp(PackageUserKey packageUserKey, Context context) {
        return Utilities.isSystemApp(packageUserKey.mPackageName, context);
    }
}
