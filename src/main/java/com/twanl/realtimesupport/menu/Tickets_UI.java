package com.twanl.realtimesupport.menu;

import com.twanl.realtimesupport.RealTimeSupport;
import com.twanl.realtimesupport.lib.Lib;
import com.twanl.realtimesupport.service.TicketLoadService;
import com.twanl.realtimesupport.util.ConfigManager;
import com.twanl.realtimesupport.util.Strings;
import com.twanl.realtimesupport.util.XMaterial;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.twanl.realtimesupport.service.TicketLoadService.*;

/**
 * Author: Twan Luttik
 * Date: 10/3/2018
 */

public class Tickets_UI implements Listener {

    private RealTimeSupport plugin = RealTimeSupport.getPlugin(RealTimeSupport.class);
    private ConfigManager config = new ConfigManager();
    private TicketLoadService tls = new TicketLoadService();
    private invAPI inv = new invAPI();
    private Lib lib = new Lib();

    public static HashMap<Player, Boolean> filter = new HashMap<>();
    private static HashMap<Player, Integer> openTicketPAGE = new HashMap<>();
    private static HashMap<Player, Integer> assignedTicketPAGE = new HashMap<>();
    private static HashMap<Player, Integer> solvedTicketPAGE = new HashMap<>();


    @EventHandler
    public void InvenClick(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        Inventory open = e.getInventory();
        String getTitle = e.getView().getTitle();
        ItemStack item = e.getCurrentItem();

        if (open == null) {
            return;
        }

        // Check if the player is a staff
        if (!lib.isStaff(p.getUniqueId())) {
            return;
        }

        filter.putIfAbsent(p, false);


        if (getTitle.equals(Strings.DgreenB + "Open " + Strings.DgrayB + "Tickets")) {
            e.setCancelled(true);
            if (item == null || !item.hasItemMeta()) {
                return;
            }

            for (Integer ID : openList) {
                if (item.getItemMeta().getDisplayName().equals(Strings.greenU + "Ticket #" + ID)) {
                    if (e.getClick() == ClickType.SHIFT_RIGHT) {
                        lib.assignTicket(p, ID);
                        return;
                    }
                    TicketOptions_UI to = new TicketOptions_UI();
                    to.ticketOptions_UI(p, ID);
                    return;
                }
            }


            openTicketPAGE.putIfAbsent(p, 1);

            if (item.getItemMeta().getDisplayName().equals(Strings.green + "Next page")) {
                int a = openTicketPAGE.get(p) + 1;
                openTicketPAGE.put(p, a);

                openTickets_UI(p);
                return;
            }

            if (item.getItemMeta().getDisplayName().equals(Strings.green + "Previous page")) {
                int a = openTicketPAGE.get(p) - 1;
                openTicketPAGE.put(p, a);

                openTickets_UI(p);
                return;
            }

            if (item.getItemMeta().getDisplayName().equals(Strings.gray + "Status: " + Strings.gold + "ASSIGNED")) {
                assignedTicket_UI(p);
                return;
            }

            if (item.getItemMeta().getDisplayName().equals(Strings.gray + "Status: " + Strings.red + "SOLVED")) {
                solvedTicket_UI(p);
                return;
            }
            return;
        }


        if (getTitle.equals(Strings.goldB + "Assigned " + Strings.DgrayB + "Tickets")) {
            e.setCancelled(true);
            if (item == null || !item.hasItemMeta()) {
                return;
            }

            if (item.getItemMeta().getDisplayName().equals(Strings.gray + "Filter:" + Strings.red + " Disabled")) {
                filter.put(p, true);
                assignedTicket_UI(p);
                return;
            }
            if (item.getItemMeta().getDisplayName().equals(Strings.gray + "Filter:" + Strings.green + " Enabled")) {
                filter.put(p, false);
                assignedTicket_UI(p);
                return;
            }


            if (item.getItemMeta().getDisplayName().equals(Strings.gray + "Status: " + Strings.green + "OPEN")) {
                openTickets_UI(p);
                return;
            }

            if (item.getItemMeta().getDisplayName().equals(Strings.gray + "Status: " + Strings.red + "SOLVED")) {
                solvedTicket_UI(p);
                return;
            }

            for (String key : lib.getStaffTickets(p.getUniqueId()).split(",")) {
                if (key.isEmpty()) {
                    break;
                }
                int ID = Integer.parseInt(key);

                if (item.getItemMeta().getDisplayName().equals(Strings.greenU + "Ticket #" + ID)) {
                    TicketOptions_UI to = new TicketOptions_UI();
                    to.ticketOptions_UI(p, ID);
                    return;
                }
            }

            assignedTicketPAGE.putIfAbsent(p, 1);

            if (item.getItemMeta().getDisplayName().equals(Strings.green + "Next page")) {
                int a = assignedTicketPAGE.get(p) + 1;
                assignedTicketPAGE.put(p, a);

                assignedTicket_UI(p);
                return;
            }

            if (item.getItemMeta().getDisplayName().equals(Strings.green + "Previous page")) {
                int a = assignedTicketPAGE.get(p) - 1;
                assignedTicketPAGE.put(p, a);

                assignedTicket_UI(p);
                return;
            }

            return;
        }

        if (getTitle.equals(Strings.redB + "Solved " + Strings.DgrayB + "Tickets")) {
            e.setCancelled(true);
            if (item == null || !item.hasItemMeta()) {
                return;
            }

            if (item.getItemMeta().getDisplayName().equals(Strings.gray + "Status: " + Strings.green + "OPEN")) {
                openTickets_UI(p);
            }

            if (item.getItemMeta().getDisplayName().equals(Strings.gray + "Status: " + Strings.gold + "ASSIGNED")) {
                assignedTicket_UI(p);
            }

            solvedTicketPAGE.putIfAbsent(p, 1);

            if (item.getItemMeta().getDisplayName().equals(Strings.green + "Next page")) {
                int a = solvedTicketPAGE.get(p) + 1;
                solvedTicketPAGE.put(p, a);

                solvedTicket_UI(p);
                return;
            }

            if (item.getItemMeta().getDisplayName().equals(Strings.green + "Previous page")) {
                int a = solvedTicketPAGE.get(p) - 1;
                solvedTicketPAGE.put(p, a);

                solvedTicket_UI(p);
            }
        }
    }


