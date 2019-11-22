package com.twanl.realtimesupport.lib;

import com.twanl.realtimesupport.RealTimeSupport;
import com.twanl.realtimesupport.menu.RateStaff_UI;
import com.twanl.realtimesupport.service.DynamicInventoryUpdatesService;
import com.twanl.realtimesupport.service.NotificationSerivce;
import com.twanl.realtimesupport.service.PurgeTicketService;
import com.twanl.realtimesupport.service.TicketLoadService;
import com.twanl.realtimesupport.util.ConfigManager;
import com.twanl.realtimesupport.util.Strings;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.twanl.realtimesupport.service.TicketLoadService.*;

/**
 * Author: Twan Luttik
 * Date: 10/3/2018
 */

@SuppressWarnings({"JavaDoc", "ALL"})
public class Lib {

    private RealTimeSupport plugin = RealTimeSupport.getPlugin(RealTimeSupport.class);
    private ConfigManager config = new ConfigManager();
    private PurgeTicketService PurgeService = new PurgeTicketService();
    private DynamicInventoryUpdatesService dynamicUpdateService = new DynamicInventoryUpdatesService();
    private TicketLoadService tls = new TicketLoadService();
    private SQLlib sql = new SQLlib();

    public String sql_table_tickets = "tickets_rts";
    public String sql_table_players = "player_rts";
    public String sql_table_staff = "staff_rts";
    public String sql_table_messages = "messages_rts";

    private List<Integer> ticketID = new ArrayList<>();
    public static HashMap<Player, Integer> createdTicket_id = new HashMap<>();
    public static HashMap<Player, Integer> menu_State = new HashMap<>();
    public static HashSet<Integer> test = new HashSet<>();
    private List<Integer> ID = new ArrayList<>();

    /**
     * CreateCommand a ticket with a message
     *
     * @param p       is the creator of the ticket
     * @param message
     */
    public void createTicket(Player p, String message) {
        // Initialize
        config.setup();
        int ID = nextID();

        // Get the timestamp of time
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();

        if (sqlEnabled()) {
            try {
                PreparedStatement statement = plugin.getConnection().prepareStatement("INSERT INTO " + sql_table_tickets + " (ID, createdBy, createdDate, subject, status, location, assignedFrom, solvedDate, rating) VALUES (?,?,?,?,?,?,?,?,?)");
                statement.setInt(1, ID);
                statement.setString(2, p.getUniqueId().toString());
                statement.setString(3, dtf.format(now));
                statement.setString(4, null);
                statement.setString(5, "OPEN");
                statement.setString(6, null);
                statement.setString(7, null);
                statement.setString(8, null);
                statement.setString(9, null);
                statement.executeUpdate();
                statement.close();

            } catch (SQLException e) {
                e.printStackTrace();
            }

            // Set the message
            setMessage(ID, p, message);

            // Cache the ticket ID
            createdTicket_id.put(p, ID);

            // add the player to the db
            if (!sql.playerHasAccount(p.getUniqueId())) {
                sql.addPlayer(p.getUniqueId());
            }


            // add the ticket to the player
            playerAddTicket(p.getUniqueId(), ID);

            // message's
            NotificationSerivce.createTicket(p, ID);

            // add the ticket to hashmap
            tls.addTicket(ID);
            openList.add(ID);

            // DynamicInventoryUpdateService
//            dynamicUpdateService.ticketCreateUpdate();
            dynamicUpdateService.updateInventory(ID, null);
        } else {
            // Set all the data
            config.getTicket().set(ID + ".createdBy", p.getUniqueId().toString());
            config.getTicket().set(ID + ".createdDate", dtf.format(now));
            config.getTicket().set(ID + ".subject", "NONE");
            config.getTicket().set(ID + ".status", "OPEN");
            config.getTicket().set(ID + ".assignedFrom", "");
            config.saveTicket();
            setMessage(ID, p, message);

            // Cache the ticket ID
            createdTicket_id.put(p, ID);

            // Add the ticket ID to the player
            playerAddTicket(p.getUniqueId(), ID);

            // message's
            NotificationSerivce.createTicket(p, ID);

            // add the ticket to hashmap
            tls.addTicket(ID);
            openList.add(ID);

            // DynamicInventoryUpdateService
//            dynamicUpdateService.ticketCreateUpdate();
            dynamicUpdateService.updateInventory(ID, null);
        }

    }

    /**
     * @param p
     * @param ID
     */
    public void assignTicket(Player p, int ID) {
        if (sqlEnabled()) {
            // Initialize
            String a = getStaffTickets(p.getUniqueId());


            // Add the ticket id to the staff
            staffAddTicket(p.getUniqueId(), ID);

            // update the status & assignedFrom
            updateTicketStatus(ID, "ASSIGNED");
            updateTicketAssignedBy(p.getUniqueId(), ID);

            // Send the player a message that his tickets is assigned
            NotificationSerivce.assignTicket(p, ID);

            // Update the List
            Object a1 = ID;
            openList.remove(a1);
            assignedList.add(ID);

            // DynamicInventoryUpdateService
//            dynamicUpdateService.ticketAssignedUpdate(ID);
            dynamicUpdateService.updateInventory(ID, getTicketCreatedByPlayer(ID).getUniqueId());

        } else {
            // Initialize
            config.setup();
            String a = getStaffTickets(p.getUniqueId());

            // Check if the player already has assigned that Ticket
            if (a.contains(ID + ",")) {
                return;
            }

            // Add the ticket id to the staff
            staffAddTicket(p.getUniqueId(), ID);

            // update the status & assignedFrom
            updateTicketStatus(ID, "ASSIGNED");
            updateTicketAssignedBy(p.getUniqueId(), ID);

            // Send the player a message that his tickets is assigned
            NotificationSerivce.assignTicket(p, ID);

            // Update the status, assignedFrom
            tls.update_status(ID);
            tls.update_assignedFrom(ID);

            // Update the List
            Object a1 = ID;
            openList.remove(a1);
            assignedList.add(ID);

            // DynamicInventoryUpdateService
//            dynamicUpdateService.ticketAssignedUpdate(ID);
            dynamicUpdateService.updateInventory(ID, getTicketCreatedByPlayer(ID).getUniqueId());
        }
    }

