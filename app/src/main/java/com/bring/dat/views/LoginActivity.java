package com.bring.dat.views;

import android.os.Bundle;
import android.widget.EditText;

import com.bring.dat.R;
import com.bring.dat.model.BDPreferences;
import com.bring.dat.model.Constants;
import com.bring.dat.model.Operations;
import com.bring.dat.model.Utils;
import com.bring.dat.model.pojo.LoginResponse;
import com.google.firebase.iid.FirebaseInstanceId;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class LoginActivity extends AppBaseActivity {

    @BindView(R.id.etEmail)
    EditText etEmail;

    @BindView(R.id.etPassword)
    EditText etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        FirebaseInstanceId.getInstance().getToken();
    }

    @OnClick(R.id.btLogin)
    public void actionLogin() {
        String deviceToken = FirebaseInstanceId.getInstance().getToken();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();
        //  startActivity(new Intent(mContext, OrdersListActivity.class));
        if (isValid()) {
            showDialog();
            apiService.loginUser(Operations.loginParams(email, password, deviceToken))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .onErrorResumeNext(throwable -> {
                        serverError();
                    })
                    .doOnNext(this::loginData)
                    .doOnError(this::serverError)
                    .subscribe();
        }
    }

    private boolean isValid() {
        if (etEmail.getText().toString().trim().isEmpty()) {
            showToast(getString(R.string.error_email_required));
            return false;
        }
        if (etPassword.getText().toString().isEmpty()) {
            showToast(getString(R.string.error_password_required));
            return false;
        }
        return true;
    }

    private void loginData(LoginResponse loginResponse) {
        dismissDialog();
        if (loginResponse.mSuccess) {
            LoginResponse.Data mData = loginResponse.mData;
            BDPreferences.putString(mContext, Constants.KEY_RESTAURANT_ID, mData.restaurantId);
            BDPreferences.putString(mContext, Constants.KEY_EMAIL, mData.restaurantEmail);
            BDPreferences.putString(mContext, Constants.KEY_TOKEN, mData.token);

            showToast(getString(R.string.success_login));
            Utils.gotoNextActivityAnimation(mContext, HomeActivity.class);
            finish();
        } else {
            showToast(getString(R.string.eror_invalid_credentials));
        }
    }

}