    public void openTickets_UI(Player p) {
        // Initializa
        config.setup();
        Inventory inv1 = plugin.getServer().createInventory(null, 54, Strings.DgreenB + "Open " + Strings.DgrayB + "Tickets");
        int slot = 0;
        openTicketPAGE.putIfAbsent(p, 1);

        // Define the pagenumber
        int pageNumber = openTicketPAGE.get(p);

        // For loop trough each ticket
        for (int item = 0; item < openList.size(); item++) {
            // Put only the ticket in the gui depending on the page number
            if (item >= lib.pageCalcMin(pageNumber) - 1 && item <= lib.pageCalcMax(pageNumber) - 1) {
                int ID = openList.get(item);

                ArrayList<String> lore = new ArrayList<>();
                lore.add(Strings.Dgray + "Created By: " + Strings.gray + lib.getTicketCreatedByPlayer(ID).getName());
                lore.add(Strings.Dgray + "Subject: " + Strings.gray + lib.getTicketSubject(ID));
                lore.add(Strings.Dgray + "Status: " + lib.getStatusFormat(ID));
                lore.add(Strings.Dgray + "Created Time: " + Strings.gray + lib.getTicketCreatedDate(ID));
                lore.add(" ");
                lore.add(Strings.grayI + "Click for more options!");

                inv.addItemV2(inv1, Strings.greenU + "Ticket #" + ID, 1, slot, Material.PAPER.toString(), lore);
                slot++;
            }
        }


        // This is for the buttons
        ArrayList<String> lorepage = new ArrayList<>();
        lorepage.add(Strings.yellow + "Page: " + pageNumber);

        if (openList.size() > lib.pageCalcMax(pageNumber)) {
            inv.addItem(inv1, Strings.green + "Next page", 1, 53, Material.ARROW, 0, lorepage);
        }

        if (pageNumber >= 2) {
            inv.addItem(inv1, Strings.green + "Previous page", 1, 52, Material.ARROW, 0, lorepage);
        }


        ArrayList<String> lore = new ArrayList<>();
        lore.add(Strings.yellow + "Shift Right Click: " + Strings.gray + "to claim the Ticket");
        inv.addItem(inv1, Strings.greenU + "Information", 1, 49, Material.NETHER_STAR, 0, lore);
        inv.addItemV2(inv1, Strings.gray + "Status: " + Strings.green + "OPEN", 1, 45, XMaterial.LIME_DYE.toString(), null);
        inv.addItemV2(inv1, Strings.gray + "Status: " + Strings.gold + "ASSIGNED", 1, 46, XMaterial.GRAY_DYE.toString(), null);
        inv.addItemV2(inv1, Strings.gray + "Status: " + Strings.red + "SOLVED", 1, 47, XMaterial.GRAY_DYE.toString(), null);

        p.openInventory(inv1);
    }


