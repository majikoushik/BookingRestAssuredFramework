package com.booking.tests.config;


import com.typesafe.config.ConfigFactory;

public final class Config {
    private static final com.typesafe.config.Config C =
            ConfigFactory.parseResources("application.conf")
                    .withFallback(ConfigFactory.load());

    public static String env()      { return C.hasPath("env") ? C.getString("env") : "local"; }
    public static String baseUrl()   { return C.getString("baseUrl"); }
    public static int timeoutMs()    { return C.getInt("timeoutMs"); }
    public static String user()      { return C.getString("auth.username"); }
    public static String pass()      { return C.getString("auth.password"); }
}

