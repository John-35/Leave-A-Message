package fr.jonathanperrinet.leave_a_message.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Jonathan Perrinet on 04/08/2016.
 */
public class ParcelableVector3 implements Parcelable {

    public double x;
    public double y;
    public double z;

    public ParcelableVector3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    protected ParcelableVector3(Parcel in) {
        x = in.readDouble();
        y = in.readDouble();
        z = in.readDouble();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(x);
        dest.writeDouble(y);
        dest.writeDouble(z);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ParcelableVector3> CREATOR = new Creator<ParcelableVector3>() {
        @Override
        public ParcelableVector3 createFromParcel(Parcel in) {
            return new ParcelableVector3(in);
        }

        @Override
        public ParcelableVector3[] newArray(int size) {
            return new ParcelableVector3[size];
        }
    };

    @Override
    public String toString() {
        return "{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }
}
