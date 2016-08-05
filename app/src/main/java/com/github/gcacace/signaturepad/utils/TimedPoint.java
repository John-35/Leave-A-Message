package com.github.gcacace.signaturepad.utils;

public class TimedPoint {
    public float x;
    public float y;
    public long timestamp;

    public com.github.gcacace.signaturepad.utils.TimedPoint set(float x, float y) {
        this.x = x;
        this.y = y;
        this.timestamp = System.currentTimeMillis();
        return this;
    }

    public float velocityFrom(com.github.gcacace.signaturepad.utils.TimedPoint start) {
        float velocity = distanceTo(start) / (this.timestamp - start.timestamp);
        if (velocity != velocity) return 0f;
        return velocity;
    }

    public float distanceTo(com.github.gcacace.signaturepad.utils.TimedPoint point) {
        return (float) Math.sqrt(Math.pow(point.x - this.x, 2) + Math.pow(point.y - this.y, 2));
    }
}
