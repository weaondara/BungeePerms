package net.alpenblock.bungeeperms.io;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.alpenblock.bungeeperms.BPConfig;
import net.alpenblock.bungeeperms.BungeePerms;
import net.alpenblock.bungeeperms.Config;
import net.alpenblock.bungeeperms.Group;
import net.alpenblock.bungeeperms.Server;
import net.alpenblock.bungeeperms.User;
import net.alpenblock.bungeeperms.World;
import net.alpenblock.bungeeperms.platform.PlatformPlugin;

public class YAMLBackEnd implements BackEnd
{

    private final String permspath;
    private Config permsconf;

    private final PlatformPlugin plugin;
    private final BPConfig config;

    public YAMLBackEnd()
    {
        plugin = BungeePerms.getInstance().getPlugin();
        config = BungeePerms.getInstance().getConfig();

        permspath = "/permissions.yml";

        checkPermFile();

        permsconf = new Config(plugin, permspath);
    }

    @Override
    public BackEndType getType()
    {
        return BackEndType.YAML;
    }

    @Override
    public void load()
    {
        //load from table
        permsconf.load();
    }

    @Override
    public List<Group> loadGroups()
    {
        List<Group> ret = new ArrayList<>();

        List<String> groups = permsconf.getSubNodes("groups");
        for (String g : groups)
        {
            ret.add(loadGroup(g));
        }
        Collections.sort(ret);

        return ret;
    }

    @Override
    public List<User> loadUsers()
    {
        List<User> ret = new ArrayList<>();

        List<String> users = permsconf.getSubNodes("users");
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
        List<String> inheritances = permsconf.getListString("groups." + group + ".inheritances", new ArrayList<String>());
        List<String> permissions = permsconf.getListString("groups." + group + ".permissions", new ArrayList<String>());
        boolean isdefault = permsconf.getBoolean("groups." + group + ".default", false);
        int rank = permsconf.getInt("groups." + group + ".rank", 1000);
        int weight = permsconf.getInt("groups." + group + ".weight", 1000);
        String ladder = permsconf.getString("groups." + group + ".ladder", "default");
        String display = permsconf.getString("groups." + group + ".display", "");
        String prefix = permsconf.getString("groups." + group + ".prefix", "");
        String suffix = permsconf.getString("groups." + group + ".suffix", "");

        //per server perms
        Map<String, Server> servers = new HashMap<>();
        for (String server : permsconf.getSubNodes("groups." + group + ".servers"))
        {
            List<String> serverperms = permsconf.getListString("groups." + group + ".servers." + server + ".permissions", new ArrayList<String>());
            String sdisplay = permsconf.getString("groups." + group + ".servers." + server + ".display", "");
            String sprefix = permsconf.getString("groups." + group + ".servers." + server + ".prefix", "");
            String ssuffix = permsconf.getString("groups." + group + ".servers." + server + ".suffix", "");

            //per server world perms
            Map<String, World> worlds = new HashMap<>();
            for (String world : permsconf.getSubNodes("groups." + group + ".servers." + server + ".worlds"))
            {
                List<String> worldperms = permsconf.getListString("groups." + group + ".servers." + server + ".worlds." + world + ".permissions", new ArrayList<String>());
                String wdisplay = permsconf.getString("groups." + group + ".servers." + server + ".worlds." + world + ".display", "");
                String wprefix = permsconf.getString("groups." + group + ".servers." + server + ".worlds." + world + ".prefix", "");
                String wsuffix = permsconf.getString("groups." + group + ".servers." + server + ".worlds." + world + ".suffix", "");

                World w = new World(world, worldperms, wdisplay, wprefix, wsuffix);
                worlds.put(world, w);
            }

            servers.put(server, new Server(server, serverperms, worlds, sdisplay, sprefix, ssuffix));
        }

        Group g = new Group(group, inheritances, permissions, servers, rank, weight, ladder, isdefault, display, prefix, suffix);
        return g;
    }

