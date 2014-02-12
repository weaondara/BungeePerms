package net.alpenblock.bungeeperms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.md_5.bungee.BungeeCord;

import net.md_5.bungee.api.config.ServerInfo;

/**
 * The Class Group.
 */
public class Group implements Comparable<Group>
{
    private Map<String,List<String>> cachedPerms;
    
	private String name;
	private List<String> inheritances;
	private List<String> perms;
	private Map<String,Server> servers;
	private int rank;
    private String ladder;
	private boolean isdefault;
	private String display;
	private String prefix;
	private String suffix;
	
	public Group(String name, List<String> inheritances, List<String> perms, Map<String,Server> servers, int rank, String ladder, boolean isdefault, String display, String prefix, String suffix) 
	{
        cachedPerms=new HashMap<>();
        
		this.isdefault = isdefault;
		this.name = name;
		this.perms = perms;
		this.servers = servers;
		this.rank = rank;
		this.ladder = ladder;
		this.inheritances = inheritances;
		this.display = display;
		this.prefix = prefix;
		this.suffix = suffix;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<String> getInheritances() {
		return inheritances;
	}
	public void setInheritances(List<String> inheritances) {
		this.inheritances = inheritances;
	}
	public List<String> getPerms() {
		return perms;
	}
	public void setPerms(List<String> perms) {
		this.perms = perms;
	}
	public Map<String, Server> getServers() {
		return servers;
	}
	public void setServers(Map<String, Server> servers) {
		this.servers = servers;
	}
	public int getRank() {
		return rank;
	}
	public void setRank(int rank) {
		this.rank = rank;
	}
    public String getLadder() {
        return ladder;
    }
    public void setLadder(String ladder) {
        this.ladder = ladder;
    }
	public boolean isDefault() {
		return isdefault;
	}
	public void setIsdefault(boolean isdefault) {
		this.isdefault = isdefault;
	}
	public String getDisplay() {
		return display;
	}
	public void setDisplay(String display) {
		this.display = display;
	}
	public String getPrefix() {
		return prefix;
	}
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	public String getSuffix() {
		return suffix;
	}
	public void setSuffix(String suffix) {
		this.suffix = suffix;
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
        List<String> effperms=cachedPerms.get(server.getName());
        if(effperms==null)
        {
            effperms=calcEffectivePerms(server);
            cachedPerms.put(server.getName(), effperms);
        }
        
        return effperms;
    }
    public List<String> getEffectivePerms(ServerInfo server,String world) 
	{
        List<String> effperms=cachedPerms.get(server.getName()+";"+world);
        if(effperms==null)
        {
            effperms=calcEffectivePerms(server,world);
            cachedPerms.put(server.getName()+";"+world, effperms);
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
				for(String perm:gperms)
				{
					boolean added=false;
					for(int i=0;i<ret.size();i++)
					{
						if(ret.get(i).equalsIgnoreCase(perm))
						{
							added=true;
							break;
						}
						else if(ret.get(i).equalsIgnoreCase("-"+perm))
						{
							ret.set(i,perm);
							added=true;
							break;
						}
						else if(perm.equalsIgnoreCase("-"+ret.get(i)))
						{
							ret.remove(i);
							added=true;
							break;
						}
					}
					if(!added)
					{
						ret.add(perm);
					}
				}
			}
		}
		for(String s:perms)
		{
			boolean added=false;
			for(int i=0;i<ret.size();i++)
			{
				if(ret.get(i).equalsIgnoreCase(s))
				{
					added=true;
					break;
				}
				else if(ret.get(i).equalsIgnoreCase("-"+s))
				{
					ret.set(i,s);
					added=true;
					break;
				}
				else if(s.equalsIgnoreCase("-"+ret.get(i)))
				{
					ret.remove(i);
					added=true;
					break;
				}
			}
			if(!added)
			{
				ret.add(s);
			}
		}
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
				for(String perm:gperms)
				{
					boolean added=false;
					for(int i=0;i<ret.size();i++)
					{
						if(ret.get(i).equalsIgnoreCase(perm))
						{
							added=true;
							break;
						}
						else if(ret.get(i).equalsIgnoreCase("-"+perm))
						{
							ret.set(i,perm);
							added=true;
							break;
						}
						else if(perm.equalsIgnoreCase("-"+ret.get(i)))
						{
							ret.remove(i);
							added=true;
							break;
						}
					}
					if(!added)
					{
						ret.add(perm);
					}
				}
			}
			
