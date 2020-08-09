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
package net.alpenblock.bungeeperms.platform.bukkit.bridge.bridges.essentials;

import com.earth2me.essentials.perm.IPermissionsHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import net.alpenblock.bungeeperms.BungeePerms;
import net.alpenblock.bungeeperms.Group;
import net.alpenblock.bungeeperms.Statics;
import net.alpenblock.bungeeperms.User;
import net.alpenblock.bungeeperms.platform.bukkit.BukkitSender;
import org.bukkit.entity.Player;

class BungeePermsHandler implements IPermissionsHandler
{

    private final BungeePerms perms;

    public BungeePermsHandler()
    {
        perms = BungeePerms.getInstance();
    }

    @Override
    public String getGroup(Player player)
    {
        User u = perms.getPermissionsManager().getUser(player.getName());
        if (u == null)
        {
            return "";
        }
        Group g = perms.getPermissionsManager().getMainGroup(u);
        return g == null ? "" : g.getName();
    }

    @Override
    public List<String> getGroups(Player player)
    {
        User u = perms.getPermissionsManager().getUser(player.getName());
        if (u == null)
            return new ArrayList();

        return new ArrayList(u.getGroupsString());
    }

    @Override
    public boolean canBuild(Player player, String group)
    {
        return true;
    }

    @Override
    public boolean inGroup(Player player, String group)
    {
        User u = perms.getPermissionsManager().getUser(player.getName());
        if (u == null)
            return false;

        Group g = perms.getPermissionsManager().getGroup(group);
        if (g == null)
            return false;

        return u.getGroups().contains(g);
    }

    @Override
    public boolean hasPermission(Player player, String node)
    {
        return perms.getPermissionsChecker().hasPermOrConsoleOnServerInWorld(new BukkitSender(player), Statics.toLower(node));
    }

    @Override
    public String getPrefix(Player player)
    {
        User u = perms.getPermissionsManager().getUser(player.getName());
        if (u == null)
            return "";

        return u.buildPrefix(new BukkitSender(player));
    }

    @Override
    public String getSuffix(Player player)
    {
        User u = perms.getPermissionsManager().getUser(player.getName());
        if (u == null)
            return "";

        return u.buildSuffix(new BukkitSender(player));
    }

    @Override
    public boolean isPermissionSet(Player player, String string)
    {
        User u = perms.getPermissionsManager().getUser(player.getName());
        if (u == null)
            return false;

        BukkitSender sender = new BukkitSender(player);
        return u.getEffectivePerms(sender.getServer(), sender.getWorld()).contains(string.toLowerCase());
    }

    @Override
    public boolean tryProvider()
    {
        return true;
    }

    @Override
    public void registerContext(String context, Function<Player, Iterable<String>> calculator, Supplier<Iterable<String>> suggestions)
    {
    }

    @Override
    public void unregisterContexts()
    {
    }

    @Override
    public String getBackendName()
    {
        return "BungeePerms";
    }
}
