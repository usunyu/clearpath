// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   DBConnection.java

package edu.imsc.clearpath.util;

import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import oracle.jdbc.OracleDriver;

public class DBConnection
{

    public DBConnection()
    {
    }

    public static Connection getConnection()
    {
        try
        {
            DriverManager.registerDriver(new OracleDriver());
            Connection connHome = DriverManager.getConnection(url_home, userName, password);
            return connHome;
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
        }
        return connHome;
    }

    static Connection connHome = null;
    static String url_home = "jdbc:oracle:thin:@gd.usc.edu:1521/adms";
    static String userName = "clearp";
    static String password = "clearp";

}
