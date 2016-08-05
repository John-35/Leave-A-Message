package com.github.gcacace.signaturepad.utils;

/**
 * Created by gcacace on 28/02/14.
 */
public class ControlTimedPoints {

    public com.github.gcacace.signaturepad.utils.TimedPoint c1;
    public com.github.gcacace.signaturepad.utils.TimedPoint c2;

    public com.github.gcacace.signaturepad.utils.ControlTimedPoints set(com.github.gcacace.signaturepad.utils.TimedPoint c1, TimedPoint c2) {
        this.c1 = c1;
        this.c2 = c2;
        return this;
    }

}
