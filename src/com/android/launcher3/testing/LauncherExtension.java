package com.android.launcher3.testing;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.android.launcher3.AppInfo;
import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherCallbacks;
import com.android.launcher3.R;
import com.android.launcher3.allapps.AllAppsSearchBarController;
import com.android.launcher3.logging.UserEventDispatcher;
import com.android.launcher3.util.ComponentKey;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a very trivial LauncherExtension. It primarily serves as a simple
 * class to exercise the LauncherOverlay interface.
 */
public class LauncherExtension extends Launcher {

    //------ Activity methods -------//
    @Override
    public void onCreate(Bundle savedInstanceState) {
        setLauncherCallbacks(new LauncherExtensionCallbacks());
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        if(mWorkspace.isCustomContentShowing()){
            mWorkspace.snapToPage(mWorkspace.numCustomPages());
        }
        super.onResume();
    }

    public class LauncherExtensionCallbacks implements LauncherCallbacks {

        private View customContent = null;

        @Override
        public void preOnCreate() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
        }

        @Override
        public void preOnResume() {
        }

        @Override
        public void onResume() {
        }

        @Override
        public void onStart() {
        }

        @Override
        public void onStop() {
        }

        @Override
        public void onPause() {
        }

        @Override
        public void onDestroy() {
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
        }

        @Override
        public void onPostCreate(Bundle savedInstanceState) {
        }

        @Override
        public void onNewIntent(Intent intent) {
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, String[] permissions,
                int[] grantResults) {
        }

        @Override
        public void onWindowFocusChanged(boolean hasFocus) {
        }

        @Override
        public boolean onPrepareOptionsMenu(Menu menu) {
            return false;
        }

        @Override
        public void dump(String prefix, FileDescriptor fd, PrintWriter w, String[] args) {
        }

        @Override
        public void onHomeIntent() {
        }

        @Override
        public boolean handleBackPressed() {
            return false;
        }

        @Override
        public void onTrimMemory(int level) {
        }

        @Override
        public void onLauncherProviderChange() {
        }

        @Override
        public void finishBindingItems(boolean upgradePath) {
        }

        @Override
        public void bindAllApplications(ArrayList<AppInfo> apps) {
        }

        @Override
        public void onWorkspaceLockedChanged() {
        }

        @Override
        public void onInteractionBegin() {
        }

        @Override
        public void onInteractionEnd() {
        }

        @Override
        public boolean startSearch(String initialQuery, boolean selectInitialQuery,
                Bundle appSearchData) {
            return false;
        }

        CustomContentCallbacks mCustomContentCallbacks = new CustomContentCallbacks() {

            // Custom content is completely shown. {@code fromResume} indicates whether this was caused
            // by a onResume or by scrolling otherwise.
            public void onShow(boolean fromResume) {
                Log.d("lijun22","left onShow");
                startWecommunity();
                LauncherExtension.this.isStartActivityToLeftCustom = true;
            }

            // Custom content is completely hidden
            public void onHide() {
                Log.d("lijun22","left onHide");
            }

            // Custom content scroll progress changed. From 0 (not showing) to 1 (fully showing).
            public void onScrollProgressChanged(float progress) {
                Log.d("lijun22","left onScrollProgressChanged progress = "+progress);
                customContent.setAlpha(progress);
            }

            // Indicates whether the user is allowed to scroll away from the custom content.
            public boolean isScrollingAllowed() {
                return true;
            }

        };

        public void startWecommunity() {
            Intent intent = new Intent("android.intent.action.MAIN");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addCategory("android.intent.category.LAUNCHER");
            intent.setComponent(new ComponentName("com.zhiyun.coin", "com.zhiyun.coin.ui.launch.LaunchActivity"));
            try {
                LauncherExtension.this.startActivity(intent);
            } catch (ActivityNotFoundException | SecurityException e) {
                Log.e(TAG, "Unable to startWecommunity intent=" + intent);
            }
        }

        @Override
        public boolean hasCustomContentToLeft() {
            return true;
        }

        @Override
        public void populateCustomContentContainer() {
//            FrameLayout customContent = new FrameLayout(LauncherExtension.this);
//            customContent.setBackgroundColor(Color.GRAY);

            customContent = getLayoutInflater().inflate(R.layout.custom_content_container,null);
            addToCustomContentPage(customContent, mCustomContentCallbacks, "");
        }

        @Override
        public UserEventDispatcher getUserEventDispatcher() { return null; }

        @Override
        public View getQsbBar() {
            return null;
        }

        @Override
        public Bundle getAdditionalSearchWidgetOptions() {
            return new Bundle();
        }

        @Override
        public boolean shouldMoveToDefaultScreenOnHomeIntent() {
            return true;
        }

        @Override
        public boolean hasSettings() {
            return false;
        }

        @Override
        public AllAppsSearchBarController getAllAppsSearchBarController() {
            return null;
        }

        @Override
        public List<ComponentKey> getPredictedApps() {
            // To debug app predictions, enable AlphabeticalAppsList#DEBUG_PREDICTIONS
            return new ArrayList<>();
        }

        @Override
        public int getSearchBarHeight() {
            return SEARCH_BAR_HEIGHT_NORMAL;
        }

        @Override
        public void setLauncherSearchCallback(Object callbacks) {
            // Do nothing
        }

        @Override
        public void onAttachedToWindow() {
        }

        @Override
        public void onDetachedFromWindow() {
        }

        @Override
        public boolean shouldShowDiscoveryBounce() {
            return false;
        }
    }
}
