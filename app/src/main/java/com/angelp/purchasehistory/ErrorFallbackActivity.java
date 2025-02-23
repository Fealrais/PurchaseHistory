package com.angelp.purchasehistory;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ErrorFallbackActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error_fallback);

        String errorDetails = getIntent().getStringExtra("error_details");

        TextView errorTextView = findViewById(R.id.error_text_view);
        errorTextView.setText(errorDetails);

        Button sendFeedbackButton = findViewById(R.id.send_feedback_button);
        sendFeedbackButton.setOnClickListener(v -> sendFeedback(errorDetails));

        Button closeButton = findViewById(R.id.close_button);
        closeButton.setOnClickListener(v -> finishAndRemoveTask());
    }

    private void sendFeedback(String errorDetails) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{GlobalExceptionHandler.PURCHASE_HISTORY_BG_GMAIL_COM});
        intent.putExtra(Intent.EXTRA_SUBJECT, "PurchaseHistory Error Feedback");
        intent.putExtra(Intent.EXTRA_TEXT, errorDetails);
        startActivity(Intent.createChooser(intent, "Send Feedback"));
    }

    @Override
    public void onBackPressed() {
        finishAndRemoveTask();
    }
}