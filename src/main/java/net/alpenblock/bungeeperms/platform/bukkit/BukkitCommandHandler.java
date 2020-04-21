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
package net.alpenblock.bungeeperms.platform.bukkit;

import net.alpenblock.bungeeperms.BPConfig;
import net.alpenblock.bungeeperms.CommandHandler;
import net.alpenblock.bungeeperms.PermissionsChecker;
import net.alpenblock.bungeeperms.platform.PlatformPlugin;
import net.alpenblock.bungeeperms.platform.Sender;

public class BukkitCommandHandler extends CommandHandler
{

    public BukkitCommandHandler(PlatformPlugin plugin, PermissionsChecker checker, BPConfig config)
    {
        super(plugin, checker, config);
    }

    @Override
    public boolean onCommand(Sender sender, String cmd, String label, String[] args)
    {
        boolean b = super.onCommand(sender, cmd, label, args);
        if (b)
        {
            return b;
        }
        return BukkitPlugin.getInstance().getBridge().onCommand(sender, cmd, label, args);
    }
}
