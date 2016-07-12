package fr.jonathanperrinet.leave_a_message.models;

/**
 * Created by Externe on 01/07/2016.
 */
public class Message {

    private double latitude, longitude;

    private float rotX, rotY, rotZ;

    public Message(double latitude, double longitude, float rotX, float rotY, float rotZ) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.rotX = rotX;
        this.rotY = rotY;
        this.rotZ = rotZ;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public float getRotX() {
        return rotX;
    }

    public float getRotY() {
        return rotY;
    }

    public float getRotZ() {
        return rotZ;
    }
}
