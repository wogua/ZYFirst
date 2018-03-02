
package com.android.launcher3.specialeffectpreview;



import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;


import com.android.launcher3.R;
import com.android.launcher3.pageindicators.PageIndicatorUnderline;

public class PreviewContainer extends LinearLayout {
    private static final String TAG = "PreviewContainer";

    EffectPreviewPagedView mPagedView;

    @SuppressLint("NewApi")
    public PreviewContainer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public PreviewContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PreviewContainer(Context context) {
        this(context, null, 0);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

		mPagedView = (EffectPreviewPagedView) this.findViewById(R.id.special_effect_paged_view_content);
		mPagedView.initParentViews(this);

    }


   public void initPagedView(){
           mPagedView.addEffectViews();
   }

}
