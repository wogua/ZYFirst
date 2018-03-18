package com.android.launcher3.overviewtab;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

/**
 * Created by lijun on 2018/3/17.
 */

public class MTabLayout extends LinearLayout {

    int tabCount;
    int width;
    int currentTab;

    public MTabLayout(Context context) {
        super(context);
    }

    public MTabLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MTabLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = View.MeasureSpec.getSize(widthMeasureSpec);
        tabCount=getChildCount();
    }

}
