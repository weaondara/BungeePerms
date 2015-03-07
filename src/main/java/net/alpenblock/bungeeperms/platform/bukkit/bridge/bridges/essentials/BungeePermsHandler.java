package net.alpenblock.bungeeperms.platform.bukkit.bridge.bridges.essentials;

import com.earth2me.essentials.perm.IPermissionsHandler;
import java.util.ArrayList;
import java.util.List;
import net.alpenblock.bungeeperms.BungeePerms;
import net.alpenblock.bungeeperms.Group;
import net.alpenblock.bungeeperms.Server;
import net.alpenblock.bungeeperms.User;
import net.alpenblock.bungeeperms.World;
import net.alpenblock.bungeeperms.platform.bukkit.BukkitConfig;
import net.alpenblock.bungeeperms.platform.bukkit.BukkitSender;
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
        return perms.getPermissionsChecker().hasPermOrConsoleOnServerInWorld(new BukkitSender(player), node);
    }

    @Override
    public String getPrefix(Player player)
    {
        User u = perms.getPermissionsManager().getUser(player.getName());
        if (u == null)
        {
            return "";
        }
        
        return u.buildPrefix(new BukkitSender(player));
    }

    @Override
    public String getSuffix(Player player)
    {
        User u = perms.getPermissionsManager().getUser(player.getName());
        if (u == null)
        {
            return "";
        }
        
        return u.buildSuffix(new BukkitSender(player));
    }
}
