package com.twanl.realtimesupport.util;

import com.twanl.realtimesupport.RealTimeSupport;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Author: Twan Luttik
 * Date: 10/30/2018
 */

public class LoadManager {

    private static HashSet<Double> configVersion = new HashSet<>();
    private static HashSet<Boolean> prefix = new HashSet<>();
    private static HashSet<Integer> user_max_tickets = new HashSet<>();
    private static List<String> Subjects = new ArrayList<>();

    public static void loadHashSet() {
        RealTimeSupport plugin = RealTimeSupport.getPlugin(RealTimeSupport.class);

        // Clear all the Hash/List before putting data into it
        clearHashSet();

        configVersion.add(plugin.getConfig().getDouble("config-version"));
        prefix.add(plugin.getConfig().getBoolean("prefix-enabled"));
        user_max_tickets.add(plugin.getConfig().getInt("user-max-tickets"));
        Subjects.addAll(plugin.getConfig().getStringList("subjects"));
    }

    public static Double configVersion() {
        return Double.valueOf(removeBrackets(configVersion.toString()));
    }

    public static Boolean prefix() {
        return prefix.contains(true);
    }

    public static Integer user_max_tickets() {
        return Integer.valueOf(removeBrackets(user_max_tickets.toString()));
    }

    public static List subjects() {
        return Subjects;
    }
    private static void clearHashSet() {
        configVersion.clear();
        prefix.clear();
        user_max_tickets.clear();
        Subjects.clear();
    }

    private static String removeBrackets(String value) {
        return value.replace("[", "").replace("]", "");
    }


}
