package com.twanl.realtimesupport.service;

import com.twanl.realtimesupport.RealTimeSupport;
import com.twanl.realtimesupport.lib.Lib;
import com.twanl.realtimesupport.util.ConfigManager;
import com.twanl.realtimesupport.util.Strings;
import org.bukkit.Bukkit;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TicketLoadService {

    private RealTimeSupport plugin = RealTimeSupport.getPlugin(RealTimeSupport.class);
    private ConfigManager config = new ConfigManager();
    private static HashMap<Integer, String> createdBy = new HashMap<>();
    private static HashMap<Integer, String> createdDate = new HashMap<>();
    private static HashMap<Integer, String> subject = new HashMap<>();
    private static HashMap<Integer, String> status = new HashMap<>();
    private static HashMap<Integer, String> assignedFrom = new HashMap<>();
    private static HashMap<Integer, String> rating = new HashMap<>();
    private static HashMap<Integer, String> solvedDate = new HashMap<>();

    public static List<Integer> openList = new ArrayList<>();
    public static List<Integer> assignedList = new ArrayList<>();
    public static List<Integer> solvedList = new ArrayList<>();

    public void loadTickets() {
        // Initialize
        Lib lib = new Lib();

        // Clear all list before putting data into it, prevent for duplicate's
        openList.clear();
        assignedList.clear();
        solvedList.clear();

        if (lib.sqlEnabled()) {
            try {
                PreparedStatement statement = plugin.getConnection().prepareStatement("SELECT * FROM tickets_rts");
                ResultSet rs = statement.executeQuery();
                while (rs.next()) {
                    String a = rs.getString("status");
                    int ID = rs.getInt("ID");


                    switch (a) {
                        case "OPEN":
                            Bukkit.getConsoleSender().sendMessage(Strings.green + ID + " " + rs.getString("createdDate") + " " + rs.getString("subject") + " " + a + " " +  rs.getString("location")
                                    + " " + rs.getString("assignedFrom") + " " + rs.getString("solvedDate") + " " + rs.getString("rating") );
                            openList.add(ID);
                            break;
                        case "ASSIGNED":
                            Bukkit.getConsoleSender().sendMessage(Strings.green + ID + " " + rs.getString("createdDate") + " " + rs.getString("subject") + " " + a + " " +  rs.getString("location")
                                    + " " + rs.getString("assignedFrom") + " " + rs.getString("solvedDate") + " " + rs.getString("rating") );
                            assignedList.add(ID);
                            break;
                        case "SOLVED":
                            Bukkit.getConsoleSender().sendMessage(Strings.red + ID + " " + rs.getString("createdDate") + " " + rs.getString("subject") + " " + a + " " +  rs.getString("location")
                                    + " " + rs.getString("assignedFrom") + " " + rs.getString("solvedDate") + " " + rs.getString("rating") );
                            solvedList.add(ID);
                            break;
                    }
                }
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }

        } else {
            // Initialize
            config.setup();

            // For loop trough the tickets file
            for (String key : config.getTicket().getConfigurationSection("").getKeys(false)) {
                int ID = Integer.parseInt(key);

                String a = config.getTicket().getString(ID + ".createdBy");
                String b = config.getTicket().getString(ID + ".createdDate");
                String c = config.getTicket().getString(ID + ".subject");
                String d = config.getTicket().getString(ID + ".status");
                String e = config.getTicket().getString(ID + ".assignedFrom");
                createdBy.put(ID, a);
                createdDate.put(ID, b);
                subject.put(ID, c);
                status.put(ID, d);
                assignedFrom.put(ID, e);

                if (lib.getTicketStatus(ID).equals("OPEN")) {
                    openList.add(ID);
                } else {
                    assignedList.add(ID);
                }

                Bukkit.getConsoleSender().sendMessage(Strings.green + createdBy.get(ID) + " " +
                        createdDate.get(ID) + " " +
                        subject.get(ID) + " " +
                        status.get(ID) + " " +
                        assignedFrom.get(ID));
            }

            for (String key1 : config.getTicketSolved().getConfigurationSection("").getKeys(false)) {
                int ID = Integer.parseInt(key1);


                String a1 = config.getTicketSolved().getString(ID + ".createdBy");
                String b1 = config.getTicketSolved().getString(ID + ".createdDate");
                String c1 = config.getTicketSolved().getString(ID + ".subject");
                String d1 = config.getTicketSolved().getString(ID + ".status");
                String e1 = config.getTicketSolved().getString(ID + ".assignedFrom");
                String f1 = config.getTicketSolved().getString(ID + ".rating");
                String g1 = config.getTicketSolved().getString(ID + ".solvedDate");
                createdBy.put(ID, a1);
                createdDate.put(ID, b1);
                subject.put(ID, c1);
                status.put(ID, d1);
                assignedFrom.put(ID, e1);
                rating.put(ID, f1);
                solvedDate.put(ID, g1);

                solvedList.add(ID);
                Bukkit.getConsoleSender().sendMessage(Strings.red + createdBy(ID) + " " +
                        createdDate(ID) + " " +
                        subject(ID) + " " +
                        status(ID) + " " +
                        assignedFrom(ID) + " " +
                        rating(ID) + " " +
                        solvedDate(ID));
            }

        }
    }


    public void addTicket(int ID) {
        config.setup();

        String a = config.getTicket().getString(ID + ".createdBy");
        String b = config.getTicket().getString(ID + ".createdDate");
        String c = config.getTicket().getString(ID + ".subject");
        String d = config.getTicket().getString(ID + ".status");
        String e = config.getTicket().getString(ID + ".assignedFrom");
        String f = config.getTicket().getString(ID + ".rating");
        createdBy.put(ID, a);
        createdDate.put(ID, b);
        subject.put(ID, c);
        status.put(ID, d);
        assignedFrom.put(ID, e);
        rating.put(ID, f);
    }

    public String createdBy(int ID) {
        return createdBy.get(ID);
    }

    public void update_createdBy(int ID) {
        config.setup();
        createdBy.put(ID, config.getTicket().getString(ID + ".createdBy"));
    }

    public String createdDate(int ID) {
        return createdDate.get(ID);
    }

    public void update_createdDate(int ID) {
        config.setup();
        createdDate.put(ID, config.getTicket().getString(ID + ".createdDate"));
    }

    public String subject(int ID) {
        return subject.get(ID);
    }

    public void update_subject(int ID) {
        config.setup();
        subject.put(ID, config.getTicket().getString(ID + ".subject"));
    }

    public String status(int ID) {
        return status.get(ID);
    }

    public void update_status(int ID) {
        config.setup();
        status.put(ID, config.getTicket().getString(ID + ".status"));
    }

    public String assignedFrom(int ID) {
        return assignedFrom.get(ID);
    }

    public void update_assignedFrom(int ID) {
        config.setup();
        assignedFrom.put(ID, config.getTicket().getString(ID + ".assignedFrom"));
    }

    public String rating(int ID) {
        return rating.get(ID);
    }

    public void update_rating(int ID) {
        config.setup();
        rating.put(ID, config.getTicketSolved().getString(ID + ".rating"));
    }

    public String solvedDate(int ID) {
        return solvedDate.get(ID);
    }

    public void update_solvedDate(int ID) {
        config.setup();
        solvedDate.put(ID, config.getTicket().getString(ID + ".solvedDate"));
    }

    public void removeTicket(int ID) {
        // Clear all list's
        Object a = ID;
        openList.remove(a);
        assignedList.remove(a);
        solvedList.remove(a);

        // Clear all HashMaps's
        createdBy.remove(ID);
        createdDate.remove(ID);
        subject.remove(ID);
        status.remove(ID);
        assignedFrom.remove(ID);
        rating.remove(ID);
        solvedDate.remove(ID);
    }

}
