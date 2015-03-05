package net.alpenblock.bungeeperms.platform.bukkit.bridge.bridges.essentials;

import com.earth2me.essentials.perm.IPermissionsHandler;
import java.util.ArrayList;
import java.util.List;
import net.alpenblock.bungeeperms.BungeePerms;
import net.alpenblock.bungeeperms.Group;
import net.alpenblock.bungeeperms.User;
import net.alpenblock.bungeeperms.platform.bukkit.BukkitConfig;
import org.bukkit.entity.Player;

class BungeePermsHandler implements IPermissionsHandler
{

    private final BungeePerms perms;

    public BungeePermsHandler()
    {
        perms = BungeePerms.getInstance();
    }

    @Override
    public String getGroup(Player player)
    {
        User u = perms.getPermissionsManager().getUser(player.getName());
        if (u == null)
        {
            return "";
        }
        Group g = perms.getPermissionsManager().getMainGroup(u);
        return g == null ? "" : g.getName();
    }

    @Override
    public List<String> getGroups(Player player)
    {
        List<String> groups = new ArrayList<>();

        User u = perms.getPermissionsManager().getUser(player.getName());
        if (u == null)
        {
            return groups;
        }

        for (Group g : u.getGroups())
        {
            groups.add(g.getName());
        }

        return groups;
    }

    @Override
    public boolean canBuild(Player player, String group)
    {
        return true;
    }

    @Override
    public boolean inGroup(Player player, String group)
    {
        User u = perms.getPermissionsManager().getUser(player.getName());
        if (u == null)
        {
            return false;
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
    public boolean hasPermission(Player player, String node)
    {
        BukkitConfig config = (BukkitConfig) perms.getConfig();
        return perms.getPermissionsChecker().hasPermOrConsoleOnServerInWorld(player.getName(), node, config.getServername(), player.getWorld().getName());
    }

    @Override
    public String getPrefix(Player player)
    {
        String prefix = "";

        User u = perms.getPermissionsManager().getUser(player.getName());
        if (u == null)
        {
            return "";
        }
        for (Group g : u.getGroups())
        {
            prefix += g.getPrefix() + (g.getPrefix().isEmpty() ? "" : " ");
        }

        return prefix;
    }

    @Override
    public String getSuffix(Player player)
    {
        String suffix = "";

        User u = perms.getPermissionsManager().getUser(player.getName());
        if (u == null)
        {
            return "";
        }
        for (Group g : u.getGroups())
        {
            suffix += g.getSuffix() + (g.getSuffix().isEmpty() ? "" : " ");
        }

        return suffix;
    }
}
