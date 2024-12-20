package com.angelp.purchasehistory.ui.home.graph;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.angelp.purchasehistorybackend.models.views.outgoing.PurchaseView;
import com.angelp.purchasehistory.databinding.FragmentGraphBinding;
import com.angelp.purchasehistory.ui.home.dashboard.DashboardViewModel;
import com.tradingview.lightweightcharts.api.chart.models.color.IntColor;
import com.tradingview.lightweightcharts.api.interfaces.SeriesApi;
import com.tradingview.lightweightcharts.api.options.models.ChartOptions;
import com.tradingview.lightweightcharts.api.options.models.HistogramSeriesOptions;
import com.tradingview.lightweightcharts.api.series.models.HistogramData;
import com.tradingview.lightweightcharts.api.series.models.Time;
import dagger.hilt.android.AndroidEntryPoint;
import kotlin.Unit;
import lombok.NoArgsConstructor;

import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
@AndroidEntryPoint
public class GraphFragment extends Fragment {

    private final String TAG = this.getClass().getSimpleName();
    private final ChartOptions chartOptions = new ChartOptions();
    private FragmentGraphBinding binding;
    private DashboardViewModel dashboardViewModel;
    private SeriesApi histogramSeries;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: View created");

        dashboardViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
        binding = FragmentGraphBinding.inflate(inflater, container, false);
        initLineGraph();

        return binding.getRoot();
    }

//todo: check graph error
    private void initLineGraph() {
        binding.graphLineChart.getApi().applyOptions(this.chartOptions);
        HistogramSeriesOptions options = new HistogramSeriesOptions();
        binding.graphLineChart.getApi().addHistogramSeries(options, (SeriesApi hs) -> {
            histogramSeries = hs;
            new Thread(() -> {
                Log.i(TAG, "initLineGraph: Started");
                List<PurchaseView> purchases = dashboardViewModel.getAllPurchases();
                List<HistogramData> collect = purchases.stream()
                        .filter(purchase -> purchase.getPrice() != null)
                        .map(purchase -> {
                            int color = Color.parseColor(purchase.getCategory() == null ? "#c4c4c4" : purchase.getCategory().getColor());
                            return new HistogramData(
                                    new Time.Utc(purchase.getTimestamp().atZone(ZoneId.systemDefault()).toEpochSecond()),
                                    purchase.getPrice().floatValue(), new IntColor(color));
                        })
                        .collect(Collectors.toList());
                Log.i(TAG, "initLineGraph: series calculated");
                try {
                    histogramSeries.setData(collect);
                } catch (IllegalStateException e) {
                    Log.e(TAG, "initLineGraph: " + e.getMessage());
                }
            }).start();
            return Unit.INSTANCE;
        });

    }
}