package com.android.launcher3.theme;

import android.content.Context;
import android.content.pm.LauncherActivityInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;

import com.android.launcher3.compat.LauncherActivityInfoCompat;

import java.lang.reflect.InvocationTargetException;
import java.util.Locale;

public class IconProvider {

    private static final boolean DBG = true;
    protected static final String TAG = "IconProvider";

    protected String mSystemState;

    public IconProvider() {
        updateSystemStateString();
    }

    public static IconProvider loadByName(String className, Context context) {
        if (TextUtils.isEmpty(className)) return new IconProvider();
        if (DBG) Log.d(TAG, "Loading IconProvider: " + className);
        try {
            Class<?> cls = Class.forName(className);
            return (IconProvider) cls.getDeclaredConstructor(Context.class).newInstance(context);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                | ClassCastException | NoSuchMethodException | InvocationTargetException e) {
            Log.e(TAG, "Bad IconProvider class", e);
            return new IconProvider();
        }
    }

    public void updateSystemStateString() {
        mSystemState = Locale.getDefault().toString();
    }

    public String getIconSystemState(String packageName) {
        return mSystemState;
    }


    public Drawable getIcon(LauncherActivityInfoCompat info, int iconDpi) {
        return info.getIcon(iconDpi);
    }
    public Drawable getIcon(LauncherActivityInfo info, int iconDpi) {
        return info.getIcon(iconDpi);
    }
    public Bitmap normalizeIcons(Bitmap bitmap) {
        return null;
    }
    public Drawable getIconFromManager(Resources resources, String iconName, int resId){
       return null;
    }
    public Integer getColor(String name , int resId ,Resources res){
        return res.getColor(resId);
    }
    public Float getDimens(String name , int resId , Resources res){
        return res.getDimension(resId);
    }
    public  Drawable getIconFromManager(Context context, String iconName, int resId){
        return getIconFromManager(context.getResources(), iconName,resId);//loadByName(context.getResources().getString(R.string.icon_provider_class),context).getIconFromManager(context.getResources(),iconName,resId);
    }
    public Bitmap getIconFromPackageName(String packageName, UserHandle user){
        return null;
    }
}