    /**
     * @param p  Close the Ticket from the player
     * @param ID Close the Ticket by ID
     */
    public void closeTicket(Player p, int ID) {
        if (sqlEnabled()) {

            // Check if the tickets is assigned from a staff, than add analytics data to the staff
            if (getTicketStatus(ID).equals("ASSIGNED")) {

                // Remove the ticketID from the staff
                OfflinePlayer staff = getTicketAssingedByPlayer(ID);
                staffRemoveTicket(staff.getUniqueId(), ID);

                // Add analytics to the player
                Player p1 = (Player) getTicketAssingedByPlayer(ID);
                addSolvedTicket(p1);

//                p1.sendMessage(Strings.prefix() + p.getName() + Strings.gray + " closed the Ticket.");

                // Send UI for rating the staff for how the ticket is resolved
                test.add(ID);
                RateStaff_UI rc = new RateStaff_UI();
                rc.openTicketsMenu(p);

            }

            // Set the Ticket status to solved
            updateTicketStatus(ID, "SOLVED");

            // Set the TimeStamp for the Ticket
            setSolvedDate(ID);

            // Remove the Ticket id from the player
            playerRemoveTicket(p.getUniqueId(), ID);

            // send a sound output for the staff BLOCK_NOTE_BLOCK_BELL
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 5, 5);

//            p.sendMessage(Strings.prefix() + Strings.gray + "You succsessfully closed your Ticket!");
            NotificationSerivce.closeTicket(p, ID);

            // Update the List
            Object a1 = ID;
            assignedList.remove(a1);
            solvedList.add(ID);

            // DynamicInventoryUpdateService
//            dynamicUpdateService.ticketSolvedUpdate(ID);
            dynamicUpdateService.updateInventory(ID, getTicketCreatedByPlayer(ID).getUniqueId());

        } else {

            // Initialize config
            config.setup();

            // Check if the tickets is assigned from a staff, than add analytics data to the staff
            if (getTicketStatus(ID).contains("ASSIGNED")) {

                // Remove the ticketID from the staff
                OfflinePlayer p2 = getTicketAssingedByPlayer(ID);
                staffRemoveTicket(p2.getUniqueId(), ID);

                // Add analytics to the player
                Player p1 = (Player) getTicketAssingedByPlayer(ID);
                addSolvedTicket(p1);

//                p1.sendMessage(Strings.prefix() + p.getName() + Strings.gray + " closed the Ticket.");

                // Send UI for rating the staff for how the ticket is resolved
                test.add(ID);
                RateStaff_UI rc = new RateStaff_UI();
                rc.openTicketsMenu(p);
            }

            // Set the Ticket status to solved
            updateTicketStatus(ID, "SOLVED");

            // Set the TimeStamp for the Ticket
            setSolvedDate(ID);
            tls.update_solvedDate(ID);

            // Remove the Ticket id from the player
            playerRemoveTicket(p.getUniqueId(), ID);

            // Move the Ticket to a different file
            String a = tls.createdBy(ID);
            String b = getTicketCreatedDate(ID);
            String c = getTicketSubject(ID);
            String d = getTicketStatus(ID);
            String e = tls.assignedFrom(ID);
            String f = getTicketSolvedDate(ID);

            config.getTicketSolved().set(ID + ".createdBy", a);
            config.getTicketSolved().set(ID + ".createdDate", b);
            config.getTicketSolved().set(ID + ".subject", c);
            config.getTicketSolved().set(ID + ".status", d);
            config.getTicketSolved().set(ID + ".assignedFrom", e);
            config.getTicketSolved().set(ID + ".solvedDate", f);
            config.saveTicketSolved();

            for (String key : config.getTicket().getConfigurationSection(ID + ".messages").getKeys(false)) {
                int i = Integer.parseInt(key);

                String message = config.getTicket().getString(ID + ".messages." + i + ".message");
                String messageReplyDate = config.getTicket().getString(ID + ".messages." + i + ".replyDate");
                config.getTicketSolved().set(ID + ".messages." + i + ".message", message);
                config.getTicketSolved().set(ID + ".messages." + i + ".replyDate", messageReplyDate);
                config.saveTicketSolved();
            }
            config.getTicket().set(ID + "", null);
            config.saveTicket();

            // send a sound output for the staff BLOCK_NOTE_BLOCK_BELL
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 5, 5);

            // Sending a message
            NotificationSerivce.closeTicket(p, ID);
//            p.sendMessage(Strings.prefix() + Strings.gray + "You succsessfully closed your Ticket!");

            // Update the List
            Object a1 = ID;
            assignedList.remove(a1);
            solvedList.add(ID);

