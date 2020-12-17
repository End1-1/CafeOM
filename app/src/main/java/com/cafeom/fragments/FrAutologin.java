package com.cafeom.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cafeom.Cnf;
import com.cafeom.WebService;
import com.cafeom.databinding.FragmentFrAutologinBinding;

public class FrAutologin extends FragmentGoodsOrder {

    private FragmentFrAutologinBinding bind;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        bind = FragmentFrAutologinBinding.inflate(getLayoutInflater(), container, false);
        return bind.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        WebService.Request r = new WebService.Request(Cnf.getString("net_server"), 1, WebService.mMethodGET);
        r.mParamers.put("sid", Cnf.getString("sid"));
        r.mParamers.put("view", "login");
        r.go();
    }

    @Override
    protected void webHandler(int requestCode, String data) {
        super.webHandler(requestCode, data);
        mOrder.loginComplete();
    }

    @Override
    protected void webHandlerError(int requestCode, String data) {
        mOrder.loginFailed();
        Cnf.setString("sid", "");
    }
}