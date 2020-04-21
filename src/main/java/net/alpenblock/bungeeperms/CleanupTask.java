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
package net.alpenblock.bungeeperms;

public class CleanupTask implements Runnable
{

    @Override
    public void run()
    {
        BungeePerms bp = BungeePerms.getInstance();
        PermissionsManager pm = bp.getPermissionsManager();

        long threshold = bp.getConfig().getCleanupThreshold() * 1000;

        pm.getUserlock().writeLock().lock();
        try
        {
            for (User u : pm.getUsers())
            {
                if ((bp.getConfig().isUseUUIDs() ? bp.getPlugin().getPlayer(u.getUUID()) : bp.getPlugin().getPlayer(u.getName())) != null)
                {
                    continue;
                }
                if (u.getLastAccess() + threshold < System.currentTimeMillis())
                {
                    pm.removeUserFromCache(u);
                }
            }
        }
        finally
        {
            pm.getUserlock().writeLock().unlock();
        }
    }
}
