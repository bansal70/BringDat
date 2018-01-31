package com.bring.dat.views.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

import com.bring.dat.R;
import com.bring.dat.model.BDPreferences;
import com.bring.dat.model.Constants;
import com.bring.dat.model.Operations;
import com.bring.dat.model.pojo.AdjustReasons;
import com.bring.dat.model.pojo.Order;
import com.bring.dat.model.pojo.Transaction;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

@SuppressWarnings("ConstantConditions")
public class AdjustSaleDialogFragment extends DialogBaseFragment{
    Unbinder unbinder;

    @BindView(R.id.editAmount)
    EditText editAmount;

    @BindView(R.id.editName)
    EditText editName;

    @BindView(R.id.editMessage)
    EditText editMessage;

    @BindView(R.id.reasonLL)
    LinearLayout reasonLL;

    Order mOrder;
    AdjustReasons mAdjustReasons;

    private OnAdjustTransaction mOnAdjustTransaction;
    private List<AdjustReasons.Option> listOptions = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_transaction_adjustment, container, false);

        unbinder = ButterKnife.bind(this, view);

        int width = RelativeLayout.LayoutParams.MATCH_PARENT;
        int height = RelativeLayout.LayoutParams.WRAP_CONTENT - 100;
        getDialog().getWindow().setLayout(width, height);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        getData();

        return view;
    }

    private void getData() {
        Bundle bundle = getArguments();
        String orderJson = bundle.getString("orderData");
        String adjustJson = bundle.getString("adjustData");

        mOrder = new Gson().fromJson(orderJson, Order.class);
        mAdjustReasons = new Gson().fromJson(adjustJson, AdjustReasons.class);

        setReasons(mAdjustReasons);
    }

    private void setReasons(AdjustReasons mReasons) {
        if (mReasons.mSuccess) {
            List<AdjustReasons.Option> mOptions = mReasons.mOption;
            listOptions.addAll(mOptions);
        }

        setData();
    }

    private void setData() {
        RadioGroup radioGroup = new RadioGroup(mContext);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        reasonLL.addView(radioGroup, p);

        for (int i =0; i< listOptions.size(); i++) {
            RadioButton mRadioButton = new RadioButton(mContext);
            mRadioButton.setId(i);
            mRadioButton.setText(listOptions.get(i).mReason);
            radioGroup.addView(mRadioButton);
        }

        radioGroup.setOnCheckedChangeListener((mRadioGroup, checkedId) -> {
            if (checkedId != -1) {
                RadioButton radioButton = mRadioGroup.findViewById(checkedId);
                radioButton.setChecked(true);
                String text = radioButton.getText().toString();
                editMessage.setText(text);
            }
        });
    }

    @OnClick(R.id.btSubmit)
    public void adjustSale() {
        if (!mActivity.isInternetActive()) {
            return;
        }
        String name = editName.getText().toString();
        String msg = editMessage.getText().toString();
        String restId = BDPreferences.readString(mContext, Constants.KEY_RESTAURANT_ID);
        String token = BDPreferences.readString(mContext, Constants.KEY_TOKEN);

        if (!isValidData()) {
            return;
        }
        mActivity.showDialog();

        if (mOrder != null) {
            apiService.adjustTransaction(Operations.adjustTransactionParams(restId, name, msg,
                    mOrder.transactionId, mOrder.orderid, token, editAmount.getText().toString()))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .onErrorResumeNext(throwable -> {
                        mActivity.serverError();
                    })
                    .doOnNext(this::adjustTransaction)
                    .doOnError(mActivity::serverError)
                    .subscribe();
        }
    }

    private void adjustTransaction(Transaction mTransaction) {
        mActivity.dismissDialog();
        showToast(mTransaction.msg);

        if (mTransaction.success) {
            mOrder.applyAdjust = "yes";
            if (mOnAdjustTransaction != null) {
                mOnAdjustTransaction.onAdjustSale(mOrder);
            }
            dismiss();
        }
    }

    private boolean isValidData() {
        if (editAmount.getText().toString().trim().isEmpty()) {
            showToast(getString(R.string.error_empty_amount));
            return false;
        }
        if (editName.getText().toString().trim().isEmpty()) {
            showToast(getString(R.string.error_empty_name));
            return false;
        }
        if (editMessage.getText().toString().trim().isEmpty()) {
            showToast(getString(R.string.prompt_adjusting_reasons));
            return false;
        }

        return true;
    }

    public interface OnAdjustTransaction {
        void onAdjustSale(Order mOrder);
    }

    public void setOnAdjustSaleListener(OnAdjustTransaction onAdjustTransaction){
        mOnAdjustTransaction = onAdjustTransaction;
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
