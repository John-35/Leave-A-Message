package fr.jonathanperrinet.leave_a_message.model;

/**
 * Created by Jonathan Perrinet on 01/07/2016.
 */
public abstract class Message {

    public static final int TYPE_DRAW = 1;
    public static final int TYPE_TEXT = 0;

    public static final String ATTR_TYPE = "type";
    public static final String ATTR_ROTX = "rotX";
    public static final String ATTR_ROTY = "rotY";
    public static final String ATTR_ROTZ = "rotZ";
    public static final String ATTR_POINTS = "points";

    private double latitude, longitude;

    private float rotX, rotY, rotZ;

    private boolean loaded;

    private String url;

    public Message(String url, double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
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

    public void setRotation(float rotX, float rotY, float rotZ) {
        this.rotX = rotX;
        this.rotY = rotY;
        this.rotZ = rotZ;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    public String getUrl() {
        return url;
    }

    public abstract void setContent(Object content);
}
