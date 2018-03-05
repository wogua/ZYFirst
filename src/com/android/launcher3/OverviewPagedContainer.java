package com.android.launcher3;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.android.launcher3.wallpaperpicker.WallpaperPagedView;
import com.android.launcher3.wallpaperpicker.WallpaperPicker;

import java.util.List;

public class OverviewPagedContainer extends FrameLayout {
    OverviewPagedView mPagedView;

    public OverviewPagedContainer(Context context) {
        this(context, null);
    }

    public OverviewPagedContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OverviewPagedContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public OverviewPagedContainer(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initView();
    }

    private void initView() {
//        mPagedView = (OverviewPagedView) findViewById(R.id.overview_pagedview);
//        mPagedView.initParentViews(this);
    }
}
