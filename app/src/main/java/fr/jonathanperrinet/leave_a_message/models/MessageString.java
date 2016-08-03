package fr.jonathanperrinet.leave_a_message.models;

/**
 * Created by Jonathan Perrinet on 01/07/2016.
 */
public class MessageString extends Message {

    private String message;

    public MessageString(String url, double latitude, double longitude, float rotX, float rotY, float rotZ, String message) {
        super(url, latitude, longitude, rotX, rotY, rotZ);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public void display() {

    }
}
