package com.twanl.realtimesupport.menu;

import com.twanl.realtimesupport.RealTimeSupport;
import com.twanl.realtimesupport.lib.Lib;
import com.twanl.realtimesupport.service.NotificationSerivce;
import com.twanl.realtimesupport.util.LoadManager;
import com.twanl.realtimesupport.util.Strings;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import static com.twanl.realtimesupport.lib.Lib.createdTicket_id;

/**
 * Author: Twan Luttik
 * Date: 10/3/2018
 */

public class CreateTicket_UI implements Listener {

    private RealTimeSupport plugin = RealTimeSupport.getPlugin(RealTimeSupport.class);
    private invAPI inv = new invAPI();
    private Lib lib = new Lib();


    @EventHandler
    public void InvenClick(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        Inventory open = e.getInventory();
        String getTitle = e.getView().getTitle();

        ItemStack item = e.getCurrentItem();

        if (open == null) {
            return;
        }


        if (getTitle.equals(Strings.DgrayB + "Choose a subject")) {
            e.setCancelled(true);
            if (item == null || !item.hasItemMeta()) {
                return;
            }
            int ID = createdTicket_id.get(p);

            for (Object subjects : LoadManager.subjects()) {

                if (item.getItemMeta().getDisplayName().equals(Strings.gray + subjects)) {
                    p.closeInventory();
                    lib.updateTicketSubject(ID, subjects.toString());
                    NotificationSerivce.updateSubject(p, ID);
                }


            }
        }

    }


    public void subject_UI(Player p) {
        // Initialize
        Inventory i = plugin.getServer().createInventory(null, invSize(), Strings.DgrayB + "Choose a subject");
        int slot = 0;

        // loop trough the list for the subjects
        for (Object key : LoadManager.subjects()) {
            inv.addItem(i, Strings.gray + key, 1, slot, Material.PAPER, 0, null);
            slot++;
        }

        p.openInventory(i);

    }


    @SuppressWarnings("ConstantConditions")
    private int invSize() {
        int listSize = LoadManager.subjects().size();

        if (listSize >= 9) {
            return 18;
        }
        if (listSize >= 18) {
            return 27;
        }
        if (listSize >= 27) {
            return 36;
        }
        if (listSize >= 36) {
            return 45;
        }
        if (listSize >= 45) {
            return 54;
        }
        return 9;
    }
}
