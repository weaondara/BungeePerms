package net.alpenblock.bungeeperms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import net.alpenblock.bungeeperms.io.BackEnd;
import net.alpenblock.bungeeperms.io.BackEndType;
import net.alpenblock.bungeeperms.io.MySQL2BackEnd;
import net.alpenblock.bungeeperms.io.MySQLBackEnd;
import net.alpenblock.bungeeperms.io.MySQLUUIDPlayerDB;
import net.alpenblock.bungeeperms.io.NoneUUIDPlayerDB;
import net.alpenblock.bungeeperms.io.UUIDPlayerDB;
import net.alpenblock.bungeeperms.io.UUIDPlayerDBType;
import net.alpenblock.bungeeperms.io.YAMLBackEnd;
import net.alpenblock.bungeeperms.io.YAMLUUIDPlayerDB;
import net.alpenblock.bungeeperms.io.migrate.Migrate2MySQL;
import net.alpenblock.bungeeperms.io.migrate.Migrate2MySQL2;
import net.alpenblock.bungeeperms.io.migrate.Migrate2YAML;
import net.alpenblock.bungeeperms.io.migrate.Migrator;
import net.alpenblock.bungeeperms.platform.PlatformPlugin;
import net.alpenblock.bungeeperms.platform.Sender;
import net.alpenblock.bungeeperms.util.ConcurrentList;

public class PermissionsManager
{

    private final PlatformPlugin plugin;
    private final BPConfig config;
    private final Debug debug;
    private boolean enabled = false;

    @Getter
    @Setter
    private BackEnd backEnd;
    @Getter
    private UUIDPlayerDB UUIDPlayerDB;

    private List<Group> groups;
    private List<User> users;
    private int permsversion;

    public PermissionsManager(PlatformPlugin p, BPConfig conf, Debug d)
    {
        plugin = p;
        config = conf;
        debug = d;

        //config
        loadConfig();

        //perms
        loadPerms();
    }

    /**
     * Loads the configuration of the plugin from the config.yml file.
     */
    public final void loadConfig()
    {
        BackEndType bet = config.getBackEndType();
        if (bet == BackEndType.YAML)
        {
            backEnd = new YAMLBackEnd();
        }
        else if (bet == BackEndType.MySQL)
        {
            backEnd = new MySQLBackEnd();
        }
        else if (bet == BackEndType.MySQL2)
        {
            backEnd = new MySQL2BackEnd();
        }

        UUIDPlayerDBType updbt = config.getUUIDPlayerDBType();
        if (updbt == UUIDPlayerDBType.None)
        {
            UUIDPlayerDB = new NoneUUIDPlayerDB();
        }
        else if (updbt == UUIDPlayerDBType.YAML)
        {
            UUIDPlayerDB = new YAMLUUIDPlayerDB();
        }
        else if (updbt == UUIDPlayerDBType.MySQL)
        {
            UUIDPlayerDB = new MySQLUUIDPlayerDB();
        }
    }

    /**
     * (Re)loads the all groups and online players from file/table.
     */
    public final void loadPerms()
    {
        BungeePerms.getLogger().info("loading permissions ...");

        //load database
        backEnd.load();

        //load all groups
        groups = backEnd.loadGroups();

        users = new ConcurrentList<>();

        //load permsversion
        permsversion = backEnd.loadVersion();

        BungeePerms.getLogger().info("permissions loaded");
    }

    /**
     * Enables the permissions manager.
     */
    public void enable()
    {
        if (enabled)
        {
            return;
        }

        //load online players; allows reload
        for (Sender s : BungeePerms.getInstance().getPlugin().getPlayers())
        {
            if (config.isUseUUIDs())
            {
                getUser(s.getUUID());
            }
            else
            {
                getUser(s.getName());
            }
        }

        enabled = true;
    }

    /**
     * Disables the permissions manager.
     */
    public void disable()
    {
        if (!enabled)
        {
            return;
        }
        enabled = false;
    }

    public void reload()
    {
        disable();

        //config
        loadConfig();

        //perms
        loadPerms();

        enable();
    }

