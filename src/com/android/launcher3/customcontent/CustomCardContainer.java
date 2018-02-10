package com.android.launcher3.customcontent;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.android.launcher3.R;
import com.android.launcher3.theme.utils.PhotoUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lijun on 18-2-8.
 */

public class CustomCardContainer extends LinearLayout implements View.OnClickListener{

    CustomCardListView mList;
    CardListAdapter mAdapter;
    View mSearchView;

    public CustomCardContainer(Context context) {
        this(context, null);
    }

    public CustomCardContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomCardContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initList();
    }

    private void initList() {
        Context context = getContext();
        mList = (CustomCardListView) findViewById(R.id.custom_card_listView);
        List<CardInfo> cardInfoList = new ArrayList<>();
        Bitmap testIcon = PhotoUtils.drawable2bitmap(context.getResources().getDrawable(R.drawable.calendar_card));
        testIcon = PhotoUtils.zoom(testIcon,80,80);
        for (int i = 0; i < 10; i++) {
            CardInfo cardInfo = new CardInfo();
            cardInfo.title = "Test";
            cardInfo.icon = testIcon;
            cardInfoList.add(cardInfo);
        }

        mAdapter = new CardListAdapter(context, cardInfoList);
        mList.setAdapter(mAdapter);

        mSearchView = findViewById(R.id.searchView);
        mSearchView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v == mSearchView){
            Intent intent = new Intent("android.search.action.GLOBAL_SEARCH");
            getContext().startActivity(intent);
        }
    }
}
