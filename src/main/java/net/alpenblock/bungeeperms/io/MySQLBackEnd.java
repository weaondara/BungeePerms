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
package net.alpenblock.bungeeperms.io;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import net.alpenblock.bungeeperms.BPConfig;
import net.alpenblock.bungeeperms.BPPermission;
import net.alpenblock.bungeeperms.BungeePerms;
import net.alpenblock.bungeeperms.Debug;
import net.alpenblock.bungeeperms.Group;
import net.alpenblock.bungeeperms.Mysql;
import net.alpenblock.bungeeperms.PermEntity;
import net.alpenblock.bungeeperms.Permable;
import net.alpenblock.bungeeperms.Server;
import net.alpenblock.bungeeperms.Statics;
import net.alpenblock.bungeeperms.TimedValue;
import net.alpenblock.bungeeperms.User;
import net.alpenblock.bungeeperms.World;
import net.alpenblock.bungeeperms.io.mysql2.EntityType;
import net.alpenblock.bungeeperms.io.mysql2.MysqlPermEntity;
import net.alpenblock.bungeeperms.io.mysql2.MysqlPermsAdapter;
import net.alpenblock.bungeeperms.io.mysql2.ValueEntry;
import net.alpenblock.bungeeperms.platform.PlatformPlugin;
import net.alpenblock.bungeeperms.platform.PlatformType;

public class MySQLBackEnd implements BackEnd
{

    private final PlatformPlugin plugin;
    private final BPConfig config;
    private final Debug debug;
    @Getter
    private final Mysql mysql;

    private final MysqlPermsAdapter adapter;
    private String table;

    public MySQLBackEnd()
    {
        plugin = BungeePerms.getInstance().getPlugin();
        config = BungeePerms.getInstance().getConfig();
        debug = BungeePerms.getInstance().getDebug();

        mysql = new Mysql(config, debug, plugin.getPlatformType() == PlatformType.Velocity);
        mysql.connect();

        table = config.getMysqlTablePrefix() + "permissions";

        PreparedStatement stmt = null;
        ResultSet res = null;
        try
        {
            stmt = mysql.stmt("SHOW TABLES LIKE '" + table + "2'");
            res = mysql.returnQuery(stmt);
            if (res.next())
            {
                res.close();
                stmt.close();
                stmt = mysql.stmt("ALTER TABLE `" + table + "2` RENAME `" + table + "`");
                stmt.execute();
                BungeePerms.getLogger().info("Renamed mysql permissions table from " + table + "2 to " + table);
            }
        }
        catch (Exception e)
        {
            if (stmt != null)
                BungeePerms.getLogger().severe("stmt: " + stmt);
            debug.log(e);
            table += "2";
        }
        finally
        {
            Mysql.close(res);
            Mysql.close(stmt);
        }

        adapter = new MysqlPermsAdapter(mysql, table);
        adapter.createTable();
    }

    @Override
    public BackEndType getType()
    {
        return BackEndType.MySQL;
    }

    @Override
    public void load()
    {
    }

    @Override
    public List<Group> loadGroups()
    {
        List<Group> ret = new ArrayList<>();

        List<String> groups = adapter.getGroups();
        for (String g : groups)
        {
            Group group = loadGroup(g);
            ret.add(group);
        }
        Collections.sort(ret);

        return ret;
    }

    @Override
    public List<User> loadUsers()
    {
        List<User> ret = new ArrayList<>();

        List<String> users = adapter.getUsers();
        BungeePerms.getInstance().getDebug().log("loading " + users.size() + " users");
        int i = 0;
        for (String u : users)
        {
            i++;
            if (i % 1000 == 0)
                BungeePerms.getInstance().getDebug().log("loaded " + i + "/" + users.size() + " users");
            User user = BungeePerms.getInstance().getConfig().isUseUUIDs() ? loadUser(UUID.fromString(u)) : loadUser(u);
            ret.add(user);
        }

        return ret;
    }

