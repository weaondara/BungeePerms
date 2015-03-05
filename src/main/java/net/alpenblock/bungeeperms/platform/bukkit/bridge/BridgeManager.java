package net.alpenblock.bungeeperms.platform.bukkit.bridge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.alpenblock.bungeeperms.platform.Sender;
import net.alpenblock.bungeeperms.platform.bukkit.BukkitPlugin;
import net.alpenblock.bungeeperms.platform.bukkit.bridge.bridges.essentials.EssentialsBridge;
import net.alpenblock.bungeeperms.platform.bukkit.bridge.bridges.vault.VaultBridge;
import net.alpenblock.bungeeperms.platform.bukkit.bridge.bridges.worldedit.WorldEditBridge;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;

public class BridgeManager implements Listener
{

    private static BridgeManager instance;

    public static BridgeManager getInstance()
    {
        return instance;
    }

    private Map<Class<? extends Bridge>, String> brigdesmap;
    private List<Bridge> bridges;

    public void load()
    {
        instance = this;

        brigdesmap = new HashMap<>();
        bridges = new ArrayList<>();

        brigdesmap.put(WorldEditBridge.class, "com.sk89q.worldedit.bukkit.WorldEditPlugin");
        brigdesmap.put(VaultBridge.class, "net.milkbowl.vault.Vault");
        brigdesmap.put(EssentialsBridge.class, "com.earth2me.essentials.Essentials");

        for (Map.Entry<Class<? extends Bridge>, String> entry : brigdesmap.entrySet())
        {
            createBridge(entry.getKey(), entry.getValue());
        }
    }

    public void enable()
    {
        for (Bridge b : bridges)
        {
            b.enable();
        }
        Bukkit.getPluginManager().registerEvents(this, BukkitPlugin.getInstance());
    }

    public void disable()
    {
        PluginEnableEvent.getHandlerList().unregister((Listener) this);
        for (Bridge b : bridges)
        {
            b.disable();
        }
    }

    public boolean onCommand(Sender sender, String cmd, String label, String[] args)
    {
        if (cmd.equalsIgnoreCase("bungeepermsbukkitbridge"))
        {
            if (!(sender.isConsole()))
            {
                sender.sendMessage(ChatColor.DARK_RED + "Only console can do that!");
                return true;
            }
            if (args.length == 1 && args[0].equalsIgnoreCase("reload"))
            {
                disable();
                enable();
            }
            return true;
        }
        return false;
    }

    public Bridge createBridge(Class<? extends Bridge> c, String classname)
    {
        try
        {
            Class.forName(classname);
            Bridge b = c.newInstance();
            bridges.add(b);
            return b;
        }
        catch (Exception ex)
        {
        }
        return null;
    }

    @EventHandler
    public void onPluginEnable(PluginEnableEvent e)
    {
        for (Map.Entry<Class<? extends Bridge>, String> entry : brigdesmap.entrySet())
        {
            try
            {
                Class.forName(entry.getValue());
                for (Bridge b : bridges)
                {
                    if (b.getClass().getName().equals(entry.getKey().getName()))
                    {
                        throw new Exception();
                    }
                }
                createBridge(entry.getKey(), entry.getValue()).enable();
            }
            catch (Exception ex)
            {
            }
        }
    }

    @EventHandler
    public void onPluginEnable2(PluginEnableEvent e)
    {
        if (e.getPlugin().getName().equalsIgnoreCase("BungeePermsBukkitBridge"))
        {
            Bukkit.getConsoleSender().sendMessage(Color.RED + "WARNING: Please remove BungeePermsBukkitBridge!!! It's now integrated in BungeePermsBukkit.");
        }
    }
}
