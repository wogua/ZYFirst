package com.android.launcher3.overviewtab;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.android.launcher3.Launcher;

/**
 * Created by lijun on 2018/3/18.
 */

public class TabContent extends FrameLayout {

    Launcher mLauncher;

    public TabContent(@NonNull Context context) {
        this(context, null);
    }

    public TabContent(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TabContent(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mLauncher = (Launcher) context;
    }

    public void showTabContent(View view, boolean anime) {
        if(view == null){
            return;
        }
        View child;
        View lastView = null;
        for (int i = 0; i < getChildCount(); i++) {
            child = getChildAt(i);
            if (child.getVisibility() == View.VISIBLE) {
                lastView = child;
            }
        }
        if (!anime || lastView == null ) {
            view.setVisibility(View.VISIBLE);
            if (lastView != null) {
                lastView.setVisibility(View.GONE);
            }
        } else {
            if (view == lastView) return;
            mLauncher.animateBettenTabs(lastView, view);
        }
    }

    public void init(View firstView){
        View child;
        for (int i = 0; i < getChildCount(); i++) {
            child = getChildAt(i);
            if (firstView == child) {
                firstView.setVisibility(VISIBLE);
                firstView.setAlpha(1.0f);
            }else {
                child.setVisibility(GONE);
                child.setAlpha(0.0f);
            }
        }
    }

    public void reset(View firstView) {
        View child;
        for (int i = 0; i < getChildCount(); i++) {
            child = getChildAt(i);
            if (firstView == child) {
                firstView.setVisibility(VISIBLE);
                firstView.setAlpha(1.0f);
                firstView.setTranslationY(0);
            }else {
                child.setVisibility(GONE);
                child.setAlpha(0.0f);
            }
        }
    }
}
