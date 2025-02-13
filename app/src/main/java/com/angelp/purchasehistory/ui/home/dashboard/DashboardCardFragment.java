package com.angelp.purchasehistory.ui.home.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import com.angelp.purchasehistory.data.Constants;
import com.angelp.purchasehistory.data.factories.DashboardComponentsFactory;
import com.angelp.purchasehistory.data.filters.PurchaseFilter;
import com.angelp.purchasehistory.data.interfaces.RefreshablePurchaseFragment;
import com.angelp.purchasehistory.data.model.DashboardComponent;
import com.angelp.purchasehistory.databinding.FragmentDashboardCardBinding;
import com.angelp.purchasehistory.ui.FullscreenGraphActivity;
import lombok.NoArgsConstructor;

import java.util.function.Consumer;

@NoArgsConstructor
public class DashboardCardFragment extends Fragment implements RefreshablePurchaseFragment {

    private DashboardComponent component;
    private PurchaseFilter filter;
    private Consumer<PurchaseFilter> setFilter;
    private int generatedId;
    private FragmentDashboardCardBinding binding;
    private RefreshableFragment fragment;

    public DashboardCardFragment(DashboardComponent dashboardComponent, PurchaseFilter filter, Consumer<PurchaseFilter> setFilter) {
        this.component = dashboardComponent;
        this.filter = filter;
        this.setFilter = setFilter;
        generatedId = View.generateViewId();
        Bundle args = new Bundle();
        args.putParcelable(Constants.ARG_COMPONENT, dashboardComponent);
        args.putParcelable(Constants.ARG_FILTER, filter);
        args.putInt("viewId", generatedId);
        setArguments(args);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            component = getArguments().getParcelable(Constants.ARG_COMPONENT);
            filter = getArguments().getParcelable(Constants.ARG_FILTER);
            generatedId = getArguments().getInt("viewId");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDashboardCardBinding.inflate(inflater, container, false);
        binding.fragmentContainerView.setId(generatedId);
        binding.title.setText(component.getTitle());
        binding.imageButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), FullscreenGraphActivity.class);
            intent.putExtra(Constants.ARG_COMPONENT, component);
            intent.putExtra(Constants.ARG_FILTER, filter);
            startActivity(intent);
        });
        fragment = DashboardComponentsFactory.createFragment(component.getFragmentName(), filter, setFilter);
        if (fragment.getArguments() != null) {
            fragment.getArguments().putInt(Constants.ARG_MAX_SIZE, 10);
            fragment.getArguments().putBoolean(Constants.ARG_SHOW_FILTER, false);
        }
        getParentFragmentManager().beginTransaction()
                .replace(binding.fragmentContainerView.getId(), fragment)
                .commit();
        return binding.getRoot();
    }

    @Override
    public void refresh(PurchaseFilter filter) {
        fragment.refresh(filter);
    }

    /**
     *
     */
    @Override
    public void onDetach() {
        super.onDetach();
        RefreshableFragment fragment = component.getFragment();
        if (fragment != null)
            getParentFragmentManager().beginTransaction().remove(fragment);
    }
}