package net.alpenblock.bungeeperms;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.alpenblock.bungeeperms.platform.MessageEncoder;
import net.alpenblock.bungeeperms.platform.PlatformPlugin;
import net.alpenblock.bungeeperms.platform.Sender;

public class HelpProvider
{

    private static final int PAGE_SIZE = 7;
    private static final List<HelpEntry> helpentries = new ArrayList<>();

    static
    {
        helpentries.add(new HelpEntry(null, enc().append("/bp").color(ChatColor.GOLD).append(" - ").color(ChatColor.WHITE).append("Welcomes you to BungeePerms").color(ChatColor.GRAY)));
        helpentries.add(new HelpEntry("bungeeperms.help", enc().append("/bp help [page]").color(ChatColor.GOLD).append(" - ").color(ChatColor.WHITE).append("Shows this help").color(ChatColor.GRAY)));
        helpentries.add(new HelpEntry("bungeeperms.reload", enc().append("/bp reload").color(ChatColor.GOLD).append(" - ").color(ChatColor.WHITE).append("Reloads the plugin").color(ChatColor.GRAY)));
        helpentries.add(new HelpEntry("bungeeperms.debug", enc().append("/bp debug <true|false>").color(ChatColor.GOLD).append(" - ").color(ChatColor.WHITE).append("En-/Disables the debug mode").color(ChatColor.GRAY)));
        helpentries.add(new HelpEntry("bungeeperms.users", enc().append("/bp users [-c]").color(ChatColor.GOLD).append(" - ").color(ChatColor.WHITE).append("Lists the users [or shows the amount]").color(ChatColor.GRAY)));
        helpentries.add(new HelpEntry("bungeeperms.user.info", enc().append("/bp user <user> info").color(ChatColor.GOLD).append(" - ").color(ChatColor.WHITE).append("Shows information about the user").color(ChatColor.GRAY)));
        helpentries.add(new HelpEntry("bungeeperms.user.delete", enc().append("/bp user <user> delete").color(ChatColor.GOLD).append(" - ").color(ChatColor.WHITE).append("Deletes the user").color(ChatColor.GRAY)));
        helpentries.add(new HelpEntry("bungeeperms.user.display", enc().append("/bp user <user> display [displayname [server [world]]]").color(ChatColor.GOLD).append(" - ").color(ChatColor.WHITE).append("Sets the display name for the user").color(ChatColor.GRAY)));
        helpentries.add(new HelpEntry("bungeeperms.user.prefix", enc().append("/bp user <user> prefix [prefix [server [world]]]").color(ChatColor.GOLD).append(" - ").color(ChatColor.WHITE).append("Sets the prefix name for the user").color(ChatColor.GRAY)));
        helpentries.add(new HelpEntry("bungeeperms.user.suffix", enc().append("/bp user <user> suffix [suffix [server [world]]]").color(ChatColor.GOLD).append(" - ").color(ChatColor.WHITE).append("Sets the suffix for the user").color(ChatColor.GRAY)));
        helpentries.add(new HelpEntry("bungeeperms.user.perms.add", enc().append("/bp user <user> addperm <perm> [server [world]]").color(ChatColor.GOLD).append(" - ").color(ChatColor.WHITE).append("Adds a permission to the user").color(ChatColor.GRAY)));
        helpentries.add(new HelpEntry("bungeeperms.user.perms.remove", enc().append("/bp user <user> removeperm <perm> [server [world]]").color(ChatColor.GOLD).append(" - ").color(ChatColor.WHITE).append("Removes a permission from a the user").color(ChatColor.GRAY)));
        helpentries.add(new HelpEntry("bungeeperms.user.perms.has", enc().append("/bp user <user> has <perm> [server [world]]").color(ChatColor.GOLD).append(" - ").color(ChatColor.WHITE).append("Checks if the user has the permission").color(ChatColor.GRAY)));
        helpentries.add(new HelpEntry("bungeeperms.user.perms.list", enc().append("/bp user <user> list").color(ChatColor.GOLD).append(" - ").color(ChatColor.WHITE).append("Lists the permissions of the user").color(ChatColor.GRAY)));
        helpentries.add(new HelpEntry("bungeeperms.user.group.add", enc().append("/bp user <user> addgroup <group>").color(ChatColor.GOLD).append(" - ").color(ChatColor.WHITE).append("Add the group to the user").color(ChatColor.GRAY)));
        helpentries.add(new HelpEntry("bungeeperms.user.group.remove", enc().append("/bp user <user> removegroup <group>").color(ChatColor.GOLD).append(" - ").color(ChatColor.WHITE).append("Removes the group from the user").color(ChatColor.GRAY)));
        helpentries.add(new HelpEntry("bungeeperms.user.group.set", enc().append("/bp user <user> setgroup <group>").color(ChatColor.GOLD).append(" - ").color(ChatColor.WHITE).append("Removes the old group in the group's ladder and adds the group to the user").color(ChatColor.GRAY)));
        helpentries.add(new HelpEntry("bungeeperms.user.groups", enc().append("/bp user <user> groups").color(ChatColor.GOLD).append(" - ").color(ChatColor.WHITE).append("Lists the groups the user is in").color(ChatColor.GRAY)));
        helpentries.add(new HelpEntry("bungeeperms.groups", enc().append("/bp groups").color(ChatColor.GOLD).append(" - ").color(ChatColor.WHITE).append("Lists the groups").color(ChatColor.GRAY)));
        helpentries.add(new HelpEntry("bungeeperms.group.info", enc().append("/bp group <group> info").color(ChatColor.GOLD).append(" - ").color(ChatColor.WHITE).append("Shows information about the group").color(ChatColor.GRAY)));
        helpentries.add(new HelpEntry("bungeeperms.group.users", enc().append("/bp group <group> users [-c]").color(ChatColor.GOLD).append(" - ").color(ChatColor.WHITE).append("Lists the users of the group [or shows the amount]").color(ChatColor.GRAY)));
        helpentries.add(new HelpEntry("bungeeperms.group.create", enc().append("/bp group <group> create").color(ChatColor.GOLD).append(" - ").color(ChatColor.WHITE).append("Create a new group").color(ChatColor.GRAY)));
        helpentries.add(new HelpEntry("bungeeperms.group.delete", enc().append("/bp group <group> delete").color(ChatColor.GOLD).append(" - ").color(ChatColor.WHITE).append("Deletes the group").color(ChatColor.GRAY)));
        helpentries.add(new HelpEntry("bungeeperms.group.inheritances.add", enc().append("/bp group <group> addinherit <addgroup>").color(ChatColor.GOLD).append(" - ").color(ChatColor.WHITE).append("Adds the addgroup to the group as inheritance").color(ChatColor.GRAY)));
        helpentries.add(new HelpEntry("bungeeperms.group.inheritances.remove", enc().append("/bp group <group> removeinherit <removegroup>").color(ChatColor.GOLD).append(" - ").color(ChatColor.WHITE).append("Removes the removegroup from the group as inheritance").color(ChatColor.GRAY)));
        helpentries.add(new HelpEntry("bungeeperms.group.rank", enc().append("/bp group <group> rank <new rank>").color(ChatColor.GOLD).append(" - ").color(ChatColor.WHITE).append("Sets the rank for the group").color(ChatColor.GRAY)));
        helpentries.add(new HelpEntry("bungeeperms.group.weight", enc().append("/bp group <group> weight <new weight>").color(ChatColor.GOLD).append(" - ").color(ChatColor.WHITE).append("Sets the weight for the group").color(ChatColor.GRAY)));
        helpentries.add(new HelpEntry("bungeeperms.group.ladder", enc().append("/bp group <group> ladder <new ladder>").color(ChatColor.GOLD).append(" - ").color(ChatColor.WHITE).append("Sets the ladder for the group").color(ChatColor.GRAY)));
        helpentries.add(new HelpEntry("bungeeperms.group.default", enc().append("/bp group <group> default <true|false>").color(ChatColor.GOLD).append(" - ").color(ChatColor.WHITE).append("Determines whether the group is a default group or not").color(ChatColor.GRAY)));
        helpentries.add(new HelpEntry("bungeeperms.group.display", enc().append("/bp group <group> display [displayname [server [world]]]").color(ChatColor.GOLD).append(" - ").color(ChatColor.WHITE).append("Sets the display name for the group").color(ChatColor.GRAY)));
        helpentries.add(new HelpEntry("bungeeperms.group.prefix", enc().append("/bp group <group> prefix [prefix [server [world]]]").color(ChatColor.GOLD).append(" - ").color(ChatColor.WHITE).append("Sets the prefix for the group").color(ChatColor.GRAY)));
        helpentries.add(new HelpEntry("bungeeperms.group.suffix", enc().append("/bp group <group> suffix [suffix [server [world]]]").color(ChatColor.GOLD).append(" - ").color(ChatColor.WHITE).append("Sets the suffix for the group").color(ChatColor.GRAY)));
        helpentries.add(new HelpEntry("bungeeperms.group.perms.add", enc().append("/bp group <group> addperm <perm> [server [world]]").color(ChatColor.GOLD).append(" - ").color(ChatColor.WHITE).append("Adds a permission to the group").color(ChatColor.GRAY)));
        helpentries.add(new HelpEntry("bungeeperms.group.perms.remove", enc().append("/bp group <group> removeperm <perm> [server [world]]").color(ChatColor.GOLD).append(" - ").color(ChatColor.WHITE).append("Removes a permission from the group").color(ChatColor.GRAY)));
        helpentries.add(new HelpEntry("bungeeperms.group.perms.has", enc().append("/bp group <group> has <perm> [server [world]]").color(ChatColor.GOLD).append(" - ").color(ChatColor.WHITE).append("Checks if the group has the permission").color(ChatColor.GRAY)));
        helpentries.add(new HelpEntry("bungeeperms.group.perms.list", enc().append("/bp group <group> list").color(ChatColor.GOLD).append(" - ").color(ChatColor.WHITE).append("Lists the permissions of the group").color(ChatColor.GRAY)));
        helpentries.add(new HelpEntry("bungeeperms.promote", enc().append("/bp promote <user> [ladder]").color(ChatColor.GOLD).append(" - ").color(ChatColor.WHITE).append("Promotes the user to the next rank").color(ChatColor.GRAY)));
        helpentries.add(new HelpEntry("bungeeperms.demote", enc().append("/bp demote <user> [ladder]").color(ChatColor.GOLD).append(" - ").color(ChatColor.WHITE).append("Demotes the user to the previous rank").color(ChatColor.GRAY)));
        helpentries.add(new HelpEntry("bungeeperms.format", enc().append("/bp format").color(ChatColor.GOLD).append(" - ").color(ChatColor.WHITE).append("Reformates the permission.yml or mysql table - ").color(ChatColor.GRAY).append("BE CAREFUL").color(ChatColor.RED)));
        helpentries.add(new HelpEntry("bungeeperms.cleanup", enc().append("/bp cleanup").color(ChatColor.GOLD).append(" - ").color(ChatColor.WHITE).append("Cleans up the permission.yml or mysql table - ").color(ChatColor.GRAY).append("!BE VERY CAREFUL! - removes a lot of players from the permissions db if configured").color(ChatColor.RED)));
        helpentries.add(new HelpEntry("bungeeperms.migrate", enc().append("/bp migrate <backend [yaml|mysql|mysql2]>").color(ChatColor.GOLD).append(" - ").color(ChatColor.WHITE).append("Migrates the backend or shows status - ").color(ChatColor.GRAY).append("!BE CAREFUL! (MAKE A BACKUP BEFORE EXECUTING)").color(ChatColor.RED)));
        helpentries.add(new HelpEntry("bungeeperms.migrate", enc().append("/bp migrate <useuuid [true|false]>").color(ChatColor.GOLD).append(" - ").color(ChatColor.WHITE).append("Migrates backends to (not) use UUIDs or shows status - ").color(ChatColor.GRAY).append("!BE CAREFUL! (MAKE A BACKUP BEFORE EXECUTING)").color(ChatColor.RED)));
        helpentries.add(new HelpEntry("bungeeperms.migrate", enc().append("/bp migrate <uuidplayerdb [None|YAML|MySQL]>").color(ChatColor.GOLD).append(" - ").color(ChatColor.WHITE).append("Migrates UUID-player-databases or shows status - ").color(ChatColor.GRAY).append("!BE CAREFUL! (MAKE A BACKUP BEFORE EXECUTING)").color(ChatColor.RED)));
        helpentries.add(new HelpEntry("bungeeperms.uuid", enc().append("/bp uuid <player|uuid> [-rm]").color(ChatColor.GOLD).append(" - ").color(ChatColor.WHITE).append("Gets the UUID of a player from database (-r: reverse; -m: ask mojang)").color(ChatColor.GRAY)));
// template        helpentries.add(new HelpEntry(null, enc().append("/bp help").color(ChatColor.GOLD).append(" - ").color(ChatColor.WHITE).append("Shows").color(ChatColor.GRAY)));
    }

