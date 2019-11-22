package com.twanl.realtimesupport;

import com.twanl.realtimesupport.lib.Lib;
import com.twanl.realtimesupport.menu.AssignedTickets_UI;
import com.twanl.realtimesupport.menu.CreateTicket_UI;
import com.twanl.realtimesupport.menu.TicketStatus_UI;
import com.twanl.realtimesupport.menu.Tickets_UI;
import com.twanl.realtimesupport.service.NotificationSerivce;
import com.twanl.realtimesupport.service.PurgeTicketService;
import com.twanl.realtimesupport.service.RatingService;
import com.twanl.realtimesupport.service.TicketLoadService;
import com.twanl.realtimesupport.util.ConfigManager;
import com.twanl.realtimesupport.util.LoadManager;
import com.twanl.realtimesupport.util.Strings;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Author: Twan Luttik
 * Date: 10/2/2018
 */

@SuppressWarnings("deprecation")
public class Commands implements CommandExecutor {

    private RealTimeSupport plugin = RealTimeSupport.getPlugin(RealTimeSupport.class);
    private CreateTicket_UI ticket = new CreateTicket_UI();
    private Tickets_UI opnTicketsInv = new Tickets_UI();
    private TicketStatus_UI TicketStatus_UI = new TicketStatus_UI();
    private PurgeTicketService pts = new PurgeTicketService();
    private ConfigManager config = new ConfigManager();
    private RatingService rs = new RatingService();
    private Lib lib = new Lib();
    private StringBuilder sb = new StringBuilder();


    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        config.setup();




        if (!(sender instanceof Player)) {

            if (cmd.getName().equalsIgnoreCase("realtimesupport")) {
                if (args[0].equalsIgnoreCase("purgeall")) {
                    pts.purgeTickets();
                    Bukkit.getConsoleSender().sendMessage(Strings.logName + Strings.gray + "You succsessfully purged " + Strings.green + pts.sessionPurgedTickets() + Strings.gray + " Ticket(s)!");
                    return true;
                } else if (args[0].equalsIgnoreCase("staff")) {

                    if (args.length == 1) {
                        Bukkit.getConsoleSender().sendMessage(Strings.logName + Strings.gray + "Command usage: /rs staff add/remove <player>");
                        return true;
                    }
                    String a = args[1];

                    if (a.equals("add")) {
                        // if the player forget to put in a player
                        if (args.length == 2) {
                            Bukkit.getConsoleSender().sendMessage(Strings.logName + Strings.gray + "specify a player");
                            return true;
                        }

                        String b = args[2];
                        Player p1 = Bukkit.getPlayer(b);

                        // Check if the player is real
                        if (p1 == null) {

                            Bukkit.getConsoleSender().sendMessage(Strings.logName + Strings.red + "Player is not valid or online!");
                            return true;
                        }

                        if (lib.isStaff(p1.getUniqueId())) {
                            Bukkit.getConsoleSender().sendMessage(Strings.logName + Strings.red + "That player is already staff!");
                            return true;
                        }
                        lib.staffAddPlayer(p1.getUniqueId());
                        Bukkit.getConsoleSender().sendMessage(Strings.logName + Strings.green + "You succssesfully added the player to the staff");
                        return true;

                    } else if (a.equals("remove")) {
                        // if the player forget to put in a player
                        if (args.length == 2) {
                            Bukkit.getConsoleSender().sendMessage(Strings.logName + Strings.gray + "specify a player");
                            return true;
                        }

                        String b = args[2];
                        Player p1 = Bukkit.getPlayer(b);

                        // Check if the player is real
                        if (p1 == null) {
                            Bukkit.getConsoleSender().sendMessage(Strings.logName + Strings.red + "Player is not valid or online!");
                            return true;
                        }

                        if (!lib.isStaff(p1.getUniqueId())) {
                            Bukkit.getConsoleSender().sendMessage(Strings.logName + Strings.red + "That player is not a staff!");
                            return true;
                        }

                        lib.staffRemovePlayer(p1.getUniqueId());
                        Bukkit.getConsoleSender().sendMessage(Strings.logName + Strings.green + "You succssesfully remove the player from the staff");
                        return true;
                    }
                    return true;
                } else if (args[0].equalsIgnoreCase("reload")) {
                    TicketLoadService tls = new TicketLoadService();
                    lib.configVersionUpdate();
                    plugin.Load();
                    tls.loadTickets();
                    Bukkit.getConsoleSender().sendMessage(Strings.logName + Strings.gray + "Plugin succssesfully reloaded!");
                    return true;
                }

            }



            sender.sendMessage(Strings.logName + Strings.red + "You can only execute commands in game!");
            return true;
        }
        Player p = (Player) sender;


