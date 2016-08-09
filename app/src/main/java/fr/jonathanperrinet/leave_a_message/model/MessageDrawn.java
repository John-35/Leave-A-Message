package fr.jonathanperrinet.leave_a_message.model;

import android.os.Parcel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by Jonathan Perrinet on 01/07/2016.
 */
public class MessageDrawn extends Message {

    public List<BezierCurve> curves;

    public MessageDrawn(String url, double latitude, double longitude) {
        super(url, latitude, longitude);
        curves = new ArrayList<>();
    }

    public MessageDrawn(Parcel in) {
        super(in);
        in.readList(curves, BezierCurve.class.getClassLoader());
    }

    public static final Creator<MessageDrawn> CREATOR = new Creator<MessageDrawn>() {
        @Override
        public MessageDrawn createFromParcel(Parcel parcel) {
            return new MessageDrawn(parcel);
        }

        @Override
        public MessageDrawn[] newArray(int size) {
            return new MessageDrawn[size];
        }
    };

    public List<BezierCurve> getCurves() {
        return curves;
    }

    @Override
    public void setContent(Object content) {
        if(content instanceof List) {
            curves = (List<BezierCurve>)content;
        }
    }

    public void setCurves(List<BezierCurve> curves) {
        this.curves = curves;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int i) {
        super.writeToParcel(dest, i);
        dest.writeList(curves);
    }
}
