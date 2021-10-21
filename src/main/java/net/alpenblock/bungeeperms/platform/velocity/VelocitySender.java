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
package net.alpenblock.bungeeperms.platform.velocity;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.alpenblock.bungeeperms.BungeePerms;
import net.alpenblock.bungeeperms.platform.MessageEncoder;
import net.alpenblock.bungeeperms.platform.Sender;
import net.kyori.adventure.text.Component;

@Getter
@AllArgsConstructor
public class VelocitySender implements Sender
{

    private final CommandSource sender;

    @Override
    public void sendMessage(String message)
    {
        if (message.replaceAll("&.", "").replaceAll("§.", "").trim().isEmpty())
            return;
        Component t = Component.text(message);
        sender.sendMessage(t);
    }

    @Override
    public void sendMessage(MessageEncoder encoder)
    {
        VelocityMessageEncoder e = (VelocityMessageEncoder) encoder;
        Component converted = VelocityMessageEncoder.convert(e.create());
        sender.sendMessage(converted);
    }

    @Override
    public String getName()
    {
        if (sender instanceof Player)
            return ((Player) sender).getUsername();
        else if (sender instanceof ConsoleCommandSource)
            return "CONSOLE";
        throw new UnsupportedOperationException();
    }

    @Override
    public UUID getUUID()
    {
        if (isConsole())
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
        }
    }

    @Override
    public String getServer()
    {
        if (sender instanceof Player)
        {
            Player pp = (Player) sender;
            return pp.getCurrentServer().isPresent() ? pp.getCurrentServer().get().getServerInfo().getName() : null;
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
            VelocityEventListener l = (VelocityEventListener) BungeePerms.getInstance().getEventListener();
            return l.getPlayerWorlds().get(((Player) sender).getUsername());
        }
        else
        {
            return null;
        }
    }

    @Override
    public boolean isConsole()
    {
        return sender instanceof ConsoleCommandSource;
    }

    @Override
    public boolean isPlayer()
    {
        return sender instanceof Player;
    }

    @Override
    public boolean isOperator()
    {
        return false;
    }
}
