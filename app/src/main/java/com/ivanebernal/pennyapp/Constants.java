package com.ivanebernal.pennyapp;

/**
 * Created by ivan on 23/06/16.
 */
public class Constants {
    public interface ACTION{
        public static String MAIN_ACTION = "com.ivanebernal.ponnyapp.action.main";
        public static String PREVIOUS_ACTION = "com.ivanebernal.ponnyapp.action.previous";
        public static String NEXT_ACTION = "com.ivanebernal.ponnyapp.action.next";
        public static String PLAY_ACTION = "com.ivanebernal.ponnyapp.action.play";
        public static String STARTFOREGROUND_ACTION = "com.ivanebernal.ponnyapp.action.startforeground";
        public static String STOPFOREGROUND_ACTION = "com.ivanebernal.ponnyapp.action.stopforeground";
    }

    public interface NOTIFICATION_ID{
        public static int FOREGROUND_SERVICE = 101;
    }
}
