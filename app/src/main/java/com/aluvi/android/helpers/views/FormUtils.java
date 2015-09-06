package com.aluvi.android.helpers.views;

import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;

/**
 * Created by usama on 8/9/15.
 */
public class FormUtils {
    public static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public static FormValidator.Validator getPhoneValidator() {
        return new FormValidator.Validator() {
            @Override
            public boolean isValid(String input) {
                return isValidPhoneNumber(input);
            }
        };
    }

    public static FormValidator.Validator getEmailValidator() {
        return new FormValidator.Validator() {
            @Override
            public boolean isValid(String input) {
                return isValidEmail(input);
            }
        };
    }

    public static boolean isValidPhoneNumber(String number) {
        return PhoneNumberUtils.isGlobalPhoneNumber(number);
    }
}