    private static PlatformPlugin plugin()
    {
        return BungeePerms.getInstance().getPlugin();
    }

    private static MessageEncoder enc()
    {
        return plugin().newMessageEncoder();
    }

    public static void sendHelpHeader(Sender sender, int page)
    {
        sender.sendMessage(enc().append("                  ------ BungeePerms - Help - Page " + (page+1) + " -----").color(ChatColor.GOLD));
        sender.sendMessage(enc().append("Aliases: ").color(ChatColor.GRAY).append("/bp").color(ChatColor.GOLD));
    }

    public static void sendHelpPage(Sender sender, int page)
    {
        sendHelpHeader(sender, page);

        int index = 0;
        for (HelpEntry he : helpentries)
        {
            if (!BungeePerms.getInstance().getPermissionsChecker().hasPermOrConsole(sender, he.getPermission()))
            {
                System.out.println("no perm" + he.getPermission());
                continue;
            }

            index++;
            if ((page + 1) * PAGE_SIZE >= index && index > page * PAGE_SIZE)
            {
                sender.sendMessage(he.getMessage());
            }
        }
    }
//    public static void m(Sender sender)
//    {
//        sender.sendMessage(ChatColor.GOLD + "/bungeeperms" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Welcomes you to BungeePerms");
//        if (checker.hasPermOrConsole(sender, "bungeeperms.help"))
//        {
//            sender.sendMessage(ChatColor.GOLD + "/bungeeperms help" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Shows this help");
//        }
//        if (checker.hasPermOrConsole(sender, "bungeeperms.reload"))
//        {a
//            sender.sendMessage(ChatColor.GOLD + "/bungeeperms reload" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Reloads the permissions");
//        }
//        if (checker.hasPermOrConsole(sender, "bungeeperms.users"))
//        {
//            sender.sendMessage(ChatColor.GOLD + "/bungeeperms users [-c]" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Lists the users [or shows the amount of them]");
//        }
//        if (checker.hasPermOrConsole(sender, "bungeeperms.user.info"))
//        {
//            sender.sendMessage(ChatColor.GOLD + "/bungeeperms user <username> info" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Shows information to the given user");
//        }
//        if (checker.hasPermOrConsole(sender, "bungeeperms.user.delete"))
//        {
//            sender.sendMessage(ChatColor.GOLD + "/bungeeperms user <username> delete" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Deletes the given user");
//        }
//        if (checker.hasPermOrConsole(sender, "bungeeperms.user.display"))
//        {
//            sender.sendMessage(ChatColor.GOLD + "/bungeeperms user <username> display [displayname [server [world]]]" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Sets the display name for the given user");
//        }
//        if (checker.hasPermOrConsole(sender, "bungeeperms.user.prefix"))
//        {
//            sender.sendMessage(ChatColor.GOLD + "/bungeeperms user <username> prefix [prefix [server [world]]]" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Sets the prefix for the given user");
//        }
//        if (checker.hasPermOrConsole(sender, "bungeeperms.user.suffix"))
//        {
//            sender.sendMessage(ChatColor.GOLD + "/bungeeperms user <username> suffix [suffix [server [world]]]" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Sets the suffix for the given user");
//        }
//        if (checker.hasPermOrConsole(sender, "bungeeperms.user.perms.add"))
//        {
//            sender.sendMessage(ChatColor.GOLD + "/bungeeperms user <username> addperm <permission> [server [world]]" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Adds a permission to the given user");
//        }
//        if (checker.hasPermOrConsole(sender, "bungeeperms.user.perms.remove"))
//        {
//            sender.sendMessage(ChatColor.GOLD + "/bungeeperms user <username> removeperm <permission> [server [world]]" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Remove a permission from the given user");
//        }
//        if (checker.hasPermOrConsole(sender, "bungeeperms.user.perms.has"))
//        {
//            sender.sendMessage(ChatColor.GOLD + "/bungeeperms user <username> has <permission> [server [world]]" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Checks if the given user has the given permission");
//        }
//        if (checker.hasPermOrConsole(sender, "bungeeperms.user.perms.list"))
//        {
//            sender.sendMessage(ChatColor.GOLD + "/bungeeperms user <username> list" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Lists the permissions of the given user");
//        }
//        if (checker.hasPermOrConsole(sender, "bungeeperms.user.group.add"))
//        {
//            sender.sendMessage(ChatColor.GOLD + "/bungeeperms user <username> addgroup <groupname>" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Added the given group to the given user");
//        }
//        if (checker.hasPermOrConsole(sender, "bungeeperms.user.group.remove"))
//        {
//            sender.sendMessage(ChatColor.GOLD + "/bungeeperms user <username> removegroup <groupname>" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Removes the given group from the given user");
//        }
//        if (checker.hasPermOrConsole(sender, "bungeeperms.user.group.set"))
//        {
//            sender.sendMessage(ChatColor.GOLD + "/bungeeperms user <username> setgroup <groupname>" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Sets the given group as the main group for the given user");
//        }
//        if (checker.hasPermOrConsole(sender, "bungeeperms.user.groups"))
//        {
//            sender.sendMessage(ChatColor.GOLD + "/bungeeperms user <username> groups" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Lists the groups the given user is in");
//        }
//        if (checker.hasPermOrConsole(sender, "bungeeperms.groups"))
//        {
//            sender.sendMessage(ChatColor.GOLD + "/bungeeperms groups" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Lists the groups");
//        }
//        if (checker.hasPermOrConsole(sender, "bungeeperms.group.info"))
//        {
//            sender.sendMessage(ChatColor.GOLD + "/bungeeperms group <groupname> info" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Shows information about the given group");
//        }
//        if (checker.hasPermOrConsole(sender, "bungeeperms.group.users"))
//        {
//            sender.sendMessage(ChatColor.GOLD + "/bungeeperms group <groupname> users [-c]" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Lists the users of the given group [or shows the amount of them]");
//        }
//        if (checker.hasPermOrConsole(sender, "bungeeperms.group.create"))
//        {
//            sender.sendMessage(ChatColor.GOLD + "/bungeeperms group <groupname> create" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Create a group with the given name");
//        }
//        if (checker.hasPermOrConsole(sender, "bungeeperms.group.delete"))
//        {
//            sender.sendMessage(ChatColor.GOLD + "/bungeeperms group <groupname> delete" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Create the given group");
//        }
//        if (checker.hasPermOrConsole(sender, "bungeeperms.group.inheritances.add"))
//        {
//            sender.sendMessage(ChatColor.GOLD + "/bungeeperms group <groupname> addinherit <group>" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Adds a inheritance to the given group");
//        }
//        if (checker.hasPermOrConsole(sender, "bungeeperms.group.inheritances.remove"))
//        {
//            sender.sendMessage(ChatColor.GOLD + "/bungeeperms group <groupname> removeinherit <group>" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Remove a inheritance from the given group");
//        }
//        if (checker.hasPermOrConsole(sender, "bungeeperms.group.rank"))
//        {
//            sender.sendMessage(ChatColor.GOLD + "/bungeeperms group <groupname> rank <new rank>" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Sets the rank for the given group");
//        }
//        if (checker.hasPermOrConsole(sender, "bungeeperms.group.weight"))
//        {
//            sender.sendMessage(ChatColor.GOLD + "/bungeeperms group <groupname> weight <new weight>" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Sets the weight for the given group");
//        }
//        if (checker.hasPermOrConsole(sender, "bungeeperms.group.ladder"))
//        {
//            sender.sendMessage(ChatColor.GOLD + "/bungeeperms group <groupname> ladder <new ladder>" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Sets the ladder for the given group");
//        }
//        if (checker.hasPermOrConsole(sender, "bungeeperms.group.default"))
//        {
//            sender.sendMessage(ChatColor.GOLD + "/bungeeperms group <groupname> default <true|false>" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Determines whether the given group is a default group or not");
//        }
//        if (checker.hasPermOrConsole(sender, "bungeeperms.group.display"))
//        {
//            sender.sendMessage(ChatColor.GOLD + "/bungeeperms group <groupname> display [displayname [server [world]]]" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Sets the display name for the given group");
//        }
//        if (checker.hasPermOrConsole(sender, "bungeeperms.group.prefix"))
//        {
//            sender.sendMessage(ChatColor.GOLD + "/bungeeperms group <groupname> prefix [prefix [server [world]]]" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Sets the prefix for the given group");
//        }
//        if (checker.hasPermOrConsole(sender, "bungeeperms.group.suffix"))
//        {
//            sender.sendMessage(ChatColor.GOLD + "/bungeeperms group <groupname> suffix [suffix [server [world]]]" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Sets the suffix for the given group");
//        }
//        if (checker.hasPermOrConsole(sender, "bungeeperms.group.perms.add"))
//        {
//            sender.sendMessage(ChatColor.GOLD + "/bungeeperms group <groupname> addperm <permission> [server [world]]" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Adds a permission to the given group");
//        }
//        if (checker.hasPermOrConsole(sender, "bungeeperms.group.perms.remove"))
//        {
//            sender.sendMessage(ChatColor.GOLD + "/bungeeperms group <groupname> removeperm <permission> [server [world]]" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Remove a permission from the given group");
//        }
//        if (checker.hasPermOrConsole(sender, "bungeeperms.group.perms.has"))
//        {
//            sender.sendMessage(ChatColor.GOLD + "/bungeeperms group <groupname> has <permission> [server [world]]" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Checks if the given group has the given permission");
//        }
//        if (checker.hasPermOrConsole(sender, "bungeeperms.group.perms.list"))
//        {
//            sender.sendMessage(ChatColor.GOLD + "/bungeeperms group <groupname> list" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Lists the permissions of the given group");
//        }
//        if (checker.hasPermOrConsole(sender, "bungeeperms.promote"))
//        {
//            sender.sendMessage(ChatColor.GOLD + "/bungeeperms promote <username> [ladder]" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Promotes the given user to the next rank");
//        }
//        if (checker.hasPermOrConsole(sender, "bungeeperms.demote"))
//        {
//            sender.sendMessage(ChatColor.GOLD + "/bungeeperms demote <username> [ladder]" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Demotes the given user to the previous rank");
//        }
//        if (checker.hasPermOrConsole(sender, "bungeeperms.format"))
//        {
//            sender.sendMessage(ChatColor.GOLD + "/bungeeperms format" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Reformates the permission.yml or mysql table - " + ChatColor.RED + " BE CAREFUL");
//        }
//        if (checker.hasPermOrConsole(sender, "bungeeperms.cleanup"))
//        {
//            sender.sendMessage(ChatColor.GOLD + "/bungeeperms cleanup" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Cleans up the permission.yml or mysql table - " + ChatColor.RED + " !BE VERY CAREFUL! - removes a lot of players from the permissions.yml if configured");
//        }
//        if (checker.hasPermOrConsole(sender, "bungeeperms.migrate"))
//        {
//            sender.sendMessage(ChatColor.GOLD + "/bungeeperms migrate <backend [yaml|mysql|mysql2]|useuuid [true|false]|uuidplayerdb [None|YAML|MySQL]>" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Does migrations of different data (permissions, uuid) or shows status - " + ChatColor.RED + " !BE CAREFUL! (MAKE A BACKUP BEFORE EXECUTING)");
//        }
//        if (checker.hasPermOrConsole(sender, "bungeeperms.uuid"))
//        {
//            sender.sendMessage(ChatColor.GOLD + "/bungeeperms uuid <player|uuid> [-rm]" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Gets the UUID of a player from database (-r: reverse; -m: ask mojang)");
//        }
//        sender.sendMessage(ChatColor.GOLD + "---------------------------------------------------");
//    }

    @Getter
    @AllArgsConstructor
    private static class HelpEntry
    {

        private final String permission;
        private final MessageEncoder message;
    }
}
