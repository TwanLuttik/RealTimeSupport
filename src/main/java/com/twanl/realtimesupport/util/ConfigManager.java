package com.twanl.realtimesupport.util;

import com.twanl.realtimesupport.RealTimeSupport;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

/**
 * Author: Twan Luttik
 * Date: 10/3/2018
 */

public class ConfigManager {

    private RealTimeSupport plugin = RealTimeSupport.getPlugin(RealTimeSupport.class);

    // Files & Config Files
    private static FileConfiguration ticketC;
    private static File ticketF;

    private static FileConfiguration playersC;
    private static File playerF;

    private static FileConfiguration ticketSolvedC;
    private static File ticketSolvedF;


    public void setup() {
        ticketF = new File(plugin.getDataFolder(), "tickets.yml");
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdir();
        }
        if (!ticketF.exists()) {
            try {
                ticketF.createNewFile();
            } catch (IOException ignored) { }
        }
        ticketC = YamlConfiguration.loadConfiguration(ticketF);

        playerF = new File(plugin.getDataFolder(), "players.yml");
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdir();
        }
        if (!playerF.exists()) {
            try {
                playerF.createNewFile();
            } catch (IOException ignored) { }
        }
        playersC = YamlConfiguration.loadConfiguration(playerF);

        ticketSolvedF = new File(plugin.getDataFolder(), "TicketSolved.yml");
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdir();
        }
        if (!ticketSolvedF.exists()) {
            try {
                ticketSolvedF.createNewFile();
            } catch (IOException ignored) { }
        }
        ticketSolvedC = YamlConfiguration.loadConfiguration(ticketSolvedF);
    }

    public FileConfiguration getTicket() {
        return ticketC;
    }

    public FileConfiguration getPlayers() {
        return playersC;
    }

    public FileConfiguration getTicketSolved() {
        return ticketSolvedC;
    }

    public void saveTicket() {
        ticketF = new File(plugin.getDataFolder(), "tickets.yml");
        try {
            ticketC.save(ticketF);
        } catch (IOException ignored) { }
    }

    public void savePlayers() {
        playerF = new File(plugin.getDataFolder(), "players.yml");
        try {
            playersC.save(playerF);
        } catch (IOException ignored) { }
    }

    public void saveTicketSolved() {
        ticketSolvedF = new File(plugin.getDataFolder(), "TicketSolved.yml");
        try {
            ticketSolvedC.save(ticketSolvedF);
        } catch (IOException ignored) { }
    }

    public void reloadTicket() {
        ticketC = YamlConfiguration.loadConfiguration(ticketF);
    }

    public void reloadPlayers() {
        playersC = YamlConfiguration.loadConfiguration(playerF);
    }

    public void reloadTicketSolved() {
        ticketSolvedC = YamlConfiguration.loadConfiguration(ticketSolvedF);
    }

}