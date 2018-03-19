package com.android.launcher3;

import android.animation.LayoutTransition;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

/**
 * Created by lijun on 17-3-31.
 */
public class ArrangeNavLinearLayout extends LinearLayout {
    Paint paint = new Paint(Color.WHITE);
    Launcher mLauncher;
    private LayoutTransition mLayoutTransition;
    public ArrangeNavLinearLayout(Context context) {
        this(context,null);
    }

    public ArrangeNavLinearLayout(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public ArrangeNavLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr,0);
    }

    public ArrangeNavLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mLauncher = (Launcher) context;
    }
    public void enableLayoutTransitions() {
        LayoutTransition transition = getLayoutTransition();
        transition.disableTransitionType(LayoutTransition.APPEARING);
        transition.disableTransitionType(LayoutTransition.DISAPPEARING);
        transition.enableTransitionType(LayoutTransition.CHANGE_APPEARING);
        transition.enableTransitionType(LayoutTransition.CHANGE_DISAPPEARING);
    }

    public void disableLayoutTransitions() {
        LayoutTransition transition = getLayoutTransition();
        transition.disableTransitionType(LayoutTransition.APPEARING);
        transition.disableTransitionType(LayoutTransition.DISAPPEARING);
        transition.disableTransitionType(LayoutTransition.CHANGE_APPEARING);
        transition.disableTransitionType(LayoutTransition.CHANGE_DISAPPEARING);
    }
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        paint.setTextSize(50);
    }

    public void measureChlid() {
        int childCount = getChildCount();
        int paddingTop = 0;
        for(int i = 0 ; i<childCount ;i++){
            View childAt = getChildAt(i);
            if(childAt!=null) {
                if (i == 0) {
                    paddingTop = childAt.getPaddingTop();

                } else {
                    childAt.setPadding(getPaddingLeft(), paddingTop, getPaddingRight(), getPaddingBottom());
                }
            }
        }
    }

    @Override
    public void onViewRemoved(View child) {
        super.onViewRemoved(child);
        if(getChildCount() == 0){
            mLauncher.showHideAppEmptyHint(true);
        }
    }

    @Override
    public void onViewAdded(View child) {
        super.onViewAdded(child);
        if(getChildCount() > 0){
            mLauncher.showHideAppEmptyHint(false);
        }
    }

    public void initEmptyHint(boolean tabHided) {
        mLauncher.showHideAppEmptyHint(!tabHided && getChildCount() <= 0);
    }
}
