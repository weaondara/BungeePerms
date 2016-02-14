package net.alpenblock.bungeeperms.platform.bukkit.bridge.bridges.worldedit;

import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.wepif.PermissionsResolver;
import java.util.List;
import net.alpenblock.bungeeperms.BungeePerms;
import net.alpenblock.bungeeperms.PermissionsManager;
import net.alpenblock.bungeeperms.Statics;
import net.alpenblock.bungeeperms.User;
import net.alpenblock.bungeeperms.platform.bukkit.BukkitConfig;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;

public class BungeePermsResolver implements PermissionsResolver
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

            return new BungeePermsResolver(server, manager);
        }
        catch (Throwable t)
        {
            return null;
        }
    }

    public BungeePermsResolver(Server server, PermissionsManager manager)
    {
        this.manager = manager;
    }

    @Override
    public boolean hasPermission(String player, String permission)
    {
        BukkitConfig config = (BukkitConfig) BungeePerms.getInstance().getConfig();
        return BungeePerms.getInstance().getPermissionsChecker().hasPermOnServer(player, Statics.toLower(permission), Statics.toLower(config.getServername()));
    }

    @Override
    public boolean hasPermission(String worldName, String name, String permission)
    {
        BukkitConfig config = (BukkitConfig) BungeePerms.getInstance().getConfig();
        return BungeePerms.getInstance().getPermissionsChecker().hasPermOnServerInWorld(name, Statics.toLower(permission), Statics.toLower(config.getServername()), Statics.toLower(worldName));
    }

    @Override
    public boolean hasPermission(OfflinePlayer player, String permission)
    {
        return hasPermission(player.getName(), permission);
    }

    @Override
    public boolean hasPermission(String worldName, OfflinePlayer player, String permission)
    {
        return hasPermission(worldName, player.getName(), permission);
    }

    @Override
    public boolean inGroup(String player, String group)
    {
        return manager.getUser(player).getGroups().contains(BungeePerms.getInstance().getPermissionsManager().getGroup(group));
    }

    @Override
    public boolean inGroup(OfflinePlayer player, String group)
    {
        return inGroup(player.getName(), group);
    }

    @Override
    public String[] getGroups(String player)
    {
        User user = manager.getUser(player);
        if (user == null)
        {
            return new String[0];
        }

        List<String> groups = user.getGroupsString();
        return groups.toArray(new String[groups.size()]);
    }

    @Override
    public String[] getGroups(OfflinePlayer player)
    {
        return getGroups(player.getName());
    }

    @Override
    public String getDetectionMessage()
    {
        String pluginname = BungeePerms.getInstance().getPlugin().getPluginName();
        return pluginname + " detected! Using " + pluginname + " for permissions.";
    }

    @Override
    public void load()
    {
    }
}
