package net.alpenblock.bungeeperms.platform.bukkit;

import net.alpenblock.bungeeperms.BungeePerms;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import net.alpenblock.bungeeperms.Group;
import net.alpenblock.bungeeperms.Lang;
import net.alpenblock.bungeeperms.PermissionsManager;
import net.alpenblock.bungeeperms.Statics;
import net.alpenblock.bungeeperms.User;
import net.alpenblock.bungeeperms.platform.EventListener;
import net.alpenblock.bungeeperms.platform.bukkit.event.BungeePermsUserChangedEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRegisterChannelEvent;
import org.bukkit.permissions.PermissibleBase;
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
        Permissible permissible = new Permissible(Bukkit.getConsoleSender(), null);
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
        String playername = e.getPlayer().getName();
        UUID uuid = null;

        if (config.isUseUUIDs())
        {
            BungeePerms.getLogger().info(Lang.translate(Lang.MessageType.LOGIN_UUID, e.getPlayer().getName(), e.getPlayer().getUniqueId()));
            uuid = e.getPlayer().getUniqueId();

            //update uuid player db
            pm().getUUIDPlayerDB().update(uuid, playername);
        }
        else
        {
            BungeePerms.getLogger().info(Lang.translate(Lang.MessageType.LOGIN, e.getPlayer().getName()));
        }

        User u = config.isUseUUIDs() ? pm().getUser(uuid) : pm().getUser(playername);
        if (u == null)
        {
            //create user and add default groups
            if (config.isUseUUIDs())
            {
                BungeePerms.getLogger().info(Lang.translate(Lang.MessageType.ADDING_DEFAULT_GROUPS_UUID, playername, uuid));
            }
            else
            {
                BungeePerms.getLogger().info(Lang.translate(Lang.MessageType.ADDING_DEFAULT_GROUPS, playername));
            }

            u = pm().createTempUser(playername, uuid);
            pm().getBackEnd().saveUser(u, true);
        }

        BukkitPlugin.getInstance().getNotifier().sendWorldUpdate(e.getPlayer());

        //inject permissible
        Permissible permissible = new Permissible(e.getPlayer(), u);
        org.bukkit.permissions.Permissible oldpermissible = Injector.inject(e.getPlayer(), permissible);
        permissible.setOldPermissible(oldpermissible);

        updateAttachment(e.getPlayer(), u);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent e)
    {
        BukkitPlugin.getInstance().getNotifier().sendWorldUpdate(e.getPlayer());

        User u = config.isUseUUIDs() ? pm().getUser(e.getPlayer().getUniqueId()) : pm().getUser(e.getPlayer().getName());
        updateAttachment(e.getPlayer(), u);
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

        User u = config.isUseUUIDs() ? pm().getUser(e.getPlayer().getUniqueId()) : pm().getUser(e.getPlayer().getName());
        updateAttachment(e.getPlayer(), u);
    }

    @EventHandler
    public void onUserUpdate(BungeePermsUserChangedEvent e)
    {
        Player p = config.isUseUUIDs() ? Bukkit.getPlayer(e.getUser().getUUID()) : Bukkit.getPlayer(e.getUser().getName());
        if (p == null)
        {
            return;
        }
        updateAttachment(p, e.getUser());
    }

    @EventHandler
    public void onPluginChannelRegister(PlayerRegisterChannelEvent e)
    {
        if (!e.getChannel().equals(BungeePerms.CHANNEL))
        {
            return;
        }
        
        BukkitPlugin.getInstance().getNotifier().sendWorldUpdate(e.getPlayer());
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] bytes)
    {
        String msg = new String(bytes);
        BungeePerms.getLogger().info("msg=" + msg);
        List<String> data = Statics.toList(msg, ";");

        BungeePerms.getInstance().getDebug().log("msg=" + msg);

        String cmd = data.get(0);
        String userorgroup = data.size() > 1 ? data.get(1) : null;

        if (cmd.equalsIgnoreCase("deleteuser"))
        {
            User u = pm().getUser(userorgroup);
            pm().removeUserFromCache(u);
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
            Runnable r = new Runnable()
            {
                @Override
                public void run()
                {
                    BungeePerms.getInstance().reload(false);
                }
            };
            Bukkit.getScheduler().runTaskLater(BukkitPlugin.getInstance(), r, 1);
        }
    }

    private PermissionsManager pm()
    {
        return BungeePerms.getInstance().getPermissionsManager();
    }

    private void updateAttachment(Player p, User u)
    {
        PermissibleBase base = Injector.getPermissible(p);
        if (!(base instanceof Permissible))
        {
            return;
        }

        Permissible perm = (Permissible) base;
        perm.updateAttachment(u, ((BukkitConfig) BungeePerms.getInstance().getConfig()).getServername(), p.getWorld() == null ? null : p.getWorld().getName());

        p.recalculatePermissions();
    }
}
