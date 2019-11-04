package com.cafeom;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;


public class ConfigLogin extends Fragment implements View.OnClickListener {

    private OnConfigLoginListener mListener;
    private EditText etPassword;

    public ConfigLogin() {
        // Required empty public constructor
    }

    public static ConfigLogin newInstance() {
        ConfigLogin fragment = new ConfigLogin();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_config_login, container, false);
        etPassword = v.findViewById(R.id.etPassword);
        v.findViewById(R.id.btnEnter).setOnClickListener(this);
        TextView tvVersion = v.findViewById(R.id.tvVersion);
        tvVersion.setText("v." + BuildConfig.VERSION_NAME);
        return v;
    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnConfigLoginListener) {
            mListener = (OnConfigLoginListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnEnter:
                mListener.login(etPassword.getText().toString());
                break;
        }
    }

    public interface OnConfigLoginListener {
        void login(String s);
    }
}
