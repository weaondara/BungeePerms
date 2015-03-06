package net.alpenblock.bungeeperms.platform.bungee;

import net.alpenblock.bungeeperms.BungeePerms;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import lombok.Getter;
import net.alpenblock.bungeeperms.Color;
import net.alpenblock.bungeeperms.Group;
import net.alpenblock.bungeeperms.PermissionsManager;
import net.alpenblock.bungeeperms.Statics;
import net.alpenblock.bungeeperms.User;
import net.alpenblock.bungeeperms.platform.EventListener;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.ServerConnection;
import net.md_5.bungee.api.CommandSender;
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
        BungeeCord.getInstance().getPluginManager().registerListener(BungeePlugin.getInstance(), this);
    }

    @Override
    public void disable()
    {
        if (!enabled)
        {
            return;
        }
        enabled = false;
        BungeeCord.getInstance().getPluginManager().unregisterListener(this);
    }

    @EventHandler(priority = Byte.MIN_VALUE)
    public void onLogin(LoginEvent e)
    {
        String playername = e.getConnection().getName();
        UUID uuid = null;
        if (config.isUseUUIDs())
        {
            uuid = e.getConnection().getUniqueId();
            BungeePerms.getLogger().log(Level.INFO, "Login by {0} ({1})", new Object[]
                                {
                                    playername, uuid
            });

            //update uuid player db
            pm().getUUIDPlayerDB().update(uuid, playername);
        }
        else
        {
            BungeePerms.getLogger().log(Level.INFO, "Login by {0}", new Object[]
                                {
                                    playername
            });
        }

        User u = config.isUseUUIDs() ? pm().getUser(uuid) : pm().getUser(playername);
        if (u == null)
        {
            BungeePerms.getLogger().log(Level.INFO, "Adding default groups to {0} ({1})", new Object[]
                                {
                                    playername, uuid
            });

            List<Group> groups = pm().getDefaultGroups();
            u = new User(playername, uuid, groups, new ArrayList<String>(), new HashMap<String, List<String>>(), new HashMap<String, Map<String, List<String>>>());
            pm().addUserToCache(u);

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
        String server = null;
        String world = null;
        if (s instanceof ProxiedPlayer)
        {
            ProxiedPlayer pp = (ProxiedPlayer) s;
            server = pp.getServer() != null ? pp.getServer().getInfo().getName() : null;
            world = server != null ? playerWorlds.get(e.getSender().getName()) : null;
        }
        e.setHasPermission(BungeePerms.getInstance().getPermissionsChecker().hasPermOrConsoleOnServerInWorld(s.getName(), e.getPermission(), server, world));
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
            for (ProxiedPlayer pp : BungeeCord.getInstance().getPlayers())
            {
                if (pp.getName().toLowerCase().startsWith(e.getCursor().toLowerCase()))
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
            BungeePerms.getLogger().severe(Color.Error + "Possible intrusion detected. Sender is " + e.getSender());
            e.setCancelled(true);
            return;
        }

        ServerConnection scon = (ServerConnection) e.getSender();

        //check network type // ignore if standalone or not registered server
        if (config.getNetworkType() == NetworkType.Standalone
                || (config.getNetworkType() == NetworkType.ServerDependend && !config.getNetworkServers().contains(scon.getInfo().getName())))
        {
            return;
        }

        //process message
        String msg = new String(e.getData());
        List<String> data = Statics.toList(msg, ";");

        String cmd = data.get(0);
        String userorgroup = data.size() > 1 ? data.get(1) : null;

        if (cmd.equalsIgnoreCase("updateplayerworld"))
        {
            String player = data.get(1);
            String world = data.get(2);

            playerWorlds.put(player, world);
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
            BungeePerms.getInstance().reload();

            //this would be bad
//            //forward plugin message to network
//            BungeePerms.getInstance().getNetworkNotifier().reloadAll(scon.getInfo().getName());
        }

        e.setCancelled(true);
    }

    private PermissionsManager pm()
    {
        return BungeePerms.getInstance().getPermissionsManager();
    }
}