            // DynamicInventoryUpdateService
//            dynamicUpdateService.ticketSolvedUpdate(ID);
            dynamicUpdateService.updateInventory(ID, getTicketCreatedByPlayer(ID).getUniqueId());
        }
    }

    /**
     * This will delete the Ticket and will not set the STATUS to solved
     * and it will not add a number to the analystics
     *
     * @param ID
     */
    public void deleteTicket(int ID) {
        // Initialize
        Player user = getTicketCreatedByPlayer(ID).getPlayer();

        if (sqlEnabled()) {

            // Check if the Ticket is assigned by a staff member
            if (getTicketStatus(ID).equals("ASSIGNED")) {
                // Initialize
                Player staff = getTicketAssingedByPlayer(ID).getPlayer();

                // Remove the Ticket ID from the STAFF member
                staffRemoveTicket(staff.getUniqueId(), ID);
            }

            // Remove the Ticket ID from the player
            playerRemoveTicket(user.getUniqueId(), ID);

            // Remove the hole Ticket
            removeTicket(ID);

            // Update the List, Check and remove the ticket ID from one of these list's, OPENLIST & SOLVEDLIST
            Object a1 = ID;
            if (openList.contains(ID)) {
                openList.remove(a1);
            }

            if (assignedList.contains(ID)) {
                assignedList.remove(a1);
            }

            // DynamicInventoryUpdateService
//            dynamicUpdateService.ticketDeleteUpdate(ID, user);
            dynamicUpdateService.updateInventory(ID, user.getUniqueId());

        } else {

            // Initialize
            config.setup();

            // Check if the Ticket is assigned by a staff member
            if (getTicketStatus(ID).equals("ASSIGNED")) {
                // Initialize
                OfflinePlayer staff = getTicketAssingedByPlayer(ID);

                // Remove the Ticket ID from the STAFF member
                staffRemoveTicket(staff.getUniqueId(), ID);
                Bukkit.getConsoleSender().sendMessage(Strings.yellow + "test");
            }

            // Remove the Ticket ID from the player
            playerRemoveTicket(user.getUniqueId(), ID);

            // Remove the hole Ticket
            removeTicket(ID);

            // Update the List, Check and remove the ticket ID from one of these list's, OPENLIST & SOLVEDLIST
            Object a1 = ID;
            if (openList.contains(ID)) {
                openList.remove(a1);
            }

            if (assignedList.contains(ID)) {
                assignedList.remove(a1);
            }

            // DynamicInventoryUpdateService
//            dynamicUpdateService.ticketDeleteUpdate(ID, user);
            dynamicUpdateService.updateInventory(ID, user.getUniqueId());

            // Remove the ticket from the hashmap
            tls.removeTicket(ID);
        }

    }


    // This will give the next coming number (it will see if a numer is missing in a sequence)
    private int nextID() {
        if (sqlEnabled()) {
            // Initialize
            int i = 1;

            // get all the BANK_ID and put into a List
            try {
                PreparedStatement statement = plugin.getConnection().prepareStatement("SELECT * FROM " + sql_table_tickets);
                ResultSet rs = statement.executeQuery();
                while (rs.next()) {
                    ID.add(rs.getInt("ID"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            // sort out the numbers from low -> high
            Collections.sort(ID);

            // get the next lowest number (it will search out the missing numbers)
            for (Integer i1 : ID) {
                if (i1 == i) {
                    i++;
                }
            }

            ID.clear();
            return i;
        } else {

            // Initialize
            config.setup();
            int i = 1;
            ticketID.clear();

            // If the file is empty it will start at 1
            if (config.getTicket().getKeys(false).size() == 0 && config.getTicketSolved().getKeys(false).size() == 0) {
                return 1;
            }

            // put all the ticketID to a List
            for (Integer id : openList) {
                ticketID.add(id);
            }

            for (Integer id : assignedList) {
                ticketID.add(id);
            }

            for (Integer id : solvedList) {
                ticketID.add(id);
            }


            // sort out the numbers from low -> high
            Collections.sort(ticketID);

            // get the next lowest number
            for (Integer i2 : ticketID) {
                if (i2 == i) {
                    i++;
                }
            }
            return i;
        }
    }

    private void setMessage(int ID, Player p, String message) {
        if (sqlEnabled()) {
            try {
                PreparedStatement statement = plugin.getConnection().prepareStatement("INSERT INTO " + sql_table_messages + " (ID, messageNumber, sender, message, time) values (?,?,?,?,?);");
                statement.setInt(1, ID);
                statement.setInt(2, 1);
                statement.setString(3, p.getName());
                statement.setString(4, message);
                statement.setString(5, getCurrentTime());
                statement.execute();
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            config.setup();
            config.getTicket().set(ID + ".messages.1.message", p.getName() + ":" + message);
            config.getTicket().set(ID + ".messages.1.replyDate", getCurrentTime());
            config.saveTicket();
        }

    }

    private void playerAddTicket(UUID uuid, int ID) {
        if (sqlEnabled()) {
            try {
                String a = getPlayerTickets(uuid) + ID + ",";
                PreparedStatement statement = plugin.getConnection().prepareStatement("UPDATE " + sql_table_players + " SET tickets = '" + a + "' WHERE UUID = ?;");
                statement.setString(1, uuid.toString());
                statement.executeUpdate();
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            config.setup();

            // Add the ticket ID to the player
            String a = getPlayerTickets(uuid);
            if (a == null) {
                config.getPlayers().set(uuid.toString() + ".tickets", ID + ",");
                config.savePlayers();
            } else {
                config.getPlayers().set(uuid.toString() + ".tickets", a + ID + ",");
                config.savePlayers();
            }
        }
    }

    private void playerRemoveTicket(UUID uuid, int ID) {
        if (sqlEnabled()) {
            try {
                String a = getPlayerTickets(uuid).replace(ID + ",", "");
                PreparedStatement statement = plugin.getConnection().prepareStatement("UPDATE " + sql_table_players + " SET tickets = '" + a + "' WHERE UUID = '" + uuid + "';");
                statement.executeUpdate();
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            config.setup();
            String a = config.getPlayers().getString(uuid + ".tickets").replace(ID + ",", "");
            config.getPlayers().set(uuid.toString() + ".tickets", a);
            config.savePlayers();
        }
    }

    private void staffAddTicket(UUID uuid, int ID) {
        if (sqlEnabled()) {
            try {
                String a = getStaffTickets(uuid) + ID + ",";
                PreparedStatement statement = plugin.getConnection().prepareStatement("UPDATE " + sql_table_staff + " SET currentTicketID = '" + a + "' WHERE UUID = '" + uuid + "';");
                statement.executeUpdate();
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            String a = getStaffTickets(uuid) + ID + ",";
            config.setup();
            config.getPlayers().set("staff." + uuid.toString() + ".currentTicketID", a);
            config.savePlayers();
        }
    }

    /**
     * @param p
     * @return+
     */
    public String getPlayerTickets(UUID uuid) {
        if (sqlEnabled()) {
            try {
                PreparedStatement statement = plugin.getConnection().prepareStatement("SELECT * FROM " + sql_table_players + " WHERE UUID='" + uuid + "';");
                ResultSet rs = statement.executeQuery();

                while (rs.next()) {
                    String s = rs.getString("tickets");
                    rs.close();
                    return s;
                }

                while (!rs.next()) {
                    return "";
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
            return "";
        } else {
            config.setup();
            return config.getPlayers().getString(uuid.toString() + ".tickets");
        }
    }

    /**
     * @param p
     * @return
     */
    public String getStaffTickets(UUID uuid) {
        if (sqlEnabled()) {
            try {
                PreparedStatement statement = plugin.getConnection().prepareStatement("SELECT * FROM " + sql_table_staff + " WHERE UUID = '" + uuid + "';");
                ResultSet rs = statement.executeQuery();

                while (rs.next()) {
                    String s = rs.getString("currentTicketID");
                    if (s == null) {
                        return "";
                    }
                    rs.close();
                    return s;
                }


            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        } else {
            config.setup();
            return config.getPlayers().getString("staff." + uuid.toString() + ".currentTicketID");
        }
    }

    private void staffRemoveTicket(UUID uuid, int ID) {
        if (sqlEnabled()) {
            try {
                String a = getStaffTickets(uuid);
                String b = a.replace(ID + ",", "").replace("'", "''");
                Bukkit.getConsoleSender().sendMessage(Strings.red + a + "  |  " + b);
                PreparedStatement statement = plugin.getConnection().prepareStatement("UPDATE " + sql_table_staff + " SET currentTicketID = '" + b + "' WHERE UUID = '" + uuid + "';");
                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            config.setup();
            String a = getStaffTickets(uuid);
            String b = a.replace(ID + ",", "");
            config.getPlayers().set("staff." + uuid.toString() + ".currentTicketID", b);
            config.savePlayers();
        }
    }

    private void updateTicketStatus(int ID, String status) {
        if (sqlEnabled()) {
            try {
                PreparedStatement statement = plugin.getConnection().prepareStatement("UPDATE " + sql_table_tickets + " SET status = '" + status + "' WHERE ID='" + ID + "';");
                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            config.setup();
            config.getTicket().set(ID + ".status", status);
            config.saveTicket();
            tls.update_status(ID);
        }
    }

    private void updateTicketAssignedBy(UUID uuid, int ID) {
        if (sqlEnabled()) {
            try {
                PreparedStatement statement = plugin.getConnection().prepareStatement("UPDATE " + sql_table_tickets + " SET assignedFrom = '" + uuid + "' WHERE ID='" + ID + "';");
                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            config.setup();
            config.getTicket().set(ID + ".assignedFrom", uuid.toString());
            config.saveTicket();
        }
    }

    private void clearTicketAssignedBy(int ID) {
        if (sqlEnabled()) {
            try {
                PreparedStatement statement = plugin.getConnection().prepareStatement("UPDATE " + sql_table_tickets + " SET assignedFrom = '' WHERE ID= " + ID + ";");
                statement.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            config.setup();
            config.getTicket().set(ID + ".assignedFrom", "");
            config.saveTicket();
        }
    }


    public void updateTicketSubject(int ID, String subject) {
        if (sqlEnabled()) {
            try {
                String a = subject.replace("'", "''");
                PreparedStatement statement = plugin.getConnection().prepareStatement("UPDATE " + sql_table_tickets + " SET subject = '" + a + "' WHERE ID='" + ID + "';");
                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            // DynamicInventoryUpdateService
//            dynamicUpdateService.ticketSubjectUpdate();
            dynamicUpdateService.updateInventory(ID, null);

        } else {
            config.setup();
            config.getTicket().set(ID + ".subject", subject);
            config.saveTicket();

            tls.update_subject(ID);

            // DynamicInventoryUpdateService
//            dynamicUpdateService.ticketSubjectUpdate();
            dynamicUpdateService.updateInventory(ID, null);
        }
    }

    /**
     * Get the location of the player and set it on the ticket data
     *
     * @param p
     * @param ID
     */
    public void updateTicketLocation(Player p, int ID) {
        if (sqlEnabled()) {
            // Get the player location
            String world = p.getWorld().getName();
            int locX = (int) p.getLocation().getX();
            int locY = (int) p.getLocation().getY();
            int locZ = (int) p.getLocation().getZ();

            // put the location into 1 string, save more space for the file
            String a = world + " " + locX + " " + locY + " " + locZ;


            try {
                PreparedStatement statement = plugin.getConnection().prepareStatement("UPDATE " + sql_table_tickets + " SET location = '" + a + "' WHERE ID='" + ID + "';");
                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            // Send a message
            NotificationSerivce.updateLocation(p, ID);

            // Update Inventory
//            dynamicUpdateService.ticketLocationUpdate(ID);
            dynamicUpdateService.updateInventory(ID, null);

        } else {
            config.setup();

            // Get the player location
            String world = p.getWorld().getName();
            int locX = (int) p.getLocation().getX();
            int locY = (int) p.getLocation().getY();
            int locZ = (int) p.getLocation().getZ();

            // put the location into 1 string, save more space for the file
            String a = world + " " + locX + " " + locY + " " + locZ;

            // save
            config.getTicket().set(ID + ".location", a);
            config.saveTicket();

            // Send a message
            NotificationSerivce.updateLocation(p, ID);

            // Update Inventory
//            dynamicUpdateService.ticketLocationUpdate(ID);
            dynamicUpdateService.updateInventory(ID, null);
        }
    }

    /**
     * Get the current time in HH:MM:SS
     *
     * @return
     */
    private String getCurrentTime() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
    }

    public String getStatusFormat(int id) {
        String a = getTicketStatus(id);

        switch (a) {
            case "OPEN":
                return Strings.green + "OPEN";
            case "ASSIGNED":
                return Strings.gold + "ASSIGNED";
            case "SOLVED":
                return Strings.red + "SOLVED";
        }
        return null;
    }

    /**
     * Check if the player has any tickets
     *
     * @param p
     * @return
     */
    public boolean playerHasTickets(Player p) {
        String a = getPlayerTickets(p.getUniqueId());
        if (a == null) {
            return false;
        }
        if (a.isEmpty()) {
            return false;
        }
        return true;
    }

    public boolean staffHasAssignedTickets(Player p) {
        if (sqlEnabled()) {
            try {
                PreparedStatement statement = plugin.getConnection().prepareStatement("SELECT * FROM " + sql_table_staff + " WHERE UUID = ?;");
                statement.setString(1, p.getUniqueId().toString());
                ResultSet rs = statement.executeQuery();

                while (rs.next()) {
                    if (rs.getString("currentTicketID") == null) {
                        rs.close();
                        return false;
                    }
                    if (rs.getString("currentTicketID").isEmpty()) {
                        rs.close();
                        return false;
                    }

                    rs.close();
                    return true;
                }
                rs.close();

            } catch (SQLException e) {
                e.printStackTrace();
            }

        } else {
            config.setup();
            String i = config.getPlayers().getString("staff." + p.getUniqueId() + ".currentTicketID");
            if (i.isEmpty()) {
                return false;
            }
            return true;
        }
        return false;
    }

    public void addSolvedTicket(Player p) {
        if (sqlEnabled()) {
            try {
                int i = staffTotalTicketSolved(p) + 1;
                PreparedStatement statement = plugin.getConnection().prepareStatement("update " + sql_table_staff + " SET ticketSolved = " + i + " WHERE UUID = ?;");
                statement.setString(1, p.getUniqueId().toString());
                statement.execute();
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            config.setup();

            int i = config.getPlayers().getInt("staff." + p.getUniqueId() + ".staffTicketSolved");
            config.getPlayers().set("staff." + p.getUniqueId() + ".staffTicketSolved", i + 1);
            config.savePlayers();
        }

    }


    public OfflinePlayer getTicketAssingedByPlayer(int ID) {
        if (sqlEnabled()) {
            try {
                PreparedStatement statement = plugin.getConnection().prepareStatement("SELECT * FROM " + sql_table_tickets + " WHERE ID = ?");
                statement.setInt(1, ID);
                ResultSet rs = statement.executeQuery();

                while (rs.next()) {
                    String s = rs.getString("assignedFrom");
                    rs.close();
                    return Bukkit.getOfflinePlayer(UUID.fromString(s));
                }
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        } else {
            if (tls.assignedFrom(ID).isEmpty()) {
                return null;
            }
            return Bukkit.getOfflinePlayer(UUID.fromString(tls.assignedFrom(ID)));
        }
    }

    public OfflinePlayer getTicketCreatedByPlayer(int ID) {
        if (sqlEnabled()) {
            try {
                PreparedStatement statement = plugin.getConnection().prepareStatement("SELECT * FROM " + sql_table_tickets + " WHERE ID = ?;");
                statement.setInt(1, ID);
                ResultSet rs = statement.executeQuery();

                while (rs.next()) {
                    String s = rs.getString("createdBy");
                    rs.close();
                    return Bukkit.getOfflinePlayer(UUID.fromString(s));
                }
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        } else {
            if (tls.createdBy(ID).isEmpty()) {
                return null;
            }
            return Bukkit.getOfflinePlayer(UUID.fromString(tls.createdBy(ID)));
        }
    }

    /**
     * Get the ticket create date in format > YYYY/MM/DD HH:MM:SS
     *
     * @param ID
     * @return
     */
    public String getTicketCreatedDate(int ID) {
        if (sqlEnabled()) {
            try {
                PreparedStatement statement = plugin.getConnection().prepareStatement("SELECT * FROM " + sql_table_tickets + " WHERE ID = ?;");
                statement.setInt(1, ID);
                ResultSet rs = statement.executeQuery();

                while (rs.next()) {
                    String s = rs.getString("createdDate");
                    rs.close();
                    return s;
                }
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return "";
        } else {
            return tls.createdDate(ID);
        }
    }

    /**
     * Get the ticket solved date in format > YYYY/MM/DD HH:MM:SS
     *
     * @param ID
     * @return
     */
    public String getTicketSolvedDate(int ID) {
        if (sqlEnabled()) {
            try {
                PreparedStatement statement = plugin.getConnection().prepareStatement("SELECT * FROM " + sql_table_tickets + " WHERE ID = ?;");
                statement.setInt(1, ID);
                ResultSet rs = statement.executeQuery();

                while (rs.next()) {
                    String s = rs.getString("solvedDate");
                    rs.close();
                    return s;
                }
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return "";
        } else {
            return tls.solvedDate(ID);
        }
    }

    /**
     * Get the ticket subject
     *
     * @param ID
     * @return
     */
    public String getTicketSubject(int ID) {
        if (sqlEnabled()) {
            try {
                PreparedStatement statement = plugin.getConnection().prepareStatement("SELECT * FROM " + sql_table_tickets + " WHERE ID = ?;");
                statement.setInt(1, ID);
                ResultSet rs = statement.executeQuery();

                while (rs.next()) {
                    String s = rs.getString("subject");
                    rs.close();
                    return s;
                }
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return "";
        } else {
            return tls.subject(ID);
        }
    }

    /**
     * Get the ticket status
     *
     * @param ID
     * @return
     */
    public String getTicketStatus(int ID) {
        if (sqlEnabled()) {
            try {
                PreparedStatement statement = plugin.getConnection().prepareStatement("SELECT * FROM " + sql_table_tickets + " WHERE ID = ?;");
                statement.setInt(1, ID);
                ResultSet rs = statement.executeQuery();

                while (rs.next()) {
                    String s = rs.getString("status");
                    rs.close();
                    return s;
                }
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return "";
        } else {
            return tls.status(ID);
        }
    }

    /**
     * get the location from the config
     * and it into a string sorted out
     *
     * @param ID
     * @return
     */
    public String getTicketLocationFormat(int ID) {
        if (sqlEnabled()) {
            try {
                PreparedStatement statement = plugin.getConnection().prepareStatement("SELECT * FROM " + sql_table_tickets + " WHERE ID = ?;");
                statement.setInt(1, ID);
                ResultSet rs = statement.executeQuery();

                while (rs.next()) {
                    String a = rs.getString("location");
                    if (a == null) {
                        rs.close();
                        return "None";
                    }
                    rs.close();
                    String[] b = a.split(" ");
                    String a1 = b[1] + "X ";
                    String a2 = b[2] + "Y ";
                    String a3 = b[3] + "Z";
                    return a1 + a2 + a3;
                }
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            config.setup();
            if (!config.getTicket().isSet(ID + ".location")) {
                return "None";
            }
            String[] a = config.getTicket().getString(ID + ".location").split(" ");
            String a1 = a[1] + "X ";
            String a2 = a[2] + "Y ";
            String a3 = a[3] + "Z";
            return a1 + a2 + a3;
        }
        return null;
    }

    public Location getTicketLocation(int ID) {
        if (sqlEnabled()) {
            try {
                PreparedStatement statement = plugin.getConnection().prepareStatement("SELECT * FROM " + sql_table_tickets + " WHERE ID = ?;");
                statement.setInt(1, ID);
                ResultSet rs = statement.executeQuery();
                while (rs.next()) {
                    String a = rs.getString("location");
                    String[] b = a.split(" ");
                    World w = Bukkit.getWorld(b[0]);
                    double x = Double.parseDouble(b[1]);
                    double y = Double.parseDouble(b[2]);
                    double z = Double.parseDouble(b[3]);
                    rs.close();
                    return new Location(w, x, y, z);
                }
                rs.close();

            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            config.setup();

            String[] a = config.getTicket().getString(ID + ".location").split(" ");
            World w = Bukkit.getWorld(a[0]);
            double x = Double.parseDouble(a[1]);
            double y = Double.parseDouble(a[2]);
            double z = Double.parseDouble(a[3]);

            return new Location(w, x, y, z);
        }
        return null;
    }


    public boolean openTicketsAvailable() {
        return !openList.isEmpty();
    }

    public boolean hasTicketID(Player p, int id) {
        if (sqlEnabled()) {
            try {
                PreparedStatement statement = plugin.getConnection().prepareStatement("SELECT * FROM " + sql_table_players + " WHERE UUID = ?;");
                statement.setString(1, p.getUniqueId().toString());
                ResultSet rs = statement.executeQuery();

                while (rs.next()) {
                    String[] a = rs.getString("tickets").split(",");
                    for (String key : a) {
                        if (key.isEmpty()) {
                            break;
                        }
                        int i = Integer.parseInt(key);
                        if (i == id) {
                            rs.close();
                            return true;
                        }
                    }

                }
                rs.close();
                return false;

            } catch (SQLException e) {
                e.printStackTrace();
            }

        } else {
            // Initialize
            config.setup();

            // Check if the player is set in the file
            if (!config.getPlayers().isSet(p.getUniqueId() + ".tickets")) {
                return false;
            }

            // Loop trough the ticket of the player of its match
            String[] a = config.getPlayers().getString(p.getUniqueId() + ".tickets").split(",");
            for (String key : a) {
                if (key.isEmpty()) {
                    break;
                }
                int i = Integer.parseInt(key);
                if (i == id) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    public int staffTotalTicketSolved(Player p) {
        if (sqlEnabled()) {
            try {
                PreparedStatement statement = plugin.getConnection().prepareStatement("SELECT * FROM " + sql_table_staff + " WHERE UUID = ?;");
                statement.setString(1, p.getUniqueId().toString());
                ResultSet rs = statement.executeQuery();
                while (rs.next()) {
                    int i = rs.getInt("ticketSolved");
                    rs.close();
                    return i;
                }
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            config.setup();
            if (!config.getPlayers().isSet("staff." + p.getUniqueId() + ".staffTicketSolved")) {
                return 0;
            }
            return config.getPlayers().getInt("staff." + p.getUniqueId() + ".staffTicketSolved");
        }
        return 0;
    }

    public boolean staffTicketSolved(int ID) {
        return tls.status(ID).equals("SOLVED");
    }

    public int playerTotalTickets(Player p) {
        if (sqlEnabled()) {
            String a = getPlayerTickets(p.getUniqueId());
            int x = 0;


            String[] b = a.split(",");
            for (String key : b) {
                if (key.isEmpty()) {
                    break;
                }
                x++;
            }
            return x;
        } else {
            config.setup();

            if (!config.getPlayers().isSet(p.getUniqueId() + ".tickets")) {
                return 0;
            }

            int count = 0;
            String[] a = config.getPlayers().getString(p.getUniqueId() + ".tickets").split(",");
            for (String key : a) {
                if (key.isEmpty()) {
                    continue;
                }
                count++;
            }
            return count;
        }
    }

    public int staffTotalTickets(Player p) {
        int count = 0;

        if (sqlEnabled()) {
            if (getStaffTickets(p.getUniqueId()).isEmpty()) {
                return 0;
            }

            String[] a = getStaffTickets(p.getUniqueId()).split(",");
            for (String key : a) {
                if (key.isEmpty()) {
                    continue;
                }
                count++;
            }
        } else {
            config.setup();

            if (!config.getPlayers().isSet("staff." + p.getUniqueId() + ".currentTicketID")) {
                return 0;
            }


            String[] a = config.getPlayers().getString("staff." + p.getUniqueId() + ".currentTicketID").split(",");
            for (String key : a) {
                if (key.isEmpty()) {
                    continue;
                }
                count++;
            }
        }
        return count;
    }

    public int getTicketIdAssigned(Player p) {
        config.setup();
        return config.getPlayers().getInt("staff." + p.getUniqueId() + ".currentTicketID");
    }

    public void staffAddPlayer(UUID uuid) {
        if (sqlEnabled()) {
            try {
                PreparedStatement statement = plugin.getConnection().prepareStatement("INSERT INTO " + sql_table_staff + " (UUID , currentTicketID, ticketSolved, rating) VALUES (?,?,?,?)");
                statement.setString(1, uuid.toString());
                statement.setString(2, null);
                statement.setInt(3, 0);
                statement.setString(4, "0#0#0#0#0#");
                statement.executeUpdate();
                statement.close();

            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            config.setup();
            config.getPlayers().set("staff." + uuid + ".currentTicketID", "");
            config.getPlayers().set("staff." + uuid + ".staffTicketSolved", 0);
            config.getPlayers().set("staff." + uuid + ".rating", "0#0#0#0#0#");
            config.savePlayers();
        }
    }

    /**
     * @param uuid Remove the player by uuid
     */
    public void staffRemovePlayer(UUID uuid) {
        config.setup();

        // Re open the assigned tickets
        OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
        for (String key : getStaffTickets(p.getUniqueId()).split(",")) {
            if (key.isEmpty()) {
                break;
            }
            int ID = Integer.parseInt(key);

            // Change the ticket status OPEN
            config.getTicket().set(ID + ".status", "OPEN");
            config.saveTicket();

            // Remove the assignedFrom ticket from the ticket
            config.getTicket().set(ID + ".assignedFrom", "");
            config.saveTicket();

            // Update the status CACHE
            tls.update_status(ID);
            tls.update_assignedFrom(ID);

            // Remove the id from the ASSIGNEDLIST
            Object a = ID;
            assignedList.remove(a);

            // Add the id to the OPENLIST
            openList.add(ID);
        }

        // Remove the player from the staff
        config.getPlayers().set("staff." + uuid, null);
        config.savePlayers();
    }

    /**
     * @param uuid The player uuid
     * @return if the player is a staff it will return true
     */
    public boolean isStaff(UUID uuid) {
        if (sqlEnabled()) {
            try {
                PreparedStatement statement = plugin.getConnection().prepareStatement("SELECT * FROM " + sql_table_staff + " WHERE UUID = ?;");
                statement.setString(1, uuid.toString());

                ResultSet rs = statement.executeQuery();

                while (!rs.next()) {
                    rs.close();
                    return false;
                }
                rs.close();
                return true;

            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        } else {
            config.setup();
            return config.getPlayers().isSet("staff." + uuid);
        }
    }

    public void saveRatingToTicket(int rating, int ID) {
        if (sqlEnabled()) {
            try {
                PreparedStatement statement = plugin.getConnection().prepareStatement("UPDATE " + sql_table_tickets + " SET rating = " + rating + " WHERE ID = ?;");
                statement.setInt(1, ID);
                statement.execute();
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            config.setup();
            config.getTicketSolved().set(ID + ".rating", rating);
            config.saveTicketSolved();
            tls.update_rating(ID);
        }
    }

    public int getTicketRating(int ID) {
        if (sqlEnabled()) {
            try {
                PreparedStatement statement = plugin.getConnection().prepareStatement("SELECT * FROM tickets_rts WHERE ID = ?;");
                statement.setInt(1, ID);
                ResultSet rs = statement.executeQuery();

                while (rs.next()) {
                    int i = rs.getInt("rating");
                    rs.close();
                    return i;
                }
                rs.close();
                return 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            config.setup();
            return config.getTicketSolved().getInt(ID + ".rating");
        }
        return 0;
    }

    public void replyToTicket(int ID, Player p, String message) {
        if (sqlEnabled()) {
            // Initialize
            int i = 1;
            int messageNumber = 0;

            // If the file is empty it will start at 1 TODO: i don't know sure if we need this

            // put all the ticketID to a List
            try {
                PreparedStatement statement = plugin.getConnection().prepareStatement("SELECT * FROM " + sql_table_messages + " WHERE ID = ?;");
                statement.setInt(1, ID);

                ResultSet rs = statement.executeQuery();
                while (rs.next()) {
                    ticketID.add(rs.getInt("messageNumber"));
                }
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            // sort out the numbers from low -> high
            Collections.sort(ticketID);

            // get the next following number
            for (Integer i2 : ticketID) {
                if (i2 == i) {
                    i++;
                }
            }
            messageNumber = i;

            // Save the ticket
            try {
                PreparedStatement statement = plugin.getConnection().prepareStatement("INSERT INTO " + sql_table_messages + " (ID, messageNumber, sender, message, time) VALUES (?,?,?,?,?);");
                statement.setInt(1, ID);
                statement.setInt(2, messageNumber);
                statement.setString(3, p.getName());
                statement.setString(4, message);
                statement.setString(5, getCurrentTime());
                statement.execute();
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            // clear the list for preventing weird error or duplication bugs
            ticketID.clear();

        } else {
            // Initialize
            config.setup();
            int messageNumber = 0;
            int i = 1;
            String a = p.getName() + ":" + message;

            // If the file is empty it will start at 1
            if (config.getTicket().getKeys(false).size() == 0) {
                return;
            }

            // put all the ticketID to a List
            for (String key : config.getTicket().getConfigurationSection(ID + ".messages").getKeys(false)) {
                ticketID.add(Integer.valueOf(key));
            }

            // sort out the numbers from low -> high
            Collections.sort(ticketID);

            // get the next following number
            for (Integer i2 : ticketID) {
                if (i2 == i) {
                    i++;
                }
            }
            messageNumber = i;

            // Save the ticket
            config.getTicket().set(ID + ".messages." + messageNumber + ".message", a);
            config.getTicket().set(ID + ".messages." + messageNumber + ".replyDate", getCurrentTime());
            config.saveTicket();

            // clear the list for preventing weird error or duplication bugs
            ticketID.clear();
        }
    }


    public String ticketAssingedFrom(int id) {
        config.setup();
        String a = config.getTicket().getString(id + ".assignedFrom");

        // Check if its empty
        if (a.isEmpty()) {
            return "";
        }
        Player p = Bukkit.getPlayer(UUID.fromString(a));

        return p.getName();
    }

    public void configVersionUpdate() {
        double currentVersion = 1.2;
        double configVersion = plugin.getConfig().getDouble("config-version");

        if (!plugin.getConfig().isSet("config-version")) {
            File file = new File(plugin.getDataFolder(), "config.yml");
            file.delete();

            plugin.getConfig().options().copyDefaults(true);
            plugin.saveDefaultConfig();
            plugin.reloadConfig();
            Bukkit.getConsoleSender().sendMessage(Strings.logName + "Created a new config file!");
            return;
        }

        if (configVersion != currentVersion) {
            File file = new File(plugin.getDataFolder(), "config.yml");
            file.delete();

            plugin.getConfig().options().copyDefaults(true);
            plugin.saveDefaultConfig();
            plugin.reloadConfig();
            Bukkit.getConsoleSender().sendMessage(Strings.logName + "Created a new config file!");
            return;
        }
        plugin.reloadConfig();
        plugin.saveConfig();
    }

    public int pageCalcMin(int page) {
        int pageSlots = page * 45;
        return pageSlots - 44;
    }

    public int pageCalcMax(int page) {
        return page * 45;
    }

//    public void createTicketDEV(Player p, String message, int amountOfTickets) {
//
//        for (int item = 0; item < amountOfTickets; item++) {
//            // Initialize
//            config.setup();
//            int ID = nextID();
//
//            // Get the timestamp of time
//            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
//            LocalDateTime now = LocalDateTime.now();
//
//            if (sqlEnabled()) {
//                try {
//                    PreparedStatement statement = plugin.getConnection().prepareStatement("INSERT INTO " + sql_table_tickets + " (ID, createdBy, createdDate, subject, status, location, assignedFrom, solvedDate, rating) VALUES (?,?,?,?,?,?,?,?,?)");
//                    statement.setInt(1, ID);
//                    statement.setString(2, p.getUniqueId().toString());
//                    statement.setString(3, dtf.format(now));
//                    statement.setString(4, null);
//                    statement.setString(5, "OPEN");
//                    statement.setString(6, null);
//                    statement.setString(7, null);
//                    statement.setString(8, null);
//                    statement.setString(9, null);
//                    statement.executeUpdate();
//                    statement.close();
//
//                } catch (SQLException e) {
//                    e.printStackTrace();
//                }
//
//                // Set the message
//                setMessage(ID, p, message);
//
//                // Cache the ticket ID
//                createdTicket_id.put(p, ID);
//
//                // add the player to the table
//                if (!sql.playerHasAccount(p.getUniqueId())) {
//                    sql.addPlayer(p.getUniqueId());
//                }
//
//
//                // add the ticket to the player
//                playerAddTicket(p.getUniqueId(), ID);
//
//                // message's
//                NotificationSerivce.createTicket(p, ID);
//
//                // add the ticket to hashmap
//                tls.addTicket(ID);
//                openList.add(ID);
//
//                // DynamicInventoryUpdateService
//                dynamicUpdateService.ticketCreateUpdate();
//            } else {
//                // Set all the data
//                config.getTicket().set(ID + ".createdBy", p.getUniqueId().toString());
//                config.getTicket().set(ID + ".createdDate", dtf.format(now));
//                config.getTicket().set(ID + ".subject", "NONE");
//                config.getTicket().set(ID + ".status", "OPEN");
//                config.getTicket().set(ID + ".assignedFrom", "");
//                config.saveTicket();
//                setMessage(ID, p, message);
//
//                // Cache the ticket ID
//                createdTicket_id.put(p, ID);
//
//                // Add the ticket ID to the player
//                playerAddTicket(p.getUniqueId(), ID);
//
//                // message's
//                NotificationSerivce.createTicket(p, ID);
//
//                // add the ticket to hashmap
//                tls.addTicket(ID);
//                openList.add(ID);
//
//                // DynamicInventoryUpdateService
//                dynamicUpdateService.ticketCreateUpdate();
//            }
//        }
//    }

    /**
     * if a staff want to cancel a ticket
     *
     * @param ID
     */
    public void cancelTicket(Player p, int ID) {
        if (sqlEnabled()) {

            // remove the assignedfrom and change the status to open
            clearTicketAssignedBy(ID);
            updateTicketStatus(ID, "OPEN");

            // remove the ticket id from the staff
            staffRemoveTicket(p.getUniqueId(), ID);

            // Update The list
            Object o = ID;
            assignedList.remove(o);
            openList.add(ID);

            // Update the inventory
//            dynamicUpdateService.ticketCancelUpdate(ID);
            dynamicUpdateService.updateInventory(ID, getTicketCreatedByPlayer(ID).getUniqueId());

        } else {
            config.setup();

            // remove the assignedfrom and change the status to open
            clearTicketAssignedBy(ID);
            updateTicketStatus(ID, "OPEN");

            // remove the ticket id from the staff
            String a = getStaffTickets(p.getUniqueId()).replace(ID + ",", "");
            config.getPlayers().set("staff." + p.getUniqueId() + ".currentTicketID", a);
            config.savePlayers();


            // Update the cache
            tls.update_status(ID);
            tls.update_assignedFrom(ID);

            // Update The list
            Object o = ID;
            assignedList.remove(o);
            openList.add(ID);


            // Update the inventory
//            dynamicUpdateService.ticketCancelUpdate(ID);
            dynamicUpdateService.updateInventory(ID, getTicketCreatedByPlayer(ID).getUniqueId());
        }
    }

    public boolean sqlEnabled() {
        return plugin.getConfig().getBoolean("MySQL.enable-MySQL");
    }

    public void removeTicket(int ID) {
        if (sqlEnabled()) {
            try {
                PreparedStatement statement = plugin.getConnection().prepareStatement("DELETE FROM " + sql_table_tickets + " WHERE ID = ?;");
                statement.setInt(1, ID);
                statement.executeUpdate();
                statement.close();

                PreparedStatement s2 = plugin.getConnection().prepareStatement("DELETE FROM  " + sql_table_messages + " WHERE ID = ?;");
                s2.setInt(1, ID);
                s2.executeUpdate();
                s2.close();

            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            config.setup();
            config.getTicket().set(ID + "", null);
            config.saveTicket();
        }
        Object o = ID;
        assignedList.remove(o);
        openList.remove(o);
        solvedList.remove(o);
    }

    public void updateStaffRating(Player p, int star) {
        // Initialize
        String[] a = getStaffRating(p.getUniqueId()).split("#");
        List<String> list = new ArrayList<>();

        if (sqlEnabled()) {
            // put all the ticket id from the staff into a list
            for (String key : a) {
                String b = key.replace("#", "");
                list.add(b);
            }

            // Some magic stuff,  i don't remember what it do but i did write it
            int i1 = Integer.parseInt(list.get(star));
            int i2 = i1 + 1;
            String c = String.valueOf(i2);
            list.set(star, c);

            // Some magic stuff 2
            String Final = "";
            for (String key : list) {
                Final = Final + key + "#";
            }

            // Clear the list for preventing weird error/bugs
            list.clear();

            // Save the new rating
            try {
                PreparedStatement statement = plugin.getConnection().prepareStatement("UPDATE " + sql_table_staff + " SET rating = '" + Final + "' WHERE UUID = ?;");
                statement.setString(1, p.getUniqueId().toString());
                statement.execute();
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }

        } else {
            // Initialize
            config.setup();

            // put all the ticket id from the staff into a list
            for (String key : a) {
                String b = key.replace("#", "");
                list.add(b);
            }

            // Some magic stuff,  i don't remember what it do but i did write it
            int i1 = Integer.parseInt(list.get(star));
            int i2 = i1 + 1;
            String c = String.valueOf(i2);
            list.set(star, c);

            // Some magic stuff 2
            String Final = "";
            for (String key : list) {
                Final = Final + key + "#";
            }

            // Clear the list for preventing weird error/bugs
            list.clear();

            // Save the new rating
            config.getPlayers().set("staff." + p.getUniqueId() + ".rating", Final);
            config.savePlayers();

        }
    }

    private String getStaffRating(UUID uuid) {
        if (sqlEnabled()) {
            try {
                PreparedStatement statement = plugin.getConnection().prepareStatement("SELECT * FROM " + sql_table_staff + " WHERE UUID = ?;");
                statement.setString(1, uuid.toString());
                ResultSet rs = statement.executeQuery();

                while (rs.next()) {
                    String a = rs.getString("rating");
                    rs.close();
                    return a;

                }
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            config.setup();
            return config.getPlayers().getString("staff." + uuid + ".rating");
        }
        return null;
    }

    public void setSolvedDate(int id) {

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now().plusDays(1);

        String[] a = dtf.format(now).split(":");
        int i = PurgeService.getNear15Minute(Integer.parseInt(a[1]));

        String b = a[0] + ":" + i;

        if (sqlEnabled()) {
            try {
                PreparedStatement statement = plugin.getConnection().prepareStatement("update " + sql_table_tickets + " SET solvedDate = '" + b + "' WHERE ID = ?;");
                statement.setInt(1, id);
                statement.execute();
                statement.close();

            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            config.setup();
            config.getTicket().set(id + ".solvedDate", b);
            config.saveTicket();
        }
    }

    public ArrayList<String> staff() {
        ArrayList<String> a = new ArrayList<>();
        if (sqlEnabled()) {
            try {
                PreparedStatement statement = plugin.getConnection().prepareStatement("SELECT * FROM staff_rts");
                ResultSet rs = statement.executeQuery();
                while (rs.next()) {
                    a.add(rs.getString("UUID"));
                }
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return a;
        } else {
            config.setup();

            if (config.getPlayers().getConfigurationSection("staff") == null) return a;
            for (String key : config.getPlayers().getConfigurationSection("staff").getKeys(false)) {
                a.add(key);
            }
            return a;
        }
    }


    public boolean ticketExist(int ID) {
        if (sqlEnabled()) {
            try {
                PreparedStatement statement = plugin.getConnection().prepareStatement("SELECT * FROM tickets_rts WHERE ID = ?;");
                statement.setInt(1, ID);
                ResultSet rs = statement.executeQuery();
                while (rs.next()) {
                    rs.close();
                    return true;
                }

                rs.close();
                return false;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        } else {
            config.setup();
            return config.getTicket().isSet(ID+"");
        }
    }

    public String getTicketMessage(int ID, int messageNumber) {
        if (sqlEnabled()) {
            try {
                PreparedStatement statement = plugin.getConnection().prepareStatement("SELECT * FROM messages_rts WHERE ID = ? AND messageNumber = ?;");
                statement.setInt(1, ID);
                statement.setInt(2, messageNumber);
                statement.execute();

                ResultSet rs = statement.executeQuery();
                while (rs.next()) {
                    String a = rs.getString("message");
                    rs.close();
                    return a;
                }
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return "";
        } else {
            config.setup();
            String[] message = config.getTicket().getString(ID + ".messages." + messageNumber + ".message").split(":");
            return message[1];
        }
    }

    public String getTicketMessageReplydate(int ID, int messageNumber) {
        if (sqlEnabled()) {
            try {
                PreparedStatement statement = plugin.getConnection().prepareStatement("SELECT * FROM messages_rts WHERE ID = ? AND messageNumber = ?;");
                statement.setInt(1, ID);
                statement.setInt(2, messageNumber);
                statement.execute();

                ResultSet rs = statement.executeQuery();
                while (rs.next()) {
                    String a = rs.getString("time");
                    rs.close();
                    return a;
                }
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return "";
        } else {
            config.setup();
            return config.getTicket().getString(ID + ".messages." + messageNumber + ".replyDate");
        }
    }

    public String getTicketMessageSender(int ID, int messageNumber) {
        if (sqlEnabled()) {
            try {
                PreparedStatement statement = plugin.getConnection().prepareStatement("SELECT * FROM messages_rts WHERE ID = ? AND messageNumber = ?;");
                statement.setInt(1, ID);
                statement.setInt(2, messageNumber);
                statement.execute();

                ResultSet rs = statement.executeQuery();
                while (rs.next()) {
                    String a = rs.getString("sender");
                    rs.close();
                    return a;
                }
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return "";
        } else {
            config.setup();
            String[] message = config.getTicket().getString(ID + ".messages." + messageNumber + ".message").split(":");
            return message[0];
        }
    }

}
