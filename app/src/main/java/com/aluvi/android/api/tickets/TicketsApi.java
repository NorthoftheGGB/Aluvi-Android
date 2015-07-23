package com.aluvi.android.api.tickets;

import com.aluvi.android.api.AluviApi;
import com.aluvi.android.api.AluviApiKeys;
import com.aluvi.android.api.ApiCallback;
import com.aluvi.android.api.request.AluviAuthenticatedRequest;
import com.aluvi.android.api.tickets.model.TicketData;
import com.aluvi.android.model.realm.Ticket;
import com.aluvi.android.model.realm.Trip;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.SimpleType;
import com.spothero.volley.JacksonRequest;
import com.spothero.volley.JacksonRequestListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by matthewxi on 7/16/15.
 */


public class TicketsApi {

    public interface RefreshTicketsCallback {
        void success(List<TicketData> tickets);

        void failure(int statusCode);
    }

    public static void requestCommuterTickets(Ticket ticketToWork, Ticket ticketFromWork, final RequestCommuterTicketsCallback callback) {
        CommuterTicketsRequest requestParams = new CommuterTicketsRequest(ticketToWork, ticketFromWork);
        AluviAuthenticatedRequest<CommuterTicketsResponse> request = new AluviAuthenticatedRequest<>(
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

        request.addAcceptedStatusCodes(new int[]{201, 403});
        AluviApi.getInstance().getRequestQueue().add(request);
    }


    public static void cancelRiderTicketRequest(Ticket ticket, final ApiCallback callback) {

        Map<String, String> params = new HashMap<String, String>();
        params.put(AluviApiKeys.RIDE_ID_KEY, String.valueOf(ticket.getRideId()));
        AluviAuthenticatedRequest request = new AluviAuthenticatedRequest<Void>(
                Request.Method.POST,
                AluviApi.API_POST_REQUEST_CANCELLED,
                params,
                new JacksonRequestListener<Void>() {
                    @Override
                    public void onResponse(Void response, int statusCode, VolleyError error) {
                        if (statusCode == 200) {
                            callback.success();
                        } else {
                            callback.failure(statusCode);
                        }
                    }

                    @Override
                    public JavaType getReturnType() {
                        return SimpleType.construct(Void.class);
                    }
                }
        );
        request.addAcceptedStatusCodes(new int[]{200, 403});
        AluviApi.getInstance().getRequestQueue().add(request);

    }

    public static void cancelRiderScheduledTicket(Ticket ticket, final ApiCallback callback) {

        Map<String, String> params = new HashMap<String, String>();
        params.put(AluviApiKeys.FARE_KEY, String.valueOf(ticket.getHovFare().getId()));
        AluviAuthenticatedRequest request = new AluviAuthenticatedRequest<Void>(
                Request.Method.POST,
                AluviApi.CANCEL_RIDER_SCHEDULED_TICKET,
                params,
                new JacksonRequestListener<Void>() {
                    @Override
                    public void onResponse(Void response, int statusCode, VolleyError error) {
                        if (statusCode == 200) {
                            callback.success();
                        } else {
                            callback.failure(statusCode);
                        }
                    }

                    @Override
                    public JavaType getReturnType() {
                        return SimpleType.construct(Void.class);
                    }
                }
        );

        request.addAcceptedStatusCodes(new int[]{200, 403});
        AluviApi.getInstance().getRequestQueue().add(request);

    }

    public static void cancelTrip(Trip trip, final ApiCallback callback) {
        AluviAuthenticatedRequest request = new AluviAuthenticatedRequest<Void>(
                Request.Method.DELETE,
                AluviApi.API_DELETE_TRIP + trip.getTripId(),
                new JacksonRequestListener<Void>() {
                    @Override
                    public void onResponse(Void response, int statusCode, VolleyError error) {
                        if (statusCode == 200) {
                            callback.success();
                        } else {
                            callback.failure(statusCode);
                        }
                    }

                    @Override
                    public JavaType getReturnType() {
                        return SimpleType.construct(Void.class);
                    }
                }
        );

        request.addAcceptedStatusCodes(new int[]{200, 400});
        AluviApi.getInstance().getRequestQueue().add(request);

    }

    public static void refreshTickets(final RefreshTicketsCallback callback) {
        AluviAuthenticatedRequest<List<TicketData>> request = new AluviAuthenticatedRequest<>(
                Request.Method.GET,
                AluviApi.API_GET_ACTIVE_TICKETS,
                new JacksonRequestListener<List<TicketData>>() {
                    @Override
                    public void onResponse(List<TicketData> response, int statusCode, VolleyError error) {
                        if (statusCode == 200) {
                            callback.success(response);
                        } else {
                            callback.failure(statusCode);
                        }
                    }

                    @Override
                    public JavaType getReturnType() {
                        return JacksonRequest.getObjectMapper().getTypeFactory().constructCollectionType(List.class, TicketData.class);
                    }
                }
        );

        request.addAcceptedStatusCodes(new int[]{200, 400});
        AluviApi.getInstance().getRequestQueue().add(request);
    }
}