    @Override
    public Group loadGroup(String group)
    {
        MysqlPermEntity mpe = adapter.getGroup(group);
        if (mpe.getName() == null)
            return null;

        boolean isdefault = getFirstValue(mpe.getData("default"), false);
        int rank = getFirstValue(mpe.getData("rank"), 1000);
        int weight = getFirstValue(mpe.getData("weight"), 1000);
        String ladder = getFirstValue(mpe.getData("ladder"), null, null, "default");

        Group g = new Group(mpe.getName(), new ArrayList(), new ArrayList(), new ArrayList(), new ArrayList(), new HashMap(), rank, weight, ladder, isdefault, null, null, null);
        loadServerWorlds(mpe, g);
        g.invalidateCache();

        return g;
    }

    @Override
    public User loadUser(String user)
    {
        MysqlPermEntity mpe = adapter.getUser(user);
        if (mpe.getName() == null)
            return null;

        UUID uuid = BungeePerms.getInstance().getConfig().isUseUUIDs() ? BungeePerms.getInstance().getPermissionsManager().getUUIDPlayerDB().getUUID(mpe.getName()) : null;
        User u = new User(mpe.getName(), uuid, new ArrayList(), new ArrayList(), new ArrayList(), new ArrayList(), new HashMap(), null, null, null);
        loadServerWorlds(mpe, u);
        u.invalidateCache();

        return u;
    }

    @Override
    public User loadUser(UUID user)
    {
        MysqlPermEntity mpe = adapter.getUser(user.toString());
        if (mpe.getName() == null)
            return null;

        String username = BungeePerms.getInstance().getPermissionsManager().getUUIDPlayerDB().getPlayerName(user);
        User u = new User(username, user, new ArrayList(), new ArrayList(), new ArrayList(), new ArrayList(), new HashMap(), null, null, null);
        loadServerWorlds(mpe, u);
        u.invalidateCache();

        return u;
    }

    @Override
    public int loadVersion()
    {
        MysqlPermEntity mpe = adapter.getVersion();
        int version = getFirstValue(mpe.getData("version"), 2);
        return version;
    }

    @Override
    public void saveVersion(int version, boolean savetodisk)
    {
        adapter.saveData("version", EntityType.Version, "version", mkValueList(mkList(String.valueOf(version)), null, null), null, null);
    }

    @Override
    public boolean isUserInDatabase(User user)
    {
        return adapter.isInBD(BungeePerms.getInstance().getConfig().isUseUUIDs() ? user.getUUID().toString() : user.getName(), EntityType.User);
    }

    @Override
    public List<String> getRegisteredUsers()
    {
        return adapter.getUsers();
    }

    @Override
    public List<String> getGroupUsers(Group group)
    {
        return adapter.getGroupUsers(group.getName());
    }

    @Override
    public synchronized void saveUser(User user, boolean savetodisk)
    {
        if (BungeePerms.getInstance().getConfig().isSaveAllUsers() || !user.isNothingSpecial())
        {
            saveUserGroups(user, null, null);
            saveUserTimedGroups(user, null, null);
            saveUserPerms(user, null, null);
            saveUserTimedPerms(user, null, null);
            saveUserDisplay(user, null, null);
            saveUserPrefix(user, null, null);
            saveUserSuffix(user, null, null);

            for (Map.Entry<String, Server> se : user.getServers().entrySet())
            {
                saveUserGroups(user, se.getKey(), null);
                saveUserTimedGroups(user, se.getKey(), null);
                saveUserPerms(user, se.getKey(), null);
                saveUserTimedPerms(user, se.getKey(), null);
                saveUserDisplay(user, se.getKey(), null);
                saveUserPrefix(user, se.getKey(), null);
                saveUserSuffix(user, se.getKey(), null);

                for (Map.Entry<String, World> we : se.getValue().getWorlds().entrySet())
                {
                    saveUserGroups(user, se.getKey(), we.getKey());
                    saveUserTimedGroups(user, se.getKey(), we.getKey());
                    saveUserPerms(user, se.getKey(), we.getKey());
                    saveUserTimedPerms(user, se.getKey(), we.getKey());
                    saveUserDisplay(user, se.getKey(), we.getKey());
                    saveUserPrefix(user, se.getKey(), we.getKey());
                    saveUserSuffix(user, se.getKey(), we.getKey());
                }
            }
        }
    }

