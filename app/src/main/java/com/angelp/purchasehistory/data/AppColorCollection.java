package com.angelp.purchasehistory.data;

import android.content.Context;
import com.angelp.purchasehistory.R;
import lombok.Getter;

@Getter
public class AppColorCollection {
    private final int foregroundColor;
    private final int backgroundColor;
    private final int middleColor;

    public AppColorCollection(Context context) {
        this.foregroundColor = context.getColor(R.color.foreground_color);
        this.backgroundColor = context.getColor(R.color.background_color);
        this.middleColor = context.getColor(R.color.middle_color);
    }
}
