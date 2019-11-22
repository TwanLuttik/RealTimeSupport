package com.twanl.realtimesupport.events;

import com.twanl.realtimesupport.RealTimeSupport;
import com.twanl.realtimesupport.lib.Lib;
import com.twanl.realtimesupport.menu.Tickets_UI;
import com.twanl.realtimesupport.util.Strings;
import com.twanl.realtimesupport.util.UpdateChecker;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Author: Twan Luttik
 * Date: 10/3/2018
 */

public class JoinEvent implements Listener {

    private RealTimeSupport plugin = RealTimeSupport.getPlugin(RealTimeSupport.class);
    private Lib lib = new Lib();


    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        final Player p = e.getPlayer();

        // Update message
        if (p.hasPermission("realtimesupport.update")) {
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                UpdateChecker checker = new UpdateChecker();

                if (!plugin.getDescription().getVersion().equals(checker.getUpdatedVersion()) && checker.isPreRelease()) {
                    p.sendMessage(Strings.DgrayBS + "                      \n" +
                            Strings.redB + "WARNING, you are running a Dev Build\n" +
                            Strings.green + "Version: " + checker.preReleaseVersion() + " \n"+
                            Strings.DgrayBS + "                      ");

                } else if (checker.hasUpdate() || checker.isPreRelease()) {
                    if (checker.isPreRelease()) {
                        p.sendMessage(Strings.DgrayBS + "                      \n");
                        plugin.nms.sendClickableHovarableMessageURL(p, Strings.red + "You are running a Dev build, please download the newest stable build!", Strings.gold + "Click to go to the download page", "https://www.spigotmc.org/resources/realtime-support-beta.61288/");
                        p.sendMessage(" \n" +
                                Strings.white + "Your version: " + plugin.getDescription().getVersion() + "\n" +
                                Strings.white + "Newest version: " + Strings.green + checker.getUpdatedVersion() + "\n" +
                                Strings.DgrayBS + "                      ");
                    } else {
                        p.sendMessage(Strings.DgrayBS + "                      \n");
                        plugin.nms.sendClickableHovarableMessageURL(p, Strings.red + "RealTime Support is outdated!", Strings.gold + "Click to go to the download page", "https://www.spigotmc.org/resources/realtime-support-beta.61288/");
                        p.sendMessage(" \n" +
                                Strings.white + "Your version: " + plugin.getDescription().getVersion() + "\n" +
                                Strings.white + "Newest version: " + Strings.green + checker.getUpdatedVersion() + "\n" +
                                Strings.DgrayBS + "                      ");
                    }
                } else {
                    p.sendMessage(Strings.DgrayBS + "                      \n" +
                            Strings.green + "RealTime Support is up to date.\n" +
                            Strings.DgrayBS + "                      ");
                }


            }, 20);

        }

        if (lib.isStaff(p.getUniqueId())) {
            Tickets_UI.filter.put(p, false);
            if (lib.openTicketsAvailable()) {
                p.sendMessage(Strings.gray + "There is still some tickets open that you can assign. " + Strings.green + "/rs tickets");
            }
        }


    }
}
