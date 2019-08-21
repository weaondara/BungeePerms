package net.alpenblock.bungeeperms.io;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.SneakyThrows;
import net.alpenblock.bungeeperms.BPConfig;
import net.alpenblock.bungeeperms.BungeePerms;
import net.alpenblock.bungeeperms.Config;
import net.alpenblock.bungeeperms.Group;
import net.alpenblock.bungeeperms.Lang;
import net.alpenblock.bungeeperms.Server;
import net.alpenblock.bungeeperms.Statics;
import net.alpenblock.bungeeperms.TimedValue;
import net.alpenblock.bungeeperms.User;
import net.alpenblock.bungeeperms.World;
import net.alpenblock.bungeeperms.config.YamlConfiguration;
import net.alpenblock.bungeeperms.platform.PlatformPlugin;

public class YAMLBackEnd implements BackEnd
{

    private final static String permspathgroups = "/permissions.groups.yml";
    private final static String permspathusers = "/permissions.users.yml";
    private Config permsconfgroups;
    private Config permsconfusers;

    private final PlatformPlugin plugin;
    private final BPConfig config;

    @SneakyThrows
    public YAMLBackEnd()
    {
        plugin = BungeePerms.getInstance().getPlugin();
        config = BungeePerms.getInstance().getConfig();

        //migrate
        File oldfile = new File(plugin.getPluginFolder(), "/permissions.yml");
        if (oldfile.isFile())
        {
            YamlConfiguration g = YamlConfiguration.loadConfiguration(oldfile);
            YamlConfiguration u = YamlConfiguration.loadConfiguration(oldfile);
            g.set("users", null);
            u.set("groups", null);
            new File(plugin.getPluginFolder(), permspathgroups).createNewFile();
            new File(plugin.getPluginFolder(), permspathusers).createNewFile();
            g.save(new File(plugin.getPluginFolder(), permspathgroups));
            u.save(new File(plugin.getPluginFolder(), permspathusers));
            oldfile.renameTo(new File(plugin.getPluginFolder(), "/permissions.yml.replaced"));
        }

        checkPermFiles();

        permsconfgroups = new Config(plugin, permspathgroups);
        permsconfusers = new Config(plugin, permspathusers);
    }

    @Override
    public BackEndType getType()
    {
        return BackEndType.YAML;
    }

    @Override
    public void load()
    {
        //load from file
        permsconfgroups.load();
        permsconfusers.load();
    }

