/* 
 * Copyright (C) 2020 wea_ondara
 *
 * BungeePerms is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BungeePerms is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.alpenblock.bungeeperms;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.alpenblock.bungeeperms.Lang.MessageType;
import net.alpenblock.bungeeperms.platform.MessageEncoder;
import net.alpenblock.bungeeperms.platform.MessageEncoder.ClickEvent;
import net.alpenblock.bungeeperms.platform.MessageEncoder.HoverEvent;
import net.alpenblock.bungeeperms.platform.PlatformPlugin;
import net.alpenblock.bungeeperms.platform.Sender;

public class HelpProvider
{

    private static final int PAGE_SIZE = 7;
    private static final List<HelpEntry> HELP_ENTRIES = new ArrayList<>();

    static
    {
        HELP_ENTRIES.add(new HelpEntry(null,/*                                   */ makeClickCommand("/bp", Lang.translate(MessageType.HELP_WELCOME))));
        HELP_ENTRIES.add(new HelpEntry("bungeeperms.help",/*                     */ makeSuggestCommand("/bp help [page]", Lang.translate(MessageType.HELP_HELP))));
        HELP_ENTRIES.add(new HelpEntry("bungeeperms.reload",/*                   */ makeClickCommand("/bp reload", Lang.translate(MessageType.HELP_RELOAD))));
        HELP_ENTRIES.add(new HelpEntry("bungeeperms.debug",/*                    */ makeSuggestCommand("/bp debug <true|false>", Lang.translate(MessageType.HELP_DEBUG))));
        HELP_ENTRIES.add(new HelpEntry("bungeeperms.overview",/*                 */ makeSuggestCommand("/bp overview", Lang.translate(MessageType.HELP_OVERVIEW))));
        HELP_ENTRIES.add(new HelpEntry("bungeeperms.users",/*                    */ makeSuggestCommand("/bp users [-c]", Lang.translate(MessageType.HELP_USERS))));
        HELP_ENTRIES.add(new HelpEntry("bungeeperms.user.info",/*                */ makeSuggestCommand("/bp user <user> info [server [world]]", Lang.translate(MessageType.HELP_USER_INFO))));
        HELP_ENTRIES.add(new HelpEntry("bungeeperms.user.delete",/*              */ makeSuggestCommand("/bp user <user> delete", Lang.translate(MessageType.HELP_USER_DELETE))));
        HELP_ENTRIES.add(new HelpEntry("bungeeperms.user.display",/*             */ makeSuggestCommand("/bp user <user> display [displayname [server [world]]]", Lang.translate(MessageType.HELP_USER_DISPLAY))));
        HELP_ENTRIES.add(new HelpEntry("bungeeperms.user.prefix",/*              */ makeSuggestCommand("/bp user <user> prefix [prefix [server [world]]]", Lang.translate(MessageType.HELP_USER_PREFIX))));
        HELP_ENTRIES.add(new HelpEntry("bungeeperms.user.suffix",/*              */ makeSuggestCommand("/bp user <user> suffix [suffix [server [world]]]", Lang.translate(MessageType.HELP_USER_SUFFIX))));
        HELP_ENTRIES.add(new HelpEntry("bungeeperms.user.perms.add",/*           */ makeSuggestCommand("/bp user <user> addperm <perm> [server [world]]", Lang.translate(MessageType.HELP_USER_ADDPERM))));
        HELP_ENTRIES.add(new HelpEntry("bungeeperms.user.perms.add",/*           */ makeSuggestCommand("/bp user <user> addtimedperm <perm> <duration> [server [world]]", Lang.translate(MessageType.HELP_USER_ADDTIMEDPERM))));
        HELP_ENTRIES.add(new HelpEntry("bungeeperms.user.perms.remove",/*        */ makeSuggestCommand("/bp user <user> removeperm <perm> [server [world]]", Lang.translate(MessageType.HELP_USER_REMOVEPERM))));
        HELP_ENTRIES.add(new HelpEntry("bungeeperms.user.perms.remove",/*        */ makeSuggestCommand("/bp user <user> removetimedperm <perm> [server [world]]", Lang.translate(MessageType.HELP_USER_REMOVETIMEDPERM))));
        HELP_ENTRIES.add(new HelpEntry("bungeeperms.user.perms.has",/*           */ makeSuggestCommand("/bp user <user> has <perm> [server [world]]", Lang.translate(MessageType.HELP_USER_HAS))));
        HELP_ENTRIES.add(new HelpEntry("bungeeperms.user.perms.list",/*          */ makeSuggestCommand("/bp user <user> list [server [world]]", Lang.translate(MessageType.HELP_USER_LIST))));
        HELP_ENTRIES.add(new HelpEntry("bungeeperms.user.perms.list",/*          */ makeSuggestCommand("/bp user <user> listonly [server [world]]", Lang.translate(MessageType.HELP_USER_LIST))));
        HELP_ENTRIES.add(new HelpEntry("bungeeperms.user.group.add",/*           */ makeSuggestCommand("/bp user <user> addgroup <group> [server [world]]", Lang.translate(MessageType.HELP_USER_ADDGROUP))));
        HELP_ENTRIES.add(new HelpEntry("bungeeperms.user.group.add",/*           */ makeSuggestCommand("/bp user <user> addtimedgroup <group> <duration> [server [world]]", Lang.translate(MessageType.HELP_USER_ADDTIMEDGROUP))));
        HELP_ENTRIES.add(new HelpEntry("bungeeperms.user.group.remove",/*        */ makeSuggestCommand("/bp user <user> removegroup <group> [server [world]]", Lang.translate(MessageType.HELP_USER_REMOVEGROUP))));
        HELP_ENTRIES.add(new HelpEntry("bungeeperms.user.group.remove",/*        */ makeSuggestCommand("/bp user <user> removetimedgroup <group> [server [world]]", Lang.translate(MessageType.HELP_USER_REMOVETIMEDGROUP))));
        HELP_ENTRIES.add(new HelpEntry("bungeeperms.user.group.set",/*           */ makeSuggestCommand("/bp user <user> setgroup <group>", Lang.translate(MessageType.HELP_USER_SETGROUP))));
        HELP_ENTRIES.add(new HelpEntry("bungeeperms.user.groups",/*              */ makeSuggestCommand("/bp user <user> groups", Lang.translate(MessageType.HELP_USER_GROUPS))));
        HELP_ENTRIES.add(new HelpEntry("bungeeperms.groups",/*                   */ makeClickCommand("/bp groups", Lang.translate(MessageType.HELP_GROUPS))));
        HELP_ENTRIES.add(new HelpEntry("bungeeperms.group.info",/*               */ makeSuggestCommand("/bp group <group> info [server [world]]", Lang.translate(MessageType.HELP_GROUP_INFO))));
        HELP_ENTRIES.add(new HelpEntry("bungeeperms.group.users",/*              */ makeSuggestCommand("/bp group <group> users [-c]", Lang.translate(MessageType.HELP_GROUP_USERS))));
        HELP_ENTRIES.add(new HelpEntry("bungeeperms.group.create",/*             */ makeSuggestCommand("/bp group <group> create", Lang.translate(MessageType.HELP_GROUP_CREATE))));
        HELP_ENTRIES.add(new HelpEntry("bungeeperms.group.delete",/*             */ makeSuggestCommand("/bp group <group> delete", Lang.translate(MessageType.HELP_GROUP_DELETE))));
        HELP_ENTRIES.add(new HelpEntry("bungeeperms.group.inheritances.add",/*   */ makeSuggestCommand("/bp group <group> addinherit <addgroup> [server [world]]", Lang.translate(MessageType.HELP_GROUP_ADDINHERIT))));
        HELP_ENTRIES.add(new HelpEntry("bungeeperms.group.inheritances.add",/*   */ makeSuggestCommand("/bp group <group> addtimedinherit <addgroup> <duration> [server [world]]", Lang.translate(MessageType.HELP_GROUP_ADDTIMEDINHERIT))));
        HELP_ENTRIES.add(new HelpEntry("bungeeperms.group.inheritances.remove",/**/ makeSuggestCommand("/bp group <group> removeinherit <removegroup> [server [world]]", Lang.translate(MessageType.HELP_GROUP_REMOVEINHERIT))));
        HELP_ENTRIES.add(new HelpEntry("bungeeperms.group.inheritances.remove",/**/ makeSuggestCommand("/bp group <group> removetimedinherit <removegroup> [server [world]]", Lang.translate(MessageType.HELP_GROUP_REMOVETIMEDINHERIT))));
        HELP_ENTRIES.add(new HelpEntry("bungeeperms.group.rank",/*               */ makeSuggestCommand("/bp group <group> rank <new rank>", Lang.translate(MessageType.HELP_GROUP_RANK))));
        HELP_ENTRIES.add(new HelpEntry("bungeeperms.group.weight",/*             */ makeSuggestCommand("/bp group <group> weight <new weight>", Lang.translate(MessageType.HELP_GROUP_WEIGHT))));
        HELP_ENTRIES.add(new HelpEntry("bungeeperms.group.ladder",/*             */ makeSuggestCommand("/bp group <group> ladder <new ladder>", Lang.translate(MessageType.HELP_GROUP_LADDER))));
        HELP_ENTRIES.add(new HelpEntry("bungeeperms.group.default",/*            */ makeSuggestCommand("/bp group <group> default <true|false>", Lang.translate(MessageType.HELP_GROUP_DEFAULT))));
        HELP_ENTRIES.add(new HelpEntry("bungeeperms.group.display",/*            */ makeSuggestCommand("/bp group <group> display [displayname [server [world]]]", Lang.translate(MessageType.HELP_GROUP_DISPLAY))));
        HELP_ENTRIES.add(new HelpEntry("bungeeperms.group.prefix",/*             */ makeSuggestCommand("/bp group <group> prefix [prefix [server [world]]]", Lang.translate(MessageType.HELP_GROUP_PREFIX))));
        HELP_ENTRIES.add(new HelpEntry("bungeeperms.group.suffix",/*             */ makeSuggestCommand("/bp group <group> suffix [suffix [server [world]]]", Lang.translate(MessageType.HELP_GROUP_SUFFIX))));
        HELP_ENTRIES.add(new HelpEntry("bungeeperms.group.perms.add",/*          */ makeSuggestCommand("/bp group <group> addperm <perm> [server [world]]", Lang.translate(MessageType.HELP_GROUP_ADDPERM))));
        HELP_ENTRIES.add(new HelpEntry("bungeeperms.group.perms.add",/*          */ makeSuggestCommand("/bp group <group> addtimedperm <perm> <duration> [server [world]]", Lang.translate(MessageType.HELP_GROUP_ADDTIMEDPERM))));
        HELP_ENTRIES.add(new HelpEntry("bungeeperms.group.perms.remove",/*       */ makeSuggestCommand("/bp group <group> removeperm <perm> [server [world]]", Lang.translate(MessageType.HELP_GROUP_REMOVEPERM))));
        HELP_ENTRIES.add(new HelpEntry("bungeeperms.group.perms.remove",/*       */ makeSuggestCommand("/bp group <group> removetimedperm <perm> [server [world]]", Lang.translate(MessageType.HELP_GROUP_REMOVETIMEDPERM))));
        HELP_ENTRIES.add(new HelpEntry("bungeeperms.group.perms.has",/*          */ makeSuggestCommand("/bp group <group> has <perm> [server [world]]", Lang.translate(MessageType.HELP_GROUP_HAS))));
        HELP_ENTRIES.add(new HelpEntry("bungeeperms.group.perms.list",/*         */ makeSuggestCommand("/bp group <group> list", Lang.translate(MessageType.HELP_GROUP_LIST))));
        HELP_ENTRIES.add(new HelpEntry("bungeeperms.group.perms.list",/*         */ makeSuggestCommand("/bp group <group> listonly", Lang.translate(MessageType.HELP_GROUP_LIST))));
        HELP_ENTRIES.add(new HelpEntry("bungeeperms.search",/*                   */ makeSuggestCommand("/bp search <permission>", Lang.translate(MessageType.HELP_SEARCH))));
        HELP_ENTRIES.add(new HelpEntry("bungeeperms.promote",/*                  */ makeSuggestCommand("/bp promote <user> [ladder]", Lang.translate(MessageType.HELP_PROMOTE))));
        HELP_ENTRIES.add(new HelpEntry("bungeeperms.demote",/*                   */ makeSuggestCommand("/bp demote <user> [ladder]", Lang.translate(MessageType.HELP_DEMOTE))));
        HELP_ENTRIES.add(new HelpEntry("bungeeperms.format",/*                   */ makeClickCommand("/bp format", Lang.translate(MessageType.HELP_FORMAT))));
        HELP_ENTRIES.add(new HelpEntry("bungeeperms.cleanup",/*                  */ makeClickCommand("/bp cleanup", Lang.translate(MessageType.HELP_CLEANUP))));
        HELP_ENTRIES.add(new HelpEntry("bungeeperms.migrate",/*                  */ makeSuggestCommand("/bp migrate <backend> [yaml|mysql]", Lang.translate(MessageType.HELP_MIGRATE_BACKEND))));
        HELP_ENTRIES.add(new HelpEntry("bungeeperms.migrate",/*                  */ makeSuggestCommand("/bp migrate <useuuid> [true|false]", Lang.translate(MessageType.HELP_MIGRATE_USEUUID))));
        HELP_ENTRIES.add(new HelpEntry("bungeeperms.uuid",/*                     */ makeSuggestCommand("/bp uuid <player|uuid> [-rm]", Lang.translate(MessageType.HELP_UUID))));
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
        for (HelpEntry he : HELP_ENTRIES)
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
