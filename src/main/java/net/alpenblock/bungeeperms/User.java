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
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Plugin;

@Getter
@Setter
@ToString
public class User
{
	private Plugin p;
	
    @Getter(value = AccessLevel.PRIVATE)
    @Setter(value = AccessLevel.PRIVATE)
    private Map<String,List<String>> cachedPerms;
    
	private String name;
    private UUID UUID;
	private List<Group> groups;
	private List<String> extraperms;
	private Map<String, List<String>> serverPerms;
    private Map<String, Map<String, List<String>>> serverWorldPerms;
    
    private Map<String, Boolean> checkResults;
    private Map<String, Map<String, Boolean>> serverCheckResults;
    private Map<String, Map<String, Map<String, Boolean>>> serverWorldCheckResults;
	
	public User(Plugin p, String name, UUID UUID, List<Group> groups, List<String> extraperms, Map<String, List<String>> serverPerms, Map<String, Map<String, List<String>>> serverWorldPerms) 
	{
		this.p = p;
        cachedPerms=new HashMap<>();
        checkResults=new HashMap<>();
        serverCheckResults=new HashMap<>();
        serverWorldCheckResults=new HashMap<>();
        
		this.name = name;
		this.UUID = UUID;
		this.groups = groups;
		this.extraperms = extraperms;
		this.serverPerms = serverPerms;
		this.serverWorldPerms = serverWorldPerms;
	}
	
	public boolean hasPerm(String perm)
	{
        Boolean cached=checkResults.get(perm.toLowerCase());
        if(cached!=null)
        {
            return cached;
        }
        
		List<String> perms=getEffectivePerms();
        
        Boolean has=BungeePerms.getInstance().getPermissionsManager().getResolver().has(perms, perm);
		
        boolean res = has!=null && has;
        
        checkResults.put(perm.toLowerCase(), res);
        
        return res;
	}
	public boolean hasPermOnServer(String perm, ServerInfo server) 
	{
        Map<String, Boolean> serverresults = serverCheckResults.get(server.getName());
        if(serverresults == null)
        {
            serverresults=new HashMap<>();
            serverCheckResults.put(server.getName(), serverresults);
        }
        
        Boolean cached=serverresults.get(perm.toLowerCase());
        if(cached!=null)
        {
            return cached;
        }
        
		List<String> perms=getEffectivePerms(server);
        
        Boolean has=BungeePerms.getInstance().getPermissionsManager().getResolver().has(perms, perm);
		
        boolean res = has!=null && has;
        
        serverresults.put(perm.toLowerCase(), res);
        
        return res;
	}
    public boolean hasPermOnServerInWorld(String perm, ServerInfo server, String world) 
	{
        Map<String, Map<String, Boolean>> serverresults = serverWorldCheckResults.get(server.getName());
        if(serverresults == null)
        {
            serverresults=new HashMap<>();
            serverWorldCheckResults.put(server.getName(), serverresults);
        }
        
        Map<String, Boolean> worldresults = serverresults.get(world);
        if(worldresults == null)
        {
            worldresults=new HashMap<>();
            serverresults.put(world, worldresults);
        }
        
        Boolean cached=worldresults.get(perm.toLowerCase());
        if(cached!=null)
        {
            return cached;
        }
        
		List<String> perms=getEffectivePerms(server, world);
		        
        Boolean has=BungeePerms.getInstance().getPermissionsManager().getResolver().has(perms, perm);
		
        boolean res = has!=null && has;
        
        worldresults.put(perm.toLowerCase(), res);
        
        return res;
	}
	
	public List<String> getEffectivePerms()
	{
        List<String> effperms=cachedPerms.get("global");
        if(effperms==null)
        {
            effperms=calcEffectivePerms();
            cachedPerms.put("global", effperms);
        }
        
        return effperms;
    }
    public List<String> getEffectivePerms(ServerInfo server) 
	{
        List<String> effperms=cachedPerms.get(server.getName().toLowerCase());
        if(effperms==null)
        {
            effperms=calcEffectivePerms(server);
            cachedPerms.put(server.getName().toLowerCase(), effperms);
        }
        
        return effperms;
    }
    public List<String> getEffectivePerms(ServerInfo server, String world) 
	{
        List<String> effperms=cachedPerms.get(server.getName().toLowerCase()+";"+world.toLowerCase());
        if(effperms==null)
        {
            effperms=calcEffectivePerms(server,world);
            cachedPerms.put(server.getName().toLowerCase()+";"+world.toLowerCase(), effperms);
        }
        
        return effperms;
    }
    
