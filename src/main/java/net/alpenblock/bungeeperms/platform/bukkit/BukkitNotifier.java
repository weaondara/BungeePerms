package net.alpenblock.bungeeperms.platform.bukkit;

import java.util.UUID;
import lombok.AllArgsConstructor;
import net.alpenblock.bungeeperms.BungeePerms;
import net.alpenblock.bungeeperms.Group;
import net.alpenblock.bungeeperms.User;
import net.alpenblock.bungeeperms.platform.NetworkNotifier;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@AllArgsConstructor
public class BukkitNotifier implements NetworkNotifier
{

    private final BukkitConfig config;

    @Override
    public void deleteUser(User u, String origin)
    {
        //if standalone don't notify bungee
        if (config.isStandalone())
        {
            return;
        }

        if (config.isUseUUIDs())
        {
            sendPM(u.getUUID(), "deleteUser;" + u.getUUID(), origin);
        }
        else
        {
            sendPM(u.getName(), "deleteUser;" + u.getName(), origin);
        }
    }

    @Override
    public void deleteGroup(Group g, String origin)
    {
        //if standalone don't notify bungee
        if (config.isStandalone())
        {
            return;
        }

        sendPMAll("deleteGroup;" + g.getName(), origin);
    }

    @Override
    public void reloadUser(User u, String origin)
    {
        //if standalone don't notify bungee
        if (config.isStandalone())
        {
            return;
        }

        if (config.isUseUUIDs())
        {
            sendPM(u.getUUID(), "reloadUser;" + u.getUUID(), origin);
        }
        else
        {
            sendPM(u.getName(), "reloadUser;" + u.getName(), origin);
        }
    }

    @Override
    public void reloadGroup(Group g, String origin)
    {
        //if standalone don't notify bungee
        if (config.isStandalone())
        {
            return;
        }

        sendPMAll("reloadGroup;" + g.getName(), origin);
    }

    @Override
    public void reloadUsers(String origin)
    {
        //if standalone don't notify bungee
        if (config.isStandalone())
        {
            return;
        }

        sendPMAll("reloadUsers;", origin);
    }

    @Override
    public void reloadGroups(String origin)
    {
        //if standalone don't notify bungee
        if (config.isStandalone())
        {
            return;
        }

        sendPMAll("reloadGroups", origin);
    }

    @Override
    public void reloadAll(String origin)
    {
        //if standalone don't notify bungee
        if (config.isStandalone())
        {
            return;
        }

        sendPMAll("reloadall", origin);
    }

    //bukkit-bungeeperms reload information functions
    private void sendPM(String player, String msg, String origin)
    {
        //if standalone don't notify bungee
        if (config.isStandalone())
        {
            return;
        }

        Player p = Bukkit.getPlayer(player);
        if (p != null)
        {
            p.sendPluginMessage(BukkitPlugin.getInstance(), BungeePerms.CHANNEL, msg.getBytes());
        }
    }

    private void sendPM(UUID player, String msg, String origin)
    {
        //if standalone don't notify bungee
        if (config.isStandalone())
        {
            return;
        }

        Player p = Bukkit.getPlayer(player);
        if (p != null)
        {
            p.sendPluginMessage(BukkitPlugin.getInstance(), BungeePerms.CHANNEL, msg.getBytes());
        }
    }

    private void sendPMAll(String msg, String origin)
    {
        //if standalone don't notify bungee
        if (config.isStandalone())
        {
            return;
        }

        Player p = Bukkit.getOnlinePlayers().iterator().hasNext() ? Bukkit.getOnlinePlayers().iterator().next() : null;
        if (p != null)
        {
            p.sendPluginMessage(BukkitPlugin.getInstance(), BungeePerms.CHANNEL, msg.getBytes());
        }
    }

    public void sendWorldUpdate(Player p)
    {
        //if standalone don't notify bungee
        if (config.isStandalone())
        {
            return;
        }

        String world = p.getWorld() == null ? "" : p.getWorld().getName();
        p.sendPluginMessage(BukkitPlugin.getInstance(), BungeePerms.CHANNEL, ("playerworldupdate;" + p.getName() + ";" + world).getBytes());
    }
}
