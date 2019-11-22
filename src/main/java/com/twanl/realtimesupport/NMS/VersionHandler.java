package com.twanl.realtimesupport.NMS;

import org.bukkit.entity.Player;

/**
 * Created by Twan on 3/26/2018.
 **/
public abstract interface VersionHandler {

    public abstract void sendActionBar(Player paramPlayer, String paramString);

    public abstract void sendClickableMessage(Player p, String textpart, String clickabletext, String runcommand);

    public abstract void sendHoverableMessage(Player paramPlayer, String paramString1, String paramString2, String paramString3);

    public abstract void sendClickableHoverableMessage(Player paramPlayer, String textpart, String clickabletext, String hovertext, String runcommand);

    public abstract void sendClickableHovarableMessageURL(Player p, String clickabletext, String hovertext, String URL);

}
