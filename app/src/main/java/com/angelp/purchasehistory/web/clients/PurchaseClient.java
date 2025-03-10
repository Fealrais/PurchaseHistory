package com.angelp.purchasehistory.web.clients;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
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

    public void getExportedCsv(Context context, Uri saveLocation) {
        try (Response res = get(BACKEND_URL + "/purchase/export")) {
            ResponseBody body = res.body();
            if (res.isSuccessful() && body != null) {
                createFile(context, body.bytes(), saveLocation);
            } else {
                if (body != null) {
                    ErrorResponse errorResponse = gson.fromJson(body.string(), ErrorResponse.class);
                    if ("NO_PURCHASES_TO_SHOW".equals(errorResponse.getDetail()))
                        throw new WebException(R.string.no_purchases_to_show);
                }
                throw new WebException(R.string.could_not_download_file);
            }
        } catch (IOException e) {
            Log.e(TAG, "Error downloading CSV: " + e.getMessage());
        }
    }

    private void createFile(Context context, byte[] bytes, Uri location) {
        if (location != null) {
            try (OutputStream outputStream = context.getContentResolver().openOutputStream(location)) {
                if (outputStream != null) {
                    outputStream.write(bytes);
                    Log.i(TAG, "CSV file saved successfully: " + location.getPath());
                } else {
                    Log.e(TAG, "OutputStream is null");
                }
            } catch (IOException e) {
                Log.e(TAG, "Error writing CSV file: " + e.getMessage());
            }
        } else {
            Log.e(TAG, "Failed to create file URI");
        }
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
