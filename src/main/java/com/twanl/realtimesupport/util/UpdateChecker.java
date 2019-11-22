package com.twanl.realtimesupport.util;

import com.twanl.realtimesupport.RealTimeSupport;
import org.bukkit.Bukkit;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Twan Luttik
 * Date: 10/4/2018
 */

public class UpdateChecker {

    private RealTimeSupport plugin = RealTimeSupport.getPlugin(RealTimeSupport.class);

    // get the updated version from the spigotMC API
    public String getUpdatedVersion() {
        StringBuilder sb = new StringBuilder();

        try {
            int version = 61288;
            URLConnection connection = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + version).openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }
            in.close();
        } catch (IOException e) {
            System.out.println("---------------------------------");
            plugin.getLogger().info("Failed to check for a update on spigot.");
            System.out.println("---------------------------------");
        }
        return sb.toString();
    }

    // check if the plugin is in a pre release
    public boolean isPreRelease() {
        //This is assuming you pass in the substring from input.
        File file = new File(plugin.getDataFolder(), "../"); //Change this to the directory you want to search in.

        List<String> filesContainingSubstring = new ArrayList<String>();

        if (file.exists() && file.isDirectory()) {
            String[] files = file.list(); //get the files in String format.
            for (String fileName : files) {
                if (fileName.contains("RealTimeSupport"))
                    filesContainingSubstring.add(fileName);
            }
        }
        for (String key : filesContainingSubstring) {
            if (key.contains("-R")){
                Bukkit.getConsoleSender().sendMessage(Strings.logName + Strings.green + "Currently running a dev build!");
                return true;
            }
        }
        return false;
    }

    public String preReleaseVersion() {
        //This is assuming you pass in the substring from input.
        File file = new File(plugin.getDataFolder(), "../"); //Change this to the directory you want to search in.

        List<String> filesContainingSubstring = new ArrayList<String>();

        if (file.exists() && file.isDirectory()) {
            String[] files = file.list(); //get the files in String format.
            for (String fileName : files) {
                if (fileName.contains("RealTimeSupport"))
                    filesContainingSubstring.add(fileName);
            }
        }
        for (String key : filesContainingSubstring) {
            if (key.contains("-R")){
                String[] a = key.split("RealTimeSupport-");
                return a[1].replace(".jar", "");
            }
        }
        return "";
    }

    // check if the plugin has a an update
    public boolean hasUpdate() {
        return !plugin.getDescription().getVersion().equals(getUpdatedVersion());
    }
}
