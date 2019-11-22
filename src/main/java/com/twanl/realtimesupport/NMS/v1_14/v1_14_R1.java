package com.twanl.realtimesupport.NMS.v1_14;

import com.twanl.realtimesupport.NMS.VersionHandler;
import net.minecraft.server.v1_14_R1.ChatMessageType;
import net.minecraft.server.v1_14_R1.IChatBaseComponent;
import net.minecraft.server.v1_14_R1.PacketPlayOutChat;
import net.minecraft.server.v1_14_R1.PlayerConnection;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

/**
 * Author: Twan Luttik
 * Date: 11/2/2018
 */
public class v1_14_R1 implements VersionHandler {

    public v1_14_R1() {}

    public void sendClickableHoverableMessage(Player p, String textpart, String clickabletext, String hovertext, String runcommand) {
        IChatBaseComponent chat = IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + textpart + "\",\"extra\":" +
                "[{\"text\":\"" + clickabletext + "\",\"hoverEvent\":{\"action\":\"show_text\", " +
                "\"value\":\"" + hovertext + "\"},\"clickEvent\":{\"action\":\"run_command\",\"value\":" +
                "\"/" + runcommand + "\"}}]}");
        PacketPlayOutChat packet = new PacketPlayOutChat(chat);
        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
    }

    public void sendClickableMessage(Player p, String textpart, String clickabletext, String runcommand) {
        IChatBaseComponent chat = IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + textpart + "\",\"extra\":" +
                "[{\"text\":\"" + clickabletext + "\",\"clickEvent\":{\"action\":\"run_command\",\"value\":" +
                "\"/" + runcommand + "\"}}]}");
        PacketPlayOutChat packet = new PacketPlayOutChat(chat);
        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
    }

    public void sendHoverableMessage(Player p, String textpart, String hoverabletext, String hovertext) {
        IChatBaseComponent chat = IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + textpart + "\",\"extra\":" +
                "[{\"text\":\"" + hoverabletext + "\",\"hoverEvent\":{\"action\":\"show_text\", " +
                "\"value\":\"" + hovertext + "\"}}]}");
        PacketPlayOutChat packet = new PacketPlayOutChat(chat);
        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
    }

    public void sendActionBar(Player p, String text) {
        PlayerConnection connection = ((CraftPlayer) p).getHandle().playerConnection;
        IChatBaseComponent localIChatBaseComponent = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + text + "\"}");
        PacketPlayOutChat localPacketPlayOutChat = new PacketPlayOutChat(localIChatBaseComponent, ChatMessageType.GAME_INFO);
        connection.sendPacket(localPacketPlayOutChat);
    }

    public void sendClickableHovarableMessageURL(Player p, String clickabletext, String hovertext, String URL) {
        IChatBaseComponent chat = IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + clickabletext + "\",\"clickEvent\":{\"action\":\"open_url\", " +
                "\"value\":\"" + URL + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"" + hovertext + "\"}]}}}");
        PacketPlayOutChat packet = new PacketPlayOutChat(chat);
        ((CraftPlayer)p).getHandle().playerConnection.sendPacket(packet);

    }
}
