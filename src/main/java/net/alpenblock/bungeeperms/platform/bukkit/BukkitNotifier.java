package net.alpenblock.bungeeperms.platform.bukkit;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import net.alpenblock.bungeeperms.BungeePerms;
import net.alpenblock.bungeeperms.Group;
import net.alpenblock.bungeeperms.User;
import net.alpenblock.bungeeperms.platform.NetworkNotifier;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
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

            //send config for match checking
            sendConfig(p);
        }
        else
        {
            sendPMAll(msg, origin);
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

            //send config for match checking
            sendConfig(p);
        }
        else
        {
            sendPMAll(msg, origin);
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
            p.sendPluginMessage(BukkitPlugin.getInstance(), BungeePerms.CHANNEL, msg.getBytes());//todo use utf8 encoding

            //send config for match checking
            sendConfig(p);
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

        //send config for match checking
        sendConfig(p);
    }

    private long lastConfigUpdate = 0;

    private void sendConfig(Player p)
    {
        synchronized (this)
        {
            long now = System.currentTimeMillis();
            if (lastConfigUpdate + 5 * 60 * 1000 < now)
            {
                lastConfigUpdate = now;
                p.sendPluginMessage(BukkitPlugin.getInstance(), BungeePerms.CHANNEL, ("configcheck;" + config.getServername() + ";" + config.getBackEndType() + ";" + config.getUUIDPlayerDBType() + ";" + config.isUseUUIDs()).getBytes());
            }
        }
    }
}
