package com.cafeom.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cafeom.data.IdNameData;

public class IdNameSpinnerAdapter extends ArrayAdapter<IdNameData> {

    IdNameData [] mData;

    public IdNameSpinnerAdapter(@NonNull Context context, int resource, @NonNull IdNameData[] objects) {
        super(context, resource, objects);
        mData = objects;
    }

    @Override
    public int getCount() {
        return mData.length;
    }

    @Nullable
    @Override
    public IdNameData getItem(int position) {
        return mData[position];
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        TextView v = (TextView) super.getView(position, convertView, parent);
        v.setText(mData[position].f_name);
        return v;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        TextView v = (TextView) super.getDropDownView(position, convertView, parent);
        v.setText(mData[position].f_name);
        return v;
    }

    public int getItemPosition(String s) {
        for (int i = 0; i < mData.length; i++) {
            if (mData[i].f_name.toLowerCase().equals(s.toLowerCase())) {
                return i;
            }
        }
        return -1;
    }
}