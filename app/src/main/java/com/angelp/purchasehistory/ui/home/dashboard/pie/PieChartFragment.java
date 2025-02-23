package com.angelp.purchasehistory.ui.home.dashboard.pie;

import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.ViewModelProvider;
import com.angelp.purchasehistory.R;
import com.angelp.purchasehistory.data.AppColorCollection;
import com.angelp.purchasehistory.data.Constants;
import com.angelp.purchasehistory.data.filters.PurchaseFilter;
import com.angelp.purchasehistory.data.interfaces.RefreshablePurchaseFragment;
import com.angelp.purchasehistory.databinding.FragmentPieChartBinding;
import com.angelp.purchasehistory.ui.home.dashboard.DashboardViewModel;
import com.angelp.purchasehistory.ui.home.dashboard.graph.CurrencyValueFormatter;
import com.angelp.purchasehistory.ui.home.dashboard.purchases.PurchaseFilterDialog;
import com.angelp.purchasehistory.util.AndroidUtils;
import com.angelp.purchasehistory.util.CommonUtils;
import com.angelp.purchasehistorybackend.models.views.outgoing.CategoryView;
import com.angelp.purchasehistorybackend.models.views.outgoing.analytics.CategoryAnalyticsEntry;
import com.angelp.purchasehistorybackend.models.views.outgoing.analytics.CategoryAnalyticsReport;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.MPPointF;
import dagger.hilt.android.AndroidEntryPoint;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@AndroidEntryPoint
public class PieChartFragment extends RefreshablePurchaseFragment implements OnChartValueSelectedListener {
    private static final String ARG_FILTER = "purchase_filter";
    private final String TAG = this.getClass().getSimpleName();
    private final PurchaseFilterDialog filterDialog = new PurchaseFilterDialog(true);
    private DashboardViewModel viewModel;
    private FragmentPieChartBinding binding;
    private boolean showFilter;
    private AppColorCollection appColorCollection;
    private Typeface tf;
    private Typeface tfBold;
    private PurchaseFilter previousFilter;
    private BigDecimal sum;

