package net.alpenblock.bungeeperms.platform.bukkit;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import net.alpenblock.bungeeperms.BungeePerms;
import net.alpenblock.bungeeperms.Statics;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

public class Permissible extends PermissibleBase
{

    private CommandSender sender;
    private Map<String, PermissionAttachmentInfo> permissions;
    private org.bukkit.permissions.Permissible oldpermissible = new PermissibleBase(null);

    public Permissible(CommandSender sender)
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
    }

    public boolean hasSuperPerm(String perm)
    {
        if (oldpermissible == null)
        {
            return super.hasPermission(perm);
        }
        return oldpermissible.hasPermission(perm);
    }

    @Override
    public boolean hasPermission(String permission)
    {
        boolean res = BungeePerms.getInstance().getPermissionsChecker().hasPermOrConsoleOnServerInWorld(
                sender.getName(),
                permission,
                ((BukkitConfig)BungeePerms.getInstance().getConfig()).getServername(),
                (sender instanceof Player ? ((Player) sender).getWorld().getName() : null));
//        System.out.println(sender.getName()+" has perm "+permission+"="+res);
//        if(sender instanceof ConsoleCommandSender)
//        {
//            System.out.println("console :D");
//        }

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
            super.recalculatePermissions();
            return;
        }
        oldpermissible.recalculatePermissions();
    }

    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions()
    {
        if (oldpermissible == null)
        {
            return super.getEffectivePermissions();
        }
        return new LinkedHashSet<>(permissions.values());
    }

    @Override
    public boolean isOp()
    {
        if (oldpermissible == null)
        {
            return super.isOp();
        }
        return oldpermissible.isOp();
    }

    @Override
    public void setOp(boolean value)
    {
        if (oldpermissible == null)
        {
            super.setOp(value);
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
            return super.addAttachment(plugin);
        }
        return oldpermissible.addAttachment(plugin);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, int ticks)
    {
        if (oldpermissible == null)
        {
            return super.addAttachment(plugin, ticks);
        }
        return oldpermissible.addAttachment(plugin, ticks);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value)
    {
        if (oldpermissible == null)
        {
            return super.addAttachment(plugin, name, value);
        }
        return oldpermissible.addAttachment(plugin, name, value);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value, int ticks)
    {
        if (oldpermissible == null)
        {
            return super.addAttachment(plugin, name, value, ticks);
        }
        return oldpermissible.addAttachment(plugin, name, value, ticks);
    }

    @Override
    public void removeAttachment(PermissionAttachment attachment)
    {
        if (oldpermissible == null)
        {
            super.removeAttachment(attachment);
            return;
        }
        oldpermissible.removeAttachment(attachment);
    }

    @Override
    public synchronized void clearPermissions()
    {
        if (oldpermissible == null)
        {
            super.clearPermissions();
            return;
        }
        if (oldpermissible instanceof PermissibleBase)
        {
            PermissibleBase base = (PermissibleBase) oldpermissible;
            base.clearPermissions();
        }
    }
}
