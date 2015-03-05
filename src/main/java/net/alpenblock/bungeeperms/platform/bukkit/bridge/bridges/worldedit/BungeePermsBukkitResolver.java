package net.alpenblock.bungeeperms.platform.bukkit.bridge.bridges.worldedit;

import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.wepif.DinnerPermsResolver;
import com.sk89q.wepif.PermissionsResolver;
import net.alpenblock.bungeeperms.BungeePerms;
import net.alpenblock.bungeeperms.PermissionsManager;
import net.alpenblock.bungeeperms.User;
import net.alpenblock.bungeeperms.platform.bukkit.BukkitConfig;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.permissions.Permissible;

public class BungeePermsBukkitResolver extends DinnerPermsResolver
{

    private final PermissionsManager manager;

    public static PermissionsResolver factory(Server server, YAMLProcessor config)
    {
        try
        {
            PermissionsManager manager = BungeePerms.getInstance().getPermissionsManager();

            if (manager == null)
            {
                return null;
            }

            return new BungeePermsBukkitResolver(server, manager);
        }
        catch (Throwable t)
        {
            return null;
        }
    }

    public BungeePermsBukkitResolver(Server server, PermissionsManager manager)
    {
        super(server);
        this.manager = manager;
    }

    @Override
    public boolean hasPermission(String worldName, String name, String permission)
    {
        BukkitConfig config = (BukkitConfig) BungeePerms.getInstance().getConfig();
        return BungeePerms.getInstance().getPermissionsChecker().hasPermOnServerInWorld(name, permission, config.getServername(), worldName);
    }

    @Override
    public boolean hasPermission(OfflinePlayer player, String permission)
    {
        Permissible permissible = getPermissible(player);
        if (permissible == null)
        {
            return BungeePerms.getInstance().getPermissionsChecker().hasPerm(player.getName(), permission);
        }
        else
        {
            return permissible.hasPermission(permission);
        }
    }

    @Override
    public boolean hasPermission(String worldName, OfflinePlayer player, String permission)
    {
        return hasPermission(worldName, player.getName(), permission);
    }

    @Override
    public boolean inGroup(OfflinePlayer player, String group)
    {
        return super.inGroup(player, group)
                || manager.getUser(player.getName()).getGroups().contains(BungeePerms.getInstance().getPermissionsManager().getGroup(group));
    }

    @Override
    public String[] getGroups(OfflinePlayer player)
    {
        if (getPermissible(player) == null)
        {
            User user = manager.getUser(player.getName());
            if (user == null)
            {
                return new String[0];
            }

            String[] groups = new String[user.getGroups().size()];
            for (int i = 0; i < user.getGroups().size(); i++)
            {
                groups[i] = user.getGroups().get(i).getName();
            }

            return groups;
        }
        else
        {
            return super.getGroups(player);
        }
    }

    @Override
    public String getDetectionMessage()
    {
        return "BungeePermsBukkit detected! Using BungeePermsBukkit for permissions.";
    }
}
