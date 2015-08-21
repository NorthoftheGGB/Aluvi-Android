package com.aluvi.android.helpers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by usama on 8/20/15.
 */
public class CurrencyUtils {
    public static String getFormattedDollars(int cents) {
        BigDecimal displayDecimal = new BigDecimal(cents)
                .divide(new BigDecimal(100))
                .setScale(2, RoundingMode.HALF_EVEN);
        return NumberFormat.getCurrencyInstance(Locale.getDefault()).format(displayDecimal.doubleValue());
    }
}