    @Override
    public synchronized void saveGroup(Group group, boolean savetodisk)
    {
        saveGroupInheritances(group, null, null);
        saveGroupTimedInheritances(group, null, null);
        saveGroupPerms(group, null, null);
        saveGroupTimedPerms(group, null, null);
        saveGroupRank(group);
        saveGroupLadder(group);
        saveGroupDefault(group);
        saveGroupDisplay(group, null, null);
        saveGroupPrefix(group, null, null);
        saveGroupSuffix(group, null, null);

        for (Map.Entry<String, Server> se : group.getServers().entrySet())
        {
            saveGroupInheritances(group, se.getKey(), null);
            saveGroupTimedInheritances(group, se.getKey(), null);
            saveGroupPerms(group, se.getKey(), null);
            saveGroupTimedPerms(group, se.getKey(), null);
            saveGroupDisplay(group, se.getKey(), null);
            saveGroupPrefix(group, se.getKey(), null);
            saveGroupSuffix(group, se.getKey(), null);

            for (Map.Entry<String, World> we : se.getValue().getWorlds().entrySet())
            {
                saveGroupInheritances(group, se.getKey(), we.getKey());
                saveGroupTimedInheritances(group, se.getKey(), we.getKey());
                saveGroupPerms(group, se.getKey(), we.getKey());
                saveGroupTimedPerms(group, se.getKey(), we.getKey());
                saveGroupDisplay(group, se.getKey(), we.getKey());
                saveGroupPrefix(group, se.getKey(), we.getKey());
                saveGroupSuffix(group, se.getKey(), we.getKey());
            }
        }
    }

    @Override
    public synchronized void deleteUser(User user)
    {
        adapter.deleteEntity(BungeePerms.getInstance().getConfig().isUseUUIDs() ? user.getUUID().toString() : user.getName(), EntityType.User);
    }

    @Override
    public synchronized void deleteGroup(Group group)
    {
        adapter.deleteEntity(group.getName(), EntityType.Group);
    }

    @Override
    public synchronized void saveUserGroups(User user, String server, String world)
    {
        server = Statics.toLower(server);
        world = server == null ? null : Statics.toLower(world);

        List<String> groups = user.getGroupsString();
        if (server != null)
        {
            groups = user.getServer(server).getGroupsString();
            if (world != null)
            {
                groups = user.getServer(server).getWorld(world).getGroupsString();
            }
        }

        adapter.saveData(BungeePerms.getInstance().getConfig().isUseUUIDs() ? user.getUUID().toString() : user.getName(),
                         EntityType.User, "groups",
                         mkValueList(groups, server, world), server, world);
    }

    @Override
    public synchronized void saveUserTimedGroups(User user, String server, String world)
    {
        server = Statics.toLower(server);
        world = server == null ? null : Statics.toLower(world);

        List<TimedValue<String>> groups = user.getTimedGroupsString();
        if (server != null)
        {
            groups = user.getServer(server).getTimedGroupsString();
            if (world != null)
            {
                groups = user.getServer(server).getWorld(world).getTimedGroupsString();
            }
        }

        adapter.saveData(BungeePerms.getInstance().getConfig().isUseUUIDs() ? user.getUUID().toString() : user.getName(),
                         EntityType.User, "timedgroups",
                         mkValueListTimed(groups, server, world), server, world);
    }

    @Override
    public synchronized void saveUserPerms(User user, String server, String world)
    {
        server = Statics.toLower(server);
        world = server == null ? null : Statics.toLower(world);

        List<String> perms = user.getPerms();
        if (server != null)
        {
            perms = user.getServer(server).getPerms();
            if (world != null)
            {
                perms = user.getServer(server).getWorld(world).getPerms();
            }
        }

        adapter.saveData(BungeePerms.getInstance().getConfig().isUseUUIDs() ? user.getUUID().toString() : user.getName(),
                         EntityType.User, "permissions",
                         mkValueList(perms, server, world), server, world);
    }

