package com.cafeom.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.cafeom.Cnf;
import com.cafeom.R;
import com.cafeom.WebService;
import com.cafeom.adapters.OrdersViewAdapter;
import com.cafeom.data.OrderData;
import com.cafeom.databinding.FragmentFrViewOrdersBinding;

public class FrViewOrders extends FragmentGoodsOrder {

    private FragmentFrViewOrdersBinding bind;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        bind = FragmentFrViewOrdersBinding.inflate(getLayoutInflater(), container, false);
        bind.back.setOnClickListener(this);
        bind.rv.setLayoutManager(new LinearLayoutManager(getContext()));
        bind.rv.setAdapter(new OrdersViewAdapter(mOrder));
        return bind.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        loadOrders();
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
                OrderData.parseOrders(data);
                bind.rv.getAdapter().notifyDataSetChanged();
                break;
        }
    }

    private void loadOrders() {
        bind.progress.setVisibility(View.VISIBLE);
        WebService.Request r = new WebService.Request(Cnf.getString("net_server"), 1, WebService.mMethodGET);
        r.mParamers.put("sid", Cnf.getString("sid"));
        r.mParamers.put("view", "vieworders");
        r.go();
    }
}