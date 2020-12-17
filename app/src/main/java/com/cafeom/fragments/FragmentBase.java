package com.cafeom.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.cafeom.AppAct;
import com.cafeom.Dialog;
import com.cafeom.R;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public abstract class FragmentBase extends Fragment
    implements View.OnClickListener {

    protected JsonParser mJsonParser = new JsonParser();

    public FragmentBase() {
    }

    public String tag() {
        return getClass().getName();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(webResponse, new IntentFilter("web_response"));
    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(webResponse);
    }

    @Override
    public void onClick(View view) {

    }

    protected void hideProgress() {
        View v = getView();
        if (v == null) {
            return;
        }
        ProgressBar p = v.findViewById(R.id.progress);
        if (p != null) {
            p.setVisibility(View.GONE);
        }
    }

    protected void webHandler(int requestCode, String data) {
    }

    protected void webHandlerError(int requestCode, String data) {
        Dialog.alertDialog(getView().getContext(), R.string.Error, data);
    }

    protected JsonObject jsonObject(String s) {
        return mJsonParser.parse(s).getAsJsonObject();
    }

    protected JsonArray jsonArray(String s) {
        return mJsonParser.parse(s).getAsJsonArray();
    }

    public static <T> T newInstance(Class<T> type) {
        try {
            T fragment = type.newInstance();
            return fragment;
        } catch (java.lang.IllegalAccessException e) {
            e.printStackTrace();
        } catch (java.lang.InstantiationException e) {
            e.printStackTrace();
        }
        return null;
    }

    BroadcastReceiver webResponse = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            hideProgress();
            int webcode = intent.getIntExtra("webresponse", 0);
            int requestCode = intent.getIntExtra("requestcode", 0);
            String s =intent.getStringExtra("data");
            if (webcode > 299 || webcode < 200) {
                webHandlerError(requestCode, s);
            } else {
                webHandler(requestCode, s);
            }
        }
    };
}
