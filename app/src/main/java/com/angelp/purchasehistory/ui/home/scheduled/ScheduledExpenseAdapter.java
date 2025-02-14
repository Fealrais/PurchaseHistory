package com.angelp.purchasehistory.ui.home.scheduled;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.angelp.purchasehistory.R;
import com.angelp.purchasehistory.util.AndroidUtils;
import com.angelp.purchasehistorybackend.models.views.outgoing.ScheduledExpenseView;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

import static com.angelp.purchasehistory.util.AndroidUtils.setNextTimestampString;

@Getter
public class ScheduledExpenseAdapter extends RecyclerView.Adapter<ScheduledExpenseAdapter.ViewHolder> {

    private final List<ScheduledExpenseView> scheduledExpenses;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onEditClick(ScheduledExpenseView item);
        void onDeleteClick(ScheduledExpenseView item);

        void onTriggerClick(ScheduledExpenseView scheduledExpense);
    }

    public ScheduledExpenseAdapter(List<ScheduledExpenseView> scheduledExpenses, OnItemClickListener listener) {
        sort(scheduledExpenses);
        this.scheduledExpenses = scheduledExpenses;
        this.listener = listener;
    }

    public static void sort(List<ScheduledExpenseView> scheduledExpenses) {
        scheduledExpenses.sort((a, b) -> {
            LocalDateTime nextTimestampA = a.getPeriod().getNextTimestamp(a.getTimestamp());
            LocalDateTime nextTimestampB = b.getPeriod().getNextTimestamp(b.getTimestamp());
            return nextTimestampA.compareTo(nextTimestampB);
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_view_scheduled_expense, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ScheduledExpenseView scheduledExpense = scheduledExpenses.get(position);
        holder.bind(scheduledExpense, listener);
    }

    @Override
    public int getItemCount() {
        return scheduledExpenses.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewName;
        private final TextView textViewPrice;
        private final TextView textViewNextDate;
        private final View viewCategoryBorder;
        private final View menuOptions;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            viewCategoryBorder = itemView.findViewById(R.id.viewCategoryBorder);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewPrice = itemView.findViewById(R.id.textViewPrice);
            textViewNextDate = itemView.findViewById(R.id.textViewNextDate);
            menuOptions = itemView.findViewById(R.id.menuOptions);
        }

        public void bind(ScheduledExpenseView scheduledExpense, OnItemClickListener listener) {
            viewCategoryBorder.setBackgroundColor(AndroidUtils.getColor(scheduledExpense.getCategory()));
            textViewName.setText(scheduledExpense.getNote());
            textViewPrice.setText(String.valueOf(scheduledExpense.getPrice()));
            setNextTimestampString(textViewNextDate, scheduledExpense);
            menuOptions.setOnClickListener(view -> {
                PopupMenu popup = new PopupMenu(super.itemView.getContext(), menuOptions);
                popup.inflate(R.menu.scheduled_purchase_options_menu);
                popup.setOnMenuItemClickListener(item -> {
                    int itemId = item.getItemId();
                    if (itemId == R.id.menu_edit) {
                        listener.onEditClick(scheduledExpense);
                        return true;
                    } else if (itemId == R.id.menu_delete) {
                        listener.onDeleteClick(scheduledExpense);
                        return true;
                    } else if (itemId == R.id.menu_trigger) {
                        listener.onTriggerClick(scheduledExpense);
                        return true;
                    }
                    return false;
                });
                popup.show();
            });
        }


    }
}