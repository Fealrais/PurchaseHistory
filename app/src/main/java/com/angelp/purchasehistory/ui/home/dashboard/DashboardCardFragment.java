package com.angelp.purchasehistory.ui.home.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.fragment.app.Fragment;
import com.angelp.purchasehistory.data.Constants;
import com.angelp.purchasehistory.data.factories.DashboardComponentsFactory;
import com.angelp.purchasehistory.data.interfaces.RefreshablePurchaseFragment;
import com.angelp.purchasehistory.data.model.DashboardComponent;
import com.angelp.purchasehistory.databinding.FragmentDashboardCardBinding;
import com.angelp.purchasehistory.ui.FullscreenGraphActivity;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@NoArgsConstructor
public class DashboardCardFragment extends Fragment {

    private DashboardComponent component;
    private int generatedId;
    private Integer marginBottom;
    private FragmentDashboardCardBinding binding;

    public DashboardCardFragment(DashboardComponent dashboardComponent) {
        this.component = dashboardComponent;
        generatedId = View.generateViewId();
        Bundle args = new Bundle();
        args.putParcelable(Constants.Arguments.ARG_COMPONENT, dashboardComponent);
        args.putInt(Constants.Arguments.VIEW_ID, generatedId);
        setArguments(args);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            component = getArguments().getParcelable(Constants.Arguments.ARG_COMPONENT);
            generatedId = getArguments().getInt(Constants.Arguments.VIEW_ID);
            marginBottom = getArguments().getInt(Constants.Arguments.MARGIN_BOTTOM);
        }
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDashboardCardBinding.inflate(inflater, container, false);
        binding.fragmentContainerView.setId(generatedId);
        binding.title.setText(component.getTitle());
        binding.imageButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), FullscreenGraphActivity.class);
            intent.putExtra(Constants.Arguments.ARG_COMPONENT, component);
            startActivity(intent);
        });
        RefreshablePurchaseFragment fragment = DashboardComponentsFactory.createFragment(component.getFragmentName());
        if (fragment.getArguments() != null) {
            fragment.getArguments().putInt(Constants.Arguments.ARG_MAX_SIZE, 6);
            fragment.getArguments().putBoolean(Constants.Arguments.ARG_SHOW_FILTER, false);
        }
        getChildFragmentManager().beginTransaction()
                .replace(binding.fragmentContainerView.getId(), fragment)
                .commit();
        setMarginBottomIfLast();
        return binding.getRoot();
    }

    private void setMarginBottomIfLast() {
        if (marginBottom == 0) return;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(16, 0, 16, marginBottom);
        binding.getRoot().setLayoutParams(params);
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