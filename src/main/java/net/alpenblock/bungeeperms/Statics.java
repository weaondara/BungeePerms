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
package net.alpenblock.bungeeperms;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.SneakyThrows;
import net.alpenblock.bungeeperms.platform.PlatformPlugin;
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
        for (String aliase : aliases)
        {
            if (aliase.equalsIgnoreCase(arg))
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
        setField(instance.getClass(), instance, var, varname);
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
            sender.sendMessage(Lang.translate(Lang.MessageType.COMMAND_TOO_MANY_ARGUMENTS));
            return false;
        }
        else if (args.length < length)
        {
            sender.sendMessage(Lang.translate(Lang.MessageType.COMMAND_TOO_FEW_ARGUMENTS));
            return false;
        }
        return true;
    }

    public static boolean matchArgs(Sender sender, String[] args, int min, int max)
    {
        if (args.length > max)
        {
           sender.sendMessage(Lang.translate(Lang.MessageType.COMMAND_TOO_MANY_ARGUMENTS));
            return false;
        }
        else if (args.length < min)
        {
            sender.sendMessage(Lang.translate(Lang.MessageType.COMMAND_TOO_FEW_ARGUMENTS));
            return false;
        }
        return true;
    }

    public static String format(String format, Object... args)
    {
        return MessageFormat.format(format, args);
    }

    public static String localeString(Locale locale)
    {
        return locale.getLanguage() + (locale.getCountry().isEmpty() ? "" : "-" + locale.getCountry());
    }

    public static String toLower(String s)
    {
        return s == null ? null : s.toLowerCase();
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

    public static boolean listContains(List<String> list, String element)
    {
        for (String l : list)
        {
            if (l.equalsIgnoreCase(element))
            {
                return true;
            }
        }
        return false;
    }

    public static boolean isEmpty(String s)
    {
        return s == null || s.isEmpty();
    }

    public static String formatDisplay(String append)
    {
        return isEmpty(append) ? "" : append + " ";
    }

    public static boolean isInt(String s)
    {
        try
        {
            Integer.parseInt(s);
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public static String arrayToString(String[] arr, int start, int count, String seperator)
    {
        String s = "";
        for (int i = 0; i < count; i++)
        {
            s += arr[start + i] + seperator;
        }
        if (s.length() >= seperator.length())
        {
            s = s.substring(0, s.length() - seperator.length());
        }
        return s;
    }

    public static String[] parseCommand(String cmd)
    {
        List<String> ret = new ArrayList();

        Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(cmd);
        while (m.find())
            ret.add(m.group(1).replaceAll("(^\")|(\"$)", ""));

//        int i = 0;
//        int j = 0;
//        boolean b = false;
//        while ((j = cmd.indexOf("\"", j)) > -1)
//        {
//            if (b)
//            {
//                if (j < cmd.length() - 1 && cmd.charAt(j + 1) == ' ')
//                {
//                    ret.add(cmd.substring(i, j));
//                    i = j;
//                    b = !b;
//                    continue;
//                }
//                else if (j == cmd.length() - 1)
//                {
//                    ret.add(cmd.substring(i, j));
//                    break;
//                }
//                else
//                {
//                    continue;
//                }
//            }
//            else if (!b)
//            {
//                if (i > 0 && cmd.charAt(i - 1) == ' ')
//                {
//                    b = !b;
//                    i = j;
//                    break;
//                }
//                else if (i == 0)
//                {
//                    b = !b;
//                    i = j;
//                    break;
//                }
//            }
//        }
//
//        if (b)
//        {
//            System.out.println("i=" + i);
//            System.out.println("j=" + j);
//        }
//        String s = "";
//        boolean b = false;
//        for (int i = 0; i < cmd.length(); i++)
//        {
//            switch (cmd.charAt(i))
//            {
//                case '\"':
//                    if (b)
//                    {
//                        if (i < cmd.length() - 1 && cmd.charAt(i + 1) != ' ')
//                        {
//                            s += cmd.charAt(i);
//                            break;
//                        }
//                        else
//                        {
//                            ret.add(s);
//                        }
//                    }
//                    else
//                    {
//                        if (i > 0 && cmd.charAt(i - 1) != ' ')
//                        {
//                            s += cmd.charAt(i);
//                            break;
//                        }
//                    }
//                    b = !b;
//                    break;
//                default:
//                    s += cmd.charAt(i);
//                    break;
//            }
//        }
        return ret.toArray(new String[ret.size()]);
    }

    public static String[] array(String... elements)
    {
        return elements;
    }

    public static <T> List<T> list(T... elements)
    {
        return new ArrayList(Arrays.asList(elements));
    }

    public static Integer getBuild(PlatformPlugin p)
    {
        String v = p.getVersion();
        if (v.contains("dev") && v.contains("#"))
        {
            int i = Integer.parseInt(v.split("#")[1]);
            return i;
        }
        else
        {
            return null;
        }
    }

    public static BPPermission makeBPPerm(String perm, String server, String world, PermEntity holder)
    {
        return new BPPermission(perm, holder.getName(), holder instanceof Group, server, world, null, null);
    }

    public static BPPermission makeBPPermTimed(TimedValue<String> perm, String server, String world, PermEntity holder)
    {
        return new BPPermission(perm.getValue(), holder.getName(), holder instanceof Group, server, world, perm.getStart(), perm.getDuration());
    }

    public static List<BPPermission> makeBPPerms(List<String> perms, String server, String world, PermEntity holder)
    {
        List<BPPermission> ret = new ArrayList();
        for (String p : perms)
            ret.add(makeBPPerm(p, server, world, holder));
        return ret;
    }

    public static List<BPPermission> makeBPPermsTimed(List<TimedValue<String>> perms, String server, String world, PermEntity holder)
    {
        List<BPPermission> ret = new ArrayList();
        for (TimedValue p : perms)
            ret.add(makeBPPermTimed(p, server, world, holder));
        return ret;
    }
}