    @Override
    public List<Group> loadGroups()
    {
        List<Group> ret = new ArrayList<>();

        List<String> groups = permsconfgroups.getSubNodes("groups");
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

        List<String> users = permsconfusers.getSubNodes("users");
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
        permsconfgroups.setAutoSavingEnabled(false);

        List<String> inheritances = permsconfgroups.getListString("groups." + group + ".inheritances", new ArrayList<String>());
        List<TimedValue<String>> timedinheritances = getTimed(permsconfgroups, "groups." + group + ".timedinheritances");
        List<String> permissions = permsconfgroups.getListString("groups." + group + ".permissions", new ArrayList<String>());
        List<TimedValue<String>> timedperms = getTimed(permsconfgroups, "groups." + group + ".timedpermissions");
        boolean isdefault = permsconfgroups.getBoolean("groups." + group + ".default", false);
        int rank = permsconfgroups.getInt("groups." + group + ".rank", 1000);
        int weight = permsconfgroups.getInt("groups." + group + ".weight", 1000);
        String ladder = permsconfgroups.getString("groups." + group + ".ladder", "default");
        String display = permsconfgroups.getString("groups." + group + ".display", null);
        String prefix = permsconfgroups.getString("groups." + group + ".prefix", null);
        String suffix = permsconfgroups.getString("groups." + group + ".suffix", null);

        //per server perms
        Map<String, Server> servers = new HashMap<>();
        for (String server : permsconfgroups.getSubNodes("groups." + group + ".servers"))
        {
            List<String> serverperms = permsconfgroups.getListString("groups." + group + ".servers." + server + ".permissions", new ArrayList<String>());
            List<TimedValue<String>> stimedperms = getTimed(permsconfgroups, "groups." + group + ".servers." + server + ".timedpermissions");
            String sdisplay = permsconfgroups.getString("groups." + group + ".servers." + server + ".display", null);
            String sprefix = permsconfgroups.getString("groups." + group + ".servers." + server + ".prefix", null);
            String ssuffix = permsconfgroups.getString("groups." + group + ".servers." + server + ".suffix", null);

            //per server world perms
            Map<String, World> worlds = new HashMap<>();
            for (String world : permsconfgroups.getSubNodes("groups." + group + ".servers." + server + ".worlds"))
            {
                List<String> worldperms = permsconfgroups.getListString("groups." + group + ".servers." + server + ".worlds." + world + ".permissions", new ArrayList<String>());
                List<TimedValue<String>> wtimedperms = getTimed(permsconfgroups, "groups." + group + ".servers." + server + ".worlds." + world + ".timedpermissions");
                String wdisplay = permsconfgroups.getString("groups." + group + ".servers." + server + ".worlds." + world + ".display", null);
                String wprefix = permsconfgroups.getString("groups." + group + ".servers." + server + ".worlds." + world + ".prefix", null);
                String wsuffix = permsconfgroups.getString("groups." + group + ".servers." + server + ".worlds." + world + ".suffix", null);

                World w = new World(Statics.toLower(world), worldperms, wtimedperms, wdisplay, wprefix, wsuffix);
                worlds.put(Statics.toLower(world), w);
            }

            servers.put(Statics.toLower(server), new Server(Statics.toLower(server), serverperms, stimedperms, worlds, sdisplay, sprefix, ssuffix));
        }

        permsconfgroups.setAutoSavingEnabled(true);

        Group g = new Group(group, inheritances, timedinheritances, permissions, timedperms, servers, rank, weight, ladder, isdefault, display, prefix, suffix);
        g.invalidateCache();
        return g;
    }

    @Override
    public User loadUser(String user)
    {
        User u = loadUser0(user);
        if (u == null)
            return null;
        UUID uuid = BungeePerms.getInstance().getPermissionsManager().getUUIDPlayerDB().getUUID(user);
        u.setName(user);
        u.setUUID(uuid);
        u.invalidateCache();
        return u;
    }

    @Override
    public User loadUser(UUID user)
    {
        User u = loadUser0(user.toString());
        if (u == null)
            return null;
        String username = BungeePerms.getInstance().getPermissionsManager().getUUIDPlayerDB().getPlayerName(user);
        u.setName(username);
        u.setUUID(user);
        u.invalidateCache();
        return u;
    }

