package net.alpenblock.bungeeperms.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class VersionUtils
{
    private static Method method;
    private static boolean useReflection;

    static {
        try {
            method = Bukkit.class.getDeclaredMethod("getOnlinePlayers");
            useReflection = method.getReturnType() == Player[].class;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public static Collection<? extends Player> getOnlinePlayers() {
        try {
            if (!useReflection) {
                return Bukkit.getOnlinePlayers();
            } else {
                Player[] playersArray = (Player[]) method.invoke(null);
                return Arrays.asList(playersArray);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

}
