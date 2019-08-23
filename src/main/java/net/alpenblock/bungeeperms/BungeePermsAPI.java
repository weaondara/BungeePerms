package net.alpenblock.bungeeperms;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import net.alpenblock.bungeeperms.platform.PlatformType;
import net.alpenblock.bungeeperms.platform.bukkit.BukkitConfig;

public class BungeePermsAPI
{

    //group
    /**
     * Get all available groups
     *
     * @return all available groups
     */
    public static List<String> groups()
    {
        List<String> groups = new ArrayList<>();
        for (Group g : BungeePerms.getInstance().getPermissionsManager().getGroups())
            groups.add(g.getName());
        return groups;
    }

    /**
     * Tests if a group has the given permission
     *
     * @param group the group
     * @param permission the permission
     * @param server the server; may be null; if server == "" then the current server is used or null if BungeeCord
     * @param world the world; may be null
     * @return whether or not the group has the permission
     */
    public static boolean groupHas(String group, String permission, String server, String world)
    {
        server = server(server);
        world = world(server, world);
        permission = Statics.toLower(permission);
        Group g = BungeePerms.getInstance().getPermissionsManager().getGroup(group);
        if (g == null)
            return false;

        return g.has(permission, server, world);
    }

    /**
     * Adds the given permission to the group
     *
     * @param group the group
     * @param permission the permission
     * @param server the server; may be null; if server == "" then the current server is used or null if BungeeCord
     * @param world the world; may be null
     * @return true if the permission has been added successfully
     */
    public static boolean groupAdd(String group, String permission, String server, String world)
    {
        server = server(server);
        world = world(server, world);
        permission = Statics.toLower(permission);
        Group g = BungeePerms.getInstance().getPermissionsManager().getGroup(group);
        if (g == null)
            return false;

        BungeePerms.getInstance().getPermissionsManager().addGroupPerm(g, server, world, permission);
        return true;
    }

    /**
     * Removes the given permission from the group
     *
     * @param group the group
     * @param permission the permission
     * @param server the server; may be null; if server == "" then the current server is used or null if BungeeCord
     * @param world the world; may be null
     * @return true if the permission has been removed successfully
     */
    public static boolean groupRemove(String group, String permission, String server, String world)
    {
        server = server(server);
        world = world(server, world);
        permission = Statics.toLower(permission);
        Group g = BungeePerms.getInstance().getPermissionsManager().getGroup(group);
        if (g == null)
            return false;

        BungeePerms.getInstance().getPermissionsManager().removeGroupPerm(g, server, world, permission);
        return true;
    }

    /**
     * Adds the given timed permission to the group
     *
     * @param group the group
     * @param permission the permission
     * @param server the server; may be null; if server == "" then the current server is used or null if BungeeCord
     * @param world the world; may be null
     * @param start when the permission takes effect
     * @param dur the duration in seconds the permission is active
     * @return true if the permission has been added successfully
     */
    public static boolean groupTimedAdd(String group, String permission, String server, String world, Date start, int dur)
    {
        server = server(server);
        world = world(server, world);
        permission = Statics.toLower(permission);
        Group g = BungeePerms.getInstance().getPermissionsManager().getGroup(group);
        if (g == null)
            return false;

        BungeePerms.getInstance().getPermissionsManager().addGroupTimedPerm(g, server, world, new TimedValue(permission, start, dur));
        return true;
    }

    /**
     * Removes the given timed permission from the group
     *
     * @param group the group
     * @param permission the permission
     * @param server the server; may be null; if server == "" then the current server is used or null if BungeeCord
     * @param world the world; may be null
     * @return true if the permission has been removed successfully
     */
    public static boolean groupTimedRemove(String group, String permission, String server, String world)
    {
        server = server(server);
        world = world(server, world);
        permission = Statics.toLower(permission);
        Group g = BungeePerms.getInstance().getPermissionsManager().getGroup(group);
        if (g == null)
            return false;

        BungeePerms.getInstance().getPermissionsManager().removeGroupTimedPerm(g, server, world, permission);
        return true;
    }

