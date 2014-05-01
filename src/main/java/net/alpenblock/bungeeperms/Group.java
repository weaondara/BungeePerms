package net.alpenblock.bungeeperms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.config.ServerInfo;

@Getter
@Setter
@ToString
public class Group implements Comparable<Group>
{
    @Getter(value = AccessLevel.PRIVATE)
    @Setter(value = AccessLevel.PRIVATE)
    private Map<String,List<String>> cachedPerms;
    
	private String name;
	private List<String> inheritances;
	private List<String> perms;
	private Map<String,Server> servers;
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
        
        cachedPerms=new HashMap<>();
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
    public List<String> getEffectivePerms(ServerInfo server,String world) 
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
		for(Group g:BungeePerms.getInstance().getPermissionsManager().getGroups())
		{
			if(inheritances.contains(g.getName()))
			{
				List<String> gperms=g.getEffectivePerms();
                ret.addAll(gperms);
			}
		}
        ret.addAll(perms);
        
        ret=BungeePerms.getInstance().getPermissionsManager().getResolver().simplify(ret);
        
		return ret;
	}
	public List<String> calcEffectivePerms(ServerInfo server) 
	{
		List<String> ret=new ArrayList<>();
		for(Group g:BungeePerms.getInstance().getPermissionsManager().getGroups())
		{
			if(inheritances.contains(g.getName()))
			{
				List<String> gperms=g.getEffectivePerms(server);
				ret.addAll(gperms);
			}
		}
		
		ret.addAll(perms);
		
		//per server perms
        Server srv=servers.get(server.getName().toLowerCase());
        if(srv!=null)
        {
            List<String> perserverperms=srv.getPerms();
            ret.addAll(perserverperms);
        }
        
        ret=BungeePerms.getInstance().getPermissionsManager().getResolver().simplify(ret);
		
		return ret;
	}
	public List<String> calcEffectivePerms(ServerInfo server,String world) 
	{
		List<String> ret=new ArrayList<>();
		for(Group g:BungeePerms.getInstance().getPermissionsManager().getGroups())
		{
			if(inheritances.contains(g.getName()))
			{
                List<String> gperms=g.getEffectivePerms(server,world);
				ret.addAll(gperms);
            }
		}

		ret.addAll(perms);

        //per server perms
        Server srv=servers.get(server.getName().toLowerCase());
        if(srv!=null)
        {
            List<String> perserverperms=srv.getPerms();
            ret.addAll(perserverperms);
            
            World w=srv.getWorlds().get(world.toLowerCase());
            if(w!=null)
            {
               List<String> serverworldperms=w.getPerms();
               ret.addAll(serverworldperms);
            }
        }
        
        ret=BungeePerms.getInstance().getPermissionsManager().getResolver().simplify(ret);
        
		return ret;
	}
	
	public boolean has(String perm) 
	{
		List<String> perms=getEffectivePerms();
        
        Boolean has=BungeePerms.getInstance().getPermissionsManager().getResolver().has(perms, perm);
		
        return has!=null && has;
	}
	public boolean hasOnServer(String perm,ServerInfo server) 
	{
		List<String> perms=getEffectivePerms(server);
        
        Boolean has=BungeePerms.getInstance().getPermissionsManager().getResolver().has(perms, perm);
		
        return has!=null && has;
	}
	public boolean hasOnServerInWorld(String perm,ServerInfo server,String world) 
	{
		List<String> perms=getEffectivePerms(server,world);
		        
        Boolean has=BungeePerms.getInstance().getPermissionsManager().getResolver().has(perms, perm);
		
        return has!=null && has;
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
                    ServerInfo si=BungeeCord.getInstance().config.getServers().get(server);
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
                    ServerInfo si=BungeeCord.getInstance().config.getServers().get(lserver);
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
    }
    public void recalcPerms(String server,String world)
    {
        ServerInfo si=BungeeCord.getInstance().config.getServers().get(server);
        List<String> effperms=calcEffectivePerms(si,world);
        cachedPerms.put(si.getName().toLowerCase()+";"+world.toLowerCase(), effperms);
    }

    
    public List<BPPermission> getPermsWithOrigin(String server, String world)
    {
        List<BPPermission> ret=new ArrayList<>();
        
        //add inherited groups' perms
		for(Group g:BungeePerms.getInstance().getPermissionsManager().getGroups())
		{            
			if(inheritances.contains(g.getName()))
			{
                List<BPPermission> inheritgroupperms=g.getPermsWithOrigin(server, world);
                for(BPPermission perm:inheritgroupperms)
                {
                    ret.add(perm);
                }
            }
		}
		
		for(String s:perms)
		{
			BPPermission perm=new BPPermission(s,name,true,null,null);
            ret.add(perm);
		}
		
		//per server perms
        for(Map.Entry<String, Server> srv:servers.entrySet())
        {
            //check for server
            if(server!=null && !srv.getKey().equalsIgnoreCase(server))
            {
                continue;
            }
            
            List<String> perserverperms=srv.getValue().getPerms();
            for(String s:perserverperms)
            {
                BPPermission perm=new BPPermission(s,name,true,srv.getKey(),null);
                ret.add(perm);
            }

            //per server world perms
            for(Map.Entry<String, World> w:srv.getValue().getWorlds().entrySet())
            {
                //check for world
                if(world!=null && !w.getKey().equalsIgnoreCase(world))
                {
                    continue;
                }
                
                List<String> perserverworldperms=w.getValue().getPerms();
                for(String s:perserverworldperms)
                {
                    BPPermission perm=new BPPermission(s,name,true,srv.getKey(),w.getKey());
                    ret.add(perm);
                }
            }
        }
        
		return ret;
    }
    
    
    @Override
    public int compareTo(Group g)
    {
        return -Integer.compare(rank, g.getRank());
    }
}
