package com.twanl.realtimesupport.menu;

import com.twanl.realtimesupport.RealTimeSupport;
import com.twanl.realtimesupport.lib.Lib;
import com.twanl.realtimesupport.service.NotificationSerivce;
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

import static com.twanl.realtimesupport.menu.ReplyMessages_UI.aa;

public class TicketOptions_UI implements Listener {

    private RealTimeSupport plugin = RealTimeSupport.getPlugin(RealTimeSupport.class);
    private invAPI inv = new invAPI();
    private Lib lib = new Lib();
    private static int i = 0;
    public static int backButtonID = 0;

    @EventHandler
    public void InvenClick(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        Inventory open = e.getInventory();
        String getTitle = e.getView().getTitle();

        ItemStack item = e.getCurrentItem();

        if (open == null) {
            return;
        }


        int ID = i;
        if (getTitle.equals(Strings.DgrayB + "Ticket " + Strings.Dgray + "#" + Strings.DgrayB + ID + " | Options")) {
            e.setCancelled(true);
            if (item == null || !item.hasItemMeta()) {
                return;
            }

            if (item.getItemMeta().getDisplayName().equals(Strings.gray + "Claim Ticket")) {
                lib.assignTicket(p, ID);
                ticketOptions_UI(p, ID);
                return;
            }

            if (item.getItemMeta().getDisplayName().equals(Strings.gray + "Message's")) {
                aa.put(p, 2);
                ReplyMessages_UI rm = new ReplyMessages_UI();
                rm.replyMessages_UI(p, ID);
                return;
            }

            if (item.getItemMeta().getDisplayName().equals(Strings.gray + "Mark Ticket solved")) {
                NotificationSerivce.closeTicketConfirmation(lib.getTicketCreatedByPlayer(i).getPlayer(), i);
                return;
            }

            if (item.getItemMeta().getDisplayName().equals(Strings.gray + "Delete Ticket")) {
                lib.deleteTicket(ID);
                p.closeInventory();
            }

            if (item.getItemMeta().getDisplayName().equals(Strings.gray + "Cancel Ticket")) {
                lib.cancelTicket(p, ID);
            }

            if (item.getItemMeta().getDisplayName().equals(Strings.gray + "Teleport to location")) {
                p.teleport(lib.getTicketLocation(ID));
            }

            if (item.getItemMeta().getDisplayName().equals(Strings.gray + "Back")) {
                if (backButtonID == 1) {
                    AssignedTickets_UI a1 = new AssignedTickets_UI();
                    a1.assignedTickets(p);
                    backButtonID = 0;
                } else {
                    Tickets_UI ot = new Tickets_UI();
                    ot.openTickets_UI(p);
                    backButtonID = 0;
                }


            }
        }

    }


    public void ticketOptions_UI(Player p, int ID) {
        Inventory inv1 = plugin.getServer().createInventory(null, 36, Strings.DgrayB + "Ticket " + Strings.Dgray + "#" + Strings.DgrayB + ID + " | Options");
        i = ID;

        ArrayList<String> lore = new ArrayList<>();

        if (lib.getTicketStatus(ID).equals("ASSIGNED")) {
            inv.addItem(inv1, Strings.gray + "Mark Ticket solved", 1, 8, Material.ENCHANTED_BOOK, 0, null);
            lore.add(Strings.red + "WARNING, this is not for completing a Ticket!");
            inv.addItem(inv1, Strings.gray + "Delete Ticket", 1, 32, Material.BARRIER, 0, lore);

            lore.clear();
            lore.add(Strings.red + "Use this if you want to un assign this ticket.");
            inv.addItem(inv1, Strings.gray + "Cancel Ticket", 1, 30, Material.REDSTONE, 0, null);

            if (!lib.getTicketLocationFormat(ID).equals("None")) {
                inv.addItem(inv1, Strings.gray + "Teleport to location", 1, 5, Material.COMPASS, 0, null);
            }

        } else if (lib.getTicketStatus(ID).equals("OPEN")) {

            inv.addItemV2(inv1, Strings.gray + "Claim Ticket", 1, 13, XMaterial.LIME_DYE.toString(), null);
        }

        for (int x = 18; x < 27; x++) {
            inv.addItemV2(inv1, " ", 1, x, XMaterial.BLACK_STAINED_GLASS_PANE.toString(), null);
        }

        inv.addItemV2(inv1, Strings.gray + "Message's", 1, 4, XMaterial.PAPER.toString(), null);
        inv.addItemV2(inv1, Strings.gray + "Back", 1, 35, XMaterial.RED_STAINED_GLASS_PANE.toString(), null);

        lore.clear();
        lore.add(Strings.Dgray + "Subject: " + Strings.gray + lib.getTicketSubject(ID));
        lore.add(Strings.Dgray + "Location: " + Strings.gray + lib.getTicketLocationFormat(ID));
        inv.addItem(inv1, Strings.yellow + "Information", 1, 0, Material.BOOK, 0, lore);

        p.openInventory(inv1);
    }


}
