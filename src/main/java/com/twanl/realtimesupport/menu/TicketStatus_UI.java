package com.twanl.realtimesupport.menu;

import com.twanl.realtimesupport.RealTimeSupport;
import com.twanl.realtimesupport.lib.Lib;
import com.twanl.realtimesupport.util.ConfigManager;
import com.twanl.realtimesupport.util.Strings;
import com.twanl.realtimesupport.util.XMaterial;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static com.twanl.realtimesupport.menu.ReplyMessages_UI.aa;

/**
 * Author: Twan Luttik
 * Date: 10/4/2018
 */

public class TicketStatus_UI implements Listener {

    private RealTimeSupport plugin = RealTimeSupport.getPlugin(RealTimeSupport.class);
    private ConfigManager config = new ConfigManager();
    private invAPI inv = new invAPI();
    private Lib lib = new Lib();

    @EventHandler
    public void InvenClick(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        Inventory open = e.getInventory();
        ItemStack item = e.getCurrentItem();
        String getTitle = e.getView().getTitle();


        if (open == null) {
            return;
        }

        if (lib.isStaff(p.getUniqueId())) {
            return;
        }

        config.setup();


        if (lib.playerHasTickets(p)) {
            String[] userTickets = lib.getPlayerTickets(p.getUniqueId()).split(",");
            for (String key : userTickets) {
                int ID = Integer.parseInt(key);

                if (getTitle.equals(Strings.DgrayB + "Ticket " + Strings.Dgray + "#" + Strings.DgrayB + ID)) {
                    e.setCancelled(true);
                    if (item == null || !item.hasItemMeta()) {
                        return;
                    }

                    if (item.getItemMeta().getDisplayName().equals(Strings.gray + "Mark Ticket solved")) {
                        lib.closeTicket(p, ID);
                        return;
                    }

                    if (item.getItemMeta().getDisplayName().equals(Strings.gray + "Message's")) {
                        aa.put(p, 1);
                        ReplyMessages_UI rm = new ReplyMessages_UI();
                        rm.replyMessages_UI(p, ID);
                        return;
                    }

                    if (item.getItemMeta().getDisplayName().equals(Strings.gray + "Back")) {
                        chooseTicket_UI(p);
                        return;
                    }

                    if (item.getItemMeta().getDisplayName().equals(Strings.gray + "Set location")) {
                        lib.updateTicketLocation(p, ID);
                        ticketStatus_UI(p, ID);
                        return;
                    }

                    if (item.getItemMeta().getDisplayName().equals(Strings.gray + "Change Subject")) {
                        CreateTicket_UI ct = new CreateTicket_UI();
                        Lib.createdTicket_id.put(p, ID);
                        ct.subject_UI(p);
                        return;
                    }

                    if (item.getItemMeta().getDisplayName().equals(Strings.gray + "Delete Ticket")) {
                        lib.deleteTicket(ID);
                    }
                }
            }
        }


        if (getTitle.equals(Strings.DgrayB + "Choose Ticket")) {
            e.setCancelled(true);
            if (item == null || !item.hasItemMeta()) {
                return;
            }

            ticketStatusPAGE.putIfAbsent(p, 1);

            if (item.getItemMeta().getDisplayName().equals(Strings.green + "Next page")) {
                int a = ticketStatusPAGE.get(p) + 1;
                ticketStatusPAGE.put(p, a);
                chooseTicket_UI(p);
                return;
            }

            if (item.getItemMeta().getDisplayName().equals(Strings.green + "Previous page")) {
                int a = ticketStatusPAGE.get(p) - 1;
                ticketStatusPAGE.put(p, a);
                chooseTicket_UI(p);
                return;
            }

            String[] userTickets = lib.getPlayerTickets(p.getUniqueId()).split(",");
            for (String key : userTickets) {
                if (item.getItemMeta().getDisplayName().equals(Strings.greenU + "Ticket #" + key)) {
                    ticketStatus_UI(p, Integer.parseInt(key));
                }

            }


        }
    }

    private static HashMap<Player, Integer> ticketStatusPAGE = new HashMap<>();

