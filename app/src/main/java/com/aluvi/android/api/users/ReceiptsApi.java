package com.aluvi.android.api.users;

import com.aluvi.android.api.AluviApi;
import com.aluvi.android.api.request.AluviAuthRequestListener;
import com.aluvi.android.api.request.AluviAuthenticatedRequest;
import com.aluvi.android.api.users.models.ReceiptData;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.SimpleType;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;

/**
 * Created by usama on 8/20/15.
 */
public class ReceiptsApi {
    public interface ReceiptsCallback {
        void success(List<ReceiptData> receipts);

        void failure(int statusCode);
    }

    public static void getReceipts(final ReceiptsCallback callback) {
        AluviAuthenticatedRequest<List<ReceiptData>> request = new AluviAuthenticatedRequest<List<ReceiptData>>(
                Request.Method.GET,
                AluviApi.API_RECEIPTS,
                new HashMap<String, String>(),
                new AluviAuthRequestListener<List<ReceiptData>>() {
                    @Override
                    public void onAuthenticatedResponse(List<ReceiptData> response, int statusCode, VolleyError error) {
                        if (statusCode == HttpURLConnection.HTTP_OK)
                            callback.success(response);
                        else
                            callback.failure(statusCode);
                    }

                    @Override
                    public JavaType getReturnType() {
                        return CollectionType.construct(List.class, SimpleType.construct(ReceiptData.class));
                    }
                }
        );

        request.addAcceptedStatusCodes(new int[]{HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_BAD_REQUEST});
        AluviApi.getInstance().getRequestQueue().add(request);
    }
}
