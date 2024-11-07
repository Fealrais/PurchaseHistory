package com.example.purchasehistory.ui.home.qr;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.angelp.purchasehistorybackend.models.views.outgoing.CategoryView;
import com.example.purchasehistory.R;
import com.example.purchasehistory.databinding.CategorySpinnerItemBinding;

import java.util.List;

import static com.example.purchasehistory.util.CommonUtils.COLOR_REGEX;

public class CategorySpinnerAdapter extends ArrayAdapter<CategoryView> {
    private final List<CategoryView> items;
    CategorySpinnerItemBinding binding;

    public CategorySpinnerAdapter(@NonNull Context context, List<CategoryView> items) {
        super(context, R.layout.category_spinner_item);
        this.items = items;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = initView(convertView,parent);
        binding = CategorySpinnerItemBinding.bind(view);
        CategoryView item = getItem(position);
        binding.categoryName.setText(item.getName());

        String color = COLOR_REGEX.matcher(item.getColor()).find() ? item.getColor() : "#c4c4c4";
        int parsedColor = Color.parseColor(color);
        if (Color.luminance(parsedColor) > 0.5)
            binding.categoryName.setTextColor(Color.BLACK);
        else
            binding.categoryName.setTextColor(Color.WHITE);
        binding.categoryName.setBackgroundColor(parsedColor);
        return binding.getRoot();
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getView(position, convertView, parent);
    }

    private View initView(View convertView,
                          ViewGroup parent)
    {
        // It is used to set our custom view.
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.category_spinner_item, parent, false);
        }

        return convertView;
    }
    @Nullable
    @Override
    public CategoryView getItem(int position) {
        return items.get(position);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }
}
