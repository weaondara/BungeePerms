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

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import lombok.Getter;
import net.alpenblock.bungeeperms.BPPermission;
import net.alpenblock.bungeeperms.BungeePerms;
import net.alpenblock.bungeeperms.Statics;
import net.alpenblock.bungeeperms.User;
import net.alpenblock.bungeeperms.platform.Sender;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.permissions.ServerOperator;
import org.bukkit.plugin.Plugin;

public class BPPermissible extends PermissibleBase
{

    private final CommandSender sender;
    private PermissionAttachment attachment;
    private final Map<String, PermissionAttachmentInfo> permissions;
    private final Map<String, PermissionAttachmentInfo> superperms;
    @Getter
    private final Permissible oldPermissible;
    private final ServerOperator oldOpable;
    private final ServerOperator opable;
    private boolean opdisabled = false;
    private boolean init = false;

    public BPPermissible(CommandSender sender, User u, Permissible oldPermissible)
    {
        super(sender);
        this.sender = sender;
        this.oldPermissible = oldPermissible;
        permissions = Collections.synchronizedMap(new LinkedHashMap<String, PermissionAttachmentInfo>()
        {
            @Override
            public PermissionAttachmentInfo put(String k, PermissionAttachmentInfo v)
            {
                PermissionAttachmentInfo existing = this.get(k);
                if (existing != null)
                {
                    return existing;
                }
                return super.put(k, v);
            }
        });
        superperms = Collections.synchronizedMap(new LinkedHashMap<String, PermissionAttachmentInfo>()
        {
            @Override
            public PermissionAttachmentInfo put(String k, PermissionAttachmentInfo v)
            {
                PermissionAttachmentInfo existing = this.get(k);
                if (existing != null)
                {
                    return existing;
                }
                return super.put(k, v);
            }
        });

        //inject an opable
        oldOpable = Statics.getField(PermissibleBase.class, oldPermissible, ServerOperator.class, "opable");
        opable = new ServerOperator()
        {
            @Override
            public boolean isOp()
            {
                BukkitConfig config = (BukkitConfig) BungeePerms.getInstance().getConfig();
                if (opdisabled)
                {
                    if (!config.isAllowops())
                    {
                        return false;
                    }
                }
                if (config.isDebug())
                {
                    BungeePerms.getLogger().info("op check: " + BPPermissible.this.sender.getName() + " has OP: " + oldOpable.isOp());
                }
                return oldOpable.isOp();
            }

            @Override
            public void setOp(boolean value)
            {
                oldOpable.setOp(value);
            }
        };

        init = true;

        recalculatePermissions();
    }

    public boolean hasSuperPerm(String perm)
    {
        if (oldPermissible == null)
        {
            return false;
        }
        return oldPermissible.hasPermission(perm);
    }

    @Override
    public boolean hasPermission(String permission)
    {
        boolean res = BungeePerms.getInstance().getPermissionsChecker().hasPermOrConsoleOnServerInWorld(new BukkitSender(sender), permission);
        return res;
    }

    @Override
    public boolean hasPermission(Permission permission)
    {
        return hasPermission(permission.getName());
    }

    @Override
    public void recalculatePermissions()
    {
        if (!init)
        {
            return;
        }

        //calculate superperms
        opdisabled = true;
        oldPermissible.recalculatePermissions();
        opdisabled = false;

        permissions.clear();
        synchronized(superperms) {
            permissions.putAll(superperms);
        }

        BukkitConfig config = (BukkitConfig) BungeePerms.getInstance().getConfig();

        //clear perm check result caches
        User u = config.isUseUUIDs() && sender instanceof Player
                 ? BungeePerms.getInstance().getPermissionsManager().getUser(((Player) sender).getUniqueId())
                 : BungeePerms.getInstance().getPermissionsManager().getUser(sender.getName());
        if (u != null)
        {
            u.flushCache();
        }

        //calc bp perms if enabled
        if (config.isSuperpermscompat())
        {
            //get bp perms and override superperms
            if (!(sender instanceof Player))
            {
                return;
            }

            Sender s = config.isUseUUIDs()
                       ? BungeePerms.getInstance().getPlugin().getPlayer(((Player) sender).getUniqueId())
                       : BungeePerms.getInstance().getPlugin().getPlayer(sender.getName());
            if (u == null || s == null)
            {
                return;
            }

            List<BPPermission> perms = u.getEffectivePerms(s.getServer(), s.getWorld());
            List<PermissionAttachmentInfo> childperms = addChildPerms(perms);

            if (!config.isUseRegexPerms())
            {
                for (PermissionAttachmentInfo pai : childperms)
                {
                    if (pai.getPermission().endsWith("*"))
                    {
                        boolean found = false;
                        String regex = "^" + pai.getPermission().toLowerCase().replaceAll("\\.", "\\\\.").replaceAll("\\*", ".*") + "$";
                        Pattern pat = Pattern.compile(regex);
                        for (Map.Entry<String, PermissionAttachmentInfo> e : permissions.entrySet())
                        {
                            if (pat.matcher(e.getKey().toLowerCase()).find())
                            {
                                Statics.setField(e.getValue(), pai.getValue(), "value");
                                if (e.getKey().equalsIgnoreCase(pai.getPermission()))
                                {
                                    found = true;
                                }
                            }
                        }
                        if (!found)
                        {
                            permissions.put(pai.getPermission(), pai);
                        }
                    }
                    else
                    {
                        boolean found = false;
                        for (Map.Entry<String, PermissionAttachmentInfo> e : permissions.entrySet())
                        {
                            if (e.getKey().equalsIgnoreCase(pai.getPermission()))
                            {
                                Statics.setField(e.getValue(), pai.getValue(), "value");
                                found = true;
                                break;
                            }
                        }
                        if (!found)
                        {
                            permissions.put(pai.getPermission(), pai);
                        }
                    }
                }
            }
            else
            {
                //todo do regex check
                for (PermissionAttachmentInfo pai : childperms)
                {
                    permissions.put(pai.getPermission(), pai);
                }
            }

            //todo fix user.hasperm with superperms
        }
    }

