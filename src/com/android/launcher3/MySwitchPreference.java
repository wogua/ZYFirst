package com.android.launcher3;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.SwitchPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import com.android.zylauncher.badge.BadgeController;

/**
 * Created by lijun on 2018/3/9.
 */

public class MySwitchPreference extends SwitchPreference {

    private Switch mSwitch;
    SharedPreferences sharedPreferences;

    public MySwitchPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        sharedPreferences = Utilities.getPrefs(getContext());
    }

    public MySwitchPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr,0);
    }

    public MySwitchPreference(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    @Override
    public View getView(View convertView, ViewGroup parent) {
        return super.getView(convertView, parent);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        mSwitch = (Switch) view.findViewById(R.id.switch_btn);
        initSwitch();
        mSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = mSwitch.isChecked();
                if(SpecialEffectPagedView.SPECIAL_EFFECT_CYCLE_SLIDE.equals(getKey())){
                    sharedPreferences.edit().putBoolean(SpecialEffectPagedView.SPECIAL_EFFECT_CYCLE_SLIDE, checked).commit();
                }else if(BadgeController.BADGE_PREFERENCE_KEY.equals(getKey())){
                    sharedPreferences.edit().putBoolean(BadgeController.BADGE_PREFERENCE_KEY, checked).commit();
                }
            }
        });

    }

    private void initSwitch(){
        boolean checked = false;
        if(SpecialEffectPagedView.SPECIAL_EFFECT_CYCLE_SLIDE.equals(getKey())){
            checked = Utilities.getPrefs(getContext()).getBoolean(SpecialEffectPagedView.SPECIAL_EFFECT_CYCLE_SLIDE, true);
        }else if(BadgeController.BADGE_PREFERENCE_KEY.equals(getKey())){
            checked = Utilities.getPrefs(getContext()).getBoolean(BadgeController.BADGE_PREFERENCE_KEY, true);
        }
        mSwitch.setChecked(checked);
    }
}
