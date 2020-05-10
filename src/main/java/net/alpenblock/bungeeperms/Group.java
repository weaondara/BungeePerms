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
package net.alpenblock.bungeeperms;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    public static final Comparator<Group> RANK_COMPARATOR = new Comparator<Group>()
    {
        @Override
        public int compare(Group g1, Group g2)
        {
            return -Integer.compare(g1.getRank(), g2.getRank());
        }
    };
    public static final Comparator<Group> WEIGHT_COMPARATOR = new Comparator<Group>()
    {
        @Override
        public int compare(Group g1, Group g2)
        {
            return -Integer.compare(g1.getWeight(), g2.getWeight());
        }
    };

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
            s = new Server(name, new ArrayList(), new ArrayList(), new ArrayList(), new ArrayList(), new HashMap(), null, null, null);
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

    @Override
    @Deprecated
    public List<String> getGroupsString()
    {
        return getInheritancesString();
    }

    @Override
    @Deprecated
    public List<TimedValue<String>> getTimedGroupsString()
    {
        return getTimedInheritancesString();
    }

    @Override
    @Deprecated
    public void setGroups(List<String> groups)
    {
        setInheritances(groups);
    }

    @Override
    @Deprecated
    public void setTimedGroups(List<TimedValue<String>> groups)
    {
        setTimedInheritances(groups);
    }

    @Deprecated
    public boolean has(String perm)
    {
        return has(perm, null, null);
    }

    @Deprecated
    public boolean hasOnServer(String perm, String server)
    {
        return has(perm, server, null);
    }

    @Deprecated
    public boolean hasOnServerInWorld(String perm, String server, String world)
    {
        return has(perm, server, world);
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

        LinkedHashSet<BPPermission> ret = new LinkedHashSet<>();

        //inheritances
        List<Group> inherit = new ArrayList(getInheritances());
        inherit.sort(new Comparator<Group>()
        {
            @Override
            public int compare(Group o1, Group o2)
            {
                return -Integer.compare(o1.getWeight(), o2.getWeight());
            }
        });
        for (Group g : inherit)
        {
            List<BPPermission> gperms = g.getEffectivePerms(server, world);
            ret.addAll(gperms);
        }

        //timed inheritances
        List<TimedValue<Group>> tinherit = new ArrayList(getTimedInheritances());
        tinherit.sort(new Comparator<TimedValue<Group>>()
        {
            @Override
            public int compare(TimedValue<Group> o1, TimedValue<Group> o2)
            {
                return -Integer.compare(o1.getValue().getWeight(), o2.getValue().getWeight());
            }
        });
        for (TimedValue<Group> tg : tinherit)
        {
            List<BPPermission> gperms = tg.getValue().getEffectivePerms(server, world);
            for (int i = 0; i < gperms.size(); i++)
            {
                BPPermission p = gperms.get(i).clone();
                if (p.getTimedStart() == null || tg.getStart().getTime() + tg.getDuration() > p.getTimedStart().getTime() + p.getTimedDuration())
                {
                    p.setTimedStart(tg.getStart());
                    p.setTimedDuration(tg.getDuration());
                }
                gperms.set(i, p);
            }
            ret.addAll(gperms);
        }

        //perms
        ret.addAll(Statics.makeBPPerms(perms, null, null, this));
        ret.addAll(Statics.makeBPPermsTimed(timedPerms, null, null, this));

        //per server perms
        Server srv = servers.get(server);
        if (srv != null)
        {
            //inheritances
            inherit = new ArrayList(srv.getGroups());
            inherit.sort(new Comparator<Group>()
            {
                @Override
                public int compare(Group o1, Group o2)
                {
                    return -Integer.compare(o1.getWeight(), o2.getWeight());
                }
            });
            for (Group g : inherit)
            {
                List<BPPermission> gperms = g.getEffectivePerms(server, world);
                ret.addAll(gperms);
            }

            //timed inheritances
            tinherit = new ArrayList(srv.getTimedGroups());
            tinherit.sort(new Comparator<TimedValue<Group>>()
            {
                @Override
                public int compare(TimedValue<Group> o1, TimedValue<Group> o2)
                {
                    return -Integer.compare(o1.getValue().getWeight(), o2.getValue().getWeight());
                }
            });
            for (TimedValue<Group> tg : tinherit)
            {
                List<BPPermission> gperms = tg.getValue().getEffectivePerms(server, world);
                for (int i = 0; i < gperms.size(); i++)
                {
                    BPPermission p = gperms.get(i).clone();
                    if (p.getTimedStart() == null || tg.getStart().getTime() + tg.getDuration() > p.getTimedStart().getTime() + p.getTimedDuration())
                    {
                        p.setTimedStart(tg.getStart());
                        p.setTimedDuration(tg.getDuration());
                    }
                    gperms.set(i, p);
                }
                ret.addAll(gperms);
            }

            ret.addAll(Statics.makeBPPerms(srv.getPerms(), server, null, this));
            ret.addAll(Statics.makeBPPermsTimed(srv.getTimedPerms(), server, null, this));

            World w = srv.getWorld(world);
            if (w != null)
            {
                //inheritances
                inherit = new ArrayList(w.getGroups());
                inherit.sort(new Comparator<Group>()
                {
                    @Override
                    public int compare(Group o1, Group o2)
                    {
                        return -Integer.compare(o1.getWeight(), o2.getWeight());
                    }
                });
                for (Group g : inherit)
                {
                    List<BPPermission> gperms = g.getEffectivePerms(server, world);
                    ret.addAll(gperms);
                }

                //timed inheritances
                tinherit = new ArrayList(w.getTimedGroups());
                tinherit.sort(new Comparator<TimedValue<Group>>()
                {
                    @Override
                    public int compare(TimedValue<Group> o1, TimedValue<Group> o2)
                    {
                        return -Integer.compare(o1.getValue().getWeight(), o2.getValue().getWeight());
                    }
                });
                for (TimedValue<Group> tg : tinherit)
                {
                    List<BPPermission> gperms = tg.getValue().getEffectivePerms(server, world);
                    for (int i = 0; i < gperms.size(); i++)
                    {
                        BPPermission p = gperms.get(i).clone();
                        if (p.getTimedStart() == null || tg.getStart().getTime() + tg.getDuration() > p.getTimedStart().getTime() + p.getTimedDuration())
                        {
                            p.setTimedStart(tg.getStart());
                            p.setTimedDuration(tg.getDuration());
                        }
                        gperms.set(i, p);
                    }
                    ret.addAll(gperms);
                }

                ret.addAll(Statics.makeBPPerms(w.getPerms(), server, world, this));
                ret.addAll(Statics.makeBPPermsTimed(w.getTimedPerms(), server, world, this));
            }
        }

        return new ArrayList(ret);
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

        for (Group g : getInheritances())
            count += g.getPermissionsCount(server, world);
        for (TimedValue<Group> g : getTimedInheritances())
            count += g.getValue().getPermissionsCount(server, world);

        Server s = getServer(server);
        if (s == null)
            return count;

        for (Group g : s.getGroups())
            count += g.getPermissionsCount(server, world);
        for (TimedValue<Group> g : s.getTimedGroups())
            count += g.getValue().getPermissionsCount(server, world);

        World w = s.getWorld(world);
        if (world == null)
            return count;

        for (Group g : w.getGroups())
            count += g.getPermissionsCount(server, world);
        for (TimedValue<Group> g : w.getTimedGroups())
            count += g.getValue().getPermissionsCount(server, world);

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
                prefix += Statics.formatDisplay(w.getPrefix());
        }

        return Statics.isEmpty(prefix) ? "" : prefix.substring(0, prefix.length() - 1) + ChatColor.RESET;
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
                suffix += Statics.formatDisplay(w.getSuffix());
        }

        return Statics.isEmpty(suffix) ? "" : suffix.substring(0, suffix.length() - 1) + ChatColor.RESET;
    }

    public String buildDisplay(String server, String world)
    {
        String display = "";

        //global
        display += Statics.formatDisplay(this.display);

        //server
        Server s = getServer(server);
        if (s != null)
        {
            display += Statics.formatDisplay(s.getDisplay());

            //world
            World w = s.getWorld(world);
            if (w != null)
                display += Statics.formatDisplay(w.getDisplay());
        }

        return Statics.isEmpty(display) ? "" : display.substring(0, display.length() - 1) + ChatColor.RESET;
    }

    Long getNextTimedEntry(Set<Group> checkedgroups)
    {
        if (checkedgroups.contains(this))
            return null;
        checkedgroups.add(this);

        boolean dosave = false;

        Long next = null;

        //timed inheritances
        List<SimpleEntry<SimpleEntry<String, String>, List<TimedValue<String>>>> ll = new ArrayList();
        List<SimpleEntry<String, String>> tosaveinherit = new ArrayList();
        ll.add(new SimpleEntry<>(new SimpleEntry<String, String>(null, null), timedInheritances));
        for (Server s : servers.values())
        {
            ll.add(new SimpleEntry<>(new SimpleEntry<String, String>(s.getServer(), null), s.getTimedGroupsString()));
            for (World w : s.getWorlds().values())
                ll.add(new SimpleEntry<>(new SimpleEntry<String, String>(s.getServer(), w.getWorld()), w.getTimedGroupsString()));
        }

        for (SimpleEntry<SimpleEntry<String, String>, List<TimedValue<String>>> l : ll)
        {
            for (int i = 0; i < l.getValue().size(); i++)
            {
                TimedValue<String> g = l.getValue().get(i);
                long end = g.getStart().getTime() + g.getDuration() * 1000;
                if (end < System.currentTimeMillis())
                {
                    //remove timed inheritance
                    BungeePerms.getInstance().getDebug().log("removing timed inheritance " + g.getValue() + " from group " + name + " (" + l.getKey().getKey() + "," + l.getKey().getValue() + ")");
                    l.getValue().remove(g);
                    i--;
                    dosave = true;
                    tosaveinherit.add(l.getKey());
                }
                else
                {
                    next = next == null ? end : Math.min(next, end);
                }
            }
        }

        //timed perms
        ll = new ArrayList();
        List<SimpleEntry<String, String>> tosaveperms = new ArrayList();
        ll.add(new SimpleEntry<>(new SimpleEntry<String, String>(null, null), timedPerms));
        for (Server s : servers.values())
        {
            ll.add(new SimpleEntry<>(new SimpleEntry<String, String>(s.getServer(), null), s.getTimedPerms()));
            for (World w : s.getWorlds().values())
                ll.add(new SimpleEntry<>(new SimpleEntry<String, String>(s.getServer(), w.getWorld()), w.getTimedPerms()));
        }

        for (SimpleEntry<SimpleEntry<String, String>, List<TimedValue<String>>> l : ll)
        {
            for (int i = 0; i < l.getValue().size(); i++)
            {
                TimedValue<String> p = l.getValue().get(i);
                long end = p.getStart().getTime() + p.getDuration() * 1000;
                if (end < System.currentTimeMillis())
                {
                    //remove timed perm
                    BungeePerms.getInstance().getDebug().log("removing timed permission " + p.getValue() + " from group " + name + " (" + l.getKey().getKey() + "," + l.getKey().getValue() + ")");
                    l.getValue().remove(p);
                    i--;
                    dosave = true;
                    tosaveperms.add(l.getKey());
                }
                else
                {
                    next = next == null ? end : Math.min(next, end);
                }
            }
        }

        if (dosave)
        {
            //save
            for (SimpleEntry<String, String> sw : tosaveinherit)
            {
                BungeePerms.getInstance().getDebug().log("saving timed inherits for group " + name + " (" + sw.getKey() + "," + sw.getValue() + ")");
                BungeePerms.getInstance().getPermissionsManager().getBackEnd().saveGroupTimedInheritances(this, sw.getKey(), sw.getValue());
            }
            for (SimpleEntry<String, String> sw : tosaveperms)
            {
                BungeePerms.getInstance().getDebug().log("saving timed permissions for group " + name + " (" + sw.getKey() + "," + sw.getValue() + ")");
                BungeePerms.getInstance().getPermissionsManager().getBackEnd().saveGroupTimedPerms(this, sw.getKey(), sw.getValue());
            }

            //invalidate everything
            for (Group g : BungeePerms.getInstance().getPermissionsManager().getGroups())
                g.invalidateCache();
            for (User u : BungeePerms.getInstance().getPermissionsManager().getUsers())
                u.invalidateCache();
//            BungeePerms.getInstance().getNetworkNotifier().reloadGroup(this, null);
            BungeePerms.getInstance().getEventDispatcher().dispatchGroupChangeEvent(this);
        }

        //inheritances recursive
        List<String> allgroups = new ArrayList();
        allgroups.addAll(inheritances);
        for (TimedValue<String> tgroup : timedInheritances)
            allgroups.add(tgroup.getValue());
        for (Server srv : servers.values())
        {
            allgroups.addAll(srv.getGroupsString());
            for (TimedValue<String> tgroup : srv.getTimedGroupsString())
                allgroups.add(tgroup.getValue());
            for (World w : srv.getWorlds().values())
            {
                allgroups.addAll(w.getGroupsString());
                for (TimedValue<String> tgroup : w.getTimedGroupsString())
                    allgroups.add(tgroup.getValue());
            }
        }
        for (String s : allgroups)
        {
            Group g = BungeePerms.getInstance().getPermissionsManager().getGroup(s);
            if (g == null)
                continue;
            Long end = g.getNextTimedEntry(checkedgroups);
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
