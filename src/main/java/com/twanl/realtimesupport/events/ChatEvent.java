package com.twanl.realtimesupport.events;

import com.twanl.realtimesupport.lib.Lib;
import com.twanl.realtimesupport.service.DynamicInventoryUpdatesService;
import com.twanl.realtimesupport.service.NotificationSerivce;
import com.twanl.realtimesupport.util.ConfigManager;
import com.twanl.realtimesupport.util.Strings;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.UUID;

import static com.twanl.realtimesupport.menu.ReplyMessages_UI.id;
import static com.twanl.realtimesupport.menu.ReplyMessages_UI.reply;

public class ChatEvent implements Listener {

    private ConfigManager config = new ConfigManager();
    private DynamicInventoryUpdatesService DynamicUpdateService = new DynamicInventoryUpdatesService();
    private Lib lib = new Lib();


    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();


        if (reply.get(p) == null) {
            return;
        }


        if (reply.get(p)) {
            config.setup();
            int ID = id.get(p);
            String a = e.getMessage();
            if (a.contains("@")) {
                p.sendMessage(Strings.prefix() + Strings.gray + "You cancelled replying to the Ticket!");
                reply.clear();
                e.setCancelled(true);
                return;
            }


            lib.replyToTicket(ID, p, a);
            reply.clear();
            NotificationSerivce.replyMessage(p, receiverChecker(p.getUniqueId(), ID), ID);
            try {
                DynamicUpdateService.updateInventory(ID, receiverChecker(p.getUniqueId(), ID));
            } catch (Exception e1) {
                e1.printStackTrace();
                e1.getCause();
            }
            e.setCancelled(true);
        }

    }


    private UUID receiverChecker(UUID uuid, int ID) {
        if (uuid != lib.getTicketCreatedByPlayer(ID).getUniqueId()) {
            return lib.getTicketCreatedByPlayer(ID).getUniqueId();
        }
        return lib.getTicketCreatedByPlayer(ID).getUniqueId();
    }


}
