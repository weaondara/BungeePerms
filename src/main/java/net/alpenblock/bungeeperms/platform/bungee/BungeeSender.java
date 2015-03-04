package net.alpenblock.bungeeperms.platform.bungee;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.alpenblock.bungeeperms.platform.Sender;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.command.ConsoleCommandSender;

@Getter
@AllArgsConstructor
public class BungeeSender implements Sender
{

    private CommandSender sender;

    @Override
    public void sendMessage(String message)
    {
        sender.sendMessage(message);
    }

    @Override
    public String getName()
    {
        return sender.getName();
    }

    @Override
    public UUID getUUID()
    {
        if (sender instanceof ConsoleCommandSender)
        {
            return UUID.fromString("00000000-0000-0000-0000-000000000000");
        }
        else if (sender instanceof ProxiedPlayer)
        {
            return ((ProxiedPlayer) sender).getUniqueId();
        }
        else
        {
            throw new UnsupportedOperationException("CommandSender derivative " + sender.getClass().getName() + " is unknown!");
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
        //todo: impl
        return null;
    }

    @Override
    public boolean isConsole()
    {
        return sender instanceof ConsoleCommandSender;
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
