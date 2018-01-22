package com.bring.dat.views.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.bring.dat.R;
import com.bring.dat.model.BDPreferences;
import com.bring.dat.model.Constants;
import com.bring.dat.views.HomeActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class SettingsFragment extends AppBaseFragment{

    Unbinder unbinder;

    @BindView(R.id.btChangeAlias)
    Button btChangeAlias;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        unbinder = ButterKnife.bind(this, view);

        if (BDPreferences.readString(mContext, Constants.KEY_LOGIN_TYPE).equals(Constants.LOGIN_LOGGER)) {
            btChangeAlias.setText(getString(R.string.prompt_switch_admin));
        } else {
            btChangeAlias.setText(getString(R.string.prompt_switch_logger));
        }

        return view;
    }

    @OnClick(R.id.btChangeAlias)
    public void changeAlias() {
        if (BDPreferences.readString(mContext, Constants.KEY_LOGIN_TYPE).equals(Constants.LOGIN_LOGGER)) {
            BDPreferences.putString(mContext, Constants.KEY_LOGIN_TYPE, Constants.LOGIN_ADMIN);
        } else {
            BDPreferences.putString(mContext, Constants.KEY_LOGIN_TYPE, Constants.LOGIN_LOGGER);
        }

        Intent intent = new Intent(mContext, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unbinder.unbind();
    }
}
