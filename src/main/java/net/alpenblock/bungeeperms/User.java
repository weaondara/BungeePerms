package net.alpenblock.bungeeperms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.md_5.bungee.BungeeCord;

import net.md_5.bungee.api.config.ServerInfo;

public class User implements Player
{
    private Map<String,List<String>> cachedPerms;
    
	private String name;
	private List<Group> groups;
	private List<String> extraperms;
	private Map<String, List<String>> serverperms;
	
	public User(String name, List<Group> groups, List<String> extraperms, Map<String, List<String>> serverperms) 
	{
        cachedPerms=new HashMap<>();
        
		this.name = name;
		this.groups = groups;
		this.extraperms = extraperms;
		this.serverperms = serverperms;
	}
	
	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Sets the name.
	 *
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Gets the groups.
	 *
	 * @return the groups
	 */
	public List<Group> getGroups() {
		return groups;
	}
	
	/**
	 * Sets the groups.
	 *
	 * @param groups the new groups
	 */
	public void setGroups(List<Group> groups) {
		this.groups = groups;
	}
	
	/**
	 * Gets the extraperms.
	 *
	 * @return the extraperms
	 */
	public List<String> getExtraperms() {
		return extraperms;
	}
	
	/**
	 * Sets the extraperms.
	 *
	 * @param extraperms the new extraperms
	 */
	public void setExtraperms(List<String> extraperms) {
		this.extraperms = extraperms;
	}
	
	/**
	 * Gets the server perms.
	 *
	 * @return the server perms
	 */
	public Map<String, List<String>> getServerPerms() {
		return serverperms;
	}
	
	/**
	 * Sets the server perms.
	 *
	 * @param serverperms the serverperms
	 */
	public void setServerPerms(Map<String, List<String>> serverperms) {
		this.serverperms = serverperms;
	}
	
	/**
	 * Checks for perm.
	 *
	 * @param perm the perm
	 * @return true, if successful
	 */
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
	
	/**
	 * Checks for perm on server.
	 *
	 * @param perm the perm
	 * @param server the server
	 * @return true, if successful
	 */
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
	
    /**
	 * Gets the effective perms.
	 *
	 * @return the effective perms
	 */
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
    /**
	 * Gets the effective perms.
	 *
	 * @param server the server
	 * @return the effective perms
	 */
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
			Server srv=g.getServers().get(server.getName());
			if(srv==null)
			{
				srv=new Server(server.getName(),new ArrayList<String>(),"","","");
			}
			List<String> serverperms=srv.getPerms();
			for(String perm:serverperms)
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
		
		//per server perms
		List<String> perserverperms=serverperms.get(server.getName());
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

    public void recalcPerms() 
    {
        List<String> effperms=calcEffectivePerms();
        cachedPerms.put("global", effperms);
    }
    public void recalcPerms(String server)
    {
        ServerInfo si=BungeeCord.getInstance().config.getServers().get(server);
        List<String> effperms=calcEffectivePerms(si);
        cachedPerms.put(si.getName(), effperms);
    }
    
    public synchronized void recalcAllPerms() 
    {
        for(String server:cachedPerms.keySet())
        {
            if(server.equalsIgnoreCase("global"))
            {
                recalcPerms();
            }
            else
            {
                recalcPerms(server);
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
        return serverperms.isEmpty()&extraperms.isEmpty();
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
}
