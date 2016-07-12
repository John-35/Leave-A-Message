package fr.jonathanperrinet.leave_a_message.models;

/**
 * Created by Externe on 01/07/2016.
 */
public class MessagePolyline extends Message {

    private Polyline polyline;

    public MessagePolyline(double latitude, double longitude, float rotX, float rotY, float rotZ, Polyline polyline) {
        super(latitude, longitude, rotX, rotY, rotZ);
        this.polyline = polyline;
    }

    public Polyline getPolyline() {
        return polyline;
    }
}
