// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   EndingNode.java

package Objects;


public class EndingNode
{

    public EndingNode()
    {
        stInfo = null;
        lat = 0.0D;
        lon = 0.0D;
        t = -1D;
    }

    public EndingNode(String stInfo, double lat, double lon, double t)
    {
        this.stInfo = stInfo;
        this.lat = lat;
        this.lon = lon;
        this.t = t;
    }

    public String getstInfo()
    {
        return stInfo;
    }

    public double getLat()
    {
        return lat;
    }

    public double getLon()
    {
        return lon;
    }

    public double gett()
    {
        return t;
    }

    public void setstInfo(String s)
    {
        stInfo = s;
    }

    public void setLat(double l)
    {
        lat = l;
    }

    public void setLon(double l)
    {
        lon = l;
    }

    public void setT(double v)
    {
        t = v;
    }

    String stInfo;
    double lat;
    double lon;
    double t;
}
