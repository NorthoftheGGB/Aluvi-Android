package com.aluvi.android.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import com.afollestad.materialdialogs.MaterialDialog;
import com.aluvi.android.R;
import com.aluvi.android.helpers.views.DialogUtils;
import com.aluvi.android.helpers.views.FormValidator;
import com.aluvi.android.managers.PaymentManager;
import com.aluvi.android.managers.callbacks.DataCallback;
import com.aluvi.android.model.local.CreditCard;

import java.util.regex.Pattern;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by usama on 8/11/15.
 */
public class CreditCardInfoDialogFragment extends DialogFragment {
    public interface CreditCardListener {
        void onStripeTokenReceived(String token);

        void onError(String message);
    }

    @Bind(R.id.credit_card_info_edit_text_number) EditText mCreditCardNumberEditExt;
    @Bind(R.id.credit_card_info_edit_text_expiration_date) EditText mCreditCardExpirationDateEditText;
    @Bind(R.id.credit_card_info_edit_text_cvv) EditText mCVVEditText;

    private static final Pattern CREDIT_CARD_PATTERN = Pattern.compile("([0-9]{0,4})|([0-9]{4}-)+|([0-9]{4}-[0-9]{0,4})+");
    private CreditCardListener mListener;

    private Dialog mDefaultProgressDialog;

    public static CreditCardInfoDialogFragment newInstance() {
        return new CreditCardInfoDialogFragment();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (CreditCardListener) activity;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View rootView = View.inflate(getActivity(), R.layout.fragment_credit_card_info, null);
        ButterKnife.bind(this, rootView);

        initCreditCardFormatter();
        initExpirationDateFormat();
        return new MaterialDialog.Builder(getActivity())
                .title(R.string.card_details)
                .customView(rootView, false)
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .autoDismiss(false)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        validateFields();
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                        dismiss();

                        mListener.onError("User cancelled credit card registration");
                    }
                })
                .build();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mDefaultProgressDialog != null) {
            mDefaultProgressDialog.cancel();
            mDefaultProgressDialog = null;
        }
    }

    private void initCreditCardFormatter() {
        mCreditCardNumberEditExt.addTextChangedListener(new TextWatcher() {
            private boolean lockFormatting = false;
            private final int MAX_CREDIT_CARD_LENGTH_WITH_DASHES = 19;

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0 && !CREDIT_CARD_PATTERN.matcher(s).matches() && !lockFormatting) {
                    String input = s.toString();
                    String numbersOnly = keepNumbersOnly(input);
                    String code = formatNumbersAsCode(numbersOnly);

                    lockFormatting = true;
                    mCreditCardNumberEditExt.setText(code);
                    mCreditCardNumberEditExt.setSelection(code.length());

                    if (mCreditCardNumberEditExt.getText().length() > MAX_CREDIT_CARD_LENGTH_WITH_DASHES) {
                        mCreditCardNumberEditExt.clearFocus();
                    }

                    lockFormatting = false;
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            private String keepNumbersOnly(CharSequence s) {
                return s.toString().replaceAll("[^0-9]", ""); // Should of course be more robust
            }

            private String formatNumbersAsCode(CharSequence s) {
                int groupDigits = 0;
                String tmp = "";
                for (int i = 0; i < s.length(); ++i) {
                    tmp += s.charAt(i);
                    ++groupDigits;
                    if (groupDigits == 4) {
                        tmp += "-";
                        groupDigits = 0;
                    }
                }
                return tmp;
            }
        });
    }

    private void initExpirationDateFormat() {
        mCreditCardExpirationDateEditText.addTextChangedListener(new TextWatcher() {
            private boolean lockFormatting = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!lockFormatting) {
                    lockFormatting = true;
                    if (s.length() == 2) {
                        s.append("/");
                        mCreditCardExpirationDateEditText.setSelection(s.length());
                    }


                    if (s.length() == 5) {
                        mCreditCardExpirationDateEditText.clearFocus();
                        mCVVEditText.requestFocus();
                        mCVVEditText.setCursorVisible(true);
                    }

                    lockFormatting = false;
                }
            }
        });
    }

    public void validateFields() {
        String cardNumber = mCreditCardNumberEditExt.getText().toString();
        String cardExpiration = mCreditCardExpirationDateEditText.getText().toString();
        String cvv = mCVVEditText.getText().toString();

        boolean isValid = new FormValidator(getString(R.string.field_required_error))
                .addField(mCreditCardNumberEditExt)
                .addField(mCreditCardExpirationDateEditText, getString(R.string.invalid_expiration_date),
                        new FormValidator.Validator() {
                            @Override
                            public boolean isValid(String input) {
                                int expirationMonth = extractYear(input);
                                int expirationYear = extractYear(input);
                                return expirationMonth != -1 && expirationYear != -1;
                            }
                        })
                .addField(mCVVEditText)
                .validate();

        if (isValid) {
            mDefaultProgressDialog = DialogUtils.getDefaultProgressDialog(getActivity(), false);

            CreditCard card = new CreditCard(cardNumber, extractMonth(cardExpiration), extractYear(cardExpiration), cvv);
            PaymentManager.getInstance().requestToken(card, new DataCallback<String>() {
                @Override
                public void success(String result) {
                    if (!isDetached())
                        dismiss();

                    if (mDefaultProgressDialog != null)
                        mDefaultProgressDialog.cancel();

                    mListener.onStripeTokenReceived(result);
                }

                @Override
                public void failure(String message) {
                    if (getView() != null)
                        Snackbar.make(getView(), message, Snackbar.LENGTH_SHORT).show();

                    if (!isDetached())
                        dismiss();

                    if (mDefaultProgressDialog != null)
                        mDefaultProgressDialog.cancel();

                    mListener.onError(message);
                }
            });
        }
    }

    private int extractMonth(String expirationDate) {
        try {
            String[] split = expirationDate.split("/");
            if (split.length > 0)
                return Integer.parseInt(split[0]);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        return -1;
    }

    private int extractYear(String expirationDate) {
        try {
            String[] split = expirationDate.split("/");
            if (split.length > 1)
                return Integer.parseInt(split[1]);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        return -1;
    }

    @Override
    public void onDestroyView() {
        ButterKnife.unbind(this);
        super.onDestroyView();
    }
}
