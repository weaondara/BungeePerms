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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.alpenblock.bungeeperms.platform.Sender;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class User implements PermEntity
{

    @Getter(value = AccessLevel.PRIVATE)
    @Setter(value = AccessLevel.PRIVATE)
    private Map<String, Map<String, List<BPPermission>>> cachedPerms;
    @Getter(value = AccessLevel.PRIVATE)
    @Setter(value = AccessLevel.PRIVATE)
    private Map<String, Map<String, Map<String, Boolean>>> permCheckResults;

    private String name;
    private UUID UUID;
    private List<String> groups;
    private List<TimedValue<String>> timedGroups;
    private List<String> perms;
    private List<TimedValue<String>> timedPerms;
    private Map<String, Server> servers;

    private String display;
    private String prefix;
    private String suffix;

    private long lastAccess;
    private Long nextTimedEntryRunOut;

    public User(String name, UUID UUID, List<String> groups, List<TimedValue<String>> timedgroups, List<String> perms, List<TimedValue<String>> timedperms, Map<String, Server> servers, String display, String prefix, String suffix)
    {
        cachedPerms = new HashMap<>();
        permCheckResults = new HashMap<>();

        this.name = name;
        this.UUID = UUID;
        this.groups = groups;
        this.timedGroups = timedgroups;
        this.perms = perms;
        this.timedPerms = timedperms;
        this.servers = servers;

        this.display = display;
        this.prefix = prefix;
        this.suffix = suffix;

        access();
    }

    @Override
    public Server getServer(String name)
    {
        access();
        checkTimedEntries();

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
        access();
        checkTimedEntries();

        perm = Statics.toLower(perm);

        for (TimedValue<String> t : timedPerms)
            if (t.getValue().equalsIgnoreCase(perm))
                return true;
        return false;
    }

    public List<Group> getGroups()
    {
        access();

        List<Group> ret = new ArrayList<>();
        for (String name : groups)
        {
            Group g = BungeePerms.getInstance().getPermissionsManager().getGroup(name);
            if (g != null)
                ret.add(g);
        }

        return ret;
    }

    @Override
    public List<String> getGroupsString()
    {
        access();

        return groups;
    }

    public List<TimedValue<Group>> getTimedGroups()
    {
        access();
        checkTimedEntries();

        List<TimedValue<Group>> ret = new ArrayList<>();
        for (TimedValue<String> name : timedGroups)
        {
            Group g = BungeePerms.getInstance().getPermissionsManager().getGroup(name.getValue());
            if (g != null)
                ret.add(new TimedValue<Group>(g, name.getStart(), name.getDuration()));
        }

        return ret;
    }

    @Override
    public List<TimedValue<String>> getTimedGroupsString()
    {
        access();
        checkTimedEntries();

        return timedGroups;
    }

    @Deprecated
    public boolean hasPerm(String perm)
    {
        return hasPerm(perm, null, null);
    }

    @Deprecated
    public boolean hasPermOnServer(String perm, String server)
    {
        return hasPerm(perm, server, null);
    }

    @Deprecated
    public boolean hasPermOnServerInWorld(String perm, String server, String world)
    {
        return hasPerm(perm, server, null);
    }

    public boolean hasPerm(String perm, String server, String world)
    {
        access();
        checkTimedEntries();

        Sender s = getSender();
        return hasPerm(s, perm, server, world);
    }

    public boolean hasPerm(Sender s, String perm, String server, String world)
    {
        access();
        checkTimedEntries();

        perm = Statics.toLower(perm);
        server = Statics.toLower(server);
        world = server == null ? null : Statics.toLower(world);

        //check cached perms
        Boolean cached = getCachedResult(perm, server, world);
        if (cached != null)
        {
            //debug mode
            debug("cache: " + perm, cached);
            return cached;
        }

        //check perms
        List<BPPermission> perms = getEffectivePerms(server, world);

        //pre process
        perms = BungeePerms.getInstance().getPermissionsResolver().preprocess(perms, s);

        //resolve
        Boolean has = BungeePerms.getInstance().getPermissionsResolver().hasPerm(perms, perm);

        //post process
        has = BungeePerms.getInstance().getPermissionsResolver().postprocess(perm, has, s);

        //only true if really true
        has = has != null && has;

        //cache
        setCachedResult(perm, has, server, world);

        //debug mode
        debug("resolve: " + perm, has);

        return has;
    }

    public List<BPPermission> getEffectivePerms(String server, String world)
    {
        access();
        checkTimedEntries();

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
        access();
        checkTimedEntries();

        server = Statics.toLower(server);
        world = server == null ? null : Statics.toLower(world);

        LinkedHashSet<BPPermission> ret = new LinkedHashSet<>();

        //groups
        List<Group> groups = getGroups();
        groups.sort(new Comparator<Group>()
        {
            @Override
            public int compare(Group o1, Group o2)
            {
                return -Integer.compare(o1.getWeight(), o2.getWeight());
            }
        });
        for (Group g : groups)
        {
            List<BPPermission> gperms = g.getEffectivePerms(server, world);
            ret.addAll(gperms);
        }

        //timed groups
        List<TimedValue<Group>> tgroups = getTimedGroups();
        tgroups.sort(new Comparator<TimedValue<Group>>()
        {
            @Override
            public int compare(TimedValue<Group> o1, TimedValue<Group> o2)
            {
                return -Integer.compare(o1.getValue().getWeight(), o2.getValue().getWeight());
            }
        });
        for (TimedValue<Group> tg : tgroups)
        {
            List<BPPermission> gperms = tg.getValue().getEffectivePerms(server, world);
            for (int i = 0; i < gperms.size(); i++)
            {
                BPPermission p = gperms.get(i).clone();
                if (p.getTimedStart() == null || tg.getStart().getTime() + (long)tg.getDuration() * 1000 > p.getTimedStart().getTime() + (long)p.getTimedDuration() * 1000)
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
        Server srv = getServer(server);
        if (srv != null)
        {
            //groups
            groups = srv.getGroups();
            groups.sort(new Comparator<Group>()
            {
                @Override
                public int compare(Group o1, Group o2)
                {
                    return -Integer.compare(o1.getWeight(), o2.getWeight());
                }
            });
            for (Group g : groups)
            {
                List<BPPermission> gperms = g.getEffectivePerms(server, world);
                ret.addAll(gperms);
            }

            //timed groups
            tgroups = srv.getTimedGroups();
            tgroups.sort(new Comparator<TimedValue<Group>>()
            {
                @Override
                public int compare(TimedValue<Group> o1, TimedValue<Group> o2)
                {
                    return -Integer.compare(o1.getValue().getWeight(), o2.getValue().getWeight());
                }
            });
            for (TimedValue<Group> tg : tgroups)
            {
                List<BPPermission> gperms = tg.getValue().getEffectivePerms(server, world);
                for (int i = 0; i < gperms.size(); i++)
                {
                    BPPermission p = gperms.get(i).clone();
                    if (p.getTimedStart() == null || tg.getStart().getTime() + (long)tg.getDuration() * 1000 > p.getTimedStart().getTime() + (long)p.getTimedDuration() * 1000)
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
                //groups
                groups = w.getGroups();
                groups.sort(new Comparator<Group>()
                {
                    @Override
                    public int compare(Group o1, Group o2)
                    {
                        return -Integer.compare(o1.getWeight(), o2.getWeight());
                    }
                });
                for (Group g : groups)
                {
                    List<BPPermission> gperms = g.getEffectivePerms(server, world);
                    ret.addAll(gperms);
                }

                //timed groups
                tgroups = w.getTimedGroups();
                tgroups.sort(new Comparator<TimedValue<Group>>()
                {
                    @Override
                    public int compare(TimedValue<Group> o1, TimedValue<Group> o2)
                    {
                        return -Integer.compare(o1.getValue().getWeight(), o2.getValue().getWeight());
                    }
                });
                for (TimedValue<Group> tg : tgroups)
                {
                    List<BPPermission> gperms = tg.getValue().getEffectivePerms(server, world);
                    for (int i = 0; i < gperms.size(); i++)
                    {
                        BPPermission p = gperms.get(i).clone();
                        if (p.getTimedStart() == null || tg.getStart().getTime() + (long)tg.getDuration() * 1000 > p.getTimedStart().getTime() + (long)p.getTimedDuration() * 1000)
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
        BungeePerms.getInstance().getDebug().log("invalidate cache for user " + name);
        flushCache();
        nextTimedEntryRunOut = getNextTimedEntry();
    }

    public boolean isNothingSpecial()
    {
        access();

        for (Group g : getGroups())
            if (!g.isDefault())
                return false;
        if (!timedGroups.isEmpty())
            return false;

        for (Server s : servers.values())
        {
            if (!s.getGroups().isEmpty()
                || !s.getTimedGroups().isEmpty()
                || !s.getPerms().isEmpty()
                || !s.getTimedPerms().isEmpty()
                || !Statics.isEmpty(s.getDisplay())
                || !Statics.isEmpty(s.getPrefix())
                || !Statics.isEmpty(s.getSuffix()))
                return false;
            for (World w : s.getWorlds().values())
                if (!w.getGroups().isEmpty()
                    || !w.getTimedGroups().isEmpty()
                    || !w.getPerms().isEmpty()
                    || w.getTimedPerms().isEmpty()
                    || !Statics.isEmpty(w.getDisplay())
                    || !Statics.isEmpty(w.getPrefix())
                    || !Statics.isEmpty(w.getSuffix()))
                    return false;
        }
        return perms.isEmpty() && timedPerms.isEmpty() && Statics.isEmpty(display) && Statics.isEmpty(prefix) && Statics.isEmpty(suffix);
    }

    public Group getGroupByLadder(String ladder)
    {
        access();

        for (Group g : getGroups())
            if (g.getLadder().equalsIgnoreCase(ladder))
                return g;
        return null;
    }

    public List<BPPermission> getPermsWithOrigin(String server, String world)
    {
        access();
        checkTimedEntries();

        List<BPPermission> ret = getEffectivePerms(server, world);

        //pre process
        ret = BungeePerms.getInstance().getPermissionsResolver().preprocess(ret, getSender());

        return ret;
    }

    public int getOwnPermissionsCount(String server, String world)
    {
        access();
        checkTimedEntries();

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
        access();
        checkTimedEntries();

        int count = getOwnPermissionsCount(server, world);

        for (Group g : getGroups())
            count += g.getPermissionsCount(server, world);
        for (TimedValue<Group> g : getTimedGroups())
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

    public String buildPrefix()
    {
        access();

        Sender sender = getSender();
        return buildPrefix(sender);
    }

    public String buildPrefix(Sender sender)
    {
        return buildPrefix(sender != null ? sender.getServer() : null, sender != null ? sender.getWorld() : null);
    }

    public String buildPrefix(String server, String world)
    {
        access();
        checkTimedEntries();

        String prefix = "";

        List<String> prefixes = new ArrayList<>();

        //from groups
        Set<Group> groupset = new LinkedHashSet<>(getGroups());
        for (TimedValue<Group> g : getTimedGroups())
            groupset.add(g.getValue());
        for (Server s : servers.values())
        {
            groupset.addAll(s.getGroups());
            for (TimedValue<Group> g : s.getTimedGroups())
                groupset.add(g.getValue());

            for (World w : s.getWorlds().values())
            {
                groupset.addAll(w.getGroups());
                for (TimedValue<Group> g : w.getTimedGroups())
                    groupset.add(g.getValue());
            }
        }
        List<Group> groups = new ArrayList(groupset);
        Collections.sort(groups, Group.WEIGHT_COMPARATOR);

        for (Group g : groups)
        {
            //global
            if (!Statics.isEmpty(g.getPrefix()))
                prefixes.add(g.getPrefix());

            //server
            Server s = g.getServer(server);
            if (s != null)
            {
                if (!Statics.isEmpty(s.getPrefix()))
                    prefixes.add(s.getPrefix());

                //world
                World w = s.getWorld(world);
                if (w != null)
                    if (!Statics.isEmpty(w.getPrefix()))
                        prefixes.add(w.getPrefix());
            }
        }

        //global
        if (!Statics.isEmpty(this.prefix))
            prefixes.add(this.prefix);

        //server
        Server s = getServer(server);
        if (s != null)
        {
            if (!Statics.isEmpty(s.getPrefix()))
                prefixes.add(s.getPrefix());

            //world
            World w = s.getWorld(world);
            if (w != null)
                if (!Statics.isEmpty(w.getPrefix()))
                    prefixes.add(w.getPrefix());
        }

        for (String p : prefixes)
        {
            if (!ChatColor.strip(p.replaceAll("&", ChatColor.COLOR_CHAR + "")).isEmpty() //current prefix empty
                && !prefix.isEmpty() //built prefix empty
                && !ChatColor.strip(prefix.replaceAll("&", ChatColor.COLOR_CHAR + "")).endsWith(" ")) //built prefix ends with space
                prefix += " ";
            prefix += p;
        }

        return prefix
               + (BungeePerms.getInstance().getConfig().isTerminatePrefixSpace() ? " " : "")
               + (BungeePerms.getInstance().getConfig().isTerminatePrefixReset() ? ChatColor.RESET : "");
    }

    public String buildSuffix()
    {
        access();

        Sender sender = getSender();
        return buildSuffix(sender);
    }

    public String buildSuffix(Sender sender)
    {
        return buildSuffix(sender != null ? sender.getServer() : null, sender != null ? sender.getWorld() : null);
    }

    public String buildSuffix(String server, String world)
    {
        access();
        checkTimedEntries();

        String suffix = "";

        List<String> suffixes = new ArrayList<>();

        //from groups
        Set<Group> groupset = new LinkedHashSet<>(getGroups());
        for (TimedValue<Group> g : getTimedGroups())
            groupset.add(g.getValue());
        for (Server s : servers.values())
        {
            groupset.addAll(s.getGroups());
            for (TimedValue<Group> g : s.getTimedGroups())
                groupset.add(g.getValue());

            for (World w : s.getWorlds().values())
            {
                groupset.addAll(w.getGroups());
                for (TimedValue<Group> g : w.getTimedGroups())
                    groupset.add(g.getValue());
            }
        }
        List<Group> groups = new ArrayList(groupset);
        Collections.sort(groups, Group.WEIGHT_COMPARATOR);

        for (Group g : groups)
        {
            //global
            if (!Statics.isEmpty(g.getSuffix()))
                suffixes.add(g.getSuffix());

            //server
            Server s = g.getServer(server);
            if (s != null)
            {
                if (!Statics.isEmpty(s.getSuffix()))
                    suffixes.add(s.getSuffix());

                //world
                World w = s.getWorld(world);
                if (w != null)
                    if (!Statics.isEmpty(w.getSuffix()))
                        suffixes.add(w.getSuffix());
            }
        }

        //global
        if (!Statics.isEmpty(this.suffix))
            suffixes.add(this.suffix);

        //server
        Server s = getServer(server);
        if (s != null)
        {
            if (!Statics.isEmpty(s.getSuffix()))
                suffixes.add(s.getSuffix());

            //world
            World w = s.getWorld(world);
            if (w != null)
                if (!Statics.isEmpty(w.getSuffix()))
                    suffixes.add(w.getSuffix());
        }

        for (String suf : suffixes)
        {
            if (!ChatColor.strip(suf.replaceAll("&", ChatColor.COLOR_CHAR + "")).isEmpty() //current suffix empty
                && !suffix.isEmpty() //built suffix empty
                && !ChatColor.strip(suffix.replaceAll("&", ChatColor.COLOR_CHAR + "")).endsWith(" ")) //built suffix ends with space
                suffix += " ";
            suffix += suf;
        }

        return suffix
               + (BungeePerms.getInstance().getConfig().isTerminateSuffixSpace() ? " " : "")
               + (BungeePerms.getInstance().getConfig().isTerminateSuffixReset() ? ChatColor.RESET : "");
    }

    public void flushCache()
    {
        access();

        permCheckResults.clear();
        cachedPerms.clear();
    }

    private Sender getSender()
    {
        return BungeePerms.getInstance().getConfig().isUseUUIDs()
               ? BungeePerms.getInstance().getPlugin().getPlayer(UUID)
               : BungeePerms.getInstance().getPlugin().getPlayer(name);
    }

    private void debug(String perm, boolean result)
    {
        if (BungeePerms.getInstance().getConfig().isDebug())
        {
            BungeePerms.getLogger().info("perm check: " + name + " has " + perm + ": " + result);
        }
    }

    private void access()
    {
        lastAccess = System.currentTimeMillis();
    }

    private void checkTimedEntries()
    {
        if (nextTimedEntryRunOut != null && nextTimedEntryRunOut < System.currentTimeMillis())
        {
            invalidateCache();
        }
    }

    private Long getNextTimedEntry()
    {
        boolean dosave = false;
        Long next = null;

        //timed groups
        List<SimpleEntry<SimpleEntry<String, String>, List<TimedValue<String>>>> ll = new ArrayList();
        List<SimpleEntry<String, String>> tosavegroups = new ArrayList();
        ll.add(new SimpleEntry<>(new SimpleEntry<String, String>(null, null), timedGroups));
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
                long end = g.getStart().getTime() + (long)g.getDuration() * 1000;
                if (end < System.currentTimeMillis())
                {
                    BungeePerms.getInstance().getDebug().log("removing timed group " + g.getValue() + " from user " + name + " (" + l.getKey().getKey() + "," + l.getKey().getValue() + ")");
                    l.getValue().remove(g);
                    i--;
                    dosave = true;
                    tosavegroups.add(l.getKey());
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
                long end = p.getStart().getTime() + (long)p.getDuration() * 1000;
                if (end < System.currentTimeMillis())
                {
                    //remove timed perm
                    BungeePerms.getInstance().getDebug().log("removing timed permission " + p.getValue() + " from user " + name + " (" + l.getKey().getKey() + "," + l.getKey().getValue() + ")");
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
            for (SimpleEntry<String, String> sw : tosavegroups)
            {
                BungeePerms.getInstance().getDebug().log("saving timed groups for user " + name + " (" + sw.getKey() + "," + sw.getValue() + ")");
                BungeePerms.getInstance().getPermissionsManager().getBackEnd().saveUserTimedGroups(this, sw.getKey(), sw.getValue());
            }
            for (SimpleEntry<String, String> sw : tosaveperms)
            {
                BungeePerms.getInstance().getDebug().log("saving timed permissions for user " + name + " (" + sw.getKey() + "," + sw.getValue() + ")");
                BungeePerms.getInstance().getPermissionsManager().getBackEnd().saveUserTimedPerms(this, sw.getKey(), sw.getValue());
            }

            flushCache();
//            BungeePerms.getInstance().getNetworkNotifier().reloadUser(this, null);
            BungeePerms.getInstance().getEventDispatcher().dispatchUserChangeEvent(this);
        }

        //groups recursive
        List<String> allgroups = new ArrayList();
        allgroups.addAll(groups);
        for (TimedValue<String> tgroup : timedGroups)
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
        Set<Group> checkedgroups = new HashSet();
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
        if (BungeePerms.getInstance().getConfig().isDebug())
            BungeePerms.getInstance().getDebug().log("getNextTimedPermission " + name + ": " + next);
        return next;
    }

    private Boolean getCachedResult(String permission, String server, String world)
    {
        checkTimedEntries();
        return getPermMap(server, world).get(permission);
    }

    private void setCachedResult(String permission, boolean value, String server, String world)
    {
        getPermMap(server, world).put(permission, value);
    }

    private Map<String, Boolean> getPermMap(String server, String world)
    {
        //get server
        Map<String, Map<String, Boolean>> worldmap = permCheckResults.get(server);
        if (worldmap == null)
        {
            worldmap = new HashMap();
            permCheckResults.put(server, worldmap);
        }

        //get world
        Map<String, Boolean> permmap = worldmap.get(world);
        if (permmap == null)
        {
            permmap = new HashMap();
            worldmap.put(world, permmap);
        }

        return permmap;
    }
}
