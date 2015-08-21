package com.aluvi.android.model.realm;

import com.aluvi.android.api.tickets.model.RiderData;
import com.aluvi.android.api.tickets.model.TicketData;

import org.joda.time.LocalDate;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by matthewxi on 7/13/15.
 */
public class Ticket extends RealmObject {
    public static final String STATE_CREATED = "created";
    public static final String STATE_REQUESTED = "requested";
    public static final String STATE_CANCELLED = "cancelled"; // cancelled before scheduled
    public static final String STATE_SCHEDULED = "scheduled";
    public static final String STATE_STARTED = "started";
    public static final String STATE_COMMUTE_SCHEDULER_FAILED = "commute_scheduler_failed";
    public static final String STATE_IN_PROGRESS = "in_progress";
    public static final String STATE_COMPLETE = "complete";
    public static final String STATE_ABORTED = "aborted";  // cancelled after scheduled
    public static final String STATE_IRRELEVANT = "irrelevant";

    // These primary keys could be retrieved from related objects
    private int id, carId, driverId, fare_id;

    private double originLatitude, originLongitude;
    private String originPlaceName, originShortName;

    private double destinationLatitude, destinationLongitude;
    private String destinationPlaceName, destinationShortName;

    private double meetingPointLatitude, meetingPointLongitude;
    private String meetingPointPlaceName;

    private double dropOffPointLatitude, dropOffPointLongitude;
    private String dropOffPointPlaceName;

    private boolean confirmed; // User has viewed the itinerary and accepted
    private boolean driving;
    private int fixedPrice, estimatedEarnings;

    private Date requestedTimestamp, estimatedArrivalTime,
            desiredArrival, pickupTime,
            lastUpdated, rideDate;

    private String state, rideType, direction;

    private Driver driver;
    private Car car;
    private Trip trip;
    private RealmList<Rider> riders;

    public static Ticket buildNewTicket(Date rideDate, Route route) {
        return buildNewTicket(rideDate, route, false);
    }

    public static Ticket buildNewTicket(Date rideDate, Route route, boolean reverseDirection) {
        Ticket ticket = new Ticket();
        ticket.setRideDate(rideDate);

        RealmLatLng origin = reverseDirection ? route.getDestination() : route.getOrigin();
        RealmLatLng destination = reverseDirection ? route.getOrigin() : route.getDestination();

        ticket.setOriginLatitude(origin.getLatitude());
        ticket.setOriginLongitude(origin.getLongitude());
        ticket.setOriginPlaceName(reverseDirection ? route.getDestinationPlaceName() : route.getOriginPlaceName());

        ticket.setDestinationLatitude(destination.getLatitude());
        ticket.setDestinationLongitude(destination.getLongitude());
        ticket.setDestinationPlaceName(reverseDirection ? route.getOriginPlaceName() : route.getDestinationPlaceName());

        ticket.setDriving(route.isDriving());

        int hour = reverseDirection ? Route.getHour(route.getReturnTime()) : Route.getHour(route.getPickupTime());
        int minute = reverseDirection ? Route.getMinute(route.getReturnTime()) : Route.getMinute(route.getReturnTime());

        Calendar cal = GregorianCalendar.getInstance();
        cal.setTime(rideDate);
        cal.add(Calendar.HOUR_OF_DAY, hour);
        cal.add(Calendar.MINUTE, minute);
        ticket.setPickupTime(cal.getTime());
        ticket.setLastUpdated(new Date());
        return ticket;
    }