    /**
     * Validates all loaded groups and users and fixes invalid objects.
     */
    public synchronized void validateUsersGroups()
    {
        //group check - remove inheritances
        for (int i = 0; i < groups.size(); i++)
        {
            Group group = groups.get(i);
            List<String> inheritances = group.getInheritances();
            for (int j = 0; j < inheritances.size(); j++)
            {
                if (getGroup(inheritances.get(j)) == null)
                {
                    inheritances.remove(j);
                    j--;
                }
            }
            backEnd.saveGroupInheritances(group);
        }
        //perms recalc and bukkit perms update
        for (Group g : groups)
        {
            g.recalcPerms();

            //send bukkit update info
            BungeePerms.getInstance().getNetworkNotifier().reloadGroup(g);
        }

        //user check
        for (int i = 0; i < users.size(); i++)
        {
            User u = users.get(i);
            for (int j = 0; j < u.getGroups().size(); j++)
            {
                if (getGroup(u.getGroups().get(j).getName()) == null)
                {
                    u.getGroups().remove(j);
                    j--;
                }
            }
            backEnd.saveUserGroups(u);
        }

        //perms recalc and bukkit perms update
        for (User u : users)
        {
            u.recalcPerms();

            //send bukkit update info
            BungeePerms.getInstance().getNetworkNotifier().reloadUser(u);
        }

        //user groups check - backEnd
        List<User> backendusers = backEnd.loadUsers();
        for (int i = 0; i < backendusers.size(); i++)
        {
            User u = backendusers.get(i);
            for (int j = 0; j < u.getGroups().size(); j++)
            {
                if (getGroup(u.getGroups().get(j).getName()) == null)
                {
                    u.getGroups().remove(j);
                    j--;
                }
            }
            backEnd.saveUserGroups(u);
        }
    }

    /**
     * Get the group of the player with the highesst rank. Do not to be confused with the rank property. The higher the rank the smaller the rank property. (1 is highest rank; 1000 is a low rank)
     *
     * @param player the user to get the main group of
     * @return the main group of the user (highest rank)
     * @throws NullPointerException if player is null
     */
    public synchronized Group getMainGroup(User player)
    {
        if (player == null)
        {
            throw new NullPointerException("player is null");
        }
        if (player.getGroups().isEmpty())
        {
            return null;
        }
        Group ret = player.getGroups().get(0);
        for (int i = 1; i < player.getGroups().size(); i++)
        {
            if (player.getGroups().get(i).getWeight() < ret.getWeight())
            {
                ret = player.getGroups().get(i);
            }
        }
        return ret;
    }

    /**
     * Gets the next (higher) group in the same ladder.
     *
     * @param group the group to get the next group of
     * @return the next group in the same ladder or null if the group has no next group
     * @throws IllegalArgumentException if the group ladder does not exist (anymore)
     */
    public synchronized Group getNextGroup(Group group)
    {
        List<Group> laddergroups = getLadderGroups(group.getLadder());

        for (int i = 0; i < laddergroups.size(); i++)
        {
            if (laddergroups.get(i).getRank() == group.getRank())
            {
                if (i + 1 < laddergroups.size())
                {
                    return laddergroups.get(i + 1);
                }
                else
                {
                    return null;
                }
            }
        }
        throw new IllegalArgumentException("group ladder does not exist (anymore)");
    }

    /**
     * Gets the previous (lower) group in the same ladder.
     *
     * @param group the group to get the previous group of
     * @return the previous group in the same ladder or null if the group has no previous group
     * @throws IllegalArgumentException if the group ladder does not exist (anymore)
     */
    public synchronized Group getPreviousGroup(Group group)
    {
        List<Group> laddergroups = getLadderGroups(group.getLadder());

        for (int i = 0; i < laddergroups.size(); i++)
        {
            if (laddergroups.get(i).getRank() == group.getRank())
            {
                if (i > 0)
                {
                    return laddergroups.get(i - 1);
                }
                else
                {
                    return null;
                }
            }
        }
        throw new IllegalArgumentException("group ladder does not exist (anymore)");
    }

