package fr.jonathanperrinet.leave_a_message.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Jonathan Perrinet on 04/08/2016.
 */
public class ParcelableVector3 implements Parcelable {

    public float x;
    public float y;
    public float z;

    public ParcelableVector3(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    protected ParcelableVector3(Parcel in) {
        x = in.readFloat();
        y = in.readFloat();
        z = in.readFloat();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(x);
        dest.writeFloat(y);
        dest.writeFloat(z);
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

    public boolean sameAs(ParcelableVector3 vector) {
        return (x == vector.x) && (y == vector.y) && (z == vector.z);
    }

}
