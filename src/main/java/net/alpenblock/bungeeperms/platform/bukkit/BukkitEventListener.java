package net.alpenblock.bungeeperms.platform.bukkit;

import net.alpenblock.bungeeperms.BungeePerms;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import lombok.Getter;
import net.alpenblock.bungeeperms.Group;
import net.alpenblock.bungeeperms.PermissionsManager;
import net.alpenblock.bungeeperms.Statics;
import net.alpenblock.bungeeperms.User;
import net.alpenblock.bungeeperms.platform.EventListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class BukkitEventListener implements Listener, EventListener, PluginMessageListener
{

    @Getter
    private final Map<String, String> playerWorlds = new HashMap<>();

    private boolean enabled = false;

    private final BukkitConfig config;

    public BukkitEventListener(BukkitConfig config)
    {
        this.config = config;
    }

    @Override
    public void enable()
    {
        if (enabled)
        {
            return;
        }
        enabled = true;
        Bukkit.getPluginManager().registerEvents(this, BukkitPlugin.getInstance());

        //inject into console // seems to be best place here
        Permissible permissible = new Permissible(Bukkit.getConsoleSender());
        org.bukkit.permissions.Permissible oldpermissible = Injector.inject(Bukkit.getConsoleSender(), permissible);
        permissible.setOldPermissible(oldpermissible);
    }

    @Override
    public void disable()
    {
        if (!enabled)
        {
            return;
        }
        enabled = false;
        Statics.unregisterListener(this);

        //uninject from console // seems to be best place here
        Injector.uninject(Bukkit.getConsoleSender());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLogin(PlayerLoginEvent e)
    {
        if (config.isUseUUIDs())
        {
            pm().reloadUser(e.getPlayer().getUniqueId());
            BungeePerms.getLogger().log(Level.INFO, "Login by {0} ({1})", new Object[]
                                {
                                    e.getPlayer().getName(), e.getPlayer().getUniqueId()
            });
        }
        else
        {
            pm().reloadUser(e.getPlayer().getName());
            BungeePerms.getLogger().log(Level.INFO, "Login by {0}", new Object[]
                                {
                                    e.getPlayer().getName()
            });
        }

        //inject permissible
        Permissible permissible = new Permissible(e.getPlayer());
        org.bukkit.permissions.Permissible oldpermissible = Injector.inject(e.getPlayer(), permissible);
        permissible.setOldPermissible(oldpermissible);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent e)
    {
        BukkitPlugin.getInstance().getNotifier().sendWorldUpdate(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent e)
    {
        //uninject permissible
        Injector.uninject(e.getPlayer());

        User u;
        if (config.isUseUUIDs())
        {
            u = pm().getUser(e.getPlayer().getUniqueId());
        }
        else
        {
            u = pm().getUser(e.getPlayer().getName());
        }
        pm().removeUserFromCache(u);
    }

    @EventHandler
    public void onChangedWorld(PlayerChangedWorldEvent e)
    {
        BukkitPlugin.getInstance().getNotifier().sendWorldUpdate(e.getPlayer());
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] bytes)
    {
        String msg = new String(bytes);
        List<String> data = Statics.toList(msg, ";");

        BungeePerms.getInstance().getDebug().log("msg=" + msg);

        String cmd = data.get(0);
        String userorgroup = data.size() > 1 ? data.get(1) : null;

        if (cmd.equalsIgnoreCase("deleteuser"))
        {
            User u = pm().getUser(userorgroup);
            pm().removeUserFromCache(u);

            //refreshBukkitPermissions(userorgroup);
        }
        else if (cmd.equalsIgnoreCase("deletegroup"))
        {
            Group g = pm().getGroup(userorgroup);
            pm().removeGroupFromCache(g);
            for (Group gr : pm().getGroups())
            {
                gr.recalcPerms();
            }
            for (User u : pm().getUsers())
            {
                u.recalcPerms();
            }
        }
        else if (cmd.equalsIgnoreCase("reloaduser"))
        {
            pm().reloadUser(userorgroup);
        }
        else if (cmd.equalsIgnoreCase("reloadgroup"))
        {
            pm().reloadGroup(userorgroup);
        }
        else if (cmd.equalsIgnoreCase("reloadusers"))
        {
            pm().reloadUsers();
        }
        else if (cmd.equalsIgnoreCase("reloadgroups"))
        {
            pm().reloadGroups();
        }
        else if (cmd.equalsIgnoreCase("reloadall"))
        {
            BungeePerms.getInstance().reload();
        }
    }

    private PermissionsManager pm()
    {
        return BungeePerms.getInstance().getPermissionsManager();
    }
}
