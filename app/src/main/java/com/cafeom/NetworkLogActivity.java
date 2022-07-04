package com.cafeom;

import android.os.Bundle;

import com.cafeom.databinding.ActivityNetworkLogBinding;

public class NetworkLogActivity extends ActivityClass {

    private ActivityNetworkLogBinding _b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _b = ActivityNetworkLogBinding.inflate(getLayoutInflater());
        _b.edtMessage.setText(Cnf.getString("last_network_error"));
        setContentView(_b.getRoot());
    }
}