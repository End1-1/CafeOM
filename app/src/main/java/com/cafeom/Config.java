package com.cafeom;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.Window;

public class Config extends AppAct implements ConfigLogin.OnConfigLoginListener,
    ConfigCnf.OnConfigCnfListener{

    private static final String frsLogin = "frlogin";
    private static final String frsCnf = "frcnf";
    ConfigLogin frLogin = new ConfigLogin();
    ConfigCnf frCnf = new ConfigCnf();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment, frLogin, frsLogin);
        fragmentTransaction.commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void login(String s) {
        if (s.equals(Cnf.getString("cnf_password"))) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment, frCnf, frsCnf);
            fragmentTransaction.commit();
        }
    }

    @Override
    public void config() {
        finish();
    }
}
