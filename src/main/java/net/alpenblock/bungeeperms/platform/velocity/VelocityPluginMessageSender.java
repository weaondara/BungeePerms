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

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import lombok.RequiredArgsConstructor;
import net.alpenblock.bungeeperms.BungeePerms;
import net.alpenblock.bungeeperms.platform.PluginMessageSender;

@RequiredArgsConstructor
public class VelocityPluginMessageSender implements PluginMessageSender
{

    private final ProxyServer proxyServer;
    
    @Override
    public void sendPluginMessage(String target, String channel, String msg)
    {
        RegisteredServer si = proxyServer.getServer(target).get();
        String[] split = channel.split(":");
        MinecraftChannelIdentifier i = MinecraftChannelIdentifier.create(split[0], split[1]);
        if (si == null)
        {
            BungeePerms.getLogger().info("No server found for " + target);
            return;
        }

        proxyServer.getServer(target).get().sendPluginMessage(i, msg.getBytes());
    }
}
