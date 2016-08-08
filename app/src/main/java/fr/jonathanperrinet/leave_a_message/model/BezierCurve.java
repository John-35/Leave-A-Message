package fr.jonathanperrinet.leave_a_message.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Jonathan Perrinet on 04/08/2016.
 */
public class BezierCurve implements Parcelable {

    public ParcelableVector3 startPoint;
    public ParcelableVector3 control1;
    public ParcelableVector3 endPoint;
    public ParcelableVector3 control2;

    public BezierCurve(float sx, float sy,
                       float cx1, float cy1,
                       float cx2, float cy2,
                       float ex, float ey,
                       int width, int height) {
        this.startPoint = new ParcelableVector3(sx / width, sy / height, 0);
        this.control1 = new ParcelableVector3(cx1 / width, cy1 / height, 0);
        this.endPoint = new ParcelableVector3(ex / width, ey / height, 0);
        this.control2 = new ParcelableVector3(cx2 / width, cy2 / height, 0);
    }

    public BezierCurve(ParcelableVector3[] points) throws Exception {
        if(points.length != 4) {
            throw new Exception("Nombre de points invalides ! Attendu : 4, Obtenu : " + points.length);
        }

        this.startPoint = points[0];
        this.control1 = points[1];
        this.control2 = points[2];
        this.endPoint = points[3];
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
