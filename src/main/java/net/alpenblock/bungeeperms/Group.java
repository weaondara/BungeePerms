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
    private Map<String, List<String>> cachedPerms;

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

    public void setIsdefault(boolean isdefault)
    {
        this.isdefault = isdefault;
    }

    public List<String> getEffectivePerms()
    {
        List<String> effperms = cachedPerms.get("global");
        if (effperms == null)
        {
            effperms = calcEffectivePerms();
            cachedPerms.put("global", effperms);
        }

        return new ArrayList<>(effperms);
    }

    public List<String> getEffectivePerms(String server)
    {
        server = Statics.toLower(server);

        List<String> effperms = cachedPerms.get(server);
        if (effperms == null)
        {
            effperms = calcEffectivePerms(server);
            cachedPerms.put(server, effperms);
        }

        return new ArrayList<>(effperms);
    }

    public List<String> getEffectivePerms(String server, String world)
    {
        server = Statics.toLower(server);
        world = Statics.toLower(world);

        List<String> effperms = cachedPerms.get(server + ";" + world);
        if (effperms == null)
        {
            effperms = calcEffectivePerms(server, world);
            cachedPerms.put(server + ";" + world, effperms);
        }

        return new ArrayList<>(effperms);
    }

    public List<String> calcEffectivePerms()
    {
        List<String> ret = new ArrayList<>();
        for (Group g : BungeePerms.getInstance().getPermissionsManager().getGroups())
        {
            if (inheritances.contains(g.getName()))
            {
                List<String> gperms = g.getEffectivePerms();
                ret.addAll(gperms);
            }
        }
        ret.addAll(perms);

        ret = BungeePerms.getInstance().getPermissionsResolver().simplify(ret);

        return ret;
    }

    public List<String> calcEffectivePerms(String server)
    {
        server = Statics.toLower(server);

        List<String> ret = new ArrayList<>();
        for (Group g : BungeePerms.getInstance().getPermissionsManager().getGroups())
        {
            if (inheritances.contains(g.getName()))
            {
                List<String> gperms = g.getEffectivePerms(server);
                ret.addAll(gperms);
            }
        }

        ret.addAll(perms);

        //per server perms
        Server srv = servers.get(server);
        if (srv != null)
        {
            List<String> perserverperms = srv.getPerms();
            ret.addAll(perserverperms);
        }

        ret = BungeePerms.getInstance().getPermissionsResolver().simplify(ret);

        return ret;
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
        Server srv = servers.get(server);
        if (srv != null)
        {
            List<String> perserverperms = srv.getPerms();
            ret.addAll(perserverperms);

            World w = srv.getWorld(world);
            List<String> serverworldperms = w.getPerms();
            ret.addAll(serverworldperms);
        }

        ret = BungeePerms.getInstance().getPermissionsResolver().simplify(ret);

        return ret;
    }

    public boolean has(String perm)
    {
        perm = Statics.toLower(perm);

        List<String> perms = getEffectivePerms();

        Boolean has = BungeePerms.getInstance().getPermissionsResolver().has(perms, Statics.toLower(perm));

        return has != null && has;
    }

    public boolean hasOnServer(String perm, String server)
    {
        perm = Statics.toLower(perm);
        server = Statics.toLower(server);

        List<String> perms = getEffectivePerms(server);

        Boolean has = BungeePerms.getInstance().getPermissionsResolver().has(perms, perm);

        return has != null && has;
    }

    public boolean hasOnServerInWorld(String perm, String server, String world)
    {
        perm = Statics.toLower(perm);
        server = Statics.toLower(server);
        world = Statics.toLower(world);

        List<String> perms = getEffectivePerms(server, world);

        Boolean has = BungeePerms.getInstance().getPermissionsResolver().has(perms, perm);

        return has != null && has;
    }

    public void recalcPerms()
    {
        recalcPerms0();

        //call event
        BungeePerms.getInstance().getEventDispatcher().dispatchGroupChangeEvent(this);
    }

    public void recalcPerms(String server)
    {
        recalcPerms0(server);

        //call event
        BungeePerms.getInstance().getEventDispatcher().dispatchGroupChangeEvent(this);
    }

    public void recalcPerms(String server, String world)
    {
        recalcPerms0(server, world);

        //call event
        BungeePerms.getInstance().getEventDispatcher().dispatchGroupChangeEvent(this);
    }

    public void recalcPerms0()
    {
        for (Map.Entry<String, List<String>> e : cachedPerms.entrySet())
        {
            String where = e.getKey();
            List<String> l = Statics.toList(where, ";");
            String server = Statics.toLower(l.get(0));

            if (l.size() == 1)
            {
                if (server.equalsIgnoreCase("global"))
                {
                    cachedPerms.put("global", calcEffectivePerms());
                }
                else
                {
                    List<String> effperms = calcEffectivePerms(server);
                    cachedPerms.put(server, effperms);
                }
            }
            else if (l.size() == 2)
            {
                String world = Statics.toLower(l.get(1));
                recalcPerms0(server, world);
            }
        }
    }

    public void recalcPerms0(String server)
    {
        for (Map.Entry<String, List<String>> e : cachedPerms.entrySet())
        {
            String where = e.getKey();
            List<String> l = Statics.toList(where, ";");
            String lserver = Statics.toLower(l.get(0));

            if (lserver.equalsIgnoreCase(server))
            {
                if (l.size() == 1)
                {
                    List<String> effperms = calcEffectivePerms(lserver);
                    cachedPerms.put(lserver, effperms);
                }
                else if (l.size() == 2)
                {
                    String world = Statics.toLower(l.get(1));
                    recalcPerms0(lserver, world);
                }
            }
        }
    }

    public void recalcPerms0(String server, String world)
    {
        server = Statics.toLower(server);
        world = Statics.toLower(world);

        List<String> effperms = calcEffectivePerms(server, world);
        cachedPerms.put(server + ";" + world, effperms);
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

    public int getOwnPermissionsCount(String server, String world)
    {
        int count = perms.size();

        Server s = getServer(server);
        if (s == null)
        {
            return count;
        }
        count += s.getPerms().size();

        World w = s.getWorld(world);
        if (world == null)
        {
            return count;
        }
        count += w.getPerms().size();

        return count;
    }

    public int getPermissionsCount(String server, String world)
    {
        int count = getOwnPermissionsCount(server, world);

        for (String group : inheritances)
        {
            Group g = BungeePerms.getInstance().getPermissionsManager().getGroup(group);
            if (g == null)
            {
                continue;
            }
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

    @Override
    public int compareTo(Group g)
    {
        return -Integer.compare(rank, g.getRank());
    }
}