    @Override
    public synchronized void saveUserTimedPerms(User user, String server, String world)
    {
        server = Statics.toLower(server);
        world = server == null ? null : Statics.toLower(world);

        List<TimedValue<String>> perms = user.getTimedPerms();
        if (server != null)
        {
            perms = user.getServer(server).getTimedPerms();
            if (world != null)
            {
                perms = user.getServer(server).getWorld(world).getTimedPerms();
            }
        }

        adapter.saveData(BungeePerms.getInstance().getConfig().isUseUUIDs() ? user.getUUID().toString() : user.getName(),
                         EntityType.User, "timedpermissions",
                         mkValueListTimed(perms, server, world), server, world);
    }

    @Override
    public synchronized void saveUserDisplay(User user, String server, String world)
    {
        server = Statics.toLower(server);
        world = Statics.toLower(world);

        String display = user.getDisplay();
        if (server != null)
        {
            display = user.getServer(server).getDisplay();
            if (world != null)
            {
                display = user.getServer(server).getWorld(world).getDisplay();
            }
        }
        adapter.saveData(BungeePerms.getInstance().getConfig().isUseUUIDs() ? user.getUUID().toString() : user.getName(),
                         EntityType.User, "display", mkList(new ValueEntry(display, server, world)), server, world);
    }

    @Override
    public synchronized void saveUserPrefix(User user, String server, String world)
    {
        server = Statics.toLower(server);
        world = Statics.toLower(world);

        String prefix = user.getPrefix();
        if (server != null)
        {
            prefix = user.getServer(server).getPrefix();
            if (world != null)
            {
                prefix = user.getServer(server).getWorld(world).getPrefix();
            }
        }
        adapter.saveData(BungeePerms.getInstance().getConfig().isUseUUIDs() ? user.getUUID().toString() : user.getName(),
                         EntityType.User, "prefix", mkList(new ValueEntry(prefix, server, world)), server, world);
    }

    @Override
    public synchronized void saveUserSuffix(User user, String server, String world)
    {
        server = Statics.toLower(server);
        world = Statics.toLower(world);

        String suffix = user.getSuffix();
        if (server != null)
        {
            suffix = user.getServer(server).getSuffix();
            if (world != null)
            {
                suffix = user.getServer(server).getWorld(world).getSuffix();
            }
        }
        adapter.saveData(BungeePerms.getInstance().getConfig().isUseUUIDs() ? user.getUUID().toString() : user.getName(),
                         EntityType.User, "suffix", mkList(new ValueEntry(suffix, server, world)), server, world);
    }

    @Override
    public synchronized void saveGroupPerms(Group group, String server, String world)
    {
        server = Statics.toLower(server);
        world = server == null ? null : Statics.toLower(world);

        List<String> perms = group.getPerms();
        if (server != null)
        {
            perms = group.getServer(server).getPerms();
            if (world != null)
            {
                perms = group.getServer(server).getWorld(world).getPerms();
            }
        }

        adapter.saveData(group.getName(), EntityType.Group, "permissions", mkValueList(perms, server, world), server, world);
    }

    @Override
    public synchronized void saveGroupTimedPerms(Group group, String server, String world)
    {
        server = Statics.toLower(server);
        world = server == null ? null : Statics.toLower(world);

        List<TimedValue<String>> perms = group.getTimedPerms();
        if (server != null)
        {
            perms = group.getServer(server).getTimedPerms();
            if (world != null)
            {
                perms = group.getServer(server).getWorld(world).getTimedPerms();
            }
        }

        adapter.saveData(group.getName(), EntityType.Group, "timedpermissions", mkValueListTimed(perms, server, world), server, world);
    }

    @Override
    public synchronized void saveGroupInheritances(Group group, String server, String world)
    {
        server = Statics.toLower(server);
        world = server == null ? null : Statics.toLower(world);

        List<String> inheritances = group.getInheritancesString();
        if (server != null)
        {
            inheritances = group.getServer(server).getGroupsString();
            if (world != null)
            {
                inheritances = group.getServer(server).getWorld(world).getGroupsString();
            }
        }

        adapter.saveData(group.getName(), EntityType.Group, "inheritances", mkValueList(inheritances, server, world), server, world);
    }

