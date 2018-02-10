package com.android.launcher3.customcontent;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.launcher3.R;

import java.util.List;

/**
 * Created by lijun on 18-2-8.
 */

public class CardListAdapter extends BaseAdapter {

    private List<CardInfo> mData;
    private final LayoutInflater mLayoutInflater;

    public CardListAdapter(Context context, List<CardInfo> data) {
        mData = data;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;

        if (convertView == null) {
            view = mLayoutInflater.inflate(R.layout.custom_card_item, parent, false);
        } else {
            view = convertView;
        }
        ImageView icon = (ImageView) view.findViewById(R.id.card_icon);
        TextView title = (TextView) view.findViewById(R.id.card_title);
        TextView content = (TextView) view.findViewById(R.id.card_content);

        CardInfo cardInfo = mData.get(position);
        icon.setImageBitmap(cardInfo.icon);
        title.setText(cardInfo.title);
        content.setText("自定义卡片");
        return view;
    }
}
