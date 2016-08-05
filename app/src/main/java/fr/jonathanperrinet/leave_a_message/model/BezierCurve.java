package fr.jonathanperrinet.leave_a_message.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.github.gcacace.signaturepad.utils.TimedPoint;

/**
 * Created by Jonathan Perrinet on 04/08/2016.
 */
public class BezierCurve implements Parcelable {

    private final static double SCALE = 10;

    public ParcelableVector3 startPoint;
    public ParcelableVector3 control1;
    public ParcelableVector3 endPoint;
    public ParcelableVector3 control2;

    public BezierCurve(double sx, double sy, double cx1, double cy1, double cx2, double cy2, double ex, double ey) {
        this.startPoint = new ParcelableVector3(sx / SCALE, sy / SCALE, 0);
        this.control1 = new ParcelableVector3(cx1 / SCALE, cy1 / SCALE, 0);
        this.endPoint = new ParcelableVector3(ex / SCALE, ey / SCALE, 0);
        this.control2 = new ParcelableVector3(cx2 / SCALE, cy2 / SCALE, 0);
    }

    public BezierCurve(TimedPoint startPoint, TimedPoint control1, TimedPoint endPoint, TimedPoint control2) {
        this.startPoint = new ParcelableVector3(startPoint.x / SCALE, startPoint.y / SCALE, 0);
        this.control1 = new ParcelableVector3(control1.x / SCALE, control1.y / SCALE, 0);
        this.endPoint = new ParcelableVector3(endPoint.x / SCALE, endPoint.y / SCALE, 0);
        this.control2 = new ParcelableVector3(control2.x / SCALE, control2.y / SCALE, 0);
    }

    protected BezierCurve(Parcel in) {
        startPoint = in.readParcelable(ParcelableVector3.class.getClassLoader());
        control1 = in.readParcelable(ParcelableVector3.class.getClassLoader());
        endPoint = in.readParcelable(ParcelableVector3.class.getClassLoader());
        control2 = in.readParcelable(ParcelableVector3.class.getClassLoader());
    }

    public static final Creator<BezierCurve> CREATOR = new Creator<BezierCurve>() {
        @Override
        public BezierCurve createFromParcel(Parcel in) {
            return new BezierCurve(in);
        }

        @Override
        public BezierCurve[] newArray(int size) {
            return new BezierCurve[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(startPoint, i);
        parcel.writeParcelable(control1, i);
        parcel.writeParcelable(endPoint, i);
        parcel.writeParcelable(control2, i);
    }

    @Override
    public String toString() {
        return "BezierCurve{" +
                "start=" + startPoint +
                ", end=" + endPoint +
                '}';
    }
}
