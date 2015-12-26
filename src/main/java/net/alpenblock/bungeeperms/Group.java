package net.alpenblock.bungeeperms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Group implements Comparable<Group>, PermEntity
{

    @Getter(value = AccessLevel.PRIVATE)
    @Setter(value = AccessLevel.PRIVATE)
    private Map<String, Map<String, List<String>>> cachedPerms;

    private String name;
    private List<String> inheritances;
    private List<String> perms;
    private Map<String, Server> servers;
    private int rank;
    private int weight;
    private String ladder;
    private boolean isdefault;
    private String display;
    private String prefix;
    private String suffix;

    public Group(String name, List<String> inheritances, List<String> perms, Map<String, Server> servers, int rank, int weight, String ladder, boolean isdefault, String display, String prefix, String suffix)
    {
        this.name = name;
        this.inheritances = inheritances;
        this.perms = perms;
        this.servers = servers;
        this.rank = rank;
        this.weight = weight;
        this.ladder = ladder;
        this.isdefault = isdefault;
        this.display = display;
        this.prefix = prefix;
        this.suffix = suffix;

        cachedPerms = new HashMap<>();
    }

    @Override
    public Server getServer(String name)
    {
        name = Statics.toLower(name);
        if (name == null)
        {
            return null;
        }
        Server s = servers.get(name);
        if (s == null)
        {
            s = new Server(name, new ArrayList<String>(), new HashMap<String, World>(), null, null, null);
            servers.put(name, s);
        }

        return s;
    }

    public boolean isDefault()
    {
        return isdefault;
    }

    public void setDefault(boolean isdefault)
    {
        this.isdefault = isdefault;
    }

    public List<String> getEffectivePerms(String server, String world)
    {
        server = Statics.toLower(server);
        world = Statics.toLower(world);

        List<String> effperms;
        if (existsPermCacheList(server, world))
        {
            effperms = getCachedPerms(server, world);
        }
        else
        {
            effperms = calcEffectivePerms(server, world);
            setCachedPerms(effperms, server, world);
        }

        return new ArrayList(effperms);
    }

    public List<String> calcEffectivePerms(String server, String world)
    {
        server = Statics.toLower(server);
        world = Statics.toLower(world);

        List<String> ret = new ArrayList<>();
        for (Group g : BungeePerms.getInstance().getPermissionsManager().getGroups())
        {
            if (inheritances.contains(g.getName()))
            {
                List<String> gperms = g.getEffectivePerms(server, world);
                ret.addAll(gperms);
            }
        }

        ret.addAll(perms);

        //per server perms
        Server srv = getServer(server);
        if (srv != null)
        {
            List<String> perserverperms = srv.getPerms();
            ret.addAll(perserverperms);

            World w = srv.getWorld(world);
            if (w != null)
            {
                List<String> serverworldperms = w.getPerms();
                ret.addAll(serverworldperms);
            }
        }

        ret = BungeePerms.getInstance().getPermissionsResolver().simplify(ret);

        return ret;
    }

    public boolean hasPerm(String perm, String server, String world)
    {
        perm = Statics.toLower(perm);
        server = Statics.toLower(server);
        world = Statics.toLower(world);

        List<String> perms = getEffectivePerms(server, world);

        Boolean has = BungeePerms.getInstance().getPermissionsResolver().has(perms, perm);

        return has != null && has;
    }

    public void recalcPerms(String server, String world)
    {
        server = Statics.toLower(server);
        world = Statics.toLower(world);

        deletePermCache(server, world);

        //call event
        BungeePerms.getInstance().getEventDispatcher().dispatchGroupChangeEvent(this);
    }

    public List<BPPermission> getPermsWithOrigin(String server, String world)
    {
        List<BPPermission> ret = new ArrayList<>();

        //add inherited groups' perms
        for (Group g : BungeePerms.getInstance().getPermissionsManager().getGroups())
        {
            if (inheritances.contains(g.getName()))
            {
                List<BPPermission> inheritgroupperms = g.getPermsWithOrigin(server, world);
                for (BPPermission perm : inheritgroupperms)
                {
                    ret.add(perm);
                }
            }
        }

        for (String s : perms)
        {
            BPPermission perm = new BPPermission(s, name, true, null, null);
            ret.add(perm);
        }

        //per server perms
        for (Map.Entry<String, Server> srv : servers.entrySet())
        {
            //check for server
            if (server == null || !srv.getKey().equalsIgnoreCase(server))
            {
                continue;
            }

            List<String> perserverperms = srv.getValue().getPerms();
            for (String s : perserverperms)
            {
                BPPermission perm = new BPPermission(s, name, true, srv.getKey(), null);
                ret.add(perm);
            }

            //per server world perms
            for (Map.Entry<String, World> w : srv.getValue().getWorlds().entrySet())
            {
                //check for world
                if (world == null || !w.getKey().equalsIgnoreCase(world))
                {
                    continue;
                }

                List<String> perserverworldperms = w.getValue().getPerms();
                for (String s : perserverworldperms)
                {
                    BPPermission perm = new BPPermission(s, name, true, srv.getKey(), w.getKey());
                    ret.add(perm);
                }
            }
        }

        return ret;
    }

    public int getOwnPermissionsCount()
    {
        int count = perms.size();

        for (Server s : servers.values())
        {
            count += s.getPerms().size();
            for (World w : s.getWorlds().values())
            {
                count += w.getPerms().size();
            }
        }

        return count;
    }

    public int getPermissionsCount()
    {
        int count = getOwnPermissionsCount();

        for (String group : inheritances)
        {
            Group g = BungeePerms.getInstance().getPermissionsManager().getGroup(group);
            if (g == null)
            {
                continue;
            }
            count += g.getOwnPermissionsCount();
        }

        return count;
    }

    public String buildPrefix(String server, String world)
    {
        String prefix = "";

        //global
        prefix += Statics.formatDisplay(this.prefix);

        //server
        Server s = getServer(server);
        if (s != null)
        {
            prefix += Statics.formatDisplay(s.getPrefix());

            //world
            World w = s.getWorld(world);
            if (w != null)
            {
                prefix += Statics.formatDisplay(w.getPrefix());
            }
        }

        return prefix.isEmpty() ? prefix : prefix.substring(0, prefix.length() - 1) + ChatColor.RESET;
    }

    public String buildSuffix(String server, String world)
    {
        String suffix = "";

        //global
        suffix += Statics.formatDisplay(this.suffix);

        //server
        Server s = getServer(server);
        if (s != null)
        {
            suffix += Statics.formatDisplay(s.getSuffix());

            //world
            World w = s.getWorld(world);
            if (w != null)
            {
                suffix += Statics.formatDisplay(w.getSuffix());
            }
        }

        return suffix.isEmpty() ? suffix : suffix.substring(0, suffix.length() - 1) + ChatColor.RESET;
    }

    //perm cache mgmt
    private List<String> getCachedPerms(String server, String world)
    {
        return getPermCacheList(server, world);
    }

    private void setCachedPerms(List<String> perms, String server, String world)
    {
        getPermCacheList(server, world).clear();
        getPermCacheList(server, world).addAll(perms);
    }

    private List<String> getPermCacheList(String server, String world)
    {
        //get server
        Map<String, List<String>> worldmap = cachedPerms.get(server);
        if (worldmap == null)
        {
            worldmap = new HashMap();
            cachedPerms.put(server, worldmap);
        }

        //get world
        List<String> permmap = worldmap.get(world);
        if (permmap == null)
        {
            permmap = new ArrayList();
            worldmap.put(world, permmap);
        }

        return permmap;
    }

    private void deletePermCache(String server, String world)
    {
        if (server == null)
        {
            cachedPerms.clear();
            return;
        }

        //get server
        Map<String, List<String>> worldmap = cachedPerms.get(server);
        if (worldmap == null)
        {
            return;
        }

        if (world == null)
        {
            worldmap.clear();
            return;
        }

        //get world
        worldmap.remove(world);
    }

    private boolean existsPermCacheList(String server, String world)
    {
        return cachedPerms.containsKey(server) && cachedPerms.get(server).containsKey(world);
    }

    @Override
    public int compareTo(Group g)
    {
        return -Integer.compare(rank, g.getRank());
    }
}
