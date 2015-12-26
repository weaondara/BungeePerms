package net.alpenblock.bungeeperms.platform.bukkit;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import lombok.Getter;
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

    private CommandSender sender;
    private PermissionAttachment attachment;
    private Map<String, PermissionAttachmentInfo> permissions;
    private Map<String, PermissionAttachmentInfo> superperms;
    @Getter
    private Permissible oldPermissible = null;
    private ServerOperator oldOpable = null;
    private ServerOperator opable = null;
    private boolean opdisabled = false;
    private boolean init = false;

    public BPPermissible(CommandSender sender, User u, Permissible oldPermissible)
    {
        super(sender);
        this.sender = sender;
        this.oldPermissible = oldPermissible;
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
        superperms = new LinkedHashMap<String, PermissionAttachmentInfo>()
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

        //inject an opable
        oldOpable = Statics.getField(PermissibleBase.class, oldPermissible, ServerOperator.class, "opable");
        opable = new ServerOperator()
        {
            @Override
            public boolean isOp()
            {
                if (opdisabled)
                {
                    BukkitConfig config = (BukkitConfig) BungeePerms.getInstance().getConfig();
                    if (!config.isAllowops())
                    {
                        return false;
                    }
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
        permissions.putAll(superperms);

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

            List<String> perms = u.getEffectivePerms(s.getServer(), s.getWorld());
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

    private List<PermissionAttachmentInfo> addChildPerms(List<String> perms)
    {
        Map<String, Boolean> map = new LinkedHashMap();
        for (String perm : perms)
        {
            map.put(perm.startsWith("-") ? perm.substring(1) : perm, !perm.startsWith("-"));
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
        return new LinkedHashSet<>(superperms.values());
    }

    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions()
    {
        return new LinkedHashSet<>(permissions.values());
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
        return true;
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
        Statics.setField(PermissibleBase.class, oldPermissible, new HashMap<String, PermissionAttachmentInfo>(), "permissions");
        Statics.setField(PermissibleBase.class, oldPermissible, oldOpable, "opable");
        Injector.inject(sender, oldPermissible);

        recalculatePermissions();
    }
}
