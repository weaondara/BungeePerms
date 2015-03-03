package net.alpenblock.bungeeperms.platform.bungee;

import net.alpenblock.bungeeperms.BungeePerms;
import net.alpenblock.bungeeperms.platform.PluginMessageSender;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.config.ServerInfo;

public class BungeePluginMessageSender implements PluginMessageSender
{
    
    @Override
    public void sendPluginMessage(String target, String channel, String msg)
    {
        ServerInfo si = BungeeCord.getInstance().getServerInfo(target);
        if (si == null)
        {
            BungeePerms.getLogger().info("No server found for " + target);
            return;
        }
        
        si.sendData(channel, msg.getBytes());
    }
}
