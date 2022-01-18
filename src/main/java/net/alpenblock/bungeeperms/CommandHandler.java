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

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import net.alpenblock.bungeeperms.Lang.MessageType;
import net.alpenblock.bungeeperms.io.BackEndType;
import net.alpenblock.bungeeperms.io.MySQLUUIDPlayerDB;
import net.alpenblock.bungeeperms.io.UUIDPlayerDB;
import net.alpenblock.bungeeperms.io.YAMLUUIDPlayerDB;
import net.alpenblock.bungeeperms.platform.PlatformPlugin;
import net.alpenblock.bungeeperms.platform.Sender;
import net.alpenblock.bungeeperms.uuid.UUIDFetcher;

@AllArgsConstructor
public class CommandHandler
{

    protected PlatformPlugin plugin;
    protected PermissionsChecker checker;
    protected BPConfig config;

    public boolean onCommand(Sender sender, String cmd, String label, String[] args)
    {
        if (!cmd.equalsIgnoreCase("bungeeperms"))
        {
            return false;
        }

        if (BungeePerms.getInstance().getConfig().isDebug())
        {
            BungeePerms.getInstance().getPlugin().getLogger().info(Lang.translate(MessageType.COMMAND_ISSUED, sender.getName(), cmd, Statics.arrayToString(args, 0, args.length, " ")));
        }

        //reparse command
        args = Statics.parseCommand(Statics.arrayToString(args, 0, args.length, " "));

        if (args.length == 0)
        {
            sender.sendMessage(Lang.translate(MessageType.BUNGEEPERMS));
            sender.sendMessage(Lang.translate(MessageType.VERSION, plugin.getVersion()));
            sender.sendMessage(Lang.translate(MessageType.AUTHOR, plugin.getAuthor()));
            return true;
        }
        else if (args.length > 0)
        {
            if (args[0].equalsIgnoreCase("help"))
            {
                return handleHelp(sender, args);
            }
            else if (args[0].equalsIgnoreCase("reload"))
            {
                return handleReload(sender, args);
            }
            else if (args[0].equalsIgnoreCase("debug"))
            {
                return handleDebug(sender, args);
            }
            else if (args[0].equalsIgnoreCase("overview"))
            {
                return handleOverview(sender, args);
            }
            else if (args[0].equalsIgnoreCase("users"))
            {
                return handleUsers(sender, args);
            }
            else if (args[0].equalsIgnoreCase("user"))
            {
                return handleUserCommands(sender, args);
            }
            else if (args[0].equalsIgnoreCase("groups"))
            {
                return handleGroups(sender, args);
            }
            else if (args[0].equalsIgnoreCase("group"))
            {
                return handleGroupCommands(sender, args);
            }
            else if (args[0].equalsIgnoreCase("promote"))
            {
                return handlePromote(sender, args);
            }
            else if (args[0].equalsIgnoreCase("demote"))
            {
                return handleDemote(sender, args);
            }
            else if (args[0].equalsIgnoreCase("format"))
            {
                return handleFormat(sender, args);
            }
            else if (args[0].equalsIgnoreCase("cleanup"))
            {
                return handleCleanup(sender, args);
            }
            else if (args[0].equalsIgnoreCase("migrate"))
            {
                return handleMigrate(sender, args);
            }
            else if (args[0].equalsIgnoreCase("uuid"))
            {
                return handleUUID(sender, args);
            }
            else if (args[0].equalsIgnoreCase("search"))
            {
                return handleSearch(sender, args);
            }
        }
        return false;
    }

    private boolean handleHelp(Sender sender, String[] args)
    {
        if (!checker.hasOrConsole(sender, "bungeeperms.help", true))
        {
//            showHelp(sender);
            return true;
        }

        if (!Statics.matchArgs(sender, args, 1, 2))
        {
            return true;
        }

        int page = 1;
        if (args.length == 2)
        {
            try
            {
                page = Integer.parseInt(args[1]);
                if (page < 1)
                {
                    throw new Exception();
                }
            }
            catch (Exception e)
            {
                sender.sendMessage(Lang.translate(MessageType.ERR_INVALID_INT_VALUE));
                return true;
            }
        }

        page--;

        HelpProvider.sendHelpPage(sender, page);

        return true;
    }

    private boolean handleReload(Sender sender, String[] args)
    {
        if (!checker.hasOrConsole(sender, "bungeeperms.reload", true))
        {
            return true;
        }

        BungeePerms.getInstance().reload(true);
        sender.sendMessage(Lang.translate(MessageType.PERMISSIONS_RELOADED));
        return true;
    }

    private boolean handleDebug(Sender sender, String[] args)
    {
        if (!checker.hasOrConsole(sender, "bungeeperms.debug", true))
        {
            return true;
        }

        if (!Statics.matchArgs(sender, args, 2))
        {
            return true;
        }

        boolean val;
        try
        {
            val = parseTrueFalse(args[1]);
        }
        catch (Exception e)
        {
            sender.sendMessage(Lang.translate(MessageType.ERR_INVALID_BOOL_VALUE));
            return true;
        }

        if (val)
        {
            config.setDebug(true);
            sender.sendMessage(Lang.translate(MessageType.DEBUG_ENABLED));
            return true;
        }
        else
        {
            config.setDebug(false);
            sender.sendMessage(Lang.translate(MessageType.DEBUG_DISABLED));
            return true;
        }
    }

    private boolean handleOverview(Sender sender, String[] args)
    {
        if (!checker.hasOrConsole(sender, "bungeeperms.overview", true))
        {
            return true;
        }

        if (!Statics.matchArgs(sender, args, 1))
        {
            return true;
        }

        List<String> msgs = new ArrayList();
        msgs.add(Lang.translate(MessageType.OVERVIEW_HEADER));
        Map<Group, Object> tree = new HashMap();
        List<Group> groups = new ArrayList(BungeePerms.getInstance().getPermissionsManager().getGroups());
        Map<Group, List<Group>> parents = new HashMap();
        for (Group g : groups)
            for (Group child : g.getInheritances())
            {
                List l = parents.getOrDefault(child, new ArrayList());
                l.add(g);
                parents.put(child, l);
            }

        //roots
        for (int i = 0; i < groups.size(); i++)
        {
            Group g = groups.get(i);
            if (parents.getOrDefault(g, new ArrayList()).isEmpty())
            {
                tree.put(g, new HashMap());
                groups.remove(i--);
            }
        }

        //leaves
        boolean didsomthing;
        do
        {
            didsomthing = false;
            for (int i = 0; i < groups.size(); i++)
            {
                Group g = groups.get(i);
                List<Group> pl = parents.get(g);
                for (Group p : pl)
                    didsomthing |= addToTree(tree, p, g);
            }
        }
        while (didsomthing);

        List<String> bla = treeToMsgs(tree, 0);
        msgs.addAll(bla);

        for (String msg : msgs)
            sender.sendMessage(msg);

        return true;
    }

    private boolean handleUsers(Sender sender, String[] args)
    {
        if (!checker.hasOrConsole(sender, "bungeeperms.users.list", true))
        {
            return true;
        }

        if (!Statics.matchArgs(sender, args, 1, 2))
        {
            return true;
        }

        if (args.length == 1)
        {
            if (config.isUseUUIDs())
            {
                Map<UUID, String> usersmap = pm().getRegisteredUsersUUID();
                if (usersmap.isEmpty())
                {
                    sender.sendMessage(Lang.translate(MessageType.NO_USERS_FOUND));
                    return true;
                }
                List<Map.Entry<UUID, String>> users = new ArrayList(usersmap.entrySet());
                users.sort(new Comparator<Map.Entry<UUID, String>>()
                {
                    @Override
                    public int compare(Map.Entry<UUID, String> o1, Map.Entry<UUID, String> o2)
                    {
                        return String.CASE_INSENSITIVE_ORDER.compare(o1.getValue(), o2.getValue());
                    }
                });

                String out = Lang.translate(MessageType.REGISTERED_USERS);
                for (int i = 0; i < users.size(); i++)
                {
                    //mc chat packet length; string most likely 1 byte/char so 1.1 bytes/char should be safe
                    if (out.length() * 1.1 > Short.MAX_VALUE)
                    {
                        sender.sendMessage(out);
                        out = Lang.translate(MessageType.REGISTERED_USERS);
                    }
                    out += Color.User + users.get(i).getValue() + Color.Text + " (" + Color.User + users.get(i).getKey() + Color.Text + ")" + (i + 1 < users.size() ? ", " : "");
                }
                sender.sendMessage(out);
                return true;
            }
            else
            {
                List<String> users = pm().getRegisteredUsers();
                if (users.isEmpty())
                {
                    sender.sendMessage(Lang.translate(MessageType.NO_USERS_FOUND));
                    return true;
                }

                users.sort(String.CASE_INSENSITIVE_ORDER);

                String out = Lang.translate(MessageType.REGISTERED_USERS);
                for (int i = 0; i < users.size(); i++)
                {
                    //mc chat packet length; string most likely 1 byte/char so 1.1 bytes/char should be safe
                    if (out.length() * 1.1 > Short.MAX_VALUE)
                    {
                        sender.sendMessage(out);
                        out = Lang.translate(MessageType.REGISTERED_USERS);
                    }
                    out += Color.User + users.get(i) + Color.Text + (i + 1 < users.size() ? ", " : "");
                }
                sender.sendMessage(out);
                return true;
            }
        }
        else //args count == 2
        {
            //for counting
            if (!args[1].equalsIgnoreCase("-c"))
            {
                return false;
            }

            sender.sendMessage(Lang.translate(MessageType.REGISTERED_USERS_COUNT, pm().getRegisteredUsers().size()));
            return true;
        }
    }

    private boolean handleUserCommands(Sender sender, String[] args)
    {
        if (args.length < 3)
        {
            sender.sendMessage(Lang.translate(MessageType.COMMAND_TOO_FEW_ARGUMENTS));
            return true;
        }

        if (Statics.argAlias(args[2], "list", "listonly"))
        {
            return handleUserCommandsList(sender, args);
        }
        else if (args[2].equalsIgnoreCase("groups"))
        {
            return handleUserCommandsGroups(sender, args);
        }
        else if (args[2].equalsIgnoreCase("info"))
        {
            return handleUserCommandsInfo(sender, args);
        }
        else if (args[2].equalsIgnoreCase("delete"))
        {
            return handleUserCommandsDelete(sender, args);
        }
        else if (Statics.argAlias(args[2], "add", "addperm", "addpermission"))
        {
            return handleUserCommandsPermAdd(sender, args);
        }
        else if (Statics.argAlias(args[2], "remove", "removeperm", "removepermission"))
        {
            return handleUserCommandsPermRemove(sender, args);
        }
        else if (Statics.argAlias(args[2], "timedadd", "addtimed", "timedaddperm", "addtimedperm", "addpermtimed"))
        {
            return handleUserCommandsTimedPermAdd(sender, args);
        }
        else if (Statics.argAlias(args[2], "timedremove", "removetimed", "timedremoveperm", "removetimedperm", "removepermtimed"))
        {
            return handleUserCommandsTimedPermRemove(sender, args);
        }
        else if (Statics.argAlias(args[2], "has", "check"))
        {
            return handleUserCommandsHas(sender, args);
        }
        else if (args[2].equalsIgnoreCase("addgroup"))
        {
            return handleUserCommandsGroupAdd(sender, args);
        }
        else if (args[2].equalsIgnoreCase("removegroup"))
        {
            return handleUserCommandsGroupRemove(sender, args);
        }
        else if (args[2].equalsIgnoreCase("setgroup"))
        {
            return handleUserCommandsGroupSet(sender, args);
        }
        else if (args[2].equalsIgnoreCase("addtimedgroup"))
        {
            return handleUserCommandsTimedGroupAdd(sender, args);
        }
        else if (args[2].equalsIgnoreCase("removetimedgroup"))
        {
            return handleUserCommandsTimedGroupRemove(sender, args);
        }
        else if (args[2].equalsIgnoreCase("display"))
        {
            return handleUserCommandsDisplay(sender, args);
        }
        else if (args[2].equalsIgnoreCase("prefix"))
        {
            return handleUserCommandsPrefix(sender, args);
        }
        else if (args[2].equalsIgnoreCase("suffix"))
        {
            return handleUserCommandsSuffix(sender, args);
        }

        //alias handling
        else if (Statics.argAlias(args[2], "group", "perm", "permission", "timed"))
        {
            if (!Statics.matchArgs(sender, args, 5))
            {
                return true;
            }

            String[] newargs =
            {
                args[0], args[1], args[3] + args[2], args[4]
            };
            return onCommand(sender, "bungeeperms", "bp", newargs);
        }
        return false;
    }

//user commands
    private boolean handleUserCommandsList(Sender sender, String[] args)
    {
        if (!checker.hasOrConsole(sender, "bungeeperms.user.perms.list", true))
        {
            return true;
        }

        if (!Statics.matchArgs(sender, args, 3, 6))
        {
            return true;
        }

        boolean specialpage = args.length > 3 && Statics.isInt(args[args.length - 1]);
        int page = specialpage ? Integer.parseInt(args[args.length - 1]) : 1;
        if (page < 1)
        {
            sender.sendMessage(Lang.translate(MessageType.ERR_INVALID_INT_VALUE));
            return true;
        }

        String player = Statics.getFullPlayerName(args[1]);
        boolean only = args[2].equalsIgnoreCase("listonly");
        String server = args.length > (3 + (specialpage ? 1 : 0)) ? args[3].toLowerCase() : null;//todo tolower with config locale
        String world = args.length > (4 + (specialpage ? 1 : 0)) ? args[4].toLowerCase() : null;

        User user = pm().getUser(player);
        if (user == null)
        {
            sender.sendMessage(Lang.translate(MessageType.ERR_USER_NOT_EXISTING, player));
            return true;
        }

        if (config.isUseUUIDs())
        {
            sender.sendMessage(Lang.translate(MessageType.USER_PERMISSIONS_LIST_HEADER_UUID, user.getName(), user.getUUID()));
        }
        else
        {
            sender.sendMessage(Lang.translate(MessageType.USER_PERMISSIONS_LIST_HEADER, user.getName()));
        }

        List<BPPermission> perms = user.getPermsWithOrigin(server, world);
        list(sender, perms, player, page, only, server, world);
//        sender.sendMessage(Lang.translate(MessageType.PERMISSIONS_LIST_HEADER_PAGE, page, perms.size() / 20 + (perms.size() % 20 > 0 ? 1 : 0)));
//        for (int i = (page - 1) * 20; i < page * 20 && i < perms.size(); i++)
//        {
//            BPPermission perm = perms.get(i);
//            String dur = formatDuration(perm);
//            sender.sendMessage(Lang.translate(MessageType.PERMISSIONS_LIST_ITEM,
//                                              perm.getPermission(),
//                                              (!perm.isGroup() && perm.getOrigin().equalsIgnoreCase(player) ? Lang.translate(MessageType.OWN) : perm.getOrigin()),
//                                              (perm.getServer() != null ? " | " + Color.Value + perm.getServer() + Color.Text : ""),
//                                              (perm.getWorld() != null ? " | " + Color.Value + perm.getWorld() + Color.Text : ""),
//                                              dur == null ? "" : ", " + Color.Value + dur + Color.Text));
//        }
        return true;
    }

