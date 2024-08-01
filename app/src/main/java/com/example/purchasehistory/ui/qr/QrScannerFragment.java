package com.example.purchasehistory.ui.qr;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.angelp.purchasehistorybackend.models.views.incoming.PurchaseDTO;
import com.angelp.purchasehistorybackend.models.views.outgoing.CategoryView;
import com.angelp.purchasehistorybackend.models.views.outgoing.PurchaseView;
import com.example.purchasehistory.PurchaseHistoryApplication;
import com.example.purchasehistory.R;
import com.example.purchasehistory.components.form.CreateCategoryDialog;
import com.example.purchasehistory.components.form.DatePickerFragment;
import com.example.purchasehistory.components.form.TimePickerFragment;
import com.example.purchasehistory.databinding.FragmentQrBinding;
import com.example.purchasehistory.util.AfterTextChangedWatcher;
import com.google.zxing.integration.android.IntentIntegrator;
import dagger.hilt.android.AndroidEntryPoint;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@AndroidEntryPoint
public class QrScannerFragment extends Fragment {
    private static final String TAG = "QRCodeFragment";
    private static final String ScanResultExtra = "SCAN_RESULT";
    private QrScannerViewModel qrScannerViewModel;
    private FragmentQrBinding binding;
    private final ActivityResultLauncher<Intent> getQRResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent intent = result.getData();
                    if (intent != null) {
                        String data = intent.getStringExtra(ScanResultExtra);
                        Log.i(TAG, "Scanned: " + data);
                        fillQRForm(new PurchaseDTO(data));
                        Toast.makeText(getContext(), "Scanned : " + data, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getContext(), "Cancelled", Toast.LENGTH_LONG).show();
                    }
                }
            });
    private final ActivityResultLauncher<String> requestPermission = registerForActivityResult(new ActivityResultContracts.RequestPermission(), (isGranted) -> {
        if (isGranted) {
            initQRCodeScanner();
        } else {
            PurchaseHistoryApplication.getInstance().getApplicationContext().getMainExecutor().execute(() -> Toast.makeText(getContext(), "The application cannot function without this", Toast.LENGTH_SHORT).show());
        }
    });
    private List<CategoryView> allCategories = new ArrayList<>();
    private TimePickerFragment timePicker;
    private DatePickerFragment datePicker;
    private CreateCategoryDialog categoryDialog;
    private ArrayAdapter<CategoryView> categoryAdapter;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: View created");

        qrScannerViewModel = new ViewModelProvider(this).get(QrScannerViewModel.class);
        binding = FragmentQrBinding.inflate(inflater, container, false);
        initQRForm(inflater);
        getChildFragmentManager().setFragmentResultListener("categoryResult", getViewLifecycleOwner(), (requestKey, result) -> {
            CategoryView newCategoryView = result.getParcelable("newCategoryView");
            if (newCategoryView != null) categoryAdapter.add(newCategoryView);
        });


        timePicker.getTimeResult().observe(getViewLifecycleOwner(), (v) -> {
                PurchaseDTO value = qrScannerViewModel.getCurrentPurchaseDTO();
                value.setTime(v);
                fillQRForm(value);

        });
        datePicker.getDateResult().observe(getViewLifecycleOwner(), (v) -> {
                PurchaseDTO value = qrScannerViewModel.getCurrentPurchaseDTO();
                value.setDate(v);
                fillQRForm(value);
        });
        binding.qrPriceInput.addTextChangedListener(new AfterTextChangedWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                PurchaseDTO value = new PurchaseDTO();
                String str = binding.qrPriceInput.getText().toString();
                if (str.trim().isEmpty()) value.setPrice(new BigDecimal(BigInteger.ZERO));
                else value.setPrice(new BigDecimal(str));
                qrScannerViewModel.updatePurchaseDTO(value);
            }
        });
        binding.qrTimeInput.setOnClickListener((v) -> timePicker.show(getParentFragmentManager(), "timePicker"));
        binding.qrDateInput.setOnClickListener((v) -> datePicker.show(getParentFragmentManager(), "datePicker"));
        binding.qrCategoryAddButton.setOnClickListener((v) -> categoryDialog.show(getParentFragmentManager(), "createCategoryDialog"));
        binding.qrCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                CategoryView categoryView = allCategories.get(position);
                PurchaseDTO value = qrScannerViewModel.getCurrentPurchaseDTO();
                value.setCategoryId(categoryView.getId());
                qrScannerViewModel.updatePurchaseDTO(value);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                PurchaseDTO value = qrScannerViewModel.getCurrentPurchaseDTO();
                value.setCategoryId(null);
                qrScannerViewModel.getPurchaseDTO().postValue(value);
            }
        });
        qrScannerViewModel.getPurchaseDTO().observe(getViewLifecycleOwner(), (dto) -> {
            String storeId = dto.getStoreId() == null ? "-" : dto.getStoreId();
            binding.qrStoreIdValue.setText(storeId);
            String billId = dto.getBillId() == null ? "-" : dto.getBillId();
            binding.qrBillIdValue.setText(billId);
        });

        new Thread(() -> {
            allCategories = qrScannerViewModel.getAllCategories();
            categoryAdapter = new ArrayAdapter<>(this.getContext(), android.R.layout.simple_spinner_item, allCategories);
            getActivity().runOnUiThread(() -> binding.qrCategorySpinner.setAdapter(categoryAdapter));
        }).start();
        return binding.getRoot();
    }

    private void fillQRForm(PurchaseDTO purchaseDTO) {
        new Thread(() -> {
            qrScannerViewModel.updatePurchaseDTO(purchaseDTO);
            if (purchaseDTO.getPrice() != null)
                binding.qrPriceInput.setText(String.format(purchaseDTO.getPrice().toString()));
            if (purchaseDTO.getTimestamp() != null) {
                binding.qrDateInput.setText(purchaseDTO.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE));
                binding.qrTimeInput.setText(purchaseDTO.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_TIME));
            }
        }).start();

    }

    private void openCameraFlow(LayoutInflater inflater) {
        if (ContextCompat.checkSelfPermission(inflater.getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            initQRCodeScanner();
        } else {
            requestPermission.launch(Manifest.permission.CAMERA);
        }
    }

    private void initQRForm(LayoutInflater inflater) {
        timePicker = new TimePickerFragment();
        datePicker = new DatePickerFragment();
        categoryDialog = new CreateCategoryDialog();
        binding.qrFloatingQrButton.setOnClickListener((view) -> openCameraFlow(inflater));
        binding.qrClearButton.setOnClickListener(v -> resetForm());
        binding.qrSubmitButton.setOnClickListener((view) -> {
            Log.i(TAG, "Submit is WIP");
            onSubmit(qrScannerViewModel.getPurchaseDTO().getValue());
        });

    }

    private void resetForm() {
        qrScannerViewModel.getPurchaseDTO().postValue(new PurchaseDTO());
        binding.qrCategorySpinner.setSelection(0);
        binding.qrPriceInput.getText().clear();
        binding.qrDateInput.setText(R.string.date);
        binding.qrTimeInput.setText(R.string.time);
    }

    private void initQRCodeScanner() {
        Log.i(TAG, "initQRCodeScanner: STARTED");
        IntentIntegrator integrator = IntentIntegrator.forSupportFragment(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setOrientationLocked(true);
        integrator.setBarcodeImageEnabled(true);
        integrator.setCaptureActivity(CaptureActivityPortrait.class);
        integrator.setPrompt("Scan the QR code from a bill");
        Intent scanIntent = integrator.createScanIntent();
        getQRResult.launch(scanIntent);
        integrator.initiateScan();
    }


    private void onSubmit(PurchaseDTO data) {
        new Thread(() -> {
            PurchaseView purchaseView = qrScannerViewModel.createPurchaseView(data);
            if (purchaseView != null) {
                PurchaseHistoryApplication.getInstance().alert("Created purchase #" + purchaseView.getBillId() + ". Cost:" + purchaseView.getPrice());
                if (getActivity() != null)
                    getActivity().runOnUiThread(this::resetForm);
            } else
                PurchaseHistoryApplication.getInstance().alert("Failed to register purchase #");
        }).start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}