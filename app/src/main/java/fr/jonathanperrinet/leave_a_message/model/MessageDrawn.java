package fr.jonathanperrinet.leave_a_message.model;

import java.util.List;

/**
 * Created by Jonathan Perrinet on 01/07/2016.
 */
public class MessageDrawn extends Message {

    public List<BezierCurve> curves;

    public MessageDrawn(String url, double latitude, double longitude) {
        super(url, latitude, longitude);
    }

    public List<BezierCurve> getCurves() {
        return curves;
    }

    @Override
    public void setContent(Object content) {
        //TODO: à faire
    }

    public void setCurves(List<BezierCurve> curves) {
        this.curves = curves;
    }
}
