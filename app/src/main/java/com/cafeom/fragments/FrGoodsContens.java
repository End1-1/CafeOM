package com.cafeom.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cafeom.Cnf;
import com.cafeom.R;
import com.cafeom.WebService;
import com.cafeom.data.GoodsData;
import com.cafeom.databinding.FragmentFrGoodsContensBinding;

public class FrGoodsContens extends FragmentGoodsOrder {

    private FragmentFrGoodsContensBinding bind;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        bind = FragmentFrGoodsContensBinding.inflate(getLayoutInflater(), container, false);
        bind.neworder.setOnClickListener(this);
        bind.vieworders.setOnClickListener(this);
        bind.partnerorders.setOnClickListener(this);
        return bind.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        bind.progress.setVisibility(View.VISIBLE);
        WebService.Request r = new WebService.Request(Cnf.getString("net_server"), 1, WebService.mMethodGET);
        r.mParamers.put("view", "goods");
        r.mParamers.put("sid", Cnf.getString("sid"));
        r.go();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.neworder:
                mOrder.newOrder();
                break;
            case R.id.vieworders:
                mOrder.viewOrders();
                break;
            case R.id.partnerorders:
                mOrder.partnersOrders();
                break;
        }
    }

    @Override
    protected void webHandler(int requestCode, String data) {
        super.webHandler(requestCode, data);
        switch (requestCode) {
            case 1:
                GoodsData.parseGoods(data);
                break;
        }
    }
}