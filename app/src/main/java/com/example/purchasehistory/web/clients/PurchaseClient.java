package com.example.purchasehistory.web.clients;

import android.content.ContentValues;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import com.angelp.purchasehistorybackend.models.views.incoming.CategoryDTO;
import com.angelp.purchasehistorybackend.models.views.incoming.PurchaseDTO;
import com.angelp.purchasehistorybackend.models.views.outgoing.CategoryView;
import com.angelp.purchasehistorybackend.models.views.outgoing.PageView;
import com.angelp.purchasehistorybackend.models.views.outgoing.PurchaseView;
import com.example.purchasehistory.PurchaseHistoryApplication;
import com.example.purchasehistory.data.filters.PurchaseFilter;
import com.example.purchasehistory.data.model.Category;
import com.example.purchasehistory.data.model.PurchaseResponse;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import okhttp3.Response;
import okhttp3.ResponseBody;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
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

    public List<PurchaseView> getAllPurchases(PurchaseFilter filter) throws RuntimeException {
        try (Response res = get(BACKEND_URL + "/purchase/filtered?"+filter)) {
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

    public PageView<PurchaseView> getPurchases(PurchaseFilter filter) throws RuntimeException {
        try (Response res = get(BACKEND_URL + "/purchase/paged?"+filter)) {
            ResponseBody body = res.body();
            if (body != null) {
                String json = body.string();
                Log.i("httpResponse", "Get all purchases: " + json);
                if (res.isSuccessful()) {
                    return gson.fromJson(json, new TypeToken<PageView<PurchaseView>>() {}.getType());
                } else {
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
        return new ArrayList<>();
    }

    public Category createCategory(CategoryDTO categoryDTO) {
        try (Response res = post(BACKEND_URL + "/category", categoryDTO)) {
            ResponseBody body = res.body();
            if (res.isSuccessful() && body != null) {
                String json = body.string();
                Log.i("httpResponse", "Create Purchase: " + json);
                return gson.fromJson(json, Category.class);
            } else throw new IOException("Failed to initialize game");
        } catch (IOException ignored) {
        }
        return null;
    }

    public Uri getExportedCsv() {
        try (Response res = get(BACKEND_URL + "/purchase/export")) {
            ResponseBody body = res.body();
            if (res.isSuccessful() && body != null) {
                String header = res.header("Content-Disposition");
                String filename = header == null ? "unknown" : header.substring(header.indexOf("=") + 1);
                return createFile(filename, body.bytes());
            } else throw new IOException("Could not download file");
        } catch (IOException e) {
            Log.e(TAG, "getExportedCsv: " + e.getMessage());
        }
        return null;
    }

    private Uri createFile(String name, byte[] bytes) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Downloads.DISPLAY_NAME, name);
        contentValues.put(MediaStore.Downloads.MIME_TYPE, "text/csv");
        contentValues.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

        Uri uri = PurchaseHistoryApplication.getContext().getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);

        if (uri != null) {
            try (OutputStream outputStream = PurchaseHistoryApplication.getContext().getContentResolver().openOutputStream(uri)) {
                if (outputStream != null) {
                    outputStream.write(bytes);
                }
            } catch (IOException e) {
                Log.e(TAG, "createFile: " + e.getMessage());
            }
        }
        return uri;
    }

}
