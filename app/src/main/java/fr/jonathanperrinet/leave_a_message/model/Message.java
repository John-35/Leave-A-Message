package fr.jonathanperrinet.leave_a_message.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Jonathan Perrinet on 01/07/2016.
 */
public abstract class Message implements Parcelable {

    public static final int TYPE_DRAW = 1;
    public static final int TYPE_TEXT = 0;

    public static final String ATTR_TYPE = "type";
    public static final String ATTR_ROTX = "rotX";
    public static final String ATTR_ROTY = "rotY";
    public static final String ATTR_ROTZ = "rotZ";
    public static final String ATTR_POINTS = "points";
    public static final String ATTR_TEXT = "text";

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

    public Message(Parcel in) {
        latitude = in.readDouble();
        longitude = in.readDouble();
        rotX = in.readFloat();
        rotY = in.readFloat();
        rotZ = in.readFloat();
        loaded = in.readInt() == 1;
        url = in.readString();
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int i) {
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeFloat(rotX);
        dest.writeFloat(rotY);
        dest.writeFloat(rotZ);
        dest.writeInt(loaded ? 1 : 0);
        dest.writeString(url);
    }

    @Override
    public String toString() {
        return "Message{" +
                "url='" + url + '\'' +
                ", loaded=" + loaded +
                '}';
    }

    /*public String toJSonFormat() {
        StringBuilder builder = new StringBuilder("'")
                .append(ATTR_ROTX)
                .append("':")
                .append(rotX)
                .append(",'")
                .append(ATTR_ROTY)
                .append("':")
                .append(rotY)
                .append(",'")
                .append(ATTR_ROTZ)
                .append("':")
                .append(rotZ);
        return builder.toString();
    }*/
}