    private User loadUser0(String user)
    {
        if (!permsconfusers.keyExists("users." + user))
            return null;

        permsconfusers.setAutoSavingEnabled(false);

        //load user from database
        List<String> groups = permsconfusers.getListString("users." + user + ".groups", new ArrayList<String>());
        List<TimedValue<String>> timedgroups = getTimed(permsconfusers, "users." + user + ".timedgroups");
        List<String> perms = permsconfusers.getListString("users." + user + ".permissions", new ArrayList<String>());
        List<TimedValue<String>> timedperms = getTimed(permsconfusers, "users." + user + ".timedpermissions");
        String display = permsconfusers.getString("users." + user + ".display", null);
        String prefix = permsconfusers.getString("users." + user + ".prefix", null);
        String suffix = permsconfusers.getString("users." + user + ".suffix", null);

        //per server perms
        Map<String, Server> servers = new HashMap<>();
        for (String server : permsconfusers.getSubNodes("users." + user + ".servers"))
        {
            List<String> serverperms = permsconfusers.getListString("users." + user + ".servers." + server + ".permissions", new ArrayList<String>());
            List<TimedValue<String>> stimedperms = getTimed(permsconfusers, "users." + user + ".servers." + server + ".timedpermissions");
            String sdisplay = permsconfusers.getString("users." + user + ".servers." + server + ".display", null);
            String sprefix = permsconfusers.getString("users." + user + ".servers." + server + ".prefix", null);
            String ssuffix = permsconfusers.getString("users." + user + ".servers." + server + ".suffix", null);

            //per server world perms
            Map<String, World> worlds = new HashMap<>();
            for (String world : permsconfusers.getSubNodes("users." + user + ".servers." + server + ".worlds"))
            {
                List<String> worldperms = permsconfusers.getListString("users." + user + ".servers." + server + ".worlds." + world + ".permissions", new ArrayList<String>());
                List<TimedValue<String>> wtimedperms = getTimed(permsconfusers, "users." + user + ".servers." + server + ".worlds." + world + ".timedpermissions");
                String wdisplay = permsconfusers.getString("users." + user + ".servers." + server + ".worlds." + world + ".display", null);
                String wprefix = permsconfusers.getString("users." + user + ".servers." + server + ".worlds." + world + ".prefix", null);
                String wsuffix = permsconfusers.getString("users." + user + ".servers." + server + ".worlds." + world + ".suffix", null);

                World w = new World(Statics.toLower(world), worldperms, wtimedperms, wdisplay, wprefix, wsuffix);
                worlds.put(Statics.toLower(world), w);
            }

            servers.put(Statics.toLower(server), new Server(Statics.toLower(server), serverperms, stimedperms, worlds, sdisplay, sprefix, ssuffix));
        }
        permsconfusers.setAutoSavingEnabled(true);

        User u = new User(null, null, groups, timedgroups, perms, timedperms, servers, display, prefix, suffix);
        return u;
    }

    @Override
    public int loadVersion()
    {
        return permsconfgroups.getInt("version", 1);
    }

    @Override
    public void saveVersion(int version, boolean savetodisk)
    {
        permsconfgroups.setInt("version", version);
        permsconfusers.setInt("version", version);

        if (savetodisk)
        {
            permsconfgroups.save();
            permsconfusers.save();
        }
    }

    @Override
    public boolean isUserInDatabase(User user)
    {
        return permsconfusers.keyExists("users." + (BungeePerms.getInstance().getConfig().isUseUUIDs() ? user.getUUID().toString() : user.getName()));
    }

    private void checkPermFiles()
    {
        File fg = new File(plugin.getPluginFolder(), permspathgroups);
        File fu = new File(plugin.getPluginFolder(), permspathusers);
        if (!fg.isFile() || !fu.isFile())
        {
            BungeePerms.getLogger().info(Lang.translate(Lang.MessageType.NO_PERM_FILE));
        }
    }

    @Override
    public List<String> getRegisteredUsers()
    {
        return permsconfusers.getSubNodes("users");
    }

