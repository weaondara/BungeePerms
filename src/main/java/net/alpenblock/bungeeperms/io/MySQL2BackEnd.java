package net.alpenblock.bungeeperms.io;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.alpenblock.bungeeperms.BPConfig;
import net.alpenblock.bungeeperms.BungeePerms;
import net.alpenblock.bungeeperms.Debug;
import net.alpenblock.bungeeperms.Group;
import net.alpenblock.bungeeperms.Mysql;
import net.alpenblock.bungeeperms.Server;
import net.alpenblock.bungeeperms.Statics;
import net.alpenblock.bungeeperms.User;
import net.alpenblock.bungeeperms.World;
import net.alpenblock.bungeeperms.io.mysql2.EntityType;
import net.alpenblock.bungeeperms.io.mysql2.MysqlPermEntity;
import net.alpenblock.bungeeperms.io.mysql2.MysqlPermsAdapter2;
import net.alpenblock.bungeeperms.io.mysql2.ValueEntry;
import net.alpenblock.bungeeperms.platform.PlatformPlugin;

public class MySQL2BackEnd implements BackEnd
{

    private final PlatformPlugin plugin;
    private final BPConfig config;
    private final Debug debug;
    private final Mysql mysql;

    private final MysqlPermsAdapter2 adapter;
    private final String table;

    public MySQL2BackEnd()
    {
        plugin = BungeePerms.getInstance().getPlugin();
        config = BungeePerms.getInstance().getConfig();
        debug = BungeePerms.getInstance().getDebug();

        mysql = new Mysql(config.getConfig(), debug, "bungeeperms");
        mysql.connect();

        table = config.getTablePrefix() + "permissions2";

        adapter = new MysqlPermsAdapter2(mysql, table);
        adapter.createTable();
    }

