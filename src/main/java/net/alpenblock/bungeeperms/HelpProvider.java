package net.alpenblock.bungeeperms;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.alpenblock.bungeeperms.platform.MessageEncoder;
import net.alpenblock.bungeeperms.platform.MessageEncoder.ClickEvent;
import net.alpenblock.bungeeperms.platform.MessageEncoder.HoverEvent;
import net.alpenblock.bungeeperms.platform.PlatformPlugin;
import net.alpenblock.bungeeperms.platform.Sender;

public class HelpProvider
{

    private static final int PAGE_SIZE = 7;
    private static final List<HelpEntry> helpentries = new ArrayList<>();

    static
    {
        helpentries.add(new HelpEntry(null,/*                                   */ makeClickCommand("/bp", "Welcomes you to BungeePerms")));
        helpentries.add(new HelpEntry("bungeeperms.help",/*                     */ makeSuggestCommand("/bp help [page]", "Shows this help")));
        helpentries.add(new HelpEntry("bungeeperms.reload",/*                   */ makeClickCommand("/bp reload", "Reloads the plugin")));
        helpentries.add(new HelpEntry("bungeeperms.debug",/*                    */ makeSuggestCommand("/bp debug <true|false>", "En-/Disables the debug mode")));
        helpentries.add(new HelpEntry("bungeeperms.users",/*                    */ makeSuggestCommand("/bp users [-c]", "Lists the users [or shows the amount]")));
        helpentries.add(new HelpEntry("bungeeperms.user.info",/*                */ makeSuggestCommand("/bp user <user> info", "Shows information about the user")));
        helpentries.add(new HelpEntry("bungeeperms.user.delete",/*              */ makeSuggestCommand("/bp user <user> delete", "Deletes the user")));
        helpentries.add(new HelpEntry("bungeeperms.user.display",/*             */ makeSuggestCommand("/bp user <user> display [displayname [server [world]]]", "Sets the display name for the user")));
        helpentries.add(new HelpEntry("bungeeperms.user.prefix",/*              */ makeSuggestCommand("/bp user <user> prefix [prefix [server [world]]]", "Sets the prefix name for the user")));
        helpentries.add(new HelpEntry("bungeeperms.user.suffix",/*              */ makeSuggestCommand("/bp user <user> suffix [suffix [server [world]]]", "Sets the suffix for the user")));
        helpentries.add(new HelpEntry("bungeeperms.user.perms.add",/*           */ makeSuggestCommand("/bp user <user> addperm <perm> [server [world]]", "Adds a permission to the user")));
        helpentries.add(new HelpEntry("bungeeperms.user.perms.remove",/*        */ makeSuggestCommand("/bp user <user> removeperm <perm> [server [world]]", "Removes a permission from a the user")));
        helpentries.add(new HelpEntry("bungeeperms.user.perms.has",/*           */ makeSuggestCommand("/bp user <user> has <perm> [server [world]]", "Checks if the user has the permission")));
        helpentries.add(new HelpEntry("bungeeperms.user.perms.list",/*          */ makeSuggestCommand("/bp user <user> list", "Lists the permissions of the user")));
        helpentries.add(new HelpEntry("bungeeperms.user.group.add",/*           */ makeSuggestCommand("/bp user <user> addgroup <group>", "Add the group to the user")));
        helpentries.add(new HelpEntry("bungeeperms.user.group.remove",/*        */ makeSuggestCommand("/bp user <user> removegroup <group>", "Removes the group from the user")));
        helpentries.add(new HelpEntry("bungeeperms.user.group.set",/*           */ makeSuggestCommand("/bp user <user> setgroup <group>", "Removes the old group in the group's ladder and adds the group to the user")));
        helpentries.add(new HelpEntry("bungeeperms.user.groups",/*              */ makeSuggestCommand("/bp user <user> groups", "Lists the groups the user is in")));
        helpentries.add(new HelpEntry("bungeeperms.groups",/*                   */ makeClickCommand("/bp groups", "Lists the groups")));
        helpentries.add(new HelpEntry("bungeeperms.group.info",/*               */ makeSuggestCommand("/bp group <group> info", "Shows information about the group")));
        helpentries.add(new HelpEntry("bungeeperms.group.users",/*              */ makeSuggestCommand("/bp group <group> users [-c]", "Lists the users of the group [or shows the amount]")));
        helpentries.add(new HelpEntry("bungeeperms.group.create",/*             */ makeSuggestCommand("/bp group <group> create", "Create a new group")));
        helpentries.add(new HelpEntry("bungeeperms.group.delete",/*             */ makeSuggestCommand("/bp group <group> delete", "Deletes the group")));
        helpentries.add(new HelpEntry("bungeeperms.group.inheritances.add",/*   */ makeSuggestCommand("/bp group <group> addinherit <addgroup>", "Adds the addgroup to the group as inheritance")));
        helpentries.add(new HelpEntry("bungeeperms.group.inheritances.remove",/**/ makeSuggestCommand("/bp group <group> removeinherit <removegroup>", "Removes the removegroup from the group as inheritance")));
        helpentries.add(new HelpEntry("bungeeperms.group.rank",/*               */ makeSuggestCommand("/bp group <group> rank <new rank>", "Sets the rank for the group")));
        helpentries.add(new HelpEntry("bungeeperms.group.weight",/*             */ makeSuggestCommand("/bp group <group> weight <new weight>", "Sets the weight for the group")));
        helpentries.add(new HelpEntry("bungeeperms.group.ladder",/*             */ makeSuggestCommand("/bp group <group> ladder <new ladder>", "Sets the ladder for the group")));
        helpentries.add(new HelpEntry("bungeeperms.group.default",/*            */ makeSuggestCommand("/bp group <group> default <true|false>", "Determines whether the group is a default group or not")));
        helpentries.add(new HelpEntry("bungeeperms.group.display",/*            */ makeSuggestCommand("/bp group <group> display [displayname [server [world]]]", "Sets the display name for the group")));
        helpentries.add(new HelpEntry("bungeeperms.group.prefix",/*             */ makeSuggestCommand("/bp group <group> prefix [prefix [server [world]]]", "Sets the prefix for the group")));
        helpentries.add(new HelpEntry("bungeeperms.group.suffix",/*             */ makeSuggestCommand("/bp group <group> suffix [suffix [server [world]]]", "Sets the suffix for the group")));
        helpentries.add(new HelpEntry("bungeeperms.group.perms.add",/*          */ makeSuggestCommand("/bp group <group> addperm <perm> [server [world]]", "Adds a permission to the group")));
        helpentries.add(new HelpEntry("bungeeperms.group.perms.remove",/*       */ makeSuggestCommand("/bp group <group> removeperm <perm> [server [world]]", "Removes a permission from the group")));
        helpentries.add(new HelpEntry("bungeeperms.group.perms.has",/*          */ makeSuggestCommand("/bp group <group> has <perm> [server [world]]", "Checks if the group has the permission")));
        helpentries.add(new HelpEntry("bungeeperms.group.perms.list",/*         */ makeSuggestCommand("/bp group <group> list", "Lists the permissions of the group")));
        helpentries.add(new HelpEntry("bungeeperms.promote",/*                  */ makeSuggestCommand("/bp promote <user> [ladder]", "Promotes the user to the next rank")));
        helpentries.add(new HelpEntry("bungeeperms.demote",/*                   */ makeSuggestCommand("/bp demote <user> [ladder]", "Demotes the user to the previous rank")));
        helpentries.add(new HelpEntry("bungeeperms.format",/*                   */ makeClickCommand("/bp format", "Reformates the permission.yml or mysql table - ").append("BE CAREFUL").color(ChatColor.RED)));
        helpentries.add(new HelpEntry("bungeeperms.cleanup",/*                  */ makeClickCommand("/bp cleanup", "Cleans up the permission.yml or mysql table - ").append("!BE VERY CAREFUL! - removes a lot of players from the permissions db if configured").color(ChatColor.RED)));
        helpentries.add(new HelpEntry("bungeeperms.migrate",/*                  */ makeSuggestCommand("/bp migrate <backend [yaml|mysql|mysql2]>", "Migrates the backend or shows status - ").append("!BE CAREFUL! (MAKE A BACKUP BEFORE EXECUTING)").color(ChatColor.RED)));
        helpentries.add(new HelpEntry("bungeeperms.migrate",/*                  */ makeSuggestCommand("/bp migrate <useuuid [true|false]>", "Migrates backends to (not) use UUIDs or shows status - ").append("!BE CAREFUL! (MAKE A BACKUP BEFORE EXECUTING)").color(ChatColor.RED)));
        helpentries.add(new HelpEntry("bungeeperms.migrate",/*                  */ makeSuggestCommand("/bp migrate <uuidplayerdb [None|YAML|MySQL]>", "Migrates UUID-player-databases or shows status - ").append("!BE CAREFUL! (MAKE A BACKUP BEFORE EXECUTING)").color(ChatColor.RED)));
        helpentries.add(new HelpEntry("bungeeperms.uuid",/*                     */ makeSuggestCommand("/bp uuid <player|uuid> [-rm]", "Gets the UUID of a player from database (-r: reverse; -m: ask mojang)")));
// template        helpentries.add(new HelpEntry(null, makeClickCommand("/bp help", "Shows").color(ChatColor.GRAY)));
    }

    private static MessageEncoder makeClickCommand(String cmd, String help)
    {
        return enc()
                //cmd
                .append(cmd)
                .color(ChatColor.GOLD)
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, cmd))
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, enc().append("Click to execute")))
                //reset
                .append("")
                .reset()
                //seperator
                .append(" - ")
                .color(ChatColor.WHITE)
                //help
                .append(help)
                .color(ChatColor.GRAY);
    }

    private static MessageEncoder makeSuggestCommand(String cmd, String help)
    {
        return enc()
                //cmd
                .append(cmd)
                .color(ChatColor.GOLD)
                .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, cmd))
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, enc().append("Click to suggest")))
                //reset
                .append("")
                .reset()
                //seperator
                .append(" - ")
                .color(ChatColor.WHITE)
                //help
                .append(help)
                .color(ChatColor.GRAY);
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
        sender.sendMessage(enc().append("Aliases: ").color(ChatColor.GRAY).append("/bp").color(ChatColor.GOLD)
                .append("       ").color(ChatColor.GRAY).append("<required>").color(ChatColor.GOLD)
                .append("       ").color(ChatColor.GRAY).append("[optional]").color(ChatColor.GOLD));
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
