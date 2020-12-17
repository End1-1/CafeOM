package com.cafeom.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.cafeom.Cnf;
import com.cafeom.Dialog;
import com.cafeom.R;
import com.cafeom.WebService;
import com.cafeom.adapters.OrderGoodsAdapter;
import com.cafeom.data.DataParser;
import com.cafeom.data.GoodsData;
import com.cafeom.data.OrderData;
import com.cafeom.databinding.FragmentFrOrderGoodsBinding;

public class FrOrderGoods extends FragmentGoodsOrder {

    private FragmentFrOrderGoodsBinding bind;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        bind = FragmentFrOrderGoodsBinding.inflate(getLayoutInflater(), container, false);
        bind.searchFragment.setOnClickListener(this);
        bind.save.setOnClickListener(this);
        bind.branch.setOnClickListener(this);
        bind.rv.setLayoutManager(new LinearLayoutManager(getContext()));
        bind.rv.setAdapter(new OrderGoodsAdapter(mOrder));
        if (mOrder.mOrderData.f_branchname.length() > 0) {
            bind.branch.setText(String.format("%s %s", mOrder.mOrderData.f_branchname, mOrder.mOrderData.f_storename));
        } else {
            bind.branch.setText(getString(R.string.SelectBranch));
        }
        return bind.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!mOrder.mOpenOrder.isEmpty()) {
            bind.progress.setVisibility(View.VISIBLE);
            bind.save.setVisibility(View.GONE);
            WebService.Request r = new WebService.Request(Cnf.getString("net_server"), 2, WebService.mMethodGET);
            r.mParamers.put("sid", Cnf.getString("sid"));
            r.mParamers.put("view", "openorder");
            r.mParamers.put("id", mOrder.mOpenOrder);
            r.go();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.searchFragment:
                mOrder.searchFragment();
                break;
            case R.id.save:
                saveOrder();
                break;
            case R.id.branch:
                mOrder.searchBranch();
                break;
        }
    }

    @Override
    protected void webHandler(int requestCode, String data) {
        bind.save.setVisibility(View.VISIBLE);
        switch (requestCode) {
            case 1:
                mOrder.reset();
                break;
            case 2:
                mOrder.mOrderData = DataParser.parse(data, OrderData.class);
                bind.rv.getAdapter().notifyDataSetChanged();
                mOrder.mOpenOrder = "";
                if (mOrder.mOrderData.f_branchname.length() > 0) {
                    bind.branch.setText(String.format("%s %s", mOrder.mOrderData.f_branchname, mOrder.mOrderData.f_storename));
                } else {
                    bind.branch.setText(getString(R.string.SelectBranch));
                }
                break;
        }
    }

    @Override
    protected void webHandlerError(int requestCode, String data) {
        super.webHandlerError(requestCode, data);
        bind.save.setVisibility(View.VISIBLE);
    }

    private void saveOrder() {
        if (mOrder.mOrderData.mGoodsOrder.size() == 0) {
            Dialog.alertDialog(getContext(), R.string.Error, R.string.EmptyOrder);
            return;
        }
        if (mOrder.mOrderData.f_branchname.isEmpty()) {
            Dialog.alertDialog(getContext(), R.string.Error, R.string.BranchNameEmpty);
            return;
        }
        if (mOrder.mOrderData.f_storename.isEmpty()) {
            Dialog.alertDialog(getContext(), R.string.Error, R.string.StoreNameEmpty);
            return;
        }
        bind.save.setVisibility(View.GONE);
        WebService.Request r = new WebService.Request(Cnf.getString("net_server"), 1, WebService.mMethodPOST);
        r.mParamers.put("sid", Cnf.getString("sid"));
        r.mParamers.put("view", "saveorder");
        r.mParamers.put("body", GoodsData.listToString(mOrder.mOrderData.mGoodsOrder));
        r.mParamers.put("header", DataParser.gson().toJson(mOrder.mOrderData, OrderData.class));
        r.go();
    }
}