    private boolean handleUserCommandsGroups(Sender sender, String[] args)
    {
        if (!checker.hasOrConsole(sender, "bungeeperms.user.groups", true))
        {
            return true;
        }

        if (!Statics.matchArgs(sender, args, 3))
        {
            return true;
        }

        String player = Statics.getFullPlayerName(args[1]);
        User user = pm().getUser(player);
        if (user == null)
        {
            sender.sendMessage(Lang.translate(MessageType.ERR_USER_NOT_EXISTING, player));
            return true;
        }

        sender.sendMessage(Lang.translate(MessageType.USER_GROUPS_HEADER, user.getName()));
        List<String> groups = new ArrayList(user.getGroupsString());
        groups.sort(String.CASE_INSENSITIVE_ORDER);
        for (String g : groups)
        {
            sender.sendMessage(Color.Text + "- " + Color.Value + g);
        }
        List<TimedValue<String>> tgroups = new ArrayList(user.getTimedGroupsString());
        tgroups.sort(new Comparator<TimedValue<String>>()
        {
            @Override
            public int compare(TimedValue<String> o1, TimedValue<String> o2)
            {
                return String.CASE_INSENSITIVE_ORDER.compare(o1.getValue(), o2.getValue());
            }
        });
        for (TimedValue<String> g : tgroups)
        {
            sender.sendMessage(Color.Text + "- " + Color.Value + g.getValue() + Color.Text + "(" + Color.Value + formatDuration(g) + Color.Text + ")");
        }
        return true;
    }

    private boolean handleUserCommandsInfo(Sender sender, String[] args)
    {
        if (!checker.hasOrConsole(sender, "bungeeperms.user.info", true))
        {
            return true;
        }

        if (!Statics.matchArgs(sender, args, 3, 5))
        {
            return true;
        }

        String player = Statics.getFullPlayerName(args[1]);
        String server = args.length > 3 ? args[3].toLowerCase() : null;
        String world = args.length > 4 ? args[4].toLowerCase() : null;

        User user = pm().getUser(player);
        if (user == null)
        {
            sender.sendMessage(Lang.translate(MessageType.ERR_USER_NOT_EXISTING, player));
            return true;
        }

        sender.sendMessage(Lang.translate(MessageType.USER_ABOUT, user.getName()));

        sender.sendMessage(Lang.translate(MessageType.USER_UUID, user.getUUID()));

        List<SimpleEntry<Group, String>> groups = new ArrayList();
        String groupstr = "";
        List<Group> glist = user.getGroups();
        List<TimedValue<Group>> tglist = user.getTimedGroups();
        if (server != null)
        {
            glist.addAll(user.getServer(server).getGroups());
            tglist.addAll(user.getServer(server).getTimedGroups());
            if (world != null)
            {
                glist.addAll(user.getServer(server).getWorld(world).getGroups());
                tglist.addAll(user.getServer(server).getWorld(world).getTimedGroups());
            }
        }
        for (int i = 0; i < glist.size(); i++)
            groups.add(new SimpleEntry(glist.get(i), Color.Value + glist.get(i).getName() + Color.Text
                                                     + " (" + Color.Value + glist.get(i).getPerms().size() + Color.Text + ")"));
        for (int i = 0; i < tglist.size(); i++)
        {
            groups.add(new SimpleEntry(tglist.get(i).getValue(), Color.Value + tglist.get(i).getValue().getName() + Color.Text
                                                                 + " (" + Color.Value + tglist.get(i).getValue().getPerms().size()
                                                                 + Color.Text + "|" + Color.Value + formatDuration(tglist.get(i)) + Color.Text + ")"));
        }
        groups.sort(new Comparator<SimpleEntry<Group, String>>()
        {
            @Override
            public int compare(SimpleEntry<Group, String> o1, SimpleEntry<Group, String> o2)
            {
                return Group.WEIGHT_COMPARATOR.compare(o1.getKey(), o2.getKey());
            }
        });
        if (groups.isEmpty())
            groupstr = Color.Text + "(" + Lang.translate(MessageType.NONE) + ")";
        else
            for (int i = 0; i < groups.size(); i++)
                groupstr += groups.get(i).getValue() + (i + 1 < groups.size() ? ", " : "");
        sender.sendMessage(Lang.translate(MessageType.USER_GROUPS, groupstr));

        //user perms
        sender.sendMessage(Lang.translate(MessageType.USER_PERMISSIONS, user.getOwnPermissionsCount(server, world)));

        //all group perms
        sender.sendMessage(Lang.translate(MessageType.USER_ALL_PERMISSIONS_COUNT, user.getPermissionsCount(server, world)));

        //prepare displayables
        Permable perm = user;
        if (server != null)
        {
            perm = ((User) perm).getServer(server);
            if (world != null)
                perm = ((Server) perm).getWorld(world);
        }

        //display
        sender.sendMessage(Lang.translatePreserveArgs(MessageType.DISPLAY, (!Statics.isEmpty(perm.getDisplay()) ? perm.getDisplay().replaceAll("" + ChatColor.COLOR_CHAR, "&") : Color.Text + "(" + Lang.translate(MessageType.NONE) + ")")));

        //prefix
        sender.sendMessage(Lang.translatePreserveArgs(MessageType.PREFIX, (!Statics.isEmpty(perm.getPrefix()) ? perm.getPrefix().replaceAll("" + ChatColor.COLOR_CHAR, "&") : Color.Text + "(" + Lang.translate(MessageType.NONE) + ")")));

        //suffix
        sender.sendMessage(Lang.translatePreserveArgs(MessageType.SUFFIX, (!Statics.isEmpty(perm.getSuffix()) ? perm.getSuffix().replaceAll("" + ChatColor.COLOR_CHAR, "&") : Color.Text + "(" + Lang.translate(MessageType.NONE) + ")")));

        //full prefix
        String buildPrefix = user.buildPrefix(server, world);
        sender.sendMessage(Lang.translatePreserveArgs(MessageType.PREFIX_FULL, (!Statics.isEmpty(buildPrefix) ? buildPrefix.replaceAll("" + ChatColor.COLOR_CHAR, "&") : Color.Text + "(" + Lang.translate(MessageType.NONE) + ")")));

        //full suffix
        String buildSuffix = user.buildSuffix(server, world);
        sender.sendMessage(Lang.translatePreserveArgs(MessageType.SUFFIX_FULL, (!Statics.isEmpty(buildSuffix) ? buildSuffix.replaceAll("" + ChatColor.COLOR_CHAR, "&") : Color.Text + "(" + Lang.translate(MessageType.NONE) + ")")));
        return true;
    }

    private boolean handleUserCommandsDelete(Sender sender, String[] args)
    {
        if (!checker.hasOrConsole(sender, "bungeeperms.user.delete", true))
        {
            return true;
        }

        if (!Statics.matchArgs(sender, args, 3))
        {
            return true;
        }

        String player = Statics.getFullPlayerName(args[1]);
        User user = pm().getUser(player);
        if (user == null)
        {
            sender.sendMessage(Lang.translate(MessageType.ERR_USER_NOT_EXISTING, player));
            return true;
        }

        pm().deleteUser(user);

        sender.sendMessage(Lang.translate(MessageType.USER_DELETED));
        return true;
    }

    private boolean handleUserCommandsPermAdd(Sender sender, String[] args)
    {
        if (!checker.hasOrConsole(sender, "bungeeperms.user.perms.add", true))
        {
            return true;
        }

        if (!Statics.matchArgs(sender, args, 4, 6))
        {
            return true;
        }

        String player = Statics.getFullPlayerName(args[1]);
        String perm = args[3].toLowerCase();
        String server = args.length > 4 ? args[4].toLowerCase() : null;
        String world = args.length > 5 ? args[5].toLowerCase() : null;
        User user = pm().getUser(player);
        if (user == null)
        {
            sender.sendMessage(Lang.translate(MessageType.ERR_USER_NOT_EXISTING, player));
            return true;
        }

        if (server == null)
        {
            if (!user.getPerms().contains(perm))
            {
                pm().addUserPerm(user, null, null, perm);
                sender.sendMessage(Lang.translate(MessageType.USER_ADDED_PERM, perm, user.getName()));
            }
            else
            {
                sender.sendMessage(Lang.translate(MessageType.USER_ALREADY_HAS_PERM, user.getName(), perm));
            }
        }
        else
        {
            Server srv = user.getServer(server);

            if (world == null)
            {
                if (!srv.getPerms().contains(perm))
                {
                    pm().addUserPerm(user, server, null, perm);
                    sender.sendMessage(Lang.translate(MessageType.USER_ADDED_PERM_SERVER, perm, user.getName(), server));
                }
                else
                {
                    sender.sendMessage(Lang.translate(MessageType.USER_ALREADY_HAS_PERM_SERVER, user.getName(), perm, server));
                }
            }
            else
            {
                World w = srv.getWorld(world);

                if (!w.getPerms().contains(perm))
                {
                    pm().addUserPerm(user, server, world, perm);
                    sender.sendMessage(Lang.translate(MessageType.USER_ADDED_PERM_SERVER_WORLD, perm, user.getName(), server, world));
                }
                else
                {
                    sender.sendMessage(Lang.translate(MessageType.USER_ALREADY_HAS_PERM_SERVER_WORLD, user.getName(), perm, server, world));
                }
            }
        }
        return true;
    }

    private boolean handleUserCommandsPermRemove(Sender sender, String[] args)
    {
        if (!checker.hasOrConsole(sender, "bungeeperms.user.perms.remove", true))
        {
            return true;
        }

        if (!Statics.matchArgs(sender, args, 4, 6))
        {
            return true;
        }

        String player = Statics.getFullPlayerName(args[1]);
        String perm = args[3].toLowerCase();
        String server = args.length > 4 ? args[4].toLowerCase() : null;
        String world = args.length > 5 ? args[5].toLowerCase() : null;
        User user = pm().getUser(player);
        if (user == null)
        {
            sender.sendMessage(Lang.translate(MessageType.ERR_USER_NOT_EXISTING, player));
            return true;
        }

        if (server == null)
        {
            if (user.getPerms().contains(perm))
            {
                pm().removeUserPerm(user, null, null, perm);
                sender.sendMessage(Lang.translate(MessageType.USER_REMOVED_PERM, perm, user.getName()));
            }
            else
            {
                sender.sendMessage(Lang.translate(MessageType.USER_NEVER_HAD_PERM, user.getName(), perm));
            }
        }
        else
        {
            Server srv = user.getServer(server);

            if (world == null)
            {
                if (srv.getPerms().contains(perm))
                {
                    pm().removeUserPerm(user, server, null, perm);
                    sender.sendMessage(Lang.translate(MessageType.USER_REMOVED_PERM_SERVER, perm, user.getName(), server));
                }
                else
                {
                    sender.sendMessage(Lang.translate(MessageType.USER_NEVER_HAD_PERM_SERVER, user.getName(), perm, server));
                }
            }
            else
            {
                World w = srv.getWorld(world);

                if (w.getPerms().contains(perm))
                {
                    pm().removeUserPerm(user, server, world, perm);
                    sender.sendMessage(Lang.translate(MessageType.USER_REMOVED_PERM_SERVER_WORLD, perm, user.getName(), server, world));
                }
                else
                {
                    sender.sendMessage(Lang.translate(MessageType.USER_NEVER_HAD_PERM_SERVER_WORLD, user.getName(), perm, server, world));
                }
            }
        }
        return true;
    }

