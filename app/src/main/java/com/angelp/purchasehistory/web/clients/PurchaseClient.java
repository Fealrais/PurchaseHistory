package com.angelp.purchasehistory.web.clients;

import android.content.ContentValues;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import com.angelp.purchasehistory.PurchaseHistoryApplication;
import com.angelp.purchasehistory.R;
import com.angelp.purchasehistory.data.filters.PurchaseFilter;
import com.angelp.purchasehistory.data.model.Category;
import com.angelp.purchasehistory.data.model.PurchaseResponse;
import com.angelp.purchasehistorybackend.models.views.incoming.CategoryDTO;
import com.angelp.purchasehistorybackend.models.views.incoming.PurchaseDTO;
import com.angelp.purchasehistorybackend.models.views.outgoing.CategoryView;
import com.angelp.purchasehistorybackend.models.views.outgoing.PageView;
import com.angelp.purchasehistorybackend.models.views.outgoing.PurchaseView;
import com.angelp.purchasehistorybackend.models.views.outgoing.analytics.CalendarReport;
import com.angelp.purchasehistorybackend.models.views.outgoing.analytics.CategoryAnalyticsReport;
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
            } else throw new IOException("Failed to create purchase");
        } catch (IOException ignored) {
        }
        return null;
    }

    public void validatePurchase(PurchaseDTO purchaseDTO) {
        try (Response res = post(BACKEND_URL + "/purchase/check", purchaseDTO)) {
            utils.getBody(res, PurchaseView.class);
        } catch (IOException e) {
            Log.e(TAG, "validatePurchase: ", e);
        }
    }

    public PurchaseView editPurchase(PurchaseDTO purchaseDTO, Long id) {
        try (Response res = put(BACKEND_URL + "/purchase/" + id, purchaseDTO)) {
            ResponseBody body = res.body();
            if (res.isSuccessful() && body != null) {
                String json = body.string();
                Log.i("httpResponse", "Edit Purchase: " + json);
                return gson.fromJson(json, PurchaseResponse.class).toPurchaseView();
            } else throw new IOException("Failed to edit purchase");
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
        try (Response res = get(BACKEND_URL + "/purchase/filtered?" + filter)) {
            ResponseBody body = res.body();
            if (body != null) {
                String json = body.string();
                Log.i("httpResponse", "Get all purchases: " + json);
                if (res.isSuccessful())
                    return Arrays.stream(gson.fromJson(json, PurchaseResponse[].class)).map(PurchaseResponse::toPurchaseView).collect(Collectors.toList());
                else {
                    ErrorResponse errorResponse = gson.fromJson(json, ErrorResponse.class);
                    throw new RuntimeException(String.valueOf(errorResponse));
                }
            }
        } catch (IOException | JsonParseException | NullPointerException e) {
            Log.e(TAG, "getAllPurchases ERROR: " + e.getMessage());
        }
        return null;
    }

    public PageView<PurchaseView> getPurchases(PurchaseFilter filter) throws RuntimeException {
        try (Response res = get(BACKEND_URL + "/purchase/paged?" + filter)) {
            ResponseBody body = res.body();
            if (body != null) {
                String json = body.string();
                Log.i("httpResponse", "Get all purchases: " + json);
                if (res.isSuccessful()) {
                    return gson.fromJson(json, new TypeToken<PageView<PurchaseView>>() {
                    }.getType());
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

    public CalendarReport getCategorizedCalendarReport(PurchaseFilter filter) {
        try (Response res = get(BACKEND_URL + "/purchase/analytics/calendar?" + filter)) {
            ResponseBody body = res.body();
            if (body != null) {
                String json = body.string();
                Log.i("httpResponse", "Get all purchases: " + json);
                if (res.isSuccessful()) {
                    return gson.fromJson(json, CalendarReport.class);
                } else {
                    ErrorResponse errorResponse = gson.fromJson(json, ErrorResponse.class);
                    throw new RuntimeException(errorResponse.getDetail());
                }
            }
        } catch (IOException | JsonParseException e) {
            Log.e(TAG, "getCalendarReport ERROR: " + e.getMessage());
        }
        return null;
    }

    public CalendarReport getCalendarReport(PurchaseFilter filter) {
        try (Response res = get(BACKEND_URL + "/purchase/analytics/monthly?" + filter)) {
            ResponseBody body = res.body();
            if (body != null) {
                String json = body.string();
                Log.i("httpResponse", "Get all purchases: " + json);
                if (res.isSuccessful()) {
                    return gson.fromJson(json, CalendarReport.class);
                } else {
                    ErrorResponse errorResponse = gson.fromJson(json, ErrorResponse.class);
                    throw new RuntimeException(errorResponse.getDetail());
                }
            }
        } catch (IOException | JsonParseException e) {
            Log.e(TAG, "getCalendarReport ERROR: " + e.getMessage());
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
            Log.e(TAG, "getAllCategories ERROR: " + e.getMessage());
        }
        return new ArrayList<>();
    }

    public Category createCategory(CategoryDTO categoryDTO) {
        try (Response res = post(BACKEND_URL + "/category", categoryDTO)) {
            ResponseBody body = res.body();
            if (res.isSuccessful() && body != null) {
                String json = body.string();
                Log.i("httpResponse", "Created Category: " + json);
                return gson.fromJson(json, Category.class);
            } else throw new IOException("Failed to create category");
        } catch (IOException ignored) {
        }
        return null;
    }

    public Category editCategory(int id, CategoryDTO categoryDTO) {
        try (Response res = put(BACKEND_URL + "/category/" + id, categoryDTO)) {
            ResponseBody body = res.body();
            if (res.isSuccessful() && body != null) {
                String json = body.string();
                Log.i("httpResponse", "Edited Category: " + json);
                return gson.fromJson(json, Category.class);
            } else throw new IOException("Failed to edit category");
        } catch (IOException ignored) {
        }
        return null;
    }

    public Uri getExportedCsv() throws IOException {
        try (Response res = get(BACKEND_URL + "/purchase/export")) {
            ResponseBody body = res.body();
            if (res.isSuccessful() && body != null) {
                String header = res.header("Content-Disposition");
                String filename = header == null ? "unknown" : header.substring(header.indexOf("=") + 1);
                return createFile(filename, body.bytes());
            } else {
                if (body != null) {
                    ErrorResponse errorResponse = gson.fromJson(body.string(), ErrorResponse.class);
                    if ("NO_PURCHASES_TO_SHOW".equals(errorResponse.getDetail()))
                        throw new WebException(R.string.no_purchases_to_show);
                }
                throw new WebException(R.string.could_not_download_file);
            }
        }
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

    public CategoryAnalyticsReport getCategoryAnalyticsReport(PurchaseFilter filter) {
        try (Response res = get(BACKEND_URL + "/category/analytics?" + filter)) {
            ResponseBody body = res.body();
            if (body != null) {
                String json = body.string();
                Log.i("httpResponse", "getCategoryAnalyticsReport : " + json);
                if (res.isSuccessful())
                    return gson.fromJson(json, CategoryAnalyticsReport.class);
                else {
                    ErrorResponse errorResponse = gson.fromJson(json, ErrorResponse.class);
                    throw new IOException(String.valueOf(errorResponse));
                }
            }
        } catch (IOException | JsonParseException | NullPointerException e) {
            Log.e(TAG, "analytics ERROR: " + e.getMessage());
        }
        return null;
    }

    public boolean deletePurchase(Long purchaseId) {
        try (Response res = delete(BACKEND_URL + "/purchase?id=" + purchaseId)) {
            if (res.isSuccessful()) {
                Log.i("httpResponse", "delete purchase : Success");
                return true;
            }
            if (res.body() != null && res.body().contentLength() > 0) {
                ErrorResponse errorResponse = gson.fromJson(res.body().string(), ErrorResponse.class);
                throw new IOException(errorResponse.getDetail());
            }
        } catch (IOException | JsonParseException e) {
            Log.e(TAG, "ERROR: " + e.getMessage());
        }
        return false;
    }

}
