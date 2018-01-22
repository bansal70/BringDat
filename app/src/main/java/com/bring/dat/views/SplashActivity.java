package com.bring.dat.views;

import android.os.Bundle;
import android.os.Handler;

import com.bring.dat.R;
import com.bring.dat.model.BDPreferences;
import com.bring.dat.model.Constants;
import com.bring.dat.model.Utils;

public class SplashActivity extends AppBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(this::startMain, 3000);
    }

    void startMain() {
        if (!BDPreferences.readString(mContext, Constants.KEY_RESTAURANT_ID).isEmpty()) {
            Utils.gotoNextActivityAnimation(mContext, HomeActivity.class);
            finish();
            return;
        }

        Utils.gotoNextActivityAnimation(mContext, MainActivity.class);
        finish();
    }
}