        if (cmd.getName().equalsIgnoreCase("realtimesupport")) {
            if (args.length == 0) {

            } else if (args[0].equalsIgnoreCase("create")) {

                if (lib.isStaff(p.getUniqueId())) {
                    p.sendMessage(Strings.prefix() + Strings.gray + "Only non-staff members can create a Ticket!");
                    return true;
                }


                if (p.hasPermission("realtimesupport.create")) {
                    // Check if the player has not overide the max amount of ticket creations
                    if (LoadManager.user_max_tickets() != -1 && lib.playerTotalTickets(p) >= LoadManager.user_max_tickets()) {
                        p.sendMessage("The limit of the amount of tickets are 5 total.");
                        return true;
                    }

                    // Check if the message is not filled in
                    if (args.length == 1) {
                        p.sendMessage(Strings.prefix() + Strings.gray + "Enter your message");
                        return true;
                    }

                    // This will put the hole message to one string
                    for (int i = 1; i != args.length; i++) {
                        sb.append(args[i]).append(" ");
                    }
                    String message = sb.toString();

                    // CreateCommand the ticket
                    lib.createTicket(p, message);
//                    lib.createTicketDEV(p, message, 50);

                    // Clear the StringBuilder, preventing for getting a messega from a other ticket
                    sb.delete(0, sb.length());

                    // Open ticket subject GUI
                    ticket.subject_UI(p);
                }

            } else if (args[0].equalsIgnoreCase("status")) {

                if (lib.isStaff(p.getUniqueId())) {
                    // Check if the staff has any assigned tickets
                    if (!lib.staffHasAssignedTickets(p)) {
                        p.sendMessage(Strings.prefix() + Strings.gray + "You don't have any claimed tickets right now!");
                        return true;
                    }

                    AssignedTickets_UI a = new AssignedTickets_UI();
                    a.assignedTickets(p);
                    return true;
                }

                if (p.hasPermission("realtimesupport.status")) {

                    // Check if the player has any tickets
                    if (!lib.playerHasTickets(p)) {
                        p.sendMessage(Strings.red + "You don't have any tickets right now!");
                        return true;
                    }

                    TicketStatus_UI.chooseTicket_UI(p);
                }
            } else if (args[0].equalsIgnoreCase("tickets")) {
                if (lib.isStaff(p.getUniqueId())) {
                    Tickets_UI.filter.putIfAbsent(p, false);
                    opnTicketsInv.openTickets_UI(p);
                }

            } else if (args[0].equalsIgnoreCase("info")) {
                if (lib.isStaff(p.getUniqueId())) {
                    p.sendMessage(Strings.DgrayBS + "                             \n" +
                            Strings.gray + "Total Ticket's solved: " + Strings.white + lib.staffTotalTicketSolved(p) + "\n" +
                            Strings.gray + "Average rating: " + Strings.white + rs.calculateAVG(p) + "\n" +
                            Strings.DgrayBS + "                             \n");
                }

            } else if (args[0].equalsIgnoreCase("help")) {
                if (p.hasPermission("realtimesupport.help")) {
                    p.sendMessage(Strings.DgrayBS + "                " + Strings.greenB + " RealTime Support " + Strings.DgrayBS + "                \n" +
                            Strings.reset + " \n" +
                            Strings.gold + "/rs create <message> " + Strings.gray + "create a new Ticket\n" +
                            Strings.gold + "/rs status " + Strings.gray + "see the information about your Ticket\n" +
                            Strings.gold + "/rs info " + Strings.gray + "analytics information\n" +
                            Strings.gold + "/rs tickets " + Strings.gray + "main menu for the staff\n" +
                            Strings.gold + "/rs purgeall " + Strings.gray + "purge all solved tickets that exist more than 24 hours\n" +
                            Strings.gold + "/rs staff add <player> " + Strings.gray + "add a player to your staff\n" +
                            Strings.gold + "/rs staff remove <player> " + Strings.gray + "remove a player form your staff\n" +
                            " \n");
                }
            } else if (args[0].equalsIgnoreCase("purgeall")) {
                if (p.hasPermission("realtimesupport.admin")) {
                    pts.purgeTickets();
                    p.sendMessage(Strings.prefix() + Strings.gray + "You succsessfully purged " + Strings.green + pts.sessionPurgedTickets() + Strings.gray + " Ticket(s)!");
                }
            } else if (args[0].equalsIgnoreCase("staff")) {
                if (p.hasPermission("realtimesupport.admin")) {

                    if (args.length == 1) {
                        p.sendMessage(Strings.prefix() + Strings.gray + "Command usage: /rs staff add/remove <player>");
                        return true;
                    }
                    String a = args[1];

                    if (a.equals("add")) {
                        // if the player forget to put in a player
                        if (args.length == 2) {
                            p.sendMessage(Strings.prefix() + Strings.gray + "specify a player");
                            return true;
                        }

                        String b = args[2];
                        Player p1 = Bukkit.getPlayer(b);

                        // Check if the player is real
                        if (p1 == null) {
                            p.sendMessage(Strings.prefix() + Strings.red + "Player is not valid or online!");
                            return true;
                        }

                        if (lib.isStaff(p1.getUniqueId())) {
                            p.sendMessage(Strings.prefix() + Strings.red + "That player is already staff!");
                            return true;
                        }
                        lib.staffAddPlayer(p1.getUniqueId());
                        p.sendMessage(Strings.prefix() + Strings.green + "You succssesfully added the player to the staff");

                    } else if (a.equals("remove")) {
                        // if the player forget to put in a player
                        if (args.length == 2) {
                            p.sendMessage(Strings.prefix() + Strings.gray + "specify a player");
                            return true;
                        }

                        String b = args[2];
                        Player p1 = Bukkit.getPlayer(b);

                        // Check if the player is real
                        if (p1 == null) {
                            p.sendMessage(Strings.prefix() + Strings.red + "Player is not valid or online!");
                            return true;
                        }

                        if (!lib.isStaff(p1.getUniqueId())) {
                            p.sendMessage(Strings.prefix() + Strings.red + "That player is not a staff!");
                            return true;
                        }

                        lib.staffRemovePlayer(p1.getUniqueId());
                        p.sendMessage(Strings.prefix() + Strings.green + "You succssesfully remove the player from the staff");
                    }
                }
            } else if (args[0].equalsIgnoreCase("close")) {
                if (p.hasPermission("realtimesupport")) {

                    if (args.length == 1) {
                        p.sendMessage(Strings.red + "Please enter a Ticket ID!");
                        return true;
                    }

                    // Check if the player has a Ticket at the moment
                    int i = Integer.parseInt(args[1]);

                    if (lib.isStaff(p.getUniqueId())) {
                        Player p1 = (Player) lib.getTicketCreatedByPlayer(i);
                        NotificationSerivce.closeTicketConfirmation(p1, i);
                        return true;
                    }

                    if (!lib.hasTicketID(p, i)) {
                        p.sendMessage(Strings.red + "You don't have that Ticket ID.");
                        return true;
                    }


                    lib.closeTicket(p, i);

                }

            } else if (args[0].equalsIgnoreCase("reload")) {
                if (p.hasPermission("realtimesupport.reload")) {
                    TicketLoadService tls = new TicketLoadService();
                    lib.configVersionUpdate();
                    plugin.Load();
                    tls.loadTickets();
                    p.sendMessage(Strings.prefix() + Strings.gray + "Plugin succssesfully reloaded!");
                }
            }

        }

        return true;
    }

}
