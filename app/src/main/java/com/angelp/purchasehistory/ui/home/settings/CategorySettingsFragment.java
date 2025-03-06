package com.angelp.purchasehistory.ui.home.settings;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import com.angelp.purchasehistory.R;
import com.angelp.purchasehistory.components.form.EditCategoryDialog;
import com.angelp.purchasehistory.util.AndroidUtils;
import com.angelp.purchasehistory.web.clients.PurchaseClient;
import com.angelp.purchasehistorybackend.models.views.outgoing.CategoryView;
import dagger.hilt.android.AndroidEntryPoint;

import javax.inject.Inject;
import java.util.List;

@AndroidEntryPoint
public class CategorySettingsFragment extends PreferenceFragmentCompat {

    @Inject
    PurchaseClient purchaseClient;
    private EditCategoryDialog editCategoryDialog;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.category_preferences, rootKey);
        PreferenceCategory category = findPreference("categories_preference_category");
        setupCategoryEdit(category);
    }

    private void setupCategoryEdit(PreferenceCategory category) {
        new Thread(() -> {
            List<CategoryView> allCategories = purchaseClient.getAllCategories();
            for (CategoryView categoryView : allCategories) {
                Preference categoryPreference = new Preference(getContext());
                setupCategory(categoryView, categoryPreference);
                categoryPreference.setOnPreferenceClickListener((p) -> {
                    editCategoryDialog = new EditCategoryDialog(categoryView.getId(), categoryView,
                            (newCategory) -> setupCategory(newCategory, categoryPreference));
                    editCategoryDialog.show(getParentFragmentManager(), "Edit_category");
                    return false;
                });
                Preference deletePreference = new Preference(getContext());
                deletePreference.setTitle("Delete");
                deletePreference.setOnPreferenceClickListener((p) -> {
                    new Thread(() -> {
//                        purchaseClient.deleteCategory(categoryView.getId());
                        new Handler(Looper.getMainLooper()).post(() -> category.removePreference(categoryPreference));
                    }).start();
                    return false;
                });
                category.addPreference(categoryPreference);
            }
        }).start();
    }

    private void setupCategory(CategoryView category, Preference categoryPreference) {
        Drawable unwrappedDrawable = AppCompatResources.getDrawable(getContext(), R.drawable.circle);
        Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
        DrawableCompat.setTint(wrappedDrawable, AndroidUtils.getColor(category));
        new Handler(Looper.getMainLooper()).post(() -> {
            categoryPreference.setTitle(category.getName());
            categoryPreference.setSummary(category.getColor());
            categoryPreference.setIcon(wrappedDrawable);
        });
    }


}