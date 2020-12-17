package com.cafeom.fragments;

import android.content.Context;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.cafeom.Cnf;
import com.cafeom.GoodsOrder;
import com.cafeom.R;
import com.cafeom.WebService;
import com.cafeom.databinding.FragmentFrOrderLoginBinding;
import com.google.gson.JsonObject;

public class FrOrderLogin extends FragmentGoodsOrder {

    private FragmentFrOrderLoginBinding bind;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        bind = FragmentFrOrderLoginBinding.inflate(getLayoutInflater(), container, false);
        bind.enter.setOnClickListener(this);
        bind.username.setText(Cnf.getString("autologin"));
        if (!bind.username.getText().toString().isEmpty()) {
            bind.password.requestFocus();
        }
        return bind.getRoot();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.enter:
                bind.progress.setVisibility(View.VISIBLE);
                WebService.Request r = new WebService.Request(String.format("%s",
                        Cnf.getString("net_server")), 1, WebService.mMethodGET);
                r.mParamers.put("username", bind.username.getText().toString());
                r.mParamers.put("password", bind.password.getText().toString());
                r.mParamers.put("view", "login");
                r.go();
                break;
        }
    }

    @Override
    protected void webHandler(int requestCode, String data) {
        super.webHandler(requestCode, data);
        JsonObject jo = jsonObject(data);
        switch (requestCode) {
            case 1:
                responseLogin(jo.get("sid").getAsString());
                break;
        }
    }

    private void responseLogin(String sid) {
        Cnf.setString("sid", sid);
        Cnf.setString("autologin", bind.username.getText().toString());
        mOrder.loginComplete();
    }
}