	public List<String> calcEffectivePerms()
	{
		List<String> ret=new ArrayList<>();
		for(Group g:groups)
		{
			List<String> gperms=g.getEffectivePerms();
            ret.addAll(gperms);
		}
        ret.addAll(extraperms);
        
        ret=BungeePerms.getInstance().getPermissionsManager().getResolver().simplify(ret);
        
		return ret;
	}
	public List<String> calcEffectivePerms(ServerInfo server)
	{
		List<String> ret=new ArrayList<>();
		for(Group g:groups)
		{
			List<String> gperms=g.getEffectivePerms(server);
			ret.addAll(gperms);
		}
		ret.addAll(extraperms);
		
		//per server perms
		List<String> perserverPerms=serverPerms.get(server.getName().toLowerCase());
		if(perserverPerms!=null)
		{
			ret.addAll(perserverPerms);
		}
        
        ret=BungeePerms.getInstance().getPermissionsManager().getResolver().simplify(ret);
		
		return ret;
	}
	public List<String> calcEffectivePerms(ServerInfo server, String world)
	{
		List<String> ret=new ArrayList<>();
		for(Group g:groups)
		{
			List<String> gperms=g.getEffectivePerms(server,world);
			ret.addAll(gperms);
		}
		
		ret.addAll(extraperms);
		
		//per server perms
		List<String> perserverPerms=serverPerms.get(server.getName().toLowerCase());
		if(perserverPerms!=null)
		{
			ret.addAll(perserverPerms);
		}
        
        //per server world perms
        Map<String,List<String>> serverPerms=serverWorldPerms.get(server.getName().toLowerCase());
        if(serverPerms!=null)
        {
            List<String> serverWorldPerms=serverPerms.get(world.toLowerCase());
            if(serverWorldPerms!=null)
            {
                ret.addAll(serverWorldPerms);
            }
        }
        
        ret=BungeePerms.getInstance().getPermissionsManager().getResolver().simplify(ret);
		
		return ret;
	}

    public void recalcPerms() 
    {
        for(Map.Entry<String, List<String>> e:cachedPerms.entrySet())
        {
            String where=e.getKey();
            List<String> l=Statics.toList(where, ";");
            String server=l.get(0);
            
            if(l.size()==1)
            {
                if(server.equalsIgnoreCase("global"))
                {
                    cachedPerms.put("global", calcEffectivePerms());
                }
                else
                {
                    ServerInfo si=p.getProxy().getConfig().getServers().get(server);
                    List<String> effperms=calcEffectivePerms(si);
                    cachedPerms.put(si.getName().toLowerCase(), effperms);
                }
            }
            else if(l.size()==2)
            {
                String world=l.get(1);
                
                recalcPerms(server,world);
            }
        }
        
        checkResults.clear();
        serverCheckResults.clear();
        serverWorldCheckResults.clear();
    }
    public void recalcPerms(String server)
    {
        for(Map.Entry<String, List<String>> e:cachedPerms.entrySet())
        {
            String where=e.getKey();
            List<String> l=Statics.toList(where, ";");
            String lserver=l.get(0);
            
            if(lserver.equalsIgnoreCase(server))
            {
                if(l.size()==1)
                {
                    ServerInfo si=p.getProxy().getConfig().getServers().get(lserver);
                    List<String> effperms=calcEffectivePerms(si);
                    cachedPerms.put(si.getName().toLowerCase(), effperms);
                }
                else if(l.size()==2)
                {
                    String world=l.get(1);
                    recalcPerms(server,world);
                }
            }
        }
        
        Map<String, Boolean> serverresults = serverCheckResults.get(server);
        if(serverresults != null)
        {
            serverresults.clear();
        }
        
        Map<String, Map<String, Boolean>> worldresults = serverWorldCheckResults.get(server);
        if(worldresults != null)
        {
            worldresults.clear();
        }
    }
    public void recalcPerms(String server,String world)
    {
        ServerInfo si=p.getProxy().getConfig().getServers().get(server);
        List<String> effperms=calcEffectivePerms(si,world);
        cachedPerms.put(si.getName().toLowerCase()+";"+world.toLowerCase(), effperms);
        
        Map<String, Map<String, Boolean>> serverresults = serverWorldCheckResults.get(server);
        if(serverresults != null)
        {
            Map<String, Boolean> worldresults = serverresults.get(world);
            if(worldresults != null)
            {
                worldresults.clear();
            }
        }
    }
    
    public boolean isNothingSpecial() 
    {
        for(Group g:groups)
        {
            if(!g.isDefault())
            {
                return false;
            }
        }
        return serverWorldPerms.isEmpty()&serverPerms.isEmpty()&extraperms.isEmpty();
    }

    public Group getGroupByLadder(String ladder) 
    {
        for(Group g:groups)
        {
            if(g.getLadder().equalsIgnoreCase(ladder))
            {
                return g;
            }
        }
        return null;
    }
    
    public List<BPPermission> getPermsWithOrigin(String server, String world)
    {
        List<BPPermission> ret=new ArrayList<>();
        
        //add groups' perms
		for(Group g:groups)
		{
            ret.addAll(g.getPermsWithOrigin(server, world));
		}
		
		for(String s:extraperms)
		{
			BPPermission perm=new BPPermission(s,name,false,null,null);
            ret.add(perm);
		}
		
		//per server perms
        for(Map.Entry<String, List<String>> srv:serverPerms.entrySet())
        {
            //check for server
            if(server!=null && !srv.getKey().equalsIgnoreCase(server))
            {
                continue;
            }
            
            List<String> perserverPerms=srv.getValue();
            for(String s:perserverPerms)
            {
                BPPermission perm=new BPPermission(s,name,false,srv.getKey(),null);
                ret.add(perm);
                
                //per server world perms
                Map<String, List<String>> worldperms = serverWorldPerms.get(srv.getKey());
                if(worldperms==null)
                {
                    continue;
                }
                for(Map.Entry<String, List<String>> w:worldperms.entrySet())
                {
                    //check for world
                    if(world!=null && !w.getKey().equalsIgnoreCase(world))
                    {
                        continue;
                    }
                    
                    
                    List<String> perserverWorldPerms=w.getValue();
                    for(String s2:perserverWorldPerms)
                    {
                        BPPermission perm2=new BPPermission(s2,name,false,srv.getKey(),w.getKey());
                        ret.add(perm2);
                    }
                }
            }
        }
        
		return ret;
    }
}
