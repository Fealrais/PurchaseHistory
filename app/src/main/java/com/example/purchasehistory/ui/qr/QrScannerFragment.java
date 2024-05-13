package com.example.purchasehistory.ui.qr;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.purchasehistory.PurchaseHistoryApplication;
import com.example.purchasehistory.databinding.FragmentQrBinding;
import com.google.zxing.integration.android.IntentIntegrator;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class QrScannerFragment extends Fragment {
    private static final String TAG = "QRCodeFragment";
    private static final String ScanResultExtra = "SCAN_RESULT";
    private QrScannerViewModel qrScannerViewModel;
    private final ActivityResultLauncher<Intent> getQRResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent intent = result.getData();
                    if (intent != null) {

                        String data = intent.getStringExtra(ScanResultExtra);
                        Log.i(TAG, "Scanned: " + data);
                        fillFormFromQrCode(data);
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
    private FragmentQrBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: View created");

        qrScannerViewModel = new ViewModelProvider(this).get(QrScannerViewModel.class);
        openCameraFlow(inflater);
        binding = FragmentQrBinding.inflate(inflater, container, false);
        initQRForm();

        return binding.getRoot();
    }

    private void openCameraFlow(LayoutInflater inflater) {
        if (ContextCompat.checkSelfPermission(inflater.getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            initQRCodeScanner();
        } else {
            requestPermission.launch(Manifest.permission.CAMERA);
        }
    }

    private void initQRForm() {
        binding.qrFloatingQrButton.setOnClickListener((view)-> initQRCodeScanner());
        binding.qrClearButton.setOnClickListener((view)->{
            binding.qrPriceInput.getText().clear();
            binding.qrDateInput.getText().clear();
            binding.qrTimeInput.getText().clear();
        });
        binding.qrSubmitButton.setOnClickListener((view)->{
            Log.i(TAG, "Submit is WIP");
//            String price = binding.qrPriceInput.getText().toString();
//            String date = binding.qrDateInput.getText().toString();
//            String time = binding.qrTimeInput.getText().toString();
//            String categoryId = binding.qrCategorySpinner.getSelectedItem().getCategoryId();

//            todo: create object and send it to the backend

//            onSubmit();
        });
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

    private void fillFormFromQrCode(String data) {

//        todo: instead of sending the data, fill the form with what you have
        onSubmit(data);
    }

    private void onSubmit(String data) {
        new Thread(qrScannerViewModel.createPurchaseView(data)).start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}