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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


public class ReplyMessages_UI implements Listener {

    private RealTimeSupport plugin = RealTimeSupport.getPlugin(RealTimeSupport.class);
    private ConfigManager config = new ConfigManager();
    private invAPI inv = new invAPI();
    private Lib lib = new Lib();

    public static HashMap<Player, Integer> id = new HashMap<>();
    public static HashMap<Player, Boolean> reply = new HashMap<>();
    public static HashMap<Player, Integer> aa = new HashMap<>();

    @EventHandler
    public void InvenClick(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        Inventory open = e.getInventory();
        String getTitle = e.getView().getTitle();
        ItemStack item = e.getCurrentItem();

        if (open == null) {
            return;
        }

        id.putIfAbsent(p, 0);
        int ID = id.get(p);

        if (getTitle.equals(Strings.DgrayB + "Ticket " + Strings.Dgray + "#" + Strings.DgrayB + ID)) {
            e.setCancelled(true);
            if (item == null || !item.hasItemMeta()) {
                return;
            }

            if (item.getItemMeta().getDisplayName().equals(Strings.gray + "Reply")) {
                p.closeInventory();
                p.sendMessage(" \n" + Strings.prefix() + Strings.gray + "Type your message in the chat, If you want to cancel type @ in chat.\n" + " ");
                reply.put(p, true);
                return;
            }

            if (item.getItemMeta().getDisplayName().equals(Strings.gray + "Back")) {
                // Check from whice menu this UI is opened

                switch (aa.get(p)) {
                    case 1:
                        TicketStatus_UI ts = new TicketStatus_UI();
                        ts.ticketStatus_UI(p, ID);
                        return;
                    case 2:
                        TicketOptions_UI to = new TicketOptions_UI();
                        to.ticketOptions_UI(p, ID);
                }
            }
        }
    }


    @SuppressWarnings("deprecation")
    public void replyMessages_UI(Player p, int ID) {

        // Initialize
        Inventory i = plugin.getServer().createInventory(null, 54, Strings.DgrayB + "Ticket " + Strings.Dgray + "#" + Strings.DgrayB + ID);
        config.setup();
        int slot = 0;
        id.put(p, ID);
        List<Integer> messageNumbers = new ArrayList<>();
        List<String> lore = new ArrayList<>();


        if (lib.sqlEnabled()) {

            // put all the message numbers from the ticket id into List
            try {
                PreparedStatement statement = plugin.getConnection().prepareStatement("SELECT * FROM messages_rts WHERE ID = ?;");
                statement.setInt(1, ID);
                ResultSet rs = statement.executeQuery();
                while (rs.next()) {
                    messageNumbers.add(rs.getInt("messageNumber"));
                }

                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            // Sort the list
            Collections.sort(messageNumbers);

            // For loopt list of all the messages
            for (int messageID : messageNumbers) {

                List<String> tempStringList = new ArrayList<>();
                // put the message into an arraylist
                for (String key : lib.getTicketMessage(ID, messageID).split(" ")) {
                    if (key.isEmpty()) {
                        continue;
                    }
                    tempStringList.add(key);
                }

                String a = "";
                for (String key : tempStringList) {

                    a = a + " " + key;
                    if (countWords(a) > 8) {
                        lore.add(Strings.white + a);
                        a = "";
                    }

                    if (a.length() > 50) {
                        lore.add(Strings.white + a);
                        a = "";
                    }
                }
                lore.add(Strings.white + a);
                tempStringList.clear();


                lore.add("");
                lore.add(Strings.DgrayI + "Replied: " + lib.getTicketMessageReplydate(ID, messageID));

                inv.addItemV2(i, Strings.grayU + lib.getTicketMessageSender(ID, messageID), 1, slot, XMaterial.WRITABLE_BOOK.toString(), lore);
                slot++;
                lore.clear();
            }


        } else {
            // put all the message numbers from the ticket id into List
            for (String key : config.getTicket().getConfigurationSection(ID + ".messages").getKeys(false)) {
                int messageID = Integer.parseInt(key);
                messageNumbers.add(messageID);

            }

            // Sort the list
            Collections.sort(messageNumbers);

            // For loopt list of all the messages
            for (int messageID : messageNumbers) {

                List<String> tempStringList = new ArrayList<>();
                // put the message into an arraylist
                for (String key : lib.getTicketMessage(ID, messageID).split(" ")) {
                    if (key.isEmpty()) {
                        continue;
                    }
                    tempStringList.add(key);
                }

                String a = "";
                for (String key : tempStringList) {

                    a = a + " " + key;
                    if (countWords(a) > 8) {
                        lore.add(Strings.white + a);
                        a = "";
                    }

                    if (a.length() > 50) {
                        lore.add(Strings.white + a);
                        a = "";
                    }
                }
                lore.add(Strings.white + a);
                tempStringList.clear();


                lore.add("");
                lore.add(Strings.DgrayI + "Replied: " + lib.getTicketMessageReplydate(ID, messageID));

                inv.addItemV2(i, Strings.grayU + lib.getTicketMessageSender(ID, messageID), 1, slot, XMaterial.WRITABLE_BOOK.toString(), lore);
                slot++;
                lore.clear();


//
//            for (String key : config.getTicket().getConfigurationSection(ID + ".messages").getKeys(false)) {
//                String[] message = config.getTicket().getString(ID + ".messages." + key + ".message").split(":");
//
//                lore.add(Strings.white + message[1]);
//
//
//                lore.add("");
//                lore.add(Strings.DgrayI + "Replied: " + getTime(ID, Integer.parseInt(key)));
//
//                inv.addItemV2(i, Strings.gray + message[0], 1, slot, XMaterial.WRITABLE_BOOK.toString(), lore);
//                slot++;
//                lore.clear();
//            }
            }
        }

        if (lib.getTicketStatus(ID).equals("ASSIGNED")) {
            inv.addItem(i, Strings.gray + "Reply", 1, 49, Material.SLIME_BALL, 0, null);
        }
        inv.addItemV2(i, Strings.gray + "Back", 1, 53, XMaterial.WHITE_STAINED_GLASS_PANE.toString(), null);

        p.openInventory(i);
    }


    private String getTime(int ID, int messageNumber) {
        config.setup();
        return config.getTicket().getString(ID + ".messages." + messageNumber + ".replyDate");
    }

    public static int countWords(String s) {

        int wordCount = 0;

        boolean word = false;
        int endOfLine = s.length() - 1;

        for (int i = 0; i < s.length(); i++) {
            // if the char is a letter, word = true.
            if (Character.isLetter(s.charAt(i)) && i != endOfLine) {
                word = true;
                // if char isn't a letter and there have been letters before,
                // counter goes up.
            } else if (!Character.isLetter(s.charAt(i)) && word) {
                wordCount++;
                word = false;
                // last word of String; if it doesn't end with a non letter, it
                // wouldn't count without this.
            } else if (Character.isLetter(s.charAt(i)) && i == endOfLine) {
                wordCount++;
            }
        }
        return wordCount;
    }

}