    /**
     * Adds an inheritance to the group
     *
     * @param group the group
     * @param groupadd the group to add as inheritance
     * @return true if the inheritance has been added successfully
     */
    public static boolean groupAddInheritance(String group, String groupadd)
    {
        Group g = BungeePerms.getInstance().getPermissionsManager().getGroup(group);
        if (g == null)
            return false;

        Group add = BungeePerms.getInstance().getPermissionsManager().getGroup(groupadd);
        if (add == null)
            return false;

        if (g.getInheritances().contains(add))
            return false;

        BungeePerms.getInstance().getPermissionsManager().addGroupInheritance(g, add);
        return true;
    }

    /**
     * Removes an inheritance from the group
     *
     * @param group the group
     * @param groupremove the group to remove as inheritance
     * @return true if the inheritance has been removed successfully
     */
    public static boolean groupRemoveInheritance(String group, String groupremove)
    {
        Group g = BungeePerms.getInstance().getPermissionsManager().getGroup(group);
        if (g == null)
            return false;

        Group remove = BungeePerms.getInstance().getPermissionsManager().getGroup(groupremove);
        if (remove == null)
            return false;

        if (!g.getInheritances().contains(g))
            return false;

        BungeePerms.getInstance().getPermissionsManager().removeGroupInheritance(g, remove);
        return true;
    }

    /**
     * Adds a timed inheritance to the group
     *
     * @param group the group
     * @param groupadd the group to add as inheritance
     * @param start when the inheritance takes effect
     * @param dur the duration in seconds the inheritance is active
     * @return true if the inheritance has been added successfully
     */
    public static boolean groupAddTimedInheritance(String group, String groupadd, Date start, int dur)
    {
        Group g = BungeePerms.getInstance().getPermissionsManager().getGroup(group);
        if (g == null)
            return false;

        Group add = BungeePerms.getInstance().getPermissionsManager().getGroup(groupadd);
        if (add == null)
            return false;

        for (TimedValue<String> ti : g.getTimedInheritancesString())
            if (ti.getValue().equalsIgnoreCase(groupadd))
                return false;

        BungeePerms.getInstance().getPermissionsManager().addGroupTimedInheritance(g, new TimedValue<>(add, start, dur));
        return true;
    }

    /**
     * Removes a timed inheritance from the group
     *
     * @param group the group
     * @param groupremove the group to remove as inheritance
     * @return true if the inheritance has been removed successfully
     */
    public static boolean groupRemoveTimedInheritance(String group, String groupremove)
    {
        Group g = BungeePerms.getInstance().getPermissionsManager().getGroup(group);
        if (g == null)
            return false;

        Group remove = BungeePerms.getInstance().getPermissionsManager().getGroup(groupremove);
        if (remove == null)
            return false;

        boolean found = false;
        for (TimedValue<String> ti : g.getTimedInheritancesString())
            if (ti.getValue().equalsIgnoreCase(groupremove))
            {
                found = true;
                break;
            }

        if (!found)
            return false;

        BungeePerms.getInstance().getPermissionsManager().removeGroupTimedInheritance(g, remove);
        return true;
    }

    /**
     * Gets the full prefix of the given group
     *
     * @param group the group
     * @param server the server; may be null; if server == "" then the current server is used or null if BungeeCord
     * @param world the world; may be null
     * @return the full prefix; may be null
     */
    public static String groupPrefix(String group, String server, String world)
    {
        server = server(server);
        world = world(server, world);

        Group g = BungeePerms.getInstance().getPermissionsManager().getGroup(group);
        if (g == null)
            return null;

        return g.buildPrefix(server, world);
    }

