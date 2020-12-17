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

import com.cafeom.databinding.FragmentConfigCnfBinding;

public class ConfigCnf extends Fragment implements  View.OnClickListener {

    private OnConfigCnfListener mListener;
    private FragmentConfigCnfBinding bind;

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
        bind = FragmentConfigCnfBinding.inflate(getLayoutInflater(), container, false);
        View v = bind.getRoot();
        v.findViewById(R.id.btnSave).setOnClickListener(this);
        setEt(v, R.id.etPassword, Cnf.getString("cnf_password"));
        setEt(v, R.id.etServerAddress, Cnf.getString("server_address"));
        setEt(v, R.id.etServerPort, Cnf.getString("server_port"));
        setEt(v, R.id.edReminderId, Cnf.getString("reminder_id"));
        setCheck(v, R.id.chOnlyReady, Cnf.getBoolean("readyonly"));
        bind.orderServerAddress.setText(Cnf.getString("net_server"));
        bind.branch.setText(Cnf.getString("branch"));
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
                Cnf.setString("cnf_password", et(R.id.etPassword));
                Cnf.setString("server_address", et(R.id.etServerAddress));
                Cnf.setString("server_port", et(R.id.etServerPort));
                Cnf.setString("reminder_id", et(R.id.edReminderId));
                Cnf.setBoolean("readyonly", getCheck(R.id.chOnlyReady));
                Cnf.setString("net_server", bind.orderServerAddress.getText().toString());
                Cnf.setString("branch", bind.branch.getText().toString());
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
