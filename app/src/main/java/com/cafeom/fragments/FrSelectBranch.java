package com.cafeom.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cafeom.Cnf;
import com.cafeom.R;
import com.cafeom.WebService;
import com.cafeom.adapters.IdNameSpinnerAdapter;
import com.cafeom.data.DataParser;
import com.cafeom.data.IdNameData;
import com.cafeom.databinding.FragmentFrSelectBranchBinding;

import java.util.List;

public class FrSelectBranch extends FragmentGoodsOrder {

    private FragmentFrSelectBranchBinding bind;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        bind = FragmentFrSelectBranchBinding.inflate(getLayoutInflater(), container, false);
        bind.back.setOnClickListener(this);
        bind.save.setOnClickListener(this);
        return bind.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        WebService.Request r = new WebService.Request(Cnf.getString("net_server"), 1, WebService.mMethodGET);
        r.mParamers.put("sid", Cnf.getString("sid"));
        r.mParamers.put("view", "branch");
        r.mParamers.put("branch", Cnf.getString("branch"));
        r.go();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back:
                mOrder.orderFragment();
                break;
            case R.id.save:
                IdNameData d = (IdNameData) bind.branch.getSelectedItem();
                mOrder.mOrderData.f_branchid = d.f_id;
                mOrder.mOrderData.f_branchname = d.f_name;
                d = (IdNameData) bind.storage.getSelectedItem();
                mOrder.mOrderData.f_storeid = d.f_id;
                mOrder.mOrderData.f_storename = d.f_name;
                mOrder.orderFragment();
                break;
        }
    }

    @Override
    protected void webHandler(int requestCode, String data) {
        super.webHandler(requestCode, data);
        InData inData = DataParser.gson().fromJson(data, InData.class);
        IdNameData[] data1 = new IdNameData[inData.data_branches.size()];
        inData.data_branches.toArray(data1);
        IdNameSpinnerAdapter branch = new IdNameSpinnerAdapter(getContext(), R.layout.item_simple_spinner, data1);
        bind.branch.setAdapter(branch);
        IdNameData[] data2 = new IdNameData[inData.data_storages.size()];
        inData.data_storages.toArray(data2);
        IdNameSpinnerAdapter storage = new IdNameSpinnerAdapter(getContext(), R.layout.item_simple_spinner, data2);
        bind.storage.setAdapter(storage);
        if (!mOrder.mOrderData.f_branchname.isEmpty()) {
            bind.branch.setSelection(((IdNameSpinnerAdapter) bind.branch.getAdapter()).getItemPosition(mOrder.mOrderData.f_branchname));
        }
        if (!mOrder.mOrderData.f_storename.isEmpty()) {
            bind.storage.setSelection(((IdNameSpinnerAdapter) bind.storage.getAdapter()).getItemPosition(mOrder.mOrderData.f_storename));
        }
    }

    public class InData {
        public List<IdNameData> data_branches;
        public List<IdNameData> data_storages;
    }
}