    @Override
    public List<String> getGroupUsers(Group group)
    {
        List<String> users = new ArrayList<>();

        for (String user : permsconfusers.getSubNodes("users"))
        {
            if (permsconfusers.getListString("users." + user + ".groups", new ArrayList<String>()).contains(group.getName()))
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
            String uname = BungeePerms.getInstance().getConfig().isUseUUIDs() ? user.getUUID().toString() : user.getName();

            permsconfusers.setListString("users." + uname + ".groups", user.getGroupsString());
            setTimed(permsconfusers, "users." + uname + ".timedgroups", user.getTimedGroupsString());
            permsconfusers.setListString("users." + uname + ".permissions", user.getPerms());
            setTimed(permsconfusers, "users." + uname + ".timedpermissions", user.getTimedPerms());

            for (Map.Entry<String, Server> se : user.getServers().entrySet())
            {
                permsconfusers.setListString("users." + uname + ".servers." + se.getKey() + ".permissions", se.getValue().getPerms());
                setTimed(permsconfusers, "users." + uname + ".servers." + se.getKey() + ".timedpermissions", se.getValue().getTimedPerms());
                permsconfusers.setString("users." + uname + ".servers." + se.getKey() + ".display", se.getValue().getDisplay());
                permsconfusers.setString("users." + uname + ".servers." + se.getKey() + ".prefix", se.getValue().getPrefix());
                permsconfusers.setString("users." + uname + ".servers." + se.getKey() + ".suffix", se.getValue().getSuffix());

                for (Map.Entry<String, World> we : se.getValue().getWorlds().entrySet())
                {
                    permsconfusers.setListString("users." + uname + ".servers." + se.getKey() + ".worlds." + we.getKey() + ".permissions", we.getValue().getPerms());
                    setTimed(permsconfusers, "users." + uname + ".servers." + se.getKey() + ".worlds." + we.getKey() + ".timedpermissions", we.getValue().getTimedPerms());
                    permsconfusers.setString("users." + uname + ".servers." + se.getKey() + ".worlds." + we.getKey() + ".display", we.getValue().getDisplay());
                    permsconfusers.setString("users." + uname + ".servers." + se.getKey() + ".worlds." + we.getKey() + ".prefix", we.getValue().getPrefix());
                    permsconfusers.setString("users." + uname + ".servers." + se.getKey() + ".worlds." + we.getKey() + ".suffix", we.getValue().getSuffix());
                }
            }
        }
    }

    @Override
    public synchronized void saveGroup(Group group, boolean savetodisk)
    {
        permsconfgroups.setListString("groups." + group.getName() + ".inheritances", group.getInheritancesString());
        setTimed(permsconfgroups, "groups." + group.getName() + ".timedinheritances", group.getTimedInheritancesString());
        permsconfgroups.setListString("groups." + group.getName() + ".permissions", group.getPerms());
        setTimed(permsconfgroups, "groups." + group.getName() + ".timedpermissions", group.getTimedPerms());
        permsconfgroups.setInt("groups." + group.getName() + ".rank", group.getRank());
        permsconfgroups.setString("groups." + group.getName() + ".ladder", group.getLadder());
        permsconfgroups.setBool("groups." + group.getName() + ".default", group.isDefault());
        permsconfgroups.setString("groups." + group.getName() + ".display", group.getDisplay());
        permsconfgroups.setString("groups." + group.getName() + ".prefix", group.getPrefix());
        permsconfgroups.setString("groups." + group.getName() + ".suffix", group.getSuffix());

        for (Map.Entry<String, Server> se : group.getServers().entrySet())
        {
            permsconfgroups.setListString("groups." + group.getName() + ".servers." + se.getKey() + ".permissions", se.getValue().getPerms());
            setTimed(permsconfgroups, "groups." + group.getName() + ".servers." + se.getKey() + ".timedpermissions", se.getValue().getTimedPerms());
            permsconfgroups.setString("groups." + group.getName() + ".servers." + se.getKey() + ".display", se.getValue().getDisplay());
            permsconfgroups.setString("groups." + group.getName() + ".servers." + se.getKey() + ".prefix", se.getValue().getPrefix());
            permsconfgroups.setString("groups." + group.getName() + ".servers." + se.getKey() + ".suffix", se.getValue().getSuffix());

            for (Map.Entry<String, World> we : se.getValue().getWorlds().entrySet())
            {
                permsconfgroups.setListString("groups." + group.getName() + ".servers." + se.getKey() + ".worlds." + we.getKey() + ".permissions", we.getValue().getPerms());
                setTimed(permsconfgroups, "groups." + group.getName() + ".servers." + se.getKey() + ".worlds." + we.getKey() + ".timedpermissions", we.getValue().getTimedPerms());
                permsconfgroups.setString("groups." + group.getName() + ".servers." + se.getKey() + ".worlds." + we.getKey() + ".display", we.getValue().getDisplay());
                permsconfgroups.setString("groups." + group.getName() + ".servers." + se.getKey() + ".worlds." + we.getKey() + ".prefix", we.getValue().getPrefix());
                permsconfgroups.setString("groups." + group.getName() + ".servers." + se.getKey() + ".worlds." + we.getKey() + ".suffix", we.getValue().getSuffix());
            }
        }

        if (savetodisk)
        {
            permsconfgroups.save();
        }
    }

