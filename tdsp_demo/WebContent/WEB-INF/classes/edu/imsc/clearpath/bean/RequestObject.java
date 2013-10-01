// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RequestObject.java

package edu.imsc.clearpath.bean;


public class RequestObject
{

    public RequestObject(String pUpdate, String pCarPool, String pDay, String pStart, String pEnd, String pTime)
    {
        update = pUpdate;
        carPool = pCarPool;
        day = pDay;
        start = pStart;
        end = pEnd;
        time = pTime;
    }

    public String getUpdate()
    {
        return update;
    }

    public void setUpdate(String update)
    {
        this.update = update;
    }

    public String getCarPool()
    {
        return carPool;
    }

    public void setCarPool(String carPool)
    {
        this.carPool = carPool;
    }

    public String getDay()
    {
        return day;
    }

    public void setDay(String day)
    {
        this.day = day;
    }

    public String getStart()
    {
        return start;
    }

    public void setStart(String start)
    {
        this.start = start;
    }

    public String getEnd()
    {
        return end;
    }

    public void setEnd(String end)
    {
        this.end = end;
    }

    public String getTime()
    {
        return time;
    }

    public void setTime(String time)
    {
        this.time = time;
    }

    public String getReroute()
    {
        return reroute;
    }

    public void setReroute(String reroute)
    {
        this.reroute = reroute;
    }

    String update;
    String carPool;
    String day;
    String start;
    String end;
    String time;
    String reroute;
}
