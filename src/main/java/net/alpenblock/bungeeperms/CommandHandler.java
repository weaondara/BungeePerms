package net.alpenblock.bungeeperms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
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

        if (args.length == 0)
        {
            sender.sendMessage(ChatColor.GOLD + "Welcome to BungeePerms, a BungeeCord permissions plugin");
            sender.sendMessage(Color.Text + "Version " + ChatColor.GOLD + plugin.getVersion());
            sender.sendMessage(Color.Text + "Author " + ChatColor.GOLD + plugin.getAuthor());
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
        //todo: better help output with pages

        if (checker.hasOrConsole(sender, "bungeeperms.help", true))
        {
            showHelp(sender);
            return true;
        }
        return true;
    }

    private boolean handleReload(Sender sender, String[] args)
    {
        if (!checker.hasOrConsole(sender, "bungeeperms.reload", true))
        {
            return true;
        }

        BungeePerms.getInstance().reload();
        sender.sendMessage(Color.Text + "Permissions reloaded");
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

        if (args[1].equalsIgnoreCase("on"))
        {
            config.setDebug(true);
            sender.sendMessage(Color.Text + "Debug mode enabled.");
            return true;
        }
        else if (args[1].equalsIgnoreCase("off"))
        {
            config.setDebug(false);
            sender.sendMessage(Color.Text + "Debug mode disabled.");
            return true;
        }
        else
        {
            sender.sendMessage(Color.Error + "'on' or 'off' is required!");
            return true;
        }
    }

    private boolean handleUsers(Sender sender, String[] args)
    {
        if (!checker.hasOrConsole(sender, "bungeeperms.users.list", true))
        {
            return true;
        }

        if (args.length == 1)
        {
            List<String> users = pm().getRegisteredUsers();
            if (users.isEmpty())
            {
                sender.sendMessage(Color.Text + "No players found!");
            }
            else
            {
                String out = Color.Text + "Following players are registered: ";
                for (int i = 0; i < users.size(); i++)
                {
                    out += Color.User + users.get(i) + Color.Text + (i + 1 < users.size() ? ", " : "");
                }
                sender.sendMessage(out);
            }
            return true;
        }
        else if (args.length == 2)
        {
            //for counting
            if (!args[1].equalsIgnoreCase("-c"))
            {
                return false;
            }

            if (pm().getRegisteredUsers().isEmpty())
            {
                sender.sendMessage(Color.Text + "No players found!");
            }
            else
            {
                sender.sendMessage(Color.Text + "There are " + Color.Value + pm().getRegisteredUsers().size() + Color.Text + " players registered.");
            }
            return true;
        }
        else
        {
            Messages.sendTooManyArgsMessage(sender);
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
        else if (args[2].equalsIgnoreCase("has"))
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
        return false;
    }

//user commands
    private boolean handleUserCommandsList(Sender sender, String[] args)
    {
        if (!checker.hasOrConsole(sender, "bungeeperms.user.perms.list", true))
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
            sender.sendMessage(Color.Error + "The player " + Color.User + player + Color.Error + " does not exist!");
            return true;
        }

        sender.sendMessage(Color.Text + "Permissions of the player " + Color.User + user.getName() + Color.Text + " (" + Color.User + user.getUUID() + Color.Text + "):");
        List<BPPermission> perms = user.getPermsWithOrigin(server, world);
        for (BPPermission perm : perms)
        {
            sender.sendMessage(Color.Text + "- " + Color.Value + perm.getPermission() + Color.Text
                    + " ("
                    + Color.Value + (!perm.isGroup() && perm.getOrigin().equalsIgnoreCase(player) ? "own" : perm.getOrigin()) + Color.Text
                    + (perm.getServer() != null ? " | " + Color.Value + perm.getServer() + Color.Text : "")
                    + (perm.getWorld() != null ? " | " + Color.Value + perm.getWorld() + Color.Text : "")
                    + ")");
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
            sender.sendMessage(Color.Error + "The player " + Color.User + player + Color.Error + " does not exist!");
            return true;
        }

        sender.sendMessage(Color.Text + "Groups of the player " + Color.User + user.getName() + Color.Text + ":");
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

        if (!Statics.matchArgs(sender, args, 3))
        {
            return true;
        }

        String player = Statics.getFullPlayerName(args[1]);
        User user = pm().getUser(player);
        if (user == null)
        {
            sender.sendMessage(Color.Error + "The player " + Color.User + player + Color.Error + " does not exist!");
            return true;
        }

        sender.sendMessage(Color.Text + "About " + Color.User + user.getName());

        sender.sendMessage(Color.Text + "UUID: " + Color.Value + user.getUUID());

        String groups = "";
        for (int i = 0; i < user.getGroups().size(); i++)
        {
            groups += Color.Value + user.getGroups().get(i).getName() + Color.Text + " (" + Color.Value + user.getGroups().get(i).getPerms().size() + Color.Text + ")" + (i + 1 < user.getGroups().size() ? ", " : "");
        }
        sender.sendMessage(Color.Text + "Groups: " + groups);

        //all group perms
        sender.sendMessage(Color.Text + "Effective permissions: " + Color.Value + user.getEffectivePerms().size());//TODO
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
            sender.sendMessage(Color.Error + "The player " + Color.User + player + Color.Error + " does not exist!");
            return true;
        }

        pm().deleteUser(user);

        sender.sendMessage(Color.Text + "User deleted");
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
            sender.sendMessage(Color.Error + "The player " + Color.User + player + Color.Error + " does not exist!");
            return true;
        }

        if (server == null)
        {
            if (user.getExtraPerms().contains("-" + perm))
            {
                pm().removeUserPerm(user, "-" + perm);
                sender.sendMessage(Color.Text + "Added permission " + Color.Value + perm + Color.Text + " to player " + Color.User + user.getName() + Color.Text + ".");
            }
            else if (!user.getExtraPerms().contains(perm))
            {
                pm().addUserPerm(user, perm);
                sender.sendMessage(Color.Text + "Added permission " + Color.Value + perm + Color.Text + " to player " + Color.User + user.getName() + Color.Text + ".");
            }
            else
            {
                sender.sendMessage(Color.Text + "The player " + Color.Value + user.getName() + Color.Text + " already has the permission " + Color.Value + perm + Color.Text + ".");
            }
        }
        else
        {
            if (world == null)
            {
                List<String> perserverperms = user.getServerPerms().get(server);
                if (perserverperms == null)
                {
                    perserverperms = new ArrayList<>();
                    user.getServerPerms().put(server, perserverperms);
                }

                if (perserverperms.contains("-" + perm))
                {
                    pm().removeUserPerServerPerm(user, server, "-" + perm);
                    sender.sendMessage(Color.Text + "Added permission " + Color.Value + perm + Color.Text + " to player " + Color.User + user.getName() + Color.Text + " on server " + Color.Value + server + Color.Text + ".");
                }
                else if (!perserverperms.contains(perm))
                {
                    pm().addUserPerServerPerm(user, server, perm);
                    sender.sendMessage(Color.Text + "Added permission " + Color.Value + perm + Color.Text + " to player " + Color.User + user.getName() + Color.Text + " on server " + Color.Value + server + Color.Text + ".");
                }
                else
                {
                    sender.sendMessage(Color.Text + "The player " + Color.Value + user.getName() + Color.Text + " alreday has the permission " + Color.Value + perm + Color.Text + " on server " + Color.Value + server + Color.Text + ".");
                }
            }
            else
            {
                Map<String, List<String>> perserverperms = user.getServerWorldPerms().get(server);
                if (perserverperms == null)
                {
                    perserverperms = new HashMap<>();
                    user.getServerWorldPerms().put(server, perserverperms);
                }

                List<String> perserverworldperms = perserverperms.get(world);
                if (perserverworldperms == null)
                {
                    perserverworldperms = new ArrayList<>();
                    perserverperms.put(world, perserverworldperms);
                }

                if (perserverworldperms.contains("-" + perm))
                {
                    pm().removeUserPerServerWorldPerm(user, server, world, "-" + perm);
                    sender.sendMessage(Color.Text + "Added permission " + Color.Value + perm + Color.Text + " to player " + Color.User + user.getName() + Color.Text + " on server " + Color.Value + server + Color.Text + " in world " + Color.Value + world + Color.Text + ".");
                }
                else if (!perserverworldperms.contains(perm))
                {
                    pm().addUserPerServerWorldPerm(user, server, world, perm);
                    sender.sendMessage(Color.Text + "Added permission " + Color.Value + perm + Color.Text + " to player " + Color.User + user.getName() + Color.Text + " on server " + Color.Value + server + Color.Text + " in world " + Color.Value + world + Color.Text + ".");
                }
                else
                {
                    sender.sendMessage(Color.Text + "The player " + Color.Value + user.getName() + Color.Text + " alreday has the permission " + Color.Value + perm + Color.Text + " on server " + Color.Value + server + Color.Text + " in world " + Color.Value + world + Color.Text + ".");
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
            sender.sendMessage(Color.Error + "The player " + Color.User + player + Color.Error + " does not exist!");
            return true;
        }

        if (server == null)
        {
            if (user.getExtraPerms().contains(perm))
            {
                pm().removeUserPerm(user, perm);
                sender.sendMessage(Color.Text + "Removed permission " + Color.Value + perm + Color.Text + " from player " + Color.User + user.getName() + Color.Text + ".");
            }
            else if (!user.getExtraPerms().contains("-" + perm))
            {
                pm().addUserPerm(user, "-" + perm);
                sender.sendMessage(Color.Text + "Removed permission " + Color.Value + perm + Color.Text + " from player " + Color.User + user.getName() + Color.Text + ".");
            }
            else
            {
                sender.sendMessage(Color.Text + "The player " + Color.Value + user.getName() + Color.Text + " never had the permission " + Color.Value + perm + Color.Text + ".");
            }
        }
        else
        {
            if (world == null)
            {
                List<String> perserverperms = user.getServerPerms().get(server);
                if (perserverperms == null)
                {
                    perserverperms = new ArrayList<>();
                    user.getServerPerms().put(server, perserverperms);
                }

                if (perserverperms.contains(perm))
                {
                    pm().removeUserPerServerPerm(user, server, perm);
                    sender.sendMessage(Color.Text + "Removed permission " + Color.Value + perm + Color.Text + " from player " + Color.User + user.getName() + Color.Text + " on server " + Color.Value + server + Color.Text + ".");
                }
                else if (!perserverperms.contains("-" + perm))
                {
                    pm().addUserPerServerPerm(user, server, "-" + perm);
                    sender.sendMessage(Color.Text + "Removed permission " + Color.Value + perm + Color.Text + " from player " + Color.User + user.getName() + Color.Text + " on server " + Color.Value + server + Color.Text + ".");
                }
                else
                {
                    sender.sendMessage(Color.Text + "The player " + Color.Value + user.getName() + Color.Text + " never had the permission " + Color.Value + perm + Color.Text + " on server " + Color.Value + server + Color.Text + ".");
                }
            }
            else
            {
                Map<String, List<String>> perserverperms = user.getServerWorldPerms().get(server);
                if (perserverperms == null)
                {
                    perserverperms = new HashMap<>();
                    user.getServerWorldPerms().put(server, perserverperms);
                }

                List<String> perserverworldperms = perserverperms.get(world);
                if (perserverworldperms == null)
                {
                    perserverworldperms = new ArrayList<>();
                    perserverperms.put(world, perserverworldperms);
                }

                if (perserverworldperms.contains(perm))
                {
                    pm().removeUserPerServerWorldPerm(user, server, world, perm);
                    sender.sendMessage(Color.Text + "Removed permission " + Color.Value + perm + Color.Text + " from player " + Color.User + user.getName() + Color.Text + " on server " + Color.Value + server + Color.Text + " in world " + Color.Value + world + Color.Text + ".");
                }
                else if (!perserverworldperms.contains("-" + perm))
                {
                    pm().addUserPerServerWorldPerm(user, server, world, "-" + perm);
                    sender.sendMessage(Color.Text + "Removed permission " + Color.Value + perm + Color.Text + " from player " + Color.User + user.getName() + Color.Text + " on server " + Color.Value + server + Color.Text + " in world " + Color.Value + world + Color.Text + ".");
                }
                else
                {
                    sender.sendMessage(Color.Text + "The player " + Color.Value + user.getName() + Color.Text + " never had the permission " + Color.Value + perm + Color.Text + " on server " + Color.Value + server + Color.Text + " in world " + Color.Value + world + Color.Text + ".");
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
        String server = args.length > 4 ? args[4].toLowerCase() : null;
        String world = args.length > 5 ? args[5].toLowerCase() : null;
        User user = pm().getUser(player);
        if (user == null)
        {
            sender.sendMessage(Color.Error + "The player " + Color.User + player + Color.Error + " does not exist!");
            return true;
        }

        if (server == null)
        {
            boolean has = checker.hasPerm(player, args[3].toLowerCase());
            sender.sendMessage(Color.Text + "Player " + Color.User + user.getName() + Color.Text + " has the permission " + Color.Value + args[3] + Color.Text + ": " + (has ? ChatColor.GREEN : ChatColor.RED) + String.valueOf(has).toUpperCase());
        }
        else
        {
            if (world == null)
            {
                boolean has = checker.hasPermOnServer(user.getName(), args[3].toLowerCase(), server);
                sender.sendMessage(Color.Text + "Player " + Color.User + user.getName() + Color.Text + " has the permission " + Color.Value + args[3] + Color.Text + " on server " + Color.Value + server + Color.Text + ": " + (has ? ChatColor.GREEN : ChatColor.RED) + String.valueOf(has).toUpperCase());
            }
            else
            {
                boolean has = checker.hasPermOnServerInWorld(user.getName(), args[3].toLowerCase(), server, world);
                sender.sendMessage(Color.Text + "Player " + Color.User + user.getName() + Color.Text + " has the permission " + Color.Value + args[3] + Color.Text + " on server " + Color.Value + server + Color.Text + " in world " + Color.Value + world + Color.Text + ": " + (has ? ChatColor.GREEN : ChatColor.RED) + String.valueOf(has).toUpperCase());
            }
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
            sender.sendMessage(Color.Error + "The group " + Color.User + groupname + Color.Error + " does not exist!");
            return true;
        }

        User u = pm().getUser(player);
        if (u == null)
        {
            sender.sendMessage(Color.Error + "The player " + Color.User + player + Color.Error + " does not exist!");
            return true;
        }

        List<Group> groups = u.getGroups();
        for (Group g : groups)
        {
            if (g.getName().equalsIgnoreCase(group.getName()))
            {
                sender.sendMessage(Color.Error + "Player is already in group " + Color.Value + groupname + Color.Error + "!");
                return true;
            }
        }

        pm().addUserGroup(u, group);
        sender.sendMessage(Color.Text + "Added group " + Color.Value + groupname + Color.Text + " to player " + Color.User + u.getName() + Color.Text + ".");
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
            sender.sendMessage(Color.Error + "The group " + Color.User + groupname + Color.Error + " does not exist!");
            return true;
        }

        User u = pm().getUser(player);
        if (u == null)
        {
            sender.sendMessage(Color.Error + "The player " + Color.User + player + Color.Error + " does not exist!");
            return true;
        }

        List<Group> groups = u.getGroups();
        for (Group g : groups)
        {
            if (g.getName().equalsIgnoreCase(group.getName()))
            {
                pm().removeUserGroup(u, group);
                sender.sendMessage(Color.Text + "Removed group " + Color.Value + groupname + Color.Text + " from player " + Color.User + u.getName() + Color.Text + ".");
                return true;
            }
        }
        sender.sendMessage(Color.Error + "Player is not in group " + Color.Value + groupname + Color.Error + "!");
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
            sender.sendMessage(Color.Error + "The group " + Color.User + groupname + Color.Error + " does not exist!");
            return true;
        }

        User u = pm().getUser(player);
        if (u == null)
        {
            sender.sendMessage(Color.Error + "The player " + Color.User + player + Color.Error + " does not exist!");
            return true;
        }

        List<Group> laddergroups = pm().getLadderGroups(group.getLadder());
        for (Group g : laddergroups)
        {
            pm().removeUserGroup(u, g);
        }

        pm().addUserGroup(u, group);
        sender.sendMessage(Color.Text + "Set group " + Color.Value + groupname + Color.Text + " for player " + Color.User + u.getName() + Color.Text + ".");
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
            sender.sendMessage(Color.Text + "No groups found!");
        }
        else
        {
            sender.sendMessage(Color.Text + "There are following groups:");
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
        return false;
    }

//group commands
    private boolean handleGroupCommandsList(Sender sender, String[] args)
    {
        if (!checker.hasOrConsole(sender, "bungeeperms.group.perms.list", true))
        {
            return true;
        }

        if (args.length > 5)
        {
            Messages.sendTooManyArgsMessage(sender);
            return true;
        }

        String groupname = args[1];
        String server = args.length > 3 ? args[3] : null;
        String world = args.length > 4 ? args[4] : null;
        Group group = pm().getGroup(groupname);

        if (group == null)
        {
            sender.sendMessage(Color.Error + "The group " + Color.Value + groupname + Color.Error + " does not exist!");
            return true;
        }

        sender.sendMessage(Color.Text + "Permissions of the group " + Color.Value + group.getName() + Color.Text + ":");
        List<BPPermission> perms = group.getPermsWithOrigin(server, world);
        for (BPPermission perm : perms)
        {
            sender.sendMessage(Color.Text + "- " + Color.Value + perm.getPermission() + Color.Text
                    + " ("
                    + Color.Value + (perm.getOrigin().equalsIgnoreCase(groupname) ? "own" : perm.getOrigin()) + Color.Text
                    + (perm.getServer() != null ? " | " + Color.Value + perm.getServer() + Color.Text : "")
                    + (perm.getWorld() != null ? " | " + Color.Value + perm.getWorld() + Color.Text : "")
                    + ")");
        }
        return true;
    }

    private boolean handleGroupCommandsInfo(Sender sender, String[] args)
    {
        if (!checker.hasOrConsole(sender, "bungeeperms.group.info", true))
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
            sender.sendMessage(Color.Error + "The group " + Color.Value + groupname + Color.Error + " does not exist!");
            return true;
        }

        sender.sendMessage(Color.Text + "Info to group " + Color.Value + group.getName() + Color.Text + ":");

        //inheritances
        String inheritances = "";
        for (int i = 0; i < group.getInheritances().size(); i++)
        {
            inheritances += Color.Value + group.getInheritances().get(i) + Color.Text + " (" + Color.Value + pm().getGroup(group.getInheritances().get(i)).getPerms().size() + Color.Text + ")" + (i + 1 < group.getInheritances().size() ? ", " : "");
        }
        if (inheritances.length() == 0)
        {
            inheritances = Color.Text + "(none)";
        }
        sender.sendMessage(Color.Text + "Inheritances: " + inheritances);

        //group perms
        sender.sendMessage(Color.Text + "Group permissions: " + Color.Value + group.getPerms().size());

        //group rank
        sender.sendMessage(Color.Text + "Rank: " + Color.Value + group.getRank());

        //group weight
        sender.sendMessage(Color.Text + "Weight: " + Color.Value + group.getWeight());

        //group ladder
        sender.sendMessage(Color.Text + "Ladder: " + Color.Value + group.getLadder());

        //default
        sender.sendMessage(Color.Text + "Default: " + Color.Value + (group.isDefault() ? ChatColor.GREEN : ChatColor.RED) + String.valueOf(group.isDefault()).toUpperCase());

        //all group perms
        sender.sendMessage(Color.Text + "Effective permissions: " + Color.Value + group.getEffectivePerms().size());

        //display
        sender.sendMessage(Color.Text + "Dislay name: " + ChatColor.RESET + (group.getDisplay().length() > 0 ? group.getDisplay() : Color.Text + "(none)"));

        //prefix
        sender.sendMessage(Color.Text + "Prefix: " + ChatColor.RESET + (group.getPrefix().length() > 0 ? group.getPrefix() : Color.Text + "(none)"));

        //suffix
        sender.sendMessage(Color.Text + "Suffix: " + ChatColor.RESET + (group.getSuffix().length() > 0 ? group.getSuffix() : Color.Text + "(none)"));
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
            sender.sendMessage(Color.Error + "The group " + Color.Value + groupname + Color.Error + " doesn't exists!");
            return true;
        }
        List<String> users = pm().getGroupUsers(group);

        if (args.length == 3)
        {
            if (users.isEmpty())
            {
                sender.sendMessage(Color.Text + "No players found!");
            }
            else
            {
                String out = Color.Text + "Following players are in group " + Color.Value + group.getName() + Color.Text + ": ";
                for (int i = 0; i < users.size(); i++)
                {
                    out += Color.User + users.get(i) + Color.Text + (i + 1 < users.size() ? ", " : "");
                }
                sender.sendMessage(out);
            }
            return true;
        }
        else if (args.length == 4)
        {
            if (!args[3].equalsIgnoreCase("-c"))
            {
                return false;
            }
            if (users.isEmpty())
            {
                sender.sendMessage(Color.Text + "No players found!");
            }
            else
            {
                sender.sendMessage(Color.Text + "There are " + Color.Value + users.size() + Color.Text + " players in group " + Color.Value + group.getName() + Color.Text + ".");
            }
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
            sender.sendMessage(Color.Error + "The group " + Color.Value + groupname + Color.Error + " already exists!");
            return true;
        }
        Group group = new Group(groupname, new ArrayList<String>(), new ArrayList<String>(), new HashMap<String, Server>(), 1500, 1500, "default", false, "", "", "");
        pm().addGroup(group);
        sender.sendMessage(Color.Text + "Group " + Color.Value + groupname + Color.Text + " created.");
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
        if (group != null)
        {
            sender.sendMessage(Color.Text + "Group deletion in progress ... this may take a while (backend integrity check).");
            pm().deleteGroup(group);
            sender.sendMessage(Color.Text + "Group " + Color.Value + group.getName() + Color.Text + " deleted.");
        }
        else
        {
            sender.sendMessage(Color.Error + "The group " + Color.Value + groupname + Color.Error + " does not exist!");
        }
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
            sender.sendMessage(Color.Error + "The group " + Color.Value + groupname + Color.Error + " does not exist!");
            return true;
        }

        //global perm
        if (server == null)
        {
            if (group.getPerms().contains("-" + perm))
            {
                pm().removeGroupPerm(group, "-" + perm);
                sender.sendMessage(Color.Text + "Added permission " + Color.Value + perm + Color.Text + " to group " + Color.Value + group.getName() + Color.Text + ".");
            }
            else if (!group.getPerms().contains(perm))
            {
                pm().addGroupPerm(group, perm);
                sender.sendMessage(Color.Text + "Added permission " + Color.Value + perm + Color.Text + " to group " + Color.Value + group.getName() + Color.Text + ".");
            }
            else
            {
                sender.sendMessage(Color.Text + "The group " + Color.Value + group.getName() + Color.Text + " already has the permission " + Color.Value + perm + Color.Text + ".");
            }
        }
        else
        {
            Server srv = group.getServers().get(server);
            if (srv == null)
            {
                srv = new Server(server, new ArrayList<String>(), new HashMap<String, World>(), "", "", "");
                group.getServers().put(server, srv);
            }

            //per server perm
            if (world == null)
            {
                List<String> perserverperms = srv.getPerms();
                if (perserverperms.contains("-" + perm))
                {
                    pm().removeGroupPerServerPerm(group, server, "-" + perm);
                    sender.sendMessage(Color.Text + "Added permission " + Color.Value + perm + Color.Text + " to group " + Color.Value + group.getName() + Color.Text + " on server " + Color.Value + server + Color.Text + ".");
                }
                else if (!perserverperms.contains(perm))
                {
                    pm().addGroupPerServerPerm(group, server, perm);
                    sender.sendMessage(Color.Text + "Added permission " + Color.Value + perm + Color.Text + " to group " + Color.Value + group.getName() + Color.Text + " on server " + Color.Value + server + Color.Text + ".");
                }
                else
                {
                    sender.sendMessage(Color.Text + "The group " + Color.Value + group.getName() + Color.Text + " already has the permission " + Color.Value + perm + Color.Text + " on server " + Color.Value + server + Color.Text + ".");
                }
            }

            //per server world perms
            else
            {
                World w = srv.getWorlds().get(world);
                if (w == null)
                {
                    w = new World(world, new ArrayList<String>(), "", "", "");
                    srv.getWorlds().put(world, w);
                }

                List<String> perserverworldperms = w.getPerms();
                if (perserverworldperms.contains("-" + perm))
                {
                    pm().removeGroupPerServerWorldPerm(group, server, world, "-" + perm);
                    sender.sendMessage(Color.Text + "Added permission " + Color.Value + perm + Color.Text + " to group " + Color.Value + group.getName() + Color.Text + " on server " + Color.Value + server + Color.Text + " in world " + Color.Value + world + Color.Text + ".");
                }
                else if (!perserverworldperms.contains(perm))
                {
                    pm().addGroupPerServerWorldPerm(group, server, world, perm);
                    sender.sendMessage(Color.Text + "Added permission " + Color.Value + perm + Color.Text + " to group " + Color.Value + group.getName() + Color.Text + " on server " + Color.Value + server + Color.Text + " in world " + Color.Value + world + Color.Text + ".");
                }
                else
                {
                    sender.sendMessage(Color.Text + "The group " + Color.Value + group.getName() + Color.Text + " already has the permission " + Color.Value + perm + Color.Text + " on server " + Color.Value + server + Color.Text + " in world " + Color.Value + world + Color.Text + ".");
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
            sender.sendMessage(Color.Error + "The group " + Color.Value + groupname + Color.Error + " does not exist!");
            return true;
        }

        //global perm
        if (server == null)
        {
            if (group.getPerms().contains(perm))
            {
                pm().removeGroupPerm(group, perm);
                sender.sendMessage(Color.Text + "Removed permission " + Color.Value + perm + Color.Text + " from group " + Color.Value + group.getName() + Color.Text + ".");
            }
            else if (!group.getPerms().contains("-" + perm))
            {
                pm().addGroupPerm(group, "-" + perm);
                sender.sendMessage(Color.Text + "Removed permission " + Color.Value + perm + Color.Text + " from group " + Color.Value + group.getName() + Color.Text + ".");
            }
            else
            {
                sender.sendMessage(Color.Text + "The group " + Color.Value + group.getName() + Color.Text + " never had the permission " + Color.Value + perm + Color.Text + ".");
            }
        }
        else
        {
            Server srv = group.getServers().get(server);
            if (srv == null)
            {
                srv = new Server(server, new ArrayList<String>(), new HashMap<String, World>(), "", "", "");
                group.getServers().put(server, srv);
            }

            //per server perm
            if (world == null)
            {
                List<String> perserverperms = srv.getPerms();
                if (perserverperms.contains(perm))
                {
                    pm().removeGroupPerServerPerm(group, server, perm);
                    sender.sendMessage(Color.Text + "Removed permission " + Color.Value + perm + Color.Text + " from group " + Color.Value + group.getName() + Color.Text + " on server " + Color.Value + server + Color.Text + ".");
                }
                else if (!perserverperms.contains("-" + perm))
                {
                    pm().addGroupPerServerPerm(group, server, "-" + perm);
                    sender.sendMessage(Color.Text + "Removed permission " + Color.Value + perm + Color.Text + " from group " + Color.Value + group.getName() + Color.Text + " on server " + Color.Value + server + Color.Text + ".");
                }
                else
                {
                    sender.sendMessage(Color.Text + "The group " + Color.Value + group.getName() + Color.Text + " never had the permission " + Color.Value + perm + Color.Text + " on server " + Color.Value + server + Color.Text + ".");
                }
            }
            else
            {
                World w = srv.getWorlds().get(world);
                if (w == null)
                {
                    w = new World(world, new ArrayList<String>(), "", "", "");
                    srv.getWorlds().put(world, w);
                }

                List<String> perserverworldperms = w.getPerms();
                if (perserverworldperms.contains(perm))
                {
                    pm().removeGroupPerServerWorldPerm(group, server, world, perm);
                    sender.sendMessage(Color.Text + "Removed permission " + Color.Value + perm + Color.Text + " from group " + Color.Value + group.getName() + Color.Text + " on server " + Color.Value + server + Color.Text + " in world " + Color.Value + world + Color.Text + ".");
                }
                else if (!perserverworldperms.contains("-" + perm))
                {
                    pm().addGroupPerServerWorldPerm(group, server, world, "-" + perm);
                    sender.sendMessage(Color.Text + "Removed permission " + Color.Value + perm + Color.Text + " from group " + Color.Value + group.getName() + Color.Text + " on server " + Color.Value + server + Color.Text + " in world " + Color.Value + world + Color.Text + ".");
                }
                else
                {
                    sender.sendMessage(Color.Text + "The group " + Color.Value + group.getName() + Color.Text + " never had the permission " + Color.Value + perm + Color.Text + " on server " + Color.Value + server + Color.Text + " in world " + Color.Value + world + Color.Text + ".");
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
            sender.sendMessage(Color.Error + "The group " + Color.Value + groupname + Color.Error + " does not exist!");
            return true;
        }

        //global perm
        if (server == null)
        {
            boolean has = group.has(perm.toLowerCase());
            sender.sendMessage(Color.Text + "Group " + Color.Value + group.getName() + Color.Text + " has the permission " + Color.Value + args[3] + Color.Text + ": " + (has ? ChatColor.GREEN : ChatColor.RED) + String.valueOf(has).toUpperCase());
        }
        else
        {
            //per server perm
            if (world == null)
            {
                boolean has = group.hasOnServer(perm.toLowerCase(), server);
                sender.sendMessage(Color.Text + "Group " + Color.Value + group.getName() + Color.Text + " has the permission " + Color.Value + args[3] + Color.Text + " on server " + Color.Value + server + Color.Text + ": " + (has ? ChatColor.GREEN : ChatColor.RED) + String.valueOf(has).toUpperCase());
            }

            //per server world perm
            else
            {
                boolean has = group.hasOnServerInWorld(perm.toLowerCase(), server, world);
                sender.sendMessage(Color.Text + "Group " + Color.Value + group.getName() + Color.Text + " has the permission " + Color.Value + args[3] + Color.Text + " on server " + Color.Value + server + Color.Text + " in world " + Color.Value + world + Color.Text + ": " + (has ? ChatColor.GREEN : ChatColor.RED) + String.valueOf(has).toUpperCase());
            }
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
            sender.sendMessage(Color.Error + "The group " + Color.Value + groupname + Color.Error + " does not exist!");
            return true;
        }

        Group toadd = pm().getGroup(addgroup);
        if (toadd == null)
        {
            sender.sendMessage(Color.Error + "The group " + Color.Value + addgroup + Color.Error + " does not exist!");
            return true;
        }

        List<String> inheritances = group.getInheritances();

        //check for already existing inheritance
        for (String s : inheritances)
        {
            if (s.equalsIgnoreCase(toadd.getName()))
            {
                sender.sendMessage(Color.Error + "The group already inherits from " + Color.Value + addgroup + Color.Error + "!");
                return true;
            }
        }

        pm().addGroupInheritance(group, toadd);

        sender.sendMessage(Color.Text + "Added inheritance " + Color.Value + addgroup + Color.Text + " to group " + Color.Value + group.getName() + Color.Text + ".");
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
            sender.sendMessage(Color.Error + "The group " + Color.Value + groupname + Color.Error + " does not exist!");
            return true;
        }

        Group toremove = pm().getGroup(removegroup);
        if (toremove == null)
        {
            sender.sendMessage(Color.Error + "The group " + Color.Value + removegroup + Color.Error + " does not exist!");
            return true;
        }

        List<String> inheritances = group.getInheritances();
        for (String s : inheritances)
        {
            if (s.equalsIgnoreCase(toremove.getName()))
            {
                pm().removeGroupInheritance(group, toremove);

                sender.sendMessage(Color.Text + "Removed inheritance " + Color.Value + removegroup + Color.Text + " from group " + Color.Value + group.getName() + Color.Text + ".");
                return true;
            }
        }
        sender.sendMessage(Color.Error + "The group " + Color.Value + group.getName() + Color.Error + " does not inherit from group " + Color.Value + removegroup + Color.Error + "!");
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
            sender.sendMessage(Color.Error + "A whole number greater than 0 is required!");
            return true;
        }
        Group group = pm().getGroup(groupname);
        if (group == null)
        {
            sender.sendMessage(Color.Error + "The group " + Color.Value + groupname + Color.Error + " does not exist!");
            return true;
        }

        pm().rankGroup(group, rank);
        sender.sendMessage(Color.Text + "Group rank set for group " + Color.Value + group.getName() + Color.Text + ".");
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
            sender.sendMessage(Color.Error + "A whole number greater than 0 is required!");
            return true;
        }
        Group group = pm().getGroup(groupname);
        if (group == null)
        {
            sender.sendMessage(Color.Error + "The group " + Color.Value + groupname + Color.Error + " does not exist!");
            return true;
        }

        pm().weightGroup(group, weight);
        sender.sendMessage(Color.Text + "Group weight set for group " + Color.Value + group.getName() + Color.Text + ".");
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
            sender.sendMessage(Color.Error + "The group " + Color.Value + groupname + Color.Error + " does not exist!");
            return true;
        }
        pm().ladderGroup(group, ladder);
        sender.sendMessage(Color.Text + "Group ladder set for group " + Color.Value + group.getName() + Color.Text + ".");
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
            sender.sendMessage(Color.Error + "A form of '" + Color.Value + "true" + Color.Error + "','" + Color.Value + "false" + Color.Error + "','" + Color.Value + "yes" + Color.Error + "' or '" + Color.Value + "no" + Color.Error + "' is required!");
            return true;
        }

        Group group = pm().getGroup(groupname);
        if (group == null)
        {
            sender.sendMessage(Color.Error + "The group " + Color.Value + groupname + Color.Error + " does not exist!");
            return true;
        }
        pm().setGroupDefault(group, isdefault);
        sender.sendMessage(Color.Text + "Marked group " + Color.Value + group.getName() + Color.Text + " as " + (isdefault ? "" : "non-") + "default.");
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
        String display = args.length > 3 ? args[3] : "";
        String server = args.length > 4 ? args[4].toLowerCase() : null;
        String world = args.length > 5 ? args[5].toLowerCase() : null;
        Group group = pm().getGroup(groupname);
        if (group == null)
        {
            sender.sendMessage(Color.Error + "The group " + Color.Value + groupname + Color.Error + " does not exist!");
            return true;
        }
        pm().setGroupDisplay(group, display, server, world);
        sender.sendMessage(Color.Text + "Set display name for group " + Color.Value + group.getName() + Color.Text + ".");
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
        String prefix = args.length > 3 ? args[3] : "";
        String server = args.length > 4 ? args[4].toLowerCase() : null;
        String world = args.length > 5 ? args[5].toLowerCase() : null;
        Group group = pm().getGroup(groupname);
        if (group == null)
        {
            sender.sendMessage(Color.Error + "The group " + Color.Value + groupname + Color.Error + " does not exist!");
            return true;
        }
        pm().setGroupPrefix(group, prefix, server, world);
        sender.sendMessage(Color.Text + "Set prefix for group " + Color.Value + group.getName() + Color.Text + ".");
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
        String suffix = args.length > 3 ? args[3] : "";
        String server = args.length > 4 ? args[4].toLowerCase() : null;
        String world = args.length > 5 ? args[5].toLowerCase() : null;
        Group group = pm().getGroup(groupname);
        if (group == null)
        {
            sender.sendMessage(Color.Error + "The group " + Color.Value + groupname + Color.Error + " does not exist!");
            return true;
        }
        pm().setGroupSuffix(group, suffix, server, world);
        sender.sendMessage(Color.Text + "Set suffix for group " + Color.Value + group.getName() + Color.Text + ".");
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
            sender.sendMessage(Color.Error + "The player " + Color.User + args[1] + Color.Error + " does not exist!");
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
                sender.sendMessage(Color.Error + "The player " + Color.User + user.getName() + Color.Error + " doesn't have a group!");
                return true;
            }
            nextgroup = pm().getNextGroup(playergroup);
        }

        if (nextgroup == null)
        {
            sender.sendMessage(Color.Error + "The player " + Color.User + user.getName() + Color.Error + " can't be promoted!");
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
                sender.sendMessage(Color.Error + "You do not exist!");
                return true;
            }
            Group issuergroup = pm().getMainGroup(issuer);
            if (issuergroup == null)
            {
                sender.sendMessage(Color.Error + "You don't have a group!");
                return true;
            }
            if (!(issuergroup.getRank() < nextgroup.getRank()))
            {
                sender.sendMessage(Color.Error + "You can't promote the player " + Color.User + user.getName() + Color.Error + "!");
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
        sender.sendMessage(Color.User + user.getName() + Color.Text + " is now " + Color.Value + nextgroup.getName() + Color.Text + "!");

        //promote msg to user
        if (config.isNotifyPromote())
        {
            Sender s = plugin.getPlayer(user.getName());
            if (s != null)
            {
                s.sendMessage(Color.Text + "You were promoted to " + Color.Value + nextgroup.getName() + Color.Text + "!");
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
            sender.sendMessage(Color.Error + "The player " + Color.User + args[1] + Color.Error + " does not exist!");
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
                sender.sendMessage(Color.Error + "The player " + Color.User + user.getName() + Color.Error + " doesn't have a group!");
                return true;
            }
            previousgroup = pm().getPreviousGroup(playergroup);
        }

        if (previousgroup == null)
        {
            sender.sendMessage(Color.Error + "The player " + Color.User + user.getName() + Color.Error + " can't be demoted!");
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
                sender.sendMessage(Color.Error + "You do not exist!");
                return true;
            }
            Group issuergroup = pm().getMainGroup(issuer);
            if (issuergroup == null)
            {
                sender.sendMessage(Color.Error + "You don't have a group!");
                return true;
            }
            if (!(issuergroup.getRank() < playergroup.getRank()))
            {
                sender.sendMessage(Color.Error + "You can't demote the player " + Color.User + user.getName() + Color.Error + "!");
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
        sender.sendMessage(Color.User + user.getName() + Color.Text + " is now " + Color.Value + previousgroup.getName() + Color.Text + "!");

        //demote msg to user
        if (config.isNotifyDemote())
        {
            Sender s = plugin.getPlayer(user.getName());
            if (s != null)
            {
                s.sendMessage(Color.Text + "You were demoted to " + Color.Value + previousgroup.getName() + Color.Text + "!");
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

        sender.sendMessage(Color.Text + "Formating permissions file/table ...");
        pm().format();
        sender.sendMessage(Color.Message + "Finished formating.");
        return true;
    }

    private boolean handleCleanup(Sender sender, String[] args)
    {
        if (!checker.hasOrConsole(sender, "bungeeperms.cleanup", true))
        {
            return true;
        }

        sender.sendMessage(Color.Text + "Cleaning up permissions file/table ...");
        int deleted = pm().cleanup();
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
            if (pm().getBackEnd().getType() == BackEndType.MySQL)
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

    private void showHelp(Sender sender)
    {
        sender.sendMessage(ChatColor.GOLD + "                  ------ BungeePerms - Help -----");
        sender.sendMessage(ChatColor.GRAY + "Aliases: " + ChatColor.GOLD + "/bp");
        sender.sendMessage(ChatColor.GOLD + "/bungeeperms" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Welcomes you to BungeePerms");
        if (checker.hasPermOrConsole(sender.getName(), "bungeeperms.help"))
        {
            sender.sendMessage(ChatColor.GOLD + "/bungeeperms help" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Shows this help");
        }
        if (checker.hasPermOrConsole(sender.getName(), "bungeeperms.reload"))
        {
            sender.sendMessage(ChatColor.GOLD + "/bungeeperms reload" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Reloads the permissions");
        }
        if (checker.hasPermOrConsole(sender.getName(), "bungeeperms.users"))
        {
            sender.sendMessage(ChatColor.GOLD + "/bungeeperms users [-c]" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Lists the users [or shows the amount of them]");
        }
        if (checker.hasPermOrConsole(sender.getName(), "bungeeperms.user.info"))
        {
            sender.sendMessage(ChatColor.GOLD + "/bungeeperms user <username> info" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Shows information to the given user");
        }
        if (checker.hasPermOrConsole(sender.getName(), "bungeeperms.user.delete"))
        {
            sender.sendMessage(ChatColor.GOLD + "/bungeeperms user <username> delete" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Deletes the given user");
        }
        if (checker.hasPermOrConsole(sender.getName(), "bungeeperms.user.perms.add"))
        {
            sender.sendMessage(ChatColor.GOLD + "/bungeeperms user <username> addperm <permission> [server [world]]" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Adds a permission to the given user");
        }
        if (checker.hasPermOrConsole(sender.getName(), "bungeeperms.user.perms.remove"))
        {
            sender.sendMessage(ChatColor.GOLD + "/bungeeperms user <username> removeperm <permission> [server [world]]" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Remove a permission from the given user");
        }
        if (checker.hasPermOrConsole(sender.getName(), "bungeeperms.user.perms.has"))
        {
            sender.sendMessage(ChatColor.GOLD + "/bungeeperms user <username> has <permission> [server [world]]" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Checks if the given user has the given permission");
        }
        if (checker.hasPermOrConsole(sender.getName(), "bungeeperms.user.perms.list"))
        {
            sender.sendMessage(ChatColor.GOLD + "/bungeeperms user <username> list" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Lists the permissions of the given user");
        }
        if (checker.hasPermOrConsole(sender.getName(), "bungeeperms.user.group.add"))
        {
            sender.sendMessage(ChatColor.GOLD + "/bungeeperms user <username> addgroup <groupname>" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Added the given group to the given user");
        }
        if (checker.hasPermOrConsole(sender.getName(), "bungeeperms.user.group.remove"))
        {
            sender.sendMessage(ChatColor.GOLD + "/bungeeperms user <username> removegroup <groupname>" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Removes the given group from the given user");
        }
        if (checker.hasPermOrConsole(sender.getName(), "bungeeperms.user.group.set"))
        {
            sender.sendMessage(ChatColor.GOLD + "/bungeeperms user <username> setgroup <groupname>" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Sets the given group as the main group for the given user");
        }
        if (checker.hasPermOrConsole(sender.getName(), "bungeeperms.user.groups"))
        {
            sender.sendMessage(ChatColor.GOLD + "/bungeeperms user <username> groups" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Lists the groups the given user is in");
        }
        if (checker.hasPermOrConsole(sender.getName(), "bungeeperms.groups"))
        {
            sender.sendMessage(ChatColor.GOLD + "/bungeeperms groups" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Lists the groups");
        }
        if (checker.hasPermOrConsole(sender.getName(), "bungeeperms.group.info"))
        {
            sender.sendMessage(ChatColor.GOLD + "/bungeeperms group <groupname> info" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Shows information about the given group");
        }
        if (checker.hasPermOrConsole(sender.getName(), "bungeeperms.group.users"))
        {
            sender.sendMessage(ChatColor.GOLD + "/bungeeperms group <groupname> users [-c]" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Lists the users of the given group [or shows the amount of them]");
        }
        if (checker.hasPermOrConsole(sender.getName(), "bungeeperms.group.create"))
        {
            sender.sendMessage(ChatColor.GOLD + "/bungeeperms group <groupname> create" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Create a group with the given name");
        }
        if (checker.hasPermOrConsole(sender.getName(), "bungeeperms.group.delete"))
        {
            sender.sendMessage(ChatColor.GOLD + "/bungeeperms group <groupname> delete" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Create the given group");
        }
        if (checker.hasPermOrConsole(sender.getName(), "bungeeperms.group.inheritances.add"))
        {
            sender.sendMessage(ChatColor.GOLD + "/bungeeperms group <groupname> addinherit <group>" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Adds a inheritance to the given group");
        }
        if (checker.hasPermOrConsole(sender.getName(), "bungeeperms.group.inheritances.remove"))
        {
            sender.sendMessage(ChatColor.GOLD + "/bungeeperms group <groupname> removeinherit <group>" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Remove a inheritance from the given group");
        }
        if (checker.hasPermOrConsole(sender.getName(), "bungeeperms.group.rank"))
        {
            sender.sendMessage(ChatColor.GOLD + "/bungeeperms group <groupname> rank <new rank>" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Sets the rank for the given group");
        }
        if (checker.hasPermOrConsole(sender.getName(), "bungeeperms.group.weight"))
        {
            sender.sendMessage(ChatColor.GOLD + "/bungeeperms group <groupname> weight <new weight>" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Sets the weight for the given group");
        }
        if (checker.hasPermOrConsole(sender.getName(), "bungeeperms.group.ladder"))
        {
            sender.sendMessage(ChatColor.GOLD + "/bungeeperms group <groupname> ladder <new ladder>" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Sets the ladder for the given group");
        }
        if (checker.hasPermOrConsole(sender.getName(), "bungeeperms.group.default"))
        {
            sender.sendMessage(ChatColor.GOLD + "/bungeeperms group <groupname> default <true|false>" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Determines whether the given group is a default group or not");
        }
        if (checker.hasPermOrConsole(sender.getName(), "bungeeperms.group.display"))
        {
            sender.sendMessage(ChatColor.GOLD + "/bungeeperms group <groupname> display [displayname> [server [world]]]" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Sets the display name for the given group");
        }
        if (checker.hasPermOrConsole(sender.getName(), "bungeeperms.group.prefix"))
        {
            sender.sendMessage(ChatColor.GOLD + "/bungeeperms group <groupname> prefix [prefix> [server [world]]]" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Sets the prefix for the given group");
        }
        if (checker.hasPermOrConsole(sender.getName(), "bungeeperms.group.suffix"))
        {
            sender.sendMessage(ChatColor.GOLD + "/bungeeperms group <groupname> suffix [suffix> [server [world]]]" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Sets the suffix for the given group");
        }
        if (checker.hasPermOrConsole(sender.getName(), "bungeeperms.group.perms.add"))
        {
            sender.sendMessage(ChatColor.GOLD + "/bungeeperms group <groupname> addperm <permission> [server [world]]" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Adds a permission to the given group");
        }
        if (checker.hasPermOrConsole(sender.getName(), "bungeeperms.group.perms.remove"))
        {
            sender.sendMessage(ChatColor.GOLD + "/bungeeperms group <groupname> removeperm <permission> [server [world]]" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Remove a permission from the given group");
        }
        if (checker.hasPermOrConsole(sender.getName(), "bungeeperms.group.perms.has"))
        {
            sender.sendMessage(ChatColor.GOLD + "/bungeeperms group <groupname> has <permission> [server [world]]" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Checks if the given group has the given permission");
        }
        if (checker.hasPermOrConsole(sender.getName(), "bungeeperms.group.perms.list"))
        {
            sender.sendMessage(ChatColor.GOLD + "/bungeeperms group <groupname> list" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Lists the permissions of the given group");
        }
        if (checker.hasPermOrConsole(sender.getName(), "bungeeperms.promote"))
        {
            sender.sendMessage(ChatColor.GOLD + "/bungeeperms promote <username> [ladder]" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Promotes the given user to the next rank");
        }
        if (checker.hasPermOrConsole(sender.getName(), "bungeeperms.demote"))
        {
            sender.sendMessage(ChatColor.GOLD + "/bungeeperms demote <username> [ladder]" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Demotes the given user to the previous rank");
        }
        if (checker.hasPermOrConsole(sender.getName(), "bungeeperms.format"))
        {
            sender.sendMessage(ChatColor.GOLD + "/bungeeperms format" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Reformates the permission.yml or mysql table - " + ChatColor.RED + " BE CAREFUL");
        }
        if (checker.hasPermOrConsole(sender.getName(), "bungeeperms.cleanup"))
        {
            sender.sendMessage(ChatColor.GOLD + "/bungeeperms cleanup" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Cleans up the permission.yml or mysql table - " + ChatColor.RED + " !BE VERY CAREFUL! - removes a lot of players from the permissions.yml if configured");
        }
        if (checker.hasPermOrConsole(sender.getName(), "bungeeperms.migrate"))
        {
            sender.sendMessage(ChatColor.GOLD + "/bungeeperms migrate <backend [yaml|mysql|mysql2]|useuuid [true|false]|uuidplayerdb [None|YAML|MySQL]>" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Does migrations of different data (permissions, uuid) or shows status - " + ChatColor.RED + " !BE CAREFUL! (MAKE A BACKUP BEFORE EXECUTING)");
        }
        if (checker.hasPermOrConsole(sender.getName(), "bungeeperms.uuid"))
        {
            sender.sendMessage(ChatColor.GOLD + "/bungeeperms uuid <player|uuid> [-rm]" + ChatColor.WHITE + " - " + ChatColor.GRAY + "Gets the UUID of a player from database (-r: reverse; -m: ask mojang)");
        }
        sender.sendMessage(ChatColor.GOLD + "---------------------------------------------------");
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
}
