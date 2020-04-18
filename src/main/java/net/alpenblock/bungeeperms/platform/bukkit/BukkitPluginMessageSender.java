/* 
 * Copyright (C) 2020 wea_ondara
 *
 * BungeePerms is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BungeePerms is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
