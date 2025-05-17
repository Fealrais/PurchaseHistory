package com.angelp.purchasehistory.ui.home.qr;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.angelp.purchasehistory.PurchaseHistoryApplication;
import com.angelp.purchasehistory.R;
import com.angelp.purchasehistory.components.form.CreateCategoryDialog;
import com.angelp.purchasehistory.components.form.DatePickerFragment;
import com.angelp.purchasehistory.components.form.TimePickerFragment;
import com.angelp.purchasehistory.data.Constants;
import com.angelp.purchasehistory.data.model.ScheduledNotification;
import com.angelp.purchasehistory.databinding.FragmentQrBinding;
import com.angelp.purchasehistory.util.AfterTextChangedWatcher;
import com.angelp.purchasehistory.util.AndroidUtils;
import com.angelp.purchasehistory.util.Utils;
import com.angelp.purchasehistorybackend.models.views.incoming.PurchaseDTO;
import com.angelp.purchasehistorybackend.models.views.outgoing.CategoryView;
import com.angelp.purchasehistorybackend.models.views.outgoing.PurchaseView;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.integration.android.IntentIntegrator;
import dagger.hilt.android.AndroidEntryPoint;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

//import static com.google.android.gms.ads.AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize;

@AndroidEntryPoint
public class QrScannerFragment extends Fragment {
    private static final String TAG = "QRCodeFragment";
    private static final String ScanResultExtra = "SCAN_RESULT";
    final DecimalFormat formatter = new DecimalFormat("#,###,###.00");