    /**
     * Gets all groups of the given ladder.
     *
     * @param ladder the ladder of the groups to get
     * @return a sorted list of all matched groups
     */
    public synchronized List<Group> getLadderGroups(String ladder)
    {
        List<Group> ret = new ArrayList<>();

        for (Group g : groups)
        {
            if (g.getLadder().equalsIgnoreCase(ladder))
            {
                ret.add(g);
            }
        }

        Collections.sort(ret);

        return ret;
    }

    /**
     * Gets a list of all existing ladders.
     *
     * @return a list of all ladders
     */
    public synchronized List<String> getLadders()
    {
        List<String> ret = new ArrayList<>();

        for (Group g : groups)
        {
            if (!ret.contains(g.getLadder()))
            {
                ret.add(g.getLadder());
            }
        }

        return ret;
    }

    /**
     * Gets a list of all groups that are marked as default and given to all users by default.
     *
     * @return a list of default groups
     */
    public synchronized List<Group> getDefaultGroups()
    {
        List<Group> ret = new ArrayList<>();
        for (Group g : groups)
        {
            if (g.isDefault())
            {
                ret.add(g);
            }
        }
        return ret;
    }

    /**
     * Gets a group by its name.
     *
     * @param groupname the name of the group to get
     * @return the found group if any or null
     */
    public synchronized Group getGroup(String groupname)
    {
        for (Group g : groups)
        {
            if (g.getName().equalsIgnoreCase(groupname))
            {
                return g;
            }
        }
        return null;
    }

    /**
     * Gets a user by its name. If the user is not loaded it will be loaded.
     *
     * @param usernameoruuid the name or the UUID of the user to get
     * @return the found user or null if it does not exist
     */
    public synchronized User getUser(String usernameoruuid)
    {
        UUID uuid = Statics.parseUUID(usernameoruuid);
        if (config.isUseUUIDs())
        {
            if (uuid != null)
            {
                return getUser(uuid);
            }
        }

        for (User u : users)
        {
            if (u.getName().equalsIgnoreCase(usernameoruuid))
            {
                return u;
            }
        }

        //load user from database
        User u = null;
        if (config.isUseUUIDs())
        {
            if (uuid == null)
            {
                uuid = UUIDPlayerDB.getUUID(usernameoruuid);
            }
            if (uuid != null)
            {
                u = backEnd.loadUser(uuid);
            }
        }
        else
        {
            u = backEnd.loadUser(usernameoruuid);
        }
        if (u != null)
        {
            users.add(u);
            return u;
        }

        return null;
    }

    /**
     * Gets a user by its UUID. If the user is not loaded it will be loaded.
     *
     * @param uuid the uuid of the user to get
     * @return the found user or null if it does not exist
     */
    public synchronized User getUser(UUID uuid)
    {
        for (User u : users)
        {
            if (u.getUUID().equals(uuid))
            {
                return u;
            }
        }

        //load user from database
        User u = backEnd.loadUser(uuid);
        if (u != null)
        {
            users.add(u);
            return u;
        }

        return null;
    }

    /**
     * Gets an unmodifiable list of all groups
     *
     * @return an unmodifiable list of all groups
     */
    public List<Group> getGroups()
    {
        return Collections.unmodifiableList(groups);
    }

    /**
     * Gets an unmodifiable list of all loaded users
     *
     * @return an unmodifiable list of all loaded users
     */
    public List<User> getUsers()
    {
        return Collections.unmodifiableList(users);
    }

    /**
     * Gets a list of all users
     *
     * @return a list of all users
     */
    public List<String> getRegisteredUsers()
    {
        return backEnd.getRegisteredUsers();
    }

    public List<String> getGroupUsers(Group group)
    {
        return backEnd.getGroupUsers(group);
    }

    /**
     * Deletes a user from cache and database.
     *
     * @param user the user to delete
     */
    public synchronized void deleteUser(User user)
    {
        //cache
        users.remove(user);

        //database
        backEnd.deleteUser(user);

        //send bukkit update infoif(useUUIDs)
        BungeePerms.getInstance().getNetworkNotifier().deleteUser(user);
    }

