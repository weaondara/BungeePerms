package net.alpenblock.bungeeperms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.alpenblock.bungeeperms.platform.Sender;

@Getter
@Setter
@ToString
public class User implements PermEntity
{

    @Getter(value = AccessLevel.PRIVATE)
    @Setter(value = AccessLevel.PRIVATE)
    private Map<String, List<String>> cachedPerms;
    @Getter(value = AccessLevel.PRIVATE)
    @Setter(value = AccessLevel.PRIVATE)
    private Map<String, Boolean> checkResults;
    @Getter(value = AccessLevel.PRIVATE)
    @Setter(value = AccessLevel.PRIVATE)
    private Map<String, Map<String, Boolean>> serverCheckResults;
    @Getter(value = AccessLevel.PRIVATE)
    @Setter(value = AccessLevel.PRIVATE)
    private Map<String, Map<String, Map<String, Boolean>>> serverWorldCheckResults;

    private String name;
    private UUID UUID;
    private List<Group> groups;
    private List<String> perms;
    private Map<String, Server> servers;

    private String display;
    private String prefix;
    private String suffix;

    private long lastAccess;

    public User(String name, UUID UUID, List<Group> groups, List<String> extraPerms, Map<String, Server> servers, String display, String prefix, String suffix)
    {
        cachedPerms = new HashMap<>();
        checkResults = new HashMap<>();
        serverCheckResults = new HashMap<>();
        serverWorldCheckResults = new HashMap<>();

        this.name = name;
        this.UUID = UUID;
        this.groups = groups;
        this.perms = extraPerms;
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

    public boolean hasPerm(String perm)
    {
        access();

        Sender s = getSender();
        return hasPerm(s, perm);
    }

    public boolean hasPermOnServer(String perm, String server)
    {
        access();

        Sender s = getSender();
        return hasPermOnServer(s, perm, server);
    }

    public boolean hasPermOnServerInWorld(String perm, String server, String world)
    {
        access();

        Sender s = getSender();
        return hasPermOnServerInWorld(s, perm, server, world);
    }

    public boolean hasPerm(Sender s, String perm)
    {
        access();

        perm = Statics.toLower(perm);

        //ops have every permission so *
        if (s != null && s.isOperator())
        {
            //debug mode
            debug(perm, true);
            return true;
        }

        //check cached perms
        Boolean cached = checkResults.get(perm);
        if (cached != null)
        {
            //debug mode
            debug(perm, cached);
            return cached;
        }

        //check perms
        List<String> perms = getEffectivePerms();

        //pre process
        perms = BungeePerms.getInstance().getPermissionsResolver().preprocess(perms, s);

        //resolve
        Boolean has = BungeePerms.getInstance().getPermissionsResolver().has(perms, perm);

        //post process
        has = BungeePerms.getInstance().getPermissionsResolver().postprocess(perm, has, s);

        //only true if really true
        has = has != null && has;

        //cache
        checkResults.put(perm, has);

        //debug mode
        debug(perm, has);

        return has;
    }

    public boolean hasPermOnServer(Sender s, String perm, String server)
    {
        access();

        perm = Statics.toLower(perm);
        server = Statics.toLower(server);

        //ops have every permission so *
        if (s != null && s.isOperator())
        {
            //debug mode
            debug(perm, true);
            return true;
        }

        //check cached perms
        Map<String, Boolean> serverresults = serverCheckResults.get(server);
        if (serverresults == null)
        {
            serverresults = new HashMap<>();
            serverCheckResults.put(server, serverresults);
        }

        Boolean cached = serverresults.get(perm);
        if (cached != null)
        {
            //debug mode
            debug(perm, cached);
            return cached;
        }

        //check perms
        List<String> perms = getEffectivePerms(server);

        //pre process
        perms = BungeePerms.getInstance().getPermissionsResolver().preprocess(perms, s);

        //resolve
        Boolean has = BungeePerms.getInstance().getPermissionsResolver().has(perms, perm);

        //post process
        has = BungeePerms.getInstance().getPermissionsResolver().postprocess(perm, has, s);

        //only true if really true
        has = has != null && has;

        //cache
        serverresults.put(perm, has);

        //debug mode
        debug(perm, has);

        return has;
    }

    public boolean hasPermOnServerInWorld(Sender s, String perm, String server, String world)
    {
        access();

        perm = Statics.toLower(perm);
        server = Statics.toLower(server);
        world = Statics.toLower(world);

        //ops have every permission so *
        if (s != null && s.isOperator())
        {
            //debug mode
            debug(perm, true);
            return true;
        }

        //check cached perms
        Map<String, Map<String, Boolean>> serverresults = serverWorldCheckResults.get(server);
        if (serverresults == null)
        {
            serverresults = new HashMap<>();
            serverWorldCheckResults.put(server, serverresults);
        }

        Map<String, Boolean> worldresults = serverresults.get(world);
        if (worldresults == null)
        {
            worldresults = new HashMap<>();
            serverresults.put(world, worldresults);
        }

        Boolean cached = worldresults.get(perm);
        if (cached != null)
        {
            //debug mode
            debug(perm, cached);
            return cached;
        }

        //check perms
        List<String> perms = getEffectivePerms(server, world);

        //pre process
        perms = BungeePerms.getInstance().getPermissionsResolver().preprocess(perms, s);

        //resolve
        Boolean has = BungeePerms.getInstance().getPermissionsResolver().has(perms, perm);

        //post process
        has = BungeePerms.getInstance().getPermissionsResolver().postprocess(perm, has, s);

        //only true if really true
        has = has != null && has;

        //cache
        worldresults.put(perm, has);

        //debug mode
        debug(perm, has);

        return has;
    }

    public List<String> getEffectivePerms()
    {
        access();

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
        access();

        server = Statics.toLower(server);

        List<String> effperms = cachedPerms.get(Statics.toLower(server));
        if (effperms == null)
        {
            effperms = calcEffectivePerms(server);
            cachedPerms.put(server, effperms);
        }

        return new ArrayList<>(effperms);
    }

    public List<String> getEffectivePerms(String server, String world)
    {
        access();

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
        access();

        List<String> ret = new ArrayList<>();
        for (Group g : groups)
        {
            List<String> gperms = g.getEffectivePerms();
            ret.addAll(gperms);
        }
        ret.addAll(perms);

        ret = BungeePerms.getInstance().getPermissionsResolver().simplify(ret);

        return ret;
    }

    public List<String> calcEffectivePerms(String server)
    {
        access();

        List<String> ret = new ArrayList<>();
        for (Group g : groups)
        {
            List<String> gperms = g.getEffectivePerms(server);
            ret.addAll(gperms);
        }
        ret.addAll(perms);

        //per server perms
        Server srv = getServer(server);
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
        access();

        List<String> ret = new ArrayList<>();
        for (Group g : groups)
        {
            List<String> gperms = g.getEffectivePerms(server, world);
            ret.addAll(gperms);
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

    public void recalcPerms()
    {
        recalcPerms0();

        //call event
        BungeePerms.getInstance().getEventDispatcher().dispatchUserChangeEvent(this);
    }

    public void recalcPerms(String server)
    {
        recalcPerms0(server);

        //call event
        BungeePerms.getInstance().getEventDispatcher().dispatchUserChangeEvent(this);
    }

    public void recalcPerms(String server, String world)
    {
        recalcPerms0(server, world);

        //call event
        BungeePerms.getInstance().getEventDispatcher().dispatchUserChangeEvent(this);
    }

    private void recalcPerms0()
    {
        access();

        for (Map.Entry<String, List<String>> e : cachedPerms.entrySet())
        {
            String where = e.getKey();
            List<String> l = Statics.toList(where, ";");
            String server = l.get(0);

            if (l.size() == 1)
            {
                if (server.equalsIgnoreCase("global"))
                {
                    cachedPerms.put("global", calcEffectivePerms());
                }
                else
                {
                    List<String> effperms = calcEffectivePerms(server);
                    cachedPerms.put(Statics.toLower(server), effperms);
                }
            }
            else if (l.size() == 2)
            {
                String world = l.get(1);

                recalcPerms0(server, world);
            }
        }

        checkResults.clear();
        serverCheckResults.clear();
        serverWorldCheckResults.clear();
    }

    private void recalcPerms0(String server)
    {
        access();

        server = Statics.toLower(server);

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

        Map<String, Boolean> serverresults = serverCheckResults.get(server);
        if (serverresults != null)
        {
            serverresults.clear();
        }

        Map<String, Map<String, Boolean>> worldresults = serverWorldCheckResults.get(server);
        if (worldresults != null)
        {
            worldresults.clear();
        }
    }

    private void recalcPerms0(String server, String world)
    {
        access();

        server = Statics.toLower(server);
        world = Statics.toLower(world);

        List<String> effperms = calcEffectivePerms(server, world);
        cachedPerms.put(server + ";" + world, effperms);

        Map<String, Map<String, Boolean>> serverresults = serverWorldCheckResults.get(server);
        if (serverresults != null)
        {
            Map<String, Boolean> worldresults = serverresults.get(world);
            if (worldresults != null)
            {
                worldresults.clear();
            }
        }
    }

    public boolean isNothingSpecial()
    {
        access();

        for (Group g : groups)
        {
            if (!g.isDefault())
            {
                return false;
            }
        }
        for (Server s : servers.values())
        {
            if (!s.getPerms().isEmpty() || !s.getDisplay().isEmpty() || !s.getPrefix().isEmpty() || !s.getSuffix().isEmpty())
            {
                return false;
            }
            for (World w : s.getWorlds().values())
            {
                if (!s.getPerms().isEmpty() || !w.getDisplay().isEmpty() || !w.getPrefix().isEmpty() || !w.getSuffix().isEmpty())
                {
                    return false;
                }
            }
        }
        return perms.isEmpty() && display!= null && display.isEmpty() && prefix != null && prefix.isEmpty() && suffix != null && suffix.isEmpty();
    }

    public Group getGroupByLadder(String ladder)
    {
        access();

        for (Group g : groups)
        {
            if (g.getLadder().equalsIgnoreCase(ladder))
            {
                return g;
            }
        }
        return null;
    }

    public List<BPPermission> getPermsWithOrigin(String server, String world)
    {
        access();

        List<BPPermission> ret = new ArrayList<>();

        //add groups' perms
        for (Group g : groups)
        {
            ret.addAll(g.getPermsWithOrigin(server, world));
        }

        for (String s : perms)
        {
            BPPermission perm = new BPPermission(s, name, false, null, null);
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

            List<String> perserverPerms = srv.getValue().getPerms();
            for (String s : perserverPerms)
            {
                BPPermission perm = new BPPermission(s, name, false, srv.getKey(), null);
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

                List<String> perserverWorldPerms = w.getValue().getPerms();
                for (String s2 : perserverWorldPerms)
                {
                    BPPermission perm2 = new BPPermission(s2, name, false, srv.getKey(), w.getKey());
                    ret.add(perm2);
                }
            }

        }

        return ret;
    }

    public List<String> getGroupsString()
    {
        access();

        List<String> ret = new ArrayList<>();
        for (Group g : groups)
        {
            ret.add(g.getName());
        }

        return ret;
    }

    public int getOwnPermissionsCount()
    {
        access();

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
        access();

        int count = getOwnPermissionsCount();

        for (Group g : groups)
        {
            count += g.getOwnPermissionsCount();
        }

        return count;
    }

    public String buildPrefix()
    {
        access();

        Sender sender = getSender();
        return buildPrefix(sender);
    }

    public String buildSuffix()
    {
        access();

        Sender sender = getSender();
        return buildSuffix(sender);
    }

    public String buildPrefix(Sender sender)
    {
        access();

        String prefix = "";

        List<String> prefixes = new ArrayList<>();

        for (Group g : groups)
        {
            //global
            if (!Statics.isEmpty(g.getPrefix()))
            {
                prefixes.add(g.getPrefix());
            }

            //server
            Server s = g.getServer(sender != null ? sender.getServer() : null);
            if (s != null)
            {
                if (!Statics.isEmpty(s.getPrefix()))
                {
                    prefixes.add(s.getPrefix());
                }

                //world
                World w = s.getWorld(sender != null ? sender.getWorld() : null);
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
        Server s = getServer(sender != null ? sender.getServer() : null);
        if (s != null)
        {
            if (!Statics.isEmpty(s.getPrefix()))
            {
                prefixes.add(s.getPrefix());
            }

            //world
            World w = s.getWorld(sender != null ? sender.getWorld() : null);
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
            if (!ChatColor.strip(p.replaceAll("&", "�")).isEmpty()
                    && !prefix.isEmpty()
                    && !ChatColor.strip(prefix.replaceAll("&", "�")).endsWith(" "))
            {
                prefix += " ";
            }
            prefix += p;
        }

        return prefix
                + (BungeePerms.getInstance().getConfig().isTerminatePrefixSpace() ? " " : "")
                + (BungeePerms.getInstance().getConfig().isTerminatePrefixReset() ? ChatColor.RESET : "");
    }

    public String buildSuffix(Sender sender)
    {
        access();

        String suffix = "";

        List<String> suffixes = new ArrayList<>();

        for (Group g : groups)
        {
            //global
            if (!Statics.isEmpty(g.getSuffix()))
            {
                suffixes.add(g.getSuffix());
            }

            //server
            Server s = g.getServer(sender != null ? sender.getServer() : null);
            if (s != null)
            {
                if (!Statics.isEmpty(s.getSuffix()))
                {
                    suffixes.add(s.getSuffix());
                }

                //world
                World w = s.getWorld(sender != null ? sender.getWorld() : null);
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
        Server s = getServer(sender != null ? sender.getServer() : null);
        if (s != null)
        {
            if (!Statics.isEmpty(s.getSuffix()))
            {
                suffixes.add(s.getSuffix());
            }

            //world
            World w = s.getWorld(sender != null ? sender.getWorld() : null);
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
            if (!ChatColor.strip(suf.replaceAll("&", "�")).isEmpty()
                    && !suffix.isEmpty()
                    && !ChatColor.strip(suffix.replaceAll("&", "�")).endsWith(" "))
            {
                suffix += " ";
            }
            suffix += suf;
        }

        return suffix
                + (BungeePerms.getInstance().getConfig().isTerminateSuffixSpace() ? " " : "")
                + (BungeePerms.getInstance().getConfig().isTerminateSuffixReset() ? ChatColor.RESET : "");
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

    @Deprecated
    public List<String> getExtraPerms() //todo: remove
    {
        return perms;
    }

    @Deprecated
    public void setExtraPerms(List<String> perms) //todo: remove
    {
        this.perms = perms;
    }
}