    public void assignedTicket_UI(Player p) {
        // Initialize
        Inventory inv1 = plugin.getServer().createInventory(null, 54, Strings.goldB + "Assigned " + Strings.DgrayB + "Tickets");
        config.setup();
        int slot = 0;
        assignedTicketPAGE.putIfAbsent(p, 1);
        int pageNumber = assignedTicketPAGE.get(p);
        filter.putIfAbsent(p, false);

        if (!filter.get(p)) {
            for (int item = 0; item < assignedList.size(); item++) {
                if (item >= lib.pageCalcMin(pageNumber) - 1 && item <= lib.pageCalcMax(pageNumber) - 1) {
                    int ID = assignedList.get(item);

                    ArrayList<String> lore = new ArrayList<>();
                    lore.add(Strings.Dgray + "Created By: " + Strings.gray + lib.getTicketCreatedByPlayer(ID).getName());
                    lore.add(Strings.Dgray + "Subject: " + Strings.gray + lib.getTicketSubject(ID));
                    lore.add(Strings.Dgray + "Status: " + lib.getStatusFormat(ID));
                    lore.add(Strings.Dgray + "Created Time: " + Strings.gray + lib.getTicketCreatedDate(ID));
                    lore.add(" ");
                    lore.add(Strings.Dgray + "Assigned By: " + Strings.gray + lib.getTicketAssingedByPlayer(ID).getName());
                    lore.add(" ");
                    lore.add(Strings.grayI + "Click for more options!");

                    inv.addItem(inv1, Strings.greenU + "Ticket #" + ID, 1, slot, Material.PAPER, 0, lore);
                    slot++;
                }
            }
        } else {
            List<Integer> aa1 = new ArrayList();
            aa1.clear();

            String a = lib.getStaffTickets(p.getUniqueId());
            for (String key : a.split(",")) {
                if (key.isEmpty()) {
                    break;
                }
                int i = Integer.parseInt(key);
                aa1.add(i);
            }

            for (int item = 0; item < aa1.size(); item++) {
                if (item >= lib.pageCalcMin(pageNumber) - 1 && item <= lib.pageCalcMax(pageNumber) - 1) {
                    int ID = aa1.get(item);

                    ArrayList<String> lore = new ArrayList<>();
                    lore.add(Strings.Dgray + "Created By: " + Strings.gray + lib.getTicketCreatedByPlayer(ID).getName());
                    lore.add(Strings.Dgray + "Subject: " + Strings.gray + lib.getTicketSubject(ID));
                    lore.add(Strings.Dgray + "Status: " + lib.getStatusFormat(ID));
                    lore.add(Strings.Dgray + "Created Time: " + Strings.gray + lib.getTicketCreatedDate(ID));
                    lore.add(" ");
                    lore.add(Strings.Dgray + "Assigned By: " + Strings.gray + lib.getTicketAssingedByPlayer(ID).getName());
                    lore.add(" ");
                    lore.add(Strings.grayI + "Click for more options!");

                    inv.addItem(inv1, Strings.greenU + "Ticket #" + ID, 1, slot, Material.PAPER, 0, lore);
                    slot++;
                }
            }
        }

        ArrayList<String> lorepage = new ArrayList<>();
        lorepage.add(Strings.yellow + "Page: " + pageNumber);

        if (assignedList.size() > lib.pageCalcMax(pageNumber)) {
            inv.addItem(inv1, Strings.green + "Next page", 1, 53, Material.ARROW, 0, lorepage);
        }

        if (pageNumber >= 2) {
            inv.addItem(inv1, Strings.green + "Previous page", 1, 52, Material.ARROW, 0, lorepage);
        }

        inv.addItemV2(inv1, Strings.gray + "Status: " + Strings.green + "OPEN", 1, 45, XMaterial.GRAY_DYE.toString(), null);
        inv.addItemV2(inv1, Strings.gray + "Status: " + Strings.gold + "ASSIGNED", 1, 46, XMaterial.LIME_DYE.toString(), null);
        inv.addItemV2(inv1, Strings.gray + "Status: " + Strings.red + "SOLVED", 1, 47, XMaterial.GRAY_DYE.toString(), null);

        ArrayList<String> lore = new ArrayList<>();
        lore.add(Strings.Dgray + "This will filter for your assinged tickets.");
        if (!filter.get(p)) {
            inv.addItem(inv1, Strings.gray + "Filter:" + Strings.red + " Disabled", 1, 50, Material.TRIPWIRE_HOOK, 0, lore);
        } else if (filter.get(p)) {
            inv.addItemGlow(inv1, Strings.gray + "Filter:" + Strings.green + " Enabled", 1, 50, XMaterial.TRIPWIRE_HOOK.toString(), lore);
        }

        p.openInventory(inv1);
    }


