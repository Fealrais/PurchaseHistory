package com.angelp.purchasehistory.ui.home.qr;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.angelp.purchasehistory.R;
import com.angelp.purchasehistory.databinding.CategorySpinnerItemBinding;
import com.angelp.purchasehistorybackend.models.views.outgoing.CategoryView;

import java.util.List;

import static com.angelp.purchasehistory.util.Utils.COLOR_REGEX;

public class CategorySpinnerAdapter extends ArrayAdapter<CategoryView> {
    CategorySpinnerItemBinding binding;

    public CategorySpinnerAdapter(@NonNull Context context, List<CategoryView> items) {
        super(context, R.layout.category_spinner_item, items);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = initView(convertView, parent);
        binding = CategorySpinnerItemBinding.bind(view);
        CategoryView item = getItem(position);
        if (item.getId() == null) binding.categoryColor.setVisibility(View.GONE);
        else {
            binding.categoryColor.setVisibility(View.VISIBLE);
            String color = COLOR_REGEX.matcher(item.getColor()).find() ? item.getColor() : "#c4c4c4";
            int parsedColor = Color.parseColor(color);
            binding.categoryColor.getBackground().setTint(parsedColor);
        }
        binding.categoryName.setText(item.getName());

        return binding.getRoot();
    }


    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getView(position, convertView, parent);
    }

    private View initView(View convertView,
                          ViewGroup parent) {
        // It is used to set our custom view.
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.category_spinner_item, parent, false);
        }

        return convertView;
    }

    @Override
    public long getItemId(int position) {
        CategoryView item = getItem(position);
        if (item == null || item.getId() == null) return Long.MIN_VALUE;
        return item.getId();
    }
}
