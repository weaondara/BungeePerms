package net.alpenblock.bungeeperms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.alpenblock.bungeeperms.platform.Sender;

public class TabCompleter
{

    private static final List<Entry> ENTRIES = new ArrayList<>();

    static
    {
        ENTRIES.add(new Entry("bungeeperms.help",/*                     */ "/bp help [page]"));
        ENTRIES.add(new Entry("bungeeperms.reload",/*                   */ "/bp reload"));
        ENTRIES.add(new Entry("bungeeperms.debug",/*                    */ "/bp debug <true|false>"));
        ENTRIES.add(new Entry("bungeeperms.overview",/*                 */ "/bp overview"));
        ENTRIES.add(new Entry("bungeeperms.users",/*                    */ "/bp users [-c]"));
        ENTRIES.add(new Entry("bungeeperms.user.info",/*                */ "/bp user <user> info [server] [world]"));
        ENTRIES.add(new Entry("bungeeperms.user.delete",/*              */ "/bp user <user> delete"));
        ENTRIES.add(new Entry("bungeeperms.user.display",/*             */ "/bp user <user> display [displayname] [server] [world]"));
        ENTRIES.add(new Entry("bungeeperms.user.prefix",/*              */ "/bp user <user> prefix [prefix] [server] [world]"));
        ENTRIES.add(new Entry("bungeeperms.user.suffix",/*              */ "/bp user <user> suffix [suffix] [server] [world]"));
        ENTRIES.add(new Entry("bungeeperms.user.perms.add",/*           */ "/bp user <user> addperm <perm> [server] [world]"));
        ENTRIES.add(new Entry("bungeeperms.user.perms.add",/*           */ "/bp user <user> addtimedperm <perm> <duration> [server] [world]"));
        ENTRIES.add(new Entry("bungeeperms.user.perms.remove",/*        */ "/bp user <user> removeperm <perm> [server] [world]"));
        ENTRIES.add(new Entry("bungeeperms.user.perms.remove",/*        */ "/bp user <user> removetimedperm <perm> [server] [world]"));
        ENTRIES.add(new Entry("bungeeperms.user.perms.has",/*           */ "/bp user <user> has <perm> [server] [world]"));
        ENTRIES.add(new Entry("bungeeperms.user.perms.list",/*          */ "/bp user <user> list [server] [world]"));
        ENTRIES.add(new Entry("bungeeperms.user.perms.list",/*          */ "/bp user <user> listonly [server] [world]"));
        ENTRIES.add(new Entry("bungeeperms.user.group.add",/*           */ "/bp user <user> addgroup <group> [server] [world]"));
        ENTRIES.add(new Entry("bungeeperms.user.group.add",/*           */ "/bp user <user> addtimedgroup <group> <duration> [server] [world]"));
        ENTRIES.add(new Entry("bungeeperms.user.group.remove",/*        */ "/bp user <user> removegroup <group> [server] [world]"));
        ENTRIES.add(new Entry("bungeeperms.user.group.remove",/*        */ "/bp user <user> removetimedgroup <group> [server] [world]"));
        ENTRIES.add(new Entry("bungeeperms.user.group.set",/*           */ "/bp user <user> setgroup <group>"));
        ENTRIES.add(new Entry("bungeeperms.user.groups",/*              */ "/bp user <user> groups"));
        ENTRIES.add(new Entry("bungeeperms.groups",/*                   */ "/bp groups"));
        ENTRIES.add(new Entry("bungeeperms.group.info",/*               */ "/bp group <group> info [server] [world]"));
        ENTRIES.add(new Entry("bungeeperms.group.users",/*              */ "/bp group <group> users [-c]"));
        ENTRIES.add(new Entry("bungeeperms.group.create",/*             */ "/bp group <group> create"));
        ENTRIES.add(new Entry("bungeeperms.group.delete",/*             */ "/bp group <group> delete"));
        ENTRIES.add(new Entry("bungeeperms.group.inheritances.add",/*   */ "/bp group <group> addinherit <addgroup> [server] [world]]"));
        ENTRIES.add(new Entry("bungeeperms.group.inheritances.add",/*   */ "/bp group <group> addtimedinherit <addgroup> <duration> [server] [world]]"));
        ENTRIES.add(new Entry("bungeeperms.group.inheritances.remove",/**/ "/bp group <group> removeinherit <removegroup> [server] [world]]"));
        ENTRIES.add(new Entry("bungeeperms.group.inheritances.remove",/**/ "/bp group <group> removetimedinherit <removegroup> [server] [world]]"));
        ENTRIES.add(new Entry("bungeeperms.group.rank",/*               */ "/bp group <group> rank <newrank>"));
        ENTRIES.add(new Entry("bungeeperms.group.weight",/*             */ "/bp group <group> weight <newweight>"));
        ENTRIES.add(new Entry("bungeeperms.group.ladder",/*             */ "/bp group <group> ladder <newladder>"));
        ENTRIES.add(new Entry("bungeeperms.group.default",/*            */ "/bp group <group> default <true|false>"));
        ENTRIES.add(new Entry("bungeeperms.group.display",/*            */ "/bp group <group> display [displayname] [server] [world]"));
        ENTRIES.add(new Entry("bungeeperms.group.prefix",/*             */ "/bp group <group> prefix [prefix] [server] [world]"));
        ENTRIES.add(new Entry("bungeeperms.group.suffix",/*             */ "/bp group <group> suffix [suffix] [server] [world]"));
        ENTRIES.add(new Entry("bungeeperms.group.perms.add",/*          */ "/bp group <group> addperm <perm> [server] [world]]"));
        ENTRIES.add(new Entry("bungeeperms.group.perms.add",/*          */ "/bp group <group> addtimedperm <perm> <duration> [server] [world]"));
        ENTRIES.add(new Entry("bungeeperms.group.perms.remove",/*       */ "/bp group <group> removeperm <perm> [server] [world]"));
        ENTRIES.add(new Entry("bungeeperms.group.perms.remove",/*       */ "/bp group <group> removetimedperm <perm> [server] [world]"));
        ENTRIES.add(new Entry("bungeeperms.group.perms.has",/*          */ "/bp group <group> has <perm> [server] [world]"));
        ENTRIES.add(new Entry("bungeeperms.group.perms.list",/*         */ "/bp group <group> list"));
        ENTRIES.add(new Entry("bungeeperms.group.perms.list",/*         */ "/bp group <group> listonly"));
        ENTRIES.add(new Entry("bungeeperms.promote",/*                  */ "/bp promote <user> [ladder]"));
        ENTRIES.add(new Entry("bungeeperms.demote",/*                   */ "/bp demote <user> [ladder]"));
        ENTRIES.add(new Entry("bungeeperms.format",/*                   */ "/bp format"));
        ENTRIES.add(new Entry("bungeeperms.cleanup",/*                  */ "/bp cleanup"));
        ENTRIES.add(new Entry("bungeeperms.migrate",/*                  */ "/bp migrate <backend> [yaml|mysql]"));
        ENTRIES.add(new Entry("bungeeperms.migrate",/*                  */ "/bp migrate <useuuid> [true|false]"));
        ENTRIES.add(new Entry("bungeeperms.uuid",/*                     */ "/bp uuid <player|uuid> [-rm]"));
    }