    public static void updateTicketWithTicketData(Ticket ticket, TicketData data, Realm realm) {
        ticket.setId(data.getTicketId());
        ticket.setState(data.getState());

        ticket.setOriginLatitude(data.getOriginLatitude());
        ticket.setOriginLongitude(data.getOriginLongitude());
        ticket.setOriginPlaceName(data.getOriginPlaceName());

        ticket.setDestinationLatitude(data.getDestinationLatitude());
        ticket.setDestinationLongitude(data.getDestinationLongitude());
        ticket.setDestinationPlaceName(data.getDestinationPlaceName());

        ticket.setDriving(data.isDriving());
        ticket.setPickupTime(data.getPickUpTime());
        ticket.setRideDate(new LocalDate()
                .fromDateFields(data.getPickUpTime())
                .toDateTimeAtStartOfDay()
                .toDate());

        ticket.setFixedPrice(data.getFixedPrice());
        ticket.setEstimatedEarnings(data.getEstimatedEarnings());

        if (data.car != null) {
            Car car = ticket.getCar() == null ? realm.createObject(Car.class) : ticket.getCar();
            Car.updateCarWithCarData(car, data.car);
            ticket.setCar(car);
        }

        if (data.driver != null) {
            Driver driver = ticket.getDriver() == null ? realm.createObject(Driver.class) : ticket.getDriver();
            Driver.updateWithDriverData(driver, data.driver);
            ticket.setDriver(driver);
        }

        if (data.getRiders() != null) {
            ticket.getRiders().where().findAll().clear();

            for (RiderData apiRider : data.getRiders()) {
                Rider rider = realm.createObject(Rider.class);
                Rider.updateWithRiderData(rider, apiRider);
                ticket.getRiders().add(rider);
            }
        }
    }

