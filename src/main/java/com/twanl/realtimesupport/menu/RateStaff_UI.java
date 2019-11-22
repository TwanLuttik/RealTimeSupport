package com.twanl.realtimesupport.menu;

import com.twanl.realtimesupport.RealTimeSupport;
import com.twanl.realtimesupport.lib.Lib;
import com.twanl.realtimesupport.service.DynamicInventoryUpdatesService;
import com.twanl.realtimesupport.service.NotificationSerivce;
import com.twanl.realtimesupport.service.RatingService;
import com.twanl.realtimesupport.util.ConfigManager;
import com.twanl.realtimesupport.util.Strings;
import com.twanl.realtimesupport.util.XMaterial;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import static com.twanl.realtimesupport.lib.Lib.test;

/**
 * Author: Twan Luttik
 * Date: 10/5/2018
 */

public class RateStaff_UI implements Listener {

    private RealTimeSupport plugin = RealTimeSupport.getPlugin(RealTimeSupport.class);
    private DynamicInventoryUpdatesService dynamicUpdateService = new DynamicInventoryUpdatesService();
    private ConfigManager config = new ConfigManager();
    private invAPI inv = new invAPI();
    private Lib lib = new Lib();
    private static int rating = 0;


    @EventHandler
    public void InvenClick(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        Inventory open = e.getInventory();
        String getTitle = e.getView().getTitle();
        config.setup();

        ItemStack item = e.getCurrentItem();

        if (open == null) {
            return;
        }


        if (getTitle.equals(Strings.DgrayB + "Rate The Ticket")) {
            e.setCancelled(true);
            if (item == null || !item.hasItemMeta()) {
                return;
            }

            int ID = Integer.parseInt(test.toString().replace("[", "").replace("]", ""));
            OfflinePlayer p1 = lib.getTicketAssingedByPlayer(ID);

            RatingService rs = new RatingService();
            if (item.getItemMeta().getDisplayName().equals(Strings.redB + "✦")) {
                p.closeInventory();
                rs.rateStaff(p1.getPlayer(), 0);
                rating = 1;
            }

            if (item.getItemMeta().getDisplayName().equals(Strings.goldB + "✦✦")) {
                p.closeInventory();
                rs.rateStaff(p1.getPlayer(), 1);
                rating = 2;
            }

            if (item.getItemMeta().getDisplayName().equals(Strings.yellowB + "✦✦✦")) {
                p.closeInventory();
                rs.rateStaff(p1.getPlayer(), 2);
                rating = 3;
            }

            if (item.getItemMeta().getDisplayName().equals(Strings.greenB + "✦✦✦✦")) {
                p.closeInventory();
                rs.rateStaff(p1.getPlayer(), 3);
                rating = 4;
            }

            if (item.getItemMeta().getDisplayName().equals(Strings.DgreenB + "✦✦✦✦✦")) {
                p.closeInventory();
                rs.rateStaff(p1.getPlayer(), 4);
                rating = 5;
            }
            NotificationSerivce.rateStaff(p, p1.getUniqueId(), ID, rating);
            lib.saveRatingToTicket(rating, ID);
            dynamicUpdateService.updateInventory(0 , null);
            test.clear();

        }
    }


    public void openTicketsMenu(Player p) {
        Inventory i = plugin.getServer().createInventory(null, 9, Strings.DgrayB + "Rate The Ticket");
        inv.addItemV2(i, Strings.redB + "✦", 1, 2, XMaterial.RED_STAINED_GLASS_PANE.toString(), null);
        inv.addItemV2(i, Strings.goldB + "✦✦", 1, 3, XMaterial.ORANGE_STAINED_GLASS_PANE.toString(), null);
        inv.addItemV2(i, Strings.yellowB + "✦✦✦", 1, 4, XMaterial.YELLOW_STAINED_GLASS_PANE.toString(), null);
        inv.addItemV2(i, Strings.greenB + "✦✦✦✦", 1, 5, XMaterial.LIME_STAINED_GLASS_PANE.toString(), null);
        inv.addItemV2(i, Strings.DgreenB + "✦✦✦✦✦", 1, 6, XMaterial.GREEN_STAINED_GLASS_PANE.toString(), null);

        p.openInventory(i);
    }


}
