package net.alpenblock.bungeeperms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.md_5.bungee.BungeeCord;

import net.md_5.bungee.api.config.ServerInfo;

public class User
{
    private Map<String,List<String>> cachedPerms;
    
	private String name;
	private List<Group> groups;
	private List<String> extraperms;
	private Map<String, List<String>> serverperms;
    private Map<String, Map<String, List<String>>> serverworldperms;
	
	public User(String name, List<Group> groups, List<String> extraperms, Map<String, List<String>> serverperms, Map<String, Map<String, List<String>>> serverworldperms) 
	{
        cachedPerms=new HashMap<>();
        
		this.name = name;
		this.groups = groups;
		this.extraperms = extraperms;
		this.serverperms = serverperms;
		this.serverworldperms = serverworldperms;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<Group> getGroups() {
		return groups;
	}
	public void setGroups(List<Group> groups) {
		this.groups = groups;
	}
	public List<String> getExtraperms() {
		return extraperms;
	}
	public void setExtraperms(List<String> extraperms) {
		this.extraperms = extraperms;
	}
	public Map<String, List<String>> getServerPerms() {
		return serverperms;
	}
	public void setServerPerms(Map<String, List<String>> serverperms) {
		this.serverperms = serverperms;
	}
    public Map<String, Map<String, List<String>>> getServerWorldPerms() {
        return serverworldperms;
    }
    public void setServerWorldPerms(Map<String, Map<String, List<String>>> serverworldperms) {
        this.serverworldperms = serverworldperms;
    }
	
	public boolean hasPerm(String perm)
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
	public boolean hasPermOnServer(String perm, ServerInfo server) 
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
							if( lperm.get(0).equalsIgnoreCase(lp.get(0))| lp.get(0).equalsIgnoreCase("-"+lperm.get(0)))
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
    public boolean hasPermOnServerInWorld(String perm, ServerInfo server, String world) 
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
							if( lperm.get(0).equalsIgnoreCase(lp.get(0))| lp.get(0).equalsIgnoreCase("-"+lperm.get(0)))
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
		for(String s:extraperms)
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
		for(Group g:groups)
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
			
			//per server perms
//			Server srv=g.getServers().get(server.getName());
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
		
		
		for(String s:extraperms)
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
		List<String> perserverperms=serverperms.get(server.getName().toLowerCase());
		if(perserverperms==null)
		{
			perserverperms=new ArrayList<>();
		}
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
	public List<String> calcEffectivePerms(ServerInfo server, String world)
	{
		List<String> ret=new ArrayList<>();
		for(Group g:groups)
		{
			List<String> gperms=g.getEffectivePerms(server,world);
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
			
			//per server perms
//			Server srv=g.getServers().get(server.getName());
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
//            
//            //per server world perms
//            World w=srv.getWorlds().get(world);
//            if(w==null)
//            {
//                w=new World(server.getName(),new ArrayList<String>(),"","","");
//            }
//            List<String> serverworldperms=w.getPerms();
//            for(String perm:serverworldperms)
//            {
//                boolean added=false;
//                for(int i=0;i<ret.size();i++)
//                {
//                    if(ret.get(i).equalsIgnoreCase(perm))
//                    {
//                        added=true;
//                        break;
//                    }
//                    else if(ret.get(i).equalsIgnoreCase("-"+perm))
//                    {
//                        ret.set(i,perm);
//                        added=true;
//                        break;
//                    }
//                    else if(perm.equalsIgnoreCase("-"+ret.get(i)))
//                    {
//                        ret.remove(i);
//                        added=true;
//                        break;
//                    }
//                }
//                if(!added)
//                {
//                    ret.add(perm);
//                }
//            }
		}
		
		
		for(String s:extraperms)
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
		List<String> perserverperms=serverperms.get(server.getName().toLowerCase());
		if(perserverperms==null)
		{
			perserverperms=new ArrayList<>();
		}
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
        Map<String,List<String>> serverperms=serverworldperms.get(server.getName().toLowerCase());
        if(serverperms==null)
        {
            serverperms=new HashMap<>();
        }
        List<String> serverworldperms=serverperms.get(world.toLowerCase());
        if(serverworldperms==null)
        {
            serverworldperms=new ArrayList<>();
        }
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
    
    public boolean isNothingSpecial() 
    {
        for(Group g:groups)
        {
            if(!g.isDefault())
            {
                return false;
            }
        }
        return serverworldperms.isEmpty()&serverperms.isEmpty()&extraperms.isEmpty();
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
        for(Map.Entry<String, List<String>> srv:serverperms.entrySet())
        {
            //check for server
            if(server!=null && !srv.getKey().equalsIgnoreCase(server))
            {
                continue;
            }
            
            List<String> perserverperms=srv.getValue();
            for(String s:perserverperms)
            {
                BPPermission perm=new BPPermission(s,name,false,srv.getKey(),null);
                ret.add(perm);

                //per server world perms
                for(Map.Entry<String, List<String>> w:serverworldperms.get(srv.getKey()).entrySet())
                {
                    //check for world
                    if(world!=null && !w.getKey().equalsIgnoreCase(world))
                    {
                        continue;
                    }
                    
                    
                    List<String> perserverworldperms=w.getValue();
                    for(String s2:perserverworldperms)
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
