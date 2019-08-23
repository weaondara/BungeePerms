package net.alpenblock.bungeeperms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private Long nextTimedPermissionRunOut;

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
        checkTimedPermissions();

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
        access();
        checkTimedPermissions();

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

    public List<String> getGroupsString()
    {
        access();

        return groups;
    }

    public List<TimedValue<Group>> getTimedGroups()
    {
        access();
        checkTimedPermissions();

        List<TimedValue<Group>> ret = new ArrayList<>();
        for (TimedValue<String> name : timedGroups)
        {
            Group g = BungeePerms.getInstance().getPermissionsManager().getGroup(name.getValue());
            if (g != null)
                ret.add(new TimedValue<Group>(g, name.getStart(), name.getDuration()));
        }

        return ret;
    }

    public List<TimedValue<String>> getTimedGroupsString()
    {
        access();
        checkTimedPermissions();

        return timedGroups;
    }

    @Deprecated
    public boolean hasPerm(String perm)
    {
        return hasPerm(perm, null);
    }

    @Deprecated
    public boolean hasPerm(String perm, String server)
    {
        return hasPerm(perm, server, null);
    }

    public boolean hasPerm(String perm, String server, String world)
    {
        access();
        checkTimedPermissions();

        Sender s = getSender();
        return hasPerm(s, perm, server, world);
    }

    public boolean hasPerm(Sender s, String perm, String server, String world)
    {
        access();
        checkTimedPermissions();

        perm = Statics.toLower(perm);
        server = Statics.toLower(server);
        world = server == null ? null : Statics.toLower(world);

        //check cached perms
        Boolean cached = getCachedResult(perm, server, world);
        if (cached != null)
        {
            //debug mode
            debug(perm, cached);
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
        debug(perm, has);

        return has;
    }

    public List<BPPermission> getEffectivePerms(String server, String world)
    {
        access();
        checkTimedPermissions();

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
        checkTimedPermissions();

        server = Statics.toLower(server);
        world = server == null ? null : Statics.toLower(world);

        List<BPPermission> ret = new ArrayList<>();
        for (String s : groups)
        {
            Group g = BungeePerms.getInstance().getPermissionsManager().getGroup(s);
            if (g == null)
                continue;
            List<BPPermission> gperms = g.getEffectivePerms(server, world);
            ret.addAll(gperms);
        }
        for (TimedValue<String> tg : timedGroups)
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
        Server srv = getServer(server);
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

        return ret;
    }

    public void invalidateCache()
    {
        BungeePerms.getInstance().getDebug().log("invalidate cache for user " + name);
        flushCache();
        nextTimedPermissionRunOut = getNextTimedPermission();
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
            if (!s.getPerms().isEmpty()
                || !s.getTimedPerms().isEmpty()
                || !Statics.isEmpty(s.getDisplay())
                || !Statics.isEmpty(s.getPrefix())
                || !Statics.isEmpty(s.getSuffix()))
                return false;
            for (World w : s.getWorlds().values())
                if (!w.getPerms().isEmpty()
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
        checkTimedPermissions();

        List<BPPermission> ret = getEffectivePerms(server, world);

        //pre process
        ret = BungeePerms.getInstance().getPermissionsResolver().preprocess(ret, getSender());

        return ret;
    }

    public int getOwnPermissionsCount(String server, String world)
    {
        access();
        checkTimedPermissions();

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
        checkTimedPermissions();

        int count = getOwnPermissionsCount(server, world);

        for (Group g : getGroups())
            count += g.getPermissionsCount(server, world);

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

        String prefix = "";

        List<String> prefixes = new ArrayList<>();

        for (Group g : getGroups())
        {
            //global
            if (!Statics.isEmpty(g.getPrefix()))
            {
                prefixes.add(g.getPrefix());
            }

            //server
            Server s = g.getServer(server);
            if (s != null)
            {
                if (!Statics.isEmpty(s.getPrefix()))
                {
                    prefixes.add(s.getPrefix());
                }

                //world
                World w = s.getWorld(world);
                if (w != null)
                {
                    if (!Statics.isEmpty(w.getPrefix()))
                    {
                        prefixes.add(w.getPrefix());
                    }
                }
            }
        }

        //global
        if (!Statics.isEmpty(this.prefix))
        {
            prefixes.add(this.prefix);
        }

        //server
        Server s = getServer(server);
        if (s != null)
        {
            if (!Statics.isEmpty(s.getPrefix()))
            {
                prefixes.add(s.getPrefix());
            }

            //world
            World w = s.getWorld(world);
            if (w != null)
            {
                if (!Statics.isEmpty(w.getPrefix()))
                {
                    prefixes.add(w.getPrefix());
                }
            }
        }

        for (String p : prefixes)
        {
            if (!ChatColor.strip(p.replaceAll("&", ChatColor.COLOR_CHAR + "")).isEmpty()
                && !prefix.isEmpty()
                && !ChatColor.strip(prefix.replaceAll("&", ChatColor.COLOR_CHAR + "")).endsWith(" "))
            {
                prefix += " ";
            }
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

        String suffix = "";

        List<String> suffixes = new ArrayList<>();

        for (Group g : getGroups())
        {
            //global
            if (!Statics.isEmpty(g.getSuffix()))
            {
                suffixes.add(g.getSuffix());
            }

            //server
            Server s = g.getServer(server);
            if (s != null)
            {
                if (!Statics.isEmpty(s.getSuffix()))
                {
                    suffixes.add(s.getSuffix());
                }

                //world
                World w = s.getWorld(world);
                if (w != null)
                {
                    if (!Statics.isEmpty(w.getSuffix()))
                    {
                        suffixes.add(w.getSuffix());
                    }
                }
            }
        }

        //global
        if (!Statics.isEmpty(this.suffix))
        {
            suffixes.add(this.suffix);
        }

        //server
        Server s = getServer(server);
        if (s != null)
        {
            if (!Statics.isEmpty(s.getSuffix()))
            {
                suffixes.add(s.getSuffix());
            }

            //world
            World w = s.getWorld(world);
            if (w != null)
            {
                if (!Statics.isEmpty(w.getSuffix()))
                {
                    suffixes.add(w.getSuffix());
                }
            }
        }

        for (String suf : suffixes)
        {
            if (!ChatColor.strip(suf.replaceAll("&", ChatColor.COLOR_CHAR + "")).isEmpty()
                && !suffix.isEmpty()
                && !ChatColor.strip(suffix.replaceAll("&", ChatColor.COLOR_CHAR + "")).endsWith(" "))
            {
                suffix += " ";
            }
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

    private void checkTimedPermissions()
    {
        if (nextTimedPermissionRunOut != null && nextTimedPermissionRunOut < System.currentTimeMillis())
        {
            BungeePerms.getInstance().getDebug().log("invalidated user" + name);
            flushCache();
            nextTimedPermissionRunOut = getNextTimedPermission();
        }
    }

    private Long getNextTimedPermission()
    {
        boolean dosavegroups = false;
        boolean dosaveperms = false;
        Long next = null;

        //timed groups
        for (int i = 0; i < timedGroups.size(); i++)
        {
            TimedValue<String> g = timedGroups.get(i);
            long end = g.getStart().getTime() + g.getDuration() * 1000;
            if (end < System.currentTimeMillis())
            {
                BungeePerms.getInstance().getDebug().log("removing timedgroup " + g.getValue() + " from user " + name);
                timedGroups.remove(g);
                i--;
                dosavegroups = true;
            }
            else
            {
                next = next == null ? end : Math.min(next, end);
            }
        }
        if (dosavegroups)
            BungeePerms.getInstance().getPermissionsManager().getBackEnd().saveUserTimedGroups(this);

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
                TimedValue<String> p = l.get(i);
                long end = p.getStart().getTime() + p.getDuration() * 1000;
                if (end < System.currentTimeMillis())
                {
                    //remove timed perm
                    BungeePerms.getInstance().getDebug().log("removing timed permission " + p.getValue() + " from user " + name);
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
            BungeePerms.getInstance().getPermissionsManager().getBackEnd().saveUser(this, true);

        if (dosavegroups || dosaveperms)
        {
            flushCache();
            BungeePerms.getInstance().getNetworkNotifier().reloadUser(this, null);
            BungeePerms.getInstance().getEventDispatcher().dispatchUserChangeEvent(this);
        }

        //groups
        for (String s : groups)
        {
            Group g = BungeePerms.getInstance().getPermissionsManager().getGroup(s);
            if (g == null)
                continue;
            Long end = g.getNextTimedPermission();
            if (end == null)
                continue;
            next = next == null ? end : Math.min(next, end);
        }
        for (TimedValue<String> s : timedGroups)
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

    private Boolean getCachedResult(String permission, String server, String world)
    {
        checkTimedPermissions();
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
