package com.android.launcher3;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by lijun on 2018/1/29.
 */

public class PreferenceWithTip extends ListPreference {

    String pTitle = null;
    String tipstring = null;


    public PreferenceWithTip(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.PreferenceWithTip);
        tipstring = ta.getString(R.styleable.PreferenceWithTip_tipstring);
        pTitle = ta.getString(R.styleable.PreferenceWithTip_titlestring);
        ta.recycle();
    }

    public PreferenceWithTip(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PreferenceWithTip(Context context) {
        this(context, null);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        TextView pTitleView = (TextView)view.findViewById(R.id.prefs_title);
        pTitleView.setText(pTitle);
        TextView pTipView = (TextView)view.findViewById(R.id.prefs_tip);
        pTipView.setText(tipstring);
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        return LayoutInflater.from(getContext()).inflate(R.layout.settings_layout_preference,
                parent, false);
    }
}