    private boolean handleUserCommandsTimedPermAdd(Sender sender, String[] args)
    {
        if (!checker.hasOrConsole(sender, "bungeeperms.user.perms.add", true))
        {
            return true;
        }

        if (!Statics.matchArgs(sender, args, 5, 7))
        {
            return true;
        }

        String player = Statics.getFullPlayerName(args[1]);
        String perm = args[3].toLowerCase();
        Integer duration = parseDuration(args[4]);
        String server = args.length > 5 ? args[5].toLowerCase() : null;
        String world = args.length > 6 ? args[6].toLowerCase() : null;

        if (duration == null || duration < 0)
        {
            sender.sendMessage(Lang.translate(MessageType.ERR_INVALID_DURATION_VALUE));
            return true;
        }

        User user = pm().getUser(player);
        if (user == null)
        {
            sender.sendMessage(Lang.translate(MessageType.ERR_USER_NOT_EXISTING, player));
            return true;
        }

        if (server == null)
        {
            if (!user.hasTimedPermSet(perm))
            {
                pm().addUserTimedPerm(user, null, null, new TimedValue(perm, new Date(), duration));
                sender.sendMessage(Lang.translate(MessageType.USER_ADDED_PERM, perm, user.getName()));
            }
            else
            {
                sender.sendMessage(Lang.translate(MessageType.USER_ALREADY_HAS_PERM, user.getName(), perm));
            }
        }
        else
        {
            Server srv = user.getServer(server);

            if (world == null)
            {
                if (!srv.hasTimedPermSet(perm))
                {
                    pm().addUserTimedPerm(user, server, null, new TimedValue(perm, new Date(), duration));
                    sender.sendMessage(Lang.translate(MessageType.USER_ADDED_PERM_SERVER, perm, user.getName(), server));
                }
                else
                {
                    sender.sendMessage(Lang.translate(MessageType.USER_ALREADY_HAS_PERM_SERVER, user.getName(), perm, server));
                }
            }
            else
            {
                World w = srv.getWorld(world);

                if (!w.hasTimedPermSet(perm))
                {
                    pm().addUserTimedPerm(user, server, world, new TimedValue(perm, new Date(), duration));
                    sender.sendMessage(Lang.translate(MessageType.USER_ADDED_PERM_SERVER_WORLD, perm, user.getName(), server, world));
                }
                else
                {
                    sender.sendMessage(Lang.translate(MessageType.USER_ALREADY_HAS_PERM_SERVER_WORLD, user.getName(), perm, server, world));
                }
            }
        }
        return true;
    }

    private boolean handleUserCommandsTimedPermRemove(Sender sender, String[] args)
    {
        if (!checker.hasOrConsole(sender, "bungeeperms.user.perms.remove", true))
        {
            return true;
        }

        if (!Statics.matchArgs(sender, args, 4, 6))
        {
            return true;
        }

        String player = Statics.getFullPlayerName(args[1]);
        String perm = args[3].toLowerCase();
        String server = args.length > 4 ? args[4].toLowerCase() : null;
        String world = args.length > 5 ? args[5].toLowerCase() : null;
        User user = pm().getUser(player);
        if (user == null)
        {
            sender.sendMessage(Lang.translate(MessageType.ERR_USER_NOT_EXISTING, player));
            return true;
        }

        if (server == null)
        {
            if (user.hasTimedPermSet(perm))
            {
                pm().removeUserTimedPerm(user, null, null, perm);
                sender.sendMessage(Lang.translate(MessageType.USER_REMOVED_PERM, perm, user.getName()));
            }
            else
            {
                sender.sendMessage(Lang.translate(MessageType.USER_NEVER_HAD_PERM, user.getName(), perm));
            }
        }
        else
        {
            Server srv = user.getServer(server);

            if (world == null)
            {
                if (srv.hasTimedPermSet(perm))
                {
                    pm().removeUserTimedPerm(user, server, null, perm);
                    sender.sendMessage(Lang.translate(MessageType.USER_REMOVED_PERM_SERVER, perm, user.getName(), server));
                }
                else
                {
                    sender.sendMessage(Lang.translate(MessageType.USER_NEVER_HAD_PERM_SERVER, user.getName(), perm, server));
                }
            }
            else
            {
                World w = srv.getWorld(world);

                if (w.hasTimedPermSet(perm))
                {
                    pm().removeUserTimedPerm(user, server, world, perm);
                    sender.sendMessage(Lang.translate(MessageType.USER_REMOVED_PERM_SERVER_WORLD, perm, user.getName(), server, world));
                }
                else
                {
                    sender.sendMessage(Lang.translate(MessageType.USER_NEVER_HAD_PERM_SERVER_WORLD, user.getName(), perm, server, world));
                }
            }
        }
        return true;
    }

    private boolean handleUserCommandsHas(Sender sender, String[] args)
    {
        if (!checker.hasOrConsole(sender, "bungeeperms.user.perms.has", true))
        {
            return true;
        }

        if (!Statics.matchArgs(sender, args, 4, 6))
        {
            return true;
        }

        String player = Statics.getFullPlayerName(args[1]);
        String perm = args[3].toLowerCase();
        String server = args.length > 4 ? args[4].toLowerCase() : null;
        String world = args.length > 5 ? args[5].toLowerCase() : null;
        User user = pm().getUser(player);
        if (user == null)
        {
            sender.sendMessage(Lang.translate(MessageType.ERR_USER_NOT_EXISTING, player));
            return true;
        }

        //global perm
        if (server == null)
        {
            boolean has = checker.hasPerm(player, perm.toLowerCase());
            sender.sendMessage(Lang.translate(MessageType.USER_HAS_PERM, user.getName(), perm, formatBool(has)));
        }

        //per server perm
        else if (world == null)
        {
            boolean has = checker.hasPermOnServer(user.getName(), perm.toLowerCase(), server);
            sender.sendMessage(Lang.translate(MessageType.USER_HAS_PERM_SERVER, user.getName(), perm, server, formatBool(has)));
        }

        //per server world perm
        else
        {
            boolean has = checker.hasPermOnServerInWorld(user.getName(), perm.toLowerCase(), server, world);
            sender.sendMessage(Lang.translate(MessageType.USER_HAS_PERM_SERVER_WORLD, user.getName(), perm, server, world, formatBool(has)));
        }
        return true;
    }

    private boolean handleUserCommandsGroupAdd(Sender sender, String[] args)
    {
        if (!checker.hasOrConsole(sender, "bungeeperms.user.group.add", true))
        {
            return true;
        }

        if (!Statics.matchArgs(sender, args, 4, 6))
        {
            return true;
        }

        String player = Statics.getFullPlayerName(args[1]);
        String groupname = args[3];
        String server = args.length > 4 ? args[4].toLowerCase() : null;
        String world = args.length > 5 ? args[5].toLowerCase() : null;
        Group group = pm().getGroup(groupname);
        if (group == null)
        {
            sender.sendMessage(Lang.translate(MessageType.ERR_GROUP_NOT_EXISTING, groupname));
            return true;
        }

        User u = pm().getUser(player);
        if (u == null)
        {
            sender.sendMessage(Lang.translate(MessageType.ERR_USER_NOT_EXISTING, player));
            return true;
        }
        Permable permable = u;
        if (server != null)
        {
            permable = u.getServer(server);
            if (world != null)
            {
                permable = u.getServer(server).getWorld(world);
            }
        }

        for (String g : permable.getGroupsString())
        {
            if (g.equalsIgnoreCase(group.getName()))
            {
                sender.sendMessage(Lang.translate(MessageType.ERR_USER_ALREADY_IN_GROUP, groupname));
                return true;
            }
        }

        pm().addUserGroup(u, group, server, world);
        if (server == null)
            sender.sendMessage(Lang.translate(MessageType.USER_ADDED_GROUP, groupname, u.getName()));
        else if (world == null)
            sender.sendMessage(Lang.translate(MessageType.USER_ADDED_GROUP_SERVER, groupname, u.getName(), server));
        else
            sender.sendMessage(Lang.translate(MessageType.USER_ADDED_GROUP_SERVER_WORLD, groupname, u.getName(), server, world));
        return true;
    }

    private boolean handleUserCommandsGroupRemove(Sender sender, String[] args)
    {
        if (!checker.hasOrConsole(sender, "bungeeperms.user.group.remove", true))
        {
            return true;
        }

        if (!Statics.matchArgs(sender, args, 4, 6))
        {
            return true;
        }

        String player = Statics.getFullPlayerName(args[1]);
        String groupname = args[3];
        String server = args.length > 4 ? args[4].toLowerCase() : null;
        String world = args.length > 5 ? args[5].toLowerCase() : null;
        Group group = pm().getGroup(groupname);
        if (group == null)
        {
            sender.sendMessage(Lang.translate(MessageType.ERR_GROUP_NOT_EXISTING, groupname));
            return true;
        }

        User u = pm().getUser(player);
        if (u == null)
        {
            sender.sendMessage(Lang.translate(MessageType.ERR_USER_NOT_EXISTING, player));
            return true;
        }

        Permable permable = u;
        if (server != null)
        {
            permable = u.getServer(server);
            if (world != null)
            {
                permable = u.getServer(server).getWorld(world);
            }
        }

        for (String g : permable.getGroupsString())
        {
            if (g.equalsIgnoreCase(group.getName()))
            {
                pm().removeUserGroup(u, group, server, world);
                if (server == null)
                    sender.sendMessage(Lang.translate(MessageType.USER_REMOVED_GROUP, groupname, u.getName()));
                else if (world == null)
                    sender.sendMessage(Lang.translate(MessageType.USER_REMOVED_GROUP_SERVER, groupname, u.getName(), server));
                else
                    sender.sendMessage(Lang.translate(MessageType.USER_REMOVED_GROUP_SERVER_WORLD, groupname, u.getName(), server, world));
                return true;
            }
        }
        sender.sendMessage(Lang.translate(MessageType.ERR_USER_NOT_IN_GROUP, groupname));
        return true;
    }

    private boolean handleUserCommandsTimedGroupAdd(Sender sender, String[] args)
    {
        if (!checker.hasOrConsole(sender, "bungeeperms.user.group.add", true))
        {
            return true;
        }

        if (!Statics.matchArgs(sender, args, 5, 7))
        {
            return true;
        }

        String player = Statics.getFullPlayerName(args[1]);
        String groupname = args[3];
        Integer duration = parseDuration(args[4]);
        String server = args.length > 5 ? args[5].toLowerCase() : null;
        String world = args.length > 6 ? args[6].toLowerCase() : null;

        if (duration == null || duration < 0)
        {
            sender.sendMessage(Lang.translate(MessageType.ERR_INVALID_DURATION_VALUE));
            return true;
        }

        Group group = pm().getGroup(groupname);
        if (group == null)
        {
            sender.sendMessage(Lang.translate(MessageType.ERR_GROUP_NOT_EXISTING, groupname));
            return true;
        }

        User u = pm().getUser(player);
        if (u == null)
        {
            sender.sendMessage(Lang.translate(MessageType.ERR_USER_NOT_EXISTING, player));
            return true;
        }

        Permable permable = u;
        if (server != null)
        {
            permable = u.getServer(server);
            if (world != null)
            {
                permable = u.getServer(server).getWorld(world);
            }
        }

        for (String g : permable.getGroupsString())
        {
            if (g.equalsIgnoreCase(group.getName()))
            {
                sender.sendMessage(Lang.translate(MessageType.ERR_USER_ALREADY_IN_GROUP, groupname));
                return true;
            }
        }
        for (TimedValue<String> g : permable.getTimedGroupsString())
        {
            if (g.getValue().equalsIgnoreCase(group.getName()))
            {
                sender.sendMessage(Lang.translate(MessageType.ERR_USER_ALREADY_IN_GROUP, groupname));
                return true;
            }
        }

        pm().addUserTimedGroup(u, new TimedValue<>(group, new Date(), duration), server, world);
        if (server == null)
            sender.sendMessage(Lang.translate(MessageType.USER_ADDED_GROUP, groupname, u.getName()));
        else if (world == null)
            sender.sendMessage(Lang.translate(MessageType.USER_ADDED_GROUP_SERVER, groupname, u.getName(), server));
        else
            sender.sendMessage(Lang.translate(MessageType.USER_ADDED_GROUP_SERVER_WORLD, groupname, u.getName(), server, world));
        return true;
    }

    private boolean handleUserCommandsTimedGroupRemove(Sender sender, String[] args)
    {
        if (!checker.hasOrConsole(sender, "bungeeperms.user.group.remove", true))
        {
            return true;
        }

        if (!Statics.matchArgs(sender, args, 4, 6))
        {
            return true;
        }

        String player = Statics.getFullPlayerName(args[1]);
        String groupname = args[3];
        String server = args.length > 4 ? args[4].toLowerCase() : null;
        String world = args.length > 5 ? args[5].toLowerCase() : null;
        Group group = pm().getGroup(groupname);
        if (group == null)
        {
            sender.sendMessage(Lang.translate(MessageType.ERR_GROUP_NOT_EXISTING, groupname));
            return true;
        }

        User u = pm().getUser(player);
        if (u == null)
        {
            sender.sendMessage(Lang.translate(MessageType.ERR_USER_NOT_EXISTING, player));
            return true;
        }

        Permable permable = u;
        if (server != null)
        {
            permable = u.getServer(server);
            if (world != null)
            {
                permable = u.getServer(server).getWorld(world);
            }
        }

        for (TimedValue<String> g : permable.getTimedGroupsString())
        {
            if (g.getValue().equalsIgnoreCase(group.getName()))
            {
                pm().removeUserTimedGroup(u, group, server, world);
                if (server == null)
                    sender.sendMessage(Lang.translate(MessageType.USER_REMOVED_GROUP, groupname, u.getName()));
                else if (world == null)
                    sender.sendMessage(Lang.translate(MessageType.USER_REMOVED_GROUP_SERVER, groupname, u.getName(), server));
                else
                    sender.sendMessage(Lang.translate(MessageType.USER_REMOVED_GROUP_SERVER_WORLD, groupname, u.getName(), server, world));
                return true;
            }
        }
        sender.sendMessage(Lang.translate(MessageType.ERR_USER_NOT_IN_GROUP, groupname));
        return true;
    }

    private boolean handleUserCommandsGroupSet(Sender sender, String[] args)
    {
        if (!checker.hasOrConsole(sender, "bungeeperms.user.group.set", true))
        {
            return true;
        }

        if (!Statics.matchArgs(sender, args, 4))
        {
            return true;
        }

        String player = Statics.getFullPlayerName(args[1]);
        String groupname = args[3];
        Group group = pm().getGroup(groupname);
        if (group == null)
        {
            sender.sendMessage(Lang.translate(MessageType.ERR_GROUP_NOT_EXISTING, groupname));
            return true;
        }

        User u = pm().getUser(player);
        if (u == null)
        {
            sender.sendMessage(Lang.translate(MessageType.ERR_USER_NOT_EXISTING, player));
            return true;
        }

        List<Group> laddergroups = pm().getLadderGroups(group.getLadder());
        for (Group g : laddergroups)
        {
            pm().removeUserGroup(u, g);
        }

        pm().addUserGroup(u, group);
        sender.sendMessage(Lang.translate(MessageType.USER_SET_GROUP, groupname, u.getName()));
        return true;
    }