    /**
     * Deletes a user from cache and database and validates all groups and users.
     *
     * @param group the group the remove
     */
    public synchronized void deleteGroup(Group group)
    {
        //cache
        groups.remove(group);

        //database
        backEnd.deleteGroup(group);

        //group validation
        validateUsersGroups();

        //send bukkit update info
        BungeePerms.getInstance().getNetworkNotifier().deleteGroup(group);
    }

    /**
     * Adds a user to cache and database.
     *
     * @param user the user to add
     */
    public synchronized void addUser(User user)
    {
        //cache
        users.add(user);

        //database
        backEnd.saveUser(user, true);

        //send bukkit update info
        BungeePerms.getInstance().getNetworkNotifier().reloadUser(user);
    }

    /**
     * Adds a group to cache and database.
     *
     * @param group the group to add
     */
    public synchronized void addGroup(Group group)
    {
        //cache
        groups.add(group);
        Collections.sort(groups);

        //database
        backEnd.saveGroup(group, true);

        //send bukkit update info
        BungeePerms.getInstance().getNetworkNotifier().reloadGroup(group);
    }

    //database and permission operations
    /**
     * Formats the permissions backEnd.
     */
    public void format()
    {
        backEnd.format(backEnd.loadGroups(), backEnd.loadUsers(), permsversion);
        backEnd.load();
        BungeePerms.getInstance().getNetworkNotifier().reloadAll();
    }

    /**
     * Cleans the permissions backEnd and wipes 0815 users.
     *
     * @return the number of deleted users
     */
    public int cleanup()
    {
        int res = backEnd.cleanup(backEnd.loadGroups(), backEnd.loadUsers(), permsversion);
        backEnd.load();
        BungeePerms.getInstance().getNetworkNotifier().reloadAll();
        return res;
    }

    /**
     * Adds the given group to the user.
     *
     * @param user the user to add the group to
     * @param group the group to add to the user
     */
    public void addUserGroup(User user, Group group)
    {
        //cache
        user.getGroups().add(group);
        Collections.sort(user.getGroups());

        //database
        backEnd.saveUserGroups(user);

        //recalc perms
        user.recalcPerms();

        //send bukkit update info
        BungeePerms.getInstance().getNetworkNotifier().reloadUser(user);
    }

    /**
     * Removes the given group from the user.
     *
     * @param user the user to remove the group from
     * @param group the group to remove from the user
     */
    public void removeUserGroup(User user, Group group)
    {
        //cache
        user.getGroups().remove(group);
        Collections.sort(user.getGroups());

        //database
        backEnd.saveUserGroups(user);

        //recalc perms
        user.recalcPerms();

        //send bukkit update info
        BungeePerms.getInstance().getNetworkNotifier().reloadUser(user);
    }

    /**
     * Adds a permission to the user.
     *
     * @param user the user to add the permission to
     * @param perm the permission to add to the user
     */
    public void addUserPerm(User user, String perm)
    {
        //cache
        user.getExtraPerms().add(perm);

        //database
        backEnd.saveUserPerms(user);

        //recalc perms
        user.recalcPerms();

        //send bukkit update info
        BungeePerms.getInstance().getNetworkNotifier().reloadUser(user);
    }

    /**
     * Removes a permission from the user.
     *
     * @param user the user to remove the permission from
     * @param perm the permission to remove from the user
     */
    public void removeUserPerm(User user, String perm)
    {
        //cache
        user.getExtraPerms().remove(perm);

        //database
        backEnd.saveUserPerms(user);

        //recalc perms
        user.recalcPerms();

        //send bukkit update info
        BungeePerms.getInstance().getNetworkNotifier().reloadUser(user);
    }

