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
        ProxiedPlayer pp = BungeeCord.getInstance().getPlayer(player);
        if (pp != null && pp.getServer() != null)
        {
            pp.getServer().getInfo().sendData(BungeePerms.CHANNEL, msg.getBytes());
        }
    }

    private void sendPM(UUID player, String msg)
    {
        ProxiedPlayer pp = BungeeCord.getInstance().getPlayer(player);
        if (pp != null && pp.getServer() != null)
        {
            pp.getServer().getInfo().sendData(BungeePerms.CHANNEL, msg.getBytes());
        }
    }

    private void sendPMAll(String msg)
    {
        for (ServerInfo si : BungeeCord.getInstance().config.getServers().values())
        {
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