    @Override
    public synchronized void saveGroupTimedInheritances(Group group, String server, String world)
    {
        server = Statics.toLower(server);
        world = server == null ? null : Statics.toLower(world);

        List<TimedValue<String>> inheritances = group.getTimedInheritancesString();
        if (server != null)
        {
            inheritances = group.getServer(server).getTimedGroupsString();
            if (world != null)
            {
                inheritances = group.getServer(server).getWorld(world).getTimedGroupsString();
            }
        }

        adapter.saveData(group.getName(), EntityType.Group, "timedinheritances", mkValueListTimed(inheritances, server, world), server, world);
    }

    @Override
    public synchronized void saveGroupLadder(Group group)
    {
        adapter.saveData(group.getName(), EntityType.Group, "ladder", mkList(new ValueEntry(group.getLadder(), null, null)), null, null);
    }

    @Override
    public synchronized void saveGroupRank(Group group)
    {
        adapter.saveData(group.getName(), EntityType.Group, "rank", mkList(new ValueEntry(String.valueOf(group.getRank()), null, null)), null, null);
    }

    @Override
    public synchronized void saveGroupWeight(Group group)
    {
        adapter.saveData(group.getName(), EntityType.Group, "weight", mkList(new ValueEntry(String.valueOf(group.getWeight()), null, null)), null, null);
    }

    @Override
    public synchronized void saveGroupDefault(Group group)
    {
        adapter.saveData(group.getName(), EntityType.Group, "default", mkList(new ValueEntry(String.valueOf(group.isDefault()), null, null)), null, null);
    }

    @Override
    public synchronized void saveGroupDisplay(Group group, String server, String world)
    {
        server = Statics.toLower(server);
        world = Statics.toLower(world);

        String display = group.getDisplay();
        if (server != null)
        {
            display = group.getServer(server).getDisplay();
            if (world != null)
            {
                display = group.getServer(server).getWorld(world).getDisplay();
            }
        }
        adapter.saveData(group.getName(), EntityType.Group, "display", mkList(new ValueEntry(display, server, world)), server, world);
    }

    @Override
    public synchronized void saveGroupPrefix(Group group, String server, String world)
    {
        server = Statics.toLower(server);
        world = Statics.toLower(world);

        String prefix = group.getPrefix();
        if (server != null)
        {
            prefix = group.getServer(server).getPrefix();
            if (world != null)
            {
                prefix = group.getServer(server).getWorld(world).getPrefix();
            }
        }
        adapter.saveData(group.getName(), EntityType.Group, "prefix", mkList(new ValueEntry(prefix, server, world)), server, world);
    }

    @Override
    public synchronized void saveGroupSuffix(Group group, String server, String world)
    {
        server = Statics.toLower(server);
        world = Statics.toLower(world);

        String suffix = group.getSuffix();
        if (server != null)
        {
            suffix = group.getServer(server).getSuffix();
            if (world != null)
            {
                suffix = group.getServer(server).getWorld(world).getSuffix();
            }
        }
        adapter.saveData(group.getName(), EntityType.Group, "suffix", mkList(new ValueEntry(suffix, server, world)), server, world);
    }

    @Override
    public synchronized void format(List<Group> groups, List<User> users, int version)
    {
        clearDatabase();
        for (int i = 0; i < groups.size(); i++)
        {
            saveGroup(groups.get(i), false);
        }
        for (int i = 0; i < users.size(); i++)
        {
            saveUser(users.get(i), false);
        }
        saveVersion(version, false);
    }

    @Override
    public synchronized int cleanup(List<Group> groups, List<User> users, int version)
    {
        int deleted = 0;

        clearDatabase();
        for (int i = 0; i < groups.size(); i++)
        {
            saveGroup(groups.get(i), false);
        }
        for (int i = 0; i < users.size(); i++)
        {
            User u = users.get(i);
            if (BungeePerms.getInstance().getConfig().isDeleteUsersOnCleanup())
            {
                //check for additional permissions and non-default groups AND onlinecheck
                if (u.isNothingSpecial()
                    && plugin.getPlayer(u.getName()) == null
                    && plugin.getPlayer(u.getUUID()) == null)
                {
                    deleted++;
                    continue;
                }
            }

            //player has to be saved
            saveUser(users.get(i), false);
        }
        saveVersion(version, false);

        return deleted;
    }

    @Override
    public void clearDatabase()
    {
        adapter.clearTable(table);
        load();
    }