    /**
     * Adds a permission to the user on the given server.
     *
     * @param user the user to add the permission to
     * @param server the server to add the permission on
     * @param perm the permission to add to the user
     */
    public void addUserPerServerPerm(User user, String server, String perm)
    {
        //cache
        List<String> perserverperms = user.getServerPerms().get(server);
        if (perserverperms == null)
        {
            perserverperms = new ArrayList<>();
        }

        perserverperms.add(perm);
        user.getServerPerms().put(server, perserverperms);

        //database
        backEnd.saveUserPerServerPerms(user, server);

        //recalc perms
        user.recalcPerms(server);

        //send bukkit update info
        BungeePerms.getInstance().getNetworkNotifier().reloadUser(user);
    }

    /**
     * Removes a permission from the user on the given server.
     *
     * @param user the user to remove the permission from
     * @param server the server to remove the permission from
     * @param perm the permission to remove from the user
     */
    public void removeUserPerServerPerm(User user, String server, String perm)
    {
        //cache
        List<String> perserverperms = user.getServerPerms().get(server);
        if (perserverperms == null)
        {
            perserverperms = new ArrayList<>();
        }

        perserverperms.remove(perm);
        user.getServerPerms().put(server, perserverperms);

        //database
        backEnd.saveUserPerServerPerms(user, server);

        //recalc perms
        user.recalcPerms(server);

        //send bukkit update info
        BungeePerms.getInstance().getNetworkNotifier().reloadUser(user);
    }

    /**
     * Adds a permission to the user on the given server in the given world.
     *
     * @param user the user to add the permission to
     * @param server the server to add the permission on
     * @param world the world to add the permission in
     * @param perm the permission to add to the user
     */
    public void addUserPerServerWorldPerm(User user, String server, String world, String perm)
    {
        //cache
        Map<String, List<String>> perserverperms = user.getServerWorldPerms().get(server);
        if (perserverperms == null)
        {
            perserverperms = new HashMap<>();
        }

        List<String> perserverworldperms = perserverperms.get(world);
        if (perserverworldperms == null)
        {
            perserverworldperms = new ArrayList<>();
        }

        perserverworldperms.add(perm);
        perserverperms.put(world, perserverworldperms);
        user.getServerWorldPerms().put(server, perserverperms);

        //database
        backEnd.saveUserPerServerWorldPerms(user, server, world);

        //recalc perms
        user.recalcPerms(server, world);

        //send bukkit update info
        BungeePerms.getInstance().getNetworkNotifier().reloadUser(user);
    }

    /**
     * Removes a permission from the user on the given server.
     *
     * @param user the user to remove the permission from
     * @param server the server to remove the permission from
     * @param world the world to remove the permission from
     * @param perm the permission to remove from the user
     */
    public void removeUserPerServerWorldPerm(User user, String server, String world, String perm)
    {
        //cache
        Map<String, List<String>> perserverperms = user.getServerWorldPerms().get(server);
        if (perserverperms == null)
        {
            perserverperms = new HashMap<>();
        }

        List<String> perserverworldperms = perserverperms.get(world);
        if (perserverworldperms == null)
        {
            perserverworldperms = new ArrayList<>();
        }

        perserverworldperms.remove(perm);
        perserverperms.put(world, perserverworldperms);
        user.getServerWorldPerms().put(server, perserverperms);

        //database
        backEnd.saveUserPerServerWorldPerms(user, server, world);

        //recalc perms
        user.recalcPerms(server, world);

        //send bukkit update info
        BungeePerms.getInstance().getNetworkNotifier().reloadUser(user);
    }

    public void addGroupPerm(Group group, String perm)
    {
        //cache
        group.getPerms().add(perm);

        //database
        backEnd.saveGroupPerms(group);

        //recalc perms
        for (Group g : groups)
        {
            g.recalcPerms();
        }
        for (User u : users)
        {
            u.recalcPerms();
        }

        //send bukkit update info
        BungeePerms.getInstance().getNetworkNotifier().reloadGroup(group);
    }

