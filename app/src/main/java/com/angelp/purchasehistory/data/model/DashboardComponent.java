package com.angelp.purchasehistory.data.model;

import com.angelp.purchasehistory.data.factories.DashboardComponentsFactory;
import com.angelp.purchasehistory.data.filters.PurchaseFilter;
import com.angelp.purchasehistory.ui.home.dashboard.RefreshableFragment;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.function.Consumer;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DashboardComponent implements Serializable {
    private int title;
    private int cardIconId;
    private int description;
    private boolean visible;
    private String fragmentName;
    private transient RefreshableFragment fragment;

    public DashboardComponent(int title, int cardIconId, int description, String fragment) {
        this.title = title;
        this.cardIconId = cardIconId;
        this.description = description;
        this.fragmentName = fragment;
        this.visible = true;
    }

    public RefreshableFragment getFragment(PurchaseFilter filter, Consumer<PurchaseFilter> setFilter) {
        if (fragment == null)
            this.fragment = DashboardComponentsFactory.createFragment(fragmentName, filter, setFilter);
        return fragment;
    }
}
