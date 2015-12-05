package net.alpenblock.bungeeperms.platform.bungee;

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
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PermissionCheckEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.TabCompleteEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class BungeeEventListener implements Listener, EventListener
{

    @Getter
    private final Map<String, String> playerWorlds = new HashMap<>();

    private boolean enabled = false;

    private final BungeeConfig config;

    public BungeeEventListener(BungeeConfig config)
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
        ProxyServer.getInstance().getPluginManager().registerListener(BungeePlugin.getInstance(), this);
    }

    @Override
    public void disable()
    {
        if (!enabled)
        {
            return;
        }
        enabled = false;
        ProxyServer.getInstance().getPluginManager().unregisterListener(this);
    }

    @EventHandler(priority = Byte.MIN_VALUE + 1)
    public void onLogin(LoginEvent e)
    {
        //don't load if cancelled
        if(e.isCancelled())
        {
            return;
        }
        
        String playername = e.getConnection().getName();
        UUID uuid = null;
        if (config.isUseUUIDs())
        {
            uuid = e.getConnection().getUniqueId();
            BungeePerms.getLogger().info(Lang.translate(Lang.MessageType.LOGIN_UUID, playername, uuid));

            //update uuid player db
            pm().getUUIDPlayerDB().update(uuid, playername);
        }
        else
        {
            BungeePerms.getLogger().info(Lang.translate(Lang.MessageType.LOGIN, playername));
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
    }

    @EventHandler(priority = Byte.MAX_VALUE)
    public void onDisconnect(PlayerDisconnectEvent e)
    {
        String playername = e.getPlayer().getName();
        UUID uuid = e.getPlayer().getUniqueId();

        User u = config.isUseUUIDs() ? pm().getUser(uuid) : pm().getUser(playername);
        pm().removeUserFromCache(u);
    }

    @EventHandler
    public void onPermissionCheck(PermissionCheckEvent e)
    {
        CommandSender s = e.getSender();
        e.setHasPermission(BungeePerms.getInstance().getPermissionsChecker().hasPermOrConsoleOnServerInWorld(new BungeeSender(s), e.getPermission()));
    }

    @EventHandler
    public void onTabcomplete(TabCompleteEvent e)
    {
        if (!config.isTabComplete())
        {
            return;
        }
        if (e.getSuggestions().isEmpty())
        {
            for (ProxiedPlayer pp : ProxyServer.getInstance().getPlayers())
            {
                if (Statics.toLower(pp.getName()).startsWith(Statics.toLower(e.getCursor())))
                {
                    e.getSuggestions().add(pp.getName());
                }
            }
        }
    }

    @EventHandler
    public void onMessage(PluginMessageEvent e)
    {
        if (!e.getTag().equalsIgnoreCase(BungeePerms.CHANNEL))
        {
            return;
        }

        if (!(e.getReceiver() instanceof ProxiedPlayer))
        {
            //lock out silly hackers
            BungeePerms.getLogger().severe(Lang.translate(Lang.MessageType.INTRUSTION_DETECTED, e.getSender()));
            e.setCancelled(true);
            return;
        }

        net.md_5.bungee.api.connection.Server scon = (net.md_5.bungee.api.connection.Server) e.getSender();

        //check network type // ignore if standalone or not registered server
        if (config.getNetworkType() == NetworkType.Standalone
                || (config.getNetworkType() == NetworkType.ServerDependend && !config.getNetworkServers().contains(scon.getInfo().getName())))
        {
            //todo add misconfiguration message
            return;
        }

        //process message
        String msg = new String(e.getData());
        BungeePerms.getLogger().info("msg=" + msg);
        List<String> data = Statics.toList(msg, ";");

        String cmd = data.get(0);
        String userorgroup = data.size() > 1 ? data.get(1) : null;

        if (cmd.equalsIgnoreCase("playerworldupdate"))
        {
            String world = data.get(2);

            playerWorlds.put(userorgroup, world);
        }
        else if (cmd.equalsIgnoreCase("deleteuser"))
        {
            User u = pm().getUser(userorgroup);
            pm().removeUserFromCache(u);

            //forward plugin message to network
            BungeePerms.getInstance().getNetworkNotifier().deleteUser(u, scon.getInfo().getName());
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

            //forward plugin message to network
            BungeePerms.getInstance().getNetworkNotifier().deleteGroup(g, scon.getInfo().getName());
        }
        else if (cmd.equalsIgnoreCase("reloaduser"))
        {
            pm().reloadUser(userorgroup);

            //forward plugin message to network
            User u = pm().getUser(userorgroup);
            if (u == null)
            {
                return;
            }
            BungeePerms.getInstance().getNetworkNotifier().reloadUser(u, scon.getInfo().getName());
        }
        else if (cmd.equalsIgnoreCase("reloadgroup"))
        {
            pm().reloadGroup(userorgroup);

            //forward plugin message to network
            Group g = pm().getGroup(userorgroup);
            if (g == null)
            {
                return;
            }
            BungeePerms.getInstance().getNetworkNotifier().reloadGroup(g, scon.getInfo().getName());
        }
        else if (cmd.equalsIgnoreCase("reloadusers"))
        {
            pm().reloadUsers();

            //forward plugin message to network
            BungeePerms.getInstance().getNetworkNotifier().reloadUsers(scon.getInfo().getName());
        }
        else if (cmd.equalsIgnoreCase("reloadgroups"))
        {
            pm().reloadGroups();

            //forward plugin message to network
            BungeePerms.getInstance().getNetworkNotifier().reloadGroups(scon.getInfo().getName());
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
            ProxyServer.getInstance().getScheduler().runAsync(BungeePlugin.getInstance(), r);

            //forward plugin message to network except to server which issued the reload
            BungeePerms.getInstance().getNetworkNotifier().reloadAll(scon.getInfo().getName());
        }

        e.setCancelled(true);
    }

    private PermissionsManager pm()
    {
        return BungeePerms.getInstance().getPermissionsManager();
    }
}