    public void removeGroupPerm(Group group, String perm)
    {
        //cache
        group.getPerms().remove(perm);

        //database
        backEnd.saveGroupPerms(group);

        //recalc perms
        for (Group g : groups)
        {
            g.recalcPerms();
        }
        for (User u : users)
        {
            u.recalcPerms();
        }

        //send bukkit update info
        BungeePerms.getInstance().getNetworkNotifier().reloadGroup(group);
    }

    public void addGroupPerServerPerm(Group group, String server, String perm)
    {
        //cache
        Server srv = group.getServers().get(server);
        if (srv == null)
        {
            srv = new Server(server, new ArrayList<String>(), new HashMap<String, World>(), "", "", "");
        }

        srv.getPerms().add(perm);

        group.getServers().put(server, srv);

        //database
        backEnd.saveGroupPerServerPerms(group, server);

        //recalc perms
        for (Group g : groups)
        {
            g.recalcPerms(server);
        }
        for (User u : users)
        {
            u.recalcPerms();
        }

        //send bukkit update info
        BungeePerms.getInstance().getNetworkNotifier().reloadGroup(group);
    }

    public void removeGroupPerServerPerm(Group group, String server, String perm)
    {
        //cache
        Server srv = group.getServers().get(server);
        if (srv == null)
        {
            srv = new Server(server, new ArrayList<String>(), new HashMap<String, World>(), "", "", "");
        }

        srv.getPerms().remove(perm);

        group.getServers().put(server, srv);

        //database
        backEnd.saveGroupPerServerPerms(group, server);

        //recalc perms
        for (Group g : groups)
        {
            g.recalcPerms(server);
        }
        for (User u : users)
        {
            u.recalcPerms();
        }

        //send bukkit update info
        BungeePerms.getInstance().getNetworkNotifier().reloadGroup(group);
    }

    public void addGroupPerServerWorldPerm(Group group, String server, String world, String perm)
    {
        //cache
        Server srv = group.getServers().get(server);
        if (srv == null)
        {
            srv = new Server(server, new ArrayList<String>(), new HashMap<String, World>(), "", "", "");
        }

        World w = srv.getWorlds().get(world);
        if (w == null)
        {
            w = new World(world, new ArrayList<String>(), "", "", "");
        }

        w.getPerms().add(perm);
        srv.getWorlds().put(world, w);
        group.getServers().put(server, srv);

        //database
        backEnd.saveGroupPerServerWorldPerms(group, server, world);

        //recalc perms
        for (Group g : groups)
        {
            g.recalcPerms(server, world);
        }
        for (User u : users)
        {
            u.recalcPerms();
        }

        //send bukkit update info
        BungeePerms.getInstance().getNetworkNotifier().reloadGroup(group);
    }

    public void removeGroupPerServerWorldPerm(Group group, String server, String world, String perm)
    {
        //cache
        Server srv = group.getServers().get(server);
        if (srv == null)
        {
            srv = new Server(server, new ArrayList<String>(), new HashMap<String, World>(), "", "", "");
        }

        World w = srv.getWorlds().get(world);
        if (w == null)
        {
            w = new World(world, new ArrayList<String>(), "", "", "");
        }

        w.getPerms().remove(perm);
        srv.getWorlds().put(world, w);
        group.getServers().put(server, srv);

        //database
        backEnd.saveGroupPerServerWorldPerms(group, server, world);

        //recalc perms
        for (Group g : groups)
        {
            g.recalcPerms(server, world);
        }
        for (User u : users)
        {
            u.recalcPerms();
        }

        //send bukkit update info
        BungeePerms.getInstance().getNetworkNotifier().reloadGroup(group);
    }

    public void addGroupInheritance(Group group, Group toadd)
    {
        //cache
        group.getInheritances().add(toadd.getName());
        Collections.sort(group.getInheritances());

        //database
        backEnd.saveGroupInheritances(group);

        //recalc perms
        for (Group g : groups)
        {
            g.recalcPerms();
        }
        for (User u : users)
        {
            u.recalcPerms();
        }

        //send bukkit update info
        BungeePerms.getInstance().getNetworkNotifier().reloadGroup(group);
    }