    @Override
    public BackEndType getType()
    {
        return BackEndType.MySQL2;
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
        for (String u : users)
        {
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
        {
            return null;
        }

        List<String> inheritances = getValue(mpe.getData("inheritances"));
        boolean isdefault = getFirstValue(mpe.getData("default"), false);
        int rank = getFirstValue(mpe.getData("rank"), 1000);
        int weight = getFirstValue(mpe.getData("weight"), 1000);
        String ladder = getFirstValue(mpe.getData("ladder"), "default");
        String display = getFirstValue(mpe.getData("display"), "");
        String prefix = getFirstValue(mpe.getData("prefix"), "");
        String suffix = getFirstValue(mpe.getData("suffix"), "");

        //perms
        List<ValueEntry> permdata = mpe.getData("permissions");
        if (permdata == null)
        {
            permdata = new ArrayList<>();
        }
        List<String> globalperms = new ArrayList<>();
        List<String> foundservers = new ArrayList<>();

        //globalperms
        for (ValueEntry e : permdata)
        {
            //check for servers 
            if (e.getServer() != null)
            {
                if (!foundservers.contains(Statics.toLower(e.getServer())))
                {
                    foundservers.add(Statics.toLower(e.getServer()));
                }
            }

            //is global perm
            else
            {
                globalperms.add(Statics.toLower(e.getValue()));
            }
        }

        //server perms
        Map<String, Server> servers = new HashMap<>();
        for (String server : foundservers)
        {
            List<String> serverperms = new ArrayList<>();
            List<String> foundworlds = new ArrayList<>();
            for (ValueEntry e : permdata)
            {
                if (e.getServer() != null && e.getServer().equalsIgnoreCase(server))
                {
                    //check for worlds 
                    if (e.getWorld() != null)
                    {
                        if (!foundworlds.contains(Statics.toLower(e.getWorld())))
                        {
                            foundworlds.add(Statics.toLower(e.getWorld()));
                        }
                    }

                    //is server perm
                    else
                    {
                        serverperms.add(Statics.toLower(e.getValue()));
                    }
                }
            }

            //world perms
            Map<String, World> worlds = new HashMap<>();
            for (String world : foundworlds)
            {
                List<String> worldperms = new ArrayList<>();
                for (ValueEntry e : permdata)
                {
                    if (e.getServer() != null && e.getServer().equalsIgnoreCase(server) && e.getWorld() != null && e.getWorld().equalsIgnoreCase(world))
                    {
                        worldperms.add(Statics.toLower(e.getValue()));
                    }
                }

                World w = new World(Statics.toLower(world), worldperms, "", "", "");
                worlds.put(Statics.toLower(world), w);
            }

            Server s = new Server(Statics.toLower(server), serverperms, worlds, "", "", "");
            servers.put(Statics.toLower(server), s);
        }

        // display props for servers and worlds
        for (Map.Entry<String, Server> server : servers.entrySet())
        {
            String sdisplay = getFirstValue(mpe.getData("display"), server.getKey(), "");
            String sprefix = getFirstValue(mpe.getData("prefix"), server.getKey(), "");
            String ssuffix = getFirstValue(mpe.getData("suffix"), server.getKey(), "");
            server.getValue().setDisplay(sdisplay);
            server.getValue().setPrefix(sprefix);
            server.getValue().setSuffix(ssuffix);

            for (Map.Entry<String, World> world : server.getValue().getWorlds().entrySet())
            {
                String wdisplay = getFirstValue(mpe.getData("display"), server.getKey(), world.getKey(), "");
                String wprefix = getFirstValue(mpe.getData("prefix"), server.getKey(), world.getKey(), "");
                String wsuffix = getFirstValue(mpe.getData("suffix"), server.getKey(), world.getKey(), "");
                world.getValue().setDisplay(wdisplay);
                world.getValue().setPrefix(wprefix);
                world.getValue().setSuffix(wsuffix);
            }
        }

        Group g = new Group(mpe.getName(), inheritances, globalperms, servers, rank, weight, ladder, isdefault, display, prefix, suffix);
        return g;
    }

    @Override
    public User loadUser(String user)
    {
        MysqlPermEntity mpe = adapter.getUser(user);
        if (mpe.getName() == null)
        {
            return null;
        }

        List<String> sgroups = getValue(mpe.getData("groups"));
        String display = getFirstValue(mpe.getData("display"), "");
        String prefix = getFirstValue(mpe.getData("prefix"), "");
        String suffix = getFirstValue(mpe.getData("suffix"), "");

        List<Group> lgroups = new ArrayList<>();
        for (String s : sgroups)
        {
            Group g = BungeePerms.getInstance().getPermissionsManager().getGroup(s);
            if (g != null)
            {
                lgroups.add(g);
            }
        }

        //perms
        List<ValueEntry> permdata = mpe.getData("permissions");
        if (permdata == null)
        {
            permdata = new ArrayList<>();
        }
        List<String> globalperms = new ArrayList<>();
        List<String> foundservers = new ArrayList<>();

        //globalperms
        for (ValueEntry e : permdata)
        {
            //check for servers 
            if (e.getServer() != null)
            {
                if (!foundservers.contains(Statics.toLower(e.getServer())))
                {
                    foundservers.add(Statics.toLower(e.getServer()));
                }
            }

            //is global perm
            else
            {
                globalperms.add(Statics.toLower(e.getValue()));
            }
        }

        //server perms
        Map<String, Server> servers = new HashMap<>();
        for (String server : foundservers)
        {
            List<String> serverperms = new ArrayList<>();
            List<String> foundworlds = new ArrayList<>();
            for (ValueEntry e : permdata)
            {
                if (e.getServer() != null && e.getServer().equalsIgnoreCase(server))
                {
                    //check for worlds 
                    if (e.getWorld() != null)
                    {
                        if (!foundworlds.contains(Statics.toLower(e.getWorld())))
                        {
                            foundworlds.add(Statics.toLower(e.getWorld()));
                        }
                    }

                    //is server perm
                    else
                    {
                        serverperms.add(Statics.toLower(e.getValue()));
                    }
                }
            }

            //world perms
            Map<String, World> worlds = new HashMap<>();
            for (String world : foundworlds)
            {
                List<String> worldperms = new ArrayList<>();
                for (ValueEntry e : permdata)
                {
                    if (e.getServer() != null && e.getServer().equalsIgnoreCase(server) && e.getWorld() != null && e.getWorld().equalsIgnoreCase(world))
                    {
                        worldperms.add(Statics.toLower(e.getValue()));
                    }
                }

                World w = new World(Statics.toLower(world), worldperms, "", "", "");
                worlds.put(Statics.toLower(world), w);
            }

            Server s = new Server(Statics.toLower(server), serverperms, worlds, "", "", "");
            servers.put(Statics.toLower(server), s);
        }

        // display props for servers and worlds
        for (Map.Entry<String, Server> server : servers.entrySet())
        {
            String sdisplay = getFirstValue(mpe.getData("display"), server.getKey(), "");
            String sprefix = getFirstValue(mpe.getData("prefix"), server.getKey(), "");
            String ssuffix = getFirstValue(mpe.getData("suffix"), server.getKey(), "");
            server.getValue().setDisplay(sdisplay);
            server.getValue().setPrefix(sprefix);
            server.getValue().setSuffix(ssuffix);

            for (Map.Entry<String, World> world : server.getValue().getWorlds().entrySet())
            {
                String wdisplay = getFirstValue(mpe.getData("display"), server.getKey(), world.getKey(), "");
                String wprefix = getFirstValue(mpe.getData("prefix"), server.getKey(), world.getKey(), "");
                String wsuffix = getFirstValue(mpe.getData("suffix"), server.getKey(), world.getKey(), "");
                world.getValue().setDisplay(wdisplay);
                world.getValue().setPrefix(wprefix);
                world.getValue().setSuffix(wsuffix);
            }
        }

        UUID uuid = BungeePerms.getInstance().getPermissionsManager().getUUIDPlayerDB().getUUID(mpe.getName());
        User u = new User(mpe.getName(), uuid, lgroups, globalperms, servers, display, prefix, suffix);
        return u;
    }

    @Override
    public User loadUser(UUID user)
    {
        MysqlPermEntity mpe = adapter.getUser(user.toString());
        if (mpe.getName() == null)
        {
            return null;
        }

        List<String> sgroups = getValue(mpe.getData("groups"));
        String display = getFirstValue(mpe.getData("display"), "");
        String prefix = getFirstValue(mpe.getData("prefix"), "");
        String suffix = getFirstValue(mpe.getData("suffix"), "");

        List<Group> lgroups = new ArrayList<>();
        for (String s : sgroups)
        {
            Group g = BungeePerms.getInstance().getPermissionsManager().getGroup(s);
            if (g != null)
            {
                lgroups.add(g);
            }
        }

        //perms
        List<ValueEntry> permdata = mpe.getData("permissions");
        if (permdata == null)
        {
            permdata = new ArrayList<>();
        }
        List<String> globalperms = new ArrayList<>();
        List<String> foundservers = new ArrayList<>();

        //globalperms
        for (ValueEntry e : permdata)
        {
            //check for servers 
            if (e.getServer() != null)
            {
                if (!foundservers.contains(Statics.toLower(e.getServer())))
                {
                    foundservers.add(Statics.toLower(e.getServer()));
                }
            }

            //is global perm
            else
            {
                globalperms.add(Statics.toLower(e.getValue()));
            }
        }

        //server perms
        Map<String, Server> servers = new HashMap<>();
        for (String server : foundservers)
        {
            List<String> serverperms = new ArrayList<>();
            List<String> foundworlds = new ArrayList<>();
            for (ValueEntry e : permdata)
            {
                if (e.getServer() != null && e.getServer().equalsIgnoreCase(server))
                {
                    //check for worlds 
                    if (e.getWorld() != null)
                    {
                        if (!foundworlds.contains(Statics.toLower(e.getWorld())))
                        {
                            foundworlds.add(Statics.toLower(e.getWorld()));
                        }
                    }

                    //is server perm
                    else
                    {
                        serverperms.add(Statics.toLower(e.getValue()));
                    }
                }
            }

            //world perms
            Map<String, World> worlds = new HashMap<>();
            for (String world : foundworlds)
            {
                List<String> worldperms = new ArrayList<>();
                for (ValueEntry e : permdata)
                {
                    if (e.getServer() != null && e.getServer().equalsIgnoreCase(server) && e.getWorld() != null && e.getWorld().equalsIgnoreCase(world))
                    {
                        worldperms.add(Statics.toLower(e.getValue()));
                    }
                }

                World w = new World(Statics.toLower(world), worldperms, "", "", "");
                worlds.put(Statics.toLower(world), w);
            }

            Server s = new Server(Statics.toLower(server), serverperms, worlds, "", "", "");
            servers.put(Statics.toLower(server), s);
        }

        // display props for servers and worlds
        for (Map.Entry<String, Server> server : servers.entrySet())
        {
            String sdisplay = getFirstValue(mpe.getData("display"), server.getKey(), "");
            String sprefix = getFirstValue(mpe.getData("prefix"), server.getKey(), "");
            String ssuffix = getFirstValue(mpe.getData("suffix"), server.getKey(), "");
            server.getValue().setDisplay(sdisplay);
            server.getValue().setPrefix(sprefix);
            server.getValue().setSuffix(ssuffix);

            for (Map.Entry<String, World> world : server.getValue().getWorlds().entrySet())
            {
                String wdisplay = getFirstValue(mpe.getData("display"), server.getKey(), world.getKey(), "");
                String wprefix = getFirstValue(mpe.getData("prefix"), server.getKey(), world.getKey(), "");
                String wsuffix = getFirstValue(mpe.getData("suffix"), server.getKey(), world.getKey(), "");
                world.getValue().setDisplay(wdisplay);
                world.getValue().setPrefix(wprefix);
                world.getValue().setSuffix(wsuffix);
            }
        }

        String username = BungeePerms.getInstance().getPermissionsManager().getUUIDPlayerDB().getPlayerName(user);
        User u = new User(username, user, lgroups, globalperms, servers, display, prefix, suffix);
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
        adapter.saveData("version", EntityType.Version, "version", mkValueList(mkList(String.valueOf(version)), null, null));
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
        if (BungeePerms.getInstance().getConfig().isSaveAllUsers() ? true : !user.isNothingSpecial())
        {
            List<String> groups = new ArrayList<>();
            for (Group g : user.getGroups())
            {
                groups.add(g.getName());
            }
            saveUserGroups(user);
            saveUserPerms(user);
            saveUserDisplay(user, null, null);
            saveUserPrefix(user, null, null);
            saveUserSuffix(user, null, null);

            for (Map.Entry<String, Server> se : user.getServers().entrySet())
            {
                saveUserPerServerPerms(user, se.getKey());
                saveUserDisplay(user, se.getKey(), null);
                saveUserPrefix(user, se.getKey(), null);
                saveUserSuffix(user, se.getKey(), null);

                for (Map.Entry<String, World> we : se.getValue().getWorlds().entrySet())
                {
                    saveUserPerServerWorldPerms(user, se.getKey(), we.getKey());
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
        saveGroupInheritances(group);
        saveGroupPerms(group);
        saveGroupRank(group);
        saveGroupLadder(group);
        saveGroupDefault(group);
        saveGroupDisplay(group, null, null);
        saveGroupPrefix(group, null, null);
        saveGroupSuffix(group, null, null);

        for (Map.Entry<String, Server> se : group.getServers().entrySet())
        {
            saveGroupPerServerPerms(group, se.getKey());
            saveGroupDisplay(group, se.getKey(), null);
            saveGroupPrefix(group, se.getKey(), null);
            saveGroupSuffix(group, se.getKey(), null);

            for (Map.Entry<String, World> we : se.getValue().getWorlds().entrySet())
            {
                saveGroupPerServerWorldPerms(group, se.getKey(), we.getKey());
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
    public synchronized void saveUserGroups(User user)
    {
        List<String> savegroups = new ArrayList<>();
        for (Group g : user.getGroups())
        {
            savegroups.add(g.getName());
        }

        adapter.saveData(BungeePerms.getInstance().getConfig().isUseUUIDs() ? user.getUUID().toString() : user.getName(), EntityType.User, "groups", mkValueList(savegroups, null, null));
    }

    @Override
    public synchronized void saveUserPerms(User user)
    {
        adapter.saveData(BungeePerms.getInstance().getConfig().isUseUUIDs() ? user.getUUID().toString() : user.getName(), EntityType.User, "permissions", mkValueList(user.getExtraPerms(), null, null), null, null);
    }

    @Override
    public synchronized void saveUserPerServerPerms(User user, String server)
    {
        server = Statics.toLower(server);

        adapter.saveData(BungeePerms.getInstance().getConfig().isUseUUIDs() ? user.getUUID().toString() : user.getName(),
                         EntityType.User, "permissions",
                         mkValueList(user.getServer(server).getPerms(), server, null), server, null);
    }

    @Override
    public synchronized void saveUserPerServerWorldPerms(User user, String server, String world)
    {
        server = Statics.toLower(server);
        world = Statics.toLower(world);
        
        adapter.saveData(BungeePerms.getInstance().getConfig().isUseUUIDs() ? user.getUUID().toString() : user.getName(),
                         EntityType.User, "permissions",
                         mkValueList(user.getServer(server).getWorld(world).getPerms(), server, world), server, world);
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
    public synchronized void saveGroupPerms(Group group)
    {
        adapter.saveData(group.getName(), EntityType.Group, "permissions", mkValueList(group.getPerms(), null, null), null, null);
    }

    @Override
    public synchronized void saveGroupPerServerPerms(Group group, String server)
    {
        server = Statics.toLower(server);

        adapter.saveData(group.getName(), EntityType.Group, "permissions", mkValueList(group.getServer(server).getPerms(), server, null), server, null);
    }

    @Override
    public synchronized void saveGroupPerServerWorldPerms(Group group, String server, String world)
    {
        server = Statics.toLower(server);
        world = Statics.toLower(world);
        
        adapter.saveData(group.getName(), EntityType.Group, "permissions", mkValueList(group.getServer(server).getWorld(world).getPerms(), server, world), server, world);
    }

    @Override
    public synchronized void saveGroupInheritances(Group group)
    {
        adapter.saveData(group.getName(), EntityType.Group, "inheritances", mkValueList(group.getInheritances(), null, null));
    }

    @Override
    public synchronized void saveGroupLadder(Group group)
    {
        adapter.saveData(group.getName(), EntityType.Group, "ladder", mkList(new ValueEntry(group.getLadder(), null, null)));
    }

    @Override
    public synchronized void saveGroupRank(Group group)
    {
        adapter.saveData(group.getName(), EntityType.Group, "rank", mkList(new ValueEntry(String.valueOf(group.getRank()), null, null)));
    }

    @Override
    public synchronized void saveGroupWeight(Group group)
    {
        adapter.saveData(group.getName(), EntityType.Group, "weight", mkList(new ValueEntry(String.valueOf(group.getWeight()), null, null)));
    }

    @Override
    public synchronized void saveGroupDefault(Group group)
    {
        adapter.saveData(group.getName(), EntityType.Group, "default", mkList(new ValueEntry(String.valueOf(group.isDefault()), null, null)));
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

    //helper functions
    private List<String> getValue(List<ValueEntry> values)
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

    private String getFirstValue(List<ValueEntry> values, String def)
    {
        if (values == null || values.isEmpty())
        {
            return def;
        }
        for (ValueEntry e : values)
        {
            if (e.getServer() == null && e.getWorld() == null)
            {
                return e.getValue();
            }
        }
        return def;
    }

    private String getFirstValue(List<ValueEntry> values, String server, String def)
    {
        if (values == null || values.isEmpty())
        {
            return def;
        }
        for (ValueEntry e : values)
        {
            if (e.getServer() != null && e.getServer().equalsIgnoreCase(server) && e.getWorld() == null)
            {
                return e.getValue();
            }
        }
        return def;
    }

    private String getFirstValue(List<ValueEntry> values, String server, String world, String def)
    {
        if (values == null || values.isEmpty())
        {
            return def;
        }
        for (ValueEntry e : values)
        {
            if (e.getServer() != null && e.getServer().equalsIgnoreCase(server) && e.getWorld() != null && e.getWorld().equalsIgnoreCase(world))
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
        List<T> l = new ArrayList<>();
        for (T e : elements)
        {
            l.add(e);
        }
        return l;
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

    @Override
    public void reloadGroup(Group group)
    {
        MysqlPermEntity mpe = adapter.getGroup(group.getName());
        List<String> inheritances = getValue(mpe.getData("inheritances"));
        boolean isdefault = getFirstValue(mpe.getData("default"), false);
        int rank = getFirstValue(mpe.getData("rank"), 1000);
        int weight = getFirstValue(mpe.getData("weight"), 1000);
        String ladder = getFirstValue(mpe.getData("ladder"), "default");
        String display = getFirstValue(mpe.getData("display"), "");
        String prefix = getFirstValue(mpe.getData("prefix"), "");
        String suffix = getFirstValue(mpe.getData("suffix"), "");

        //perms
        List<ValueEntry> permdata = mpe.getData("permissions");
        if (permdata == null)
        {
            permdata = new ArrayList<>();
        }
        List<String> globalperms = new ArrayList<>();
        List<String> foundservers = new ArrayList<>();

        //globalperms
        for (ValueEntry e : permdata)
        {
            //check for servers 
            if (e.getServer() != null)
            {
                if (!foundservers.contains(Statics.toLower(e.getServer())))
                {
                    foundservers.add(Statics.toLower(e.getServer()));
                }
            }

            //is global perm
            else
            {
                globalperms.add(Statics.toLower(e.getValue()));
            }
        }

        //server perms
        Map<String, Server> servers = new HashMap<>();
        for (String server : foundservers)
        {
            List<String> serverperms = new ArrayList<>();
            List<String> foundworlds = new ArrayList<>();
            for (ValueEntry e : permdata)
            {
                if (e.getServer() != null && e.getServer().equalsIgnoreCase(server))
                {
                    //check for worlds 
                    if (e.getWorld() != null)
                    {
                        if (!foundworlds.contains(Statics.toLower(e.getWorld())))
                        {
                            foundworlds.add(Statics.toLower(e.getWorld()));
                        }
                    }

                    //is server perm
                    else
                    {
                        serverperms.add(Statics.toLower(e.getValue()));
                    }
                }
            }

            //world perms
            Map<String, World> worlds = new HashMap<>();
            for (String world : foundworlds)
            {
                List<String> worldperms = new ArrayList<>();
                for (ValueEntry e : permdata)
                {
                    if (e.getServer() != null && e.getServer().equalsIgnoreCase(server) && e.getWorld() != null && e.getWorld().equalsIgnoreCase(world))
                    {
                        worldperms.add(Statics.toLower(e.getValue()));
                    }
                }

                World w = new World(Statics.toLower(world), worldperms, null, null, null);
                worlds.put(Statics.toLower(world), w);
            }

            Server s = new Server(Statics.toLower(server), serverperms, worlds, null, null, null);
            servers.put(Statics.toLower(server), s);
        }

        // display props for servers and worlds
        for (Map.Entry<String, Server> server : servers.entrySet())
        {
            String sdisplay = getFirstValue(mpe.getData("display"), server.getKey(), "");
            String sprefix = getFirstValue(mpe.getData("prefix"), server.getKey(), "");
            String ssuffix = getFirstValue(mpe.getData("suffix"), server.getKey(), "");
            server.getValue().setDisplay(sdisplay);
            server.getValue().setPrefix(sprefix);
            server.getValue().setSuffix(ssuffix);

            for (Map.Entry<String, World> world : server.getValue().getWorlds().entrySet())
            {
                String wdisplay = getFirstValue(mpe.getData("display"), server.getKey(), world.getKey(), "");
                String wprefix = getFirstValue(mpe.getData("prefix"), server.getKey(), world.getKey(), "");
                String wsuffix = getFirstValue(mpe.getData("suffix"), server.getKey(), world.getKey(), "");
                world.getValue().setDisplay(wdisplay);
                world.getValue().setPrefix(wprefix);
                world.getValue().setSuffix(wsuffix);
            }
        }

        group.setInheritances(inheritances);
        group.setPerms(globalperms);
        group.setIsdefault(isdefault);
        group.setRank(rank);
        group.setWeight(weight);
        group.setLadder(ladder);
        group.setDisplay(display);
        group.setPrefix(prefix);
        group.setSuffix(suffix);
        group.setServers(servers);
    }

    @Override
    public void reloadUser(User user)
    {
        MysqlPermEntity mpe = adapter.getUser(config.isUseUUIDs() ? user.getUUID().toString() : user.getName());

        List<String> sgroups = getValue(mpe.getData("groups"));
        String display = getFirstValue(mpe.getData("display"), "");
        String prefix = getFirstValue(mpe.getData("prefix"), "");
        String suffix = getFirstValue(mpe.getData("suffix"), "");

        List<Group> lgroups = new ArrayList<>();
        for (String s : sgroups)
        {
            Group g = BungeePerms.getInstance().getPermissionsManager().getGroup(s);
            if (g != null)
            {
                lgroups.add(g);
            }
        }

        //perms
        List<ValueEntry> permdata = mpe.getData("permissions");
        if (permdata == null)
        {
            permdata = new ArrayList<>();
        }
        List<String> globalperms = new ArrayList<>();
        List<String> foundservers = new ArrayList<>();

        //globalperms
        for (ValueEntry e : permdata)
        {
            //check for servers 
            if (e.getServer() != null)
            {
                if (!foundservers.contains(Statics.toLower(e.getServer())))
                {
                    foundservers.add(Statics.toLower(e.getServer()));
                }
            }

            //is global perm
            else
            {
                globalperms.add(Statics.toLower(e.getValue()));
            }
        }

        //server perms
        Map<String, Server> servers = new HashMap<>();
        for (String server : foundservers)
        {
            List<String> serverperms = new ArrayList<>();
            List<String> foundworlds = new ArrayList<>();
            for (ValueEntry e : permdata)
            {
                if (e.getServer() != null && e.getServer().equalsIgnoreCase(server))
                {
                    //check for worlds 
                    if (e.getWorld() != null)
                    {
                        if (!foundworlds.contains(Statics.toLower(e.getWorld())))
                        {
                            foundworlds.add(Statics.toLower(e.getWorld()));
                        }
                    }

                    //is server perm
                    else
                    {
                        serverperms.add(Statics.toLower(e.getValue()));
                    }
                }
            }

            //world perms
            Map<String, World> worlds = new HashMap<>();
            for (String world : foundworlds)
            {
                List<String> worldperms = new ArrayList<>();
                for (ValueEntry e : permdata)
                {
                    if (e.getServer() != null && e.getServer().equalsIgnoreCase(server) && e.getWorld() != null && e.getWorld().equalsIgnoreCase(world))
                    {
                        worldperms.add(e.getValue());
                    }
                }

                World w = new World(Statics.toLower(world), worldperms, null, null, null);
                worlds.put(Statics.toLower(world), w);
            }

            Server s = new Server(Statics.toLower(server), serverperms, worlds, null, null, null);
            servers.put(Statics.toLower(server), s);
        }

        // display props for servers and worlds
        for (Map.Entry<String, Server> server : servers.entrySet())
        {
            String sdisplay = getFirstValue(mpe.getData("display"), server.getKey(), "");
            String sprefix = getFirstValue(mpe.getData("prefix"), server.getKey(), "");
            String ssuffix = getFirstValue(mpe.getData("suffix"), server.getKey(), "");
            server.getValue().setDisplay(sdisplay);
            server.getValue().setPrefix(sprefix);
            server.getValue().setSuffix(ssuffix);

            for (Map.Entry<String, World> world : server.getValue().getWorlds().entrySet())
            {
                String wdisplay = getFirstValue(mpe.getData("display"), server.getKey(), world.getKey(), "");
                String wprefix = getFirstValue(mpe.getData("prefix"), server.getKey(), world.getKey(), "");
                String wsuffix = getFirstValue(mpe.getData("suffix"), server.getKey(), world.getKey(), "");
                world.getValue().setDisplay(wdisplay);
                world.getValue().setPrefix(wprefix);
                world.getValue().setSuffix(wsuffix);
            }
        }

        user.setGroups(lgroups);
        user.setExtraPerms(globalperms);
        user.setDisplay(display);
        user.setPrefix(prefix);
        user.setSuffix(suffix);
        user.setServers(servers);
    }
}
