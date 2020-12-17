package com.cafeom;

import android.os.Bundle;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.cafeom.data.OrderData;
import com.cafeom.databinding.ActivityGoodsOrderBinding;
import com.cafeom.fragments.FrAutologin;
import com.cafeom.fragments.FrGoodsContens;
import com.cafeom.fragments.FrOrderGoods;
import com.cafeom.fragments.FrOrderLogin;
import com.cafeom.fragments.FrPartnersOrders;
import com.cafeom.fragments.FrSearchGoods;
import com.cafeom.fragments.FrSelectBranch;
import com.cafeom.fragments.FrViewOrders;

public class GoodsOrder extends AppAct {

    private ActivityGoodsOrderBinding bind;
    public OrderData mOrderData = new OrderData();
    public String mOpenOrder = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = ActivityGoodsOrderBinding.inflate(getLayoutInflater());
        setContentView(bind.getRoot());
        LocalBroadcastManager.getInstance(this).unregisterReceiver(webResponse);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Cnf.getString("sid").isEmpty()) {
            replaceFragment(FrOrderLogin.newInstance(FrOrderLogin.class));
        } else {
            replaceFragment(FrAutologin.newInstance(FrAutologin.class));
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void loginComplete() {
        replaceFragment(FrGoodsContens.newInstance(FrGoodsContens.class));
    }

    public void loginFailed() {
        replaceFragment(FrOrderLogin.newInstance(FrOrderLogin.class));
    }

    public void newOrder() {
        replaceFragment(FrSearchGoods.newInstance(FrSearchGoods.class));
    }

    public void viewOrders() {
        replaceFragment(FrViewOrders.newInstance(FrViewOrders.class));
    }

    public void partnersOrders() {
        replaceFragment(FrPartnersOrders.newInstance(FrPartnersOrders.class));
    }

    public void searchFragment() {
        replaceFragment(FrSearchGoods.newInstance(FrSearchGoods.class));
    }

    public void orderFragment() {
        replaceFragment(FrOrderGoods.newInstance(FrOrderGoods.class));
    }

    public void openOrder(String id) {
        mOpenOrder = id;
        replaceFragment(FrOrderGoods.newInstance(FrOrderGoods.class));
    }

    public void searchBranch() {
        replaceFragment(FrSelectBranch.newInstance(FrSelectBranch.class));
    }

    public void reset() {
        mOrderData.clear();
        mOpenOrder = "";
        replaceFragment(FrGoodsContens.newInstance(FrGoodsContens.class));
    }

}