package com.aluvi.android.api.request;

import com.android.volley.AuthFailureError;
import com.spothero.volley.JacksonRequestListener;

import org.apache.http.entity.mime.MultipartEntityBuilder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Created by usama on 7/31/15.
 */
public class AluviAuthMultipartRequest<T> extends AluviAuthenticatedRequest<T> {
    private Map<String, File> mFilePayload;

    public AluviAuthMultipartRequest(int method, String endpoint, AluviPayload payload, Map<String, File> filePayload,
                                     JacksonRequestListener<T> listener) {
        super(method, endpoint, payload, listener);
        this.mFilePayload = filePayload;
    }

    public AluviAuthMultipartRequest(int method, String endpoint, Map<String, String> payload,
                                     Map<String, File> filePayload, JacksonRequestListener<T> listener) {
        super(method, endpoint, payload, listener);
        this.mFilePayload = filePayload;
    }

    @Override
    public String getBodyContentType() {
        return "multipart/form-data";
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder
                .create();

        for (Map.Entry<String, String> entry : getParams().entrySet())
            entityBuilder.addTextBody(entry.getKey(), entry.getValue());

        for (Map.Entry<String, File> entry : mFilePayload.entrySet())
            entityBuilder.addBinaryBody(entry.getKey(), entry.getValue());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            entityBuilder.build().writeTo(outputStream);
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
