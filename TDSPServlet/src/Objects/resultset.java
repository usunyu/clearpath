// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   resultset.java

package Objects;


public class resultset
{

    public resultset(double distance, double traveltime)
    {
        this.distance = distance;
        this.traveltime = traveltime;
    }

    public double getDistance()
    {
        return distance;
    }

    public double getTrvaeltime()
    {
        return traveltime;
    }

    double distance;
    double traveltime;
}