    public void removeGroupInheritance(Group group, Group toremove)
    {
        //cache
        group.getInheritances().remove(toremove.getName());
        Collections.sort(group.getInheritances());

        //database
        backEnd.saveGroupInheritances(group);

        //recalc perms
        for (Group g : groups)
        {
            g.recalcPerms();
        }
        for (User u : users)
        {
            u.recalcPerms();
        }

        //send bukkit update info
        BungeePerms.getInstance().getNetworkNotifier().reloadGroup(group);
    }

    /**
     * Set the ladder for the group.
     *
     * @param group
     * @param ladder
     */
    public void ladderGroup(Group group, String ladder)
    {
        //cache
        group.setLadder(ladder);

        //database
        backEnd.saveGroupLadder(group);

        //send bukkit update info
        BungeePerms.getInstance().getNetworkNotifier().reloadGroup(group);
    }

    /**
     * Sets the rank for the group.
     *
     * @param group
     * @param rank
     */
    public void rankGroup(Group group, int rank)
    {
        //cache
        group.setRank(rank);
        Collections.sort(groups);

        //database
        backEnd.saveGroupRank(group);

        //send bukkit update info
        BungeePerms.getInstance().getNetworkNotifier().reloadGroup(group);
    }

    /**
     * Sets the weight for the group.
     *
     * @param group
     * @param weight
     */
    public void weightGroup(Group group, int weight)
    {
        //cache
        group.setWeight(weight);
        Collections.sort(groups);

        //database
        backEnd.saveGroupWeight(group);

        //send bukkit update info
        BungeePerms.getInstance().getNetworkNotifier().reloadGroup(group);
    }

    /**
     * Sets if the the group is a default group.
     *
     * @param group
     * @param isdefault
     */
    public void setGroupDefault(Group group, boolean isdefault)
    {
        //cache
        group.setIsdefault(isdefault);

        //database
        backEnd.saveGroupDefault(group);

        //send bukkit update info
        BungeePerms.getInstance().getNetworkNotifier().reloadGroup(group);
    }

    /**
     * Sets the displayname of the group
     *
     * @param group
     * @param display
     * @param server
     * @param world
     */
    public void setGroupDisplay(Group group, String display, String server, String world)
    {
        //cache
        group.setDisplay(display);

        //database
        backEnd.saveGroupDisplay(group, server, world);

        //send bukkit update info
        BungeePerms.getInstance().getNetworkNotifier().reloadGroup(group);
    }

    /**
     * Sets the prefix for the group.
     *
     * @param group
     * @param prefix
     * @param server
     * @param world
     */
    public void setGroupPrefix(Group group, String prefix, String server, String world)
    {
        //cache
        group.setPrefix(prefix);

        //database
        backEnd.saveGroupPrefix(group, server, world);

        //send bukkit update info
        BungeePerms.getInstance().getNetworkNotifier().reloadGroup(group);
    }

    /**
     * Sets the suffix for the group.
     *
     * @param group
     * @param suffix
     * @param server
     * @param world
     */
    public void setGroupSuffix(Group group, String suffix, String server, String world)
    {
        //cache
        group.setSuffix(suffix);

        //database
        backEnd.saveGroupSuffix(group, server, world);

        //send bukkit update info
        BungeePerms.getInstance().getNetworkNotifier().reloadGroup(group);
    }

    /**
     * Migrates the permissions to the given backnd type.
     *
     * @param bet the backEnd type to migrate to
     */
    public synchronized void migrateBackEnd(BackEndType bet)
    {
        if (bet == null)
        {
            throw new NullPointerException("bet must not be null");
        }
        Migrator migrator = null;
        if (bet == BackEndType.MySQL2)
        {
            migrator = new Migrate2MySQL2(config, debug);
        }
        else if (bet == BackEndType.MySQL)
        {
            migrator = new Migrate2MySQL(config, debug);
        }
        else if (bet == BackEndType.YAML)
        {
            migrator = new Migrate2YAML(config);
        }

        if (migrator == null)
        {
            throw new UnsupportedOperationException("bet==" + bet.name());
        }

        migrator.migrate(backEnd.loadGroups(), backEnd.loadUsers(), permsversion);

        backEnd.load();
    }

