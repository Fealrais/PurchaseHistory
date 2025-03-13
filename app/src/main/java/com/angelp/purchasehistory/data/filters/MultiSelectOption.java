package com.angelp.purchasehistory.data.filters;

import android.graphics.drawable.Drawable;

public interface MultiSelectOption {

    Drawable getDrawable();

    String getLabel();

    void setSelected(boolean isChecked);
    Boolean isSelected();
}
