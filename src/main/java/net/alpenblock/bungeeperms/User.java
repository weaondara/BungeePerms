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
public class User
{

    @Getter(value = AccessLevel.PRIVATE)
    @Setter(value = AccessLevel.PRIVATE)
    private Map<String, List<String>> cachedPerms;

    private String name;
    private UUID UUID;
    private List<Group> groups;
    private List<String> extraPerms;
    private Map<String, List<String>> serverPerms;
    private Map<String, Map<String, List<String>>> serverWorldPerms;

    private Map<String, Boolean> checkResults;
    private Map<String, Map<String, Boolean>> serverCheckResults;
    private Map<String, Map<String, Map<String, Boolean>>> serverWorldCheckResults;

    public User(String name, UUID UUID, List<Group> groups, List<String> extraPerms, Map<String, List<String>> serverPerms, Map<String, Map<String, List<String>>> serverWorldPerms)
    {
        cachedPerms = new HashMap<>();
        checkResults = new HashMap<>();
        serverCheckResults = new HashMap<>();
        serverWorldCheckResults = new HashMap<>();

        this.name = name;
        this.UUID = UUID;
        this.groups = groups;
        this.extraPerms = extraPerms;
        this.serverPerms = serverPerms;
        this.serverWorldPerms = serverWorldPerms;
    }

    public boolean hasPerm(String perm)
    {
        //ops have every permission so *
        Sender s = getSender();
        if (s != null && s.isOperator())
        {
            //debug mode
            debug(perm, true);
            return true;
        }

        //check cached perms
        Boolean cached = checkResults.get(perm.toLowerCase());
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
        checkResults.put(perm.toLowerCase(), has);

        //debug mode
        debug(perm, has);

        return has;
    }

    public boolean hasPermOnServer(String perm, String server)
    {
        //ops have every permission so *
        Sender s = getSender();
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

        Boolean cached = serverresults.get(perm.toLowerCase());
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
        serverresults.put(perm.toLowerCase(), has);

        //debug mode
        debug(perm, has);

        return has;
    }

    public boolean hasPermOnServerInWorld(String perm, String server, String world)
    {
        //ops have every permission so *
        Sender s = getSender();
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

        Boolean cached = worldresults.get(perm.toLowerCase());
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
        worldresults.put(perm.toLowerCase(), has);

        //debug mode
        debug(perm, has);

        return has;
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
        List<String> effperms = cachedPerms.get(server.toLowerCase());
        if (effperms == null)
        {
            effperms = calcEffectivePerms(server);
            cachedPerms.put(server.toLowerCase(), effperms);
        }

        return new ArrayList<>(effperms);
    }

    public List<String> getEffectivePerms(String server, String world)
    {
        List<String> effperms = cachedPerms.get(server.toLowerCase() + ";" + world.toLowerCase());
        if (effperms == null)
        {
            effperms = calcEffectivePerms(server, world);
            cachedPerms.put(server.toLowerCase() + ";" + world.toLowerCase(), effperms);
        }

        return new ArrayList<>(effperms);
    }

    public List<String> calcEffectivePerms()
    {
        List<String> ret = new ArrayList<>();
        for (Group g : groups)
        {
            List<String> gperms = g.getEffectivePerms();
            ret.addAll(gperms);
        }
        ret.addAll(extraPerms);

        ret = BungeePerms.getInstance().getPermissionsResolver().simplify(ret);

        return ret;
    }

    public List<String> calcEffectivePerms(String server)
    {
        List<String> ret = new ArrayList<>();
        for (Group g : groups)
        {
            List<String> gperms = g.getEffectivePerms(server);
            ret.addAll(gperms);
        }
        ret.addAll(extraPerms);

        //per server perms
        List<String> perserverPerms = serverPerms.get(server.toLowerCase());
        if (perserverPerms != null)
        {
            ret.addAll(perserverPerms);
        }

        ret = BungeePerms.getInstance().getPermissionsResolver().simplify(ret);

        return ret;
    }