    public void migrateUseUUID(Map<String, UUID> uuids)
    {
        List<Group> groups = backEnd.loadGroups();
        List<User> users = backEnd.loadUsers();
        int version = backEnd.loadVersion();
        BungeePerms.getInstance().getConfig().setUseUUIDs(true);

        backEnd.clearDatabase();
        for (Group g : groups)
        {
            backEnd.saveGroup(g, false);
        }
        for (User u : users)
        {
            UUID uuid = uuids.get(u.getName());
            if (uuid != null)
            {
                u.setUUID(uuid);
                backEnd.saveUser(u, false);
            }
        }
        backEnd.saveVersion(version, true);
    }

    public void migrateUsePlayerNames(Map<UUID, String> playernames)
    {
        List<Group> groups = backEnd.loadGroups();
        List<User> users = backEnd.loadUsers();
        int version = backEnd.loadVersion();
        BungeePerms.getInstance().getConfig().setUseUUIDs(false);

        backEnd.clearDatabase();
        for (Group g : groups)
        {
            backEnd.saveGroup(g, false);
        }
        for (User u : users)
        {
            String playername = playernames.get(u.getUUID());
            if (playername != null)
            {
                u.setName(playername);
                backEnd.saveUser(u, false);
            }
        }
        backEnd.saveVersion(version, true);
    }

    public void migrateUUIDPlayerDB(UUIDPlayerDBType type)
    {
        Map<UUID, String> map = UUIDPlayerDB.getAll();

        if (type == UUIDPlayerDBType.None)
        {
            UUIDPlayerDB = new NoneUUIDPlayerDB();
        }
        else if (type == UUIDPlayerDBType.YAML)
        {
            UUIDPlayerDB = new YAMLUUIDPlayerDB();
        }
        else if (type == UUIDPlayerDBType.MySQL)
        {
            UUIDPlayerDB = new MySQLUUIDPlayerDB();
        }
        else
        {
            throw new UnsupportedOperationException("type==" + type);
        }
        BungeePerms.getInstance().getConfig().setUUIDPlayerDB(UUIDPlayerDB.getType());
        UUIDPlayerDB.clear();

        for (Map.Entry<UUID, String> e : map.entrySet())
        {
            UUIDPlayerDB.update(e.getKey(), e.getValue());
        }
    }

//internal functions
    public void reloadUser(String user)
    {
        User u = getUser(user);
        if (u == null)
        {
            debug.log("User " + user + " not found!!!");
            return;
        }
        backEnd.reloadUser(u);
        u.recalcPerms();
    }

    public void reloadUser(UUID uuid)
    {
        User u = getUser(uuid);
        if (u == null)
        {
            debug.log("User " + uuid + " not found!!!");
            return;
        }
        backEnd.reloadUser(u);
        u.recalcPerms();
    }

    public void reloadGroup(String group)
    {
        Group g = getGroup(group);
        if (g == null)
        {
            debug.log("Group " + group + " not found!!!");
            return;
        }
        backEnd.reloadGroup(g);
        Collections.sort(groups);
        for (Group gr : groups)
        {
            gr.recalcPerms();
        }
        for (User u : users)
        {
            u.recalcPerms();
        }
    }

    public void reloadUsers()
    {
        for (User u : users)
        {
            backEnd.reloadUser(u);
            u.recalcPerms();
        }
    }

    public void reloadGroups()
    {
        for (Group g : groups)
        {
            backEnd.reloadGroup(g);
        }
        Collections.sort(groups);
        for (Group g : groups)
        {
            g.recalcPerms();
        }
        for (User u : users)
        {
            u.recalcPerms();
        }
    }

    public void addUserToCache(User u)
    {
        users.add(u);
    }

    public void removeUserFromCache(User u)
    {
        users.remove(u);
    }

    public void addGroupToCache(Group g)
    {
        groups.add(g);
    }

    public void removeGroupFromCache(Group g)
    {
        groups.remove(g);
    }
}