    /**
     * Gets the full suffix of the given group
     *
     * @param group the group
     * @param server the server; may be null; if server == "" then the current server is used or null if BungeeCord
     * @param world the world; may be null
     * @return the full suffix; may be null
     */
    public static String groupSuffix(String group, String server, String world)
    {
        server = server(server);
        world = world(server, world);

        Group g = BungeePerms.getInstance().getPermissionsManager().getGroup(group);
        if (g == null)
            return null;

        return g.buildSuffix(server, world);
    }

    //user
    /**
     * Gets the primary/main group to the given users
     *
     * @param nameoruuid the username or uuid of the player
     * @return the primary/main group of the player; may be null
     */
    public static String userMainGroup(String nameoruuid)
    {
        User u = BungeePerms.getInstance().getPermissionsManager().getUser(nameoruuid);
        if (u == null)
            return null;
        Group g = BungeePerms.getInstance().getPermissionsManager().getMainGroup(u);
        return g == null ? null : g.getName();
    }

    /**
     * Get the "normal" groups of the player
     *
     * @param nameoruuid the username or uuid of the player
     * @return the "normal" groups of the player
     */
    public static List<String> userGroups(String nameoruuid)
    {
        User u = BungeePerms.getInstance().getPermissionsManager().getUser(nameoruuid);
        if (u == null)
            return new ArrayList();

        return new ArrayList(u.getGroupsString());
    }

    /**
     * Gets the timed groups of the player
     *
     * @param nameoruuid the username or uuid of the player
     * @return the timed groups of the player
     */
    public static List<String> userTimedGroups(String nameoruuid)
    {
        User u = BungeePerms.getInstance().getPermissionsManager().getUser(nameoruuid);
        if (u == null)
            return new ArrayList();

        List<String> ret = new ArrayList();
        for (TimedValue<String> tg : u.getTimedGroupsString())
            ret.add(tg.getValue());
        return ret;
    }

    /**
     * Gets all ("normal" and timed) groups of the player
     *
     * @param nameoruuid the username or uuid of the player
     * @return all grozup of the player
     */
    public static List<String> userAllGroups(String nameoruuid)
    {
        List<String> ret = userGroups(nameoruuid);
        ret.addAll(userTimedGroups(nameoruuid));
        return ret;
    }

    /**
     * Tests if the player is in a group
     *
     * @param nameoruuid the username or uuid of the player
     * @param group the group
     * @return whether or not the player is in the group
     */
    public static boolean userInGroup(String nameoruuid, String group)
    {
        for (String g : userAllGroups(nameoruuid))
            if (g.equalsIgnoreCase(group))
                return true;
        return false;
    }

    /**
     * Tests if the player has the given permission
     *
     * @param nameoruuid the username or uuid of the player
     * @param permission the permission
     * @param server the server; may be null; if server == "" then the current server is used or null if BungeeCord
     * @param world the world; may be null
     * @return whether or not the player has the permission
     */
    public static boolean userHasPermission(String nameoruuid, String permission, String server, String world)
    {
        server = server(server);
        world = world(server, world);

        User u = BungeePerms.getInstance().getPermissionsManager().getUser(nameoruuid);
        if (u == null)
            return false;

        return u.hasPerm(server, world, permission);
    }

    /**
     * Tests if the player has the given permission set explictly
     *
     * @param nameoruuid the username or uuid of the player
     * @param permission the permission
     * @param server the server; may be null; if server == "" then the current server is used or null if BungeeCord
     * @param world the world; may be null
     * @return whether or not the player has the permission set explictly
     */
    public static boolean userIsPermissionSet(String nameoruuid, String permission, String server, String world)
    {
        server = server(server);
        world = world(server, world);

        User u = BungeePerms.getInstance().getPermissionsManager().getUser(nameoruuid);
        if (u == null)
            return false;

        for (BPPermission p : u.getEffectivePerms(server, world))
            if (p.getPermission().equalsIgnoreCase(permission))
                return true;

        return false;
    }