    @Override
    public synchronized void deleteUser(User user)
    {
        permsconfusers.deleteNode("users." + (BungeePerms.getInstance().getConfig().isUseUUIDs() ? user.getUUID().toString() : user.getName()));
    }

    @Override
    public synchronized void deleteGroup(Group group)
    {
        permsconfgroups.deleteNode("groups." + group.getName());
    }

    @Override
    public synchronized void saveUserGroups(User user)
    {
        permsconfusers.setListStringAndSave("users." + (BungeePerms.getInstance().getConfig().isUseUUIDs() ? user.getUUID().toString() : user.getName()) + ".groups",
                                            user.getGroupsString());
    }

    @Override
    public synchronized void saveUserTimedGroups(User user)
    {
        setTimed(permsconfusers,
                 "users." + (BungeePerms.getInstance().getConfig().isUseUUIDs() ? user.getUUID().toString() : user.getName()) + ".timedgroups",
                 user.getTimedGroupsString());
        permsconfusers.save();
    }

    @Override
    public synchronized void saveUserPerms(User user, String server, String world)
    {
        server = Statics.toLower(server);
        world = server == null ? null : Statics.toLower(world);

        List<String> perms;
        String key = "users." + (BungeePerms.getInstance().getConfig().isUseUUIDs() ? user.getUUID().toString() : user.getName());
        if (server == null)
        {
            perms = user.getPerms();
        }
        else if (world == null)
        {
            perms = user.getServer(server).getPerms();
            key += ".servers." + server;
        }
        else
        {
            perms = user.getServer(server).getWorld(world).getPerms();
            key += ".servers." + server + ".worlds." + world;
        }

        permsconfusers.setListStringAndSave(key + ".permissions", perms);
    }

    @Override
    public synchronized void saveUserTimedPerms(User user, String server, String world)
    {
        server = Statics.toLower(server);
        world = server == null ? null : Statics.toLower(world);

        List<TimedValue<String>> timedperms;
        String key = "users." + (BungeePerms.getInstance().getConfig().isUseUUIDs() ? user.getUUID().toString() : user.getName());
        if (server == null)
        {
            timedperms = user.getTimedPerms();
        }
        else if (world == null)
        {
            timedperms = user.getServer(server).getTimedPerms();
            key += ".servers." + server;
        }
        else
        {
            timedperms = user.getServer(server).getWorld(world).getTimedPerms();
            key += ".servers." + server + ".worlds." + world;
        }

        setTimed(permsconfusers, key + ".timedpermissions", timedperms);
        permsconfusers.save();
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
        permsconfusers.setStringAndSave("users." + (BungeePerms.getInstance().getConfig().isUseUUIDs() ? user.getUUID().toString() : user.getName()) + (server != null ? ".servers." + server + (world != null ? ".worlds." + world : "") : "") + ".display", display);
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
        permsconfusers.setStringAndSave("users." + (BungeePerms.getInstance().getConfig().isUseUUIDs() ? user.getUUID().toString() : user.getName()) + (server != null ? ".servers." + server + (world != null ? ".worlds." + world : "") : "") + ".prefix", prefix);
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
        permsconfusers.setStringAndSave("users." + (BungeePerms.getInstance().getConfig().isUseUUIDs() ? user.getUUID().toString() : user.getName()) + (server != null ? ".servers." + server + (world != null ? ".worlds." + world : "") : "") + ".suffix", suffix);
    }

