package com.twanl.realtimesupport.lib;

import com.twanl.realtimesupport.RealTimeSupport;
import com.twanl.realtimesupport.util.Strings;
import org.bukkit.Bukkit;

import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Author: Twan Luttik
 * Date: 11/4/2018
 */
public class SQLlib {

    private RealTimeSupport plugin = RealTimeSupport.getPlugin(RealTimeSupport.class);

    public void createTable() {
        Lib lib = new Lib();
        try {
            if (!tableExist(lib.sql_table_tickets)) {
                PreparedStatement s = plugin.getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS " + lib.sql_table_tickets + " (ID INT, createdBy varchar(255), createdDate varchar(255), subject varchar(255), status varchar(255), location varchar(255), assignedFrom varchar(255), solvedDate varchar(255), rating int);");
                s.executeUpdate();
                Bukkit.getConsoleSender().sendMessage(Strings.logName + "created a table: " + Strings.green + lib.sql_table_tickets);
            }

            if (!tableExist(lib.sql_table_staff)) {
                PreparedStatement s = plugin.getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS " + lib.sql_table_staff + " (UUID varchar(255), currentTicketID varchar(255), ticketSolved int, rating varchar(255));");
                s.executeUpdate();
                Bukkit.getConsoleSender().sendMessage(Strings.logName + "created a table: " + Strings.green + lib.sql_table_staff);
            }

            if (!tableExist(lib.sql_table_players)) {
                PreparedStatement s = plugin.getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS " + lib.sql_table_players + " (UUID varchar(255), tickets varchar(255));");
                s.executeUpdate();
                Bukkit.getConsoleSender().sendMessage(Strings.logName + "created a table: " + Strings.green + lib.sql_table_players);
            }

            if (!tableExist(lib.sql_table_messages)) {
                PreparedStatement s = plugin.getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS " + lib.sql_table_messages + " (ID int, messageNumber int, sender varchar(255), message varchar(255), time varchar(255));");
                s.executeUpdate();
                Bukkit.getConsoleSender().sendMessage(Strings.logName + "created a table: " + Strings.green + lib.sql_table_messages);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Check if table exist
    private boolean tableExist(String tableName) {
        try {
            DatabaseMetaData dbm = plugin.getConnection().getMetaData();
            ResultSet tables = dbm.getTables(null, null, tableName, null);
            if (tables.next()) {
                return true;
            } else {
                return false;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void addPlayer(UUID uuid) {
        Lib lib = new Lib();
        try {
            PreparedStatement statement = plugin.getConnection().prepareStatement("INSERT INTO " + lib.sql_table_players + " (UUID , tickets) VALUES (?,?)");
            statement.setString(1, uuid.toString());
            statement.setString(2, "");
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean playerHasAccount(UUID uuid) {
        Lib lib = new Lib();
        try {
            PreparedStatement statement = plugin.getConnection().prepareStatement("SELECT * FROM " + lib.sql_table_players + " WHERE UUID=?");
            statement.setString(1, uuid.toString());
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                return true;
            }
            return false;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

}