    /**
     * Adds the given permission to the player
     *
     * @param nameoruuid the username or uuid of the player
     * @param permission the permission
     * @param server the server; may be null; if server == "" then the current server is used or null if BungeeCord
     * @param world the world; may be null
     * @return true if the permission has been added successfully
     */
    public static boolean userAdd(String nameoruuid, String permission, String server, String world)
    {
        server = server(server);
        world = world(server, world);
        permission = Statics.toLower(permission);
        User u = BungeePerms.getInstance().getPermissionsManager().getUser(nameoruuid);
        if (u == null)
            return false;

        BungeePerms.getInstance().getPermissionsManager().addUserPerm(u, server, world, permission);
        return true;
    }

    /**
     * Removes the given permission to the player
     *
     * @param nameoruuid the username or uuid of the player
     * @param permission the permission
     * @param server the server; may be null; if server == "" then the current server is used or null if BungeeCord
     * @param world the world; may be null
     * @return true if the permission has been removed successfully
     */
    public static boolean userRemove(String nameoruuid, String permission, String server, String world)
    {
        server = server(server);
        world = world(server, world);
        permission = Statics.toLower(permission);
        User u = BungeePerms.getInstance().getPermissionsManager().getUser(nameoruuid);
        if (u == null)
            return false;

        BungeePerms.getInstance().getPermissionsManager().removeUserPerm(u, server, world, permission);
        return true;
    }

    /**
     * Adds the given timed permission to the player
     *
     * @param nameoruuid the username or uuid of the player
     * @param permission the permission
     * @param server the server; may be null; if server == "" then the current server is used or null if BungeeCord
     * @param world the world; may be null
     * @param start when the permission takes effect
     * @param dur the duration in seconds the permission is active
     * @return true if the permission has been added successfully
     */
    public static boolean userTimedAdd(String nameoruuid, String permission, String server, String world, Date start, int dur)
    {
        server = server(server);
        world = world(server, world);
        permission = Statics.toLower(permission);
        User u = BungeePerms.getInstance().getPermissionsManager().getUser(nameoruuid);
        if (u == null)
            return false;

        BungeePerms.getInstance().getPermissionsManager().addUserTimedPerm(u, server, world, new TimedValue(permission, start, dur));
        return true;
    }

    /**
     * Removes the given timed permission to the player
     *
     * @param nameoruuid the username or uuid of the player
     * @param permission the permission
     * @param server the server; may be null; if server == "" then the current server is used or null if BungeeCord
     * @param world the world; may be null
     * @return true if the permission has been removed successfully
     */
    public static boolean userTimedRemove(String nameoruuid, String permission, String server, String world)
    {
        server = server(server);
        world = world(server, world);
        permission = Statics.toLower(permission);
        User u = BungeePerms.getInstance().getPermissionsManager().getUser(nameoruuid);
        if (u == null)
            return false;

        BungeePerms.getInstance().getPermissionsManager().removeUserTimedPerm(u, server, world, permission);
        return true;
    }

    /**
     * Adds a timed group to the player
     * @param nameoruuid the username or uuid of the player
     * @param group the group
     * @return true if the group has been added successfully
     */
    public static boolean userAddGroup(String nameoruuid, String group)
    {
        User u = BungeePerms.getInstance().getPermissionsManager().getUser(nameoruuid);
        if (u == null)
            return false;

        Group g = BungeePerms.getInstance().getPermissionsManager().getGroup(group);
        if (g == null)
            return false;

        if (u.getGroups().contains(g))
            return false;

        BungeePerms.getInstance().getPermissionsManager().addUserGroup(u, g);
        return true;
    }

    /**
     * Removes a group from the player
     * @param nameoruuid the username or uuid of the player
     * @param group the group
     * @return true if the group has been removed successfully
     */
    public static boolean userRemoveGroup(String nameoruuid, String group)
    {
        User u = BungeePerms.getInstance().getPermissionsManager().getUser(nameoruuid);
        if (u == null)
            return false;

        Group g = BungeePerms.getInstance().getPermissionsManager().getGroup(group);
        if (g == null)
            return false;

        if (!u.getGroups().contains(g))
            return false;

        BungeePerms.getInstance().getPermissionsManager().removeUserGroup(u, g);
        return true;
    }

