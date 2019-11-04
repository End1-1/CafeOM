package com.cafeom;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.Window;

public class Config extends AppCompatActivity implements ConfigLogin.OnConfigLoginListener,
    ConfigCnf.OnConfigCnfListener{

    private static final String frsLogin = "frlogin";
    private static final String frsCnf = "frcnf";
    ConfigLogin frLogin = new ConfigLogin();
    ConfigCnf frCnf = new ConfigCnf();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//will hide the title
        getSupportActionBar().hide();
        setContentView(R.layout.activity_config);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment, frLogin, frsLogin);
        fragmentTransaction.commit();
    }

    @Override
    public void login(String s) {
        if (s.equals(Cnf.getString(this, "cnf_password"))) {
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