    @Override
    public void reloadGroup(Group group)
    {
        MysqlPermEntity mpe = adapter.getGroup(group.getName());
        boolean isdefault = getFirstValue(mpe.getData("default"), false);
        int rank = getFirstValue(mpe.getData("rank"), 1000);
        int weight = getFirstValue(mpe.getData("weight"), 1000);
        String ladder = getFirstValue(mpe.getData("ladder"), null, null, "default");

        //set
        group.setIsdefault(isdefault);
        group.setRank(rank);
        group.setWeight(weight);
        group.setLadder(ladder);

        //reset & load
        group.setInheritances(new ArrayList());
        group.setTimedInheritances(new ArrayList());
        group.setPerms(new ArrayList());
        group.setTimedPerms(new ArrayList());
        group.setServers(new HashMap());
        group.setDisplay(null);
        group.setPrefix(null);
        group.setSuffix(null);
        loadServerWorlds(mpe, group);
        group.invalidateCache();
    }

    @Override
    public void reloadUser(User user)
    {
        MysqlPermEntity mpe = adapter.getUser(config.isUseUUIDs() ? user.getUUID().toString() : user.getName());

        //reset & load
        user.setGroups(new ArrayList());
        user.setTimedGroups(new ArrayList());
        user.setPerms(new ArrayList());
        user.setTimedPerms(new ArrayList());
        user.setServers(new HashMap());
        user.setDisplay(null);
        user.setPrefix(null);
        user.setSuffix(null);
        loadServerWorlds(mpe, user);
        user.invalidateCache();
    }

    //helper functions
    private static List<String> getValues(List<ValueEntry> values)
    {
        if (values == null)
        {
            return new ArrayList<>();
        }
        List<String> ret = new ArrayList<>();
        for (ValueEntry e : values)
        {
            ret.add(e.getValue());
        }

        return ret;
    }

    private static List<TimedValue<String>> getValuesTimed(List<ValueEntry> values)
    {
        if (values == null)
            return new ArrayList<>();

        List<TimedValue<String>> ret = new ArrayList<>();
        for (ValueEntry e : values)
        {
            if (e.getStart() == null || e.getDuration() == null)
                continue;
            ret.add(new TimedValue(e.getValue(), new Date(e.getStart().getTime()), e.getDuration()));
        }

        return ret;
    }

    private static String getFirstValue(List<ValueEntry> values, String server, String world, String def)
    {
        if (values == null || values.isEmpty())
        {
            return def;
        }
        for (ValueEntry e : values)
        {
            //servers == null || (servers match && (worlds == null || worlds match))
            if ((server == null && e.getServer() == null)
                || (server != null && e.getServer() != null && e.getServer().equalsIgnoreCase(server)
                    && ((world == null && e.getWorld() == null)
                        || (world != null && e.getWorld() != null && e.getWorld().equalsIgnoreCase(world)))))
            {
                return e.getValue();
            }
        }
        return def;
    }

    private boolean getFirstValue(List<ValueEntry> values, boolean def)
    {
        if (values == null || values.isEmpty())
        {
            return def;
        }
        try
        {
            return Boolean.parseBoolean(values.get(0).getValue());
        }
        catch (Exception e)
        {
            return def;
        }
    }

    private int getFirstValue(List<ValueEntry> values, int def)
    {
        if (values == null || values.isEmpty())
        {
            return def;
        }
        try
        {
            return Integer.parseInt(values.get(0).getValue());
        }
        catch (Exception e)
        {
            return def;
        }
    }

    private <T> List<T> mkList(T... elements)
    {
        return new ArrayList(Arrays.asList(elements));
    }

    private List<ValueEntry> mkValueList(List<String> values, String server, String world)
    {
        List<ValueEntry> l = new ArrayList<>();
        for (String s : values)
        {
            l.add(new ValueEntry(s, server, world));
        }
        return l;
    }

    private List<ValueEntry> mkValueListTimed(List<TimedValue<String>> values, String server, String world)
    {
        List<ValueEntry> l = new ArrayList<>();
        for (TimedValue<String> s : values)
        {
            l.add(new ValueEntry(s.getValue(), server, world, new Timestamp(s.getStart().getTime()), s.getDuration()));
        }
        return l;
    }

