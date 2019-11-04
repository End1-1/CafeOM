package com.cafeom;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.method.HideReturnsTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

public class ConfigCnf extends Fragment implements  View.OnClickListener {

    private OnConfigCnfListener mListener;

    public ConfigCnf() {
    }

    public static ConfigCnf newInstance() {
        ConfigCnf fragment = new ConfigCnf();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_config_cnf, container, false);
        v.findViewById(R.id.btnSave).setOnClickListener(this);
        v.findViewById(R.id.btnClearDb).setOnClickListener(this);
        setEt(v, R.id.etPassword, Cnf.getString(getContext(), "cnf_password"));
        setEt(v, R.id.etServerAddress, Cnf.getString(getContext(), "server_address"));
        setEt(v, R.id.etServerPort, Cnf.getString(getContext(), "server_port"));
        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnConfigCnfListener) {
            mListener = (OnConfigCnfListener) context;
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
            case R.id.btnSave:
                Cnf.setString(getContext(), "cnf_password", et(R.id.etPassword));
                Cnf.setString(getContext(), "server_address", et(R.id.etServerAddress));
                Cnf.setString(getContext(), "server_port", et(R.id.etServerPort));
                mListener.config();
                break;
            case R.id.btnClearDb:
                Db db = new Db(getContext());
                db.exec("delete from rem");
                break;
        }
    }

    public interface OnConfigCnfListener {
        void config();
    }

    void setEt(View v, int id, String s) {
        EditText et = v.findViewById(id);
        if (et != null) {
            et.setText(s);
        }
    }

    String et(int id) {
        EditText et = getView().findViewById(id);
        if (et == null) {
            return "NULL";
        } else {
            return et.getText().toString();
        }
    }
}
