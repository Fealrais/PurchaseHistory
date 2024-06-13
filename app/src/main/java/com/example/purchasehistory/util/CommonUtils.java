package com.example.purchasehistory.util;

import java.util.List;
import java.util.function.Predicate;

public final class CommonUtils {
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
}
