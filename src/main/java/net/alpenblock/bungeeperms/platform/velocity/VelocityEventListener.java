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
package net.alpenblock.bungeeperms.platform.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.permission.PermissionsSetupEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.permission.PermissionFunction;
import com.velocitypowered.api.permission.PermissionProvider;
import com.velocitypowered.api.permission.PermissionSubject;
import com.velocitypowered.api.permission.Tristate;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import net.alpenblock.bungeeperms.platform.proxy.NetworkType;
import net.alpenblock.bungeeperms.BungeePerms;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import net.alpenblock.bungeeperms.Group;
import net.alpenblock.bungeeperms.Lang;
import net.alpenblock.bungeeperms.PermissionsManager;
import net.alpenblock.bungeeperms.PermissionsResolver;
import net.alpenblock.bungeeperms.Statics;
import net.alpenblock.bungeeperms.User;
import net.alpenblock.bungeeperms.io.BackEndType;
import net.alpenblock.bungeeperms.platform.EventListener;
import net.alpenblock.bungeeperms.platform.proxy.ProxyConfig;

public class VelocityEventListener implements EventListener
{

    @Inject
    private ProxyServer proxyServer;

    @Getter
    private final Map<String, String> playerWorlds = new HashMap<>();

    private boolean enabled = false;

    private final ProxyConfig config;

