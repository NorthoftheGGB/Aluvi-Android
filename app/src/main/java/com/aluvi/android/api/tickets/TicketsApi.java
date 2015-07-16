package com.aluvi.android.api.tickets;

import com.aluvi.android.api.AluviApi;
import com.aluvi.android.api.request.AluviAuthenticatedRequest;
import com.aluvi.android.model.realm.Ticket;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.SimpleType;
import com.spothero.volley.JacksonRequestListener;

/**
 * Created by matthewxi on 7/16/15.
 */


public class TicketsApi {

    public static void requestCommuterTickets(Ticket ticketToWork, Ticket ticketFromWork, final RequestCommuterTicketsCallback callback) {
        CommuterTicketsRequest requestParams = new CommuterTicketsRequest(ticketToWork, ticketFromWork);
        AluviAuthenticatedRequest request = new AluviAuthenticatedRequest<CommuterTicketsResponse>(
                Request.Method.POST,
                AluviApi.API_POST_REQUEST_COMMUTER_TICKETS,
                requestParams,
                new JacksonRequestListener<CommuterTicketsResponse>() {

                    @Override
                    public void onResponse(CommuterTicketsResponse response, int statusCode, VolleyError error) {
                        if (response != null) {
                            callback.success(response);
                        } else {
                            callback.failure(statusCode);
                        }
                    }

                    @Override
                    public JavaType getReturnType() {
                        return SimpleType.construct(CommuterTicketsResponse.class);
                    }
                }
        );
        AluviApi.getInstance().getRequestQueue().add(request);

    }

}