    private boolean handleUserCommandsDisplay(Sender sender, String[] args)
    {
        if (!checker.hasOrConsole(sender, "bungeeperms.user.display", true))
        {
            return true;
        }

        if (!Statics.matchArgs(sender, args, 3, 6))
        {
            return true;
        }

        String player = Statics.getFullPlayerName(args[1]);
        String display = args.length > 3 ? args[3] : null;
        String server = args.length > 4 ? args[4].toLowerCase() : null;
        String world = args.length > 5 ? args[5].toLowerCase() : null;
        User user = pm().getUser(player);
        if (user == null)
        {
            sender.sendMessage(Lang.translate(MessageType.ERR_USER_NOT_EXISTING, player));
            return true;
        }
        pm().setUserDisplay(user, display, server, world);
        sender.sendMessage(Lang.translate(MessageType.USER_SET_DISPLAY, user.getName()));
        return true;
    }

    private boolean handleUserCommandsPrefix(Sender sender, String[] args)
    {
        if (!checker.hasOrConsole(sender, "bungeeperms.user.prefix", true))
        {
            return true;
        }

        if (!Statics.matchArgs(sender, args, 3, 6))
        {
            return true;
        }

        String player = Statics.getFullPlayerName(args[1]);
        String prefix = args.length > 3 ? args[3] : null;
        String server = args.length > 4 ? args[4].toLowerCase() : null;
        String world = args.length > 5 ? args[5].toLowerCase() : null;
        User user = pm().getUser(player);
        if (user == null)
        {
            sender.sendMessage(Lang.translate(MessageType.ERR_USER_NOT_EXISTING, player));
            return true;
        }
        pm().setUserPrefix(user, prefix, server, world);
        sender.sendMessage(Lang.translate(MessageType.USER_SET_PREFIX, user.getName()));
        return true;
    }

    private boolean handleUserCommandsSuffix(Sender sender, String[] args)
    {
        if (!checker.hasOrConsole(sender, "bungeeperms.user.suffix", true))
        {
            return true;
        }

        if (!Statics.matchArgs(sender, args, 3, 6))
        {
            return true;
        }

        String player = Statics.getFullPlayerName(args[1]);
        String suffix = args.length > 3 ? args[3] : null;
        String server = args.length > 4 ? args[4].toLowerCase() : null;
        String world = args.length > 5 ? args[5].toLowerCase() : null;
        User user = pm().getUser(player);
        if (user == null)
        {
            sender.sendMessage(Lang.translate(MessageType.ERR_USER_NOT_EXISTING, player));
            return true;
        }
        pm().setUserSuffix(user, suffix, server, world);
        sender.sendMessage(Lang.translate(MessageType.USER_SET_SUFFIX, user.getName()));
        return true;
    }
//end user commands

    private boolean handleGroups(Sender sender, String[] args)
    {
        if (!checker.hasOrConsole(sender, "bungeeperms.groups", true))
        {
            return true;
        }

        if (!Statics.matchArgs(sender, args, 1))
        {
            return true;
        }

        if (pm().getGroups().isEmpty())
        {
            sender.sendMessage(Lang.translate(MessageType.NO_GROUPS_FOUND));
        }
        else
        {
            sender.sendMessage(Lang.translate(MessageType.GROUPS_LIST_HEADER));
            for (String l : pm().getLadders())
            {
                List<Group> lg = pm().getLadderGroups(l);
                lg.sort(new Comparator<Group>()
                {
                    @Override
                    public int compare(Group o1, Group o2)
                    {
                        return String.CASE_INSENSITIVE_ORDER.compare(o1.getName(), o2.getName());
                    }
                });
                for (Group g : lg)
                {
                    sender.sendMessage(Color.Text + "- " + Color.Value + g.getName() + Color.Text + " (" + Color.Value + l + Color.Text + ")");
                }
            }
        }
        return true;
    }

    private boolean handleGroupCommands(Sender sender, String[] args)
    {
        if (args.length < 3)
        {
            sender.sendMessage(Lang.translate(MessageType.COMMAND_TOO_FEW_ARGUMENTS));
            return true;
        }

        if (Statics.argAlias(args[2], "list", "listonly"))
        {
            return handleGroupCommandsList(sender, args);
        }
        else if (args[2].equalsIgnoreCase("info"))
        {
            return handleGroupCommandsInfo(sender, args);
        }
        else if (args[2].equalsIgnoreCase("users"))
        {
            return handleGroupCommandsUsers(sender, args);
        }
        else if (args[2].equalsIgnoreCase("create"))
        {
            return handleGroupCommandsCreate(sender, args);
        }
        else if (args[2].equalsIgnoreCase("delete"))
        {
            return handleGroupCommandsDelete(sender, args);
        }
        else if (Statics.argAlias(args[2], "add", "addperm", "addpermission"))
        {
            return handleGroupCommandsPermAdd(sender, args);
        }
        else if (Statics.argAlias(args[2], "remove", "removeperm", "removepermission"))
        {
            return handleGroupCommandsPermRemove(sender, args);
        }
        else if (Statics.argAlias(args[2], "timedadd", "addtimed", "timedaddperm", "addtimedperm", "addpermtimed"))
        {
            return handleGroupCommandsTimedPermAdd(sender, args);
        }
        else if (Statics.argAlias(args[2], "timedremove", "removetimed", "timedremoveperm", "removetimedperm", "removepermtimed"))
        {
            return handleGroupCommandsTimedPermRemove(sender, args);
        }
        else if (Statics.argAlias(args[2], "has", "check"))
        {
            return handleGroupCommandsHas(sender, args);
        }
        else if (Statics.argAlias(args[2], "addinherit", "addinheritance"))
        {
            return handleGroupCommandsInheritAdd(sender, args);
        }
        else if (Statics.argAlias(args[2], "removeinherit", "removeinheritance"))
        {
            return handleGroupCommandsInheritRemove(sender, args);
        }
        else if (Statics.argAlias(args[2], "addtimedinherit", "addtimedinheritance"))
        {
            return handleGroupCommandsTimedInheritAdd(sender, args);
        }
        else if (Statics.argAlias(args[2], "removetimedinherit", "removetimedinheritance"))
        {
            return handleGroupCommandsTimedInheritRemove(sender, args);
        }
        else if (args[2].equalsIgnoreCase("rank"))
        {
            return handleGroupCommandsRank(sender, args);
        }
        else if (args[2].equalsIgnoreCase("weight"))
        {
            return handleGroupCommandsWeight(sender, args);
        }
        else if (args[2].equalsIgnoreCase("ladder"))
        {
            return handleGroupCommandsLadder(sender, args);
        }
        else if (args[2].equalsIgnoreCase("default"))
        {
            return handleGroupCommandsDefault(sender, args);
        }
        else if (args[2].equalsIgnoreCase("display"))
        {
            return handleGroupCommandsDisplay(sender, args);
        }
        else if (args[2].equalsIgnoreCase("prefix"))
        {
            return handleGroupCommandsPrefix(sender, args);
        }
        else if (args[2].equalsIgnoreCase("suffix"))
        {
            return handleGroupCommandsSuffix(sender, args);
        }

        //alias handling
        else if (Statics.argAlias(args[2], "perm", "permission", "timed"))
        {
            if (!Statics.matchArgs(sender, args, 5))
            {
                return true;
            }

            String[] newargs =
            {
                args[0], args[1], args[3] + args[2], args[4]
            };
            return onCommand(sender, "bungeeperms", "bp", newargs);
        }
        return false;
    }

//group commands
    private boolean handleGroupCommandsList(Sender sender, String[] args)
    {
        if (!checker.hasOrConsole(sender, "bungeeperms.group.perms.list", true))
        {
            return true;
        }

        if (!Statics.matchArgs(sender, args, 3, 6))
        {
            return true;
        }

        boolean specialpage = args.length > 3 && Statics.isInt(args[args.length - 1]);
        int page = specialpage ? Integer.parseInt(args[args.length - 1]) : 1;
        if (page < 1)
        {
            sender.sendMessage(Lang.translate(MessageType.ERR_INVALID_INT_VALUE));
            return true;
        }

        String groupname = args[1];
        boolean only = args[2].equalsIgnoreCase("listonly");
        String server = args.length > (3 + (specialpage ? 1 : 0)) ? args[3].toLowerCase() : null;
        String world = args.length > (4 + (specialpage ? 1 : 0)) ? args[4].toLowerCase() : null;
        Group group = pm().getGroup(groupname);

        if (group == null)
        {
            sender.sendMessage(Lang.translate(MessageType.ERR_GROUP_NOT_EXISTING, groupname));
            return true;
        }

        sender.sendMessage(Lang.translate(MessageType.GROUP_PERMISSIONS_LIST_HEADER, group.getName()));
        List<BPPermission> perms = group.getPermsWithOrigin(server, world);
        list(sender, perms, groupname, page, only, server, world);
//        for (int i = (page - 1) * 20; i < page * 20 && i < perms.size(); i++)
//        {
//            BPPermission perm = perms.get(i);
//            String dur = formatDuration(perm);
//            sender.sendMessage(Lang.translate(MessageType.PERMISSIONS_LIST_ITEM,
//                                              perm.getPermission(),
//                                              (perm.getOrigin().equalsIgnoreCase(groupname) ? Lang.translate(MessageType.OWN) : perm.getOrigin()),
//                                              (perm.getServer() != null ? " | " + Color.Value + perm.getServer() + Color.Text : ""),
//                                              (perm.getWorld() != null ? " | " + Color.Value + perm.getWorld() + Color.Text : ""),
//                                              dur == null ? "" : ", " + Color.Value + dur + Color.Text));
//        }
        return true;
    }

    private boolean handleGroupCommandsInfo(Sender sender, String[] args)
    {
        if (!checker.hasOrConsole(sender, "bungeeperms.group.info", true))
        {
            return true;
        }

        if (!Statics.matchArgs(sender, args, 3, 5))
        {
            return true;
        }

        String groupname = args[1];
        String server = args.length > 3 ? args[3].toLowerCase() : null;
        String world = args.length > 4 ? args[4].toLowerCase() : null;

        Group group = pm().getGroup(groupname);
        if (group == null)
        {
            sender.sendMessage(Lang.translate(MessageType.ERR_GROUP_NOT_EXISTING, groupname));
            return true;
        }

        sender.sendMessage(Lang.translate(MessageType.GROUP_ABOUT, group.getName()));

        //inheritances
        List<SimpleEntry<Group, String>> inherits = new ArrayList();
        String inheritancesstr = "";
        List<Group> inherit = group.getInheritances();
        List<TimedValue<Group>> timedinherit = group.getTimedInheritances();
        if (server != null)
        {
            inherit.addAll(group.getServer(server).getGroups());
            timedinherit.addAll(group.getServer(server).getTimedGroups());
            if (world != null)
            {
                inherit.addAll(group.getServer(server).getWorld(world).getGroups());
                timedinherit.addAll(group.getServer(server).getWorld(world).getTimedGroups());
            }
        }
        for (int i = 0; i < inherit.size(); i++)
        {
            inherits.add(new SimpleEntry(inherit.get(i), Color.Value + inherit.get(i).getName() + Color.Text
                                                         + " (" + Color.Value + inherit.get(i).getPerms().size() + Color.Text + ")"));
        }
        for (int i = 0; i < timedinherit.size(); i++)
        {
            inherits.add(new SimpleEntry(inherit.get(i), Color.Value + timedinherit.get(i).getValue().getName() + Color.Text
                                                         + " (" + Color.Value + timedinherit.get(i).getValue().getPerms().size() + Color.Text + "|"
                                                         + Color.Value + formatDuration(timedinherit.get(i)) + Color.Text + ")"));
        }
        inherits.sort(new Comparator<SimpleEntry<Group, String>>()
        {
            @Override
            public int compare(SimpleEntry<Group, String> o1, SimpleEntry<Group, String> o2)
            {
                return Group.WEIGHT_COMPARATOR.compare(o1.getKey(), o2.getKey());
            }
        });
        if (inherits.isEmpty())
            inheritancesstr = Color.Text + "(" + Lang.translate(MessageType.NONE) + ")";
        else
            for (int i = 0; i < inherits.size(); i++)
                inheritancesstr += inherits.get(i).getValue() + (i + 1 < inherits.size() ? ", " : "");
        sender.sendMessage(Lang.translate(MessageType.GROUP_INHERITANCES, inheritancesstr));

        //group perms
        sender.sendMessage(Lang.translate(MessageType.GROUP_PERMISSONS, group.getOwnPermissionsCount(server, world)));

        //all group perms
        sender.sendMessage(Lang.translate(MessageType.GROUP_ALL_PERMISSIONS, group.getPermissionsCount(server, world)));

        //group rank
        sender.sendMessage(Lang.translate(MessageType.GROUP_RANK, group.getRank()));

        //group weight
        sender.sendMessage(Lang.translate(MessageType.GROUP_WEIGHT, group.getWeight()));

        //group ladder
        sender.sendMessage(Lang.translate(MessageType.GROUP_LADDER, group.getLadder()));

        //default
        sender.sendMessage(Lang.translate(MessageType.GROUP_DEFAULT, formatBool(group.isDefault())));

        //prepare displayables
        Permable perm = group;
        if (server != null)
        {
            perm = ((Group) perm).getServer(server);
            if (world != null)
                perm = ((Server) perm).getWorld(world);
        }

        //display
        sender.sendMessage(Lang.translatePreserveArgs(MessageType.DISPLAY, (!Statics.isEmpty(perm.getDisplay()) ? perm.getDisplay().replaceAll("" + ChatColor.COLOR_CHAR, "&") : Color.Text + "(" + Lang.translate(MessageType.NONE) + ")")));

        //prefix
        sender.sendMessage(Lang.translatePreserveArgs(MessageType.PREFIX, (!Statics.isEmpty(perm.getPrefix()) ? perm.getPrefix().replaceAll("" + ChatColor.COLOR_CHAR, "&") : Color.Text + "(" + Lang.translate(MessageType.NONE) + ")")));

        //suffix
        sender.sendMessage(Lang.translatePreserveArgs(MessageType.SUFFIX, (!Statics.isEmpty(perm.getSuffix()) ? perm.getSuffix().replaceAll("" + ChatColor.COLOR_CHAR, "&") : Color.Text + "(" + Lang.translate(MessageType.NONE) + ")")));

        //full prefix
        String buildPrefix = group.buildPrefix(server, world);
        sender.sendMessage(Lang.translatePreserveArgs(MessageType.PREFIX_FULL, (!Statics.isEmpty(buildPrefix) ? buildPrefix.replaceAll("" + ChatColor.COLOR_CHAR, "&") : Color.Text + "(" + Lang.translate(MessageType.NONE) + ")")));

        //full suffix
        String buildSuffix = group.buildSuffix(server, world);
        sender.sendMessage(Lang.translatePreserveArgs(MessageType.SUFFIX_FULL, (!Statics.isEmpty(buildSuffix) ? buildSuffix.replaceAll("" + ChatColor.COLOR_CHAR, "&") : Color.Text + "(" + Lang.translate(MessageType.NONE) + ")")));
        return true;
    }

