package net.alpenblock.bungeeperms.platform.bukkit;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.alpenblock.bungeeperms.BungeePerms;
import net.alpenblock.bungeeperms.Statics;
import net.alpenblock.bungeeperms.User;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;

public class Permissible extends PermissibleBase
{

    private CommandSender sender;
    private PermissionAttachment attachment;
    private Map<String, PermissionAttachmentInfo> permissions;
    private List<PermissionAttachment> attachments;
    private org.bukkit.permissions.Permissible oldpermissible = new PermissibleBase(null);

    public Permissible(CommandSender sender, User u)
    {
        super(sender);
        this.sender = sender;
        permissions = new LinkedHashMap<String, PermissionAttachmentInfo>()
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
        };
        Statics.setField(PermissibleBase.class, this, permissions, "permissions");
    }

    public org.bukkit.permissions.Permissible getOldPermissible()
    {
        return oldpermissible;
    }

    public void setOldPermissible(org.bukkit.permissions.Permissible oldPermissible)
    {
        this.oldpermissible = oldPermissible;
        attachments = (List<PermissionAttachment>) Statics.getField(this, PermissibleBase.class, "attachments");
        Statics.setField(PermissibleBase.class, oldpermissible, permissions, "permissions");
        recalculatePermissions();
    }

    public boolean hasSuperPerm(String perm)
    {
        if (oldpermissible == null)
        {
            return false;
        }
        return oldpermissible.hasPermission(perm);
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
        if (oldpermissible == null)
        {
            return;
        }
        oldpermissible.recalculatePermissions();
    }

    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions()
    {
        return new LinkedHashSet<>(permissions.values());
    }

    @Override
    public boolean isOp()
    {
        return oldpermissible == null ? false : oldpermissible.isOp();
    }

    @Override
    public void setOp(boolean value)
    {
        if (oldpermissible == null)
        {
            return;
        }
        oldpermissible.setOp(value);
    }

    @Override
    public boolean isPermissionSet(String permission)
    {
        return true;
    }

    @Override
    public boolean isPermissionSet(Permission perm)
    {
        return true;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin)
    {
        if (oldpermissible == null)
        {
            return null;
        }
        return oldpermissible.addAttachment(plugin);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, int ticks)
    {
        if (oldpermissible == null)
        {
            return null;
        }
        return oldpermissible.addAttachment(plugin, ticks);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value)
    {
        if (oldpermissible == null)
        {
            return null;
        }
        return oldpermissible.addAttachment(plugin, name, value);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value, int ticks)
    {
        if (oldpermissible == null)
        {
            return null;
        }
        return oldpermissible.addAttachment(plugin, name, value, ticks);
    }

    @Override
    public void removeAttachment(PermissionAttachment attachment)
    {
        if (oldpermissible == null)
        {
            return;
        }
        oldpermissible.removeAttachment(attachment);
    }

    @Override
    public synchronized void clearPermissions()
    {
        if (oldpermissible == null)
        {
            return;
        }
        if (oldpermissible instanceof PermissibleBase)
        {
            PermissibleBase base = (PermissibleBase) oldpermissible;
            base.clearPermissions();
        }
    }

    public void updateAttachment(User u, String server, String world)
    {
        //create attachment if no existing
        if (attachment == null)
        {
            attachment = sender.addAttachment(BukkitPlugin.getInstance());
            attachment.setPermission(getUserNodeName(u), true);
        }

        Permission perm = getUserNode(u);

        //add perms
        perm.getChildren().clear();
        for (String p : u.getEffectivePerms(server, world))
        {
            if (p.startsWith("-"))
            {
                perm.getChildren().put(p.substring(1), false);
            }
            else
            {
                perm.getChildren().put(p, true);
            }
        }
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
        if (sender instanceof ConsoleCommandSender)
        {
            return "bungeeperms.console";
        }
        String id = BungeePerms.getInstance().getConfig().isUseUUIDs() ? u.getUUID().toString() : u.getName();
        return "bungeeperms.user." + id;
    }
}