    public VelocityEventListener(ProxyConfig config)
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
        proxyServer.getEventManager().register(VelocityPlugin.getInstance(), this);
    }

    @Override
    public void disable()
    {
        if (!enabled)
        {
            return;
        }
        enabled = false;
        proxyServer.getEventManager().unregisterListeners(this);
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onPermSetup(PermissionsSetupEvent e)
    {
        //load perms
        if(e.getSubject() instanceof Player)
            onLogin((Player)e.getSubject());
        
        //set up perm provider
        e.setProvider(new PermissionProvider()
        {
            @Override
            public PermissionFunction createFunction(PermissionSubject subject)
            {
                return new PermissionFunction()
                {
                    @Override
                    public Tristate getPermissionValue(String permission)
                    {
                        if (!(subject instanceof CommandSource))
                            return Tristate.fromBoolean(false);
                        CommandSource s = (CommandSource) subject;
                        boolean res = BungeePerms.getInstance().getPermissionsChecker().hasPermOrConsoleOnServerInWorld(new VelocitySender(s), permission);
                        return Tristate.fromBoolean(res);
                    }
                };
            }
        });
    }

    public void onLogin(Player p)
    {
        //don't load if cancelled
//        if (!e.getResult().isAllowed()) //not applicable
//        {
//            return;
//        }

        String playername = p.getUsername();
        UUID uuid = null;
        if (config.isUseUUIDs())
        {
            uuid = p.getUniqueId();
            BungeePerms.getLogger().info(Lang.translate(Lang.MessageType.LOGIN_UUID, playername, uuid));

            //update uuid player db
            pm().getUUIDPlayerDB().update(uuid, playername);
        }
        else
        {
            BungeePerms.getLogger().info(Lang.translate(Lang.MessageType.LOGIN, playername));
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
    }

    @Subscribe(order = PostOrder.LAST)
    public void onDisconnect(DisconnectEvent e)
    {
        String playername = e.getPlayer().getUsername();
        UUID uuid = e.getPlayer().getUniqueId();

        User u = config.isUseUUIDs() ? pm().getUser(uuid) : pm().getUser(playername);
        pm().removeUserFromCache(u);
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onServerConnected(final ServerConnectedEvent e)
    {
        //plugin messages will arrive later because plugin channels are not registered at this very moment
        playerWorlds.put(e.getPlayer().getUsername(), null);

        //send delayed uuid message to bukkit
        if (config.isUseUUIDs())
        {
            Runnable r = new Runnable()
            {
                @Override
                public void run()
                {
                    VelocityPlugin.getInstance().getNotifier().sendUUIDAndPlayer(e.getPlayer().getUsername(), e.getPlayer().getUniqueId());
                }
            };
            proxyServer.getScheduler().buildTask(VelocityPlugin.getInstance(), r).delay(1, TimeUnit.SECONDS);
        }
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onMessage(PluginMessageEvent e)
    {
//        if (!e.getTag().equalsIgnoreCase(BungeePerms.CHANNEL))
//        {
//            return;
//        }

        if (!(e.getTarget() instanceof Player))
        {
            //lock out silly hackers
            BungeePerms.getLogger().severe(Lang.translate(Lang.MessageType.INTRUSION_DETECTED, e.getSource()));
            e.setResult(PluginMessageEvent.ForwardResult.handled());
            return;
        }

        ServerConnection scon = (ServerConnection) e.getSource();

        //check network type // ignore if standalone or not registered server
        if (config.getNetworkType() == NetworkType.Standalone)
        {
            BungeePerms.getLogger().warning(Lang.translate(Lang.MessageType.MISCONFIGURATION) + ": " + Lang.translate(Lang.MessageType.MISCONFIG_BUNGEE_STANDALONE, scon.getServerInfo().getName()));
            BungeePerms.getInstance().getDebug().log(Lang.translate(Lang.MessageType.MISCONFIGURATION) + ": " + Lang.translate(Lang.MessageType.MISCONFIG_BUNGEE_STANDALONE, scon.getServerInfo().getName()));
            BungeePerms.getInstance().getDebug().log("sender = " + scon.getServerInfo().getName());
            BungeePerms.getInstance().getDebug().log("msg = " + new String(e.getData()));
            return;
        }
        if (config.getNetworkType() == NetworkType.ServerDependend && !config.getNetworkServers().contains(scon.getServerInfo().getName()))
        {
            BungeePerms.getLogger().warning(Lang.translate(Lang.MessageType.MISCONFIGURATION) + ": " + Lang.translate(Lang.MessageType.MISCONFIG_BUNGEE_SERVERDEPENDEND, scon.getServerInfo().getName()));
            BungeePerms.getInstance().getDebug().log(Lang.translate(Lang.MessageType.MISCONFIGURATION) + ": " + Lang.translate(Lang.MessageType.MISCONFIG_BUNGEE_SERVERDEPENDEND, scon.getServerInfo().getName()));
            BungeePerms.getInstance().getDebug().log("sender = " + scon.getServerInfo().getName());
            BungeePerms.getInstance().getDebug().log("msg = " + new String(e.getData()));
            return;
        }
        if (config.getNetworkType() == NetworkType.ServerDependendBlacklist && config.getNetworkServers().contains(scon.getServerInfo().getName()))
        {
            BungeePerms.getLogger().warning(Lang.translate(Lang.MessageType.MISCONFIGURATION) + ": " + Lang.translate(Lang.MessageType.MISCONFIG_BUNGEE_SERVERDEPENDENDBLACKLIST, scon.getServerInfo().getName()));
            BungeePerms.getInstance().getDebug().log(Lang.translate(Lang.MessageType.MISCONFIGURATION) + ": " + Lang.translate(Lang.MessageType.MISCONFIG_BUNGEE_SERVERDEPENDENDBLACKLIST, scon.getServerInfo().getName()));
            BungeePerms.getInstance().getDebug().log("sender = " + scon.getServerInfo().getName());
            BungeePerms.getInstance().getDebug().log("msg = " + new String(e.getData()));
            return;
        }

        //process message
        String msg = new String(e.getData());
        if (config.isDebug())
        {
            BungeePerms.getLogger().info("msg=" + msg);
        }
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
            BungeePerms.getInstance().getNetworkNotifier().deleteUser(u, scon.getServerInfo().getName());
        }
        else if (cmd.equalsIgnoreCase("deletegroup"))
        {
            Group g = pm().getGroup(userorgroup);
            pm().removeGroupFromCache(g);
            for (Group gr : pm().getGroups())
                gr.invalidateCache();
            for (User u : pm().getUsers())
                u.invalidateCache();

            //forward plugin message to network
            BungeePerms.getInstance().getNetworkNotifier().deleteGroup(g, scon.getServerInfo().getName());
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
            BungeePerms.getInstance().getNetworkNotifier().reloadUser(u, scon.getServerInfo().getName());
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
            BungeePerms.getInstance().getNetworkNotifier().reloadGroup(g, scon.getServerInfo().getName());
        }
        else if (cmd.equalsIgnoreCase("reloadusers"))
        {
            pm().reloadUsers();

            //forward plugin message to network
            BungeePerms.getInstance().getNetworkNotifier().reloadUsers(scon.getServerInfo().getName());
        }
        else if (cmd.equalsIgnoreCase("reloadgroups"))
        {
            pm().reloadGroups();

            //forward plugin message to network
            BungeePerms.getInstance().getNetworkNotifier().reloadGroups(scon.getServerInfo().getName());
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
            proxyServer.getScheduler().buildTask(VelocityPlugin.getInstance(), r).schedule();

            //forward plugin message to network except to server which issued the reload
            BungeePerms.getInstance().getNetworkNotifier().reloadAll(scon.getServerInfo().getName());
        }
        else if (cmd.equalsIgnoreCase("configcheck"))
        {
            String servername = data.get(1);
            BackEndType backend = BackEndType.getByName(data.get(2));
            boolean useuuid = Boolean.parseBoolean(data.get(3));
            PermissionsResolver.ResolvingMode resolvingmode = PermissionsResolver.ResolvingMode.valueOf(data.get(4));
            boolean groupperm = Boolean.parseBoolean(data.get(5));
            boolean regexperm = Boolean.parseBoolean(data.get(6));
            if (!scon.getServerInfo().getName().equals(servername))
            {
                BungeePerms.getLogger().warning(Lang.translate(Lang.MessageType.MISCONFIGURATION) + ": " + Lang.translate(Lang.MessageType.MISCONFIG_BUNGEE_SERVERNAME, scon.getServerInfo().getName()));
            }
            if (config.getBackendType() != backend)
            {
                BungeePerms.getLogger().warning(Lang.translate(Lang.MessageType.MISCONFIGURATION) + ": " + Lang.translate(Lang.MessageType.MISCONFIG_BUNGEE_BACKEND, scon.getServerInfo().getName()));
            }
            if (config.isUseUUIDs() != useuuid)
            {
                BungeePerms.getLogger().warning(Lang.translate(Lang.MessageType.MISCONFIGURATION) + ": " + Lang.translate(Lang.MessageType.MISCONFIG_BUNGEE_USEUUID, scon.getServerInfo().getName()));
            }
            if (config.getResolvingMode() != resolvingmode)
            {
                BungeePerms.getLogger().warning(Lang.translate(Lang.MessageType.MISCONFIGURATION) + ": " + Lang.translate(Lang.MessageType.MISCONFIG_BUNGEE_RESOLVINGMODE, scon.getServerInfo().getName()));
            }
            if (config.isGroupPermission() != groupperm)
            {
                BungeePerms.getLogger().warning(Lang.translate(Lang.MessageType.MISCONFIGURATION) + ": " + Lang.translate(Lang.MessageType.MISCONFIG_BUNGEE_GROUPPERMISSION, scon.getServerInfo().getName()));
            }
            if (config.isUseRegexPerms() != regexperm)
            {
                BungeePerms.getLogger().warning(Lang.translate(Lang.MessageType.MISCONFIGURATION) + ": " + Lang.translate(Lang.MessageType.MISCONFIG_BUNGEE_REGEXPERMISSIONS, scon.getServerInfo().getName()));
            }
        }

        e.setResult(PluginMessageEvent.ForwardResult.handled());
    }

    private PermissionsManager pm()
    {
        return BungeePerms.getInstance().getPermissionsManager();
    }
}
