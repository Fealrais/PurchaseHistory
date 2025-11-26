package com.angelp.purchasehistory.ui.home.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import com.angelp.purchasehistory.data.Constants;
import com.angelp.purchasehistory.data.factories.DashboardComponentsFactory;
import com.angelp.purchasehistory.data.interfaces.RefreshablePurchaseFragment;
import com.angelp.purchasehistory.data.model.DashboardComponent;
import com.angelp.purchasehistory.databinding.FragmentDashboardCardBinding;
import com.angelp.purchasehistory.ui.FullscreenGraphActivity;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class DashboardCardFragment extends Fragment {

    private DashboardComponent component;
    private int generatedId;
    private FragmentDashboardCardBinding binding;
    private RefreshablePurchaseFragment fragment;

    public DashboardCardFragment(DashboardComponent dashboardComponent) {
        this.component = dashboardComponent;
        generatedId = View.generateViewId();
        Bundle args = new Bundle();
        args.putParcelable(Constants.ARG_COMPONENT, dashboardComponent);
        args.putInt("viewId", generatedId);
        setArguments(args);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            component = getArguments().getParcelable(Constants.ARG_COMPONENT);
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
            startActivity(intent);
        });
        fragment = DashboardComponentsFactory.createFragment(component.getFragmentName());
        if (fragment.getArguments() != null) {
            fragment.getArguments().putInt(Constants.Arguments.ARG_MAX_SIZE, 6);
            fragment.getArguments().putBoolean(Constants.Arguments.ARG_SHOW_FILTER, false);
        }
        getChildFragmentManager().beginTransaction()
                .replace(binding.fragmentContainerView.getId(), fragment)
                .commit();
        return binding.getRoot();
    }

    /**
     *
     */
    @Override
    public void onDetach() {
        super.onDetach();
        RefreshablePurchaseFragment fragment = component.getFragment();
        if (fragment != null)
            getChildFragmentManager().beginTransaction().remove(fragment);
    }
}