package com.twanl.realtimesupport.service;

import com.twanl.realtimesupport.lib.Lib;
import com.twanl.realtimesupport.menu.*;
import com.twanl.realtimesupport.util.ConfigManager;
import com.twanl.realtimesupport.util.Strings;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

/**
 * Author: Twan Luttik
 * Date: 10/31/2018
 */
public class DynamicInventoryUpdatesService {

    private ConfigManager config = new ConfigManager();

    /**
     * This will refresh the inventory depending on what situations the player is
     * @param ID
     * @param member
     */
    public void updateInventory(int ID, UUID member) {
        // Initialize
        config.setup();
        Lib lib = new Lib();

        // Initialize all the menu's
        Tickets_UI tickets_ui = new Tickets_UI();
        TicketStatus_UI ticketStatus_ui = new TicketStatus_UI();
        TicketOptions_UI ticketOptions_ui = new TicketOptions_UI();
        AssignedTickets_UI assignedTickets_ui = new AssignedTickets_UI();
        ReplyMessages_UI replyMessages_ui = new ReplyMessages_UI();


        for (String key : lib.staff()) {
            OfflinePlayer staff = Bukkit.getOfflinePlayer(UUID.fromString(key));
            if (staff.isOnline()) {
                if (staff.getPlayer().getOpenInventory().getTopInventory().getType().getDefaultTitle().equals(Strings.DgreenB + "Open " + Strings.DgrayB + "Tickets")) {
                    tickets_ui.openTickets_UI(staff.getPlayer());
                    continue;
                }

                if (staff.getPlayer().getOpenInventory().getTopInventory().getType().getDefaultTitle().equals(Strings.goldB + "Assigned " + Strings.DgrayB + "Tickets")) {
                    tickets_ui.assignedTicket_UI(staff.getPlayer());
                    continue;
                }

                if (staff.getPlayer().getOpenInventory().getTopInventory().getType().getDefaultTitle().equals(Strings.redB + "Solved " + Strings.DgrayB + "Tickets")) {
                    tickets_ui.solvedTicket_UI(staff.getPlayer());
                    continue;
                }

                if (staff.getPlayer().getOpenInventory().getTopInventory().getType().getDefaultTitle().equals(Strings.DgrayB + "Ticket " + Strings.Dgray + "#" + Strings.DgrayB + ID + " | Options")) {


                    if (lib.getTicketStatus(ID).equals("SOLVED")) {
                        tickets_ui.openTickets_UI(staff.getPlayer());
                        continue;
                    }

                    if (!lib.ticketExist(ID)) {
                        if (TicketOptions_UI.backButtonID == 1) {
                            if (lib.staffHasAssignedTickets(staff.getPlayer())) {
                                assignedTickets_ui.assignedTickets(staff.getPlayer());
                                continue;
                            }
                        } else {
                            tickets_ui.openTickets_UI(staff.getPlayer());
                            continue;
                        }
                    }
                    ticketOptions_ui.ticketOptions_UI(staff.getPlayer(), ID);
                    continue;
                }

                if (staff.getPlayer().getOpenInventory().getTopInventory().getType().getDefaultTitle().equals(Strings.DgrayB + "Ticket " + Strings.Dgray + "#" + Strings.DgrayB + ID)) {
                    replyMessages_ui.replyMessages_UI(staff.getPlayer(), ID);
                }
            }
        }

        // This is for the regular users
        if (member != null) {
            OfflinePlayer p = Bukkit.getOfflinePlayer(member);

            if (p.isOnline()) {
                if (p.getPlayer().getOpenInventory().getTopInventory().getType().getDefaultTitle().equals(Strings.DgrayB + "Ticket " + Strings.Dgray + "#" + Strings.DgrayB + ID)) {

                    if (!lib.ticketExist(ID)) {
                        if (lib.playerHasTickets(p.getPlayer())) {
                            ticketStatus_ui.chooseTicket_UI(p.getPlayer());
                            return;
                        }
                        p.getPlayer().closeInventory();
                        return;
                    }
                    ticketStatus_ui.ticketStatus_UI(p.getPlayer(), ID);
                    return;
                }

                if (p.getPlayer().getOpenInventory().getTopInventory().getType().getDefaultTitle().equals(Strings.DgrayB + "Choose Ticket")) {
                    ticketStatus_ui.chooseTicket_UI(p.getPlayer());
                    return;
                }

                if (p.getPlayer().getOpenInventory().getTopInventory().getType().getDefaultTitle().equals(Strings.DgrayB + "Ticket " + Strings.Dgray + "#" + Strings.DgrayB + ID)) {
                    replyMessages_ui.replyMessages_UI(p.getPlayer(), ID);
                }
            }
        }
    }


}