    public PieChartFragment() {
        Bundle args = new Bundle();
        this.setArguments(args);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            showFilter = getArguments().getBoolean(Constants.Arguments.ARG_SHOW_FILTER);
        }
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
        binding = FragmentPieChartBinding.inflate(inflater, container, false);
        appColorCollection = new AppColorCollection(inflater.getContext());
        tf = ResourcesCompat.getFont(inflater.getContext(), R.font.ibmplexmono_regular);
        tfBold = ResourcesCompat.getFont(inflater.getContext(), R.font.ibmplexmono_bold);
        super.setLoadingScreen(binding.loadingBar);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        applyFilter(filterViewModel.getFilterValue());
        initFilterRow();
        initPieChart(binding.pieChart);
        new Thread(() -> setData(filterViewModel.getFilterValue())).start();
    }

    private void applyFilter(PurchaseFilter newFilter) {
        binding.piechartFilterButton.setText(R.string.filterButton);
        binding.textView.setText(newFilter.getReadableString());
    }

    private void initFilterRow() {
        binding.piechartFilterButton.setOnClickListener((v) -> openFilter());
        binding.textView.setTextColor(getContext().getColor(R.color.foreground_color));
        new Handler(Looper.getMainLooper()).post(() -> {
            binding.piechartFilterButton.setVisibility(showFilter ? View.VISIBLE : View.GONE);
            binding.textView.setVisibility(showFilter ? View.VISIBLE : View.GONE);
        });
    }

    private void openFilter() {
        filterDialog.show(getParentFragmentManager(), "piechartFilterDialog");
    }

    private void setData(PurchaseFilter filter) {
        CategoryAnalyticsReport report = viewModel.getCategoryAnalyticsReport(filter);
        previousFilter = filter.copy();
        if (report == null) {
            binding.pieChart.setCenterText("Failed to load data.\nTry again later.");
            return;
        }
        List<PieEntry> entries = report.getContent().stream().map(this::parsePieEntries).collect(Collectors.toList());
        PieDataSet dataSet = new PieDataSet(entries, "Category");

        List<Integer> categoryColors = report.getContent().stream().map(entry -> AndroidUtils.getColor(entry.getCategory())
        ).collect(Collectors.toList());
        dataSet.setAutomaticallyDisableSliceSpacing(true);
        sum = report.getTotalSum();
        setPiechartCenterText(sum);

        dataSet.setDrawIcons(false);
        dataSet.setSliceSpace(3f);
        dataSet.setIconsOffset(new MPPointF(0, 40));
        dataSet.setSelectionShift(5f);
        dataSet.setColors(categoryColors);
        dataSet.setValueTextColors(categoryColors.stream().map(AndroidUtils::getTextColor).collect(Collectors.toList()));
        dataSet.setValueTextSize(12f);
        dataSet.setValueTypeface(tf);
        dataSet.setValueFormatter(new CurrencyValueFormatter(AndroidUtils.getCurrencySymbol(getContext())));

        PieData newData = new PieData(dataSet);

        new Handler(Looper.getMainLooper()).post(() -> {
            binding.pieChart.setData(newData);
            binding.pieChart.notifyDataSetChanged();
            binding.pieChart.animateY(1000);
            binding.pieChart.invalidate();
        });
    }

    private void setPiechartCenterText(BigDecimal sum) {
        String centerText = (sum == null) ? getString(R.string.no_data) : AndroidUtils.formatCurrency(sum, getContext());
        binding.pieChart.setCenterText(centerText);
        binding.pieChart.setCenterTextColor(R.color.primaryColor);
        binding.pieChart.setCenterTextSize(24);
        binding.pieChart.setCenterTextTypeface(tf);
    }

    private void setPiechartCenterText(String centerText, float secondValue, CategoryView category) {
        String name = category.getName();
        name = CommonUtils.limitString(name, 12);
        binding.pieChart.setCenterText(name + "\n" + centerText + "\n" + AndroidUtils.formatCurrency(secondValue, getContext()));
        binding.pieChart.setCenterTextSize(16);
        binding.pieChart.setCenterTextTypeface(tf);
    }

    private PieEntry parsePieEntries(CategoryAnalyticsEntry entry) {
        String name = entry.getCategory() != null && !entry.getCategory().getName().isBlank() ? entry.getCategory().getName() : "Unknown";
        return new PieEntry(entry.getSum().floatValue(), name, entry.getCategory());
    }

    private void initPieChart(PieChart chart) {
        chart.setUsePercentValues(false);
        chart.getDescription().setEnabled(false);
        chart.setExtraOffsets(5, 10, 5, 5);

        chart.setDragDecelerationFrictionCoef(0.95f);
        chart.setDrawEntryLabels(true);

        chart.setDrawHoleEnabled(true);
        chart.setHoleColor(appColorCollection.getForegroundColor());

        chart.setTransparentCircleColor(appColorCollection.getForegroundColor());
//        chart.setTransparentCircleAlpha(50);

//        chart.setHoleRadius(58f);
//        chart.setTransparentCircleRadius(61f);
        chart.setDrawCenterText(true);
        chart.setRotationAngle(0);
        chart.setElevation(5);

        chart.setEntryLabelTypeface(tf);
        // enable rotation of the chart by touch
        chart.setRotationEnabled(true);
        chart.setHighlightPerTapEnabled(true);
        chart.setRenderer(new ColoredLabelXAxisRenderer(chart, chart.getAnimator(), chart.getViewPortHandler()));

        // add a selection listener
        chart.setOnChartValueSelectedListener(this);

        Legend l = chart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setTypeface(tf);
        l.setTextColor(appColorCollection.getForegroundColor());
        l.setDrawInside(false);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(0f);
        l.setYOffset(0f);

        // entry label styling
        chart.setEntryLabelColor(appColorCollection.getForegroundColor());
    }


    @Override
    public void onValueSelected(Entry e, Highlight h) {

        if (e == null || e.getData() == null)
            return;
        Log.i(TAG, String.format("Selected value: %s, index: %s, DataSet index: %d", e.getY(), h.getX(), h.getDataSetIndex()));
        CategoryView category = (CategoryView) e.getData();
        PurchaseFilter filterValue = filterViewModel.getFilterValue();
        filterValue.setCategory(category);
        filterViewModel.updateFilter(filterValue);
        float percentages = (e.getY() / sum.floatValue()) * 100;
        setPiechartCenterText(String.format(Locale.getDefault(), "%.2f%%", percentages), e.getY(), category);

    }

    @Override
    public void onNothingSelected() {
        Log.i(TAG, "nothing selected");
        PurchaseFilter filterValue = filterViewModel.getFilterValue();
        filterValue.setCategory(null);
        filterViewModel.updateFilter(filterValue);
        setPiechartCenterText(sum);
    }

    public void refresh(PurchaseFilter filter) {
        if (isSameFilter(filter)) return;
        isRefreshing.postValue(true);
        new Thread(() -> {
            setData(filter);
            isRefreshing.postValue(false);
        }).start();
    }

    private boolean isSameFilter(PurchaseFilter filter) {
        if (previousFilter == null) return false;
        previousFilter.setCategoryId(filter.getCategoryId());
        return filter.equals(previousFilter);
    }
}