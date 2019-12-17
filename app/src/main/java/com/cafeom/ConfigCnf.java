package com.cafeom;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.method.HideReturnsTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
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
        setEt(v, R.id.etPassword, Cnf.getString(getContext(), "cnf_password"));
        setEt(v, R.id.etServerAddress, Cnf.getString(getContext(), "server_address"));
        setEt(v, R.id.etServerPort, Cnf.getString(getContext(), "server_port"));
        setEt(v, R.id.edReminderId, Cnf.getString(getContext(), "reminder_id"));
        setCheck(v, R.id.chOnlyReady, Cnf.getBoolean(getContext(), "readyonly"));
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
                Cnf.setString(getContext(), "reminder_id", et(R.id.edReminderId));
                Cnf.setBoolean(getContext(), "readyonly", getCheck(R.id.chOnlyReady));
                mListener.config();
                break;
        }
    }

    public interface OnConfigCnfListener {
        void config();
    }

    void setCheck(View v, int id, boolean checked) {
        CheckBox ch = v.findViewById(id);
        if (ch != null) {
            ch.setChecked(checked);
        }
    }

    boolean getCheck(int id) {
        CheckBox ch = getView().findViewById(id);
        if (ch != null) {
            return ch.isChecked();
        }
        return false;
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