    @Override
    public synchronized void saveGroupPerms(Group group, String server, String world)
    {
        server = Statics.toLower(server);
        world = server == null ? null : Statics.toLower(world);

        List<String> perms;
        String key = "groups." + group.getName();
        if (server == null)
        {
            perms = group.getPerms();
        }
        else if (world == null)
        {
            perms = group.getServer(server).getPerms();
            key += ".servers." + server;
        }
        else
        {
            perms = group.getServer(server).getWorld(world).getPerms();
            key += ".servers." + server + ".worlds." + world;
        }

        permsconfgroups.setListStringAndSave(key + ".permissions", perms);
    }

    @Override
    public synchronized void saveGroupTimedPerms(Group group, String server, String world)
    {
        server = Statics.toLower(server);
        world = server == null ? null : Statics.toLower(world);

        List<TimedValue<String>> timedperms;
        String key = "groups." + group.getName();
        if (server == null)
        {
            timedperms = group.getTimedPerms();
        }
        else if (world == null)
        {
            timedperms = group.getServer(server).getTimedPerms();
            key += ".servers." + server;
        }
        else
        {
            timedperms = group.getServer(server).getWorld(world).getTimedPerms();
            key += ".servers." + server + ".worlds." + world;
        }

        setTimed(permsconfgroups, key + ".timedpermissions", timedperms);
        permsconfgroups.save();
    }

    @Override
    public synchronized void saveGroupInheritances(Group group)
    {
        permsconfgroups.setListStringAndSave("groups." + group.getName() + ".inheritances", group.getInheritancesString());
    }

    @Override
    public synchronized void saveGroupTimedInheritances(Group group)
    {
        setTimed(permsconfgroups, "groups." + group.getName() + ".timedinheritances", group.getTimedInheritancesString());
        permsconfgroups.save();
    }

    @Override
    public synchronized void saveGroupLadder(Group group)
    {
        permsconfgroups.setStringAndSave("groups." + group.getName() + ".ladder", group.getLadder());
    }

    @Override
    public synchronized void saveGroupRank(Group group)
    {
        permsconfgroups.setIntAndSave("groups." + group.getName() + ".rank", group.getRank());
    }

    @Override
    public synchronized void saveGroupWeight(Group group)
    {
        permsconfgroups.setIntAndSave("groups." + group.getName() + ".weight", group.getWeight());
    }

    @Override
    public synchronized void saveGroupDefault(Group group)
    {
        permsconfgroups.setBoolAndSave("groups." + group.getName() + ".default", group.isDefault());
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
        permsconfgroups.setStringAndSave("groups." + group.getName() + (server != null ? ".servers." + server + (world != null ? ".worlds." + world : "") : "") + ".display", display);
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
        permsconfgroups.setStringAndSave("groups." + group.getName() + (server != null ? ".servers." + server + (world != null ? ".worlds." + world : "") : "") + ".prefix", prefix);
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
        permsconfgroups.setStringAndSave("groups." + group.getName() + (server != null ? ".servers." + server + (world != null ? ".worlds." + world : "") : "") + ".suffix", suffix);
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

        permsconfgroups.save();
        permsconfusers.save();
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

        permsconfgroups.save();
        permsconfusers.save();

        return deleted;
    }

    @Override
    public void clearDatabase()
    {
        new File(BungeePerms.getInstance().getPlugin().getPluginFolder(), permspathgroups).delete();
        new File(BungeePerms.getInstance().getPlugin().getPluginFolder(), permspathusers).delete();
        permsconfgroups = new Config(BungeePerms.getInstance().getPlugin(), permspathgroups);
        permsconfusers = new Config(BungeePerms.getInstance().getPlugin(), permspathusers);
        load();
    }

