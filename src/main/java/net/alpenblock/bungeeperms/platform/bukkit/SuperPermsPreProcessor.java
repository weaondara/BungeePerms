package net.alpenblock.bungeeperms.platform.bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.alpenblock.bungeeperms.BPPermission;
import net.alpenblock.bungeeperms.BungeePerms;
import net.alpenblock.bungeeperms.PermissionsPreProcessor;
import net.alpenblock.bungeeperms.platform.Sender;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachmentInfo;

public class SuperPermsPreProcessor implements PermissionsPreProcessor
{

    @Override
    public List<String> process(List<String> perms, Sender s)
    {
        BukkitConfig config = (BukkitConfig) BungeePerms.getInstance().getConfig();
        if (!config.isSuperpermscompat())
        {
            return perms;
        }

        if (s != null)
        {
            perms.addAll(0, getSuperPerms(s));
        }

        //expand permissions
        expandChildPerms(perms);

        return perms;
    }

    private List<String> getSuperPerms(Sender s)
    {
        BukkitSender bs = (BukkitSender) s;
        CommandSender sender = bs.getSender();
        if (!(sender instanceof Player))
        {
            return new ArrayList();
        }

        Player p = (Player) sender;
        Permissible base = Injector.getPermissible(p);
        if (!(base instanceof BPPermissible))
        {
            return new ArrayList();
        }

        BPPermissible perm = (BPPermissible) base;
        List<String> l = new ArrayList(perm.getEffectiveSuperPerms().size());
        for (PermissionAttachmentInfo e : perm.getEffectiveSuperPerms())
        {
            l.add((e.getValue() ? "" : "-") + e.getPermission().toLowerCase());
        }
        return l;
    }

    private List<String> expandChildPerms(List<String> perms)
    {
        for (int i = 0; i < perms.size(); i++)
        {
            //get perm info
            String perm = perms.get(i);
            boolean neg = perm.startsWith("-");
            perm = neg ? perm.substring(1) : perm;

            //check perm
            Permission p = Bukkit.getPluginManager().getPermission(perm);
            if (p == null || p.getChildren().isEmpty())
            {
                continue;
            }

            //add all children
            List<String> l = new ArrayList();
            for (Map.Entry<String, Boolean> e : p.getChildren().entrySet())
            {
                l.add((e.getValue() ? "" : "-") + e.getKey().toLowerCase());
            }
            perms.addAll(i + 1, l);
        }

        return perms;
    }

    @Override
    public List<BPPermission> processWithOrigin(List<BPPermission> perms, Sender s)
    {
        BukkitConfig config = (BukkitConfig) BungeePerms.getInstance().getConfig();
        if (!config.isSuperpermscompat())
        {
            return perms;
        }

        if (s != null)
        {
            List<String> l = getSuperPerms(s);
            for (int i = 0; i < l.size(); i++)
            {
                perms.add(i, new BPPermission(l.get(i), "SuperPerms", true, null, null));
            }
        }

        //expand permissions
        expandChildPermsWithOrigin(perms);

        return perms;
    }

    private List<BPPermission> expandChildPermsWithOrigin(List<BPPermission> perms)
    {
        for (int i = 0; i < perms.size(); i++)
        {
            //get perm info
            String perm = perms.get(i).getPermission();
            boolean neg = perm.startsWith("-");
            perm = neg ? perm.substring(1) : perm;

            //check perm
            Permission p = Bukkit.getPluginManager().getPermission(perm);
            if (p == null || p.getChildren().isEmpty())
            {
                continue;
            }

            //add all children
            List<BPPermission> l = new ArrayList();
            for (Map.Entry<String, Boolean> e : p.getChildren().entrySet())
            {
                l.add(new BPPermission((e.getValue() ? "" : "-") + e.getKey().toLowerCase(), "SuperPerms child of " + perm, true, null, null));
            }
            perms.addAll(i + 1, l);
        }

        return perms;
    }
}
