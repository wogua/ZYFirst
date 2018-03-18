package com.android.launcher3.overviewtab;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.launcher3.Launcher;
import com.android.launcher3.R;

/**
 * Created by lijun on 2018/3/17.
 */

public class MTabLayout extends LinearLayout {

    Launcher mLauncher;
    int tabCount;
    int width;
    int currentTab;
    private int indicatorNorColor;
    private int indicatorSelectColor;

    public MTabLayout(Context context) {
        this(context, null);
    }

    public MTabLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MTabLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mLauncher = (Launcher) context;
        indicatorSelectColor = Color.parseColor("#ff00ffff");
        indicatorNorColor = Color.WHITE;
        currentTab = -1;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (getChildCount() > 0) {
            onTabChanged(0);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = View.MeasureSpec.getSize(widthMeasureSpec);
        tabCount = getChildCount();
    }

    public void onTabChanged(int index) {
        if (currentTab == -1 || currentTab != index) {
            ((TextView) getChildAt(index)).setTextColor(indicatorSelectColor);
            if (currentTab != -1) {
                ((TextView) getChildAt(currentTab)).setTextColor(indicatorNorColor);
            }
            currentTab = index;
        }
    }

    public void onTabChanged(View view) {
        int index = -1;
        for (int i = 0; i < tabCount; i++) {
            if (view == getChildAt(i)) {
                index = i;
            }
        }
        if (index >= 0) {
            onTabChanged(index);
        }
    }

    public boolean isSelect(View view) {
        for (int i = 0; i < tabCount; i++) {
            if (view == getChildAt(i) && i == currentTab) {
                return true;
            }
        }
        return false;
    }

    public View getCurrentTab() {
        if (currentTab < 0 || currentTab >= getChildCount()) {
            return null;
        }
        return getChildAt(currentTab);
    }
}
