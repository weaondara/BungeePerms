package net.alpenblock.bungeeperms.platform.bukkit.bridge.bridges.vault;

import java.util.ArrayList;
import java.util.List;
import net.alpenblock.bungeeperms.BungeePerms;
import net.alpenblock.bungeeperms.Group;
import net.alpenblock.bungeeperms.User;
import net.alpenblock.bungeeperms.platform.bukkit.BukkitConfig;
import net.alpenblock.bungeeperms.platform.bukkit.BukkitPlugin;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;

public class Permission_BungeePermsBukkit extends Permission
{

    private final String name = "BungeePermsBukkit";

    protected Plugin plugin = null;
    protected Plugin perms = null;

    public Permission_BungeePermsBukkit(Plugin plugin)
    {
        super();
        this.plugin = plugin;
        Bukkit.getServer().getPluginManager().registerEvents(new PermissionServerListener(this), BukkitPlugin.getInstance());

        // Load Plugin in case it was loaded before
        if (perms == null)
        {
            perms = plugin.getServer().getPluginManager().getPlugin("BungeePermsBukkit");
            if (perms != null && perms.isEnabled())
            {
                log.info(String.format("[%s][Permission] %s hooked.", plugin.getDescription().getName(), name));
            }
        }
    }

    public class PermissionServerListener implements Listener
    {

        Permission_BungeePermsBukkit permission = null;

        public PermissionServerListener(Permission_BungeePermsBukkit permission)
        {
            this.permission = permission;
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onPluginEnable(PluginEnableEvent event)
        {
            if (permission.perms == null)
            {
                Plugin perms = event.getPlugin();
                if (perms.getDescription().getName().equals("BungeePermsBukkit"))
                {
                    permission.perms = BukkitPlugin.getInstance();
                    log.info(String.format("[%s][Permission] %s hooked.", plugin.getDescription().getName(), permission.name));
                }
            }
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onPluginDisable(PluginDisableEvent event)
        {
            if (permission.perms != null)
            {
                if (event.getPlugin().getDescription().getName().equals("BungeePermsBukkit"))
                {
                    permission.perms = null;
                    log.info(String.format("[%s][Permission] %s un-hooked.", plugin.getDescription().getName(), permission.name));
                }
            }
        }
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public boolean isEnabled()
    {
        return perms != null && perms.isEnabled();
    }

    @Override
    public boolean hasSuperPermsCompat()
    {
        return true;
    }

    @Override
    public boolean playerHas(String world, String player, String permission)
    {
        BukkitConfig config = (BukkitConfig) BungeePerms.getInstance().getConfig();
        return BungeePerms.getInstance().getPermissionsChecker().hasPermOrConsoleOnServerInWorld(player, permission, config.getServername(), world);
    }

    @Override
    public boolean playerAdd(String world, String player, String permission)
    {
        return false;
    }

    @Override
    public boolean playerRemove(String world, String player, String permission)
    {
        return false;
    }

    @Override
    public boolean groupHas(String world, String group, String permission)
    {
        Group g = BungeePerms.getInstance().getPermissionsManager().getGroup(group);
        if (g == null)
        {
            throw new NullPointerException("group " + group + " doesn't exist");
        }
        
        BukkitConfig config = (BukkitConfig) BungeePerms.getInstance().getConfig();
        return g.hasOnServerInWorld(permission, config.getServername(), world);
    }

    @Override
    public boolean groupAdd(String world, String group, String permission)
    {
        return false;
    }

    @Override
    public boolean groupRemove(String world, String group, String permission)
    {
        return false;
    }

    @Override
    public boolean playerInGroup(String world, String player, String group)
    {
        User u = BungeePerms.getInstance().getPermissionsManager().getUser(player);
        if (u == null)
        {
            throw new NullPointerException("player " + player + " doesn't exist");
        }
        for (Group g : u.getGroups())
        {
            if (g.getName().equalsIgnoreCase(group))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean playerAddGroup(String world, String player, String group)
    {
        return false;
    }

    @Override
    public boolean playerRemoveGroup(String world, String player, String group)
    {
        return false;
    }

    @Override
    public String[] getPlayerGroups(String world, String player)
    {
        User u = BungeePerms.getInstance().getPermissionsManager().getUser(player);
        if (u == null)
        {
            throw new NullPointerException("player " + player + " doesn't exist");
        }
        List<String> groups = new ArrayList<>();
        for (Group g : u.getGroups())
        {
            groups.add(g.getName());
        }

        return groups.toArray(new String[groups.size()]);
    }

    @Override
    public String getPrimaryGroup(String world, String player)
    {
        User u = BungeePerms.getInstance().getPermissionsManager().getUser(player);
        if (u == null)
        {
            throw new NullPointerException("player " + player + " doesn't exist");
        }
        Group g = BungeePerms.getInstance().getPermissionsManager().getMainGroup(u);
        return g != null ? g.getName() : null;
    }

    @Override
    public String[] getGroups()
    {
        List<String> groups = new ArrayList<>();
        for (Group g : BungeePerms.getInstance().getPermissionsManager().getGroups())
        {
            groups.add(g.getName());
        }
        return groups.toArray(new String[groups.size()]);
    }

    @Override
    public boolean hasGroupSupport()
    {
        return true;
    }
}
