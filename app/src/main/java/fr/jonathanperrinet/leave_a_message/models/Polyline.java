package fr.jonathanperrinet.leave_a_message.models;

import java.util.ArrayList;

/**
 * Created by Jonathan Perrinet on 01/07/2016.
 */
public class Polyline {

    ArrayList<Point3D> points;

    public Polyline() {
        points = new ArrayList<>();
    }

    public void addPoint(Point3D p) {
        points.add(p);
    }

    public void addPoint(int x, int y, int z) {
        Point3D p = new Point3D(x, y, z);
    }
}
