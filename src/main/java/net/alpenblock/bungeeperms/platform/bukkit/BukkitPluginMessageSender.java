package net.alpenblock.bungeeperms.platform.bukkit;

import net.alpenblock.bungeeperms.BungeePerms;
import net.alpenblock.bungeeperms.platform.PluginMessageSender;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class BukkitPluginMessageSender implements PluginMessageSender
{

    @Override
    public void sendPluginMessage(String target, String channel, String msg)
    {
        if (!target.equalsIgnoreCase("bungee"))
        {
            return;
        }
        Player p = Bukkit.getOnlinePlayers().iterator().hasNext() ? Bukkit.getOnlinePlayers().iterator().next() : null;
        if (p == null)
        {
            BungeePerms.getLogger().info("No server found for " + target);
            return;
        }

        p.sendPluginMessage(BukkitPlugin.getInstance(), BungeePerms.CHANNEL, msg.getBytes());
    }
}
