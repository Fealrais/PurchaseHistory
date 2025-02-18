package com.angelp.purchasehistory.ui.home.scheduled;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.angelp.purchasehistory.PurchaseHistoryApplication;
import com.angelp.purchasehistory.R;
import com.angelp.purchasehistory.data.Constants;
import com.angelp.purchasehistory.data.model.ScheduledNotification;
import com.angelp.purchasehistory.databinding.FragmentScheduledExpensesBinding;
import com.angelp.purchasehistory.receivers.scheduled.InitiateNotificationReceiver;
import com.angelp.purchasehistory.web.clients.ScheduledExpenseClient;
import com.angelp.purchasehistorybackend.models.views.outgoing.ScheduledExpenseView;
import com.google.gson.Gson;
import dagger.hilt.android.AndroidEntryPoint;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static com.angelp.purchasehistory.data.Constants.Arguments.NOTIFICATION_EXTRA_ARG;

@AndroidEntryPoint
public class ScheduledExpensesFragment extends Fragment {

    public static final String CREATE_SCHEDULED_EXPENSE_DIALOG = "createScheduledExpenseDialog";
    private FragmentScheduledExpensesBinding binding;
    @Inject
    ScheduledExpenseClient scheduledExpenseClient;
    private final Gson gson = new Gson();
    private ScheduledExpenseAdapter adapter;
    private EditScheduledExpenseDialog editScheduledExpenseDialog;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentScheduledExpensesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);

        setupRecycleView();
        setupAddButton();
    }

    private void setupRecycleView() {
        new Thread(() -> {
            List<ScheduledExpenseView> scheduledExpenses = scheduledExpenseClient.findAllForUser();
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

            adapter = new ScheduledExpenseAdapter(scheduledExpenses, new ScheduledExpenseAdapter.OnItemClickListener() {
                @Override
                public void onSilenceToggleTrigger(ScheduledExpenseView item, boolean silenced) {
                    new Thread(() -> {
                        Context context = getContext();
                        if (context == null) return;
                        SharedPreferences preferences = context.getSharedPreferences(Constants.Preferences.SILENCED_NOTIFICATIONS, MODE_PRIVATE);
                        preferences.edit().putBoolean(item.getId().toString(), silenced).apply();
                        addNotificationAlarm(context, item);
                    }).start();
                }

                @Override
                public void onTriggerClick(ScheduledExpenseView item) {
                    new Thread(() -> {
                        try {
                            scheduledExpenseClient.triggerScheduledPurchase(item.getId());
                            PurchaseHistoryApplication.getInstance().alert(R.string.purchase_created_title);
                        } catch (Exception e) {
                            PurchaseHistoryApplication.getInstance().alert("Error deleting scheduled expense: " + e.getMessage());
                        }
                    }).start();
                }

                @Override
                public void onEditClick(ScheduledExpenseView item) {
                    editScheduledExpenseDialog = new EditScheduledExpenseDialog(item, (view) -> {
                        int index = adapter.getScheduledExpenses().indexOf(view);
                        if (index == -1) return;
                        adapter.getScheduledExpenses().remove(index);
                        adapter.getScheduledExpenses().add(index, view);
                        adapter.notifyItemChanged(index, view);
                        resetAlarms(adapter.getScheduledExpenses());
                    });
                    editScheduledExpenseDialog.show(getParentFragmentManager(), "editScheduledExpenseDialog");
                }

                @Override
                public void onDeleteClick(ScheduledExpenseView item) {
                    new Thread(() -> {
                        try {
                            boolean success = scheduledExpenseClient.deleteScheduledExpense(item.getId());
                            new Handler(Looper.getMainLooper()).post(() -> {
                                if (success) {
                                    int index = adapter.getScheduledExpenses().indexOf(item);
                                    adapter.getScheduledExpenses().remove(index);
                                    adapter.notifyItemRemoved(index);
                                    resetAlarms(adapter.getScheduledExpenses());
                                } else {
                                    PurchaseHistoryApplication.getInstance().alert("Error deleting scheduled expense.");
                                }
                            });

                        } catch (Exception e) {
                            PurchaseHistoryApplication.getInstance().alert("Error deleting scheduled expense: " + e.getMessage());
                        }
                    }).start();
                }
            });

            new Handler(Looper.getMainLooper()).post(() -> {
                binding.recyclerViewScheduledExpenses.setLayoutManager(linearLayoutManager);
                binding.recyclerViewScheduledExpenses.setAdapter(adapter);
            });
        }).start();
        binding.swiperefreshScheduledExpenses.setOnRefreshListener(() -> {
            refresh();
            binding.swiperefreshScheduledExpenses.setRefreshing(false);
        });
    }

    private void refresh() {
        new Thread(() -> {
            adapter.getScheduledExpenses().clear();
            List<ScheduledExpenseView> allForUser = scheduledExpenseClient.findAllForUser();
            ScheduledExpenseAdapter.sort(allForUser);
            adapter.getScheduledExpenses().addAll(allForUser);
            new Handler(Looper.getMainLooper()).post(() -> adapter.notifyDataSetChanged());
        }).start();
    }

    private void resetAlarms(List<ScheduledExpenseView> scheduledExpenses) {
        if (getContext() == null) return;
        Intent intent = new Intent(getContext(), InitiateNotificationReceiver.class);
        ArrayList<ScheduledNotification> list = new ArrayList<>();
        for (ScheduledExpenseView scheduledExpenseView : scheduledExpenses) {
            ScheduledNotification scheduledNotification = new ScheduledNotification(scheduledExpenseView);
            list.add(scheduledNotification);
        }
        intent.putParcelableArrayListExtra(NOTIFICATION_EXTRA_ARG, list);
        getContext().sendBroadcast(intent);
    }


    private void setupAddButton() {
        binding.fabAddScheduledExpense.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            } else {
                CreateScheduledExpenseDialog createDialog = new CreateScheduledExpenseDialog(this::acceptCreateScheduledExpenseResult);
                createDialog.show(getParentFragmentManager(), CREATE_SCHEDULED_EXPENSE_DIALOG);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                CreateScheduledExpenseDialog createDialog = new CreateScheduledExpenseDialog(this::acceptCreateScheduledExpenseResult);
                createDialog.show(getParentFragmentManager(), CREATE_SCHEDULED_EXPENSE_DIALOG);
            } else {
                PurchaseHistoryApplication.getInstance().alert(R.string.notification_required_warning);
            }
        }
    }

    private void addNotificationAlarm(Context context, ScheduledExpenseView newExpense) {
        if(context == null) return;
        ScheduledNotification scheduledNotification = new ScheduledNotification(newExpense);
        Intent intent = new Intent(context, InitiateNotificationReceiver.class);
        ArrayList<ScheduledNotification> list = new ArrayList<>();
        list.add(scheduledNotification);
        intent.putParcelableArrayListExtra(Constants.Arguments.NOTIFICATION_EXTRA_ARG, list);
        context.sendBroadcast(intent);

    }

    private void acceptCreateScheduledExpenseResult(ScheduledExpenseView newExpense) {
        addNotificationAlarm(getContext(), newExpense);
        adapter.getScheduledExpenses().add(newExpense);
        adapter.notifyItemInserted(adapter.getItemCount() - 1);
    }
}