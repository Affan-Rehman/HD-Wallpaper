package com.amazia_iwallcraft.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.amazia_iwallcraft.wallpaper.R;

import java.util.ArrayList;

public class SpinAdapter extends BaseAdapter {

    ArrayList<String> arrayList;
    LayoutInflater layoutInflater;

    public SpinAdapter(Context context, int textViewResourceId, ArrayList<String> arrayList) {

        this.arrayList = arrayList;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount(){
        return arrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View rowview = layoutInflater.inflate(R.layout.layout_spinner, null,true);

        TextView txtTitle = (TextView) rowview.findViewById(R.id.tv_spinner);
        txtTitle.setText(arrayList.get(position));
        return rowview;
    }

    // And here is when the "chooser" is popped up
    // Normally is the same view, but you can customize it if you want
//    @Override
//    public View getDropDownView(int position, View convertView, ViewGroup parent) {
//        TextView label = (TextView) super.getDropDownView(position, convertView, parent);
//        label.setTextColor(Color.BLACK);
//        label.setText(values[position].getName());
//
//        return label;
//    }
}