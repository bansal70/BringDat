package com.bring.dat.views;

import android.os.Bundle;

import com.bring.dat.R;
import com.bring.dat.model.BDPreferences;
import com.bring.dat.model.Constants;
import com.bring.dat.model.Utils;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.btLoggerLogin)
    public void loggerScreen() {
        Utils.gotoNextActivityAnimation(mContext, LoginActivity.class);
        finish();
        BDPreferences.putString(mContext, Constants.KEY_LOGIN_TYPE, Constants.LOGIN_LOGGER);
    }

    @OnClick(R.id.btAdminLogin)
    public void adminScreen() {
        Utils.gotoNextActivityAnimation(mContext, LoginActivity.class);
        finish();

        BDPreferences.putString(mContext, Constants.KEY_LOGIN_TYPE, Constants.LOGIN_ADMIN);
    }
}
