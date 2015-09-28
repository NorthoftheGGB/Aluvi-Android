package com.aluvi.android.managers;

import com.aluvi.android.api.users.ReceiptsApi;
import com.aluvi.android.api.users.models.ReceiptData;
import com.aluvi.android.managers.callbacks.DataCallback;
import com.aluvi.android.model.local.CreditCard;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;
import com.stripe.exception.AuthenticationException;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by usama on 8/11/15.
 */
public class PaymentManager {
    public interface OnStripeDataFetchedListener {
        void onTokenFetched(Token token);

        void onFailure(String message);
    }

    private static PaymentManager instance;
    private Stripe mStripe;

    public static synchronized void initialize(String stripeKey) {
        if (instance == null)
            instance = new PaymentManager(stripeKey);
    }

    public static synchronized PaymentManager getInstance() {
        return instance;
    }

    private PaymentManager(String stripeKey) {
        try {
            mStripe = new Stripe(stripeKey);
        } catch (AuthenticationException e) {
            e.printStackTrace();
        }
    }

    public void requestToken(CreditCard card, final OnStripeDataFetchedListener tokenCallback) {
        Card stripeCard = new Card(card.getCardNumber(), card.getExpirationMonth(),
                card.getExpirationYear(), card.getCvv());

        if (stripeCard.validateCard() && stripeCard.validateCVC()) {
            mStripe.createToken(stripeCard, new TokenCallback() {
                @Override
                public void onError(Exception e) {
                    tokenCallback.onFailure("Unable to create credit card token");
                }

                @Override
                public void onSuccess(Token token) {
                    tokenCallback.onTokenFetched(token);
                }
            });
        } else {
            tokenCallback.onFailure("Invalid credit card");
        }
    }

    public void getReceipts(final DataCallback<List<ReceiptData>> receiptCallback) {
        ReceiptsApi.getReceipts(new ReceiptsApi.ReceiptsCallback() {
            @Override
            public void success(List<ReceiptData> receipts) {
                receiptCallback.success(receipts);
            }

            @Override
            public void failure(int statusCode) {
                receiptCallback.failure("Unable to fetch receipts");
            }
        });
    }

    public void getLastTransaction(final DataCallback<ReceiptData> lastTransactionCallback) {
        getReceipts(new DataCallback<List<ReceiptData>>() {
            @Override
            public void success(List<ReceiptData> result) {
                if (result != null && result.size() > 0) {
                    Collections.sort(result, new Comparator<ReceiptData>() {
                        @Override
                        public int compare(ReceiptData lhs, ReceiptData rhs) {
                            return lhs.getDate().before(rhs.getDate()) ? 1 : -1;
                        }
                    });

                    lastTransactionCallback.success(result.get(0));
                } else {
                    lastTransactionCallback.failure("No transactions");
                }
            }

            @Override
            public void failure(String message) {
                lastTransactionCallback.failure(message);
            }
        });
    }
}
