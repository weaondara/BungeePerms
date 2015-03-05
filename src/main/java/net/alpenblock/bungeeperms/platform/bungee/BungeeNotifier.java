package net.alpenblock.bungeeperms.platform.bungee;

import java.util.UUID;
import lombok.AllArgsConstructor;
import net.alpenblock.bungeeperms.BungeePerms;
import net.alpenblock.bungeeperms.Group;
import net.alpenblock.bungeeperms.User;
import net.alpenblock.bungeeperms.platform.NetworkNotifier;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

@AllArgsConstructor
public class BungeeNotifier implements NetworkNotifier
{

    private final BungeeConfig config;

    @Override
    public void deleteUser(User u)
    {
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
        sendPMAll("deleteGroup;" + g.getName());
    }

    //bukkit-bungeeperms reload information functions
    private void sendPM(String player, String msg)
    {
        //if standalone no network messages
        if (config.getNetworkType() == NetworkType.Standalone)
        {
            return;
        }

        ProxiedPlayer pp = BungeeCord.getInstance().getPlayer(player);
        if (pp != null && pp.getServer() != null)
        {
            //ignore servers not in config and netork type is server dependend
            if (config.getNetworkType() == NetworkType.ServerDependend
                    && !config.getNetworkServers().contains(pp.getServer().getInfo().getName()))
            {
                return;
            }
            
            //send message
            pp.getServer().getInfo().sendData(BungeePerms.CHANNEL, msg.getBytes());
        }
    }

    private void sendPM(UUID player, String msg)
    {
        //if standalone no network messages
        if (config.getNetworkType() == NetworkType.Standalone)
        {
            return;
        }
        
        ProxiedPlayer pp = BungeeCord.getInstance().getPlayer(player);
        if (pp != null && pp.getServer() != null)
        {
            //ignore servers not in config and netork type is server dependend
            if (config.getNetworkType() == NetworkType.ServerDependend
                    && !config.getNetworkServers().contains(pp.getServer().getInfo().getName()))
            {
                return;
            }
            
            //send message
            pp.getServer().getInfo().sendData(BungeePerms.CHANNEL, msg.getBytes());
        }
    }

    private void sendPMAll(String msg)
    {
        //if standalone no network messages
        if (config.getNetworkType() == NetworkType.Standalone)
        {
            return;
        }
        
        for (ServerInfo si : BungeeCord.getInstance().config.getServers().values())
        {
            //ignore servers not in config and netork type is server dependend
            if (config.getNetworkType() == NetworkType.ServerDependend
                    && !config.getNetworkServers().contains(si.getName()))
            {
                return;
            }
            
            //send message
            si.sendData(BungeePerms.CHANNEL, msg.getBytes());
        }
    }

    @Override
    public void reloadUser(User u)
    {
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
        sendPMAll("reloadGroup;" + g.getName());
    }

    @Override
    public void reloadAll()
    {
        sendPMAll("reloadall");
    }
}
