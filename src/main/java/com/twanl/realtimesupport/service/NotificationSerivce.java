package com.twanl.realtimesupport.service;

import com.twanl.realtimesupport.RealTimeSupport;
import com.twanl.realtimesupport.lib.Lib;
import com.twanl.realtimesupport.util.Strings;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public class NotificationSerivce {

    private static RealTimeSupport plugin = RealTimeSupport.getPlugin(RealTimeSupport.class);
    private static Lib lib = new Lib();

    public static void createTicket(Player p, int ID) {
        p.sendMessage(Strings.prefix() + Strings.gray + "You created a new Ticket " + Strings.blueI + "(" + ID + "#" + ")" + Strings.gray + ", The " + Strings.green
                + "staff " + Strings.gray + "will be notified!");

        for (String s : lib.staff()) {
            OfflinePlayer staffPlayer = Bukkit.getOfflinePlayer(UUID.fromString(s));

            if (staffPlayer.isOnline()) {
                plugin.nms.sendClickableMessage(staffPlayer.getPlayer(), Strings.prefix() + Strings.green + p.getName() + Strings.gray + " has created a Ticket. ", Strings.green + "[Open]", "rs tickets");
            }
        }
    }

    public static void assignTicket(Player p, int ID) {

        OfflinePlayer p1 = lib.getTicketCreatedByPlayer(ID);
        if (p1.isOnline()) {
            p1.getPlayer().sendMessage(Strings.prefix() + Strings.green + p.getName() + " assigned your ticket " + Strings.blue + "(#" + ID + ")");
        }
    }

    public static void closeTicket(Player p, int ID) {

        p.sendMessage(Strings.prefix() + Strings.gray + "You succsessfully closed your ticket!");

        if (lib.getTicketStatus(ID).contains("ASSIGNED")) {
            OfflinePlayer staffPlayer = lib.getTicketAssingedByPlayer(ID);

            if (staffPlayer.isOnline()) {
                staffPlayer.getPlayer().sendMessage(Strings.prefix() + Strings.green +  p.getName() + Strings.gray + " closed the ticket " + Strings.blue + "(#" + ID + ")");
            }
        }
    }

    public static void closeTicketConfirmation(Player p, int ID) {
        plugin.nms.sendClickableMessage(p, Strings.prefix() + Strings.gray + "Is your Ticket solved click here to close " + Strings.gray + "your Ticket.", Strings.green + " [CLOSE]", "rs close " + ID);
    }

    public static void updateLocation(Player p, int ID) {
        p.sendMessage(Strings.prefix() + Strings.green + "Location set!");

        if (lib.getTicketStatus(ID).equals("ASSIGNED")) {
            OfflinePlayer staff = lib.getTicketAssingedByPlayer(ID);
            if (staff.isOnline()) {
                staff.getPlayer().sendMessage(Strings.prefix() +Strings.green + p.getName() + Strings.gray + " updated their ticket "
                        + Strings.blue + "(#" + ID + ")" + Strings.gray + "location.");
            }
        }
    }

    public static void updateSubject(Player p, int ID) {
        p.sendMessage(Strings.prefix() + Strings.green + "Subject set!");

        if (lib.getTicketStatus(ID).equals("ASSIGNED")) {
            OfflinePlayer staff = lib.getTicketAssingedByPlayer(ID);
            if (staff.isOnline()) {
                staff.getPlayer().sendMessage(Strings.prefix() +Strings.green + p.getName() + Strings.gray + " updated their ticket "
                        + Strings.blue + "(#" + ID + ")" + Strings.gray + "subject.");
            }
        }
    }


    public static void rateStaff(Player pTicketCeator, UUID uuid, int ID, int rating) {
        OfflinePlayer pStaff = Bukkit.getOfflinePlayer(uuid);

        pTicketCeator.sendMessage(Strings.prefix() + Strings.gray + "Successfully sended the rating.");

        if (pStaff.isOnline()) {
            pStaff.getPlayer().sendMessage(Strings.prefix() + Strings.green + pTicketCeator.getName() + Strings.gray + " Successfully rated ticket " + Strings.blue
                    + "(#" + ID + ")" + Strings.gray + " with " + Strings.green + rating + Strings.gray + " stars.");
        }
    }

    //TODO: clean-up
    public static void replyMessage(Player pSender, UUID uuid, int ID) {
        OfflinePlayer pReceiver = Bukkit.getOfflinePlayer(uuid);

        pSender.sendMessage("message sended");

        if (pReceiver.isOnline()) {
            pReceiver.getPlayer().sendMessage(Strings.prefix() + pSender.getName() + " has replied");
        }

    }


}
