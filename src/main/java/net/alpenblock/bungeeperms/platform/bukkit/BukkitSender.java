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

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.alpenblock.bungeeperms.BungeePerms;
import net.alpenblock.bungeeperms.platform.MessageEncoder;
import net.alpenblock.bungeeperms.platform.Sender;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

@Getter
@AllArgsConstructor
public class BukkitSender implements Sender
{

    private final CommandSender sender;

    @Override
    public void sendMessage(String message)
    {
        if (message.replaceAll("&.", "").replaceAll("§.", "").trim().isEmpty())
            return;
        sender.sendMessage(message);
    }

    @Override
    public void sendMessage(MessageEncoder encoder)
    {
        BukkitMessageEncoder e = (BukkitMessageEncoder) encoder;
        if (isPlayer() && BungeePerms.getInstance().getPlugin().isChatApiPresent())
        {
            BaseComponent[] converted = BukkitMessageEncoder.convert(e.create());
            ((Player) sender).spigot().sendMessage(converted);
        }
        else
        {
            sender.sendMessage(e.toString());
        }
    }

    @Override
    public String getName()
    {
        return sender.getName();
    }

    @Override
    public UUID getUUID()
    {
        if (!BungeePerms.getInstance().getConfig().isUseUUIDs())
        {
            throw new IllegalStateException("useuuid not enabled but uuid functionality called");
        }
        if (sender instanceof ConsoleCommandSender)
        {
            return UUID.fromString("00000000-0000-0000-0000-000000000000");
        }
        else if (sender instanceof Player)
        {
            return ((Player) sender).getUniqueId();
        }
        else
        {
            return null;
//            throw new UnsupportedOperationException("CommandSender derivative " + sender.getClass().getName() + " is unknown!");
        }
    }

    @Override
    public String getServer()
    {
        if (sender instanceof Player)
        {
            return ((BukkitConfig) BungeePerms.getInstance().getConfig()).getServername();
        }
        else
        {
            return null;
        }
    }

    @Override
    public String getWorld()
    {
        if (sender instanceof Player)
        {
            World w = ((Player) sender).getWorld();
            return w != null ? w.getName() : null;
        }
        else
        {
            return null;
        }
    }

    @Override
    public boolean isConsole()
    {
        return sender instanceof ConsoleCommandSender;
    }

    @Override
    public boolean isPlayer()
    {
        return sender instanceof Player;
    }

    @Override
    public boolean isOperator()
    {
        return isPlayer() && ((Player) sender).isOp();
    }

}
