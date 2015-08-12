package com.aluvi.android.managers;

import com.aluvi.android.managers.packages.DataCallback;
import com.aluvi.android.model.local.CreditCard;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;
import com.stripe.exception.AuthenticationException;

/**
 * Created by usama on 8/11/15.
 */
public class PaymentManager {
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

    public void requestToken(CreditCard card, final DataCallback<String> tokenCallback) {
        Card stripeCard = new Card(card.getCardNumber(), card.getExpirationMonth(),
                card.getExpirationYear(), card.getCvv());

        if (stripeCard.validateCard() && stripeCard.validateCVC()) {
            mStripe.createToken(stripeCard, new TokenCallback() {
                @Override
                public void onError(Exception e) {
                    tokenCallback.failure("Unable to create credit card token");
                }

                @Override
                public void onSuccess(Token token) {
                    tokenCallback.success(token.getId());
                }
            });
        } else {
            tokenCallback.failure("Invalid credit card");
        }
    }
}
