package com.aluvi.android.helpers;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Patterns;

import java.util.regex.Pattern;

/**
 * Created by usama on 8/15/15.
 */
public class ProfileUtils {
    public static String getUserPhoneNumber(Context context) {
        TelephonyManager tMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return tMgr != null ? tMgr.getLine1Number() : "";
    }

    public static String getUSUserPhoneNumber(Context context) {
        String phoneNumber = getUserPhoneNumber(context);
        if (phoneNumber != null && phoneNumber.length() == 11)
            phoneNumber = phoneNumber.substring(1); // Remove country code
        return phoneNumber;
    }

    public static String getUserEmailNumber(Context context) {
        Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
        Account[] accounts = AccountManager.get(context).getAccounts();
        for (Account account : accounts) {
            if (emailPattern.matcher(account.name).matches()) {
                return account.name;
            }
        }

        return null;
    }
}
