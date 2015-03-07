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
        sender.sendMessage(enc().append("                  ------ BungeePerms - Help - Page " + (page + 1) + " -----").color(ChatColor.GOLD));
        sender.sendMessage(enc().append("Aliases: ").color(ChatColor.GRAY).append("/bp").color(ChatColor.GOLD));
    }

    public static void sendHelpPage(Sender sender, int page)
    {
        sendHelpHeader(sender, page);

        int index = -1;
        for (HelpEntry he : helpentries)
        {
            if (he.getPermission() != null && !BungeePerms.getInstance().getPermissionsChecker().hasPermOrConsole(sender, he.getPermission()))
            {
                continue;
            }

            index++;
            if (index < page * PAGE_SIZE)
            {
                continue;
            }
            else if (index < (page + 1) * PAGE_SIZE)
            {
                sender.sendMessage(he.getMessage());
            }
            else
            {
                break;
            }
        }
    }

    @Getter
    @AllArgsConstructor
    private static class HelpEntry
    {

        private final String permission;
        private final MessageEncoder message;
    }
}
