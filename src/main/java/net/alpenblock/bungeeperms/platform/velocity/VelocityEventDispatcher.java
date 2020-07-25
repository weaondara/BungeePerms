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

import net.alpenblock.bungeeperms.platform.bungee.*;
import net.alpenblock.bungeeperms.Group;
import net.alpenblock.bungeeperms.User;
import net.alpenblock.bungeeperms.platform.EventDispatcher;
import net.alpenblock.bungeeperms.platform.bungee.event.BungeePermsGroupChangedEvent;
import net.alpenblock.bungeeperms.platform.bungee.event.BungeePermsReloadedEvent;
import net.alpenblock.bungeeperms.platform.bungee.event.BungeePermsUserChangedEvent;
import net.md_5.bungee.api.ProxyServer;

public class VelocityEventDispatcher implements EventDispatcher
{

    @Override
    public void dispatchReloadedEvent()
    {
        ProxyServer.getInstance().getPluginManager().callEvent(new BungeePermsReloadedEvent());
    }

    @Override
    public void dispatchGroupChangeEvent(Group g)
    {
        ProxyServer.getInstance().getPluginManager().callEvent(new BungeePermsGroupChangedEvent(g));
    }

    @Override
    public void dispatchUserChangeEvent(User u)
    {
        ProxyServer.getInstance().getPluginManager().callEvent(new BungeePermsUserChangedEvent(u));
    }

}