    public void chooseTicket_UI(Player p) {
        // Initiliaze
        config.setup();
        Inventory i = plugin.getServer().createInventory(null, invSize(p), Strings.DgrayB + "Choose Ticket");
        int slot = 0;
        ArrayList<String> lore = new ArrayList<>();

        // put something if the HashMap is empty, prevent the errors
        ticketStatusPAGE.putIfAbsent(p, 1);
        int pageNumber = ticketStatusPAGE.get(p);

        // Put all the ticket ID into a list
        List<Integer> userCreatedTickets = new ArrayList<>();
        for (String key : lib.getPlayerTickets(p.getUniqueId()).split(",")) {
            if (key.isEmpty()) {
                break;
            }
            userCreatedTickets.add(Integer.valueOf(key));
        }
        Collections.sort(userCreatedTickets);


        for (int item = 0; item < userCreatedTickets.size(); item++) {
            if (item >= lib.pageCalcMin(pageNumber) - 1 && item <= lib.pageCalcMax(pageNumber) - 1) {
                int ID = userCreatedTickets.get(item);

                lore.add(Strings.Dgray + "Subject: " + Strings.gray + lib.getTicketSubject(ID));
                lore.add(Strings.Dgray + "Status: " + lib.getStatusFormat(ID));
                inv.addItem(i, Strings.greenU + "Ticket #" + ID, 1, slot, Material.PAPER, 0, lore);

                slot++;
                lore.clear();
            }
        }

        // Check if buttons is needed
        if (invSize(p) == 54) {
            lore.clear();
            lore.add(Strings.yellow + "Page: " + pageNumber);
            if (userCreatedTickets.size() > lib.pageCalcMax(pageNumber)) {
                inv.addItem(i, Strings.green + "Next page", 1, 50, Material.ARROW, 0, lore);
            }

            if (pageNumber >= 2) {
                inv.addItem(i, Strings.green + "Previous page", 1, 48, Material.ARROW, 0, lore);
            }
        }

        p.openInventory(i);
        lore.clear();
        userCreatedTickets.clear();
    }


    public void ticketStatus_UI(Player p, int ID) {
        Inventory i = plugin.getServer().createInventory(null, 27, Strings.DgrayB + "Ticket " + Strings.Dgray + "#" + Strings.DgrayB + ID);

        List<String> lore = new ArrayList<>();
        lore.add(Strings.Dgray + "Subject: " + Strings.gray + lib.getTicketSubject(ID));
        lore.add(Strings.Dgray + "Status: " + lib.getStatusFormat(ID));
        if (lib.getTicketStatus(ID).equals("ASSIGNED")) {
            lore.add(Strings.Dgray + "Assinged By: " + Strings.gray + lib.getTicketAssingedByPlayer(ID).getName());
        }
        inv.addItem(i, Strings.gray + "Info", 1, 0, Material.BOOK, 0, lore);
        inv.addItem(i, Strings.gray + "Mark Ticket solved", 1, 8, Material.ENCHANTED_BOOK, 0, null);
        inv.addItem(i, Strings.gray + "Message's", 1, 3, Material.PAPER, 0, null);
        inv.addItemV2(i, Strings.gray + "Back", 1, 26, XMaterial.WHITE_STAINED_GLASS_PANE.toString(), null);

        lore.clear();
        lore.add(Strings.Dgray + "Location: " + Strings.gray + lib.getTicketLocationFormat(ID));
        inv.addItem(i, Strings.gray + "Set location", 1, 4, Material.COMPASS, 0, lore);

        inv.addItemV2(i, Strings.gray + "Change Subject", 1, 5, XMaterial.WRITABLE_BOOK.toString(), null);

        lore.clear();
        lore.add(Strings.red + "WARNING, this is not for completing a Ticket!");
        inv.addItem(i, Strings.gray + "Delete Ticket", 1, 22, Material.BARRIER, 0, lore);

        p.openInventory(i);
    }


    @SuppressWarnings("ConstantConditions")
    private int invSize(Player p) {
        int count = lib.playerTotalTickets(p);

        if (count > 45) {
            return 54;
        }
        if (count > 36) {
            return 45;
        }
        if (count > 27) {
            return 36;
        }
        if (count > 18) {
            return 27;
        }
        if (count > 9) {
            return 18;
        }
        return 9;
    }
}