    @Override
    public User loadUser(String user)
    {
        if (!permsconf.keyExists("users." + user))
        {
            return null;
        }

        //load user from database
        List<String> sgroups = permsconf.getListString("users." + user + ".groups", new ArrayList<String>());
        List<Group> lgroups = new ArrayList<>();
        for (String s : sgroups)
        {
            Group g = BungeePerms.getInstance().getPermissionsManager().getGroup(s);
            if (g != null)
            {
                lgroups.add(g);
            }
        }
        List<String> extrapermissions = permsconf.getListString("users." + user + ".permissions", new ArrayList<String>());

        Map<String, List<String>> serverperms = new HashMap<>();
        Map<String, Map<String, List<String>>> serverworldperms = new HashMap<>();
        for (String server : permsconf.getSubNodes("users." + user + ".servers"))
        {
            //per server perms
            serverperms.put(server, permsconf.getListString("users." + user + ".servers." + server + ".permissions", new ArrayList<String>()));

            //per server world perms
            Map<String, List<String>> worldperms = new HashMap<>();
            for (String world : permsconf.getSubNodes("users." + user + ".servers." + server + ".worlds"))
            {
                worldperms.put(world, permsconf.getListString("users." + user + ".servers." + server + ".worlds." + world + ".permissions", new ArrayList<String>()));
            }
            serverworldperms.put(server, worldperms);
        }

        UUID uuid = BungeePerms.getInstance().getPermissionsManager().getUUIDPlayerDB().getUUID(user);
        User u = new User(user, uuid, lgroups, extrapermissions, serverperms, serverworldperms);
        return u;
    }

    @Override
    public User loadUser(UUID user)
    {
        if (!permsconf.keyExists("users." + user))
        {
            return null;
        }

        //load user from database
        List<String> sgroups = permsconf.getListString("users." + user + ".groups", new ArrayList<String>());
        List<Group> lgroups = new ArrayList<>();
        for (String s : sgroups)
        {
            Group g = BungeePerms.getInstance().getPermissionsManager().getGroup(s);
            if (g != null)
            {
                lgroups.add(g);
            }
        }
        List<String> extrapermissions = permsconf.getListString("users." + user + ".permissions", new ArrayList<String>());

        Map<String, List<String>> serverperms = new HashMap<>();
        Map<String, Map<String, List<String>>> serverworldperms = new HashMap<>();
        for (String server : permsconf.getSubNodes("users." + user + ".servers"))
        {
            //per server perms
            serverperms.put(server, permsconf.getListString("users." + user + ".servers." + server + ".permissions", new ArrayList<String>()));

            //per server world perms
            Map<String, List<String>> worldperms = new HashMap<>();
            for (String world : permsconf.getSubNodes("users." + user + ".servers." + server + ".worlds"))
            {
                worldperms.put(world, permsconf.getListString("users." + user + ".servers." + server + ".worlds." + world + ".permissions", new ArrayList<String>()));
            }
            serverworldperms.put(server, worldperms);
        }

        String username = BungeePerms.getInstance().getPermissionsManager().getUUIDPlayerDB().getPlayerName(user);
        User u = new User(username, user, lgroups, extrapermissions, serverperms, serverworldperms);
        return u;
    }

    @Override
    public int loadVersion()
    {
        return permsconf.getInt("version", 1);
    }

    @Override
    public void saveVersion(int version, boolean savetodisk)
    {
        permsconf.setInt("version", version);

        if (savetodisk)
        {
            permsconf.save();
        }
    }

    @Override
    public boolean isUserInDatabase(User user)
    {
        return permsconf.keyExists("users." + (BungeePerms.getInstance().getConfig().isUseUUIDs() ? user.getUUID().toString() : user.getName()));
    }

    private void checkPermFile()
    {
        File f = new File(plugin.getPluginFolder(), permspath);
        if (!f.exists() | !f.isFile())
        {
            BungeePerms.getLogger().info("no permissions file found !!!");
        }
    }

    @Override
    public List<String> getRegisteredUsers()
    {
        return permsconf.getSubNodes("users");
    }

