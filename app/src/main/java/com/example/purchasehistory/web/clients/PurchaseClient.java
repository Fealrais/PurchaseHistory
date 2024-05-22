package com.example.purchasehistory.web.clients;

import android.content.Intent;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.util.Log;
import com.angelp.purchasehistorybackend.models.views.incoming.CategoryDTO;
import com.angelp.purchasehistorybackend.models.views.incoming.PurchaseDTO;
import com.angelp.purchasehistorybackend.models.views.outgoing.CategoryView;
import com.angelp.purchasehistorybackend.models.views.outgoing.PurchaseView;
import com.example.purchasehistory.data.model.PurchaseResponse;
import com.google.gson.JsonParseException;
import okhttp3.Response;
import okhttp3.ResponseBody;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class PurchaseClient extends HttpClient {
    private final String TAG = this.getClass().getSimpleName();

    @Inject
    public PurchaseClient() {
    }

    public PurchaseView createPurchase(PurchaseDTO purchaseDTO) {
        try (Response res = post(BACKEND_URL + "/purchase", purchaseDTO)) {
            ResponseBody body = res.body();
            if (res.isSuccessful() && body != null) {
                String json = body.string();
                Log.i("httpResponse", "Create Purchase: " + json);
                return gson.fromJson(json, PurchaseResponse.class).toPurchaseView();
            } else throw new IOException("Failed to initialize game");
        } catch (IOException ignored) {
        }
        return null;
    }

    public PurchaseView editPurchase(PurchaseDTO purchaseDTO, Long id) {
        try (Response res = put(BACKEND_URL + "/purchase/" + id, purchaseDTO)) {
            ResponseBody body = res.body();
            if (res.isSuccessful() && body != null) {
                String json = body.string();
                Log.i("httpResponse", "Create Purchase: " + json);
                return gson.fromJson(json, PurchaseResponse.class).toPurchaseView();
            } else throw new IOException("Failed to initialize game");
        } catch (IOException ignored) {
        }
        return null;
    }

    public List<PurchaseView> getAllPurchases() throws RuntimeException {
        try (Response res = get(BACKEND_URL + "/purchase")) {
            ResponseBody body = res.body();
            if (body != null) {
                String json = body.string();
                Log.i("httpResponse", "Get all purchases: " + json);
                if (res.isSuccessful())
                    return Arrays.stream(gson.fromJson(json, PurchaseResponse[].class)).map(PurchaseResponse::toPurchaseView).collect(Collectors.toList());
                else {
                    ErrorResponse errorResponse = gson.fromJson(json, ErrorResponse.class);
                    throw new RuntimeException(errorResponse.getDetail());
                }
            }
        } catch (IOException | JsonParseException e) {
            Log.e(TAG, "getAllPurchases ERROR: " + e.getMessage());
        }
        return null;
    }

    public List<CategoryView> getAllCategories() {
        try (Response res = get(BACKEND_URL + "/category")) {
            ResponseBody body = res.body();
            if (body != null) {
                String json = body.string();
                Log.i("httpResponse", "Get all categories: " + json);
                if (res.isSuccessful())
                    return Arrays.stream(gson.fromJson(json, CategoryView[].class)).collect(Collectors.toList());
                else {
                    ErrorResponse errorResponse = gson.fromJson(json, ErrorResponse.class);
                    throw new RuntimeException(errorResponse.getDetail());
                }
            }
        } catch (IOException | JsonParseException e) {
            Log.e(TAG, "getAllPurchases ERROR: " + e.getMessage());
        }
        return null;
    }

    public CategoryView createCategory(CategoryDTO categoryDTO) {
        try (Response res = post(BACKEND_URL + "/category", categoryDTO)) {
            ResponseBody body = res.body();
            if (res.isSuccessful() && body != null) {
                String json = body.string();
                Log.i("httpResponse", "Create Purchase: " + json);
                return gson.fromJson(json, CategoryView.class);
            } else throw new IOException("Failed to initialize game");
        } catch (IOException ignored) {
        }
        return null;
    }

    public Intent getExportedCsv() {
        try (Response res = get(BACKEND_URL + "/purchase/export")) {
            ResponseBody body = res.body();
            if (res.isSuccessful() && body != null) {
                String header = res.header("Content-Disposition");
                return createFile(body.bytes(), header == null ? "unknown" : header.substring(header.indexOf("=")));
            } else throw new IOException("Failed to initialize game");
        } catch (IOException ignored) {
        }
        return null;
    }

    private Intent createFile(byte[] bytes, String name) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_TITLE, "invoice.pdf");

        // Optionally, specify a URI for the directory that should be opened in
        // the system file picker when your app creates the document.
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));
        return intent;
    }
}
