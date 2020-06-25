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

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.SneakyThrows;
import net.alpenblock.bungeeperms.BPConfig;
import net.alpenblock.bungeeperms.BungeePerms;
import net.alpenblock.bungeeperms.Group;
import net.alpenblock.bungeeperms.Statics;
import net.alpenblock.bungeeperms.TimedValue;
import net.alpenblock.bungeeperms.User;
import net.alpenblock.bungeeperms.io.upstream.UpstreamIO;
import net.alpenblock.bungeeperms.platform.PlatformPlugin;
import net.alpenblock.bungeeperms.platform.bukkit.BukkitPlugin;

public class UpstreamBackEnd implements BackEnd
{

    private final PlatformPlugin plugin;
    private final BPConfig config;

    @SneakyThrows
    public UpstreamBackEnd()
    {
        plugin = BungeePerms.getInstance().getPlugin();
        config = BungeePerms.getInstance().getConfig();
    }

    @Override
    public BackEndType getType()
    {
        return BackEndType.UPSTREAM;
    }

    @Override //done
    public void load()
    {
        //check connection
        BukkitPlugin.getInstance().getUpstreamio().request("ping");
    }

    @Override //done
    @SneakyThrows
    public List<Group> loadGroups()
    {
        List<Group> ret = new ArrayList<>();

        //send & read groups
        DataInputStream is = BukkitPlugin.getInstance().getUpstreamio().request("loadgroups");
        int len = is.readInt();
        for (int i = 0; i < len; i++)
        {
            Group g = UpstreamIO.readGroup(is);
            ret.add(g);
        }
        Collections.sort(ret);

        return ret;
    }

    @Override //done
    @SneakyThrows
    public List<User> loadUsers()
    {
        List<User> ret = new ArrayList<>();

        DataInputStream is = BukkitPlugin.getInstance().getUpstreamio().request("loadusers");
        int len = is.readInt();
        BungeePerms.getInstance().getDebug().log("loading " + len + " users");
        for (int i = 0; i < len; i++)
        {
            if (i % 1000 == 0)
                BungeePerms.getInstance().getDebug().log("loaded " + i + "/" + len + " users");
            User user = UpstreamIO.readUser(is);
            ret.add(user);
        }

        return ret;
    }

    @Override //done
    @SneakyThrows
    public Group loadGroup(String group)
    {
        DataInputStream is = BukkitPlugin.getInstance().getUpstreamio().request("loadgroup:" + group);
        int len = is.readInt();
        if (len > 0)
            return UpstreamIO.readGroup(is);
        else
            return null;
    }

    @Override //done
    @SneakyThrows
    public User loadUser(String user)
    {
        DataInputStream is = BukkitPlugin.getInstance().getUpstreamio().request("loadusername:" + user);
        int len = is.readInt();
        if (len > 0)
            return UpstreamIO.readUser(is);
        else
            return null;
    }

    @Override //done
    @SneakyThrows
    public User loadUser(UUID user)
    {
        DataInputStream is = BukkitPlugin.getInstance().getUpstreamio().request("loaduseruuid:" + user.toString());
        int len = is.readInt();
        if (len > 0)
            return UpstreamIO.readUser(is);
        else
            return null;
    }

    @Override //done
    @SneakyThrows
    public int loadVersion()
    {
        DataInputStream is = BukkitPlugin.getInstance().getUpstreamio().request("loadversion");
        return is.readInt();
    }

    @Override //done
    public void saveVersion(int version, boolean savetodisk)
    {
        //do nothing
    }

    @Override //done
    @SneakyThrows
    public boolean isUserInDatabase(User user)
    {
        DataInputStream is = BukkitPlugin.getInstance().getUpstreamio().request("userindb:" + (BungeePerms.getInstance().getConfig().isUseUUIDs() ? user.getUUID().toString() : user.getName()));
        return is.readBoolean();
    }

    @Override //done
    public List<String> getRegisteredUsers()
    {
        DataInputStream is = BukkitPlugin.getInstance().getUpstreamio().request("registeredusers");
        return UpstreamIO.readStringList(is);
    }

    @Override //done
    public List<String> getGroupUsers(Group group)
    {
        DataInputStream is = BukkitPlugin.getInstance().getUpstreamio().request("groupusers:" + group.getName());
        return UpstreamIO.readStringList(is);
    }

