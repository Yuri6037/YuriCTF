package fr.yurictf.server;

public class Level {

    public static CustomLevel YURI6037_ERROR;

    static {
        YURI6037_ERROR = new CustomLevel("YURI6037_ERROR", java.util.logging.Level.SEVERE.intValue());
    }
}
