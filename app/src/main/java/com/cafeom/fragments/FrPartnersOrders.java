package com.cafeom.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cafeom.Cnf;
import com.cafeom.R;
import com.cafeom.WebService;
import com.cafeom.adapters.PartnerOrderAdapter;
import com.cafeom.data.DataParser;
import com.cafeom.data.PartnerOrderData;
import com.cafeom.databinding.FragmentFrPartnersOrdersBinding;

public class FrPartnersOrders extends FragmentGoodsOrder {

    private FragmentFrPartnersOrdersBinding bind;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        bind = FragmentFrPartnersOrdersBinding.inflate(getLayoutInflater(), container, false);
        bind.back.setOnClickListener(this);
        bind.rv.setLayoutManager(new LinearLayoutManager(getContext()));
        bind.rv.setAdapter(new PartnerOrderAdapter());
        return bind.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        getOrders();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back:
                mOrder.reset();
                break;
        }
    }

    @Override
    protected void webHandler(int requestCode, String data) {
        super.webHandler(requestCode, data);
        switch (requestCode) {
            case 1:
                PartnerOrderData.mInstance = PartnerOrderData.parseString(data);
                bind.rv.getAdapter().notifyDataSetChanged();
                break;
            case 5:
                getOrders();
                break;
        }
    }

    public void getOrders() {
        bind.progress.setVisibility(View.VISIBLE);
        WebService.Request r = new WebService.Request(Cnf.getString("net_server"), 1, WebService.mMethodGET);
        r.mParamers.put("sid", Cnf.getString("sid"));
        r.mParamers.put("view", "partnersorders");
        r.go();
    }
}