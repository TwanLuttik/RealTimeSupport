package com.twanl.realtimesupport.menu;

import com.twanl.realtimesupport.RealTimeSupport;
import com.twanl.realtimesupport.lib.Lib;
import com.twanl.realtimesupport.util.ConfigManager;
import com.twanl.realtimesupport.util.Strings;
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

import static com.twanl.realtimesupport.menu.TicketOptions_UI.backButtonID;

public class AssignedTickets_UI implements Listener {

    private RealTimeSupport plugin = RealTimeSupport.getPlugin(RealTimeSupport.class);
    private HashMap<Player, Integer> page = new HashMap<>();
    private ConfigManager config = new ConfigManager();
    private invAPI inv = new invAPI();
    private Lib lib = new Lib();


    @EventHandler
    public void InvenClick(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        Inventory open = e.getInventory();
        String getTitle = e.getView().getTitle();
        config.setup();

        ItemStack item = e.getCurrentItem();

//        if (open == null) {
//            return;
//        }

        if (!lib.isStaff(p.getUniqueId())) {
            return;
        }

        if (getTitle.equals(Strings.DgrayB + "My assigned Tickets")) {
            e.setCancelled(true);
            if (item == null || !item.hasItemMeta()) {
                return;
            }

            for (String key : lib.getStaffTickets(p.getUniqueId()).split(",")) {
                if (key.isEmpty()) {
                    return;
                }
                int ID = Integer.parseInt(key);

                if (item.getItemMeta().getDisplayName().equals(Strings.greenU + "Ticket #" + ID)) {
                    backButtonID = 1;
                    TicketOptions_UI to = new TicketOptions_UI();
                    to.ticketOptions_UI(p, ID);
                    return;
                }
            }

            page.putIfAbsent(p, 1);

            if (item.getItemMeta().getDisplayName().equals(Strings.green + "Next page")) {
                int a = page.get(p) + 1;
                page.put(p, a);

                assignedTickets(p);
                return;
            }

            if (item.getItemMeta().getDisplayName().equals(Strings.green + "Previous page")) {
                int a = page.get(p) - 1;
                page.put(p, a);

                assignedTickets(p);
            }

        }

    }

    public void assignedTickets(Player p) {
        // Intialize
        config.setup();
        int slot = 0;
        ArrayList<String> lore = new ArrayList<>();
        Inventory i = plugin.getServer().createInventory(null, invSize(p), Strings.DgrayB + "My assigned Tickets");


        page.putIfAbsent(p, 1);
        int pageNumber = page.get(p);

        // Put all the ticket ID into a list
        List<Integer> AAA = new ArrayList<>();
        for (String key : lib.getStaffTickets(p.getUniqueId()).split(",")) {
            if (key.isEmpty()) {
                break;
            }
            AAA.add(Integer.valueOf(key));
        }
        Collections.sort(AAA);

        for (int item = 0; item < AAA.size(); item++) {
            // Put only the ticket in the gui depending on the page number
            if (item >= lib.pageCalcMin(pageNumber) - 1 && item <= lib.pageCalcMax(pageNumber) - 1) {
                int ID = AAA.get(item);

                lore.add(Strings.Dgray + "Created By: " + Strings.gray + lib.getTicketCreatedByPlayer(ID).getName());
                lore.add(Strings.Dgray + "Subject: " + Strings.gray + lib.getTicketSubject(ID));
                lore.add(Strings.Dgray + "Created Time: " + Strings.gray + lib.getTicketCreatedDate(ID));
                lore.add(" ");
                lore.add(Strings.grayI + "Click for more options!");

                inv.addItem(i, Strings.greenU + "Ticket #" + ID, 1, slot, Material.PAPER, 0, lore);
                slot++;
                lore.clear();
            }
        }

        // Check if buttons is needed
        if (invSize(p) == 54) {
            // This is for the buttons
            lore.add(Strings.yellow + "Page: " + pageNumber);
            if (AAA.size() > lib.pageCalcMax(pageNumber)) {
                inv.addItem(i, Strings.green + "Next page", 1, 50, Material.ARROW, 0, lore);
            }
            if (pageNumber >= 2) {
                inv.addItem(i, Strings.green + "Previous page", 1, 48, Material.ARROW, 0, lore);
            }
        }

        p.openInventory(i);
        lore.clear();
        AAA.clear();
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
