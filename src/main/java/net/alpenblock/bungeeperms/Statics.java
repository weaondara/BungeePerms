package net.alpenblock.bungeeperms;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.SneakyThrows;
import net.alpenblock.bungeeperms.platform.Sender;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

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
        Sender p = BungeePerms.getInstance().getPlugin().getPlayer(player);
        if (p != null)
        {
            for (Sender pp : BungeePerms.getInstance().getPlugin().getPlayers())
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

    public static <T> T replaceField(Object instance, T var, String varname)
    {
        try
        {
            Field f = instance.getClass().getDeclaredField(varname);
            f.setAccessible(true);
            T old = (T) f.get(instance);
            f.set(instance, var);
            return old;
        }
        catch (Exception ex)
        {
            return null;
        }
    }

    public static <T> T getField(Object instance, Class<T> type, String varname)
    {
        try
        {
            Field f = instance.getClass().getDeclaredField(varname);
            f.setAccessible(true);
            T old = (T) f.get(instance);
            return old;
        }
        catch (Exception ex)
        {
            return null;
        }
    }

    public static <T> T getField(Class clazz, Object instance, Class<T> type, String varname)
    {
        try
        {
            Field f = clazz.getDeclaredField(varname);
            f.setAccessible(true);
            T old = (T) f.get(instance);
            return old;
        }
        catch (Exception ex)
        {
            return null;
        }
    }

    public static void setField(Object instance, Object var, String varname)
    {
        try
        {
            Field f = instance.getClass().getDeclaredField(varname);
            f.setAccessible(true);
            f.set(instance, var);
        }
        catch (Exception ex)
        {
        }
    }

    public static void setField(Class clazz, Object instance, Object var, String varname)
    {
        try
        {
            Field f = clazz.getDeclaredField(varname);
            f.setAccessible(true);
            f.set(instance, var);
        }
        catch (Exception ex)
        {
        }
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

    public static boolean matchArgs(Sender sender, String[] args, int length)
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

    public static boolean matchArgs(Sender sender, String[] args, int min, int max)
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

    @SneakyThrows
    public static void unregisterListener(Listener l)
    {
        for (Method m : l.getClass().getDeclaredMethods())
        {
            if (m.getAnnotation(EventHandler.class) != null && m.getParameterTypes().length > 0)
            {
                Class eventclass = m.getParameterTypes()[0];
                if (Event.class.isAssignableFrom(eventclass))
                {
                    HandlerList hl = (HandlerList) eventclass.getMethod("getHandlerList").invoke(null);
                    hl.unregister(l);
                }
            }
        }
    }
}