    public static List<String> tabComplete(Sender sender, String[] args)
    {
        List<Entry> l = sender == null ? ENTRIES : filterbyperm(sender, ENTRIES);
//        switch (args.length)
//        {
//            case 0:
//            case 1:
//                break;
//        }
        l = filterbyargs(l, args);
        return makeSuggestions(l, args);
    }

    private static List<Entry> filterbyperm(Sender sender, List<Entry> entries)
    {
        List<Entry> ret = new ArrayList();
        for (Entry e : entries)
        {
            if (e.getPermission() == null || BungeePerms.getInstance().getPermissionsChecker().hasPermOrConsoleOnServerInWorld(sender, e.getPermission()))
                ret.add(e);
        }
        return ret;
    }

    private static List<Entry> filterbyargs(List<Entry> entries, String[] args)
    {
        List<Entry> filtered = new ArrayList(entries);
        for (int i = 0; i < args.length; i++)
        {
            for (int j = 0; j < filtered.size(); j++)
            {
                String[] split = filtered.get(j).getTemplate().replaceAll("/bp ?", "").split(" ");

                if (i >= split.length) //more args then template elements
                {
                    filtered.remove(j--);
                    continue;
                }
                if (split[i].startsWith("<") || split[i].startsWith("[")) //value selector
                {
//                    String[] options = split[i].replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("<", "").replaceAll(">", "").split("|");
//                    if (!matchesOption(options, args[i]))
//                    {
//                        filtered.remove(j--);
//                        continue;
//                    }
                }
                else //no values selector
                {
                    if (i + 1 == args.length && !split[i].startsWith(args[i].toLowerCase()))
                    {
                        filtered.remove(j--);
                        continue;
                    }
                    else if (i + 1 < args.length && !split[i].equalsIgnoreCase(args[i]))
                    {
                        filtered.remove(j--);
                        continue;
                    }
                }
            }
        }
        return filtered;
    }

    private static List<String> makeSuggestions(List<Entry> entries, String[] args)
    {
        int pos = Math.max(0, args.length - 1);
        String lastarg = args.length == 0 ? "" : args[pos];
        List<String> ret = new ArrayList();
        for (int i = 0; i < entries.size(); i++)
        {
            String[] split = entries.get(i).getTemplate().replaceAll("/bp ?", "").split(" ");
            if (pos >= split.length) //for safety
                continue;
            if (!split[pos].startsWith("<") && !split[pos].startsWith("[") && !ret.contains(split[pos]) && split[pos].toLowerCase().startsWith(lastarg.toLowerCase()))
                ret.add(split[pos]);
            else if (split[pos].startsWith("<") || split[pos].startsWith("["))
            {
                String strip = split[pos].replaceAll("<", "").replaceAll(">", "").replaceAll("\\[", "").replaceAll("\\]", "");
                if (strip.equals("group"))
                {
                    if (BungeePerms.getInstance() != null) //needed for test cases
                        for (Group g : BungeePerms.getInstance().getPermissionsManager().getGroups())
                            if (!ret.contains(g.getName()) && g.getName().toLowerCase().startsWith(lastarg.toLowerCase()))
                                ret.add(g.getName());
                }
                else if (strip.equals("user"))
                {
                    if (BungeePerms.getInstance() != null) //needed for test cases
                        for (Sender s : BungeePerms.getInstance().getPlugin().getPlayers())
                            if (!ret.contains(s.getName()) && s.getName().toLowerCase().startsWith(lastarg.toLowerCase()))
                                ret.add(s.getName());
                }
                else if (strip.equals("-c"))
                {
                    ret.add("-c");
                }
            }
        }
        Collections.sort(ret, String.CASE_INSENSITIVE_ORDER);
        return ret;
    }

//    private static boolean matchesOption(String[] options, String arg)
//    {
//        for (String o : options)
//        {
//            switch (o)
//            {
//                case "":
//                    break;
//                default:
//                    return false;
//            }
//        }
//    }
    @AllArgsConstructor
    @Getter
    private static class Entry
    {

        private final String permission;
        private final String template;
    }
}