    private boolean handleGroupCommandsUsers(Sender sender, String[] args)
    {
        if (!checker.hasOrConsole(sender, "bungeeperms.group.users", true))
        {
            return true;
        }

        if (args.length > 4)
        {
            sender.sendMessage(Lang.translate(MessageType.COMMAND_TOO_MANY_ARGUMENTS));
            return true;
        }

        String groupname = args[1];
        Group group = pm().getGroup(groupname);
        if (group == null)
        {
            sender.sendMessage(Lang.translate(MessageType.ERR_GROUP_NOT_EXISTING, groupname));
            return true;
        }

        if (args.length == 3)
        {
            if (config.isUseUUIDs())
            {
                Map<UUID, String> usersmap = pm().getGroupUsersUUID(group);
                if (usersmap.isEmpty())
                {
                    sender.sendMessage(Lang.translate(MessageType.NO_USERS_FOUND));
                    return true;
                }

                List<Map.Entry<UUID, String>> users = new ArrayList(usersmap.entrySet());
                users.sort(new Comparator<Map.Entry<UUID, String>>()
                {
                    @Override
                    public int compare(Map.Entry<UUID, String> o1, Map.Entry<UUID, String> o2)
                    {
                        return String.CASE_INSENSITIVE_ORDER.compare(o1.getValue(), o2.getValue());
                    }
                });

                String out = Lang.translate(MessageType.GROUP_USERS_HEADER, group.getName());
                for (int i = 0; i < users.size(); i++)
                {
                    //mc chat packet length; string most likely 1 byte/char so 1.1 bytes/char should be safe
                    if (out.length() * 1.1 > Short.MAX_VALUE)
                    {
                        sender.sendMessage(out);
                        out = Lang.translate(MessageType.GROUP_USERS_HEADER, group.getName());
                    }
                    out += Color.User + users.get(i).getValue() + Color.Text + " (" + Color.User + users.get(i).getKey() + Color.Text + ")" + (i + 1 < users.size() ? ", " : "");
                }
                sender.sendMessage(out);
                return true;
            }
            else
            {
                List<String> users = pm().getGroupUsers(group);
                if (users.isEmpty())
                {
                    sender.sendMessage(Lang.translate(MessageType.NO_USERS_FOUND));
                    return true;
                }

                users.sort(String.CASE_INSENSITIVE_ORDER);

                String out = Lang.translate(MessageType.GROUP_USERS_HEADER, group.getName());
                for (int i = 0; i < users.size(); i++)
                {
                    //mc chat packet length; string most likely 1 byte/char so 1.1 bytes/char should be safe
                    if (out.length() * 1.1 > Short.MAX_VALUE)
                    {
                        sender.sendMessage(out);
                        out = Lang.translate(MessageType.GROUP_USERS_HEADER, group.getName());
                    }
                    out += Color.User + users.get(i) + Color.Text + (i + 1 < users.size() ? ", " : "");
                }
                sender.sendMessage(out);
                return true;
            }
        }
        else if (args.length == 4)
        {
            if (!args[3].equalsIgnoreCase("-c"))
            {
                return false;
            }
            List<String> users = pm().getGroupUsers(group);
            sender.sendMessage(Lang.translate(MessageType.GROUP_USERS_HEADER_COUNT, users.size(), group.getName()));
            return true;
        }
        return true;
    }

    private boolean handleGroupCommandsCreate(Sender sender, String[] args)
    {
        if (!checker.hasOrConsole(sender, "bungeeperms.group.create", true))
        {
            return true;
        }

        if (!Statics.matchArgs(sender, args, 3))
        {
            return true;
        }

        String groupname = args[1];
        if (pm().getGroup(groupname) != null)
        {
            sender.sendMessage(Lang.translate(MessageType.ERR_GROUP_ALREADY_EXISTS, groupname));
            return true;
        }
        Group group = new Group(groupname, new ArrayList(), new ArrayList(), new ArrayList(), new ArrayList(), new HashMap(), 1000, 1000, "default", false, null, null, null);
        pm().addGroup(group);
        sender.sendMessage(Lang.translate(MessageType.GROUP_CREATED, groupname));
        return true;
    }

    private boolean handleGroupCommandsDelete(Sender sender, String[] args)
    {
        if (!checker.hasOrConsole(sender, "bungeeperms.group.delete", true))
        {
            return true;
        }

        if (!Statics.matchArgs(sender, args, 3))
        {
            return true;
        }

        String groupname = args[1];
        Group group = pm().getGroup(groupname);
        if (group == null)
        {
            sender.sendMessage(Lang.translate(MessageType.ERR_GROUP_NOT_EXISTING, groupname));
            return true;
        }

        sender.sendMessage(Lang.translate(MessageType.GROUP_DELETION_IN_PROGRESS));
        pm().deleteGroup(group);
        sender.sendMessage(Lang.translate(MessageType.GROUP_DELETED, group.getName()));
        return true;
    }

    private boolean handleGroupCommandsPermAdd(Sender sender, String[] args)
    {
        if (!checker.hasOrConsole(sender, "bungeeperms.group.perms.add", true))
        {
            return true;
        }

        if (!Statics.matchArgs(sender, args, 4, 6))
        {
            return true;
        }

        String groupname = args[1];
        String perm = args[3].toLowerCase();
        String server = args.length > 4 ? args[4].toLowerCase() : null;
        String world = args.length > 5 ? args[5].toLowerCase() : null;
        Group group = pm().getGroup(groupname);
        if (group == null)
        {
            sender.sendMessage(Lang.translate(MessageType.ERR_GROUP_NOT_EXISTING, groupname));
            return true;
        }

        //global perm
        if (server == null)
        {
            if (!group.getPerms().contains(perm))
            {
                pm().addGroupPerm(group, null, null, perm);
                sender.sendMessage(Lang.translate(MessageType.GROUP_ADDED_PERM, perm, group.getName()));
            }
            else
            {
                sender.sendMessage(Lang.translate(MessageType.GROUP_ALREADY_HAS_PERM, group.getName(), perm));
            }
        }
        else
        {
            Server srv = group.getServer(server);

            //per server perm
            if (world == null)
            {
                List<String> perserverperms = srv.getPerms();
                if (!perserverperms.contains(perm))
                {
                    pm().addGroupPerm(group, server, null, perm);
                    sender.sendMessage(Lang.translate(MessageType.GROUP_ADDED_PERM_SERVER, perm, group.getName(), server));
                }
                else
                {
                    sender.sendMessage(Lang.translate(MessageType.GROUP_ALREADY_HAS_PERM_SERVER, group.getName(), perm, server));
                }
            }

            //per server world perms
            else
            {
                World w = srv.getWorld(world);

                List<String> perserverworldperms = w.getPerms();
                if (!perserverworldperms.contains(perm))
                {
                    pm().addGroupPerm(group, server, world, perm);
                    sender.sendMessage(Lang.translate(MessageType.GROUP_ADDED_PERM_SERVER_WORLD, perm, group.getName(), server, world));
                }
                else
                {
                    sender.sendMessage(Lang.translate(MessageType.GROUP_ALREADY_HAS_PERM_SERVER_WORLD, group.getName(), perm, server, world));
                }
            }
        }
        return true;
    }

    private boolean handleGroupCommandsPermRemove(Sender sender, String[] args)
    {
        if (!checker.hasOrConsole(sender, "bungeeperms.group.perms.remove", true))
        {
            return true;
        }

        if (!Statics.matchArgs(sender, args, 4, 6))
        {
            return true;
        }

        String groupname = args[1];
        String perm = args[3].toLowerCase();
        String server = args.length > 4 ? args[4].toLowerCase() : null;
        String world = args.length > 5 ? args[5].toLowerCase() : null;
        Group group = pm().getGroup(groupname);
        if (group == null)
        {
            sender.sendMessage(Lang.translate(MessageType.ERR_GROUP_NOT_EXISTING, groupname));
            return true;
        }

        //global perm
        if (server == null)
        {
            if (group.getPerms().contains(perm))
            {
                pm().removeGroupPerm(group, null, null, perm);
                sender.sendMessage(Lang.translate(MessageType.GROUP_REMOVED_PERM, perm, group.getName()));
            }
            else
            {
                sender.sendMessage(Lang.translate(MessageType.GROUP_NEVER_HAD_PERM, group.getName(), perm));
            }
        }
        else
        {
            Server srv = group.getServer(server);

            //per server perm
            if (world == null)
            {
                if (srv.getPerms().contains(perm))
                {
                    pm().removeGroupPerm(group, server, null, perm);
                    sender.sendMessage(Lang.translate(MessageType.GROUP_REMOVED_PERM_SERVER, perm, group.getName(), server));
                }
                else
                {
                    sender.sendMessage(Lang.translate(MessageType.GROUP_NEVER_HAD_PERM_SERVER, group.getName(), perm, server));
                }
            }
            else
            {
                World w = srv.getWorld(world);

                if (w.getPerms().contains(perm))
                {
                    pm().removeGroupPerm(group, server, world, perm);
                    sender.sendMessage(Lang.translate(MessageType.GROUP_REMOVED_PERM_SERVER_WORLD, perm, group.getName(), server, world));
                }
                else
                {
                    sender.sendMessage(Lang.translate(MessageType.GROUP_NEVER_HAD_PERM_SERVER_WORLD, group.getName(), perm, server, world));
                }
            }
        }
        return true;
    }

    private boolean handleGroupCommandsTimedPermAdd(Sender sender, String[] args)
    {
        if (!checker.hasOrConsole(sender, "bungeeperms.group.perms.add", true))
        {
            return true;
        }

        if (!Statics.matchArgs(sender, args, 5, 7))
        {
            return true;
        }

        String groupname = args[1];
        String perm = args[3].toLowerCase();
        Integer duration = parseDuration(args[4]);
        String server = args.length > 5 ? args[5].toLowerCase() : null;
        String world = args.length > 6 ? args[6].toLowerCase() : null;

        if (duration == null || duration < 0)
        {
            sender.sendMessage(Lang.translate(MessageType.ERR_INVALID_DURATION_VALUE));
            return true;
        }

        Group group = pm().getGroup(groupname);
        if (group == null)
        {
            sender.sendMessage(Lang.translate(MessageType.ERR_GROUP_NOT_EXISTING, groupname));
            return true;
        }

        //global perm
        if (server == null)
        {
            if (!group.hasTimedPermSet(perm))
            {
                pm().addGroupTimedPerm(group, null, null, new TimedValue(perm, new Date(), duration));
                sender.sendMessage(Lang.translate(MessageType.GROUP_ADDED_PERM, perm, group.getName()));
            }
            else
            {
                sender.sendMessage(Lang.translate(MessageType.GROUP_ALREADY_HAS_PERM, group.getName(), perm));
            }
        }
        else
        {
            Server srv = group.getServer(server);

            //per server perm
            if (world == null)
            {
                if (!srv.hasTimedPermSet(perm))
                {
                    pm().addGroupTimedPerm(group, server, null, new TimedValue(perm, new Date(), duration));
                    sender.sendMessage(Lang.translate(MessageType.GROUP_ADDED_PERM_SERVER, perm, group.getName(), server));
                }
                else
                {
                    sender.sendMessage(Lang.translate(MessageType.GROUP_ALREADY_HAS_PERM_SERVER, group.getName(), perm, server));
                }
            }

            //per server world perms
            else
            {
                World w = srv.getWorld(world);

                if (!w.hasTimedPermSet(perm))
                {
                    pm().addGroupTimedPerm(group, server, world, new TimedValue(perm, new Date(), duration));
                    sender.sendMessage(Lang.translate(MessageType.GROUP_ADDED_PERM_SERVER_WORLD, perm, group.getName(), server, world));
                }
                else
                {
                    sender.sendMessage(Lang.translate(MessageType.GROUP_ALREADY_HAS_PERM_SERVER_WORLD, group.getName(), perm, server, world));
                }
            }
        }
        return true;
    }

