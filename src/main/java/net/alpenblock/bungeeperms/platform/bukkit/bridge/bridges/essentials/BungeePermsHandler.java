package net.alpenblock.bungeeperms.platform.bukkit.bridge.bridges.essentials;

import com.earth2me.essentials.perm.IPermissionsHandler;
import java.util.ArrayList;
import java.util.List;
import net.alpenblock.bungeeperms.BungeePerms;
import net.alpenblock.bungeeperms.Group;
import net.alpenblock.bungeeperms.Statics;
import net.alpenblock.bungeeperms.User;
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
        User u = perms.getPermissionsManager().getUser(player.getName());
        if (u == null)
            return new ArrayList();

        return new ArrayList(u.getGroupsString());
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
            return false;

        Group g = perms.getPermissionsManager().getGroup(group);
        if (g == null)
            return false;

        return u.getGroups().contains(g);
    }

    @Override
    public boolean hasPermission(Player player, String node)
    {
        return perms.getPermissionsChecker().hasPermOrConsoleOnServerInWorld(new BukkitSender(player), Statics.toLower(node));
    }

    @Override
    public String getPrefix(Player player)
    {
        User u = perms.getPermissionsManager().getUser(player.getName());
        if (u == null)
            return "";

        return u.buildPrefix(new BukkitSender(player));
    }

    @Override
    public String getSuffix(Player player)
    {
        User u = perms.getPermissionsManager().getUser(player.getName());
        if (u == null)
            return "";

        return u.buildSuffix(new BukkitSender(player));
    }

    @Override
    public boolean isPermissionSet(Player player, String string)
    {
        User u = perms.getPermissionsManager().getUser(player.getName());
        if (u == null)
            return false;

        BukkitSender sender = new BukkitSender(player);
        return u.getEffectivePerms(sender.getServer(), sender.getWorld()).contains(string.toLowerCase());
    }

    @Override
    public boolean tryProvider()
    {
        return true;
    }
}
