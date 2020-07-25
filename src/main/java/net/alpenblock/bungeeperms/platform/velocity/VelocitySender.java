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

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.alpenblock.bungeeperms.BungeePerms;
import net.alpenblock.bungeeperms.platform.MessageEncoder;
import net.alpenblock.bungeeperms.platform.Sender;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

@Getter
@AllArgsConstructor
public class VelocitySender implements Sender
{

    private CommandSender sender;

    @Override
    public void sendMessage(String message)
    {
        sender.sendMessage(message);
    }

    @Override
    public void sendMessage(MessageEncoder encoder)
    {
        VelocityMessageEncoder e = (VelocityMessageEncoder) encoder;
        if (BungeePerms.getInstance().getPlugin().isChatApiPresent())
        {

            BaseComponent[] converted = VelocityMessageEncoder.convert(e.create());
            sender.sendMessage(converted);
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
        if (isConsole())
        {
            return UUID.fromString("00000000-0000-0000-0000-000000000000");
        }
        else if (sender instanceof ProxiedPlayer)
        {
            return ((ProxiedPlayer) sender).getUniqueId();
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
        if (sender instanceof ProxiedPlayer)
        {
            ProxiedPlayer pp = (ProxiedPlayer) sender;
            return pp.getServer() != null ? pp.getServer().getInfo().getName() : null;
        }
        else
        {
            return null;
        }
    }

    @Override
    public String getWorld()
    {
        VelocityEventListener l = (VelocityEventListener) BungeePerms.getInstance().getEventListener();
        return l.getPlayerWorlds().get(sender.getName());
    }

    @Override
    public boolean isConsole()
    {
        return sender.getClass().getName().equals("net.md_5.bungee.command.ConsoleCommandSender");
    }

    @Override
    public boolean isPlayer()
    {
        return sender instanceof ProxiedPlayer;
    }

    @Override
    public boolean isOperator()
    {
        return false;
    }

}
