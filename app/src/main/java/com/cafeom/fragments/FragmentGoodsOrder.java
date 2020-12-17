package com.cafeom.fragments;

import android.content.Context;

import androidx.annotation.NonNull;

import com.cafeom.GoodsOrder;

public class FragmentGoodsOrder extends FragmentBase {

    protected GoodsOrder mOrder;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mOrder = (GoodsOrder) context;
    }
}
