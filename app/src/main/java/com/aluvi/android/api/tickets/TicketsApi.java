package com.aluvi.android.api.tickets;

import com.aluvi.android.api.AluviApi;
import com.aluvi.android.api.AluviApiKeys;
import com.aluvi.android.api.ApiCallback;
import com.aluvi.android.api.request.AluviAuthRequestListener;
import com.aluvi.android.api.request.AluviAuthenticatedRequest;
import com.aluvi.android.api.tickets.model.PickupPointData;
import com.aluvi.android.api.tickets.model.TicketData;
import com.aluvi.android.model.realm.Ticket;
import com.aluvi.android.model.realm.Trip;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.SimpleType;

import java.net.HttpURLConnection;
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

    public interface PickupPointsCallback {
        void success(List<PickupPointData> points);

        void failure(int statueCode);
    }

    public static void requestCommuterTickets(Ticket ticketToWork, Ticket ticketFromWork, final RefreshTicketsCallback callback) {
        CommuterTicketsRequest requestParams = new CommuterTicketsRequest(ticketToWork, ticketFromWork);
        AluviAuthenticatedRequest<List<TicketData>> request = new AluviAuthenticatedRequest<>(
                Request.Method.POST,
                AluviApi.API_POST_REQUEST_COMMUTER_TICKETS,
                requestParams,
                new RefreshTicketAuthRequestListener(callback)
        );

        request.addAcceptedStatusCodes(new int[]{HttpURLConnection.HTTP_CREATED,
                HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_FORBIDDEN,
                HttpURLConnection.HTTP_PAYMENT_REQUIRED});
        AluviApi.getInstance().getRequestQueue().add(request);
    }

    public static void cancelTicket(Ticket ticket, final RefreshTicketsCallback callback) {
        Map<String, String> params = new HashMap<>();
        params.put(AluviApiKeys.RIDE_ID_KEY, String.valueOf(ticket.getId()));
        AluviAuthenticatedRequest<List<TicketData>> request = new AluviAuthenticatedRequest<>(
                Request.Method.POST,
                AluviApi.CANCEL_TICKET,
                params,
                new RefreshTicketAuthRequestListener(callback)
        );

        request.addAcceptedStatusCodes(new int[]{HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_FORBIDDEN});
        AluviApi.getInstance().getRequestQueue().add(request);
    }

    public static void cancelTrip(Trip trip, final ApiCallback callback) {
        AluviAuthenticatedRequest request = new AluviAuthenticatedRequest<Void>(
                Request.Method.DELETE,
                AluviApi.API_DELETE_TRIP + trip.getTripId(),
                new AluviAuthRequestListener<Void>() {
                    @Override
                    public void onAuthenticatedResponse(Void response, int statusCode, VolleyError error) {
                        if (statusCode == HttpURLConnection.HTTP_OK) {
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

        request.addAcceptedStatusCodes(new int[]{HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_BAD_REQUEST});
        AluviApi.getInstance().getRequestQueue().add(request);

    }

    public static void refreshTickets(final RefreshTicketsCallback callback) {
        AluviAuthenticatedRequest<List<TicketData>> request = new AluviAuthenticatedRequest<>(
                Request.Method.GET,
                AluviApi.API_GET_ACTIVE_TICKETS,
                new RefreshTicketAuthRequestListener(callback)
        );

        request.addAcceptedStatusCodes(new int[]{HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_BAD_REQUEST});
        AluviApi.getInstance().getRequestQueue().add(request);
    }

    public static void ridersPickedUp(Ticket ticket, final RefreshTicketsCallback callback) {
        Map<String, String> params = new HashMap<>();
        params.put(AluviApiKeys.RIDE_ID_KEY, String.valueOf(ticket.getId()));
        AluviAuthenticatedRequest<List<TicketData>> request = new AluviAuthenticatedRequest<>(
                Request.Method.POST,
                AluviApi.API_POST_RIDER_PICKUP,
                params,
                new RefreshTicketAuthRequestListener(callback)
        );

        request.addAcceptedStatusCodes(new int[]{HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_FORBIDDEN});
        AluviApi.getInstance().getRequestQueue().add(request);
    }

    public static void ridersDroppedOff(Ticket ticket, final RefreshTicketsCallback callback) {
        Map<String, String> params = new HashMap<>();
        params.put(AluviApiKeys.RIDE_ID_KEY, String.valueOf(ticket.getId()));
        AluviAuthenticatedRequest<List<TicketData>> request = new AluviAuthenticatedRequest<>(
                Request.Method.POST,
                AluviApi.API_POST_RIDER_DROPOFF,
                params,
                new RefreshTicketAuthRequestListener(callback)
        );

        request.addAcceptedStatusCodes(new int[]{HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_FORBIDDEN});
        AluviApi.getInstance().getRequestQueue().add(request);
    }

    public static void getPickupPoints(final PickupPointsCallback pointsCallback) {
        AluviAuthenticatedRequest<List<PickupPointData>> request = new AluviAuthenticatedRequest<>(
                Request.Method.GET,
                AluviApi.API_PICKUP_POINTS,
                new AluviAuthRequestListener<List<PickupPointData>>() {
                    @Override
                    public void onAuthenticatedResponse(List<PickupPointData> response, int statusCode, VolleyError error) {
                        if (statusCode == HttpURLConnection.HTTP_OK)
                            pointsCallback.success(response);
                        else
                            pointsCallback.failure(statusCode);
                    }

                    @Override
                    public JavaType getReturnType() {
                        return CollectionType.construct(List.class, SimpleType.construct(PickupPointData.class));
                    }
                }
        );

        request.addAcceptedStatusCodes(new int[]{HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_FORBIDDEN});
        AluviApi.getInstance().getRequestQueue().add(request);
    }

    private static class RefreshTicketAuthRequestListener extends AluviAuthRequestListener<List<TicketData>> {
        private RefreshTicketsCallback callback;

        public RefreshTicketAuthRequestListener(RefreshTicketsCallback callback) {
            this.callback = callback;
        }

        @Override
        public void onAuthenticatedResponse(List<TicketData> response, int statusCode, VolleyError error) {
            if (statusCode == HttpURLConnection.HTTP_OK) {
                callback.success(response);
            } else {
                callback.failure(statusCode);
            }
        }

        @Override
        public JavaType getReturnType() {
            return CollectionType.construct(List.class, SimpleType.construct(TicketData.class));
        }
    }
}
