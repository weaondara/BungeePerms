package net.alpenblock.bungeeperms.platform.bukkit;

import java.util.List;
import net.alpenblock.bungeeperms.BungeePerms;
import net.alpenblock.bungeeperms.platform.PluginMessageSender;
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
        List<Player> players = BukkitPlugin.getBukkitPlayers();
        Player p = players.iterator().hasNext() ? players.iterator().next() : null;
        if (p == null)
        {
            BungeePerms.getLogger().info("No server found for " + target);
            return;
        }

        p.sendPluginMessage(BukkitPlugin.getInstance(), BungeePerms.CHANNEL, msg.getBytes());
    }
}
