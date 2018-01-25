package com.android.launcher3.theme;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.UserHandle;
import android.util.Log;

import com.android.launcher3.LauncherAppState;
import com.android.launcher3.compat.LauncherActivityInfoCompat;

/**
 * Created by lijun on 17-3-14.
 */
public class TestIconProvider extends IconProvider {
    IconManager mIconManager;
    TestIconProvider(Context context){
        mIconManager = IconManager.getInstance(context.getApplicationContext());
        if (mIconManager.issLauncherNeedCleanCaches()) {
            mIconManager.setsLauncherNeedCleanCaches(false,context.getApplicationContext());
            LauncherAppState launcherAppState = LauncherAppState.getInstance();
            if (launcherAppState != null) {
                launcherAppState.clearIcons();
            }
        }
    }

    public Drawable getIcon(LauncherActivityInfoCompat info, int iconDpi) {
        Drawable result=null;
        if(mIconManager!=null){
            Log.i(TAG,"getIcon   1");
            result = mIconManager.getIconDrawable(info.getComponentName(),null);
        }
        if(result==null){
            Log.i(TAG,"getIcon 2");
            result = info.getIcon(iconDpi);
        }
        Log.i(TAG,"getIcon 3");
        return result;
    }
    public Bitmap normalizeIcons(Bitmap bitmap) {
        return mIconManager.normalizeIcons(bitmap);
    }
    public Drawable getIconFromManager(Resources resources, String iconName, int resId){
        return  mIconManager.getIconFromManager(resources, iconName,resId);
    }
    public Integer getColor(String name , int resId ,Resources res){
        Integer color = mIconManager.getColor(name);
        if(color==null){
        return res.getColor(resId);
        }else {
            return color;
        }
    }

    public Float getDimens(String name, int resId, Resources res) {
        Float dimens = mIconManager.getDimens(name);
        if (dimens == null) {
            return  res.getDimension(resId);
        } else {
            float density = 0;
            if (res != null) {
                density = res.getDisplayMetrics().density;
            } else {
                density = 3;
            }
            return dimens * density;
        }
    }

    @Override
    public Bitmap getIconFromPackageName(String packageName,UserHandle user) {
        return mIconManager.getIconByLBEPackage(packageName);
    }
}
