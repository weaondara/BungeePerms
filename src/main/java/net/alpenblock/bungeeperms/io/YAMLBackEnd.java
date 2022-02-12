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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import lombok.SneakyThrows;
import net.alpenblock.bungeeperms.BPConfig;
import net.alpenblock.bungeeperms.BPPermission;
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

public class YAMLBackEnd implements BackEnd {

    private final static String permspathgroups = "/permissions.groups.yml";
    private final static String permspathusers = "/permissions.users.yml";
    private Config permsconfgroups;
    private Config permsconfusers;

    private final PlatformPlugin plugin;
    private final BPConfig config;

    private final ReentrantLock userlock = new ReentrantLock();
    private final ReentrantLock grouplock = new ReentrantLock();

    @SneakyThrows
    public YAMLBackEnd() {
        plugin = BungeePerms.getInstance().getPlugin();
        config = BungeePerms.getInstance().getConfig();

        //migrate
        File oldfile = new File(plugin.getPluginFolder(), "/permissions.yml");
        if (oldfile.isFile()) {
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
    public BackEndType getType() {
        return BackEndType.YAML;
    }

    @Override
    public void load() {
        //load from file
        grouplock.lock();
        try {
            permsconfgroups.load();
        } finally {
            grouplock.unlock();
        }
        userlock.lock();
        try {
            permsconfusers.load();
        } finally {
            userlock.unlock();
        }
    }

    @Override
    public List<Group> loadGroups() {
        List<Group> ret = new ArrayList<>();

        grouplock.lock();
        try {
            List<String> groups = permsconfgroups.getSubNodes("groups");
            for (String g : groups) {
                ret.add(loadGroup(g));
            }
        } finally {
            grouplock.unlock();
        }
        Collections.sort(ret);

        return ret;
    }

    @Override
    public List<User> loadUsers() {
        List<User> ret = new ArrayList<>();

        userlock.lock();
        try {
            List<String> users = permsconfusers.getSubNodes("users");
            BungeePerms.getInstance().getDebug().log("loading " + users.size() + " users");
            int i = 0;
            for (String u : users) {
                i++;
                if (i % 1000 == 0)
                    BungeePerms.getInstance().getDebug().log("loaded " + i + "/" + users.size() + " users");
                User user = BungeePerms.getInstance().getConfig().isUseUUIDs() ? loadUser(UUID.fromString(u)) : loadUser(u);
                ret.add(user);
            }
        } finally {
            userlock.unlock();
        }

        return ret;
    }

    @Override
    public Group loadGroup(String group) {
        Group g;

        grouplock.lock();
        try {
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
            for (String server : permsconfgroups.getSubNodes("groups." + group + ".servers")) {
                List<String> serverinheritances = permsconfgroups.getListString("groups." + group + ".servers." + server + ".inheritances", new ArrayList<String>());
                List<TimedValue<String>> stimedinheritances = getTimed(permsconfgroups, "groups." + group + ".servers." + server + ".timedinheritances");
                List<String> serverperms = permsconfgroups.getListString("groups." + group + ".servers." + server + ".permissions", new ArrayList<String>());
                List<TimedValue<String>> stimedperms = getTimed(permsconfgroups, "groups." + group + ".servers." + server + ".timedpermissions");
                String sdisplay = permsconfgroups.getString("groups." + group + ".servers." + server + ".display", null);
                String sprefix = permsconfgroups.getString("groups." + group + ".servers." + server + ".prefix", null);
                String ssuffix = permsconfgroups.getString("groups." + group + ".servers." + server + ".suffix", null);

                //per server world perms
                Map<String, World> worlds = new HashMap<>();
                for (String world : permsconfgroups.getSubNodes("groups." + group + ".servers." + server + ".worlds")) {
                    List<String> worldinheritances = permsconfgroups.getListString("groups." + group + ".servers." + server + ".worlds." + world + ".inheritances", new ArrayList<String>());
                    List<TimedValue<String>> wtimedinheritances = getTimed(permsconfgroups, "groups." + group + ".servers." + server + ".worlds." + world + ".timedinheritances");
                    List<String> worldperms = permsconfgroups.getListString("groups." + group + ".servers." + server + ".worlds." + world + ".permissions", new ArrayList<String>());
                    List<TimedValue<String>> wtimedperms = getTimed(permsconfgroups, "groups." + group + ".servers." + server + ".worlds." + world + ".timedpermissions");
                    String wdisplay = permsconfgroups.getString("groups." + group + ".servers." + server + ".worlds." + world + ".display", null);
                    String wprefix = permsconfgroups.getString("groups." + group + ".servers." + server + ".worlds." + world + ".prefix", null);
                    String wsuffix = permsconfgroups.getString("groups." + group + ".servers." + server + ".worlds." + world + ".suffix", null);

                    World w = new World(Statics.toLower(world), worldinheritances, wtimedinheritances, worldperms, wtimedperms, wdisplay, wprefix, wsuffix);
                    worlds.put(Statics.toLower(world), w);
                }

                servers.put(Statics.toLower(server), new Server(Statics.toLower(server), serverinheritances, stimedinheritances, serverperms, stimedperms, worlds, sdisplay, sprefix, ssuffix));
            }

            permsconfgroups.setAutoSavingEnabled(true);

            g = new Group(group, inheritances, timedinheritances, permissions, timedperms, servers, rank, weight, ladder, isdefault, display, prefix, suffix);
        } finally {
            grouplock.unlock();
        }

        g.invalidateCache();
        return g;
    }

    @Override
    public User loadUser(String user) {
        User u;

        userlock.lock();
        try {
            u = loadUser0(user);
            if (u == null)
                return null;
            if (BungeePerms.getInstance().getConfig().isUseUUIDs()) {
                UUID uuid = BungeePerms.getInstance().getPermissionsManager().getUUIDPlayerDB().getUUID(user);
                u.setUUID(uuid);
            }
            u.setName(user);
        } finally {
            userlock.unlock();
        }

        u.invalidateCache();
        return u;
    }

    @Override
    public User loadUser(UUID user) {
        User u;

        userlock.lock();
        try {
            u = loadUser0(user.toString());
            if (u == null)
                return null;
            String username = BungeePerms.getInstance().getPermissionsManager().getUUIDPlayerDB().getPlayerName(user);
            u.setName(username);
            u.setUUID(user);
        } finally {
            userlock.unlock();
        }

        u.invalidateCache();
        return u;
    }

    private User loadUser0(String user) {
        userlock.lock();
        try {
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
            for (String server : permsconfusers.getSubNodes("users." + user + ".servers")) {
                List<String> servergroups = permsconfusers.getListString("users." + user + ".servers." + server + ".groups", new ArrayList<String>());
                List<TimedValue<String>> stimedgroups = getTimed(permsconfusers, "users." + user + ".servers." + server + ".timedgroups");
                List<String> serverperms = permsconfusers.getListString("users." + user + ".servers." + server + ".permissions", new ArrayList<String>());
                List<TimedValue<String>> stimedperms = getTimed(permsconfusers, "users." + user + ".servers." + server + ".timedpermissions");
                String sdisplay = permsconfusers.getString("users." + user + ".servers." + server + ".display", null);
                String sprefix = permsconfusers.getString("users." + user + ".servers." + server + ".prefix", null);
                String ssuffix = permsconfusers.getString("users." + user + ".servers." + server + ".suffix", null);

                //per server world perms
                Map<String, World> worlds = new HashMap<>();
                for (String world : permsconfusers.getSubNodes("users." + user + ".servers." + server + ".worlds")) {
                    List<String> worldgroups = permsconfusers.getListString("users." + user + ".servers." + server + ".worlds." + world + ".groups", new ArrayList<String>());
                    List<TimedValue<String>> wtimedgroups = getTimed(permsconfusers, "users." + user + ".servers." + server + ".worlds." + world + ".timedgroups");
                    List<String> worldperms = permsconfusers.getListString("users." + user + ".servers." + server + ".worlds." + world + ".permissions", new ArrayList<String>());
                    List<TimedValue<String>> wtimedperms = getTimed(permsconfusers, "users." + user + ".servers." + server + ".worlds." + world + ".timedpermissions");
                    String wdisplay = permsconfusers.getString("users." + user + ".servers." + server + ".worlds." + world + ".display", null);
                    String wprefix = permsconfusers.getString("users." + user + ".servers." + server + ".worlds." + world + ".prefix", null);
                    String wsuffix = permsconfusers.getString("users." + user + ".servers." + server + ".worlds." + world + ".suffix", null);

                    World w = new World(Statics.toLower(world), worldgroups, wtimedgroups, worldperms, wtimedperms, wdisplay, wprefix, wsuffix);
                    worlds.put(Statics.toLower(world), w);
                }

                servers.put(Statics.toLower(server), new Server(Statics.toLower(server), servergroups, stimedgroups, serverperms, stimedperms, worlds, sdisplay, sprefix, ssuffix));
            }
            permsconfusers.setAutoSavingEnabled(true);

            User u = new User(null, null, groups, timedgroups, perms, timedperms, servers, display, prefix, suffix);
            return u;
        } finally {
            userlock.unlock();
        }
    }

    @Override
    public int loadVersion() {
        grouplock.lock();
        try {
            return permsconfgroups.getInt("version", 1);
        } finally {
            grouplock.unlock();
        }
    }

    @Override
    public void saveVersion(int version, boolean savetodisk) {
        grouplock.lock();
        userlock.lock();
        try {
            permsconfgroups.setInt("version", version);
            permsconfusers.setInt("version", version);

            if (savetodisk) {
                permsconfgroups.save();
                permsconfusers.save();
            }
        } finally {
            userlock.unlock();
            grouplock.unlock();
        }
    }

    @Override
    public boolean isUserInDatabase(User user) {
        userlock.lock();
        try {
            return permsconfusers.keyExists("users." + (BungeePerms.getInstance().getConfig().isUseUUIDs() ? user.getUUID().toString() : user.getName()));
        } finally {
            userlock.unlock();
        }
    }

    private void checkPermFiles() {
        File fg = new File(plugin.getPluginFolder(), permspathgroups);
        File fu = new File(plugin.getPluginFolder(), permspathusers);
        if (!fg.isFile() || !fu.isFile()) {
            BungeePerms.getLogger().info(Lang.translate(Lang.MessageType.NO_PERM_FILE));
        }
    }

    @Override
    public List<String> getRegisteredUsers() {
        userlock.lock();
        try {
            return permsconfusers.getSubNodes("users");
        } finally {
            userlock.unlock();
        }
    }

    @Override
    public List<String> getGroupUsers(Group group) {
        List<String> users = new ArrayList<>();

        userlock.lock();
        try {
            for (String user : permsconfusers.getSubNodes("users")) {
                if (permsconfusers.getListString("users." + user + ".groups", new ArrayList<String>()).contains(group.getName())) {
                    users.add(user);
                }
            }
        } finally {
            userlock.unlock();
        }

        return users;
    }

    @Override
    public synchronized void saveUser(User user, boolean savetodisk) {
        userlock.lock();
        try {
            if (BungeePerms.getInstance().getConfig().isSaveAllUsers() ? true : !user.isNothingSpecial()) {
                String uname = BungeePerms.getInstance().getConfig().isUseUUIDs() ? user.getUUID().toString() : user.getName();

                permsconfusers.setListString("users." + uname + ".groups", user.getGroupsString());
                setTimed(permsconfusers, "users." + uname + ".timedgroups", user.getTimedGroupsString());
                permsconfusers.setListString("users." + uname + ".permissions", user.getPerms());
                setTimed(permsconfusers, "users." + uname + ".timedpermissions", user.getTimedPerms());

                for (Map.Entry<String, Server> se : user.getServers().entrySet()) {
                    permsconfusers.setListString("users." + uname + ".servers." + se.getKey() + ".groups", se.getValue().getGroupsString());
                    setTimed(permsconfusers, "users." + uname + ".servers." + se.getKey() + ".timedgroups", se.getValue().getTimedGroupsString());
                    permsconfusers.setListString("users." + uname + ".servers." + se.getKey() + ".permissions", se.getValue().getPerms());
                    setTimed(permsconfusers, "users." + uname + ".servers." + se.getKey() + ".timedpermissions", se.getValue().getTimedPerms());
                    permsconfusers.setString("users." + uname + ".servers." + se.getKey() + ".display", se.getValue().getDisplay());
                    permsconfusers.setString("users." + uname + ".servers." + se.getKey() + ".prefix", se.getValue().getPrefix());
                    permsconfusers.setString("users." + uname + ".servers." + se.getKey() + ".suffix", se.getValue().getSuffix());

                    for (Map.Entry<String, World> we : se.getValue().getWorlds().entrySet()) {
                        permsconfusers.setListString("users." + uname + ".servers." + se.getKey() + ".worlds." + we.getKey() + ".groups", we.getValue().getGroupsString());
                        setTimed(permsconfusers, "users." + uname + ".servers." + se.getKey() + ".worlds." + we.getKey() + ".timedgroups", we.getValue().getTimedGroupsString());
                        permsconfusers.setListString("users." + uname + ".servers." + se.getKey() + ".worlds." + we.getKey() + ".permissions", we.getValue().getPerms());
                        setTimed(permsconfusers, "users." + uname + ".servers." + se.getKey() + ".worlds." + we.getKey() + ".timedpermissions", we.getValue().getTimedPerms());
                        permsconfusers.setString("users." + uname + ".servers." + se.getKey() + ".worlds." + we.getKey() + ".display", we.getValue().getDisplay());
                        permsconfusers.setString("users." + uname + ".servers." + se.getKey() + ".worlds." + we.getKey() + ".prefix", we.getValue().getPrefix());
                        permsconfusers.setString("users." + uname + ".servers." + se.getKey() + ".worlds." + we.getKey() + ".suffix", we.getValue().getSuffix());
                    }
                }
            }
        } finally {
            userlock.unlock();
        }
    }

    @Override
    public synchronized void saveGroup(Group group, boolean savetodisk) {
        grouplock.lock();
        try {
            permsconfgroups.setAutoSavingEnabled(false);

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

            for (Map.Entry<String, Server> se : group.getServers().entrySet()) {
                permsconfgroups.setListString("groups." + group.getName() + ".servers." + se.getKey() + ".inheritances", se.getValue().getGroupsString());
                setTimed(permsconfgroups, "groups." + group.getName() + ".servers." + se.getKey() + ".timedinheritances", se.getValue().getTimedGroupsString());
                permsconfgroups.setListString("groups." + group.getName() + ".servers." + se.getKey() + ".permissions", se.getValue().getPerms());
                setTimed(permsconfgroups, "groups." + group.getName() + ".servers." + se.getKey() + ".timedpermissions", se.getValue().getTimedPerms());
                permsconfgroups.setString("groups." + group.getName() + ".servers." + se.getKey() + ".display", se.getValue().getDisplay());
                permsconfgroups.setString("groups." + group.getName() + ".servers." + se.getKey() + ".prefix", se.getValue().getPrefix());
                permsconfgroups.setString("groups." + group.getName() + ".servers." + se.getKey() + ".suffix", se.getValue().getSuffix());

                for (Map.Entry<String, World> we : se.getValue().getWorlds().entrySet()) {
                    permsconfgroups.setListString("groups." + group.getName() + ".servers." + se.getKey() + ".worlds." + we.getKey() + ".inheritances", we.getValue().getGroupsString());
                    setTimed(permsconfgroups, "groups." + group.getName() + ".servers." + se.getKey() + ".worlds." + we.getKey() + ".timedinheritances", we.getValue().getTimedGroupsString());
                    permsconfgroups.setListString("groups." + group.getName() + ".servers." + se.getKey() + ".worlds." + we.getKey() + ".permissions", we.getValue().getPerms());
                    setTimed(permsconfgroups, "groups." + group.getName() + ".servers." + se.getKey() + ".worlds." + we.getKey() + ".timedpermissions", we.getValue().getTimedPerms());
                    permsconfgroups.setString("groups." + group.getName() + ".servers." + se.getKey() + ".worlds." + we.getKey() + ".display", we.getValue().getDisplay());
                    permsconfgroups.setString("groups." + group.getName() + ".servers." + se.getKey() + ".worlds." + we.getKey() + ".prefix", we.getValue().getPrefix());
                    permsconfgroups.setString("groups." + group.getName() + ".servers." + se.getKey() + ".worlds." + we.getKey() + ".suffix", we.getValue().getSuffix());
                }
            }
            permsconfgroups.setAutoSavingEnabled(true);

            if (savetodisk) {
                permsconfgroups.save();
            }
        } finally {
            grouplock.unlock();
        }
    }

    @Override
    public synchronized void deleteUser(User user) {
        userlock.lock();
        try {
            permsconfusers.deleteNode("users." + (BungeePerms.getInstance().getConfig().isUseUUIDs() ? user.getUUID().toString() : user.getName()));
        } finally {
            userlock.unlock();
        }
    }

    @Override
    public synchronized void deleteGroup(Group group) {
        grouplock.lock();
        try {
            permsconfgroups.deleteNode("groups." + group.getName());
        } finally {
            grouplock.unlock();
        }
    }

    @Override
    public synchronized void saveUserGroups(User user, String server, String world) {
        server = Statics.toLower(server);
        world = server == null ? null : Statics.toLower(world);

        List<String> groups;
        String key = "users." + (BungeePerms.getInstance().getConfig().isUseUUIDs() ? user.getUUID().toString() : user.getName());
        if (server == null) {
            groups = user.getGroupsString();
        } else if (world == null) {
            groups = user.getServer(server).getGroupsString();
            key += ".servers." + server;
        } else {
            groups = user.getServer(server).getWorld(world).getGroupsString();
            key += ".servers." + server + ".worlds." + world;
        }

        userlock.lock();
        try {
            permsconfusers.setListStringAndSave(key + ".groups", groups);
        } finally {
            userlock.unlock();
        }
    }

    @Override
    public synchronized void saveUserTimedGroups(User user, String server, String world) {
        server = Statics.toLower(server);
        world = server == null ? null : Statics.toLower(world);

        List<TimedValue<String>> timedgroups;
        String key = "users." + (BungeePerms.getInstance().getConfig().isUseUUIDs() ? user.getUUID().toString() : user.getName());
        if (server == null) {
            timedgroups = user.getTimedGroupsString();
        } else if (world == null) {
            timedgroups = user.getServer(server).getTimedGroupsString();
            key += ".servers." + server;
        } else {
            timedgroups = user.getServer(server).getWorld(world).getTimedGroupsString();
            key += ".servers." + server + ".worlds." + world;
        }

        userlock.lock();
        try {
            setTimed(permsconfusers, key + ".timedgroups", timedgroups);
            permsconfusers.save();
        } finally {
            userlock.unlock();
        }
    }

    @Override
    public synchronized void saveUserPerms(User user, String server, String world) {
        server = Statics.toLower(server);
        world = server == null ? null : Statics.toLower(world);

        List<String> perms;
        String key = "users." + (BungeePerms.getInstance().getConfig().isUseUUIDs() ? user.getUUID().toString() : user.getName());
        if (server == null) {
            perms = user.getPerms();
        } else if (world == null) {
            perms = user.getServer(server).getPerms();
            key += ".servers." + server;
        } else {
            perms = user.getServer(server).getWorld(world).getPerms();
            key += ".servers." + server + ".worlds." + world;
        }

        userlock.lock();
        try {
            permsconfusers.setListStringAndSave(key + ".permissions", perms);
        } finally {
            userlock.unlock();
        }
    }

    @Override
    public synchronized void saveUserTimedPerms(User user, String server, String world) {
        server = Statics.toLower(server);
        world = server == null ? null : Statics.toLower(world);

        List<TimedValue<String>> timedperms;
        String key = "users." + (BungeePerms.getInstance().getConfig().isUseUUIDs() ? user.getUUID().toString() : user.getName());
        if (server == null) {
            timedperms = user.getTimedPerms();
        } else if (world == null) {
            timedperms = user.getServer(server).getTimedPerms();
            key += ".servers." + server;
        } else {
            timedperms = user.getServer(server).getWorld(world).getTimedPerms();
            key += ".servers." + server + ".worlds." + world;
        }

        userlock.lock();
        try {
            setTimed(permsconfusers, key + ".timedpermissions", timedperms);
            permsconfusers.save();
        } finally {
            userlock.unlock();
        }
    }

    @Override
    public synchronized void saveUserDisplay(User user, String server, String world) {
        server = Statics.toLower(server);
        world = Statics.toLower(world);

        String display = user.getDisplay();
        if (server != null) {
            display = user.getServer(server).getDisplay();
            if (world != null) {
                display = user.getServer(server).getWorld(world).getDisplay();
            }
        }

        userlock.lock();
        try {
            permsconfusers.setStringAndSave("users." + (BungeePerms.getInstance().getConfig().isUseUUIDs() ? user.getUUID().toString() : user.getName()) + (server != null ? ".servers." + server + (world != null ? ".worlds." + world : "") : "") + ".display", display);
        } finally {
            userlock.unlock();
        }
    }

    @Override
    public synchronized void saveUserPrefix(User user, String server, String world) {
        server = Statics.toLower(server);
        world = Statics.toLower(world);

        String prefix = user.getPrefix();
        if (server != null) {
            prefix = user.getServer(server).getPrefix();
            if (world != null) {
                prefix = user.getServer(server).getWorld(world).getPrefix();
            }
        }

        userlock.lock();
        try {
            permsconfusers.setStringAndSave("users." + (BungeePerms.getInstance().getConfig().isUseUUIDs() ? user.getUUID().toString() : user.getName()) + (server != null ? ".servers." + server + (world != null ? ".worlds." + world : "") : "") + ".prefix", prefix);
        } finally {
            userlock.unlock();
        }
    }

    @Override
    public synchronized void saveUserSuffix(User user, String server, String world) {
        server = Statics.toLower(server);
        world = Statics.toLower(world);

        String suffix = user.getSuffix();
        if (server != null) {
            suffix = user.getServer(server).getSuffix();
            if (world != null) {
                suffix = user.getServer(server).getWorld(world).getSuffix();
            }
        }
        userlock.lock();
        try {
            permsconfusers.setStringAndSave("users." + (BungeePerms.getInstance().getConfig().isUseUUIDs() ? user.getUUID().toString() : user.getName()) + (server != null ? ".servers." + server + (world != null ? ".worlds." + world : "") : "") + ".suffix", suffix);
        } finally {
            userlock.unlock();
        }
    }

    @Override
    public synchronized void saveGroupPerms(Group group, String server, String world) {
        server = Statics.toLower(server);
        world = server == null ? null : Statics.toLower(world);

        List<String> perms;
        String key = "groups." + group.getName();
        if (server == null) {
            perms = group.getPerms();
        } else if (world == null) {
            perms = group.getServer(server).getPerms();
            key += ".servers." + server;
        } else {
            perms = group.getServer(server).getWorld(world).getPerms();
            key += ".servers." + server + ".worlds." + world;
        }

        grouplock.lock();
        try {
            permsconfgroups.setListStringAndSave(key + ".permissions", perms);
        } finally {
            grouplock.unlock();
        }
    }

    @Override
    public synchronized void saveGroupTimedPerms(Group group, String server, String world) {
        server = Statics.toLower(server);
        world = server == null ? null : Statics.toLower(world);

        List<TimedValue<String>> timedperms;
        String key = "groups." + group.getName();
        if (server == null) {
            timedperms = group.getTimedPerms();
        } else if (world == null) {
            timedperms = group.getServer(server).getTimedPerms();
            key += ".servers." + server;
        } else {
            timedperms = group.getServer(server).getWorld(world).getTimedPerms();
            key += ".servers." + server + ".worlds." + world;
        }

        grouplock.lock();
        try {
            setTimed(permsconfgroups, key + ".timedpermissions", timedperms);
            permsconfgroups.save();
        } finally {
            grouplock.unlock();
        }
    }

    @Override
    public synchronized void saveGroupInheritances(Group group, String server, String world) {
        server = Statics.toLower(server);
        world = server == null ? null : Statics.toLower(world);

        List<String> inheritances;
        String key = "groups." + group.getName();
        if (server == null) {
            inheritances = group.getInheritancesString();
        } else if (world == null) {
            inheritances = group.getServer(server).getGroupsString();
            key += ".servers." + server;
        } else {
            inheritances = group.getServer(server).getWorld(world).getGroupsString();
            key += ".servers." + server + ".worlds." + world;
        }

        grouplock.lock();
        try {
            permsconfgroups.setListStringAndSave(key + ".inheritances", inheritances);
        } finally {
            grouplock.unlock();
        }
    }

    @Override
    public synchronized void saveGroupTimedInheritances(Group group, String server, String world) {
        server = Statics.toLower(server);
        world = server == null ? null : Statics.toLower(world);

        List<TimedValue<String>> timedinheritances;
        String key = "groups." + group.getName();
        if (server == null) {
            timedinheritances = group.getTimedInheritancesString();
        } else if (world == null) {
            timedinheritances = group.getServer(server).getTimedGroupsString();
            key += ".servers." + server;
        } else {
            timedinheritances = group.getServer(server).getWorld(world).getTimedGroupsString();
            key += ".servers." + server + ".worlds." + world;
        }

        grouplock.lock();
        try {
            setTimed(permsconfgroups, key + ".timedinheritances", timedinheritances);
            permsconfgroups.save();
        } finally {
            grouplock.unlock();
        }
    }

    @Override
    public synchronized void saveGroupLadder(Group group) {
        grouplock.lock();
        try {
            permsconfgroups.setStringAndSave("groups." + group.getName() + ".ladder", group.getLadder());
        } finally {
            grouplock.unlock();
        }
    }

    @Override
    public synchronized void saveGroupRank(Group group) {
        grouplock.lock();
        try {
            permsconfgroups.setIntAndSave("groups." + group.getName() + ".rank", group.getRank());
        } finally {
            grouplock.unlock();
        }
    }

    @Override
    public synchronized void saveGroupWeight(Group group) {
        grouplock.lock();
        try {
            permsconfgroups.setIntAndSave("groups." + group.getName() + ".weight", group.getWeight());
        } finally {
            grouplock.unlock();
        }
    }

    @Override
    public synchronized void saveGroupDefault(Group group) {
        grouplock.lock();
        try {
            permsconfgroups.setBoolAndSave("groups." + group.getName() + ".default", group.isDefault());
        } finally {
            grouplock.unlock();
        }
    }

    @Override
    public synchronized void saveGroupDisplay(Group group, String server, String world) {
        server = Statics.toLower(server);
        world = Statics.toLower(world);

        String display = group.getDisplay();
        if (server != null) {
            display = group.getServer(server).getDisplay();
            if (world != null) {
                display = group.getServer(server).getWorld(world).getDisplay();
            }
        }
        grouplock.lock();
        try {
            permsconfgroups.setStringAndSave("groups." + group.getName() + (server != null ? ".servers." + server + (world != null ? ".worlds." + world : "") : "") + ".display", display);
        } finally {
            grouplock.unlock();
        }
    }

    @Override
    public synchronized void saveGroupPrefix(Group group, String server, String world) {
        server = Statics.toLower(server);
        world = Statics.toLower(world);

        String prefix = group.getPrefix();
        if (server != null) {
            prefix = group.getServer(server).getPrefix();
            if (world != null) {
                prefix = group.getServer(server).getWorld(world).getPrefix();
            }
        }
        grouplock.lock();
        try {
            permsconfgroups.setStringAndSave("groups." + group.getName() + (server != null ? ".servers." + server + (world != null ? ".worlds." + world : "") : "") + ".prefix", prefix);
        } finally {
            grouplock.unlock();
        }
    }

    @Override
    public synchronized void saveGroupSuffix(Group group, String server, String world) {
        server = Statics.toLower(server);
        world = Statics.toLower(world);

        String suffix = group.getSuffix();
        if (server != null) {
            suffix = group.getServer(server).getSuffix();
            if (world != null) {
                suffix = group.getServer(server).getWorld(world).getSuffix();
            }
        }
        grouplock.lock();
        try {
            permsconfgroups.setStringAndSave("groups." + group.getName() + (server != null ? ".servers." + server + (world != null ? ".worlds." + world : "") : "") + ".suffix", suffix);
        } finally {
            grouplock.unlock();
        }
    }

    @Override
    public synchronized void format(List<Group> groups, List<User> users, int version) {
        grouplock.lock();
        userlock.lock();
        try {
            clearDatabase();
            for (int i = 0; i < groups.size(); i++) {
                saveGroup(groups.get(i), false);
            }
            for (int i = 0; i < users.size(); i++) {
                saveUser(users.get(i), false);
            }
            saveVersion(version, false);

            permsconfgroups.save();
            permsconfusers.save();
        } finally {
            userlock.unlock();
            grouplock.unlock();
        }
    }

    @Override
    public synchronized int cleanup(List<Group> groups, List<User> users, int version) {
        int deleted = 0;

        grouplock.lock();
        userlock.lock();
        try {
            clearDatabase();
            for (int i = 0; i < groups.size(); i++) {
                saveGroup(groups.get(i), false);
            }
            for (int i = 0; i < users.size(); i++) {
                User u = users.get(i);
                if (BungeePerms.getInstance().getConfig().isDeleteUsersOnCleanup()) {
                    //check for additional permissions and non-default groups AND onlinecheck
                    if (u.isNothingSpecial()
                        && BungeePerms.getInstance().getPlugin().getPlayer(u.getName()) == null
                        && BungeePerms.getInstance().getPlugin().getPlayer(u.getUUID()) == null) {
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
        } finally {
            userlock.unlock();
            grouplock.unlock();
        }

        return deleted;
    }

    @Override
    public void clearDatabase() {
        grouplock.lock();
        userlock.lock();
        try {
            new File(BungeePerms.getInstance().getPlugin().getPluginFolder(), permspathgroups).delete();
            new File(BungeePerms.getInstance().getPlugin().getPluginFolder(), permspathusers).delete();
            permsconfgroups = new Config(BungeePerms.getInstance().getPlugin(), permspathgroups);
            permsconfusers = new Config(BungeePerms.getInstance().getPlugin(), permspathusers);
            load();
        } finally {
            userlock.unlock();
            grouplock.unlock();
        }
    }

    @Override
    public void reloadGroup(Group group) {
        grouplock.lock();
        try {
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
            for (String server : permsconfgroups.getSubNodes("groups." + group.getName() + ".servers")) {
                List<String> servergroups = permsconfgroups.getListString("groups." + group + ".servers." + server + ".inheritances", new ArrayList<String>());
                List<TimedValue<String>> stimedgroups = getTimed(permsconfgroups, "groups." + group + ".servers." + server + ".timedinheritances");
                List<String> serverperms = permsconfgroups.getListString("groups." + group.getName() + ".servers." + server + ".permissions", new ArrayList<String>());
                List<TimedValue<String>> stimedperms = getTimed(permsconfgroups, "groups." + group.getName() + ".servers." + server + ".timedpermissions");
                String sdisplay = permsconfgroups.getString("groups." + group.getName() + ".servers." + server + ".display", null);
                String sprefix = permsconfgroups.getString("groups." + group.getName() + ".servers." + server + ".prefix", null);
                String ssuffix = permsconfgroups.getString("groups." + group.getName() + ".servers." + server + ".suffix", null);

                //per server world perms
                Map<String, World> worlds = new HashMap<>();
                for (String world : permsconfgroups.getSubNodes("groups." + group.getName() + ".servers." + server + ".worlds")) {
                    List<String> worldgroups = permsconfgroups.getListString("groups." + group + ".servers." + server + ".worlds." + world + ".inheritances", new ArrayList<String>());
                    List<TimedValue<String>> wtimedgroups = getTimed(permsconfgroups, "groups." + group + ".servers." + server + ".worlds." + world + ".timedinheritances");
                    List<String> worldperms = permsconfgroups.getListString("groups." + group.getName() + ".servers." + server + ".worlds." + world + ".permissions", new ArrayList<String>());
                    List<TimedValue<String>> wtimedperms = getTimed(permsconfgroups, "groups." + group.getName() + ".servers." + server + ".worlds." + world + ".timedpermissions");
                    String wdisplay = permsconfgroups.getString("groups." + group.getName() + ".servers." + server + ".worlds." + world + ".display", null);
                    String wprefix = permsconfgroups.getString("groups." + group.getName() + ".servers." + server + ".worlds." + world + ".prefix", null);
                    String wsuffix = permsconfgroups.getString("groups." + group.getName() + ".servers." + server + ".worlds." + world + ".suffix", null);

                    World w = new World(Statics.toLower(world), worldgroups, wtimedgroups, worldperms, wtimedperms, wdisplay, wprefix, wsuffix);
                    worlds.put(Statics.toLower(world), w);
                }

                servers.put(Statics.toLower(server), new Server(Statics.toLower(server), servergroups, stimedgroups, serverperms, stimedperms, worlds, sdisplay, sprefix, ssuffix));
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
        } finally {
            grouplock.unlock();
        }
        group.invalidateCache();
    }

    @Override
    public void reloadUser(User user) {
        userlock.lock();
        try {
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
            for (String server : permsconfusers.getSubNodes("users." + uname + ".servers")) {
                List<String> servergroups = permsconfusers.getListString("users." + uname + ".servers." + server + ".groups", new ArrayList<String>());
                List<TimedValue<String>> stimedgroups = getTimed(permsconfusers, "users." + uname + ".servers." + server + ".timedgroups");
                List<String> serverperms = permsconfusers.getListString("users." + uname + ".servers." + server + ".permissions", new ArrayList<String>());
                List<TimedValue<String>> stimedperms = getTimed(permsconfusers, "users." + uname + ".servers." + server + ".timedpermissions");
                String sdisplay = permsconfusers.getString("users." + uname + ".servers." + server + ".display", null);
                String sprefix = permsconfusers.getString("users." + uname + ".servers." + server + ".prefix", null);
                String ssuffix = permsconfusers.getString("users." + uname + ".servers." + server + ".suffix", null);

                //per server world perms
                Map<String, World> worlds = new HashMap<>();
                for (String world : permsconfusers.getSubNodes("users." + uname + ".servers." + server + ".worlds")) {
                    List<String> worldgroups = permsconfusers.getListString("users." + uname + ".servers." + server + ".worlds." + world + ".groups", new ArrayList<String>());
                    List<TimedValue<String>> wtimedgroups = getTimed(permsconfusers, "users." + uname + ".servers." + server + ".worlds." + world + ".timedgroups");
                    List<String> worldperms = permsconfusers.getListString("users." + uname + ".servers." + server + ".worlds." + world + ".permissions", new ArrayList<String>());
                    List<TimedValue<String>> wtimedperms = getTimed(permsconfusers, "users." + uname + ".servers." + server + ".worlds." + world + ".timedpermissions");
                    String wdisplay = permsconfusers.getString("users." + uname + ".servers." + server + ".worlds." + world + ".display", null);
                    String wprefix = permsconfusers.getString("users." + uname + ".servers." + server + ".worlds." + world + ".prefix", null);
                    String wsuffix = permsconfusers.getString("users." + uname + ".servers." + server + ".worlds." + world + ".suffix", null);

                    World w = new World(Statics.toLower(world), worldgroups, wtimedgroups, worldperms, wtimedperms, wdisplay, wprefix, wsuffix);
                    worlds.put(Statics.toLower(world), w);
                }

                servers.put(Statics.toLower(server), new Server(Statics.toLower(server), servergroups, stimedgroups, serverperms, stimedperms, worlds, sdisplay, sprefix, ssuffix));
            }

            user.setGroups(groups);
            user.setTimedGroups(timedgroups);
            user.setPerms(perms);
            user.setTimedPerms(timedperms);
            user.setDisplay(display);
            user.setPrefix(prefix);
            user.setSuffix(suffix);
            user.setServers(servers);
        } finally {
            userlock.unlock();
        }
        user.invalidateCache();
    }

    private List<TimedValue<String>> getTimed(Config conf, String key) {
        List<TimedValue<String>> ret = new ArrayList();
        for (String value : conf.getSubNodes(key)) {
            Date start = str2date(conf.getString(key + "." + value + ".start", null));
            int dur = conf.getInt(key + "." + value + ".duration", 0);
            if (start == null || dur == 0)
                continue;
            ret.add(new TimedValue(value, start, dur));
        }
        return ret;
    }

    private void setTimed(Config conf, String key, List<TimedValue<String>> values) {
        conf.deleteNode(key);
        for (TimedValue tv : values) {
            conf.setString(key + "." + tv.getValue() + ".start", date2str(tv.getStart()));
            conf.setInt(key + "." + tv.getValue() + ".duration", tv.getDuration());
        }
    }

    private String date2str(Date d) {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(d);
    }

    private Date str2date(String str) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(str);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void removeGroupReferences(Group group) {
        grouplock.lock();
        try {
            permsconfgroups.setAutoSavingEnabled(false);
            for (String g : permsconfgroups.getSubNodes("groups")) {
                removeGroupRef(permsconfgroups, group.getName(), "groups." + g + ".inheritances");
                removeGroupRefTimed(permsconfgroups, group.getName(), "groups." + g + ".timedinheritances");
                for (String server : permsconfgroups.getSubNodes("groups." + g + ".servers")) {
                    removeGroupRef(permsconfgroups, group.getName(), "groups." + g + ".servers." + server + ".inheritances");
                    removeGroupRefTimed(permsconfgroups, group.getName(), "groups." + g + ".servers." + server + ".timedinheritances");
                    for (String world : permsconfgroups.getSubNodes("groups." + g + ".servers")) {
                        removeGroupRef(permsconfgroups, group.getName(), "groups." + g + ".servers." + server + ".worlds." + world + ".inheritances");
                        removeGroupRefTimed(permsconfgroups, group.getName(), "groups." + g + ".servers." + server + ".worlds." + world + ".timedinheritances");
                    }
                }
            }
            permsconfgroups.setAutoSavingEnabled(true);
            permsconfgroups.save();
        } finally {
            grouplock.unlock();
        }

        userlock.lock();
        try {
            permsconfusers.setAutoSavingEnabled(false);
            for (String u : permsconfusers.getSubNodes("users")) {
                removeGroupRef(permsconfusers, group.getName(), "users." + u + ".groups");
                removeGroupRefTimed(permsconfusers, group.getName(), "users." + u + ".timedgroups");
                for (String server : permsconfusers.getSubNodes("users." + u + ".servers")) {
                    removeGroupRef(permsconfusers, group.getName(), "users." + u + ".servers." + server + ".groups");
                    removeGroupRefTimed(permsconfusers, group.getName(), "users." + u + ".servers." + server + ".timedgroups");
                    for (String world : permsconfusers.getSubNodes("users." + u + ".servers")) {
                        removeGroupRef(permsconfusers, group.getName(), "users." + u + ".servers." + server + ".worlds." + world + ".groups");
                        removeGroupRefTimed(permsconfusers, group.getName(), "users." + u + ".servers." + server + ".worlds." + world + ".timedgroups");
                    }
                }
            }
            permsconfusers.setAutoSavingEnabled(true);
            permsconfusers.save();
        } finally {
            userlock.unlock();
        }
    }

    private void removeGroupRef(Config c, String group, String key) {
        List<String> l = c.getListString(key, new ArrayList<String>());
        l.remove(group);
        c.setListString(key, l);
    }

    private void removeGroupRefTimed(Config c, String group, String key) {
        for (String timedref : c.getSubNodes(key))
            if (timedref.equalsIgnoreCase(group))
                c.deleteNode(key + "." + timedref);
    }

    @Override
    public List<BPPermission> getUsersWithPerm(String permission) {
       List<BPPermission> users = new ArrayList<>();
        for (String user : permsconfusers.getSubNodes("users")) {
            for (String perm : permsconfusers.getListString("users." + user + ".permissions", new ArrayList<>())) {
                if (!perm.contains(permission))
                    continue;
                BPPermission bpperm = new BPPermission(perm, user, false, null, null, null, null);
                users.add(bpperm);
            }
            for (String server : permsconfusers.getSubNodes("users." + user + ".servers")) {
                for (String perm : permsconfusers.getListString("users." + user + ".servers." + server + ".permissions", new ArrayList<>())) {
                    if (!perm.contains(permission))
                        continue;
                    BPPermission bpperm = new BPPermission(perm, user, false, server, null, null, null);
                    users.add(bpperm);
                }
                for (String world : permsconfusers.getSubNodes("users." + user + ".servers." + server + ".worlds")) {
                    for (String perm : permsconfusers.getListString("users." + user + ".servers." + server + ".worlds." + world + ".permissions", new ArrayList<>())) {
                        if (!perm.contains(permission))
                            continue;
                        BPPermission bpperm = new BPPermission(perm, user, false, server, world, null, null);
                        users.add(bpperm);
                    }
                }
            }
        }

        return users;
    }

    @Override
    public List<BPPermission> getGroupsWithPerm(String permission) {
       List<BPPermission> groups = new ArrayList<>();
        for (String group : permsconfgroups.getSubNodes("groups")) {
            for (String perm : permsconfgroups.getListString("groups." + group + ".permissions", new ArrayList<>())) {
                if (!perm.contains(permission))
                    continue;
                BPPermission bpperm = new BPPermission(perm, group, true, null, null, null, null);
                groups.add(bpperm);
            }
            for (String server : permsconfgroups.getSubNodes("groups." + group + ".servers")) {
                for (String perm : permsconfgroups.getListString("groups." + group + ".servers." + server + ".permissions", new ArrayList<>())) {
                    if (!perm.contains(permission))
                        continue;
                    BPPermission bpperm = new BPPermission(perm, group, true, server, null, null, null);
                    groups.add(bpperm);
                }
                for (String world : permsconfgroups.getSubNodes("groups." + group + ".servers." + server + ".worlds")) {
                    for (String perm : permsconfgroups.getListString("groups." + group + ".servers." + server + ".worlds." + world + ".permissions", new ArrayList<>())) {
                        if (!perm.contains(permission))
                            continue;
                        BPPermission bpperm = new BPPermission(perm, group, true, server, world, null, null);
                        groups.add(bpperm);
                    }
                }
            }
        }

        return groups;
    }
}
