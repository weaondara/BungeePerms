package net.alpenblock.bungeeperms;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class Statics
{

    public static int countSequences(String s, String seq)
    {
        int count = 0;
        for (int i = 0; i < s.length() - seq.length() + 1; i++)
        {
            if (s.substring(i, i + seq.length()).equalsIgnoreCase(seq))
            {
                count++;
            }
        }
        return count;
    }

    public static String getFullPlayerName(String player)
    {
        ProxiedPlayer p = BungeeCord.getInstance().getPlayer(player);
        if (p != null)
        {
            for (ProxiedPlayer pp : BungeeCord.getInstance().getPlayers())
            {
                if (pp.getName().startsWith(player))
                {
                    return pp.getName();
                }
            }
            return p.getName();
        }
        else
        {
            return player;
        }
    }

    public static List<String> toList(String s, String seperator)
    {
        List<String> l = new ArrayList<>();
        String ls = "";
        for (int i = 0; i < (s.length() - seperator.length()) + 1; i++)
        {
            if (s.substring(i, i + seperator.length()).equalsIgnoreCase(seperator))
            {
                l.add(ls);
                ls = "";
                i = i + seperator.length() - 1;
            }
            else
            {
                ls += s.substring(i, i + 1);
            }
        }
        if (ls.length() > 0)
        {
            l.add(ls);
        }
        return l;
    }

    public static boolean argAlias(String arg, String... aliases)
    {
        for (int i = 0; i < aliases.length; i++)
        {
            if (aliases[i].equalsIgnoreCase(arg))
            {
                return true;
            }
        }
        return false;
    }

    public static UUID parseUUID(String s)
    {
        try
        {
            return UUID.fromString(s);
        }
        catch (Exception e)
        {
        }

        if (s.length() == 32)
        {
            s = s.substring(0, 8) + "-"
                    + s.substring(8, 12) + "-"
                    + s.substring(12, 16) + "-"
                    + s.substring(16, 20) + "-"
                    + s.substring(20, 32) + "-";
            try
            {
                return UUID.fromString(s);
            }
            catch (Exception e)
            {
            }
        }

        return null;
    }
    
    public static boolean matchArgs(CommandSender sender, String[] args, int length)
    {
        if (args.length > length)
        {
            Messages.sendTooManyArgsMessage(sender);
            return false;
        }
        else if (args.length < length)
        {
            Messages.sendTooLessArgsMessage(sender);
            return false;
        }
        return true;
    }
    
    public static boolean matchArgs(CommandSender sender, String[] args, int min, int max)
    {
        if (args.length > max)
        {
            Messages.sendTooManyArgsMessage(sender);
            return false;
        }
        else if (args.length < min)
        {
            Messages.sendTooLessArgsMessage(sender);
            return false;
        }
        return true;
    }
}
