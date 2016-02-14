package net.alpenblock.bungeeperms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import net.alpenblock.bungeeperms.Lang.MessageType;
import net.alpenblock.bungeeperms.io.BackEndType;
import net.alpenblock.bungeeperms.io.UUIDPlayerDBType;
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
            BungeePerms.getInstance().getPlugin().getLogger().info(sender.getName() + " issued bungeeperms command /" + cmd + " " + Statics.arrayToString(args, 0, args.length, " "));
        }

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

        List<String> users = pm().getRegisteredUsers();
        if (users.isEmpty())
        {
            sender.sendMessage(Lang.translate(MessageType.NO_USERS_FOUND));
            return true;
        }

        if (args.length == 1)
        {
            String out = Lang.translate(MessageType.REGISTERED_USERS);
            for (int i = 0; i < users.size(); i++) //todo: translate uuids and output 1 entity per line
            {
                out += Color.User + users.get(i) + Color.Text + (i + 1 < users.size() ? ", " : "");
            }
            sender.sendMessage(out);
            return true;
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
            Messages.sendTooLessArgsMessage(sender);
            return true;
        }

        if (args[2].equalsIgnoreCase("list"))
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
        else if (Statics.argAlias(args[2], "group", "perm", "permission"))
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
        sender.sendMessage(Lang.translate(MessageType.PERMISSIONS_LIST_HEADER_PAGE, page, perms.size() / 20 + (perms.size() % 20 > 0 ? 1 : 0)));
        for (int i = (page - 1) * 20; i < page * 20 && i < perms.size(); i++)
        {
            BPPermission perm = perms.get(i);
            sender.sendMessage(Lang.translate(MessageType.PERMISSIONS_LIST_ITEM,
                                              perm.getPermission(),
                                              (!perm.isGroup() && perm.getOrigin().equalsIgnoreCase(player) ? Lang.translate(MessageType.OWN) : perm.getOrigin()),
                                              (perm.getServer() != null ? " | " + Color.Value + perm.getServer() + Color.Text : ""),
                                              (perm.getWorld() != null ? " | " + Color.Value + perm.getWorld() + Color.Text : "")));
        }
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
        for (Group g : user.getGroups())
        {
            sender.sendMessage(Color.Text + "- " + Color.Value + g.getName());
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

        String groups = "";
        for (int i = 0; i < user.getGroups().size(); i++)
        {
            groups += Color.Value + user.getGroups().get(i).getName() + Color.Text + " (" + Color.Value + user.getGroups().get(i).getPerms().size() + Color.Text + ")" + (i + 1 < user.getGroups().size() ? ", " : "");
        }
        sender.sendMessage(Lang.translate(MessageType.USER_GROUPS, groups));

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
            {
                perm = ((Server) perm).getWorld(world);
            }
        }

        //display
        sender.sendMessage(Lang.translate(MessageType.DISPLAY, (!Statics.isEmpty(perm.getDisplay()) ? perm.getDisplay() : Color.Text + "(" + Lang.translate(MessageType.NONE) + ")")));

        //prefix
        sender.sendMessage(Lang.translate(MessageType.PREFIX, (!Statics.isEmpty(perm.getPrefix()) ? perm.getPrefix() : Color.Text + "(" + Lang.translate(MessageType.NONE) + ")")));

        //suffix
        sender.sendMessage(Lang.translate(MessageType.SUFFIX, (!Statics.isEmpty(perm.getSuffix()) ? perm.getSuffix() : Color.Text + "(" + Lang.translate(MessageType.NONE) + ")")));

        //full prefix
        String buildPrefix = user.buildPrefix(server, world);
        sender.sendMessage(Lang.translate(MessageType.PREFIX_FULL, (!Statics.isEmpty(buildPrefix) ? buildPrefix : Color.Text + "(" + Lang.translate(MessageType.NONE) + ")")));

        //full suffix
        String buildSuffix = user.buildSuffix(server, world);
        sender.sendMessage(Lang.translate(MessageType.SUFFIX_FULL, (!Statics.isEmpty(buildSuffix) ? buildSuffix : Color.Text + "(" + Lang.translate(MessageType.NONE) + ")")));
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
            if (user.getExtraPerms().contains("-" + perm))
            {
                pm().removeUserPerm(user, "-" + perm);
                sender.sendMessage(Lang.translate(MessageType.USER_ADDED_PERM, perm, user.getName()));
            }
            else if (!user.getExtraPerms().contains(perm))
            {
                pm().addUserPerm(user, perm);
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
                if (srv.getPerms().contains("-" + perm))
                {
                    pm().removeUserPerServerPerm(user, server, "-" + perm);
                    sender.sendMessage(Lang.translate(MessageType.USER_ADDED_PERM_SERVER, perm, user.getName(), server));
                }
                else if (!srv.getPerms().contains(perm))
                {
                    pm().addUserPerServerPerm(user, server, perm);
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

                if (w.getPerms().contains("-" + perm))
                {
                    pm().removeUserPerServerWorldPerm(user, server, world, "-" + perm);
                    sender.sendMessage(Lang.translate(MessageType.USER_ADDED_PERM_SERVER_WORLD, perm, user.getName(), server, world));
                }
                else if (!w.getPerms().contains(perm))
                {
                    pm().addUserPerServerWorldPerm(user, server, world, perm);
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
            if (user.getExtraPerms().contains(perm))
            {
                pm().removeUserPerm(user, perm);
                sender.sendMessage(Lang.translate(MessageType.USER_REMOVED_PERM, perm, user.getName()));
            }
            else if (!user.getExtraPerms().contains("-" + perm))
            {
                pm().addUserPerm(user, "-" + perm);
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
                    pm().removeUserPerServerPerm(user, server, perm);
                    sender.sendMessage(Lang.translate(MessageType.USER_REMOVED_PERM_SERVER, perm, user.getName(), server));
                }
                else if (!srv.getPerms().contains("-" + perm))
                {
                    pm().addUserPerServerPerm(user, server, "-" + perm);
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
                    pm().removeUserPerServerWorldPerm(user, server, world, perm);
                    sender.sendMessage(Lang.translate(MessageType.USER_REMOVED_PERM_SERVER_WORLD, perm, user.getName(), server, world));
                }
                else if (!w.getPerms().contains("-" + perm))
                {
                    pm().addUserPerServerWorldPerm(user, server, world, "-" + perm);
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

        List<Group> groups = u.getGroups();
        for (Group g : groups)
        {
            if (g.getName().equalsIgnoreCase(group.getName()))
            {
                sender.sendMessage(Lang.translate(MessageType.ERR_USER_ALREADY_IN_GROUP, groupname));
                return true;
            }
        }

        pm().addUserGroup(u, group);
        sender.sendMessage(Lang.translate(MessageType.USER_ADDED_GROUP, groupname, u.getName()));
        return true;
    }

    private boolean handleUserCommandsGroupRemove(Sender sender, String[] args)
    {
        if (!checker.hasOrConsole(sender, "bungeeperms.user.group.remove", true))
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

        List<Group> groups = u.getGroups();
        for (Group g : groups)
        {
            if (g.getName().equalsIgnoreCase(group.getName()))
            {
                pm().removeUserGroup(u, group);
                sender.sendMessage(Lang.translate(MessageType.USER_REMOVED_GROUP, groupname, u.getName()));
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
                for (Group g : pm().getLadderGroups(l))
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
            Messages.sendTooLessArgsMessage(sender);
            return true;
        }

        if (args[2].equalsIgnoreCase("list"))
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
        else if (Statics.argAlias(args[2], "perm", "permission"))
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
        sender.sendMessage(Lang.translate(MessageType.PERMISSIONS_LIST_HEADER_PAGE, page, perms.size() / 20 + (perms.size() % 20 > 0 ? 1 : 0)));
        for (int i = (page - 1) * 20; i < page * 20 && i < perms.size(); i++)
        {
            BPPermission perm = perms.get(i);
            sender.sendMessage(Lang.translate(MessageType.PERMISSIONS_LIST_ITEM,
                                              perm.getPermission(),
                                              (!perm.getOrigin().equalsIgnoreCase(groupname) ? Lang.translate(MessageType.OWN) : perm.getOrigin()),
                                              (perm.getServer() != null ? " | " + Color.Value + perm.getServer() + Color.Text : ""),
                                              (perm.getWorld() != null ? " | " + Color.Value + perm.getWorld() + Color.Text : "")));
        }
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
        String inheritances = "";
        for (int i = 0; i < group.getInheritances().size(); i++)
        {
            inheritances += Color.Value + group.getInheritances().get(i) + Color.Text + " (" + Color.Value + pm().getGroup(group.getInheritances().get(i)).getPerms().size() + Color.Text + ")" + (i + 1 < group.getInheritances().size() ? ", " : "");
        }
        if (inheritances.length() == 0)
        {
            inheritances = Color.Text + "(" + Lang.translate(MessageType.NONE) + ")";
        }
        sender.sendMessage(Lang.translate(MessageType.GROUP_INHERITANCES, inheritances));

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
            {
                perm = ((Server) perm).getWorld(world);
            }
        }

        //display
        sender.sendMessage(Lang.translate(MessageType.DISPLAY, (!Statics.isEmpty(perm.getDisplay()) ? perm.getDisplay() : Color.Text + "(" + Lang.translate(MessageType.NONE) + ")")));

        //prefix
        sender.sendMessage(Lang.translate(MessageType.PREFIX, (!Statics.isEmpty(perm.getPrefix()) ? perm.getPrefix() : Color.Text + "(" + Lang.translate(MessageType.NONE) + ")")));

        //suffix
        sender.sendMessage(Lang.translate(MessageType.SUFFIX, (!Statics.isEmpty(perm.getSuffix()) ? perm.getSuffix() : Color.Text + "(" + Lang.translate(MessageType.NONE) + ")")));

        //full prefix
        String buildPrefix = group.buildPrefix(server, world);
        sender.sendMessage(Lang.translate(MessageType.PREFIX_FULL, (!Statics.isEmpty(buildPrefix) ? buildPrefix : Color.Text + "(" + Lang.translate(MessageType.NONE) + ")")));

        //full suffix
        String buildSuffix = group.buildSuffix(server, world);
        sender.sendMessage(Lang.translate(MessageType.SUFFIX_FULL, (!Statics.isEmpty(buildSuffix) ? buildSuffix : Color.Text + "(" + Lang.translate(MessageType.NONE) + ")")));
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
            Messages.sendTooManyArgsMessage(sender);
            return true;
        }

        String groupname = args[1];
        Group group = pm().getGroup(groupname);
        if (group == null)
        {
            sender.sendMessage(Lang.translate(MessageType.ERR_GROUP_NOT_EXISTING, groupname));
            return true;
        }
        List<String> users = pm().getGroupUsers(group);
        if (users.isEmpty())
        {
            sender.sendMessage(Lang.translate(MessageType.NO_USERS_FOUND));
        }

        if (args.length == 3)
        {
            String out = Lang.translate(MessageType.GROUP_USERS_HEADER, group.getName());
            for (int i = 0; i < users.size(); i++)
            {
                out += Color.User + users.get(i) + Color.Text + (i + 1 < users.size() ? ", " : ""); //todo: uuid
            }
            sender.sendMessage(out);
            return true;
        }
        else if (args.length == 4)
        {
            if (!args[3].equalsIgnoreCase("-c"))
            {
                return false;
            }
            sender.sendMessage(Lang.translate(MessageType.GROUP_USERS_HEADER, users.size(), group.getName()));
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
            sender.sendMessage(Lang.translate(MessageType.ERR_GROUP_NOT_EXISTING, groupname));
            return true;
        }
        Group group = new Group(groupname, new ArrayList<String>(), new ArrayList<String>(), new HashMap<String, Server>(), 1000, 1000, "default", false, null, null, null);
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
            if (group.getPerms().contains("-" + perm))
            {
                pm().removeGroupPerm(group, "-" + perm);
                sender.sendMessage(Lang.translate(MessageType.GROUP_ADDED_PERM, perm, group.getName()));
            }
            else if (!group.getPerms().contains(perm))
            {
                pm().addGroupPerm(group, perm);
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
                if (perserverperms.contains("-" + perm))
                {
                    pm().removeGroupPerServerPerm(group, server, "-" + perm);
                    sender.sendMessage(Lang.translate(MessageType.GROUP_ADDED_PERM_SERVER, perm, group.getName(), server));
                }
                else if (!perserverperms.contains(perm))
                {
                    pm().addGroupPerServerPerm(group, server, perm);
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
                if (perserverworldperms.contains("-" + perm))
                {
                    pm().removeGroupPerServerWorldPerm(group, server, world, "-" + perm);
                    sender.sendMessage(Lang.translate(MessageType.GROUP_ADDED_PERM_SERVER_WORLD, perm, group.getName(), server, world));
                }
                else if (!perserverworldperms.contains(perm))
                {
                    pm().addGroupPerServerWorldPerm(group, server, world, perm);
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
                pm().removeGroupPerm(group, perm);
                sender.sendMessage(Lang.translate(MessageType.GROUP_REMOVED_PERM, perm, group.getName()));
            }
            else if (!group.getPerms().contains("-" + perm))
            {
                pm().addGroupPerm(group, "-" + perm);
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
                List<String> perserverperms = srv.getPerms();
                if (perserverperms.contains(perm))
                {
                    pm().removeGroupPerServerPerm(group, server, perm);
                    sender.sendMessage(Lang.translate(MessageType.GROUP_REMOVED_PERM_SERVER, perm, group.getName(), server));
                }
                else if (!perserverperms.contains("-" + perm))
                {
                    pm().addGroupPerServerPerm(group, server, "-" + perm);
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

                List<String> perserverworldperms = w.getPerms();
                if (perserverworldperms.contains(perm))
                {
                    pm().removeGroupPerServerWorldPerm(group, server, world, perm);
                    sender.sendMessage(Lang.translate(MessageType.GROUP_REMOVED_PERM_SERVER_WORLD, perm, group.getName(), server, world));
                }
                else if (!perserverworldperms.contains("-" + perm))
                {
                    pm().addGroupPerServerWorldPerm(group, server, world, "-" + perm);
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
            boolean has = group.has(perm.toLowerCase());
            sender.sendMessage(Lang.translate(MessageType.GROUP_HAS_PERM, group.getName(), perm, formatBool(has)));
        }
        
        //per server perm
        else if (world == null)
        {
            boolean has = group.hasOnServer(perm.toLowerCase(), server);
            sender.sendMessage(Lang.translate(MessageType.GROUP_HAS_PERM_SERVER, group.getName(), perm, server, formatBool(has)));
        }

        //per server world perm
        else
        {
            boolean has = group.hasOnServerInWorld(perm.toLowerCase(), server, world);
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

        if (!Statics.matchArgs(sender, args, 4))
        {
            return true;
        }

        String groupname = args[1];
        String addgroup = args[3];

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

        List<String> inheritances = group.getInheritances();

        //check for already existing inheritance
        for (String s : inheritances)
        {
            if (s.equalsIgnoreCase(toadd.getName()))
            {
                sender.sendMessage(Lang.translate(MessageType.ERR_GROUP_ALREADY_INHERITS, groupname, addgroup));
                return true;
            }
        }

        pm().addGroupInheritance(group, toadd);

        sender.sendMessage(Lang.translate(MessageType.GROUP_ADDED_INHERITANCE, addgroup, groupname));
        return true;
    }

    private boolean handleGroupCommandsInheritRemove(Sender sender, String[] args)
    {
        if (!checker.hasOrConsole(sender, "bungeeperms.group.inheritances.remove", true))
        {
            return true;
        }

        if (!Statics.matchArgs(sender, args, 4))
        {
            return true;
        }

        String groupname = args[1];
        String removegroup = args[3];

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

        List<String> inheritances = group.getInheritances();
        for (String s : inheritances)
        {
            if (s.equalsIgnoreCase(toremove.getName()))
            {
                pm().removeGroupInheritance(group, toremove);

                sender.sendMessage(Lang.translate(MessageType.GROUP_REMOVED_INHERITANCE, removegroup, groupname));
                return true;
            }
        }
        sender.sendMessage(Lang.translate(MessageType.ERR_GROUP_ALREADY_INHERITS, groupname, removegroup));
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
            Messages.sendTooLessArgsMessage(sender);
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
        else if (migratetype.equalsIgnoreCase("uuidplayerdb"))
        {
            return handleMigrateUUIDPlayerDB(sender, args);
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
            if (pm().getBackEnd().getType() == BackEndType.MySQL)
            {
                sender.sendMessage(ChatColor.RED + "The MySQL backend is deprecated! Please consider to use MySQL2.");
            }
        }
        else if (args.length == 3)
        {
            String stype = args[2];
            BackEndType type = BackEndType.getByName(stype);
            if (type == null)
            {
                sender.sendMessage(Color.Error + "Invalid backend type! "
                        + Color.Value + BackEndType.YAML.name() + Color.Error + " or "//, "
                        //                        + Color.Value + BackEndType.MySQL.name() + Color.Error + " or "
                        + Color.Value + BackEndType.MySQL2.name() + Color.Error + " is required!");
                return true;
            }

            //disallow mysql backend
            if (type == BackEndType.MySQL)
            {
                sender.sendMessage(ChatColor.RED + "The MySQL backend is deprecated! Please use MySQL2.");
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
            Messages.sendTooManyArgsMessage(sender);
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
                for (Map.Entry<String, UUID> e : uuids.entrySet())
                {
                    pm().getUUIDPlayerDB().update(e.getValue(), e.getKey());
                }
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
                for (Map.Entry<UUID, String> e : playernames.entrySet())
                {
                    pm().getUUIDPlayerDB().update(e.getKey(), e.getValue());
                }
                sender.sendMessage(Color.Message + "Finished applying of fetched data to player-uuid-database.");
            }

            sender.sendMessage(Color.Message + "Finished migration.");
        }
        else
        {
            Messages.sendTooManyArgsMessage(sender);
        }
        return true;
    }

    private boolean handleMigrateUUIDPlayerDB(Sender sender, String[] args)
    {
        if (args.length == 2)
        {
            sender.sendMessage(Color.Text + "Currently using " + Color.Value + pm().getUUIDPlayerDB().getType().name() + Color.Text + " as uuid player database");
        }
        else if (args.length == 3)
        {
            String stype = args[2];
            UUIDPlayerDBType type = UUIDPlayerDBType.getByName(stype);
            if (type == null)
            {
                sender.sendMessage(Color.Error + "Invalid backend type! "
                        + Color.Value + UUIDPlayerDBType.None.name() + Color.Error + ", "
                        + Color.Value + UUIDPlayerDBType.YAML.name() + Color.Error + " or "
                        + Color.Value + UUIDPlayerDBType.MySQL.name() + Color.Error + " is required!");
                return true;
            }

            if (type == pm().getUUIDPlayerDB().getType())
            {
                sender.sendMessage(Color.Error + "Invalid uuid-player-database type! You can't migrate to same type!");
                return true;
            }

            sender.sendMessage(Color.Text + "Migrating uuid-player-database to " + Color.Value + type.name() + Color.Text + " ...");
            pm().migrateUUIDPlayerDB(type);
            sender.sendMessage(Color.Message + "Finished migration.");
        }
        else
        {
            Messages.sendTooManyArgsMessage(sender);
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

    private String formatBool(boolean b)
    {
        return (b ? ChatColor.GREEN : ChatColor.RED) + String.valueOf(b).toUpperCase();
    }
}
