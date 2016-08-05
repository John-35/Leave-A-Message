package fr.jonathanperrinet.leave_a_message.model;

/**
 * Created by Jonathan Perrinet on 01/07/2016.
 */
public class MessagePolyline extends Message {

    private Polyline polyline;

    public MessagePolyline(String url, double latitude, double longitude, float rotX, float rotY, float rotZ, Polyline polyline) {
        super(url, latitude, longitude, rotX, rotY, rotZ);
        this.polyline = polyline;
    }

    public Polyline getPolyline() {
        return polyline;
    }

    @Override
    public void display() {

    }
}
