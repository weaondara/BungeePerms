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
    public void deleteUser(User u)
    {
        //if standalone don't notify bungee
        if(config.isStandalone())
        {
            return;
        }
        
        if (config.isUseUUIDs())
        {
            sendPM(u.getUUID(), "deleteUser;" + u.getUUID());
        }
        else
        {
            sendPM(u.getName(), "deleteUser;" + u.getName());
        }
    }

    @Override
    public void deleteGroup(Group g)
    {
        //if standalone don't notify bungee
        if(config.isStandalone())
        {
            return;
        }
        
        sendPMAll("deleteGroup;" + g.getName());
    }

    //bukkit-bungeeperms reload information functions
    private void sendPM(String player, String msg)
    {
        Player p = Bukkit.getPlayer(player);
        if (p != null)
        {
            p.sendPluginMessage(BukkitPlugin.getInstance(), BungeePerms.CHANNEL, msg.getBytes());
        }
    }

    private void sendPM(UUID player, String msg)
    {
        Player p = Bukkit.getPlayer(player);
        if (p != null)
        {
            p.sendPluginMessage(BukkitPlugin.getInstance(), BungeePerms.CHANNEL, msg.getBytes());
        }
    }

    private void sendPMAll(String msg)
    {
        Player p = Bukkit.getOnlinePlayers().iterator().hasNext() ? Bukkit.getOnlinePlayers().iterator().next() : null;
        if (p != null)
        {
            p.sendPluginMessage(BukkitPlugin.getInstance(), BungeePerms.CHANNEL, msg.getBytes());
        }
    }

    @Override
    public void reloadUser(User u)
    {
        //if standalone don't notify bungee
        if(config.isStandalone())
        {
            return;
        }
        
        if (config.isUseUUIDs())
        {
            sendPM(u.getUUID(), "reloadUser;" + u.getUUID());
        }
        else
        {
            sendPM(u.getName(), "reloadUser;" + u.getName());
        }
    }

    @Override
    public void reloadGroup(Group g)
    {
        //if standalone don't notify bungee
        if(config.isStandalone())
        {
            return;
        }
        
        sendPMAll("reloadGroup;" + g.getName());
    }

    @Override
    public void reloadAll()
    {
        //if standalone don't notify bungee
        if(config.isStandalone())
        {
            return;
        }
        
        sendPMAll("reloadall");
    }

    public void sendWorldUpdate(Player p)
    {
        //if standalone don't notify bungee
        if(config.isStandalone())
        {
            return;
        }
        
        p.sendPluginMessage(BukkitPlugin.getInstance(), BungeePerms.CHANNEL, ("playerworldupdate;" + p.getName() + ";" + p.getWorld().getName()).getBytes());
    }
}
