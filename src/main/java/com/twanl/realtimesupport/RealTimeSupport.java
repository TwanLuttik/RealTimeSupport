package com.twanl.realtimesupport;

import com.twanl.realtimesupport.NMS.VersionHandler;
import com.twanl.realtimesupport.NMS.v1_10.v1_10_R1;
import com.twanl.realtimesupport.NMS.v1_11.v1_11_R1;
import com.twanl.realtimesupport.NMS.v1_12.v1_12_R1;
import com.twanl.realtimesupport.NMS.v1_13.v1_13_R1;
import com.twanl.realtimesupport.NMS.v1_13.v1_13_R2;
import com.twanl.realtimesupport.NMS.v1_14.v1_14_R1;
import com.twanl.realtimesupport.NMS.v1_8.v1_8_R1;
import com.twanl.realtimesupport.NMS.v1_8.v1_8_R2;
import com.twanl.realtimesupport.NMS.v1_8.v1_8_R3;
import com.twanl.realtimesupport.NMS.v1_9.v1_9_R1;
import com.twanl.realtimesupport.NMS.v1_9.v1_9_R2;
import com.twanl.realtimesupport.events.ChatEvent;
import com.twanl.realtimesupport.events.JoinEvent;
import com.twanl.realtimesupport.lib.Lib;
import com.twanl.realtimesupport.lib.SQLlib;
import com.twanl.realtimesupport.menu.*;
import com.twanl.realtimesupport.service.PurgeTicketService;
import com.twanl.realtimesupport.service.TicketLoadService;
import com.twanl.realtimesupport.util.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

//import com.twanl.realtimesupport.menu.item.Glow;

/**
 * Author: Twan Luttik
 * Date: 10/2/2018
 */

public class RealTimeSupport extends JavaPlugin {

    private RealTimeSupport instance;


    private PluginDescriptionFile pdfFile = getDescription();
    private final String PluginVersionOn = Strings.green + "(" + pdfFile.getVersion() + ")";
    private final String PluginVersionOff = Strings.red + "(" + pdfFile.getVersion() + ")";
    public VersionHandler nms;
    private Connection connection;
    public String host, database, username, password;
    public boolean ssl;
    public int port;

    @Override
    public void onEnable() {
        instance = this;



        // if the database methode is SQL than use the sql else use the file methode
        if (getConfig().getBoolean("MySQL.enable-MySQL")) {
            mysqlSetup();
            SQLlib sql = new SQLlib();
            try {
                sql.createTable();
            } catch (Exception ignored) {
            }
        }

        Load();
        getServerVersion();
        registerCommands();
        registerEvents();
//        RegisterGlow();
        BukkitTask TaskName = new PurgeTicketService().runTaskTimer(this, 20, 18000);


        Bukkit.getConsoleSender().sendMessage(Strings.logName + Strings.green + "Has been enabled " + PluginVersionOn);

        // metrics
        new Metrics(this);
        if (new Metrics(this).isEnabled()) {
            Bukkit.getConsoleSender().sendMessage(Strings.logName + Strings.green + "metrics enabled!");
        }

        UpdateChecker checker = new UpdateChecker();
        if (!getDescription().getVersion().equals(checker.getUpdatedVersion()) && checker.isPreRelease()) {
            getServer().getConsoleSender().sendMessage(Strings.green + "");
            getServer().getConsoleSender().sendMessage(Strings.green + "---------------------------------");
            getServer().getConsoleSender().sendMessage(Strings.red + "WARNING, This is a Dev Build, You have to manually look for a new update.");
            getServer().getConsoleSender().sendMessage(Strings.green + "Version: " + checker.preReleaseVersion());
            getServer().getConsoleSender().sendMessage(Strings.green + "---------------------------------");
            getServer().getConsoleSender().sendMessage(Strings.green + "");
        } else if (checker.hasUpdate() || checker.isPreRelease()) {
            if (checker.isPreRelease()) {
                getServer().getConsoleSender().sendMessage(Strings.green + "");
                getServer().getConsoleSender().sendMessage(Strings.green + "------------------------");
                getServer().getConsoleSender().sendMessage(Strings.red + "You are running a Dev build, please download the newest stable build!");
                getServer().getConsoleSender().sendMessage(Strings.white + "Newest version: " + checker.getUpdatedVersion());
                getServer().getConsoleSender().sendMessage(Strings.white + "Your version: " + Strings.green + getDescription().getVersion());
                getServer().getConsoleSender().sendMessage("Please download the new version at https://www.spigotmc.org/resources/realtime-support-beta.61288/");
                getServer().getConsoleSender().sendMessage(Strings.green + "------------------------");
                getServer().getConsoleSender().sendMessage(Strings.green + "");

            } else {
                getServer().getConsoleSender().sendMessage(Strings.green + "");
                getServer().getConsoleSender().sendMessage(Strings.green + "------------------------");
                getServer().getConsoleSender().sendMessage(Strings.red + "RealTime Support is outdated!");
                getServer().getConsoleSender().sendMessage(Strings.white + "Newest version: " + checker.getUpdatedVersion());
                getServer().getConsoleSender().sendMessage(Strings.white + "Your version: " + Strings.green + getDescription().getVersion());
                getServer().getConsoleSender().sendMessage("Please download the new version at https://www.spigotmc.org/resources/realtime-support-beta.61288/");
                getServer().getConsoleSender().sendMessage(Strings.green + "------------------------");
                getServer().getConsoleSender().sendMessage(Strings.green + "");
            }
        } else {
            getServer().getConsoleSender().sendMessage(Strings.green + "");
            getServer().getConsoleSender().sendMessage(Strings.green + "---------------------------------");
            getServer().getConsoleSender().sendMessage(Strings.green + "RealTime Support is up to date.");
            getServer().getConsoleSender().sendMessage(Strings.green + "---------------------------------");
            getServer().getConsoleSender().sendMessage(Strings.green + "");
        }
        TicketLoadService tls = new TicketLoadService();
        tls.loadTickets();
    }