    /**
     * Adds a timed group to the player
     * @param nameoruuid the username or uuid of the player
     * @param group the group
     * @param start when the group takes effect
     * @param dur the duration in seconds the group is active
     * @return true if the group has been added successfully
     */
    public static boolean userAddTimedGroup(String nameoruuid, String group, Date start, int dur)
    {
        User u = BungeePerms.getInstance().getPermissionsManager().getUser(nameoruuid);
        if (u == null)
            return false;

        Group g = BungeePerms.getInstance().getPermissionsManager().getGroup(group);
        if (g == null)
            return false;

        for (TimedValue<String> ti : u.getTimedGroupsString())
            if (ti.getValue().equalsIgnoreCase(group))
                return false;

        BungeePerms.getInstance().getPermissionsManager().addUserTimedGroup(u, new TimedValue(g, start, dur));
        return true;
    }

    /**
     * Removes a timed group from the player
     * @param nameoruuid the username or uuid of the player
     * @param group the group
     * @return true if the group has been removed successfully
     */
    public static boolean userRemoveTimedGroup(String nameoruuid, String group)
    {
        User u = BungeePerms.getInstance().getPermissionsManager().getUser(nameoruuid);
        if (u == null)
            return false;

        Group g = BungeePerms.getInstance().getPermissionsManager().getGroup(group);
        if (g == null)
            return false;

        boolean found = false;
        for (TimedValue<String> ti : u.getTimedGroupsString())
            if (ti.getValue().equalsIgnoreCase(group))
            {
                found = true;
                break;
            }

        if (!found)
            return false;

        BungeePerms.getInstance().getPermissionsManager().removeUserTimedGroup(u, g);
        return true;
    }

    /**
     * Gets the full prefix of the player
     *
     * @param nameoruuid the username or uuid of the player
     * @param server the server; may be null; if server == "" then the current server is used or null if BungeeCord
     * @param world the world; may be null
     * @return the full prefix; may be null
     */
    public static String userPrefix(String nameoruuid, String server, String world)
    {
        server = server(server);
        world = world(server, world);

        User u = BungeePerms.getInstance().getPermissionsManager().getUser(nameoruuid);
        if (u == null)
            return null;

        return u.buildSuffix(server, world);
    }

    /**
     * Gets the full suffix of the player
     *
     * @param nameoruuid the username or uuid of the player
     * @param server the server; may be null; if server == "" then the current server is used or null if BungeeCord
     * @param world the world; may be null
     * @return the full suffix; may be null
     */
    public static String userSuffix(String nameoruuid, String server, String world)
    {
        server = server(server);
        world = world(server, world);

        User u = BungeePerms.getInstance().getPermissionsManager().getUser(nameoruuid);
        if (u == null)
            return null;

        return u.buildSuffix(server, world);
    }

    //misc
    /**
     * Whether or not super perms compat is enabled
     *
     * @return whether or not super perms compat is enabled
     */
    public static boolean hasSuperPermsCompat()
    {
        BPConfig config = BungeePerms.getInstance().getConfig();
        return config instanceof BukkitConfig && ((BukkitConfig) config).isSuperpermscompat();
    }

    //uti
    private static String server(String server)
    {
        if (server.equals(""))
        {
            if (BungeePerms.getInstance().getPlugin().getPlatformType() == PlatformType.Bukkit)
                server = Statics.toLower(((BukkitConfig) BungeePerms.getInstance().getConfig()).getServername());
            else
                server = null;
        }
        return server;
    }

    private static String world(String server, String world)
    {
        world = server == null ? null : Statics.toLower(world);
        return world;
    }
}
