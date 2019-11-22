package com.twanl.realtimesupport.NMS.v1_11;

import com.twanl.realtimesupport.NMS.VersionHandler;
import net.minecraft.server.v1_11_R1.IChatBaseComponent;
import net.minecraft.server.v1_11_R1.PacketPlayOutChat;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

/**
 * Created by Twan on 3/26/2018.
 **/
public class v1_11_R1 implements VersionHandler {

    public v1_11_R1() {}

    public void sendClickableHoverableMessage(Player p, String textpart, String clickabletext, String hovertext, String runcommand) {
        IChatBaseComponent chat = IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + textpart + "\",\"extra\":" +
                "[{\"text\":\"" + clickabletext + "\",\"hoverEvent\":{\"action\":\"show_text\", " +
                "\"value\":\"" + hovertext + "\"},\"clickEvent\":{\"action\":\"run_command\",\"value\":" +
                "\"/" + runcommand + "\"}}]}");
        PacketPlayOutChat packet = new PacketPlayOutChat(chat);
        ((CraftPlayer)p).getHandle().playerConnection.sendPacket(packet);
    }

    public void sendClickableMessage(Player p, String textpart, String clickabletext, String runcommand) {
        IChatBaseComponent chat = IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + textpart + "\",\"extra\":" +
                "[{\"text\":\"" + clickabletext + "\",\"clickEvent\":{\"action\":\"run_command\",\"value\":" +
                "\"/" + runcommand + "\"}}]}");
        PacketPlayOutChat packet = new PacketPlayOutChat(chat);
        ((CraftPlayer)p).getHandle().playerConnection.sendPacket(packet);
    }

    public void sendHoverableMessage(Player p, String textpart, String hoverabletext, String hovertext) {
        IChatBaseComponent chat = IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + textpart + "\",\"extra\":" +
                "[{\"text\":\"" + hoverabletext + "\",\"hoverEvent\":{\"action\":\"show_text\", " +
                "\"value\":\"" + hovertext + "\"}}]}");
        PacketPlayOutChat packet = new PacketPlayOutChat(chat);
        ((CraftPlayer)p).getHandle().playerConnection.sendPacket(packet);
    }

    public void sendActionBar(Player player, String message) {
        IChatBaseComponent icbc = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + message + "\"}");
        PacketPlayOutChat bar = new PacketPlayOutChat(icbc, (byte)2);
        ((CraftPlayer)player).getHandle().playerConnection.sendPacket(bar);
    }

    public void sendClickableHovarableMessageURL(Player p, String clickabletext, String hovertext, String URL) {
        IChatBaseComponent chat = IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + clickabletext + "\",\"clickEvent\":{\"action\":\"open_url\", " +
                "\"value\":\"" + URL + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"" + hovertext + "\"}]}}}");
        PacketPlayOutChat packet = new PacketPlayOutChat(chat);
        ((CraftPlayer)p).getHandle().playerConnection.sendPacket(packet);
    }
}
