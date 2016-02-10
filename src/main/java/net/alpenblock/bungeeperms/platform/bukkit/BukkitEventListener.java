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
import net.alpenblock.bungeeperms.io.BackEndType;
import net.alpenblock.bungeeperms.io.UUIDPlayerDBType;
import net.alpenblock.bungeeperms.platform.EventListener;
import net.alpenblock.bungeeperms.platform.Sender;
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
import org.bukkit.permissions.Permissible;
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
        BPPermissible permissible = new BPPermissible(Bukkit.getConsoleSender(), null, Injector.getPermissible(Bukkit.getConsoleSender()));
        permissible.inject();

        //uninject from players
        for (Player p : Bukkit.getOnlinePlayers())
        {
            if (!(Injector.getPermissible(p) instanceof BPPermissible))
            {
                User u = config.isUseUUIDs() ? pm().getUser(p.getUniqueId()) : pm().getUser(p.getName());
                BPPermissible perm = new BPPermissible(p, u, Injector.getPermissible(p));
                perm.inject();
            }
            p.recalculatePermissions();
        }
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

        //uninject from players
        for (Player p : Bukkit.getOnlinePlayers())
        {
            Injector.uninject(p);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLogin(PlayerLoginEvent e)
    {
        String playername = e.getPlayer().getName();
        UUID uuid = null;

        if (config.isUseUUIDs())
        {
            uuid = e.getPlayer().getUniqueId();
            BungeePerms.getLogger().info(Lang.translate(Lang.MessageType.LOGIN_UUID, playername, uuid));

            //update uuid player db
            pm().getUUIDPlayerDB().update(uuid, playername);
        }
        else
        {
            BungeePerms.getLogger().info(Lang.translate(Lang.MessageType.LOGIN, e.getPlayer().getName()));
        }

        //remove user from cache if present
        User oldu = config.isUseUUIDs() ? pm().getUser(uuid, false) : pm().getUser(playername, false);
        if (oldu != null)
        {
            pm().removeUserFromCache(oldu);
        }

        //load user from db
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
        BPPermissible permissible = new BPPermissible(e.getPlayer(), u, Injector.getPermissible(e.getPlayer()));
        permissible.inject();

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

        User u = config.isUseUUIDs() ? pm().getUser(e.getPlayer().getUniqueId()) : pm().getUser(e.getPlayer().getName());
        pm().removeUserFromCache(u);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChangedWorld(PlayerChangedWorldEvent e)
    {
        BukkitPlugin.getInstance().getNotifier().sendWorldUpdate(e.getPlayer());

        User u = config.isUseUUIDs() ? pm().getUser(e.getPlayer().getUniqueId()) : pm().getUser(e.getPlayer().getName());
        updateAttachment(e.getPlayer(), u);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onUserUpdate(BungeePermsUserChangedEvent e)
    {
        Player p = config.isUseUUIDs() ? Bukkit.getPlayer(e.getUser().getUUID()) : Bukkit.getPlayer(e.getUser().getName());
        if (p == null)
        {
            return;
        }
        updateAttachment(p, e.getUser());
    }

    @EventHandler(priority = EventPriority.LOWEST)
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
        if (config.isStandalone())
        {
            BungeePerms.getLogger().warning(Lang.translate(Lang.MessageType.MISCONFIGURATION) + ": " + Lang.translate(Lang.MessageType.MISCONFIG_BUKKIT_STANDALONE));
            BungeePerms.getInstance().getDebug().log(Lang.translate(Lang.MessageType.MISCONFIGURATION) + ": " + Lang.translate(Lang.MessageType.MISCONFIG_BUKKIT_STANDALONE));
            BungeePerms.getInstance().getDebug().log("sender = BungeeCord");
            BungeePerms.getInstance().getDebug().log("msg = " + new String(bytes));
            return;
        }

        String msg = new String(bytes);
        if (config.isDebug())
        {
            BungeePerms.getLogger().info("msg=" + msg);
        }
        List<String> data = Statics.toList(msg, ";");

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
        else if (cmd.equalsIgnoreCase("configcheck"))
        {
            String servername = data.get(1);
            BackEndType backend = BackEndType.getByName(data.get(2));
            UUIDPlayerDBType uuidplayerdb = UUIDPlayerDBType.getByName(data.get(3));
            boolean useuuid = Boolean.parseBoolean(data.get(4));
            if (!config.getServername().equals(servername))
            {
                BungeePerms.getLogger().warning(Lang.translate(Lang.MessageType.MISCONFIGURATION) + ": " + Lang.translate(Lang.MessageType.MISCONFIG_BUKKIT_SERVERNAME));
            }
            if (config.getBackEndType() != backend)
            {
                BungeePerms.getLogger().warning(Lang.translate(Lang.MessageType.MISCONFIGURATION) + ": " + Lang.translate(Lang.MessageType.MISCONFIG_BUKKIT_BACKEND));
            }
            if (config.getUUIDPlayerDBType() != uuidplayerdb)
            {
                BungeePerms.getLogger().warning(Lang.translate(Lang.MessageType.MISCONFIGURATION) + ": " + Lang.translate(Lang.MessageType.MISCONFIG_BUKKIT_UUIDPLAYERDB));
            }
            if (config.isUseUUIDs() != useuuid)
            {
                BungeePerms.getLogger().warning(Lang.translate(Lang.MessageType.MISCONFIGURATION) + ": " + Lang.translate(Lang.MessageType.MISCONFIG_BUKKIT_USEUUID));
            }
        }
        else if (cmd.equalsIgnoreCase("uuidcheck"))
        {
            if (!config.isUseUUIDs())
            {
                return;
            }
            String uuid = data.get(2);
            Sender p = BukkitPlugin.getInstance().getPlayer(userorgroup);
            if (p != null && !p.getUUID().equals(UUID.fromString(uuid)))
            {
                BungeePerms.getLogger().warning(Lang.translate(Lang.MessageType.MISCONFIGURATION) + ": " + Lang.translate(Lang.MessageType.MISCONFIG_BUNGEECORD_BUKKIT_CONFIG));
            }
        }
    }

    private PermissionsManager pm()
    {
        return BungeePerms.getInstance().getPermissionsManager();
    }

    private void updateAttachment(Player p, User u)
    {
        Permissible base = Injector.getPermissible(p);
        if (!(base instanceof BPPermissible))
        {
            return;
        }

        BPPermissible perm = (BPPermissible) base;
        perm.updateAttachment(u, ((BukkitConfig) BungeePerms.getInstance().getConfig()).getServername(), p.getWorld() == null ? null : p.getWorld().getName());
    }
}
