package com.example.purchasehistory.data.interfaces;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public abstract class ViewHolder<T> extends RecyclerView.ViewHolder{
    public ViewHolder(@NonNull @NotNull View itemView) {
        super(itemView);
    }
    abstract public void bind(T object, FragmentManager fragmentManager, Consumer<Long> onItemDelete);
}