    @Override
    public void reloadGroup(Group group)
    {
        permsconfgroups.load();

        //load group from database
        List<String> inheritances = permsconfgroups.getListString("groups." + group.getName() + ".inheritances", new ArrayList<String>());
        List<TimedValue<String>> timedinheritances = getTimed(permsconfgroups, "groups." + group.getName() + ".timedinheritances");
        List<String> permissions = permsconfgroups.getListString("groups." + group.getName() + ".permissions", new ArrayList<String>());
        List<TimedValue<String>> timedperms = getTimed(permsconfgroups, "groups." + group.getName() + ".timedpermissions");
        boolean isdefault = permsconfgroups.getBoolean("groups." + group.getName() + ".default", false);
        int rank = permsconfgroups.getInt("groups." + group.getName() + ".rank", 1000);
        int weight = permsconfgroups.getInt("groups." + group.getName() + ".weight", 1000);
        String ladder = permsconfgroups.getString("groups." + group.getName() + ".ladder", "default");
        String display = permsconfgroups.getString("groups." + group.getName() + ".display", null);
        String prefix = permsconfgroups.getString("groups." + group.getName() + ".prefix", null);
        String suffix = permsconfgroups.getString("groups." + group.getName() + ".suffix", null);

        //per server perms
        Map<String, Server> servers = new HashMap<>();
        for (String server : permsconfgroups.getSubNodes("groups." + group.getName() + ".servers"))
        {
            List<String> serverperms = permsconfgroups.getListString("groups." + group.getName() + ".servers." + server + ".permissions", new ArrayList<String>());
            List<TimedValue<String>> stimedperms = getTimed(permsconfgroups, "groups." + group.getName() + ".servers." + server + ".timedpermissions");
            String sdisplay = permsconfgroups.getString("groups." + group.getName() + ".servers." + server + ".display", null);
            String sprefix = permsconfgroups.getString("groups." + group.getName() + ".servers." + server + ".prefix", null);
            String ssuffix = permsconfgroups.getString("groups." + group.getName() + ".servers." + server + ".suffix", null);

            //per server world perms
            Map<String, World> worlds = new HashMap<>();
            for (String world : permsconfgroups.getSubNodes("groups." + group.getName() + ".servers." + server + ".worlds"))
            {
                List<String> worldperms = permsconfgroups.getListString("groups." + group.getName() + ".servers." + server + ".worlds." + world + ".permissions", new ArrayList<String>());
                List<TimedValue<String>> wtimedperms = getTimed(permsconfgroups, "groups." + group.getName() + ".servers." + server + ".worlds." + world + ".timedpermissions");
                String wdisplay = permsconfgroups.getString("groups." + group.getName() + ".servers." + server + ".worlds." + world + ".display", null);
                String wprefix = permsconfgroups.getString("groups." + group.getName() + ".servers." + server + ".worlds." + world + ".prefix", null);
                String wsuffix = permsconfgroups.getString("groups." + group.getName() + ".servers." + server + ".worlds." + world + ".suffix", null);

                World w = new World(Statics.toLower(world), worldperms, wtimedperms, wdisplay, wprefix, wsuffix);
                worlds.put(Statics.toLower(world), w);
            }

            servers.put(Statics.toLower(server), new Server(Statics.toLower(server), serverperms, stimedperms, worlds, sdisplay, sprefix, ssuffix));
        }

        group.setInheritances(inheritances);
        group.setTimedInheritances(timedinheritances);
        group.setPerms(permissions);
        group.setTimedPerms(timedperms);
        group.setIsdefault(isdefault);
        group.setRank(rank);
        group.setWeight(weight);
        group.setLadder(ladder);
        group.setDisplay(display);
        group.setPrefix(prefix);
        group.setSuffix(suffix);
        group.setServers(servers);
        group.invalidateCache();
    }

