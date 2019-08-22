package net.alpenblock.bungeeperms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.alpenblock.bungeeperms.Lang.MessageType;
import net.alpenblock.bungeeperms.io.BackEnd;
import net.alpenblock.bungeeperms.io.BackEndType;
import net.alpenblock.bungeeperms.io.MySQLBackEnd;
import net.alpenblock.bungeeperms.io.MySQLUUIDPlayerDB;
import net.alpenblock.bungeeperms.io.UUIDPlayerDB;
import net.alpenblock.bungeeperms.io.YAMLBackEnd;
import net.alpenblock.bungeeperms.io.YAMLUUIDPlayerDB;
import net.alpenblock.bungeeperms.io.migrate.Migrate2MySQL;
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
    @Setter
    private UUIDPlayerDB UUIDPlayerDB;

    private List<Group> groups;
    private List<User> users;
    private int permsversion;

    private final ReadWriteLock grouplock = new ReentrantReadWriteLock();
    @Getter(value = AccessLevel.PACKAGE) //for cleanup
    private final ReadWriteLock userlock = new ReentrantReadWriteLock();

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
        config.load();
        BackEndType bet = config.getBackendType();
        switch (bet)
        {
            case YAML:
                backEnd = new YAMLBackEnd();
                if (config.isUseUUIDs())
                    UUIDPlayerDB = new YAMLUUIDPlayerDB();
                break;
            case MySQL:
                backEnd = new MySQLBackEnd();
                if (config.isUseUUIDs())
                    UUIDPlayerDB = new MySQLUUIDPlayerDB();
                break;
            default:
                throw new IllegalStateException("This should not have happened!");
        }
    }

    /**
     * (Re)loads the all groups and online players from file/table.
     */
    public final void loadPerms()
    {
        BungeePerms.getLogger().info(Lang.translate(MessageType.PERMISSIONS_LOADING));

        //load database
        backEnd.load();

        grouplock.writeLock().lock();
        try
        {
            groups = backEnd.loadGroups();
        }
        finally
        {
            grouplock.writeLock().unlock();
        }

        userlock.writeLock().lock();
        try
        {
            users = new ConcurrentList<>();
        }
        finally
        {
            userlock.writeLock().unlock();
        }

        //load permsversion
        permsversion = backEnd.loadVersion();

        BungeePerms.getLogger().info(Lang.translate(MessageType.PERMISSIONS_LOADED));
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
            User user;
            if (config.isUseUUIDs())
            {
                user = getUser(s.getUUID());
                if (user == null)
                {
                    createTempUser(s.getName(), s.getUUID());
                }
            }
            else
            {
                user = getUser(s.getName());
                if (user == null)
                {
                    createTempUser(s.getName(), null);
                }
            }

            //call event
            BungeePerms.getInstance().getEventDispatcher().dispatchUserChangeEvent(user);
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
        userlock.writeLock().lock();
        try
        {
            users.clear();
        }
        finally
        {
            userlock.writeLock().unlock();
        }
        enabled = false;
    }

    /**
     * Reloads the config and permissions.
     */
    public void reload()
    {
        disable();

        //config
        loadConfig();

        //perms
        loadPerms();

        enable();

        //call event
        BungeePerms.getInstance().getEventDispatcher().dispatchReloadedEvent();
    }

    /**
     * Validates all loaded groups and users and fixes invalid objects.
     */
    public synchronized void validateUsersGroups()
    {
        grouplock.readLock().lock();
        try
        {
            for (Group group : groups)
            {
                List<String> inheritances = group.getInheritancesString();
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
            //do this in 2 seperate loops to keep validation clean
            for (Group g : groups)
            {
                g.invalidateCache();

                //send bukkit update info
                BungeePerms.getInstance().getNetworkNotifier().reloadGroup(g, null);
            }
        }
        finally
        {
            grouplock.readLock().unlock();
        }

        userlock.readLock().lock();
        try
        {
            for (User u : users)
            {
                for (int j = 0; j < u.getGroupsString().size(); j++)
                {
                    if (getGroup(u.getGroupsString().get(j)) == null)
                    {
                        u.getGroupsString().remove(j);
                        j--;
                    }
                }
                backEnd.saveUserGroups(u);
            }

            //perms recalc and bukkit perms update
            //do this in 2 seperate loops to keep validation clean
            for (User u : users)
            {
                u.invalidateCache();

                //send bukkit update info
                BungeePerms.getInstance().getNetworkNotifier().reloadUser(u, null);
            }
        }
        finally
        {
            userlock.readLock().unlock();
        }

        //user groups check - backEnd
        List<User> backendusers = backEnd.loadUsers();
        for (User u : backendusers)
        {
            for (int j = 0; j < u.getGroupsString().size(); j++)
            {
                if (getGroup(u.getGroupsString().get(j)) == null)
                {
                    u.getGroupsString().remove(j);
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
            throw new NullPointerException("player is null");
        if (player.getGroupsString().isEmpty())
            return null;

        List<Group> groups = player.getGroups();
        Group ret = groups.get(0);
        for (int i = 1; i < groups.size(); i++)
        {
            if (groups.get(i).getWeight() < ret.getWeight())
            {
                ret = groups.get(i);
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

        grouplock.readLock().lock();
        try
        {
            for (Group g : groups)
            {
                if (g.getLadder().equalsIgnoreCase(ladder))
                {
                    ret.add(g);
                }
            }
        }
        finally
        {
            grouplock.readLock().unlock();
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

        grouplock.readLock().lock();
        try
        {
            for (Group g : groups)
            {
                if (!Statics.listContains(ret, g.getLadder()))
                {
                    ret.add(g.getLadder());
                }
            }
        }
        finally
        {
            grouplock.readLock().unlock();
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
        grouplock.readLock().lock();
        try
        {
            for (Group g : groups)
            {
                if (g.isDefault())
                {
                    ret.add(g);
                }
            }
        }
        finally
        {
            grouplock.readLock().unlock();
        }
        return ret;
    }

    /**
     * Gets a list of all groups that are marked as default and given to all users by default.
     *
     * @return a list of default groups
     */
    public synchronized List<String> getDefaultGroupsString()
    {
        List<String> ret = new ArrayList();
        for (Group g : getDefaultGroups())
            ret.add(g.getName());
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
        if (groupname == null)
        {
            return null;
        }

        grouplock.readLock().lock();
        try
        {
            for (Group g : groups)
                if (g.getName().equalsIgnoreCase(groupname))
                    return g;
        }
        finally
        {
            grouplock.readLock().unlock();
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
        return getUser(usernameoruuid, true);
    }

    /**
     * Gets a user by its name. If the user is not loaded it will be loaded if loadfromdb is true.
     *
     * @param usernameoruuid the name or the UUID of the user to get
     * @param loadfromdb whether or not to load the user from the database if not already loaded
     * @return the found user or null if it does not exist
     */
    public synchronized User getUser(String usernameoruuid, boolean loadfromdb)
    {
        if (usernameoruuid == null)
        {
            return null;
        }

        UUID uuid = Statics.parseUUID(usernameoruuid);
        if (config.isUseUUIDs() && uuid != null)
        {
            return getUser(uuid);
        }

        userlock.readLock().lock();
        try
        {
            for (User u : users)
                if (u.getName().equalsIgnoreCase(usernameoruuid))
                    return u;
        }
        finally
        {
            userlock.readLock().unlock();
        }

        //load user from database
        if (loadfromdb)
        {
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
                addUserToCache(u);
                return u;
            }
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
        return getUser(uuid, true);
    }

    /**
     * Gets a user by its UUID. If the user is not loaded it will be loaded if loadfromdb is true.
     *
     * @param uuid the uuid of the user to get
     * @param loadfromdb whether or not to load the user from the database if not already loaded
     * @return the found user or null if it does not exist
     */
    public synchronized User getUser(UUID uuid, boolean loadfromdb)
    {
        if (uuid == null)
        {
            return null;
        }

        userlock.readLock().lock();
        try
        {
            for (User u : users)
            {
                if (u.getUUID().equals(uuid))
                {
                    // this is java runtime convention ...
                    // finally will always be executed
//                    userlock.readLock().unlock();
                    return u;
                }
            }
        }
        finally
        {
            userlock.readLock().unlock();
        }

        //load user from database
        if (loadfromdb)
        {
            User u = backEnd.loadUser(uuid);
            if (u != null)
            {
                addUserToCache(u);
                return u;
            }
        }

        return null;
    }

    /**
     * Creates a temporary user with the given parameters and adds it to the users cache. The returned user object is no saved to the backend in this function.
     *
     * @param playername the name of the temporary user
     * @param uuid the user of the temporary user
     * @return the created user
     */
    public User createTempUser(String playername, UUID uuid)
    {
        List<String> groups = getDefaultGroupsString();
        User u = new User(playername, uuid, groups, new ArrayList(), new ArrayList(), new ArrayList(), new HashMap(), null, null, null);
        addUserToCache(u);

        return u;
    }

    /**
     * Gets an unmodifiable list of all groups
     *
     * @return an unmodifiable list of all groups
     */
    public List<Group> getGroups()
    {
        List<Group> l;
        grouplock.readLock().lock();
        try
        {
            l = Collections.unmodifiableList(groups);
        }
        finally
        {
            grouplock.readLock().unlock();
        }
        return l;
    }

    /**
     * Gets an unmodifiable list of all loaded users
     *
     * @return an unmodifiable list of all loaded users
     */
    public List<User> getUsers()
    {
        List<User> l;
        userlock.readLock().lock();
        try
        {
            l = Collections.unmodifiableList(users);
        }
        finally
        {
            userlock.readLock().unlock();
        }
        return l;
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

    /**
     * Gets a list of all user which are in the given group
     *
     * @param group the group
     * @return a list of all user which are in the given group
     */
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
        removeUserFromCache(user);

        //database
        backEnd.deleteUser(user);

        //send bukkit update infoif(useUUIDs)
        BungeePerms.getInstance().getNetworkNotifier().deleteUser(user, null);

        //call event
        BungeePerms.getInstance().getEventDispatcher().dispatchUserChangeEvent(user);
    }

    /**
     * Deletes a user from cache and database and validates all groups and users.
     *
     * @param group the group the remove
     */
    public synchronized void deleteGroup(Group group)
    {
        //cache
        removeGroupFromCache(group);

        //database
        backEnd.deleteGroup(group);

        //group validation
        validateUsersGroups();

        //send bukkit update info
        BungeePerms.getInstance().getNetworkNotifier().deleteGroup(group, null);

        //call event
        BungeePerms.getInstance().getEventDispatcher().dispatchGroupChangeEvent(group);
    }

    /**
     * Adds a user to cache and database.
     *
     * @param user the user to add
     */
    public synchronized void addUser(User user)
    {
        //cache
        addUserToCache(user);

        //database
        backEnd.saveUser(user, true);

        //send bukkit update info & changed events
        userChanged(user);
    }

    /**
     * Adds a group to cache and database.
     *
     * @param group the group to add
     */
    public synchronized void addGroup(Group group)
    {
        grouplock.writeLock().lock();
        try
        {
            groups.add(group);
            Collections.sort(groups);
        }
        finally
        {
            grouplock.writeLock().unlock();
        }

        //database
        backEnd.saveGroup(group, true);

        //send bukkit update info & changed events
        groupChanged(group);
    }

    //database and permission operations
    /**
     * Formats the permissions backEnd.
     */
    public void format()
    {
        backEnd.format(backEnd.loadGroups(), backEnd.loadUsers(), permsversion);
        backEnd.load();
        BungeePerms.getInstance().getNetworkNotifier().reloadAll(null);
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
        BungeePerms.getInstance().getNetworkNotifier().reloadAll(null);
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
        user.getGroupsString().add(group.getName());
        Collections.sort(user.getGroupsString());

        //database
        backEnd.saveUserGroups(user);

        //recalc perms
        user.invalidateCache();

        //send bukkit update info & changed events
        userChanged(user);
    }

    /**
     * Adds the given timed group to the user.
     *
     * @param user the user to add the group to
     * @param group the timed group to add to the user
     */
    public void addUserTimedGroup(User user, TimedValue<Group> group)
    {
        //cache
        user.getTimedGroupsString().add(new TimedValue(group.getValue().getName(), group.getStart(), group.getDuration()));
        Collections.sort(user.getTimedGroupsString());

        //database
        backEnd.saveUserTimedGroups(user);

        //recalc perms
        user.invalidateCache();

        //send bukkit update info & changed events
        userChanged(user);
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
        user.getGroupsString().remove(group.getName());

        //database
        backEnd.saveUserGroups(user);

        //recalc perms
        user.invalidateCache();

        //send bukkit update info & changed events
        userChanged(user);
    }

    /**
     * Removes the given timed group from the user.
     *
     * @param user the user to remove the group from
     * @param group the timed group to remove from the user
     */
    public void removeUserTimedGroup(User user, Group group)
    {
        //cache
        for (TimedValue<String> t : user.getTimedGroupsString())
            if (t.getValue().equalsIgnoreCase(group.getName()))
            {
                user.getTimedGroupsString().remove(t);
                break;
            }

        //database
        backEnd.saveUserTimedGroups(user);

        //recalc perms
        user.invalidateCache();

        //send bukkit update info & changed events
        userChanged(user);
    }

    /**
     * Adds a permission to a user.
     *
     * @param user the user
     * @param server optional: the server
     * @param world optional: the world
     * @param perm the permission to add
     */
    public void addUserPerm(User user, String server, String world, String perm)
    {
        server = Statics.toLower(server);
        world = server == null ? null : Statics.toLower(world);
        perm = Statics.toLower(perm);

        //cache
        if (server == null)
            user.getPerms().add(perm);
        else if (world == null)
            user.getServer(server).getPerms().add(perm);
        else
            user.getServer(server).getWorld(world).getPerms().add(perm);

        //database
        backEnd.saveUserPerms(user, server, world);

        //recalc perms
        user.invalidateCache();

        //send bukkit update info & changed events
        userChanged(user);
    }

    /**
     * Adds a timed permission to a user.
     *
     * @param user the user
     * @param server optional: the server
     * @param world optional: the world
     * @param perm the timed permission to add
     */
    public void addUserTimedPerm(User user, String server, String world, TimedValue perm)
    {
        server = Statics.toLower(server);
        world = server == null ? null : Statics.toLower(world);

        //cache
        if (server == null)
            user.getTimedPerms().add(perm);
        else if (world == null)
            user.getServer(server).getTimedPerms().add(perm);
        else
            user.getServer(server).getWorld(world).getTimedPerms().add(perm);

        //database
        backEnd.saveUserTimedPerms(user, server, world);

        //recalc perms
        user.invalidateCache();

        //send bukkit update info & changed events
        userChanged(user);
    }

    /**
     * Removes a permission from a user.
     *
     * @param user the user
     * @param server optional: the server
     * @param world optional: the world
     * @param perm the permission to remove
     */
    public void removeUserPerm(User user, String server, String world, String perm)
    {
        server = Statics.toLower(server);
        world = server == null ? null : Statics.toLower(world);
        perm = Statics.toLower(perm);

        //cache
        if (server == null)
            user.getPerms().remove(perm);
        else if (world == null)
            user.getServer(server).getPerms().remove(perm);
        else
            user.getServer(server).getWorld(world).getPerms().remove(perm);

        //database
        backEnd.saveUserPerms(user, server, world);

        //recalc perms
        user.invalidateCache();

        //send bukkit update info & changed events
        userChanged(user);
    }

    /**
     * Removes a timed permission from a user.
     *
     * @param user the user
     * @param server optional: the server
     * @param world optional: the world
     * @param perm timed the permission to remove
     */
    public void removeUserTimedPerm(User user, String server, String world, String perm)
    {
        server = Statics.toLower(server);
        world = server == null ? null : Statics.toLower(world);

        //cache
        if (server == null)
        {
            for (TimedValue<String> t : user.getTimedPerms())
                if (t.getValue().equalsIgnoreCase(perm))
                {
                    user.getTimedPerms().remove(t);
                    break;
                }
        }
        else if (world == null)
        {
            for (TimedValue<String> t : user.getServer(server).getTimedPerms())
                if (t.getValue().equalsIgnoreCase(perm))
                {
                    user.getServer(server).getTimedPerms().remove(t);
                    break;
                }
        }
        else
        {
            for (TimedValue<String> t : user.getServer(server).getWorld(world).getTimedPerms())
                if (t.getValue().equalsIgnoreCase(perm))
                {
                    user.getServer(server).getWorld(world).getTimedPerms().remove(t);
                    break;
                }
        }

        //database
        backEnd.saveUserTimedPerms(user, server, world);

        //recalc perms
        user.invalidateCache();

        //send bukkit update info & changed events
        userChanged(user);
    }

    /**
     * Sets the displayname of the group
     *
     * @param user the user
     * @param display the display
     * @param server the server to set the display on; may be null
     * @param world the world to ser the display in; may be null
     */
    public void setUserDisplay(User user, String display, String server, String world)
    {
        //cache
        if (server == null)
        {
            user.setDisplay(display);
        }
        else if (world == null)
        {
            user.getServer(server).setDisplay(display);
        }
        else
        {
            user.getServer(server).getWorld(world).setDisplay(display);
        }

        //database
        backEnd.saveUserDisplay(user, Statics.toLower(server), Statics.toLower(world));

        //send bukkit update info & changed events
        userChanged(user);
    }

    /**
     * Sets the prefix for the group.
     *
     * @param user the user
     * @param prefix the prefix
     * @param server the server to set the prefix on; may be null
     * @param world the world to ser the prefix in; may be null
     */
    public void setUserPrefix(User user, String prefix, String server, String world)
    {
        //cache
        if (server == null)
        {
            user.setPrefix(prefix);
        }
        else if (world == null)
        {
            user.getServer(server).setPrefix(prefix);
        }
        else
        {
            user.getServer(server).getWorld(world).setPrefix(prefix);
        }

        //database
        backEnd.saveUserPrefix(user, Statics.toLower(server), Statics.toLower(world));

        //send bukkit update info & changed events
        userChanged(user);
    }

    /**
     * Sets the suffix for the group.
     *
     * @param user the user
     * @param suffix the suffix
     * @param server the server to set the suffix on; may be null
     * @param world the world to ser the suffix in; may be null
     */
    public void setUserSuffix(User user, String suffix, String server, String world)
    {
        //cache
        if (server == null)
        {
            user.setSuffix(suffix);
        }
        else if (world == null)
        {
            user.getServer(server).setSuffix(suffix);
        }
        else
        {
            user.getServer(server).getWorld(world).setSuffix(suffix);
        }

        //database
        backEnd.saveUserSuffix(user, Statics.toLower(server), Statics.toLower(world));

        //send bukkit update info & changed events
        userChanged(user);
    }

    /**
     * Adds a permission to a group.
     *
     * @param group the group
     * @param server optional: the server
     * @param world optional: the world
     * @param perm the permission to add
     */
    public void addGroupPerm(Group group, String server, String world, String perm)
    {
        server = Statics.toLower(server);
        world = server == null ? null : Statics.toLower(world);
        perm = Statics.toLower(perm);

        //cache
        if (server == null)
            group.getPerms().add(perm);
        else if (world == null)
            group.getServer(server).getPerms().add(perm);
        else
            group.getServer(server).getWorld(world).getPerms().add(perm);

        //database
        backEnd.saveGroupPerms(group, server, world);

        //recalc perms
        for (Group g : groups)
            g.invalidateCache();
        for (User u : users)
            u.invalidateCache();

        //send bukkit update info & changed events
        groupChanged(group);
    }

    /**
     * Adds a timed permission to a group.
     *
     * @param group the group
     * @param server optional: the server
     * @param world optional: the world
     * @param perm the timed permission to add
     */
    public void addGroupTimedPerm(Group group, String server, String world, TimedValue perm)
    {
        server = Statics.toLower(server);
        world = server == null ? null : Statics.toLower(world);

        //cache
        if (server == null)
            group.getTimedPerms().add(perm);
        else if (world == null)
            group.getServer(server).getTimedPerms().add(perm);
        else
            group.getServer(server).getWorld(world).getTimedPerms().add(perm);

        //database
        backEnd.saveGroupTimedPerms(group, server, world);

        //recalc perms
        for (Group g : groups)
            g.invalidateCache();
        for (User u : users)
            u.invalidateCache();

        //send bukkit update info & changed events
        groupChanged(group);
    }

    /**
     * Removes a permission from a group.
     *
     * @param group the group
     * @param server optional: the server
     * @param world optional: the world
     * @param perm the permission to remove
     */
    public void removeGroupPerm(Group group, String server, String world, String perm)
    {
        //cache
        if (server == null)
            group.getPerms().remove(perm);
        else if (world == null)
            group.getServer(server).getPerms().remove(perm);
        else
            group.getServer(server).getWorld(world).getPerms().remove(perm);

        //database
        backEnd.saveGroupPerms(group, server, world);

        //recalc perms
        for (Group g : groups)
            g.invalidateCache();
        for (User u : users)
            u.invalidateCache();

        //send bukkit update info & changed events
        groupChanged(group);
    }

    /**
     * Removes a timed permission from a group.
     *
     * @param group the group
     * @param server optional: the server
     * @param world optional: the world
     * @param perm the timed permission to remove
     */
    public void removeGroupTimedPerm(Group group, String server, String world, String perm)
    {
        //cache
        if (server == null)
        {
            for (TimedValue<String> t : group.getTimedPerms())
                if (t.getValue().equalsIgnoreCase(perm))
                {
                    group.getTimedPerms().remove(t);
                    break;
                }
        }
        else if (world == null)
        {
            for (TimedValue<String> t : group.getServer(server).getTimedPerms())
                if (t.getValue().equalsIgnoreCase(perm))
                {
                    group.getServer(server).getTimedPerms().remove(t);
                    break;
                }
        }
        else
        {
            for (TimedValue<String> t : group.getServer(server).getWorld(world).getTimedPerms())
                if (t.getValue().equalsIgnoreCase(perm))
                {
                    group.getServer(server).getWorld(world).getTimedPerms().remove(t);
                    break;
                }
        }

        //database
        backEnd.saveGroupTimedPerms(group, server, world);

        //recalc perms
        for (Group g : groups)
            g.invalidateCache();
        for (User u : users)
            u.invalidateCache();

        //send bukkit update info & changed events
        groupChanged(group);
    }

    /**
     * Adds the toadd group to the group as inheritance
     *
     * @param group the group which should inherit
     * @param toadd the group which should be inherited
     */
    public void addGroupInheritance(Group group, Group toadd)
    {
        //cache
        group.getInheritancesString().add(toadd.getName());
        Collections.sort(group.getInheritancesString());

        //database
        backEnd.saveGroupInheritances(group);

        //recalc perms
        for (Group g : groups)
            g.invalidateCache();
        for (User u : users)
            u.invalidateCache();

        //send bukkit update info & changed events
        groupChanged(group);
    }

    /**
     * Adds the toadd timed group to the group as inheritance
     *
     * @param group the group which should inherit
     * @param toadd the timed group which should be inherited
     */
    public void addGroupTimedInheritance(Group group, TimedValue<Group> toadd)
    {
        //cache
        group.getTimedInheritancesString().add(new TimedValue(toadd.getValue().getName(), toadd.getStart(), toadd.getDuration()));
        Collections.sort(group.getTimedInheritancesString());

        //database
        backEnd.saveGroupTimedInheritances(group);

        //recalc perms
        for (Group g : groups)
            g.invalidateCache();
        for (User u : users)
            u.invalidateCache();

        //send bukkit update info & changed events
        groupChanged(group);
    }

    /**
     * Removes the toremove group from the group as inheritance
     *
     * @param group the group which should no longer inherit
     * @param toremove the group which should no longer be inherited
     */
    public void removeGroupInheritance(Group group, Group toremove)
    {
        //cache
        group.getInheritancesString().remove(toremove.getName());

        //database
        backEnd.saveGroupInheritances(group);

        //recalc perms
        for (Group g : groups)
            g.invalidateCache();
        for (User u : users)
            u.invalidateCache();

        //send bukkit update info & changed events
        groupChanged(group);
    }

    /**
     * Removes the toremove timed group from the group as inheritance
     *
     * @param group the group which should no longer inherit
     * @param toremove the timed group which should no longer be inherited
     */
    public void removeGroupTimedInheritance(Group group, Group toremove)
    {
        //cache
        for (TimedValue<String> t : group.getTimedInheritancesString())
            if (t.getValue().equalsIgnoreCase(toremove.getName()))
            {
                group.getTimedInheritancesString().remove(t);
                break;
            }

        //database
        backEnd.saveGroupTimedInheritances(group);

        //recalc perms
        for (Group g : groups)
            g.invalidateCache();
        for (User u : users)
            u.invalidateCache();

        //send bukkit update info & changed events
        groupChanged(group);
    }

    /**
     * Set the ladder for the group.
     *
     * @param group the group
     * @param ladder the ladder name
     */
    public void ladderGroup(Group group, String ladder)
    {
        //cache
        group.setLadder(ladder);

        //database
        backEnd.saveGroupLadder(group);

        //send bukkit update info & changed events
        groupChanged(group);
    }

    /**
     * Sets the rank for the group.
     *
     * @param group the group
     * @param rank the rank
     */
    public void rankGroup(Group group, int rank)
    {
        //cache
        group.setRank(rank);
        Collections.sort(groups);

        //database
        backEnd.saveGroupRank(group);

        //send bukkit update info & changed events
        groupChanged(group);
    }

    /**
     * Sets the weight for the group.
     *
     * @param group the group
     * @param weight the weight
     */
    public void weightGroup(Group group, int weight)
    {
        //cache
        group.setWeight(weight);
        Collections.sort(groups);

        //database
        backEnd.saveGroupWeight(group);

        //send bukkit update info & changed events
        groupChanged(group);
    }

    /**
     * Sets if the the group is a default group.
     *
     * @param group the group
     * @param isdefault whether or not the group is aded by default to new users
     */
    public void setGroupDefault(Group group, boolean isdefault)
    {
        //cache
        group.setIsdefault(isdefault);

        //database
        backEnd.saveGroupDefault(group);

        //send bukkit update info & changed events
        groupChanged(group);
    }

    /**
     * Sets the displayname of the group
     *
     * @param group the group
     * @param display the display
     * @param server the server to set the display on; may be null
     * @param world the world to ser the display in; may be null
     */
    public void setGroupDisplay(Group group, String display, String server, String world)
    {
        //cache
        if (server == null)
        {
            group.setDisplay(display);
        }
        else if (world == null)
        {
            group.getServer(server).setDisplay(display);
        }
        else
        {
            group.getServer(server).getWorld(world).setDisplay(display);
        }

        //database
        backEnd.saveGroupDisplay(group, server, world);

        //send bukkit update info & changed events
        groupChanged(group);
    }

    /**
     * Sets the prefix for the group.
     *
     * @param group the group
     * @param prefix the prefix
     * @param server the server to set the prefix on; may be null
     * @param world the world to ser the prefix in; may be null
     */
    public void setGroupPrefix(Group group, String prefix, String server, String world)
    {
        //cache
        if (server == null)
        {
            group.setPrefix(prefix);
        }
        else if (world == null)
        {
            group.getServer(server).setPrefix(prefix);
        }
        else
        {
            group.getServer(server).getWorld(world).setPrefix(prefix);
        }

        //database
        backEnd.saveGroupPrefix(group, server, world);

        //send bukkit update info & changed events
        groupChanged(group);
    }

    /**
     * Sets the suffix for the group.
     *
     * @param group the group
     * @param suffix the suffix
     * @param server the server to set the suffix on; may be null
     * @param world the world to ser the suffix in; may be null
     */
    public void setGroupSuffix(Group group, String suffix, String server, String world)
    {
        //cache
        if (server == null)
        {
            group.setSuffix(suffix);
        }
        else if (world == null)
        {
            group.getServer(server).setSuffix(suffix);
        }
        else
        {
            group.getServer(server).getWorld(world).setSuffix(suffix);
        }

        //database
        backEnd.saveGroupSuffix(group, server, world);

        //send bukkit update info & changed events
        groupChanged(group);
    }

    /**
     * Migrates the permissions to the given backnd type.
     *
     * @param bet the backEnd type to migrate to
     */
    public synchronized void migrateBackEnd(BackEndType bet)
    {
        Migrator migrator;
        switch (bet)
        {
            case YAML:
                migrator = new Migrate2YAML(config);
                break;
            case MySQL:
                migrator = new Migrate2MySQL(config, debug);
                break;
            default:
                throw new UnsupportedOperationException("bet = " + bet.name());
        }

        debug.log("migrate backend: loading all groups and users");
        List<Group> groups = backEnd.loadGroups();
        debug.log("migrate backend: loaded groups");
        List<User> users = backEnd.loadUsers();
        debug.log("migrate backend: loaded users");
        Map<UUID, String> uuidplayer = new HashMap();
        if (config.isUseUUIDs())
            uuidplayer = UUIDPlayerDB.getAll();

        migrator.migrate(groups, users, uuidplayer, permsversion);

        backEnd.load();
    }

    /**
     * Converts the permissions database to use UUIDs for player identification.
     *
     * @param uuids a map of player names and their corresponding UUIDs
     */
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

    /**
     * Converts the permissions database to use player names for player identification.
     *
     * @param playernames a map of UUIDs and their corresponding player names
     */
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
        u.invalidateCache();
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
        u.invalidateCache();
    }

    public void reloadGroup(String group)
    {
        Group g = getGroup(group);
        if (g == null)
        {
            debug.log("Group " + group + " not found!!!");
            return;
        }

        boolean holdread = false;
        grouplock.writeLock().lock();
        try
        {
            backEnd.reloadGroup(g);
            Collections.sort(groups);

            grouplock.readLock().lock();
            holdread = true;
        }
        finally
        {
            grouplock.writeLock().unlock();
        }
        try
        {
            for (Group gr : groups)
            {
                gr.invalidateCache();
            }
        }
        finally
        {
            if (holdread)
            {
                grouplock.readLock().unlock();
            }
        }

        userlock.readLock().lock();
        try
        {
            for (User u : users)
            {
                u.invalidateCache();
            }
        }
        finally
        {
            userlock.readLock().unlock();
        }
    }

    public void reloadUsers()
    {
        userlock.readLock().lock();
        try
        {
            for (User u : users)
            {
                backEnd.reloadUser(u);
                u.invalidateCache();
            }
        }
        finally
        {
            userlock.readLock().unlock();
        }
    }

    public void reloadGroups()
    {
        boolean holdread = false;
        grouplock.writeLock().lock();
        try
        {
            for (Group g : groups)
            {
                backEnd.reloadGroup(g);
            }
            Collections.sort(groups);

            grouplock.readLock().lock();
            holdread = true;
        }
        finally
        {
            grouplock.writeLock().unlock();
        }
        try
        {
            for (Group g : groups)
            {
                g.invalidateCache();
            }
        }
        finally
        {
            if (holdread)
            {
                grouplock.readLock().unlock();
            }
        }

        userlock.readLock().lock();
        try
        {
            for (User u : users)
            {
                u.invalidateCache();
            }
        }
        finally
        {
            userlock.readLock().unlock();
        }
    }

    public void addUserToCache(User u)
    {
        userlock.writeLock().lock();
        try
        {
            users.add(u);
        }
        finally
        {
            userlock.writeLock().unlock();
        }
    }

    public void removeUserFromCache(User u)
    {
        userlock.writeLock().lock();
        try
        {
            users.remove(u);
        }
        finally
        {
            userlock.writeLock().unlock();
        }
    }

    public void addGroupToCache(Group g)
    {
        grouplock.writeLock().lock();
        try
        {
            groups.add(g);
        }
        finally
        {
            grouplock.writeLock().unlock();
        }
    }

    public void removeGroupFromCache(Group g)
    {
        grouplock.writeLock().lock();
        try
        {
            groups.remove(g);
        }
        finally
        {
            grouplock.writeLock().unlock();
        }
    }

    private void userChanged(User u)
    {
        //send bukkit update info
        BungeePerms.getInstance().getNetworkNotifier().reloadUser(u, null);

        //call event
        BungeePerms.getInstance().getEventDispatcher().dispatchUserChangeEvent(u);
    }

    private void groupChanged(Group g)
    {
        //send bukkit update info
        BungeePerms.getInstance().getNetworkNotifier().reloadGroup(g, null);

        //call event
        BungeePerms.getInstance().getEventDispatcher().dispatchGroupChangeEvent(g);
    }
}
