package com.angelp.purchasehistory.ui.home.dashboard.pie;

import android.graphics.Canvas;
import android.graphics.Paint;
import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.renderer.PieChartRenderer;
import com.github.mikephil.charting.utils.ViewPortHandler;

public class ColoredLabelXAxisRenderer extends PieChartRenderer {


    public ColoredLabelXAxisRenderer(PieChart chart, ChartAnimator animator, ViewPortHandler viewPortHandler) {
        super(chart, animator, viewPortHandler);
    }

    @Override
    protected void drawEntryLabel(Canvas c, String label, float x, float y) {
        Paint paint = new Paint();
        paint.set(mValuePaint);
        c.drawText(label, x, y, paint);
    }
}