    public RealTimeSupport getInstance() {
        return instance;
    }


    @Override
    public void onDisable() {
        Load();
        Bukkit.getConsoleSender().sendMessage(Strings.logName + Strings.red + "Has been disabled " + PluginVersionOff);
    }



    public void Load() {
        // Initialize
        ConfigManager config = new ConfigManager();
        Lib lib = new Lib();
        config.setup();

        // Save all and reload the file's
        config.saveTicketSolved();
        config.reloadTicketSolved();
        config.saveTicket();
        config.reloadTicket();
        config.savePlayers();
        config.reloadPlayers();

        // Check for config update
        lib.configVersionUpdate();

        // Load all the option's into a HashSet
        LoadManager.loadHashSet();
    }

    private void registerCommands() {
        Commands commands = new Commands();
        getCommand("rs").setExecutor(commands);
    }


    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new CreateTicket_UI(), this);
        getServer().getPluginManager().registerEvents(new Tickets_UI(), this);
        getServer().getPluginManager().registerEvents(new RateStaff_UI(), this);
        getServer().getPluginManager().registerEvents(new TicketOptions_UI(), this);
        getServer().getPluginManager().registerEvents(new TicketStatus_UI(), this);
        getServer().getPluginManager().registerEvents(new ReplyMessages_UI(), this);
        getServer().getPluginManager().registerEvents(new AssignedTickets_UI(), this);
        getServer().getPluginManager().registerEvents(new ChatEvent(), this);
        getServer().getPluginManager().registerEvents(new JoinEvent(), this);
    }

    private void getServerVersion() {
        String a = getServer().getClass().getPackage().getName();
        String version = a.substring(a.lastIndexOf('.') + 1);

        // Check if the server has the same craftbukkit version as this plugin
        if (version.equalsIgnoreCase("v1_8_R1")) {
            nms = new v1_8_R1();
        } else if (version.equalsIgnoreCase("v1_8_R2")) {
            nms = new v1_8_R2();
        } else if (version.equalsIgnoreCase("v1_8_R3")) {
            nms = new v1_8_R3();
        } else if (version.equalsIgnoreCase("v1_9_R1")) {
            nms = new v1_9_R1();
        } else if (version.equalsIgnoreCase("v1_9_R2")) {
            nms = new v1_9_R2();
        } else if (version.equalsIgnoreCase("v1_10_R1")) {
            nms = new v1_10_R1();
        } else if (version.equalsIgnoreCase("v1_11_R1")) {
            nms = new v1_11_R1();
        } else if (version.equalsIgnoreCase("v1_12_R1")) {
            nms = new v1_12_R1();
        } else if (version.equalsIgnoreCase("v1_13_R1")) {
            nms = new v1_13_R1();
        } else if (version.equalsIgnoreCase("v1_13_R2")) {
            nms = new v1_13_R2();
        } else if (version.equalsIgnoreCase("v1_14_R1")) {
            nms = new v1_14_R1();
        } else {
            getServer().getConsoleSender().sendMessage(Strings.logName + Strings.red + "This plugin wil not work properly with version " + version);
        }
    }


    public void mysqlSetup() {

        host = getConfig().getString("MySQL.host");
        port = getConfig().getInt("MySQL.port");
        database = getConfig().getString("MySQL.database");
        username = getConfig().getString("MySQL.username");
        password = getConfig().getString("MySQL.password");

        ssl = getConfig().getBoolean("MySQL.ssl");

        try {
            synchronized (this) {
                if (getConnection() != null && !getConnection().isClosed()) {
                    return;
                }

                Class.forName("com.mysql.jdbc.Driver");
                setConnection(DriverManager.getConnection("jdbc:mysql://" + this.host + ":"
                        + this.port + "/" + this.database + "?useSSL=" + ssl, this.username, this.password));


                Bukkit.getConsoleSender().sendMessage(Strings.logName + "mySQL connected to database: " + Strings.green + database);
            }
        } catch (SQLException | ClassNotFoundException e) {
            Bukkit.getConsoleSender().sendMessage(Strings.logName + Strings.red + "mySQL cannot find database");
            Bukkit.getConsoleSender().sendMessage(Strings.logName + Strings.red + "shutting down this plugin");
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }


}
