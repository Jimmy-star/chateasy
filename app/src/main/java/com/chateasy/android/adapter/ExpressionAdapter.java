package com.chateasy.android.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.chateasy.android.R;

import java.util.List;

/**
 * Created by Administrator on 2022/1/23.
 */

public class ExpressionAdapter extends ArrayAdapter<String> {
    public ExpressionAdapter(Context context, int textViewResourceId, List<String> objects){
        super(context, textViewResourceId, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        if(convertView != null){
            convertView = View.inflate(getContext(), R.layout.layout_expression_gridview, null);
        }
        ImageView imageView = (ImageView) convertView.findViewById(R.id.iv_expression);
        String filename = getItem(position);
        int resId = getContext().getResources().getIdentifier(filename, "mipmap", getContext().getPackageName());
        imageView.setImageResource(resId);
        return convertView;
    }

}
