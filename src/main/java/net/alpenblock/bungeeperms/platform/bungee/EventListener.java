package net.alpenblock.bungeeperms.platform.bungee;

import net.alpenblock.bungeeperms.BungeePerms;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import net.alpenblock.bungeeperms.Group;
import net.alpenblock.bungeeperms.PermissionsManager;
import net.alpenblock.bungeeperms.User;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PermissionCheckEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class EventListener implements Listener
{

    @EventHandler(priority = Byte.MIN_VALUE)
    public void onLogin(LoginEvent e)
    {
        String playername = e.getConnection().getName();
        UUID uuid = null;
        if (pm().isUseUUIDs())
        {
            uuid = e.getConnection().getUniqueId();
            BungeeCord.getInstance().getLogger().log(Level.INFO, "[BungeePerms] Login by {0} ({1})", new Object[]
                                             {
                                                 playername, uuid
            });

            //update uuid player db
            pm().getUUIDPlayerDB().update(uuid, playername);
        }
        else
        {
            BungeeCord.getInstance().getLogger().log(Level.INFO, "[BungeePerms] Login by {0}", new Object[]
                                             {
                                                 playername
            });
        }

        User u = pm().isUseUUIDs() ? pm().getUser(uuid) : pm().getUser(playername);
        if (u == null)
        {
            BungeeCord.getInstance().getLogger().log(Level.INFO, "[BungeePerms] Adding default groups to {0} ({1})", new Object[]
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

        User u = pm().isUseUUIDs() ? pm().getUser(uuid) : pm().getUser(playername);
        pm().removeUserFromCache(u);
    }

    @EventHandler
    public void onPermissionCheck(PermissionCheckEvent e)
    {
        e.setHasPermission(pm().hasPermOrConsoleOnServerInWorld(e.getSender(), e.getPermission()));
    }

    private PermissionsManager pm()
    {
        return BungeePerms.getInstance().getPermissionsManager();
    }
}
