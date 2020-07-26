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
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import net.alpenblock.bungeeperms.platform.proxy.NetworkType;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import net.alpenblock.bungeeperms.Group;
import net.alpenblock.bungeeperms.Statics;
import net.alpenblock.bungeeperms.User;
import net.alpenblock.bungeeperms.platform.NetworkNotifier;
import net.alpenblock.bungeeperms.platform.proxy.ProxyConfig;

@RequiredArgsConstructor
public class VelocityNotifier implements NetworkNotifier
{
    @Inject
    private ProxyServer proxyServer;
    
    private final ProxyConfig config;

    @Override
    public void deleteUser(User u, String origin)
    {
        if (config.isUseUUIDs())
        {
            sendPM(u.getUUID(), "deleteUser;" + u.getUUID(), origin);
        }
        else
        {
            sendPM(u.getName(), "deleteUser;" + u.getName(), origin);
        }
    }

    @Override
    public void deleteGroup(Group g, String origin)
    {
        sendPMAll("deleteGroup;" + g.getName(), origin);
    }

    @Override
    public void reloadUser(User u, String origin)
    {
        if (config.isUseUUIDs())
        {
            sendPM(u.getUUID(), "reloadUser;" + u.getUUID(), origin);
        }
        else
        {
            sendPM(u.getName(), "reloadUser;" + u.getName(), origin);
        }
    }

    @Override
    public void reloadGroup(Group g, String origin)
    {
        sendPMAll("reloadGroup;" + g.getName(), origin);
    }

    @Override
    public void reloadUsers(String origin)
    {
        sendPMAll("reloadUsers", origin);
    }

    @Override
    public void reloadGroups(String origin)
    {
        sendPMAll("reloadGroups", origin);
    }

    @Override
    public void reloadAll(String origin)
    {
        sendPMAll("reloadall", origin);
    }

    public void sendUUIDAndPlayer(String name, UUID uuid)
    {
        if (config.isUseUUIDs())
        {
            sendPM(uuid, "uuidcheck;" + name + ";" + uuid, null);
        }
    }

    //bukkit-bungeeperms reload information functions
    private void sendPM(String player, String msg, String origin)
    {
        //if standalone no network messages
        if (config.getNetworkType() == NetworkType.Standalone)
        {
            return;
        }

        Player pp = proxyServer.getPlayer(player).get();
        if (pp != null && pp.getCurrentServer() != null)
        {
            //ignore servers not in config and netork type is server dependend
            if (config.getNetworkType() == NetworkType.ServerDependend
                && !Statics.listContains(config.getNetworkServers(), pp.getCurrentServer().get().getServerInfo().getName()))
            {
                return;
            }
            if (config.getNetworkType() == NetworkType.ServerDependendBlacklist
                && Statics.listContains(config.getNetworkServers(), pp.getCurrentServer().get().getServerInfo().getName()))
            {
                return;
            }

            //no feedback loop
            if (origin != null && pp.getCurrentServer().get().getServerInfo().getName().equalsIgnoreCase(origin))
            {
                return;
            }

            //send message
            pp.getCurrentServer().get().sendPluginMessage(VelocityPlugin.getCi(), msg.getBytes());
            sendConfig(pp.getCurrentServer().get().getServerInfo());
        }
    }

    private void sendPM(UUID player, String msg, String origin)
    {
        //if standalone no network messages
        if (config.getNetworkType() == NetworkType.Standalone)
        {
            return;
        }

        Player pp = proxyServer.getPlayer(player).get();
        if (pp != null && pp.getCurrentServer() != null)
        {
            //ignore servers not in config and netork type is server dependend
            if (config.getNetworkType() == NetworkType.ServerDependend
                && !Statics.listContains(config.getNetworkServers(), pp.getCurrentServer().get().getServerInfo().getName()))
            {
                return;
            }
            if (config.getNetworkType() == NetworkType.ServerDependendBlacklist
                && Statics.listContains(config.getNetworkServers(), pp.getCurrentServer().get().getServerInfo().getName()))
            {
                return;
            }

            //no feedback loop
            if (origin != null && pp.getCurrentServer().get().getServerInfo().getName().equalsIgnoreCase(origin))
            {
                return;
            }

            //send message
            pp.getCurrentServer().get().sendPluginMessage(VelocityPlugin.getCi(), msg.getBytes());
            sendConfig(pp.getCurrentServer().get().getServerInfo());
        }
    }

    private void sendPMAll(String msg, String origin)
    {
        //if standalone no network messages
        if (config.getNetworkType() == NetworkType.Standalone)
        {
            return;
        }

        for (String si : proxyServer.getConfiguration().getServers().values())
        {
            //ignore servers not in config and netork type is server dependend
            if (config.getNetworkType() == NetworkType.ServerDependend
                && !Statics.listContains(config.getNetworkServers(), si))
            {
                return;
            }
            if (config.getNetworkType() == NetworkType.ServerDependendBlacklist
                && Statics.listContains(config.getNetworkServers(), si))
            {
                return;
            }

            //no feedback loop
            if (origin != null && si.equalsIgnoreCase(origin))
            {
                continue;
            }

            //send message
            proxyServer.getServer(si).get().sendPluginMessage(VelocityPlugin.getCi(), msg.getBytes());
            sendConfig(proxyServer.getServer(si).get().getServerInfo());
        }
    }

    private long lastConfigUpdate = 0;

    private void sendConfig(ServerInfo info)
    {
        RegisteredServer rs = proxyServer.getServer(info.getName()).get();
        synchronized (this)
        {
            long now = System.currentTimeMillis();
            if (lastConfigUpdate + 5 * 60 * 1000 < now)
            {
                lastConfigUpdate = now;
                rs.sendPluginMessage(VelocityPlugin.getCi(), ("configcheck"
                                                    + ";" + info.getName()
                                                    + ";" + config.getBackendType()
                                                    + ";" + config.isUseUUIDs()
                                                    + ";" + config.getResolvingMode()
                                                    + ";" + config.isGroupPermission()
                                                    + ";" + config.isUseRegexPerms()).getBytes());
            }
        }
    }
}
