package fr.jonathanperrinet.leave_a_message.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Jonathan Perrinet on 01/07/2016.
 */
public class MessageString extends Message {

    private String text = "";

    public MessageString(String url) { super(url); }

    public MessageString(String url, double latitude, double longitude) {
        super(url, latitude, longitude);
    }

    public MessageString(Parcel in) {
        super(in);
        this.text = in.readString();
    }

    public static final Parcelable.Creator<MessageString> CREATOR = new Parcelable.Creator<MessageString>() {
        @Override
        public MessageString createFromParcel(Parcel parcel) {
            return new MessageString(parcel);
        }

        @Override
        public MessageString[] newArray(int size) {
            return new MessageString[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int i) {
        super.writeToParcel(dest, i);
        dest.writeString(text);
    }

    @Override
    public void setContent(Object content) {
        if(content instanceof String) {
            setText((String)content);
        }
    }

    @Override
    public Object getContent() {
        return text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