    private List<PermissionAttachmentInfo> addChildPerms(List<BPPermission> perms)
    {
        Map<String, Boolean> map = new LinkedHashMap();
        for (BPPermission perm : perms)
        {
            map.put(perm.getPermission().startsWith("-") ? perm.getPermission().substring(1) : perm.getPermission(), !perm.getPermission().startsWith("-"));
        }

        return addChildPerms(map);
    }

    private List<PermissionAttachmentInfo> addChildPerms(Map<String, Boolean> perms)
    {
        List<PermissionAttachmentInfo> permlist = new LinkedList();
        for (Map.Entry<String, Boolean> perm : perms.entrySet())
        {
            PermissionAttachmentInfo pai = new PermissionAttachmentInfo(oldPermissible, perm.getKey().toLowerCase(), null, perm.getValue());
            permlist.add(pai);
            Permission permission = Bukkit.getPluginManager().getPermission(pai.getPermission());
            if (permission != null && !permission.getChildren().isEmpty())
            {
                permlist.addAll(addChildPerms(permission.getChildren()));
            }
        }
        return permlist;
    }

    public Set<PermissionAttachmentInfo> getEffectiveSuperPerms()
    {
        synchronized(superperms) {
            return new LinkedHashSet<>(superperms.values());
        }
    }

    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions()
    {
        synchronized(permissions) {
            return new LinkedHashSet<>(permissions.values());
        }
    }

    @Override
    public boolean isOp()
    {
        return oldPermissible.isOp();
    }

    @Override
    public void setOp(boolean value)
    {
        oldPermissible.setOp(value);
    }

    @Override
    public boolean isPermissionSet(String permission)
    {
        return true;
    }

    @Override
    public boolean isPermissionSet(Permission perm)
    {
        return isPermissionSet(perm.getName());
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin)
    {
        return oldPermissible.addAttachment(plugin);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, int ticks)
    {
        return oldPermissible.addAttachment(plugin, ticks);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value)
    {
        return oldPermissible.addAttachment(plugin, name, value);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value, int ticks)
    {
        return oldPermissible.addAttachment(plugin, name, value, ticks);
    }

    @Override
    public void removeAttachment(PermissionAttachment attachment)
    {
        oldPermissible.removeAttachment(attachment);
    }

    @Override
    public synchronized void clearPermissions()
    {
        if (oldPermissible instanceof PermissibleBase)
        {
            PermissibleBase base = (PermissibleBase) oldPermissible;
            base.clearPermissions();
        }
    }

    public void updateAttachment(User u, String server, String world)
    {
        //create attachment if not existing
        if (attachment == null)
        {
            attachment = addAttachment(BukkitPlugin.getInstance());
            attachment.setPermission(getUserNodeName(u), true);
        }

        Permission perm = getUserNode(u);
        recalculatePermissions();
    }

    public void removeAttachment()
    {
        if (attachment != null)
        {
            removeAttachment(attachment);
            attachment.remove();
        }
    }

    private Permission getUserNode(User u)
    {
        String permname = getUserNodeName(u);
        Permission perm = Bukkit.getPluginManager().getPermission(permname);
        if (perm == null)
        {
            perm = new Permission(permname, "Internal permission for BungeePerms. DO NOT SET DIRECTLY", PermissionDefault.FALSE)
            {
                @Override
                public void recalculatePermissibles()
                {
                    // nothing to do here
                }
            };
            Bukkit.getPluginManager().addPermission(perm);
        }

        return perm;
    }

    private String getUserNodeName(User u)
    {
        return getUserNodeName(sender, u);
    }
    
    static String getUserNodeName(CommandSender sender, User u) {
        if (sender instanceof ConsoleCommandSender)
        {
            return "bungeeperms.console";
        }
        String id = BungeePerms.getInstance().getConfig().isUseUUIDs() ? u.getUUID().toString() : u.getName();
        return "bungeeperms.user." + id;
    }

    //injection things
    public void inject()
    {
        if (Injector.getPermissible(sender) == this)
        {
            return;
        }
        Statics.setField(PermissibleBase.class, oldPermissible, superperms, "permissions");
        Statics.setField(PermissibleBase.class, this, permissions, "permissions");
        Statics.setField(PermissibleBase.class, oldPermissible, opable, "opable");
        Injector.inject(sender, this);

        recalculatePermissions();
    }

    public void uninject()
    {
        if (Injector.getPermissible(sender) != this)
        {
            return;
        }
        Statics.setField(PermissibleBase.class, oldPermissible, new HashMap(), "permissions");
        Statics.setField(PermissibleBase.class, oldPermissible, oldOpable, "opable");
        Injector.inject(sender, oldPermissible);

        recalculatePermissions();
    }
}