    public static String routeDescription(Ticket ticket) {
        return ticket.getMeetingPointPlaceName() + ' ' + ticket.getDropOffPointPlaceName();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public static TicketBounds getBounds(Ticket ticket) {
        double northLat = ticket.getDestinationLatitude() > ticket.getOriginLatitude() ?
                ticket.getDestinationLatitude() : ticket.getOriginLatitude();
        double southLat = ticket.getDestinationLatitude() < ticket.getOriginLatitude() ?
                ticket.getDestinationLatitude() : ticket.getOriginLatitude();
        double eastLon = ticket.getDestinationLongitude() < ticket.getOriginLongitude() ?
                ticket.getDestinationLongitude() : ticket.getOriginLongitude();
        double westLon = ticket.getDestinationLongitude() > ticket.getOriginLongitude() ?
                ticket.getDestinationLongitude() : ticket.getOriginLongitude();
        return new TicketBounds(northLat, eastLon, southLat, westLon);
    }

    public static class TicketBounds {
        private double north, east, west, south;

        public TicketBounds(double north, double east, double south, double west) {
            this.north = north;
            this.east = east;
            this.west = west;
            this.south = south;
        }

        public double getNorth() {
            return north;
        }

        public void setNorth(double north) {
            this.north = north;
        }

        public double getEast() {
            return east;
        }

        public void setEast(double east) {
            this.east = east;
        }

        public double getWest() {
            return west;
        }

        public void setWest(double west) {
            this.west = west;
        }

        public double getSouth() {
            return south;
        }

        public void setSouth(double south) {
            this.south = south;
        }
    }

    public int getFare_id() {
        return fare_id;
    }

    public void setFare_id(int fare_id) {
        this.fare_id = fare_id;
    }

    public Date getDesiredArrival() {
        return desiredArrival;
    }

    public void setDesiredArrival(Date desiredArrival) {
        this.desiredArrival = desiredArrival;
    }

    public Date getPickupTime() {
        return pickupTime;
    }

    public void setPickupTime(Date pickupTime) {
        this.pickupTime = pickupTime;
    }


    public int getCarId() {
        return carId;
    }

    public void setCarId(int carId) {
        this.carId = carId;
    }

    public int getDriverId() {
        return driverId;
    }

    public void setDriverId(int driverId) {
        this.driverId = driverId;
    }

    public String getRideType() {
        return rideType;
    }

    public void setRideType(String rideType) {
        this.rideType = rideType;
    }

    public Date getRequestedTimestamp() {
        return requestedTimestamp;
    }

    public void setRequestedTimestamp(Date requestedTimestamp) {
        this.requestedTimestamp = requestedTimestamp;
    }

    public Date getEstimatedArrivalTime() {
        return estimatedArrivalTime;
    }

    public void setEstimatedArrivalTime(Date estimatedArrivalTime) {
        this.estimatedArrivalTime = estimatedArrivalTime;
    }

    public double getOriginLatitude() {
        return originLatitude;
    }

    public void setOriginLatitude(double originLatitude) {
        this.originLatitude = originLatitude;
    }

    public double getOriginLongitude() {
        return originLongitude;
    }

    public void setOriginLongitude(double originLongitude) {
        this.originLongitude = originLongitude;
    }

    public String getOriginPlaceName() {
        return originPlaceName;
    }

    public void setOriginPlaceName(String originPlaceName) {
        this.originPlaceName = originPlaceName;
    }

    public String getOriginShortName() {
        return originShortName;
    }

    public void setOriginShortName(String originShortName) {
        this.originShortName = originShortName;
    }

    public double getDestinationLatitude() {
        return destinationLatitude;
    }

    public void setDestinationLatitude(double destinationLatitude) {
        this.destinationLatitude = destinationLatitude;
    }

    public double getDestinationLongitude() {
        return destinationLongitude;
    }

    public void setDestinationLongitude(double destinationLongitude) {
        this.destinationLongitude = destinationLongitude;
    }

    public String getDestinationPlaceName() {
        return destinationPlaceName;
    }

    public void setDestinationPlaceName(String destinationPlaceName) {
        this.destinationPlaceName = destinationPlaceName;
    }

    public String getDestinationShortName() {
        return destinationShortName;
    }

    public void setDestinationShortName(String destinationShortName) {
        this.destinationShortName = destinationShortName;
    }

    public double getMeetingPointLatitude() {
        return meetingPointLatitude;
    }

    public void setMeetingPointLatitude(double meetingPointLatitude) {
        this.meetingPointLatitude = meetingPointLatitude;
    }

    public double getMeetingPointLongitude() {
        return meetingPointLongitude;
    }

    public void setMeetingPointLongitude(double meetingPointLongitude) {
        this.meetingPointLongitude = meetingPointLongitude;
    }

    public String getMeetingPointPlaceName() {
        return meetingPointPlaceName;
    }

    public void setMeetingPointPlaceName(String meetingPointPlaceName) {
        this.meetingPointPlaceName = meetingPointPlaceName;
    }

    public double getDropOffPointLatitude() {
        return dropOffPointLatitude;
    }

    public void setDropOffPointLatitude(double dropOffPointLatitude) {
        this.dropOffPointLatitude = dropOffPointLatitude;
    }

    public double getDropOffPointLongitude() {
        return dropOffPointLongitude;
    }

    public void setDropOffPointLongitude(double dropOffPointLongitude) {
        this.dropOffPointLongitude = dropOffPointLongitude;
    }

    public String getDropOffPointPlaceName() {
        return dropOffPointPlaceName;
    }

    public void setDropOffPointPlaceName(String dropOffPointPlaceName) {
        this.dropOffPointPlaceName = dropOffPointPlaceName;
    }

    public Date getRideDate() {
        return rideDate;
    }

    public void setRideDate(Date rideDate) {
        this.rideDate = rideDate;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public boolean isDriving() {
        return driving;
    }

    public void setDriving(boolean driving) {
        this.driving = driving;
    }

    public int getFixedPrice() {
        return fixedPrice;
    }

    public void setFixedPrice(int fixedPrice) {
        this.fixedPrice = fixedPrice;
    }

    public int getEstimatedEarnings() {
        return estimatedEarnings;
    }

    public void setEstimatedEarnings(int estimatedEarnings) {
        this.estimatedEarnings = estimatedEarnings;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public Driver getDriver() {
        return driver;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    public Car getCar() {
        return car;
    }

    public void setCar(Car car) {
        this.car = car;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Trip getTrip() {
        return trip;
    }

    public void setTrip(Trip trip) {
        this.trip = trip;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public RealmList<Rider> getRiders() {
        return riders;
    }

    public void setRiders(RealmList<Rider> riders) {
        this.riders = riders;
    }
}
