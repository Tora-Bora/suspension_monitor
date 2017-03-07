package com.example.max.suspensionmonitor;

/**
 * Created by Max on 25.02.2017.
 */

public class Constants {
    public interface ACTION {
        public static String MAIN_ACTION = "com.example.max.suspensionmonitor.action.main";
        public static String INIT_ACTION = "com.example.max.suspensionmonitor.action.init";
        public static String PREV_ACTION = "com.example.max.suspensionmonitor.action.prev";
        public static String PLAY_ACTION = "com.example.max.suspensionmonitore.action.play";
        public static String NEXT_ACTION = "com.example.max.suspensionmonitor.action.next";
        public static String STARTFOREGROUND_ACTION = "com.example.max.suspensionmonitor.action.startforeground";
        public static String STOPFOREGROUND_ACTION = "com.example.max.suspensionmonitor.action.stopforeground";
    }

    public interface NOTIFICATION_ID {
        public static int FOREGROUND_SERVICE = 101;
    }
}