    private boolean handleGroupCommandsTimedPermRemove(Sender sender, String[] args)
    {
        if (!checker.hasOrConsole(sender, "bungeeperms.group.perms.remove", true))
        {
            return true;
        }

        if (!Statics.matchArgs(sender, args, 4, 6))
        {
            return true;
        }

        String groupname = args[1];
        String perm = args[3].toLowerCase();
        String server = args.length > 4 ? args[4].toLowerCase() : null;
        String world = args.length > 5 ? args[5].toLowerCase() : null;
        Group group = pm().getGroup(groupname);
        if (group == null)
        {
            sender.sendMessage(Lang.translate(MessageType.ERR_GROUP_NOT_EXISTING, groupname));
            return true;
        }

        //global perm
        if (server == null)
        {
            if (group.hasTimedPermSet(perm))
            {
                pm().removeGroupTimedPerm(group, null, null, perm);
                sender.sendMessage(Lang.translate(MessageType.GROUP_REMOVED_PERM, perm, group.getName()));
            }
            else
            {
                sender.sendMessage(Lang.translate(MessageType.GROUP_NEVER_HAD_PERM, group.getName(), perm));
            }
        }
        else
        {
            Server srv = group.getServer(server);

            //per server perm
            if (world == null)
            {
                if (srv.hasTimedPermSet(perm))
                {
                    pm().removeGroupTimedPerm(group, server, null, perm);
                    sender.sendMessage(Lang.translate(MessageType.GROUP_REMOVED_PERM_SERVER, perm, group.getName(), server));
                }
                else
                {
                    sender.sendMessage(Lang.translate(MessageType.GROUP_NEVER_HAD_PERM_SERVER, group.getName(), perm, server));
                }
            }
            else
            {
                World w = srv.getWorld(world);

                if (w.hasTimedPermSet(perm))
                {
                    pm().removeGroupTimedPerm(group, server, world, perm);
                    sender.sendMessage(Lang.translate(MessageType.GROUP_REMOVED_PERM_SERVER_WORLD, perm, group.getName(), server, world));
                }
                else
                {
                    sender.sendMessage(Lang.translate(MessageType.GROUP_NEVER_HAD_PERM_SERVER_WORLD, group.getName(), perm, server, world));
                }
            }
        }
        return true;
    }

    private boolean handleGroupCommandsHas(Sender sender, String[] args)
    {
        if (!checker.hasOrConsole(sender, "bungeeperms.group.perms.has", true))
        {
            return true;
        }

        if (!Statics.matchArgs(sender, args, 4, 6))
        {
            return true;
        }

        String groupname = args[1];
        String perm = args[3].toLowerCase();
        String server = args.length > 4 ? args[4].toLowerCase() : null;
        String world = args.length > 5 ? args[5].toLowerCase() : null;
        Group group = pm().getGroup(groupname);
        if (group == null)
        {
            sender.sendMessage(Lang.translate(MessageType.ERR_GROUP_NOT_EXISTING, groupname));
            return true;
        }

        //global perm
        if (server == null)
        {
            boolean has = group.has(perm.toLowerCase(), null, null);
            sender.sendMessage(Lang.translate(MessageType.GROUP_HAS_PERM, group.getName(), perm, formatBool(has)));
        }

        //per server perm
        else if (world == null)
        {
            boolean has = group.has(perm.toLowerCase(), server, null);
            sender.sendMessage(Lang.translate(MessageType.GROUP_HAS_PERM_SERVER, group.getName(), perm, server, formatBool(has)));
        }

        //per server world perm
        else
        {
            boolean has = group.has(perm.toLowerCase(), server, world);
            sender.sendMessage(Lang.translate(MessageType.GROUP_HAS_PERM_SERVER_WORLD, group.getName(), perm, server, world, formatBool(has)));
        }
        return true;
    }

    private boolean handleGroupCommandsInheritAdd(Sender sender, String[] args)
    {
        if (!checker.hasOrConsole(sender, "bungeeperms.group.inheritances.add", true))
        {
            return true;
        }

        if (!Statics.matchArgs(sender, args, 4, 6))
        {
            return true;
        }

        String groupname = args[1];
        String addgroup = args[3];
        String server = args.length > 4 ? args[4].toLowerCase() : null;
        String world = args.length > 5 ? args[5].toLowerCase() : null;

        //check group existance
        Group group = pm().getGroup(groupname);
        if (group == null)
        {
            sender.sendMessage(Lang.translate(MessageType.ERR_GROUP_NOT_EXISTING, groupname));
            return true;
        }

        Group toadd = pm().getGroup(addgroup);
        if (toadd == null)
        {
            sender.sendMessage(Lang.translate(MessageType.ERR_GROUP_NOT_EXISTING, addgroup));
            return true;
        }

        Permable permable = group;
        if (server != null)
        {
            permable = group.getServer(server);
            if (world != null)
            {
                permable = group.getServer(server).getWorld(world);
            }
        }

        //check for already existing inheritance
        for (String s : permable.getGroupsString())
        {
            if (s.equalsIgnoreCase(toadd.getName()))
            {
                sender.sendMessage(Lang.translate(MessageType.ERR_GROUP_ALREADY_INHERITS, groupname, addgroup));
                return true;
            }
        }

        pm().addGroupInheritance(group, toadd, server, world);

        if (server == null)
            sender.sendMessage(Lang.translate(MessageType.GROUP_ADDED_INHERITANCE, addgroup, groupname));
        else if (world == null)
            sender.sendMessage(Lang.translate(MessageType.GROUP_ADDED_INHERITANCE_SERVER, addgroup, groupname, server));
        else
            sender.sendMessage(Lang.translate(MessageType.GROUP_ADDED_INHERITANCE_SERVER_WORLD, addgroup, groupname, server, world));
        return true;
    }

    private boolean handleGroupCommandsInheritRemove(Sender sender, String[] args)
    {
        if (!checker.hasOrConsole(sender, "bungeeperms.group.inheritances.remove", true))
        {
            return true;
        }

        if (!Statics.matchArgs(sender, args, 4, 6))
        {
            return true;
        }

        String groupname = args[1];
        String removegroup = args[3];
        String server = args.length > 4 ? args[4].toLowerCase() : null;
        String world = args.length > 5 ? args[5].toLowerCase() : null;

        Group group = pm().getGroup(groupname);
        if (group == null)
        {
            sender.sendMessage(Lang.translate(MessageType.ERR_GROUP_NOT_EXISTING, groupname));
            return true;
        }

        Group toremove = pm().getGroup(removegroup);
        if (toremove == null)
        {
            sender.sendMessage(Lang.translate(MessageType.ERR_GROUP_NOT_EXISTING, removegroup));
            return true;
        }

        Permable permable = group;
        if (server != null)
        {
            permable = group.getServer(server);
            if (world != null)
            {
                permable = group.getServer(server).getWorld(world);
            }
        }

        for (String s : permable.getGroupsString())
        {
            if (s.equalsIgnoreCase(toremove.getName()))
            {
                pm().removeGroupInheritance(group, toremove, server, world);

                if (server == null)
                    sender.sendMessage(Lang.translate(MessageType.GROUP_REMOVED_INHERITANCE, removegroup, groupname));
                else if (world == null)
                    sender.sendMessage(Lang.translate(MessageType.GROUP_REMOVED_INHERITANCE_SERVER, removegroup, groupname, server));
                else
                    sender.sendMessage(Lang.translate(MessageType.GROUP_REMOVED_INHERITANCE_SERVER_WORLD, removegroup, groupname, server, world));
                return true;
            }
        }
        sender.sendMessage(Lang.translate(MessageType.ERR_GROUP_DOES_NOT_INHERIT, groupname, removegroup));
        return true;
    }

    private boolean handleGroupCommandsTimedInheritAdd(Sender sender, String[] args)
    {
        if (!checker.hasOrConsole(sender, "bungeeperms.group.inheritances.add", true))
        {
            return true;
        }

        if (!Statics.matchArgs(sender, args, 5, 7))
        {
            return true;
        }

        String groupname = args[1];
        String addgroup = args[3];
        Integer duration = parseDuration(args[4]);
        String server = args.length > 5 ? args[5].toLowerCase() : null;
        String world = args.length > 6 ? args[6].toLowerCase() : null;

        if (duration == null || duration < 0)
        {
            sender.sendMessage(Lang.translate(MessageType.ERR_INVALID_DURATION_VALUE));
            return true;
        }

        //check group existance
        Group group = pm().getGroup(groupname);
        if (group == null)
        {
            sender.sendMessage(Lang.translate(MessageType.ERR_GROUP_NOT_EXISTING, groupname));
            return true;
        }

        Group toadd = pm().getGroup(addgroup);
        if (toadd == null)
        {
            sender.sendMessage(Lang.translate(MessageType.ERR_GROUP_NOT_EXISTING, addgroup));
            return true;
        }

        Permable permable = group;
        if (server != null)
        {
            permable = group.getServer(server);
            if (world != null)
            {
                permable = group.getServer(server).getWorld(world);
            }
        }

        //check for already existing inheritance
        for (String s : permable.getGroupsString())
        {
            if (s.equalsIgnoreCase(toadd.getName()))
            {
                sender.sendMessage(Lang.translate(MessageType.ERR_GROUP_ALREADY_INHERITS, groupname, addgroup));
                return true;
            }
        }
        for (TimedValue<String> s : permable.getTimedGroupsString())
        {
            if (s.getValue().equalsIgnoreCase(toadd.getName()))
            {
                sender.sendMessage(Lang.translate(MessageType.ERR_GROUP_ALREADY_INHERITS, groupname, addgroup));
                return true;
            }
        }

        pm().addGroupTimedInheritance(group, new TimedValue(toadd, new Date(), duration), server, world);

        if (server == null)
            sender.sendMessage(Lang.translate(MessageType.GROUP_ADDED_INHERITANCE, addgroup, groupname));
        else if (world == null)
            sender.sendMessage(Lang.translate(MessageType.GROUP_ADDED_INHERITANCE_SERVER, addgroup, groupname, server));
        else
            sender.sendMessage(Lang.translate(MessageType.GROUP_ADDED_INHERITANCE_SERVER_WORLD, addgroup, groupname, server, world));
        return true;
    }

    private boolean handleGroupCommandsTimedInheritRemove(Sender sender, String[] args)
    {
        if (!checker.hasOrConsole(sender, "bungeeperms.group.inheritances.remove", true))
        {
            return true;
        }

        if (!Statics.matchArgs(sender, args, 4, 6))
        {
            return true;
        }

        String groupname = args[1];
        String removegroup = args[3];
        String server = args.length > 4 ? args[4].toLowerCase() : null;
        String world = args.length > 5 ? args[5].toLowerCase() : null;

        Group group = pm().getGroup(groupname);
        if (group == null)
        {
            sender.sendMessage(Lang.translate(MessageType.ERR_GROUP_NOT_EXISTING, groupname));
            return true;
        }

        Group toremove = pm().getGroup(removegroup);
        if (toremove == null)
        {
            sender.sendMessage(Lang.translate(MessageType.ERR_GROUP_NOT_EXISTING, removegroup));
            return true;
        }

        Permable permable = group;
        if (server != null)
        {
            permable = group.getServer(server);
            if (world != null)
            {
                permable = group.getServer(server).getWorld(world);
            }
        }

        for (TimedValue<String> s : permable.getTimedGroupsString())
        {
            if (s.getValue().equalsIgnoreCase(toremove.getName()))
            {
                pm().removeGroupTimedInheritance(group, toremove, server, world);

                if (server == null)
                    sender.sendMessage(Lang.translate(MessageType.GROUP_REMOVED_INHERITANCE, removegroup, groupname));
                else if (world == null)
                    sender.sendMessage(Lang.translate(MessageType.GROUP_REMOVED_INHERITANCE_SERVER, removegroup, groupname, server));
                else
                    sender.sendMessage(Lang.translate(MessageType.GROUP_REMOVED_INHERITANCE_SERVER_WORLD, removegroup, groupname, server, world));
                return true;
            }
        }
        sender.sendMessage(Lang.translate(MessageType.ERR_GROUP_DOES_NOT_INHERIT, groupname, removegroup));
        return true;
    }

    private boolean handleGroupCommandsRank(Sender sender, String[] args)
    {
        if (!checker.hasOrConsole(sender, "bungeeperms.group.rank", true))
        {
            return true;
        }

        if (!Statics.matchArgs(sender, args, 4))
        {
            return true;
        }

        String groupname = args[1];
        int rank;
        try
        {
            rank = Integer.parseInt(args[3]);
            if (rank < 1)
            {
                throw new Exception();
            }
        }
        catch (Exception e)
        {
            sender.sendMessage(Lang.translate(MessageType.ERR_INVALID_INT_VALUE));
            return true;
        }

        Group group = pm().getGroup(groupname);
        if (group == null)
        {
            sender.sendMessage(Lang.translate(MessageType.ERR_GROUP_NOT_EXISTING, groupname));
            return true;
        }

        pm().rankGroup(group, rank);
        sender.sendMessage(Lang.translate(MessageType.GROUP_SET_RANK, group.getName()));
        return true;
    }

    private boolean handleGroupCommandsWeight(Sender sender, String[] args)
    {
        if (!checker.hasOrConsole(sender, "bungeeperms.group.weight", true))
        {
            return true;
        }

        if (!Statics.matchArgs(sender, args, 4))
        {
            return true;
        }

        String groupname = args[1];
        int weight;
        try
        {
            weight = Integer.parseInt(args[3]);
            if (weight < 1)
            {
                throw new Exception();
            }
        }
        catch (Exception e)
        {
            sender.sendMessage(Lang.translate(MessageType.ERR_INVALID_INT_VALUE));
            return true;
        }
        Group group = pm().getGroup(groupname);
        if (group == null)
        {
            sender.sendMessage(Lang.translate(MessageType.ERR_GROUP_NOT_EXISTING, groupname));
            return true;
        }

        pm().weightGroup(group, weight);
        sender.sendMessage(Lang.translate(MessageType.GROUP_SET_WEIGHT, group.getName()));
        return true;
    }

    private boolean handleGroupCommandsLadder(Sender sender, String[] args)
    {
        if (!checker.hasOrConsole(sender, "bungeeperms.group.ladder", true))
        {
            return true;
        }

        if (!Statics.matchArgs(sender, args, 4))
        {
            return true;
        }

        String groupname = args[1];
        String ladder = args[3];
        Group group = pm().getGroup(groupname);
        if (group == null)
        {
            sender.sendMessage(Lang.translate(MessageType.ERR_GROUP_NOT_EXISTING, groupname));
            return true;
        }
        pm().ladderGroup(group, ladder);
        sender.sendMessage(Lang.translate(MessageType.GROUP_SET_LADDER, group.getName()));
        return true;
    }