    public List<String> calcEffectivePerms(String server, String world)
    {
        List<String> ret = new ArrayList<>();
        for (Group g : groups)
        {
            List<String> gperms = g.getEffectivePerms(server, world);
            ret.addAll(gperms);
        }

        ret.addAll(extraPerms);

        //per server perms
        List<String> perserverPerms = serverPerms.get(server.toLowerCase());
        if (perserverPerms != null)
        {
            ret.addAll(perserverPerms);
        }

        //per server world perms
        Map<String, List<String>> serverPerms = serverWorldPerms.get(server.toLowerCase());
        if (serverPerms != null)
        {
            List<String> serverWorldPerms = serverPerms.get(world.toLowerCase());
            if (serverWorldPerms != null)
            {
                ret.addAll(serverWorldPerms);
            }
        }

        ret = BungeePerms.getInstance().getPermissionsResolver().simplify(ret);

        return ret;
    }

    public void recalcPerms()
    {
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
                    cachedPerms.put(server.toLowerCase(), effperms);
                }
            }
            else if (l.size() == 2)
            {
                String world = l.get(1);

                recalcPerms(server, world);
            }
        }

        checkResults.clear();
        serverCheckResults.clear();
        serverWorldCheckResults.clear();
    }

    public void recalcPerms(String server)
    {
        for (Map.Entry<String, List<String>> e : cachedPerms.entrySet())
        {
            String where = e.getKey();
            List<String> l = Statics.toList(where, ";");
            String lserver = l.get(0);

            if (lserver.equalsIgnoreCase(server))
            {
                if (l.size() == 1)
                {
                    List<String> effperms = calcEffectivePerms(lserver);
                    cachedPerms.put(lserver.toLowerCase(), effperms);
                }
                else if (l.size() == 2)
                {
                    String world = l.get(1);
                    recalcPerms(server, world);
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

    public void recalcPerms(String server, String world)
    {
        List<String> effperms = calcEffectivePerms(server, world);
        cachedPerms.put(server.toLowerCase() + ";" + world.toLowerCase(), effperms);

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
        for (Group g : groups)
        {
            if (!g.isDefault())
            {
                return false;
            }
        }
        return serverWorldPerms.isEmpty() & serverPerms.isEmpty() & extraPerms.isEmpty();
    }

    public Group getGroupByLadder(String ladder)
    {
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
        List<BPPermission> ret = new ArrayList<>();

        //add groups' perms
        for (Group g : groups)
        {
            ret.addAll(g.getPermsWithOrigin(server, world));
        }

        for (String s : extraPerms)
        {
            BPPermission perm = new BPPermission(s, name, false, null, null);
            ret.add(perm);
        }

        //per server perms
        for (Map.Entry<String, List<String>> srv : serverPerms.entrySet())
        {
            //check for server
            if (server != null && !srv.getKey().equalsIgnoreCase(server))
            {
                continue;
            }

            List<String> perserverPerms = srv.getValue();
            for (String s : perserverPerms)
            {
                BPPermission perm = new BPPermission(s, name, false, srv.getKey(), null);
                ret.add(perm);

                //per server world perms
                Map<String, List<String>> worldperms = serverWorldPerms.get(srv.getKey());
                if (worldperms == null)
                {
                    continue;
                }
                for (Map.Entry<String, List<String>> w : worldperms.entrySet())
                {
                    //check for world
                    if (world != null && !w.getKey().equalsIgnoreCase(world))
                    {
                        continue;
                    }

                    List<String> perserverWorldPerms = w.getValue();
                    for (String s2 : perserverWorldPerms)
                    {
                        BPPermission perm2 = new BPPermission(s2, name, false, srv.getKey(), w.getKey());
                        ret.add(perm2);
                    }
                }
            }
        }

        return ret;
    }

    public List<String> getGroupsString()
    {
        List<String> ret = new ArrayList<>();
        for (Group g : groups)
        {
            ret.add(g.getName());
        }

        return ret;
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
}
