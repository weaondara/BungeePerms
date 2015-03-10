package net.alpenblock.bungeeperms.platform.bungee;

import java.util.UUID;
import lombok.AllArgsConstructor;
import net.alpenblock.bungeeperms.BungeePerms;
import net.alpenblock.bungeeperms.Group;
import net.alpenblock.bungeeperms.User;
import net.alpenblock.bungeeperms.platform.NetworkNotifier;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

@AllArgsConstructor
public class BungeeNotifier implements NetworkNotifier
{

    private final BungeeConfig config;

    @Override
    public void deleteUser(User u, String origin)
    {
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
        sendPMAll("deleteGroup;" + g.getName(), origin);
    }

    @Override
    public void reloadUser(User u, String origin)
    {
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
        sendPMAll("reloadGroup;" + g.getName(), origin);
    }

    @Override
    public void reloadUsers(String origin)
    {
        sendPMAll("reloadUsers" , origin);
    }

    @Override
    public void reloadGroups(String origin)
    {
        sendPMAll("reloadGroups", origin);
    }

    @Override
    public void reloadAll(String origin)
    {
        sendPMAll("reloadall", origin);
    }

    //bukkit-bungeeperms reload information functions
    private void sendPM(String player, String msg, String origin)
    {
        //if standalone no network messages
        if (config.getNetworkType() == NetworkType.Standalone)
        {
            return;
        }

        ProxiedPlayer pp = ProxyServer.getInstance().getPlayer(player);
        if (pp != null && pp.getServer() != null)
        {
            //ignore servers not in config and netork type is server dependend
            if (config.getNetworkType() == NetworkType.ServerDependend
                    && !config.getNetworkServers().contains(pp.getServer().getInfo().getName()))
            {
                return;
            }

            //no feedback loop
            if (origin != null && pp.getServer().getInfo().getName().equalsIgnoreCase(origin))
            {
                return;
            }

            //send message
            pp.getServer().getInfo().sendData(BungeePerms.CHANNEL, msg.getBytes());
        }
    }

    private void sendPM(UUID player, String msg, String origin)
    {
        //if standalone no network messages
        if (config.getNetworkType() == NetworkType.Standalone)
        {
            return;
        }

        ProxiedPlayer pp = ProxyServer.getInstance().getPlayer(player);
        if (pp != null && pp.getServer() != null)
        {
            //ignore servers not in config and netork type is server dependend
            if (config.getNetworkType() == NetworkType.ServerDependend
                    && !config.getNetworkServers().contains(pp.getServer().getInfo().getName()))
            {
                return;
            }

            //no feedback loop
            if (origin != null && pp.getServer().getInfo().getName().equalsIgnoreCase(origin))
            {
                return;
            }

            //send message
            pp.getServer().getInfo().sendData(BungeePerms.CHANNEL, msg.getBytes());
        }
    }

    private void sendPMAll(String msg, String origin)
    {
        //if standalone no network messages
        if (config.getNetworkType() == NetworkType.Standalone)
        {
            return;
        }

        for (ServerInfo si : ProxyServer.getInstance().getConfig().getServers().values())
        {
            //ignore servers not in config and netork type is server dependend
            if (config.getNetworkType() == NetworkType.ServerDependend
                    && !config.getNetworkServers().contains(si.getName()))
            {
                return;
            }

            //no feedback loop
            if (origin != null && si.getName().equalsIgnoreCase(origin))
            {
                continue;
            }

            //send message
            si.sendData(BungeePerms.CHANNEL, msg.getBytes());
        }
    }
}
