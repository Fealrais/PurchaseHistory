package com.angelp.purchasehistory.util;

import android.util.Log;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public final class CommonUtils {
    public final static Pattern COLOR_REGEX = Pattern.compile("^#([a-fA-F0-9]{6}|[a-fA-F0-9]{3})$");

    public static <T> int findIndex(List<T> allCategories, Predicate<T> predicate) {
        int index = -1;
        for (int i = 0; i < allCategories.size(); i++) {
            T category = allCategories.get(i);
            if (predicate.test(category)) {
                index = i;
                break;
            }
        }
        return index;
    }
    public static boolean isInvalidCurrency(String str) {
        if (str == null || !str.matches("^[\\d.]+$")) return true;
        try {
            BigDecimal value = new BigDecimal(str);
            return !(value.floatValue() >= 0);
        } catch (NumberFormatException e) {
            Log.e("Currency_validation", "isValidCurrency:" + e);
            return true;
        }
    }

}
