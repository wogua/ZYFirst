package com.android.launcher3;

import android.animation.TimeInterpolator;
import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;

import com.android.launcher3.pageindicators.PageIndicatorUnderline;

public class OverviewPagedView extends PagedView{

    private static final int ROW_SIZE = 4;

    private Launcher mLauncher;

    public OverviewPagedView(Context context) {
        this(context, null);
    }

    public OverviewPagedView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OverviewPagedView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        if (context instanceof Launcher) {
            mLauncher = (Launcher) context;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        final int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
        }

        mViewport.set(0, 0, widthSize, heightSize);
        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void getEdgeVerticalPostion(int[] pos) {
        View view = getChildAt(0);
        if (view != null) {
            pos[0] = view.getTop();
            pos[1] = view.getBottom();
        }
    }

    @Override
    protected void snapToPage(int whichPage, int delta, int duration, boolean immediate, TimeInterpolator interpolator) {
        if (mPageIndicator != null) {
            ((PageIndicatorUnderline) mPageIndicator).animateToAlpha(0f);
        }
        super.snapToPage(whichPage, delta, duration, immediate, interpolator);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (mPageIndicator != null) {
            mPageIndicator.setScroll(l, mMaxScrollX);
        }
    }

    @Override
    protected void determineScrollingStart(MotionEvent ev, float touchSlopScale) {
        super.determineScrollingStart(ev, touchSlopScale);
        if (mPageIndicator != null && mTouchState != TOUCH_STATE_SCROLLING) {
            ((PageIndicatorUnderline) mPageIndicator).animateToAlpha(1.0f);
        }
    }

    @Override
    protected void resetTouchState() {
        super.resetTouchState();
        ((PageIndicatorUnderline) mPageIndicator).animateToAlpha(0.0f);
    }
}