    private QrScannerViewModel qrScannerViewModel;
    private FragmentQrBinding binding;
    private List<CategoryView> allCategories = new ArrayList<>();
    private TimePickerFragment timePicker;
    private DatePickerFragment datePicker;
    private final ActivityResultLauncher<Intent> getQRResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent intent = result.getData();
                    if (intent != null) {
                        String data = intent.getStringExtra(ScanResultExtra);
                        Log.i(TAG, "Scanned: " + data);
                        PurchaseDTO purchaseDTO = new PurchaseDTO(data);
                        qrScannerViewModel.validatePurchaseView(purchaseDTO, this::onInvalidPurchase);
                        fillQRForm(purchaseDTO);
                    } else {
                        Log.i(TAG, "QR scan Cancelled");
                    }
                }
            });
    private final ActivityResultLauncher<Intent> openGalleryRequest = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent intent = result.getData();
                    if (intent != null) {
                        Uri uri = intent.getData();
                        readQRFromImage(uri);
                    } else {
                        Log.i(TAG, "QR scan Cancelled");
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
    private CreateCategoryDialog categoryDialog;
    private CategorySpinnerAdapter categoryAdapter;
//    private AdView mAdView;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: View created");

        qrScannerViewModel = new ViewModelProvider(this).get(QrScannerViewModel.class);
        binding = FragmentQrBinding.inflate(inflater, container, false);
        initQRForm(inflater);
        if (getArguments() != null) {
            ScheduledNotification scheduledNotification = getArguments().getParcelable("scheduledNotification");
            if (scheduledNotification != null) {
                fillQRForm(scheduledNotification.getPurchaseDTO());
            }
        }
//        mAdView = new AdView(getContext());
//        mAdView.setAdSize(getCurrentOrientationAnchoredAdaptiveBannerAdSize(getContext(), R.id.adView));
//        mAdView.setAdUnitId("myAdUnitId");
//        AdRequest adRequest = new AdRequest.Builder().setRequestAgent("android_studio:ad_template").build();
//
//        // Start loading the ad.
//        mAdView.loadAd(adRequest);
//        binding.adView.addView(mAdView);

        new Thread(() -> {
            allCategories = qrScannerViewModel.getAllCategories();
            categoryAdapter = new CategorySpinnerAdapter(this.getContext(), allCategories);
            new Handler(Looper.getMainLooper()).post(() -> binding.qrCategorySpinner.setAdapter(categoryAdapter));
        }).start();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null && arguments.getBoolean(Constants.Arguments.OPEN_CAMERA)) {
            arguments.putBoolean(Constants.Arguments.OPEN_CAMERA, false);
            openCameraFlow(getLayoutInflater());
        }
    }

    private void fillQRForm(PurchaseDTO purchaseDTO) {
        new Thread(() -> {
            qrScannerViewModel.updatePurchaseDTO(purchaseDTO);
            datePicker.setValue(purchaseDTO.getDate());
            timePicker.setValue(purchaseDTO.getTime());
            new Handler(Looper.getMainLooper()).post(() -> {
                if (purchaseDTO.getStoreId() != null) binding.qrStoreIdValue.setText(purchaseDTO.getStoreId());
                if (purchaseDTO.getBillId() != null) binding.qrBillIdValue.setText(purchaseDTO.getBillId());
                if (purchaseDTO.getPrice() != null)
                    binding.qrPriceInput.setText(AndroidUtils.formatCurrency(purchaseDTO.getPrice()));
                if (purchaseDTO.getTimestamp() != null) {
                    binding.qrDateInput.setText(purchaseDTO.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE));
                    binding.qrTimeInput.setText(purchaseDTO.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_TIME));
                }
                if (Utils.defined(purchaseDTO.getNote())) {
                    binding.qrNoteInput.setText(purchaseDTO.getNote());
                }
            });
        }).start();
    }

    private void openCameraFlow(LayoutInflater inflater) {
        if (ContextCompat.checkSelfPermission(inflater.getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            initQRCodeScanner();
        } else {
            requestPermission.launch(Manifest.permission.CAMERA);
        }
    }

    private final ActivityResultLauncher<String> requestPermissionGallery = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            (isGranted) -> {
                if (isGranted) {
                    initQRPhotoChooser();
                } else {
                    showPermissionDeniedToast();
                }
            });

    private void openGalleryFlow(LayoutInflater inflater) {
        String permission = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU
                ? Manifest.permission.READ_MEDIA_IMAGES
                : Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(inflater.getContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            initQRPhotoChooser();
        } else {
            requestPermissionGallery.launch(permission);
        }
    }

    private void showPermissionDeniedToast() {
        PurchaseHistoryApplication.getInstance().getApplicationContext().getMainExecutor().execute(() ->
                Toast.makeText(getContext(), "The application cannot function without this", Toast.LENGTH_SHORT).show());
    }

    private void initQRPhotoChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        openGalleryRequest.launch(Intent.createChooser(intent, getString(R.string.scanner_gallery)));
    }

    private void readQRFromImage(Uri uri) {
        try {
            Bitmap image;
            ImageDecoder.Source source = ImageDecoder.createSource(requireContext().getContentResolver(), uri);
            image = ImageDecoder.decodeBitmap(source).copy(Bitmap.Config.RGBA_F16, true);

            int[] intArray = new int[image.getWidth() * image.getHeight()];
            image.getPixels(intArray, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());

            RGBLuminanceSource sourceRGB = new RGBLuminanceSource(image.getWidth(), image.getHeight(), intArray);
            MultiFormatReader reader = new MultiFormatReader();
            Result result = reader.decode(new BinaryBitmap(new HybridBinarizer(sourceRGB)));
            Log.i(TAG, "QR Code: " + result.getText());
            PurchaseDTO purchaseDTO = new PurchaseDTO(result.toString());
            if(purchaseDTO.getPrice() == null) {
                PurchaseHistoryApplication.getInstance().alert(R.string.failed_to_read_qr_code);
                return;
            }
            qrScannerViewModel.validatePurchaseView(purchaseDTO, this::onInvalidPurchase);
            fillQRForm(purchaseDTO);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initQRForm(LayoutInflater inflater) {
        timePicker = new TimePickerFragment(LocalTime.now());
        datePicker = new DatePickerFragment(LocalDate.now());
        categoryDialog = new CreateCategoryDialog((newCategory) -> new Handler(Looper.getMainLooper()).post(() -> {
            if (newCategory != null) {
                categoryAdapter.add(newCategory);
                binding.qrCategorySpinner.setSelection(categoryAdapter.getPosition(newCategory));
            }
        }));
        binding.qrFloatingPhotoButton.setOnClickListener((view) -> openGalleryFlow(inflater));
        binding.qrFloatingQrButton.setOnClickListener((view) -> openCameraFlow(inflater));
        binding.qrClearButton.setOnClickListener(v -> resetForm());
        binding.qrSubmitButton.setOnClickListener((view) -> onSubmit(qrScannerViewModel.getPurchaseDTO()));

        timePicker.getTimeResult().observe(getViewLifecycleOwner(), (v) -> {
            qrScannerViewModel.getPurchaseDTO().setTime(v);
            binding.qrTimeInput.setText(v.format(DateTimeFormatter.ISO_LOCAL_TIME));
        });
        datePicker.getDateResult().observe(getViewLifecycleOwner(), (v) -> {
            qrScannerViewModel.getPurchaseDTO().setDate(v);
            binding.qrDateInput.setText(v.format(DateTimeFormatter.ISO_LOCAL_DATE));
        });
        binding.qrPriceInput.addTextChangedListener(new AfterTextChangedWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (binding.qrPriceInput.hasFocus()) {
                    String str = binding.qrPriceInput.getText().toString();
                    if (Utils.isInvalidCurrency(str)) {
                        binding.qrPriceInput.setError("Invalid price!");
                        binding.qrSubmitButton.setEnabled(false);
                    } else {
                        if (str.trim().isEmpty())
                            qrScannerViewModel.getPurchaseDTO().setPrice(new BigDecimal(BigInteger.ZERO));
                        else qrScannerViewModel.getPurchaseDTO().setPrice(new BigDecimal(str));
                        binding.qrSubmitButton.setEnabled(true);
                        binding.qrSubmitButton.setError(null);
                    }
                }
            }
        });
        binding.qrNoteInput.addTextChangedListener(new AfterTextChangedWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (binding.qrNoteInput.hasFocus()) {
                    String str = binding.qrNoteInput.getText().toString();
                    qrScannerViewModel.getPurchaseDTO().setNote(str);
                }
            }
        });
        binding.qrTimeInput.setOnClickListener((v) -> timePicker.show(getParentFragmentManager(), "timePicker"));
        binding.qrDateInput.setOnClickListener((v) -> datePicker.show(getParentFragmentManager(), "datePicker"));
        binding.qrCategoryAddButton.setOnClickListener((v) -> categoryDialog.show(getParentFragmentManager(), "createCategoryDialog"));
        binding.qrCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                CategoryView categoryView = allCategories.get(position);
                qrScannerViewModel.getPurchaseDTO().setCategoryId(categoryView.getId());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                if (!allCategories.isEmpty())
                    qrScannerViewModel.getPurchaseDTO().setCategoryId(allCategories.get(0).getId());
                else qrScannerViewModel.getPurchaseDTO().setCategoryId(null);
            }
        });
    }

    private void resetForm() {
        qrScannerViewModel.resetPurchaseDto();
        if (!binding.qrCategorySpinner.getAdapter().isEmpty())
            binding.qrCategorySpinner.setSelection(0, true);
        binding.qrPriceInput.setText(AndroidUtils.formatCurrency(BigDecimal.ZERO));
        binding.qrDateInput.setText(R.string.date);
        binding.qrTimeInput.setText(R.string.time);
        datePicker.setValue(LocalDate.now());
        timePicker.setValue(LocalTime.now());
        binding.qrBillIdValue.setText("-");
        binding.qrStoreIdValue.setText("-");
        binding.qrNoteInput.setText("");
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
        if (isInvalidPurchase(data)) {
            PurchaseHistoryApplication.getInstance().alert("Invalid purchase, please try with another input or reset the page.");
            return;
        }
        setSubmitLoading(true);
        new Thread(() -> {
            PurchaseView purchaseView = qrScannerViewModel.createPurchaseView(data);
            if (purchaseView != null) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    AndroidUtils.showSuccessAnimation(getView());
                    resetForm();
                });
            }
            setSubmitLoading(false);
        }).start();
    }

    private void setSubmitLoading(boolean loading) {
        new Handler(Looper.getMainLooper()).post(() -> {
            binding.qrSubmitButton.setEnabled(!loading);
            binding.qrSubmitButton.setText(loading ? R.string.loading : R.string.submit_text);
        });
    }

    private boolean isInvalidPurchase(PurchaseDTO data) {
        return data.getPrice() == null ||
                data.getPrice().compareTo(BigDecimal.ZERO) < 0 ||
                data.getTimestamp() == null;
    }

    @Override
    public void onDestroyView() {
        // Destroy the AdView.
//        mAdView.destroy();
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onPause() {
        // Pause the AdView.
//        mAdView.pause();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Resume the AdView.
//        mAdView.resume();
    }

    private void onInvalidPurchase(Integer errorCode) {
        new Handler(Looper.getMainLooper()).post(() ->
                new AlertDialog.Builder(getContext())
                        .setIcon(R.drawable.baseline_money_off_24)
                        .setTitle(R.string.invalid_purchase_title)
                        .setMessage(errorCode)
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> resetForm())
                        .setOnDismissListener((dialog) -> resetForm())
                        .create()
                        .show()
        );
    }
}