    static void loadServerWorlds(MysqlPermEntity mpe, PermEntity p)
    {
        Map<String, Map<String, Map<String, List<ValueEntry>>>> map = mapServerWorlds(mpe, "permissions", "timedpermissions", "inheritances", "groups", "timedinheritances", "timedgroups", "prefix", "suffix", "display");

        //transfer
        for (Map.Entry<String, Map<String, Map<String, List<ValueEntry>>>> keylvl : map.entrySet())
        {
            for (Map.Entry<String, Map<String, List<ValueEntry>>> serverlvl : keylvl.getValue().entrySet())
            {
                for (Map.Entry<String, List<ValueEntry>> worldlvl : serverlvl.getValue().entrySet())
                {
                    Permable permable = (Permable) p;
                    Server s = null;
                    World w = null;
                    if (serverlvl.getKey() != null)
                    {
                        s = p.getServer(serverlvl.getKey());
                        permable = s;
                    }
                    if (s != null && worldlvl.getKey() != null)
                    {
                        w = s.getWorld(worldlvl.getKey());
                        permable = w;
                    }

                    switch (keylvl.getKey())
                    {
                        case "permissions":
                            permable.setPerms(getValues(worldlvl.getValue()));
                            break;
                        case "timedpermissions":
                            permable.setTimedPerms(getValuesTimed(worldlvl.getValue()));
                            break;
                        case "inheritances":
                        case "groups":
                            permable.setGroups(getValues(worldlvl.getValue()));
                            break;
                        case "timedinheritances":
                        case "timedgroups":
                            permable.setTimedGroups(getValuesTimed(worldlvl.getValue()));
                            break;
                        case "prefix":
                            permable.setPrefix(getFirstValue(worldlvl.getValue(), serverlvl.getKey(), worldlvl.getKey(), null));
                            break;
                        case "suffix":
                            permable.setSuffix(getFirstValue(worldlvl.getValue(), serverlvl.getKey(), worldlvl.getKey(), null));
                            break;
                        case "display":
                            permable.setDisplay(getFirstValue(worldlvl.getValue(), serverlvl.getKey(), worldlvl.getKey(), null));
                            break;
                        default:
                            break;
                    }
                }
            }
        }
    }

    //map<key, map<server, map<world, list<values>>>>
    static Map<String, Map<String, Map<String, List<ValueEntry>>>> mapServerWorlds(MysqlPermEntity mpe, String... keys)
    {
        Map<String, Map<String, Map<String, List<ValueEntry>>>> map = new HashMap();

        for (String key : keys)
        {
            //create key
            //map<server, map<world, list<values>>>
            Map<String, Map<String, List<ValueEntry>>> servermap = new HashMap();
            map.put(key, servermap);

            //parse servers and worlds
            List<ValueEntry> data = mpe.getData(key);
            if (data == null)
            {
                data = new ArrayList();
            }
            for (ValueEntry d : data)
            {
                String server = Statics.toLower(d.getServer());
                String world = server == null ? null : Statics.toLower(d.getWorld()); //check just for safety

                //get right maps/lists
                //map<world, list<values>>
                Map<String, List<ValueEntry>> worldmap;
                //list<values>
                List<ValueEntry> valuelist;
                if (servermap.containsKey(server))
                {
                    worldmap = servermap.get(server);
                }
                else
                {
                    worldmap = new HashMap();
                    servermap.put(server, worldmap);
                }
                if (worldmap.containsKey(world))
                {
                    valuelist = worldmap.get(world);
                }
                else
                {
                    valuelist = new ArrayList();
                    worldmap.put(world, valuelist);
                }

                //addvalue
                valuelist.add(d);
            }
        }

        return map;
    }

    @Override
    public void removeGroupReferences(Group g)
    {
        adapter.removeGroupReferences(g.getName());
    }

    @Override
    public List<BPPermission> getUsersWithPerm(String perm) {
        return adapter.getUsersWithPerm(perm);
    }

    @Override
    public List<BPPermission> getGroupsWithPerm(String perm) {
        return adapter.getGroupsWithPerm(perm);
    }


}