    public void solvedTicket_UI(Player p) {
        // Initialize
        Inventory inv1 = plugin.getServer().createInventory(null, 54, Strings.redB + "Solved " + Strings.DgrayB + "Tickets");
        config.setup();
        int slot = 0;
        solvedTicketPAGE.putIfAbsent(p, 1);


        int pageNumber = solvedTicketPAGE.get(p);

        for (int item = 0; item < solvedList.size(); item++) {
            if (item >= lib.pageCalcMin(pageNumber) - 1 && item <= lib.pageCalcMax(pageNumber) - 1) {
                int ID = solvedList.get(item);

                ArrayList<String> lore = new ArrayList<>();
                lore.add(Strings.Dgray + "Created By: " + Strings.gray + lib.getTicketCreatedByPlayer(ID).getName());
                lore.add(Strings.Dgray + "Subject: " + Strings.gray + lib.getTicketSubject(ID));
                lore.add(Strings.Dgray + "Status: " + Strings.red + lib.getStatusFormat(ID));
                lore.add(Strings.Dgray + "Created Date: " + Strings.gray + lib.getTicketCreatedDate(ID));
                lore.add(" ");
                lore.add(Strings.Dgray + "Assigned By: " + Strings.gray + lib.getTicketAssingedByPlayer(ID).getName());
                lore.add(Strings.Dgray + "Solved Date: " + Strings.gray + lib.getTicketSolvedDate(ID));
                lore.add(Strings.Dgray + "Ticket Rating: " + Strings.gray + lib.getTicketRating(ID) + " Star(s)");


                inv.addItem(inv1, Strings.greenU + "Ticket #" + ID, 1, slot, Material.PAPER, 0, lore);
                slot++;
                lore.clear();
            }
        }


        ArrayList<String> lorepage = new ArrayList<>();
        lorepage.add(Strings.yellow + "Page: " + pageNumber);

        if (solvedList.size() > lib.pageCalcMax(pageNumber)) {
            inv.addItem(inv1, Strings.green + "Next page", 1, 53, Material.ARROW, 0, lorepage);
        }

        if (pageNumber >= 2) {
            inv.addItem(inv1, Strings.green + "Previous page", 1, 52, Material.ARROW, 0, lorepage);
        }


        inv.addItemV2(inv1, Strings.gray + "Status: " + Strings.green + "OPEN", 1, 45, XMaterial.GRAY_DYE.toString(), null);
        inv.addItemV2(inv1, Strings.gray + "Status: " + Strings.gold + "ASSIGNED", 1, 46, XMaterial.GRAY_DYE.toString(), null);
        inv.addItemV2(inv1, Strings.gray + "Status: " + Strings.red + "SOLVED", 1, 47, XMaterial.LIME_DYE.toString(), null);

        p.openInventory(inv1);

    }
}
