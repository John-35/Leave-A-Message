package fr.jonathanperrinet.leave_a_message.model;

/**
 * Created by Jonathan Perrinet on 01/07/2016.
 */
public class MessageString extends Message {

    private String text;

    public MessageString(String url, double latitude, double longitude) {
        super(url, latitude, longitude);
    }

    @Override
    public void setContent(Object content) {
        if(content instanceof String) {
            setText((String)content);
        }
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

}