			//per server perms
//            Server srv=g.getServers().get(server.getName());
//			if(srv==null)
//			{
//				srv=new Server(server.getName(),new ArrayList<String>(),new HashMap<String,World>(),"","","");
//			}
//			List<String> serverperms=srv.getPerms();
//			for(String perm:serverperms)
//			{
//				boolean added=false;
//				for(int i=0;i<ret.size();i++)
//				{
//					if(ret.get(i).equalsIgnoreCase(perm))
//					{
//						added=true;
//						break;
//					}
//					else if(ret.get(i).equalsIgnoreCase("-"+perm))
//					{
//						ret.set(i,perm);
//						added=true;
//						break;
//					}
//					else if(perm.equalsIgnoreCase("-"+ret.get(i)))
//					{
//						ret.remove(i);
//						added=true;
//						break;
//					}
//				}
//				if(!added)
//				{
//					ret.add(perm);
//				}
//			}
		}
		
		for(String s:perms)
		{
			boolean added=false;
			for(int i=0;i<ret.size();i++)
			{
				if(ret.get(i).equalsIgnoreCase(s))
				{
					added=true;
					break;
				}
				else if(ret.get(i).equalsIgnoreCase("-"+s))
				{
					ret.set(i,s);
					added=true;
					break;
				}
				else if(s.equalsIgnoreCase("-"+ret.get(i)))
				{
					ret.remove(i);
					added=true;
					break;
				}
			}
			if(!added)
			{
				ret.add(s);
			}
		}
		
		//per server perms
        Server srv=servers.get(server.getName());
        if(srv==null)
        {
            srv=new Server(server.getName(),new ArrayList<String>(),new HashMap<String,World>(),"","","");
        }
        List<String> perserverperms=srv.getPerms();
		for(String perm:perserverperms)
		{
			boolean added=false;
			for(int i=0;i<ret.size();i++)
			{
				if(ret.get(i).equalsIgnoreCase(perm))
				{
					added=true;
					break;
				}
				else if(ret.get(i).equalsIgnoreCase("-"+perm))
				{
					ret.set(i,perm);
					added=true;
					break;
				}
				else if(perm.equalsIgnoreCase("-"+ret.get(i)))
				{
					ret.remove(i);
					added=true;
					break;
				}
			}
			if(!added)
			{
				ret.add(perm);
			}
		}
		
		return ret;
	}
	public List<String> calcEffectivePerms(ServerInfo server,String world) 
	{
		List<String> ret=new ArrayList<>();
		for(Group g:BungeePerms.getInstance().getPermissionsManager().getGroups())
		{
			if(inheritances.contains(g.getName()))
			{
				for(String perm:g.getEffectivePerms(server,world))
				{
					boolean added=false;
					for(int i=0;i<ret.size();i++)
					{
						if(ret.get(i).equalsIgnoreCase(perm))
						{
							added=true;
							break;
						}
						else if(ret.get(i).equalsIgnoreCase("-"+perm))
						{
							ret.set(i,perm);
							added=true;
							break;
						}
						else if(perm.equalsIgnoreCase("-"+ret.get(i)))
						{
							ret.remove(i);
							added=true;
							break;
						}
					}
					if(!added)
					{
						ret.add(perm);
					}
				}
			
                //per server perms
//                Server srv=g.getServers().get(server.getName());
//                if(srv==null)
//                {
//                    srv=new Server(server.getName(),new ArrayList<String>(),new HashMap<String,World>(),"","","");
//                }
//                List<String> serverperms=srv.getPerms();
//                for(String perm:serverperms)
//                {
//                    boolean added=false;
//                    for(int i=0;i<ret.size();i++)
//                    {
//                        if(ret.get(i).equalsIgnoreCase(perm))
//                        {
//                            added=true;
//                            break;
//                        }
//                        else if(ret.get(i).equalsIgnoreCase("-"+perm))
//                        {
//                            ret.set(i,perm);
//                            added=true;
//                            break;
//                        }
//                        else if(perm.equalsIgnoreCase("-"+ret.get(i)))
//                        {
//                            ret.remove(i);
//                            added=true;
//                            break;
//                        }
//                    }
//                    if(!added)
//                    {
//                        ret.add(perm);
//                    }
//                }
//                //per server world perms
//                World w=srv.getWorlds().get(world);
//                if(w==null)
//                {
//                    w=new World(server.getName(),new ArrayList<String>(),"","","");
//                }
//                List<String> serverworldperms=w.getPerms();
//                for(String perm:serverworldperms)
//                {
//                    boolean added=false;
//                    for(int i=0;i<ret.size();i++)
//                    {
//                        if(ret.get(i).equalsIgnoreCase(perm))
//                        {
//                            added=true;
//                            break;
//                        }
//                        else if(ret.get(i).equalsIgnoreCase("-"+perm))
//                        {
//                            ret.set(i,perm);
//                            added=true;
//                            break;
//                        }
//                        else if(perm.equalsIgnoreCase("-"+ret.get(i)))
//                        {
//                            ret.remove(i);
//                            added=true;
//                            break;
//                        }
//                    }
//                    if(!added)
//                    {
//                        ret.add(perm);
//                    }
//                }
            }
		}
		
		for(String s:perms)
		{
			boolean added=false;
			for(int i=0;i<ret.size();i++)
			{
				if(ret.get(i).equalsIgnoreCase(s))
				{
					added=true;
					break;
				}
				else if(ret.get(i).equalsIgnoreCase("-"+s))
				{
					ret.set(i,s);
					added=true;
					break;
				}
				else if(s.equalsIgnoreCase("-"+ret.get(i)))
				{
					ret.remove(i);
					added=true;
					break;
				}
			}
			if(!added)
			{
				ret.add(s);
			}
		}
		
		//per server perms
        Server srv=servers.get(server.getName());
        if(srv==null)
        {
            srv=new Server(server.getName(),new ArrayList<String>(),new HashMap<String,World>(),"","","");
        }
        List<String> perserverperms=srv.getPerms();
		for(String perm:perserverperms)
		{
			boolean added=false;
			for(int i=0;i<ret.size();i++)
			{
				if(ret.get(i).equalsIgnoreCase(perm))
				{
					added=true;
					break;
				}
				else if(ret.get(i).equalsIgnoreCase("-"+perm))
				{
					ret.set(i,perm);
					added=true;
					break;
				}
				else if(perm.equalsIgnoreCase("-"+ret.get(i)))
				{
					ret.remove(i);
					added=true;
					break;
				}
			}
			if(!added)
			{
				ret.add(perm);
			}
		}
        
        //per server world perms
        World w=srv.getWorlds().get(world);
        if(w==null)
        {
            w=new World(server.getName(),new ArrayList<String>(),"","","");
        }
        List<String> serverworldperms=w.getPerms();
        for(String perm:serverworldperms)
        {
            boolean added=false;
            for(int i=0;i<ret.size();i++)
            {
                if(ret.get(i).equalsIgnoreCase(perm))
                {
                    added=true;
                    break;
                }
                else if(ret.get(i).equalsIgnoreCase("-"+perm))
                {
                    ret.set(i,perm);
                    added=true;
                    break;
                }
                else if(perm.equalsIgnoreCase("-"+ret.get(i)))
                {
                    ret.remove(i);
                    added=true;
                    break;
                }
            }
            if(!added)
            {
                ret.add(perm);
            }
        }
		
		return ret;
	}
	
	public boolean has(String perm) 
	{
		List<String> perms=getEffectivePerms();
		boolean has=false;
		for(String p:perms)
		{
			if(p.equalsIgnoreCase(perm))
			{
				has=true;
			}
			else if(p.equalsIgnoreCase("-"+perm))
			{
				has=false;
			}
			else if(p.endsWith("*"))
			{
				List<String> lp=Statics.toList(p, ".");
				List<String> lperm=Statics.toList(perm, ".");
				int index=0;
				try
				{
					while(true)
					{
						if(index==0)
						{
							if( lperm.get(0).equalsIgnoreCase(lp.get(0))|
								lp.get(0).equalsIgnoreCase("-"+lperm.get(0)))
							{
								index++;
							}
							else
							{
								break;
							}
						}
						else
						{
							if(lperm.get(index).equalsIgnoreCase(lp.get(index)))
							{
								index++;
							}
							else
							{
								break;
							}
						}
					}
					if(lp.get(index).equalsIgnoreCase("*"))
					{
						has=!lp.get(0).startsWith("-");
					}
				}
				catch(Exception e){}
			}
		}
		return has;
	}
	public boolean hasOnServer(String perm,ServerInfo server) 
	{
		List<String> perms=getEffectivePerms(server);
		boolean has=false;
		for(String p:perms)
		{
			if(p.equalsIgnoreCase(perm))
			{
				has=true;
			}
			else if(p.equalsIgnoreCase("-"+perm))
			{
				has=false;
			}
			else if(p.endsWith("*"))
			{
				List<String> lp=Statics.toList(p, ".");
				List<String> lperm=Statics.toList(perm, ".");
				int index=0;
				try
				{
					while(true)
					{
						if(index==0)
						{
							if( lperm.get(0).equalsIgnoreCase(lp.get(0))|
								lp.get(0).equalsIgnoreCase("-"+lperm.get(0)))
							{
								index++;
							}
							else
							{
								break;
							}
						}
						else
						{
							if(lperm.get(index).equalsIgnoreCase(lp.get(index)))
							{
								index++;
							}
							else
							{
								break;
							}
						}
					}
					if(lp.get(index).equalsIgnoreCase("*"))
					{
						has=!lp.get(0).startsWith("-");
					}
				}
				catch(Exception e){}
			}
		}
		return has;
	}
	public boolean hasOnServerInWorld(String perm,ServerInfo server,String world) 
	{
		List<String> perms=getEffectivePerms(server,world);
		boolean has=false;
		for(String p:perms)
		{
			if(p.equalsIgnoreCase(perm))
			{
				has=true;
			}
			else if(p.equalsIgnoreCase("-"+perm))
			{
				has=false;
			}
			else if(p.endsWith("*"))
			{
				List<String> lp=Statics.toList(p, ".");
				List<String> lperm=Statics.toList(perm, ".");
				int index=0;
				try
				{
					while(true)
					{
						if(index==0)
						{
							if( lperm.get(0).equalsIgnoreCase(lp.get(0))|
								lp.get(0).equalsIgnoreCase("-"+lperm.get(0)))
							{
								index++;
							}
							else
							{
								break;
							}
						}
						else
						{
							if(lperm.get(index).equalsIgnoreCase(lp.get(index)))
							{
								index++;
							}
							else
							{
								break;
							}
						}
					}
					if(lp.get(index).equalsIgnoreCase("*"))
					{
						has=!lp.get(0).startsWith("-");
					}
				}
				catch(Exception e){}
			}
		}
		return has;
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
                    cachedPerms.put(si.getName(), effperms);
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
                    cachedPerms.put(si.getName(), effperms);
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
        cachedPerms.put(si.getName()+";"+world, effperms);
    }

    @Override
    public int compareTo(Group g)
    {
        return -Integer.compare(rank, g.getRank());
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
//                    if(perm.getOrigin().equalsIgnoreCase(g.getName()))
//                    {
                        ret.add(perm);
//                    }
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
}
