package com.android.launcher3.customcontent;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.android.launcher3.R;
import com.android.launcher3.theme.utils.PhotoUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lijun on 18-2-8.
 */

public class CustomCardContainer extends LinearLayout {

    CustomCardListView mList;
    CardListAdapter mAdapter;

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
        Bitmap testIcon = PhotoUtils.drawable2bitmap(context.getResources().getDrawable(R.drawable.ic_note));
        for (int i = 0; i < 10; i++) {
            CardInfo cardInfo = new CardInfo();
            cardInfo.title = "自定义卡片";
            cardInfo.icon = testIcon;
            cardInfoList.add(cardInfo);
        }

        mAdapter = new CardListAdapter(cardInfoList);
        mList.setAdapter(mAdapter);
    }

}
