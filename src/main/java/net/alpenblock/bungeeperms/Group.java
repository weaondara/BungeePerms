package net.alpenblock.bungeeperms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class Group implements Comparable<Group>, PermEntity
{

    @Getter(value = AccessLevel.PRIVATE)
    @Setter(value = AccessLevel.PRIVATE)
    private final Map<String, Map<String, List<BPPermission>>> cachedPerms = new HashMap<>();

    private String name;
    private List<String> inheritances;
    private List<TimedValue<String>> timedInheritances;
    private List<String> perms;
    private List<TimedValue<String>> timedPerms;
    private Map<String, Server> servers;
    private int rank;
    private int weight;
    private String ladder;
    private boolean isdefault;
    private String display;
    private String prefix;
    private String suffix;

    public Group(String name, List<String> inheritances, List<TimedValue<String>> timedinheritances, List<String> perms, List<TimedValue<String>> timedperms, Map<String, Server> servers, int rank, int weight, String ladder, boolean isdefault, String display, String prefix, String suffix)
    {
        this.name = name;
        this.timedInheritances = timedinheritances;
        this.inheritances = inheritances;
        this.perms = perms;
        this.timedPerms = timedperms;
        this.servers = servers;
        this.rank = rank;
        this.weight = weight;
        this.ladder = ladder;
        this.isdefault = isdefault;
        this.display = display;
        this.prefix = prefix;
        this.suffix = suffix;
    }

    @Override
    public Server getServer(String name)
    {
        if (name == null)
            return null;

        name = Statics.toLower(name);
        Server s = servers.get(name);
        if (s == null)
        {
            s = new Server(name, new ArrayList(), new ArrayList(), new HashMap(), null, null, null);
            servers.put(name, s);
        }

        return s;
    }

    @Override
    public boolean hasTimedPermSet(String perm)
    {
        perm = Statics.toLower(perm);

        for (TimedValue<String> t : timedPerms)
            if (t.getValue().equalsIgnoreCase(perm))
                return true;
        return false;
    }

    public boolean isDefault()
    {
        return isdefault;
    }

    public void setIsdefault(boolean isdefault)
    {
        this.isdefault = isdefault;
    }

    public List<Group> getInheritances()
    {
        List<Group> ret = new ArrayList<>();
        for (String name : inheritances)
        {
            Group g = BungeePerms.getInstance().getPermissionsManager().getGroup(name);
            if (g != null)
                ret.add(g);
        }

        return ret;
    }

    public List<String> getInheritancesString()
    {
        return inheritances;
    }

    public List<TimedValue<Group>> getTimedInheritances()
    {
        List<TimedValue<Group>> ret = new ArrayList<>();
        for (TimedValue<String> name : timedInheritances)
        {
            Group g = BungeePerms.getInstance().getPermissionsManager().getGroup(name.getValue());
            if (g != null)
                ret.add(new TimedValue(g, name.getStart(), name.getDuration()));
        }

        return ret;
    }

    public List<TimedValue<String>> getTimedInheritancesString()
    {
        return timedInheritances;
    }

    public boolean has(String perm, String server, String world)
    {
        List<BPPermission> perms = getEffectivePerms(server, world);

        Boolean has = BungeePerms.getInstance().getPermissionsResolver().hasPerm(perms, perm);

        return has != null && has;
    }

    public List<BPPermission> getEffectivePerms(String server, String world)
    {
        server = Statics.toLower(server);
        world = server == null ? null : Statics.toLower(world);

        Map<String, List<BPPermission>> worldmap = cachedPerms.get(server);
        if (worldmap == null)
            cachedPerms.put(server, worldmap = new HashMap());

        List<BPPermission> effperms = worldmap.get(world);
        if (effperms == null)
        {
            effperms = calcEffectivePerms(server, world);
            worldmap.put(world, effperms);
        }

        return new ArrayList<>(effperms);
    }

    public List<BPPermission> calcEffectivePerms(String server, String world)
    {
        server = Statics.toLower(server);
        world = server == null ? null : Statics.toLower(world);

        List<BPPermission> ret = new ArrayList<>();
        for (String s : inheritances)
        {
            Group g = BungeePerms.getInstance().getPermissionsManager().getGroup(s);
            if (g == null)
                continue;
            List<BPPermission> gperms = g.getEffectivePerms(server, world);
            ret.addAll(gperms);
        }
        for (TimedValue<String> tg : timedInheritances)
        {
            Group g = BungeePerms.getInstance().getPermissionsManager().getGroup(tg.getValue());
            if (g == null)
                continue;
            List<BPPermission> gperms = g.getEffectivePerms(server, world);
            ret.addAll(gperms);
        }

        ret.addAll(Statics.makeBPPerms(perms, null, null, this));
        ret.addAll(Statics.makeBPPermsTimed(timedPerms, null, null, this));

        //per server perms
        Server srv = servers.get(server);
        if (srv != null)
        {
            ret.addAll(Statics.makeBPPerms(srv.getPerms(), server, null, this));
            ret.addAll(Statics.makeBPPermsTimed(srv.getTimedPerms(), server, null, this));

            World w = srv.getWorld(world);
            if (w != null)
            {
                ret.addAll(Statics.makeBPPerms(w.getPerms(), server, world, this));
                ret.addAll(Statics.makeBPPermsTimed(w.getTimedPerms(), server, world, this));
            }
        }

        ret = BungeePerms.getInstance().getPermissionsResolver().simplify(ret);

        return ret;
    }

    public void invalidateCache()
    {
        BungeePerms.getInstance().getDebug().log("invalidate cache for group " + name);
        flushCache();
    }

    private void flushCache()
    {
        cachedPerms.clear();
    }

    public List<BPPermission> getPermsWithOrigin(String server, String world)
    {
        List<BPPermission> ret = getEffectivePerms(server, world);

        //pre process
//        ret = BungeePerms.getInstance().getPermissionsResolver().preprocess(ret, getSender());
        return ret;
    }

    public int getOwnPermissionsCount(String server, String world)
    {
        int count = perms.size();
        count += timedPerms.size();

        Server s = getServer(server);
        if (s == null)
            return count;

        count += s.getPerms().size();
        count += s.getTimedPerms().size();

        World w = s.getWorld(world);
        if (world == null)
            return count;

        count += w.getPerms().size();
        count += w.getTimedPerms().size();

        return count;
    }

    public int getPermissionsCount(String server, String world)
    {
        int count = getOwnPermissionsCount(server, world);

        for (String group : inheritances)
        {
            Group g = BungeePerms.getInstance().getPermissionsManager().getGroup(group);
            if (g == null)
                continue;
            count += g.getPermissionsCount(server, world);
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

    Long getNextTimedPermission()
    {
        boolean dosavegroups = false;
        boolean dosaveperms = false;

        Long next = null;

        //timed inheritances
        for (int i = 0; i < timedInheritances.size(); i++)
        {
            TimedValue g = timedInheritances.get(i);
            long end = g.getStart().getTime() + g.getDuration() * 1000;
            if (end < System.currentTimeMillis())
            {
                timedInheritances.remove(g);
                i--;
                dosavegroups = true;
            }
            else
            {
                next = next == null ? end : Math.min(next, end);
            }
        }
        if (dosavegroups)
            BungeePerms.getInstance().getPermissionsManager().getBackEnd().saveGroupTimedInheritances(this);

        //timed perms
        List<List<TimedValue<String>>> ll = new ArrayList();
        ll.add(timedPerms);
        for (Server s : servers.values())
        {
            ll.add(s.getTimedPerms());
            for (World w : s.getWorlds().values())
                ll.add(w.getTimedPerms());
        }

        for (List<TimedValue<String>> l : ll)
        {
            for (int i = 0; i < l.size(); i++)
            {
                TimedValue p = l.get(i);
                long end = p.getStart().getTime() + p.getDuration() * 1000;
                if (end < System.currentTimeMillis())
                {
                    //remove timed perm
                    l.remove(p);
                    i--;
                    dosaveperms = true;
                }
                else
                {
                    next = next == null ? end : Math.min(next, end);
                }
            }
        }
        if (dosaveperms)
            BungeePerms.getInstance().getPermissionsManager().getBackEnd().saveGroup(this, true);

        if (dosavegroups || dosaveperms)
        {
            flushCache();
            BungeePerms.getInstance().getNetworkNotifier().reloadGroup(this, null);
            BungeePerms.getInstance().getEventDispatcher().dispatchGroupChangeEvent(this);
        }

        //inheritances
        for (String s : inheritances)
        {
            Group g = BungeePerms.getInstance().getPermissionsManager().getGroup(s);
            if (g == null)
                continue;
            Long end = g.getNextTimedPermission();
            if (end == null)
                continue;
            next = next == null ? end : Math.min(next, end);
        }
        for (TimedValue<String> s : timedInheritances)
        {
            Group g = BungeePerms.getInstance().getPermissionsManager().getGroup(s.getValue());
            if (g == null)
                continue;
            Long end = g.getNextTimedPermission();
            if (end == null)
                continue;
            next = next == null ? end : Math.min(next, end);
        }
        BungeePerms.getInstance().getDebug().log("getNextTimedPermission " + name + ": " + next);
        return next;
    }

    @Override
    public int compareTo(Group g)
    {
        return -Integer.compare(rank, g.getRank());
    }
}