    private boolean handleGroupCommandsDefault(Sender sender, String[] args)
    {
        if (!checker.hasOrConsole(sender, "bungeeperms.group.default", true))
        {
            return true;
        }

        if (!Statics.matchArgs(sender, args, 4))
        {
            return true;
        }

        String groupname = args[1];
        boolean isdefault;
        try
        {
            isdefault = parseTrueFalse(args[3]);
        }
        catch (Exception e)
        {
            sender.sendMessage(Lang.translate(MessageType.ERR_INVALID_BOOL_VALUE));
            return true;
        }

        Group group = pm().getGroup(groupname);
        if (group == null)
        {
            sender.sendMessage(Lang.translate(MessageType.ERR_GROUP_NOT_EXISTING, groupname));
            return true;
        }
        pm().setGroupDefault(group, isdefault);
        sender.sendMessage(Lang.translate(MessageType.GROUP_SET_DEFAULT, group.getName(), isdefault ? Lang.translate(MessageType.DEFAULT) : Lang.translate(MessageType.NONDEFAULT)));
        return true;
    }

    private boolean handleGroupCommandsDisplay(Sender sender, String[] args)
    {
        if (!checker.hasOrConsole(sender, "bungeeperms.group.display", true))
        {
            return true;
        }

        if (!Statics.matchArgs(sender, args, 3, 6))
        {
            return true;
        }

        String groupname = args[1];
        String display = args.length > 3 ? args[3] : null;
        String server = args.length > 4 ? args[4].toLowerCase() : null;
        String world = args.length > 5 ? args[5].toLowerCase() : null;
        Group group = pm().getGroup(groupname);
        if (group == null)
        {
            sender.sendMessage(Lang.translate(MessageType.ERR_GROUP_NOT_EXISTING, groupname));
            return true;
        }
        pm().setGroupDisplay(group, display, server, world);
        sender.sendMessage(Lang.translate(MessageType.GROUP_SET_DISPLAY, group.getName()));
        return true;
    }

    private boolean handleGroupCommandsPrefix(Sender sender, String[] args)
    {
        if (!checker.hasOrConsole(sender, "bungeeperms.group.prefix", true))
        {
            return true;
        }

        if (!Statics.matchArgs(sender, args, 3, 6))
        {
            return true;
        }

        String groupname = args[1];
        String prefix = args.length > 3 ? args[3] : null;
        String server = args.length > 4 ? args[4].toLowerCase() : null;
        String world = args.length > 5 ? args[5].toLowerCase() : null;
        Group group = pm().getGroup(groupname);
        if (group == null)
        {
            sender.sendMessage(Lang.translate(MessageType.ERR_GROUP_NOT_EXISTING, groupname));
            return true;
        }
        pm().setGroupPrefix(group, prefix, server, world);
        sender.sendMessage(Lang.translate(MessageType.GROUP_SET_PREFIX, group.getName()));
        return true;
    }

    private boolean handleGroupCommandsSuffix(Sender sender, String[] args)
    {
        if (!checker.hasOrConsole(sender, "bungeeperms.group.suffix", true))
        {
            return true;
        }

        if (!Statics.matchArgs(sender, args, 3, 6))
        {
            return true;
        }

        String groupname = args[1];
        String suffix = args.length > 3 ? args[3] : null;
        String server = args.length > 4 ? args[4].toLowerCase() : null;
        String world = args.length > 5 ? args[5].toLowerCase() : null;
        Group group = pm().getGroup(groupname);
        if (group == null)
        {
            sender.sendMessage(Lang.translate(MessageType.ERR_GROUP_NOT_EXISTING, groupname));
            return true;
        }
        pm().setGroupSuffix(group, suffix, server, world);
        sender.sendMessage(Lang.translate(MessageType.GROUP_SET_SUFFIX, group.getName()));
        return true;
    }
//end group commands

    private boolean handlePromote(Sender sender, String[] args)
    {
        if (!checker.hasOrConsole(sender, "bungeeperms.promote", true))
        {
            return true;
        }

        //2 or 3 args expected
        if (!Statics.matchArgs(sender, args, 2, 3))
        {
            return true;
        }

        //get user
        String player = Statics.getFullPlayerName(args[1]);
        User user = pm().getUser(player);
        if (user == null)
        {
            sender.sendMessage(Lang.translate(MessageType.ERR_USER_NOT_EXISTING, player));
            return true;
        }

        //get next group by ladder
        Group playergroup = null;
        Group nextgroup = null;

        //ladder specified by sender
        if (args.length == 3)
        {
            String ladder = args[2];
            playergroup = user.getGroupByLadder(ladder);
            if (playergroup != null)
            {
                nextgroup = pm().getNextGroup(playergroup);
            }
            else
            {
                List<Group> laddergroups = pm().getLadderGroups(ladder);
                if (!laddergroups.isEmpty())
                {
                    nextgroup = laddergroups.get(0);
                }
            }
        }

        //no ladder specified ... assume main ladder
        else
        {
            playergroup = pm().getMainGroup(user);
            if (playergroup == null)
            {
                sender.sendMessage(Lang.translate(MessageType.ERR_USER_NO_GROUPS, user.getName()));
                return true;
            }
            nextgroup = pm().getNextGroup(playergroup);
        }

        if (nextgroup == null)
        {
            sender.sendMessage(Lang.translate(MessageType.ERR_USER_CANNOT_BE_PROMOTED, user.getName()));
            return true;
        }

        //check permissions 
        if (!checker.hasOrConsole(sender, "bungeeperms.promote." + nextgroup.getName(), true))
        {
            return true;
        }

        //permission checks if sender is a player
        if (sender.isPlayer())
        {
            User issuer = pm().getUser(sender.getName());
            if (issuer == null)
            {
                sender.sendMessage(Lang.translate(MessageType.ERR_USER_YOU_NOT_EXISTING));
                return true;
            }
            Group issuergroup = pm().getMainGroup(issuer);
            if (issuergroup == null)
            {
                sender.sendMessage(Lang.translate(MessageType.ERR_USER_YOU_NO_GROUPS));
                return true;
            }
            if (!(issuergroup.getRank() < nextgroup.getRank()))
            {
                sender.sendMessage(Lang.translate(MessageType.ERR_USER_YOU_CANNOT_PROMOTE, user.getName()));
                return true;
            }
        }

        //promote player
        //remove old group if neccessary
        if (playergroup != null)
        {
            pm().removeUserGroup(user, playergroup);
        }
        pm().addUserGroup(user, nextgroup);
        sender.sendMessage(Lang.translate(MessageType.PROMOTE_MESSAGE, user.getName(), nextgroup.getName()));

        //promote msg to user
        if (config.isNotifyPromote())
        {
            Sender s = plugin.getPlayer(user.getName());
            if (s != null)
            {
                s.sendMessage(Lang.translate(MessageType.PROMOTE_MESSAGE_TO_USER, nextgroup.getName()));
            }
        }

        return true;
    }

    private boolean handleDemote(Sender sender, String[] args)
    {
        if (!checker.hasOrConsole(sender, "bungeeperms.demote", true))
        {
            return true;
        }

        //2 or 3 args expected
        if (!Statics.matchArgs(sender, args, 2, 3))
        {
            return true;
        }

        //get user
        String player = Statics.getFullPlayerName(args[1]);
        User user = pm().getUser(player);
        if (user == null)
        {
            sender.sendMessage(Lang.translate(MessageType.ERR_USER_NOT_EXISTING, player));
            return true;
        }

        //get previous by ladder
        Group playergroup = null;
        Group previousgroup = null;

        //ladder specified by sender
        if (args.length == 3)
        {
            String ladder = args[2];
            playergroup = user.getGroupByLadder(ladder);
            if (playergroup != null)
            {
                previousgroup = pm().getPreviousGroup(playergroup);
            }
        }

        //no ladder specified ... assume main ladder
        else
        {
            playergroup = pm().getMainGroup(user);
            if (playergroup == null)
            {
                sender.sendMessage(Lang.translate(MessageType.ERR_USER_NO_GROUPS, user.getName()));
                return true;
            }
            previousgroup = pm().getPreviousGroup(playergroup);
        }

        if (previousgroup == null)
        {
            sender.sendMessage(Lang.translate(MessageType.ERR_USER_CANNOT_BE_DEMOTED, user.getName()));
            return true;
        }

        //check permissions
        if (!checker.hasOrConsole(sender, "bungeeperms.demote." + previousgroup.getName(), true))
        {
            return true;
        }

        //permision checks if sender is a player
        if (sender.isPlayer())
        {
            User issuer = pm().getUser(sender.getName());
            if (issuer == null)
            {
                sender.sendMessage(Lang.translate(MessageType.ERR_USER_YOU_NOT_EXISTING));
                return true;
            }
            Group issuergroup = pm().getMainGroup(issuer);
            if (issuergroup == null)
            {
                sender.sendMessage(Lang.translate(MessageType.ERR_USER_YOU_NO_GROUPS));
                return true;
            }
            if (!(issuergroup.getRank() < playergroup.getRank()))
            {
                sender.sendMessage(Lang.translate(MessageType.ERR_USER_YOU_CANNOT_DEMOTE, user.getName()));
                return true;
            }
        }

        //demote
        //remove old group if neccessary
        if (playergroup != null)
        {
            pm().removeUserGroup(user, playergroup);
        }
        pm().addUserGroup(user, previousgroup);
        sender.sendMessage(Lang.translate(MessageType.DEMOTE_MESSAGE, user.getName(), previousgroup.getName()));

        //demote msg to user
        if (config.isNotifyDemote())
        {
            Sender s = plugin.getPlayer(user.getName());
            if (s != null)
            {
                s.sendMessage(Lang.translate(MessageType.DEMOTE_MESSAGE_TO_USER, previousgroup.getName()));
            }
        }
        return true;
    }

    private boolean handleFormat(Sender sender, String[] args)
    {
        if (!checker.hasOrConsole(sender, "bungeeperms.format", true))
        {
            return true;
        }

        sender.sendMessage(Lang.translate(MessageType.FORMATTING));
        pm().format();
        sender.sendMessage(Lang.translate(MessageType.FORMATTING_DONE));
        return true;
    }

    private boolean handleCleanup(Sender sender, String[] args)
    {
        if (!checker.hasOrConsole(sender, "bungeeperms.cleanup", true))
        {
            return true;
        }

        sender.sendMessage(Lang.translate(MessageType.CLEANING));
        sender.sendMessage(Color.Text + "Cleaning up permissions file/table ...");
        int deleted = pm().cleanup();
        sender.sendMessage(Lang.translate(MessageType.CLEANING_DONE, deleted));
        sender.sendMessage(Color.Message + "Finished cleaning. Deleted " + Color.Value + deleted + " users" + Color.Message + ".");
        return true;
    }

    private boolean handleMigrate(Sender sender, String[] args)
    {
        if (!checker.hasOrConsole(sender, "bungeeperms.migrate", true))
        {
            return true;
        }

        //minimal 2 args
        if (args.length < 2)
        {
            sender.sendMessage(Lang.translate(MessageType.COMMAND_TOO_FEW_ARGUMENTS));
            return true;
        }

        String migratetype = args[1];
        if (migratetype.equalsIgnoreCase("backend"))
        {
            return handleMigrateBackend(sender, args);
        }
        else if (migratetype.equalsIgnoreCase("useuuid"))
        {
            return handleMigrateUseUUID(sender, args);
        }
        else
        {
            return false;
        }
    }

    private boolean handleMigrateBackend(Sender sender, String[] args)
    {
        if (args.length == 2)
        {
            sender.sendMessage(Color.Text + "Currently using " + Color.Value + pm().getBackEnd().getType().name() + Color.Text + " as backend");
        }
        else if (args.length == 3)
        {
            String stype = args[2];
            BackEndType type = BackEndType.getByName(stype);
            if (type == null)
            {
                sender.sendMessage(Color.Error + "Invalid backend type! "
                                   + Color.Value + BackEndType.YAML.name() + Color.Error + " or "
                                   + Color.Value + BackEndType.MySQL.name() + Color.Error + " is required!");
                return true;
            }

            if (type == pm().getBackEnd().getType())
            {
                sender.sendMessage(Color.Error + "Invalid backend type! You can't migrate to same type!");
                return true;
            }

            sender.sendMessage(Color.Text + "Migrating permissions to " + Color.Value + type.name() + Color.Text + " ...");
            pm().migrateBackEnd(type);
            sender.sendMessage(Color.Message + "Finished migration.");
        }
        else
        {
            sender.sendMessage(Lang.translate(MessageType.COMMAND_TOO_MANY_ARGUMENTS));
        }
        return true;
    }

