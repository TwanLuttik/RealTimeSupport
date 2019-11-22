package com.twanl.realtimesupport.service;

import com.twanl.realtimesupport.RealTimeSupport;
import com.twanl.realtimesupport.lib.Lib;
import com.twanl.realtimesupport.util.ConfigManager;
import com.twanl.realtimesupport.util.Strings;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Author: Twan Luttik
 * Date: 10/4/2018
 */

public class PurgeTicketService extends BukkitRunnable {

    private RealTimeSupport plugin = RealTimeSupport.getPlugin(RealTimeSupport.class);
    private TicketLoadService tls = new TicketLoadService();
    private ConfigManager config = new ConfigManager();
    private int purgedTickets_SESSION = 0;

    @Override
    public void run() {
        purgeTickets();
    }

    public int getNear15Minute(int minutes) {
        int mod = minutes % 15;
        int res = 0;
        if ((mod) >= 8) {
            res = minutes + (15 - mod);
        } else {
            res = minutes - mod;
        }
        return (res % 60);
    }


    private boolean DateExpired(int id) {
        // Initialze
        config.setup();
        Lib lib = new Lib();

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        String[] a = dtf.format(now).split(":");
        int i = getNear15Minute(Integer.parseInt(a[1]));
        String b = a[0] + ":" + i;


//        String c = config.getTicketSolved().getString(id + ".solvedDate");
        String c = lib.getTicketSolvedDate(id);

        // This will remove the tickets that passed the date, Like when a server is down for a few day's
        try {
            if (new SimpleDateFormat("yyyy/MM/dd HH:mm").parse(c).before(new Date())) {
                return true;
            }
        } catch (ParseException ignored) {
        }

        return c.equals(b);
    }

    private String getTime() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
    }

    public void purgeTickets() {
        Lib lib = new Lib();
        purgedTickets_SESSION = 0;
        config.setup();
        Bukkit.getConsoleSender().sendMessage("");
        Bukkit.getConsoleSender().sendMessage(Strings.blue + "-- PURGETICKET SERVICE STARTED AT " + getTime());

        List<Integer> ticketID = new ArrayList<>();

        // Put all solved ticket id's into a list
        if (lib.sqlEnabled()) {
            try {
                PreparedStatement statement = plugin.getConnection().prepareStatement("SELECT * FROM tickets_rts WHERE status = ?;");
                statement.setString(1, "SOLVED");
                ResultSet rs = statement.executeQuery();

                while (rs.next()) {
                    int ID = rs.getInt("ID");
                    ticketID.add(ID);
                }
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            for (String key : config.getTicketSolved().getConfigurationSection("").getKeys(false)) {
                int ID = Integer.parseInt(key);
                if (lib.getTicketStatus(ID).equals("SOLVED")) {
                    ticketID.add(ID);
                }
            }
        }

        // for loop trough tge ArrayList
        for (Integer ID : ticketID) {
            Bukkit.getConsoleSender().sendMessage(Strings.gold + "-- TICKET " + ID + "# FOUND");

            if (DateExpired(ID)) {
                // Remove the ticket
                lib.removeTicket(ID);
                Bukkit.getConsoleSender().sendMessage(Strings.green + "-- TICKET " + ID + "# REMOVED");
                purgedTickets_SESSION++;
            } else {
                Bukkit.getConsoleSender().sendMessage(Strings.red + "-- TICKET " + ID + "# NOT YET EXPIRED");
            }


        }

        Bukkit.getConsoleSender().sendMessage(Strings.blue + "-- PURGETICKET SERVICE ENDED");
        Bukkit.getConsoleSender().sendMessage("");
        DynamicInventoryUpdatesService a = new DynamicInventoryUpdatesService();
        a.updateInventory(0, null);


//        if (lib.sqlEnabled()) {
//            try {
//                PreparedStatement statement = plugin.getConnection().prepareStatement("SELECT * FROM tickets_rts WHERE status = ?;");
//                statement.setString(1, "SOLVED");
//                ResultSet rs = statement.executeQuery();
//
//                while (rs.next()) {
//                    int ID = rs.getInt("ID");
//                    Bukkit.getConsoleSender().sendMessage(Strings.gold + "-- TICKET " + ID + "# FOUND");
//
//                    if (DateExpired(ID)) {
//                        // Remove the ticket
//                        lib.deleteTicket(ID);
//                        Bukkit.getConsoleSender().sendMessage(Strings.green + "-- TICKET " + ID + "# REMOVED");
//                        purgedTickets_SESSION++;
//                    } else {
//                        Bukkit.getConsoleSender().sendMessage(Strings.red + "-- TICKET " + ID + "# NOT YET EXPIRED");
//                    }
//                }
//                rs.close();
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
//        } else {
//            for (String key : config.getTicketSolved().getConfigurationSection("").getKeys(false)) {
//
//                int ID = Integer.parseInt(key);
//                if (config.getTicketSolved().get(ID + ".status").equals("SOLVED")) {
//                    Bukkit.getConsoleSender().sendMessage(Strings.gold + "-- TICKET " + ID + "# FOUND");
//
//                    if (DateExpired(ID)) {
//                        config.getTicketSolved().set(ID + "", null);
//                        config.saveTicketSolved();
//                        tls.removeTicket(ID);
//                        Bukkit.getConsoleSender().sendMessage(Strings.green + "-- TICKET " + ID + "# REMOVED");
//                        purgedTickets_SESSION++;
//                    } else {
//                        Bukkit.getConsoleSender().sendMessage(Strings.red + "-- TICKET " + ID + "# NOT YET EXPIRED");
//                    }
//                }
//            }
//        }
//        Bukkit.getConsoleSender().sendMessage(Strings.blue + "-- PURGETICKET SERVICE ENDED");
//        Bukkit.getConsoleSender().sendMessage("");
//        DynamicInventoryUpdatesService a = new DynamicInventoryUpdatesService();
//        a.updateInventory(0, null);
    }

    public int sessionPurgedTickets() {
        return purgedTickets_SESSION;
    }

}
