package com.bring.dat.views.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.bring.dat.R;
import com.bring.dat.model.BDPreferences;
import com.bring.dat.model.Constants;
import com.bring.dat.model.Operations;
import com.bring.dat.model.pojo.Order;
import com.bring.dat.model.pojo.Transaction;
import com.google.gson.Gson;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

@SuppressWarnings("ConstantConditions")
public class VoidSaleDialogFragment extends DialogBaseFragment {

    Unbinder unbinder;

    @BindView(R.id.editName)
    EditText editName;

    @BindView(R.id.editMessage)
    EditText editMessage;

    Order mOrder;

    private OnVoidTransaction mOnVoidTransaction;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_void_sale, container, false);

        unbinder = ButterKnife.bind(this, view);

        int width = RelativeLayout.LayoutParams.MATCH_PARENT;
        int height = RelativeLayout.LayoutParams.MATCH_PARENT;
        getDialog().getWindow().setLayout(width, height);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        getData();

        return view;
    }

    private void getData() {
        Bundle bundle = getArguments();
        String orderJson = bundle.getString("orderData");
        mOrder = new Gson().fromJson(orderJson, Order.class);
    }

    @OnClick(R.id.btSubmit)
    public void voidSale() {
        String name = editName.getText().toString();
        String msg = editMessage.getText().toString();
        String restId = BDPreferences.readString(mContext, Constants.KEY_RESTAURANT_ID);
        String token = BDPreferences.readString(mContext, Constants.KEY_TOKEN);

        if (!isValidData()) {
            return;
        }
        mActivity.showDialog();

        if (mOrder != null) {
            apiService.voidTransaction(Operations.voidTransactionParams(restId, name, msg, mOrder.transactionId, mOrder.orderid, token))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .onErrorResumeNext(throwable -> {
                        mActivity.serverError();
                    })
                    .doOnNext(this::voidTransaction)
                    .doOnError(mActivity::serverError)
                    .subscribe();
        }
    }

    private void voidTransaction(Transaction mTransaction) {
        mActivity.dismissDialog();
        showToast(mTransaction.msg);

        if (mTransaction.success) {
            mOrder.applyVoid = "Yes";
            if (mOnVoidTransaction != null) {
                mOnVoidTransaction.onVoidSale(mOrder);
            }
            dismiss();
        }
    }

    private boolean isValidData() {
        if (editName.getText().toString().trim().isEmpty()) {
            showToast(getString(R.string.error_empty_name));
            return false;
        }
        if (editMessage.getText().toString().trim().isEmpty()) {
            showToast(getString(R.string.prompt_void_reason));
            return false;
        }

        return true;
    }

    public interface OnVoidTransaction {
        void onVoidSale(Order mOrder);
    }

    public void setOnVoidSaleListener(OnVoidTransaction onVoidTransaction){
        mOnVoidTransaction = onVoidTransaction;
    }

    @OnClick(R.id.btCancel)
    public void cancelVoid() {
        dismiss();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unbinder.unbind();
    }
}
