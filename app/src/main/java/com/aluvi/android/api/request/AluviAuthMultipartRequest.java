package com.aluvi.android.api.request;

import com.android.volley.AuthFailureError;
import com.spothero.volley.JacksonRequestListener;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Created by usama on 7/31/15.
 */
public class AluviAuthMultipartRequest<T> extends AluviAuthenticatedRequest<T> {
    private MultipartEntityBuilder mMultipartEntityBuilder = MultipartEntityBuilder
            .create();
    private HttpEntity mEntity;

    public AluviAuthMultipartRequest(int method, String endpoint, AluviPayload payload, Map<String, File> filePayload,
                                     JacksonRequestListener<T> listener) {
        this(method, endpoint, payload.toMap(), filePayload, listener);
    }

    public AluviAuthMultipartRequest(int method, String endpoint, Map<String, String> payload,
                                     Map<String, File> filePayload, JacksonRequestListener<T> listener) {
        super(method, endpoint, payload, listener);

        for (Map.Entry<String, String> entry : getParams().entrySet()) {
            if (entry.getValue() != null) {
                mMultipartEntityBuilder.addTextBody(entry.getKey(), entry.getValue());
            }
        }

        for (Map.Entry<String, File> entry : filePayload.entrySet()) {
            ContentType contentType = null;
            String fileName = entry.getValue().getName();
            if (fileName.contains("jpg") || fileName.contains("jpeg"))
                contentType = ContentType.create("image/jpeg");
            else
                contentType = ContentType.MULTIPART_FORM_DATA;
            mMultipartEntityBuilder.addBinaryBody(entry.getKey(), entry.getValue(), contentType, fileName);
        }


        mEntity = mMultipartEntityBuilder.build();
    }

    @Override
    public String getBodyContentType() {
        return mEntity.getContentType().getValue();
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            mEntity.writeTo(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return outputStream.toByteArray();
    }
}