    @Override //done
    @SneakyThrows
    public synchronized void saveUser(User user, boolean savetodisk)
    {
        if (!BungeePerms.getInstance().getConfig().isSaveAllUsers() && user.isNothingSpecial())
            return;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream os = new DataOutputStream(baos);
        UpstreamIO.writeUser(os, user);
        os.flush();
        BukkitPlugin.getInstance().getUpstreamio().request("saveuser", null, null, baos);
    }

    @Override //done
    @SneakyThrows
    public synchronized void saveGroup(Group group, boolean savetodisk)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream os = new DataOutputStream(baos);
        UpstreamIO.writeGroup(os, group);
        os.flush();
        BukkitPlugin.getInstance().getUpstreamio().request("savegroup", null, null, baos);
    }

    @Override //done
    public synchronized void deleteUser(User user)
    {
        BukkitPlugin.getInstance().getUpstreamio().request("deleteuser:" + (BungeePerms.getInstance().getConfig().isUseUUIDs() ? user.getUUID().toString() : user.getName()));
    }

    @Override //done
    public synchronized void deleteGroup(Group group)
    {
        BukkitPlugin.getInstance().getUpstreamio().request("deletegroup:" + group.getName());
    }

    @Override //done
    @SneakyThrows
    public synchronized void saveUserGroups(User user, String server, String world)
    {
        server = Statics.toLower(server);
        world = server == null ? null : Statics.toLower(world);

        String uname = (BungeePerms.getInstance().getConfig().isUseUUIDs() ? user.getUUID().toString() : user.getName());

        List<String> groups;
        if (server == null)
            groups = user.getGroupsString();
        else if (world == null)
            groups = user.getServer(server).getGroupsString();
        else
            groups = user.getServer(server).getWorld(world).getGroupsString();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream os = new DataOutputStream(baos);
        UpstreamIO.writeStringList(os, groups);
        os.flush();
        BukkitPlugin.getInstance().getUpstreamio().request("saveusergroups:" + uname, server, world, baos);
    }

    @Override //done
    @SneakyThrows
    public synchronized void saveUserTimedGroups(User user, String server, String world)
    {
        server = Statics.toLower(server);
        world = server == null ? null : Statics.toLower(world);

        String uname = (BungeePerms.getInstance().getConfig().isUseUUIDs() ? user.getUUID().toString() : user.getName());

        List<TimedValue<String>> timedgroups;
        if (server == null)
            timedgroups = user.getTimedGroupsString();
        else if (world == null)
            timedgroups = user.getServer(server).getTimedGroupsString();
        else
            timedgroups = user.getServer(server).getWorld(world).getTimedGroupsString();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream os = new DataOutputStream(baos);
        UpstreamIO.writeStringListTimed(os, timedgroups);
        os.flush();
        BukkitPlugin.getInstance().getUpstreamio().request("saveusertimedgroups:" + uname, server, world, baos);
    }

    @Override //done
    @SneakyThrows
    public synchronized void saveUserPerms(User user, String server, String world)
    {
        server = Statics.toLower(server);
        world = server == null ? null : Statics.toLower(world);

        String uname = (BungeePerms.getInstance().getConfig().isUseUUIDs() ? user.getUUID().toString() : user.getName());

        List<String> perms;
        if (server == null)
            perms = user.getPerms();
        else if (world == null)
            perms = user.getServer(server).getPerms();
        else
            perms = user.getServer(server).getWorld(world).getPerms();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream os = new DataOutputStream(baos);
        UpstreamIO.writeStringList(os, perms);
        os.flush();
        BukkitPlugin.getInstance().getUpstreamio().request("saveuserperms:" + uname, server, world, baos);
    }

    @Override //done
    @SneakyThrows
    public synchronized void saveUserTimedPerms(User user, String server, String world)
    {
        server = Statics.toLower(server);
        world = server == null ? null : Statics.toLower(world);

        String uname = (BungeePerms.getInstance().getConfig().isUseUUIDs() ? user.getUUID().toString() : user.getName());

        List<TimedValue<String>> timedperms;
        if (server == null)
            timedperms = user.getTimedPerms();
        else if (world == null)
            timedperms = user.getServer(server).getTimedPerms();
        else
            timedperms = user.getServer(server).getWorld(world).getTimedPerms();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream os = new DataOutputStream(baos);
        UpstreamIO.writeStringListTimed(os, timedperms);
        os.flush();
        BukkitPlugin.getInstance().getUpstreamio().request("saveusertimedperms:" + uname, server, world, baos);
    }

    @Override //done
    @SneakyThrows
    public synchronized void saveUserDisplay(User user, String server, String world)
    {
        server = Statics.toLower(server);
        world = Statics.toLower(world);

        String uname = (BungeePerms.getInstance().getConfig().isUseUUIDs() ? user.getUUID().toString() : user.getName());

        String display = user.getDisplay();
        if (server != null)
        {
            display = user.getServer(server).getDisplay();
            if (world != null)
            {
                display = user.getServer(server).getWorld(world).getDisplay();
            }
        }

        BukkitPlugin.getInstance().getUpstreamio().request("saveuserdisplay:" + uname + ":" + display, server, world, new ByteArrayOutputStream());
    }

    @Override //done
    @SneakyThrows
    public synchronized void saveUserPrefix(User user, String server, String world)
    {
        server = Statics.toLower(server);
        world = Statics.toLower(world);

        String uname = (BungeePerms.getInstance().getConfig().isUseUUIDs() ? user.getUUID().toString() : user.getName());

        String prefix = user.getPrefix();
        if (server != null)
        {
            prefix = user.getServer(server).getPrefix();
            if (world != null)
            {
                prefix = user.getServer(server).getWorld(world).getPrefix();
            }
        }

        BukkitPlugin.getInstance().getUpstreamio().request("saveuserprefix:" + uname + ":" + prefix, server, world, new ByteArrayOutputStream());
    }

    @Override //done
    @SneakyThrows
    public synchronized void saveUserSuffix(User user, String server, String world)
    {
        server = Statics.toLower(server);
        world = Statics.toLower(world);

        String uname = (BungeePerms.getInstance().getConfig().isUseUUIDs() ? user.getUUID().toString() : user.getName());

        String suffix = user.getSuffix();
        if (server != null)
        {
            suffix = user.getServer(server).getSuffix();
            if (world != null)
            {
                suffix = user.getServer(server).getWorld(world).getSuffix();
            }
        }

        BukkitPlugin.getInstance().getUpstreamio().request("saveusersuffix:" + uname + ":" + suffix, server, world, new ByteArrayOutputStream());
    }

    @Override //done
    @SneakyThrows
    public synchronized void saveGroupPerms(Group group, String server, String world)
    {
        server = Statics.toLower(server);
        world = server == null ? null : Statics.toLower(world);

        List<String> perms;
        if (server == null)
            perms = group.getPerms();
        else if (world == null)
            perms = group.getServer(server).getPerms();
        else
            perms = group.getServer(server).getWorld(world).getPerms();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream os = new DataOutputStream(baos);
        UpstreamIO.writeStringList(os, perms);
        os.flush();
        BukkitPlugin.getInstance().getUpstreamio().request("savegroupperms:" + group.getName(), server, world, baos);
    }

    @Override //done
    @SneakyThrows
    public synchronized void saveGroupTimedPerms(Group group, String server, String world)
    {
        server = Statics.toLower(server);
        world = server == null ? null : Statics.toLower(world);

        List<TimedValue<String>> timedperms;
        if (server == null)
            timedperms = group.getTimedPerms();
        else if (world == null)
            timedperms = group.getServer(server).getTimedPerms();
        else
            timedperms = group.getServer(server).getWorld(world).getTimedPerms();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream os = new DataOutputStream(baos);
        UpstreamIO.writeStringListTimed(os, timedperms);
        os.flush();
        BukkitPlugin.getInstance().getUpstreamio().request("savegrouptimedperms:" + group.getName(), server, world, baos);
    }

    @Override //done
    @SneakyThrows
    public synchronized void saveGroupInheritances(Group group, String server, String world)
    {
        server = Statics.toLower(server);
        world = server == null ? null : Statics.toLower(world);

        List<String> inheritances;
        if (server == null)
            inheritances = group.getInheritancesString();
        else if (world == null)
            inheritances = group.getServer(server).getGroupsString();
        else
            inheritances = group.getServer(server).getWorld(world).getGroupsString();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream os = new DataOutputStream(baos);
        UpstreamIO.writeStringList(os, inheritances);
        os.flush();
        BukkitPlugin.getInstance().getUpstreamio().request("savegroupinherits:" + group.getName(), server, world, baos);
    }

    @Override //done
    @SneakyThrows
    public synchronized void saveGroupTimedInheritances(Group group, String server, String world)
    {
        server = Statics.toLower(server);
        world = server == null ? null : Statics.toLower(world);

        List<TimedValue<String>> timedinheritances;
        if (server == null)
            timedinheritances = group.getTimedInheritancesString();
        else if (world == null)
            timedinheritances = group.getServer(server).getTimedGroupsString();
        else
            timedinheritances = group.getServer(server).getWorld(world).getTimedGroupsString();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream os = new DataOutputStream(baos);
        UpstreamIO.writeStringListTimed(os, timedinheritances);
        os.flush();
        BukkitPlugin.getInstance().getUpstreamio().request("savegrouptimedinherits:" + group.getName(), server, world, baos);
    }

    @Override //done
    public synchronized void saveGroupLadder(Group group)
    {
        BukkitPlugin.getInstance().getUpstreamio().request("savegroupladder:" + group.getName() + ":" + group.getLadder());
    }

    @Override //done
    public synchronized void saveGroupRank(Group group)
    {
        BukkitPlugin.getInstance().getUpstreamio().request("savegrouprank:" + group.getName() + ":" + group.getRank());
    }

    @Override //done
    public synchronized void saveGroupWeight(Group group)
    {
        BukkitPlugin.getInstance().getUpstreamio().request("savegroupweight:" + group.getName() + ":" + group.getWeight());
    }

    @Override //done
    public synchronized void saveGroupDefault(Group group)
    {
        BukkitPlugin.getInstance().getUpstreamio().request("savegroupdefault:" + group.getName() + ":" + group.isDefault());
    }

    @Override //done
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
        BukkitPlugin.getInstance().getUpstreamio().request("savegroupdisplay:" + group.getName() + ":" + display, server, world, new ByteArrayOutputStream());
    }

    @Override //done
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
        BukkitPlugin.getInstance().getUpstreamio().request("savegroupprefix:" + group.getName() + ":" + prefix, server, world, new ByteArrayOutputStream());
    }

    @Override //done
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
        BukkitPlugin.getInstance().getUpstreamio().request("savegroupsuffix:" + group.getName() + ":" + suffix, server, world, new ByteArrayOutputStream());
    }

    @Override //done
    public synchronized void format(List<Group> groups, List<User> users, int version)
    {
        BukkitPlugin.getInstance().getUpstreamio().request("format");
    }

    @Override //done
    @SneakyThrows
    public synchronized int cleanup(List<Group> groups, List<User> users, int version)
    {
        DataInputStream is = BukkitPlugin.getInstance().getUpstreamio().request("cleanup");
        return is.readInt();
    }

    @Override //done
    public void clearDatabase()
    {
        load();
    }

    @Override //done
    public void reloadGroup(Group group)
    {
        Group loaded = loadGroup(group.getName());
        if (loaded == null)
            return;

        group.setInheritances(loaded.getInheritancesString());
        group.setTimedInheritances(loaded.getTimedInheritancesString());
        group.setPerms(loaded.getPerms());
        group.setTimedPerms(loaded.getTimedPerms());
        group.setIsdefault(loaded.isDefault());
        group.setRank(loaded.getRank());
        group.setWeight(loaded.getWeight());
        group.setLadder(loaded.getLadder());
        group.setDisplay(loaded.getDisplay());
        group.setPrefix(loaded.getPrefix());
        group.setSuffix(loaded.getSuffix());
        group.setServers(loaded.getServers());
        group.invalidateCache();
    }

    @Override //done
    public void reloadUser(User user)
    {
        String uname = config.isUseUUIDs() ? user.getUUID().toString() : user.getName();
        User loaded = loadUser(uname);
        if (loaded == null)
            return;

        user.setGroups(loaded.getGroupsString());
        user.setTimedGroups(loaded.getTimedGroupsString());
        user.setPerms(loaded.getPerms());
        user.setTimedPerms(loaded.getTimedPerms());
        user.setDisplay(loaded.getDisplay());
        user.setPrefix(loaded.getPrefix());
        user.setSuffix(loaded.getSuffix());
        user.setServers(loaded.getServers());
        user.invalidateCache();
    }

    @Override //done
    public void removeGroupReferences(Group group)
    {
        BukkitPlugin.getInstance().getUpstreamio().request("delgrouprefs:" + group.getName());
    }
}