    private boolean handleMigrateUseUUID(Sender sender, String[] args)
    {
        if (args.length == 2)
        {
            sender.sendMessage(Color.Text + "Currently using " + Color.Value + (config.isUseUUIDs() ? "UUIDs" : "player names") + Color.Text + " for player identification");
        }
        else if (args.length == 3)
        {
            String stype = args[2];
            Boolean type = null;
            try
            {
                type = parseTrueFalse(stype);
            }
            catch (Exception e)
            {
            }
            if (type == null)
            {
                sender.sendMessage(Color.Error + "Invalid use-uuid type! "
                                   + Color.Value + "true" + Color.Error + " or "
                                   + Color.Value + "false" + Color.Error + " is required!");
                return true;
            }

            if (type == config.isUseUUIDs())
            {
                sender.sendMessage(Color.Error + "Invalid use-uuid type! You can't migrate to same type!");
                return true;
            }

            if (type)
            {
                sender.sendMessage(Color.Text + "Migrating permissions using UUIDs for player identification ...");

                //fetch uuids from mojang
                sender.sendMessage(Color.Text + "Fetching UUIDs ...");
                UUIDFetcher fetcher = new UUIDFetcher(pm().getBackEnd().getRegisteredUsers(), config.getFetcherCooldown());
                fetcher.fetchUUIDs();
                Map<String, UUID> uuids = fetcher.getUUIDs();
                sender.sendMessage(Color.Message + "Finished fetching.");

                //migrate permissions backend
                sender.sendMessage(Color.Text + "Migrating player identification ...");
                pm().migrateUseUUID(uuids);
                sender.sendMessage(Color.Message + "Finished player identification migration.");

                //add fetched uuids to uuidplayerdb
                sender.sendMessage(Color.Text + "Applying fetched data to player-uuid-database ...");
                UUIDPlayerDB updb = config.getBackendType() == BackEndType.YAML ? new YAMLUUIDPlayerDB() : new MySQLUUIDPlayerDB();
                updb.clear();
                for (Map.Entry<String, UUID> e : uuids.entrySet())
                {
                    updb.update(e.getValue(), e.getKey());
                }
                pm().setUUIDPlayerDB(updb);
                sender.sendMessage(Color.Message + "Finished applying of fetched data to player-uuid-database.");
            }
            else
            {
                sender.sendMessage(Color.Text + "Migrating permissions using player names for player identification ...");

                //fetch playernames from mojang
                sender.sendMessage(Color.Text + "Fetching " + (type ? "UUIDs" : "player names") + " ...");
                UUIDFetcher fetcher = new UUIDFetcher(pm().getBackEnd().getRegisteredUsers(), config.getFetcherCooldown());
                fetcher.fetchPlayerNames();
                Map<UUID, String> playernames = fetcher.getPlayerNames();
                sender.sendMessage(Color.Message + "Finished fetching.");

                //migrate permissions backend
                sender.sendMessage(Color.Text + "Migrating player identification ...");
                pm().migrateUsePlayerNames(playernames);
                sender.sendMessage(Color.Message + "Finished player identification migration.");

                //add fetched playername to uuidplayerdb
                sender.sendMessage(Color.Text + "Applying fetched data to player-uuid-database ...");
                if (pm().getUUIDPlayerDB() != null)
                {
                    pm().getUUIDPlayerDB().clear();
                    pm().setUUIDPlayerDB(null);
                }
                sender.sendMessage(Color.Message + "Finished applying of fetched data to player-uuid-database.");
            }
            BungeePerms.getInstance().getPermissionsManager().reload();

            sender.sendMessage(Color.Message + "Finished migration.");
        }
        else
        {
            sender.sendMessage(Lang.translate(MessageType.COMMAND_TOO_MANY_ARGUMENTS));
        }
        return true;
    }

    private boolean handleUUID(Sender sender, String[] args)
    {
        if (!checker.hasOrConsole(sender, "bungeeperms.uuid", true))
        {
            return true;
        }

        if (!Statics.matchArgs(sender, args, 2, 3))
        {
            return true;
        }

        boolean reverse = false;
        boolean mojang = false;
        String what = args[1];
        UUID uuidwhat = Statics.parseUUID(what);
        if (args.length > 2)
        {
            String params = args[2];
            if (params.startsWith("-"))
            {
                reverse = params.contains("r");
                mojang = params.contains("m");
            }
        }

        if (reverse)
        {
            if (uuidwhat == null)
            {
                sender.sendMessage(Color.Error + "UUID invalid!");
                return true;
            }
        }

        if (mojang && reverse)
        {
            String name = UUIDFetcher.getPlayerNameFromMojang(uuidwhat);
            if (name == null)
            {
                sender.sendMessage(Color.Text + "Mojang does not know this player.");
            }
            else
            {
                sender.sendMessage(Color.Text + "Mojang says: Player name of " + Color.Value + uuidwhat + Color.Text + " is " + Color.User + name + Color.Text + ".");
            }
        }
        else if (mojang && !reverse)
        {
            UUID uuid = UUIDFetcher.getUUIDFromMojang(what, null);
            if (uuid == null)
            {
                sender.sendMessage(Color.Text + "Mojang does not know this player.");
            }
            else
            {
                sender.sendMessage(Color.Text + "Mojang says: UUID of " + Color.User + what + Color.Text + " is " + Color.Value + uuid + Color.Text + ".");
            }
        }
        else if (!mojang && reverse)
        {
            String name = pm().getUUIDPlayerDB().getPlayerName(uuidwhat);
            if (name == null)
            {
                sender.sendMessage(Color.Text + "The UUID-player database does not know this player.");
            }
            else
            {
                sender.sendMessage(Color.Text + "The UUID-player database says: Player name of " + Color.Value + uuidwhat + Color.Text + " is " + Color.User + name + Color.Text + ".");
            }
        }
        else if (!mojang && !reverse)
        {
            UUID uuid = pm().getUUIDPlayerDB().getUUID(what);
            if (uuid == null)
            {
                sender.sendMessage(Color.Text + "The UUID-player database does not know this player.");
            }
            else
            {
                sender.sendMessage(Color.Text + "The UUID-player database says: UUID of " + Color.User + what + Color.Text + " is " + Color.Value + uuid + Color.Text + ".");
            }
        }
        return true;
    }

    private boolean handleSearch(Sender sender, String[] args) {
        if (!checker.hasOrConsole(sender, "bungeeperms.search", true)) {
            return true;
        }

        if (!Statics.matchArgs(sender, args, 2, 3)) {
            return true;
        }

        int page = 1;
        if (args.length == 3 && Statics.isInt(args[2])) {
            page = Integer.parseInt(args[2]);
        }

        String perm = args[1].toLowerCase();
        List<BPPermission> userperm = pm().getBackEnd().getUsersWithPerm(perm);
        List<BPPermission> groupperm = pm().getBackEnd().getGroupsWithPerm(perm);

        if (config.isUseUUIDs()) {
            Map<UUID, String> map = new HashMap<>();
            map = pm().getUUIDPlayerDB().getAll();
            for (BPPermission bpp : userperm) {
                UUID uuid = UUID.fromString(bpp.getOrigin());
                if (map.containsKey(uuid))
                    bpp.setOrigin(map.get(uuid));
            }
        }

        if (!userperm.isEmpty()) {
            userperm.sort(Comparator.comparing(BPPermission::getOrigin));
            sender.sendMessage(Lang.translate(MessageType.SEARCH_USER_HEADER, perm));
            list(sender, userperm, null, page, false, null, null);
        } else {
            sender.sendMessage(Lang.translate(MessageType.SEARCH_NO_USER_FOUND, perm));
        }

        if (!groupperm.isEmpty()) {
            groupperm.sort(Comparator.comparing(BPPermission::getOrigin));
            sender.sendMessage(Lang.translate(MessageType.SEARCH_GROUP_HEADER, perm));
            list(sender, groupperm, null, page, false, null, null);
        } else {
            sender.sendMessage(Lang.translate(MessageType.SEARCH_NO_GROUP_FOUND, perm));
        }
        return true;
    }

    //command util
    private void list(Sender sender, List<BPPermission> perms, String entity, int page, boolean only, String server, String world)
    {
        if (only)
        {
            perms = new ArrayList(perms);
            for (int i = 0; i < perms.size(); i++)
            {
                BPPermission perm = perms.get(i);
                if ((server != null && !server.equalsIgnoreCase(perm.getServer()))
                    || (server != null && world != null && !world.equalsIgnoreCase(perm.getWorld())))
                    perms.remove(i--);
            }
        }
        if (config.getResolvingMode() == PermissionsResolver.ResolvingMode.BESTMATCH)
        {
            perms = new ArrayList(perms);
            Collections.sort(perms);
        }

        sender.sendMessage(Lang.translate(MessageType.PERMISSIONS_LIST_HEADER_PAGE, page, perms.size() / 20 + (perms.size() % 20 > 0 ? 1 : 0)));
        for (int i = (page - 1) * 20; i < page * 20 && i < perms.size(); i++)
        {
            BPPermission perm = perms.get(i);
            String dur = formatDuration(perm);
            sender.sendMessage(Lang.translate(MessageType.PERMISSIONS_LIST_ITEM,
                                              perm.getPermission(),
                                              (!perm.isGroup() && perm.getOrigin().equalsIgnoreCase(entity) ? Lang.translate(MessageType.OWN) : perm.getOrigin()),
                                              (perm.getServer() != null ? " | " + Color.Value + perm.getServer() + Color.Text : ""),
                                              (perm.getWorld() != null ? " | " + Color.Value + perm.getWorld() + Color.Text : ""),
                                              dur == null ? "" : ", " + Color.Value + dur + Color.Text));
        }
    }

    private boolean addToTree(Map<Group, Object> tree, Group parent, Group child)
    {
        boolean didsomething = false;
        for (Map.Entry<Group, Object> e : tree.entrySet())
        {
            if (e.getKey() == parent)
            {
                if (!((Map<Group, Object>) e.getValue()).containsKey(child))
                {
                    ((Map<Group, Object>) e.getValue()).put(child, new HashMap());
                    didsomething = true;
                }
            }
            else
            {
                didsomething |= addToTree((Map<Group, Object>) e.getValue(), parent, child);
            }
        }
        return didsomething;
    }

    private List<String> treeToMsgs(Map<Group, Object> tree, int indent)
    {
        List<List<String>> msgs = treeToMsgs0(tree, indent);
        List<String> ret = new ArrayList();

        List<Integer> widths = new ArrayList();
        if (!msgs.isEmpty())
            for (String i : msgs.get(0))
                widths.add(0);
        for (int i = 0; i < widths.size(); i++)
        {
            int len = 0;
            for (List<String> l : msgs)
                len = Math.max(len, l.get(i).length());
            widths.set(i, len);
        }
        for (List<String> l : msgs)
        {
            for (int i = 0; i < l.size(); i++)
                l.set(i, String.format("%" + (i == 0 ? "-" : "") + widths.get(i) + "s", l.get(i)));
            ret.add(Lang.translate(MessageType.OVERVIEW_ITEM, l.toArray()));
        }

        return ret;
    }

    private List<List<String>> treeToMsgs0(Map<Group, Object> tree, int indent)
    {
        List<List<String>> msgs = new ArrayList();
        List<Map.Entry<Group, Object>> entries = new ArrayList(tree.entrySet());
        entries.sort(new Comparator<Map.Entry<Group, Object>>()
        {
            @Override
            public int compare(Map.Entry<Group, Object> o1, Map.Entry<Group, Object> o2)
            {
                return String.CASE_INSENSITIVE_ORDER.compare(o1.getKey().getName(), o2.getKey().getName());
            }
        });
        for (Map.Entry<Group, Object> e : entries)
        {
            Group g = e.getKey();
            String space = new String(new char[indent]).replace("\0", " ");
            msgs.add(Arrays.asList(space + "- " + Color.Value + g.getName() + Color.Text,
                                   g.getLadder(),
                                   "" + g.getPermissionsCount(null, null),
                                   "" + g.getOwnPermissionsCount(null, null),
                                   "" + BungeePerms.getInstance().getPermissionsManager().getGroupUsers(g).size(),
                                   "" + g.getRank(),
                                   "" + g.getWeight()));
            msgs.addAll(treeToMsgs0((Map<Group, Object>) e.getValue(), indent + 1));
        }
        return msgs;
    }

    //other util
    private PermissionsManager pm()
    {
        return BungeePerms.getInstance().getPermissionsManager();
    }

    private boolean parseTrueFalse(String truefalse)
    {
        if (Statics.argAlias(truefalse, "true", "yes", "t", "y", "+"))
        {
            return true;
        }
        else if (Statics.argAlias(truefalse, "false", "no", "f", "n", "-"))
        {
            return false;
        }
        throw new IllegalArgumentException("truefalse does not represent a boolean value");
    }

    private Integer parseDuration(String str)
    {
        if (Statics.isEmpty(str) || !str.matches("([0-9]+d)?([0-9]+h)?([0-9]+m)?([0-9]+s)?"))
            return null;

        int dur = 0;
        int index = str.indexOf("d");
        if (index >= 0)
        {
            dur += Integer.parseInt(str.substring(0, index)) * 24 * 60 * 60;
            str = str.substring(index + 1);
        }
        index = str.indexOf("h");
        if (index >= 0)
        {
            dur += Integer.parseInt(str.substring(0, index)) * 60 * 60;
            str = str.substring(index + 1);
        }
        index = str.indexOf("m");
        if (index >= 0)
        {
            dur += Integer.parseInt(str.substring(0, index)) * 60;
            str = str.substring(index + 1);
        }
        index = str.indexOf("s");
        if (index >= 0)
        {
            dur += Integer.parseInt(str.substring(0, index));
            str = str.substring(index + 1);
        }

        return dur;
    }

    private String formatBool(boolean b)
    {
        return (b ? ChatColor.GREEN : ChatColor.RED) + String.valueOf(b).toUpperCase();
    }

    private String formatDuration(BPPermission perm)
    {
        return perm.getTimedStart() == null ? null : formatDuration(perm.getTimedStart(), perm.getTimedDuration());
    }

    private String formatDuration(TimedValue val)
    {
        return val.getStart() == null ? null : formatDuration(val.getStart(), val.getDuration());
    }

    private String formatDuration(Date start, int duration)
    {
        String dur = "";
        long end = start.getTime() + (long)duration * 1000;
        long d = end - System.currentTimeMillis();
        d /= 1000;
        if (d > 24 * 60 * 60)
        {
            dur += (d / (24 * 60 * 60)) + "d";
            d %= 24 * 60 * 60;
        }
        if (d > 60 * 60)
        {
            dur += (d / (60 * 60)) + "h";
            d %= 60 * 60;
        }
        if (d > 60)
        {
            dur += (d / (60)) + "m";
            d %= 60;
        }
        if (d > 0)
            dur += d + "s";
        else if (dur.length() == 0)
            dur += "0s";
        return dur;
    }
}