    @Override
    public void reloadUser(User user)
    {
        permsconfusers.load();

        String uname = config.isUseUUIDs() ? user.getUUID().toString() : user.getName();

        //load user from database
        List<String> groups = permsconfusers.getListString("users." + uname + ".groups", new ArrayList<String>());
        List<TimedValue<String>> timedgroups = getTimed(permsconfusers, "users." + uname + ".timedgroups");
        List<String> perms = permsconfusers.getListString("users." + uname + ".permissions", new ArrayList<String>());
        List<TimedValue<String>> timedperms = getTimed(permsconfusers, "users." + uname + ".timedpermissions");
        String display = permsconfusers.getString("users." + uname + ".display", null);
        String prefix = permsconfusers.getString("users." + uname + ".prefix", null);
        String suffix = permsconfusers.getString("users." + uname + ".suffix", null);

        //per server perms
        Map<String, Server> servers = new HashMap<>();
        for (String server : permsconfusers.getSubNodes("users." + uname + ".servers"))
        {
            List<String> serverperms = permsconfusers.getListString("users." + uname + ".servers." + server + ".permissions", new ArrayList<String>());
            List<TimedValue<String>> stimedperms = getTimed(permsconfusers, "users." + uname + ".servers." + server + ".timedpermissions");
            String sdisplay = permsconfusers.getString("users." + uname + ".servers." + server + ".display", null);
            String sprefix = permsconfusers.getString("users." + uname + ".servers." + server + ".prefix", null);
            String ssuffix = permsconfusers.getString("users." + uname + ".servers." + server + ".suffix", null);

            //per server world perms
            Map<String, World> worlds = new HashMap<>();
            for (String world : permsconfusers.getSubNodes("users." + uname + ".servers." + server + ".worlds"))
            {
                List<String> worldperms = permsconfusers.getListString("users." + uname + ".servers." + server + ".worlds." + world + ".permissions", new ArrayList<String>());
                List<TimedValue<String>> wtimedperms = getTimed(permsconfusers, "users." + uname + ".servers." + server + ".worlds." + world + ".timedpermissions");
                String wdisplay = permsconfusers.getString("users." + uname + ".servers." + server + ".worlds." + world + ".display", null);
                String wprefix = permsconfusers.getString("users." + uname + ".servers." + server + ".worlds." + world + ".prefix", null);
                String wsuffix = permsconfusers.getString("users." + uname + ".servers." + server + ".worlds." + world + ".suffix", null);

                World w = new World(Statics.toLower(world), worldperms, wtimedperms, wdisplay, wprefix, wsuffix);
                worlds.put(Statics.toLower(world), w);
            }

            servers.put(Statics.toLower(server), new Server(Statics.toLower(server), serverperms, stimedperms, worlds, sdisplay, sprefix, ssuffix));
        }

        user.setGroups(groups);
        user.setTimedGroups(timedgroups);
        user.setPerms(perms);
        user.setTimedPerms(timedperms);
        user.setDisplay(display);
        user.setPrefix(prefix);
        user.setSuffix(suffix);
        user.setServers(servers);
        user.invalidateCache();
    }

    private List<TimedValue<String>> getTimed(Config conf, String key)
    {
        List<TimedValue<String>> ret = new ArrayList();
        for (String value : conf.getSubNodes(key))
        {
            Date start = str2date(conf.getString(key + "." + value + ".start", null));
            int dur = conf.getInt(key + "." + value + ".duration", 0);
            if (start == null || dur == 0)
                continue;
            ret.add(new TimedValue(value, start, dur));
        }
        return ret;
    }

    private void setTimed(Config conf, String key, List<TimedValue<String>> values)
    {
        conf.deleteNode(key);
        for (TimedValue tv : values)
        {
            conf.setString(key + "." + tv.getValue() + ".start", date2str(tv.getStart()));
            conf.setInt(key + "." + tv.getValue() + ".duration", tv.getDuration());
        }
    }

    private String date2str(Date d)
    {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(d);
    }

    private Date str2date(String str)
    {
        try
        {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(str);
        }
        catch (Exception e)
        {
            return null;
        }
    }
}
