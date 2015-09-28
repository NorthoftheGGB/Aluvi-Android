package com.aluvi.android.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.aluvi.android.R;
import com.aluvi.android.api.users.models.ReceiptData;
import com.aluvi.android.application.AluviRealm;
import com.aluvi.android.fragments.base.BaseButterFragment;
import com.aluvi.android.helpers.CurrencyUtils;
import com.aluvi.android.managers.PaymentManager;
import com.aluvi.android.managers.UserStateManager;
import com.aluvi.android.managers.callbacks.Callback;
import com.aluvi.android.managers.callbacks.DataCallback;
import com.aluvi.android.model.realm.Profile;
import com.stripe.android.model.Token;

import java.text.SimpleDateFormat;

import butterknife.Bind;
import butterknife.OnClick;
import io.realm.Realm;

/**
 * Created by usama on 9/27/15.
 */
public class PaymentInfoFragment extends BaseButterFragment implements CreditCardInfoDialogFragment.CreditCardListener {
    @Bind(R.id.payment_info_text_view_amount_spent) TextView mAmountSpentTextView;
    @Bind(R.id.payment_info_button_pay_to) Button mPayWithButton;
    @Bind(R.id.payment_info_button_pay_withdraw) Button mPayToButton;
    @Bind(R.id.payment_info_view_divider) View mDivider;
    @Bind(R.id.payment_info_text_view_last_transaction) TextView mLastTransactionDateTextView;

    public static PaymentInfoFragment newInstance() {
        return new PaymentInfoFragment();
    }

    @Override
    public View getRootView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_payment_info, container, false);
    }

    @Override
    public void initUI() {
        refreshUI();
    }

    private void refreshUI() {
        Profile profile = UserStateManager.getInstance().getProfile();

        String lastFour = profile.getCardLastFour();
        lastFour = lastFour == null ? "" : lastFour;

        String recipientLastFour = profile.getRecipientCardLastFour();
        recipientLastFour = recipientLastFour == null ? "" : recipientLastFour;

        mPayWithButton.setText("Pay with " + profile.getCardBrand() + " ending in " + lastFour);
        mAmountSpentTextView.setText(CurrencyUtils.getFormattedDollars(profile.getCommuterBalanceCents()));

        if (!UserStateManager.getInstance().isUserDriver()) {
            mDivider.setVisibility(View.INVISIBLE);
            mPayToButton.setVisibility(View.INVISIBLE);
        } else {
            mPayToButton.setText("Get paid to " + profile.getRecipientCardBrand() + " ending in " + recipientLastFour);
        }

        updateLastTransaction();
    }

    private SimpleDateFormat mMonthDayFormat = new SimpleDateFormat("MM/dd");

    private void updateLastTransaction() {
        PaymentManager.getInstance().getLastTransaction(new DataCallback<ReceiptData>() {
            @Override
            public void success(ReceiptData result) {
                if (mLastTransactionDateTextView != null)
                    mLastTransactionDateTextView.setText("Last transaction on " + mMonthDayFormat.format(result.getDate()));
            }

            @Override
            public void failure(String message) {
                if (getView() != null)
                    Snackbar.make(getView(), message, Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.payment_info_button_pay_to)
    public void onPaymentInfoButtonClicked() {
        CreditCardInfoDialogFragment.newInstance().show(getChildFragmentManager(), "pay");
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.payment_info_button_pay_withdraw)
    public void onWithdrawPaymentInfoButtonClicked() {
        CreditCardInfoDialogFragment.newInstance().show(getChildFragmentManager(), "withdraw");
    }

    @Override
    public void onStripeTokenReceived(Token token, CreditCardInfoDialogFragment fragment) {
        String mCardPayToken = null, mCardWithdrawToken = null;

        if (fragment.getTag().equals("pay")) {
            mCardPayToken = token.getId();
        } else {
            mCardWithdrawToken = token.getId();
        }

        Realm realm = AluviRealm.getDefaultRealm();
        realm.beginTransaction();
        Profile profile = UserStateManager.getInstance().getProfile();
        if (mCardPayToken != null)
            profile.setDefaultCardToken(mCardPayToken);

        if (mCardWithdrawToken != null)
            profile.setRecipientDebitCardToken(mCardWithdrawToken);
        realm.commitTransaction();

        UserStateManager.getInstance().saveProfile(new Callback() {
            @Override
            public void success() {
                if (getView() != null) {
                    Snackbar.make(getView(), R.string.profile_saved, Snackbar.LENGTH_SHORT).show();
                    refreshUI();
                }
            }

            @Override
            public void failure(String message) {
                if (getView() != null)
                    Snackbar.make(getView(), message, Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onCreditCardProcessingError(String message) {
        if (getView() != null)
            Snackbar.make(getView(), message, Snackbar.LENGTH_SHORT).show();
    }
}