    @Override
    public List<String> getGroupUsers(Group group)
    {
        List<String> users = new ArrayList<>();

        for (String user : permsconf.getSubNodes("users"))
        {
            if (permsconf.getListString("users." + user + ".groups", new ArrayList<String>()).contains(group.getName()))
            {
                users.add(user);
            }
        }

        return users;
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
            permsconf.setListString("users." + (BungeePerms.getInstance().getConfig().isUseUUIDs() ? user.getUUID().toString() : user.getName()) + ".groups", groups);
            permsconf.setListString("users." + (BungeePerms.getInstance().getConfig().isUseUUIDs() ? user.getUUID().toString() : user.getName()) + ".permissions", user.getExtraPerms());

            for (Map.Entry<String, List<String>> se : user.getServerPerms().entrySet())
            {
                permsconf.setListString("users." + (BungeePerms.getInstance().getConfig().isUseUUIDs() ? user.getUUID().toString() : user.getName()) + ".servers." + se.getKey() + ".permissions", se.getValue());
            }
            for (Map.Entry<String, Map<String, List<String>>> swe : user.getServerWorldPerms().entrySet())
            {
                for (Map.Entry<String, List<String>> we : swe.getValue().entrySet())
                {
                    permsconf.getListString("users." + (BungeePerms.getInstance().getConfig().isUseUUIDs() ? user.getUUID().toString() : user.getName()) + ".servers." + swe.getKey() + ".worlds." + we.getKey() + ".permissions", we.getValue());
                }
            }

            if (savetodisk)
            {
                permsconf.save();
            }
        }
    }

    @Override
    public synchronized void saveGroup(Group group, boolean savetodisk)
    {
        permsconf.setListString("groups." + group.getName() + ".inheritances", group.getInheritances());
        permsconf.setListString("groups." + group.getName() + ".permissions", group.getPerms());
        permsconf.setInt("groups." + group.getName() + ".rank", group.getRank());
        permsconf.setString("groups." + group.getName() + ".ladder", group.getLadder());
        permsconf.setBool("groups." + group.getName() + ".default", group.isDefault());
        permsconf.setString("groups." + group.getName() + ".display", group.getDisplay());
        permsconf.setString("groups." + group.getName() + ".prefix", group.getPrefix());
        permsconf.setString("groups." + group.getName() + ".suffix", group.getSuffix());

        for (Map.Entry<String, Server> se : group.getServers().entrySet())
        {
            permsconf.setListString("groups." + group.getName() + ".servers." + se.getKey() + ".permissions", se.getValue().getPerms());
            permsconf.setString("groups." + group.getName() + ".servers." + se.getKey() + ".display", se.getValue().getDisplay());
            permsconf.setString("groups." + group.getName() + ".servers." + se.getKey() + ".prefix", se.getValue().getPrefix());
            permsconf.setString("groups." + group.getName() + ".servers." + se.getKey() + ".suffix", se.getValue().getSuffix());

            for (Map.Entry<String, World> we : se.getValue().getWorlds().entrySet())
            {
                permsconf.setListString("groups." + group.getName() + ".servers." + se.getKey() + ".worlds." + we.getKey() + ".permissions", we.getValue().getPerms());
                permsconf.setString("groups." + group.getName() + ".servers." + se.getKey() + ".worlds." + we.getKey() + ".display", we.getValue().getDisplay());
                permsconf.setString("groups." + group.getName() + ".servers." + se.getKey() + ".worlds." + we.getKey() + ".prefix", we.getValue().getPrefix());
                permsconf.setString("groups." + group.getName() + ".servers." + se.getKey() + ".worlds." + we.getKey() + ".suffix", we.getValue().getSuffix());
            }
        }

        if (savetodisk)
        {
            permsconf.save();
        }
    }

    @Override
    public synchronized void deleteUser(User user)
    {
        permsconf.deleteNode("users." + (BungeePerms.getInstance().getConfig().isUseUUIDs() ? user.getUUID().toString() : user.getName()));
    }

    @Override
    public synchronized void deleteGroup(Group group)
    {
        permsconf.deleteNode("groups." + group.getName());
    }

    @Override
    public synchronized void saveUserGroups(User user)
    {
        List<String> savegroups = new ArrayList<>();
        for (Group g : user.getGroups())
        {
            savegroups.add(g.getName());
        }

        permsconf.setListStringAndSave("users." + (BungeePerms.getInstance().getConfig().isUseUUIDs() ? user.getUUID().toString() : user.getName()) + ".groups", savegroups);
    }

    @Override
    public synchronized void saveUserPerms(User user)
    {
        permsconf.setListStringAndSave("users." + (BungeePerms.getInstance().getConfig().isUseUUIDs() ? user.getUUID().toString() : user.getName()) + ".permissions", user.getExtraPerms());
    }

    @Override
    public synchronized void saveUserPerServerPerms(User user, String server)
    {
        permsconf.setListStringAndSave("users." + (BungeePerms.getInstance().getConfig().isUseUUIDs() ? user.getUUID().toString() : user.getName()) + ".servers." + server + ".permissions", user.getServerPerms().get(server));
    }

    @Override
    public synchronized void saveUserPerServerWorldPerms(User user, String server, String world)
    {
        permsconf.setListStringAndSave("users." + (BungeePerms.getInstance().getConfig().isUseUUIDs() ? user.getUUID().toString() : user.getName()) + ".servers." + server + ".worlds." + world + ".permissions", user.getServerWorldPerms().get(server).get(world));
    }

    @Override
    public synchronized void saveGroupPerms(Group group)
    {
        permsconf.setListStringAndSave("groups." + group.getName() + ".permissions", group.getPerms());
    }

    @Override
    public synchronized void saveGroupPerServerPerms(Group group, String server)
    {
        permsconf.setListStringAndSave("groups." + group.getName() + ".servers." + server + ".permissions", group.getServers().get(server).getPerms());
    }

    @Override
    public synchronized void saveGroupPerServerWorldPerms(Group group, String server, String world)
    {
        permsconf.setListStringAndSave("groups." + group.getName() + ".servers." + server + ".worlds." + world + ".permissions", group.getServers().get(server).getWorlds().get(world).getPerms());
    }

    @Override
    public synchronized void saveGroupInheritances(Group group)
    {
        permsconf.setListStringAndSave("groups." + group.getName() + ".inheritances", group.getInheritances());
    }

    @Override
    public synchronized void saveGroupLadder(Group group)
    {
        permsconf.setStringAndSave("groups." + group.getName() + ".ladder", group.getLadder());
    }

    @Override
    public synchronized void saveGroupRank(Group group)
    {
        permsconf.setIntAndSave("groups." + group.getName() + ".rank", group.getRank());
    }

    @Override
    public synchronized void saveGroupWeight(Group group)
    {
        permsconf.setIntAndSave("groups." + group.getName() + ".weight", group.getWeight());
    }

    @Override
    public synchronized void saveGroupDefault(Group group)
    {
        permsconf.setBoolAndSave("groups." + group.getName() + ".default", group.isDefault());
    }

    @Override
    public synchronized void saveGroupDisplay(Group group, String server, String world)
    {
        permsconf.setStringAndSave("groups." + group.getName() + (server != null ? ".servers." + server + (world != null ? ".worlds." + world : "") : "") + ".display", group.getDisplay());
    }

    @Override
    public synchronized void saveGroupPrefix(Group group, String server, String world)
    {
        permsconf.setStringAndSave("groups." + group.getName() + (server != null ? ".servers." + server + (world != null ? ".worlds." + world : "") : "") + ".prefix", group.getPrefix());
    }

    @Override
    public synchronized void saveGroupSuffix(Group group, String server, String world)
    {
        permsconf.setStringAndSave("groups." + group.getName() + (server != null ? ".servers." + server + (world != null ? ".worlds." + world : "") : "") + ".suffix", group.getSuffix());
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

        permsconf.save();
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
                        && BungeePerms.getInstance().getPlugin().getPlayer(u.getName()) == null
                        && BungeePerms.getInstance().getPlugin().getPlayer(u.getUUID()) == null)
                {
                    deleted++;
                    continue;
                }
            }

            //player has to be saved
            saveUser(users.get(i), false);
        }
        saveVersion(version, false);

        permsconf.save();

        return deleted;
    }

    @Override
    public void clearDatabase()
    {
        new File(BungeePerms.getInstance().getPlugin().getPluginFolder() + permspath).delete();
        permsconf = new Config(BungeePerms.getInstance().getPlugin(), permspath);
    }

    @Override
    public void reloadGroup(Group group)
    {
        permsconf.load();

        //load group from database
        List<String> inheritances = permsconf.getListString("groups." + group.getName() + ".inheritances", new ArrayList<String>());
        List<String> permissions = permsconf.getListString("groups." + group.getName() + ".permissions", new ArrayList<String>());
        boolean isdefault = permsconf.getBoolean("groups." + group.getName() + ".default", false);
        int rank = permsconf.getInt("groups." + group.getName() + ".rank", 1000);
        int weight = permsconf.getInt("groups." + group.getName() + ".weight", 1000);
        String ladder = permsconf.getString("groups." + group.getName() + ".ladder", "default");
        String display = permsconf.getString("groups." + group.getName() + ".display", "");
        String prefix = permsconf.getString("groups." + group.getName() + ".prefix", "");
        String suffix = permsconf.getString("groups." + group.getName() + ".suffix", "");

        //per server perms
        Map<String, Server> servers = new HashMap<>();
        for (String server : permsconf.getSubNodes("groups." + group.getName() + ".servers"))
        {
            List<String> serverperms = permsconf.getListString("groups." + group.getName() + ".servers." + server + ".permissions", new ArrayList<String>());
            String sdisplay = permsconf.getString("groups." + group.getName() + ".servers." + server + ".display", "");
            String sprefix = permsconf.getString("groups." + group.getName() + ".servers." + server + ".prefix", "");
            String ssuffix = permsconf.getString("groups." + group.getName() + ".servers." + server + ".suffix", "");

            //per server world perms
            Map<String, World> worlds = new HashMap<>();
            for (String world : permsconf.getSubNodes("groups." + group.getName() + ".servers." + server + ".worlds"))
            {
                List<String> worldperms = permsconf.getListString("groups." + group.getName() + ".servers." + server + ".worlds." + world + ".permissions", new ArrayList<String>());
                String wdisplay = permsconf.getString("groups." + group.getName() + ".servers." + server + ".worlds." + world + ".display", "");
                String wprefix = permsconf.getString("groups." + group.getName() + ".servers." + server + ".worlds." + world + ".prefix", "");
                String wsuffix = permsconf.getString("groups." + group.getName() + ".servers." + server + ".worlds." + world + ".suffix", "");

                World w = new World(world, worldperms, wdisplay, wprefix, wsuffix);
                worlds.put(world, w);
            }

            servers.put(server, new Server(server, serverperms, worlds, sdisplay, sprefix, ssuffix));
        }

        group.setInheritances(inheritances);
        group.setPerms(permissions);
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
        permsconf.load();

        //load user from database
        List<String> sgroups = permsconf.getListString("users." + (config.isUseUUIDs() ? user.getUUID().toString() : user.getName()) + ".groups", new ArrayList<String>());
        List<Group> lgroups = new ArrayList<>();
        for (String s : sgroups)
        {
            Group g = BungeePerms.getInstance().getPermissionsManager().getGroup(s);
            if (g != null)
            {
                lgroups.add(g);
            }
        }
        List<String> extrapermissions = permsconf.getListString("users." + (config.isUseUUIDs() ? user.getUUID().toString() : user.getName()) + ".permissions", new ArrayList<String>());

        Map<String, List<String>> serverperms = new HashMap<>();
        Map<String, Map<String, List<String>>> serverworldperms = new HashMap<>();
        for (String server : permsconf.getSubNodes("users." + (config.isUseUUIDs() ? user.getUUID().toString() : user.getName()) + ".servers"))
        {
            //per server perms
            serverperms.put(server, permsconf.getListString("users." + (config.isUseUUIDs() ? user.getUUID().toString() : user.getName()) + ".servers." + server + ".permissions", new ArrayList<String>()));

            //per server world perms
            Map<String, List<String>> worldperms = new HashMap<>();
            for (String world : permsconf.getSubNodes("users." + (config.isUseUUIDs() ? user.getUUID().toString() : user.getName()) + ".servers." + server + ".worlds"))
            {
                worldperms.put(world, permsconf.getListString("users." + (config.isUseUUIDs() ? user.getUUID().toString() : user.getName()) + ".servers." + server + ".worlds." + world + ".permissions", new ArrayList<String>()));
            }
            serverworldperms.put(server, worldperms);
        }

        user.setGroups(lgroups);
        user.setExtraPerms(extrapermissions);
        user.setServerPerms(serverperms);
        user.setServerWorldPerms(serverworldperms);
    }
}
