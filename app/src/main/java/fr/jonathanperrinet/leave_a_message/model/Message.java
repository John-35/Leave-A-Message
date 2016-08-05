package fr.jonathanperrinet.leave_a_message.model;

/**
 * Created by Jonathan Perrinet on 01/07/2016.
 */
public abstract class Message {

    private double latitude, longitude;

    private float rotX, rotY, rotZ;

    private boolean loaded;

    private String url;

    public Message(String url, double latitude, double longitude, float rotX, float rotY, float rotZ) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.rotX = rotX;
        this.rotY = rotY;
        this.rotZ = rotZ;
        this.url = url;
        loaded = false;
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

    abstract public void display();

    public boolean isLoaded() {
        return loaded;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
