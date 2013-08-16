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
	 * Gets the inheritances.
	 *
	 * @return the inheritances
	 */
	public List<String> getInheritances() {
		return inheritances;
	}
	
	/**
	 * Sets the inheritances.
	 *
	 * @param inheritances the new inheritances
	 */
	public void setInheritances(List<String> inheritances) {
		this.inheritances = inheritances;
	}
	
	/**
	 * Gets the perms.
	 *
	 * @return the perms
	 */
	public List<String> getPerms() {
		return perms;
	}
	
	/**
	 * Sets the perms.
	 *
	 * @param perms the new perms
	 */
	public void setPerms(List<String> perms) {
		this.perms = perms;
	}
	
	/**
	 * Gets the servers.
	 *
	 * @return the servers
	 */
	public Map<String, Server> getServers() {
		return servers;
	}
	
	/**
	 * Sets the servers.
	 *
	 * @param serverperms the servers
	 */
	public void setServers(Map<String, Server> servers) {
		this.servers = servers;
	}
	
	/**
	 * Gets the rank.
	 *
	 * @return the rank
	 */
	public int getRank() {
		return rank;
	}
	
	/**
	 * Sets the rank.
	 *
	 * @param rank the new rank
	 */
	public void setRank(int rank) {
		this.rank = rank;
	}

    /**
     * @return the ladder
     */
    public String getLadder() {
        return ladder;
    }

    /**
     * @param ladder the ladder to set
     */
    public void setLadder(String ladder) {
        this.ladder = ladder;
    }
	
	/**
	 * Checks if is default.
	 *
	 * @return true, if is default
	 */
	public boolean isDefault() {
		return isdefault;
	}
	
	/**
	 * Sets the isdefault.
	 *
	 * @param isdefault the new isdefault
	 */
	public void setIsdefault(boolean isdefault) {
		this.isdefault = isdefault;
	}
	
	/**
	 * Gets the display.
	 *
	 * @return the display
	 */
	public String getDisplay() {
		return display;
	}
	
	/**
	 * Sets the display.
	 *
	 * @param display the new display
	 */
	public void setDisplay(String display) {
		this.display = display;
	}
	
	/**
	 * Gets the prefix.
	 *
	 * @return the prefix
	 */
	public String getPrefix() {
		return prefix;
	}
	
	/**
	 * Sets the prefix.
	 *
	 * @param prefix the new prefix
	 */
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	
	/**
	 * Gets the suffix.
	 *
	 * @return the suffix
	 */
	public String getSuffix() {
		return suffix;
	}
	
	/**
	 * Sets the suffix.
	 *
	 * @param suffix the new suffix
	 */
	public void setSuffix(String suffix) {
		this.suffix = suffix;
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
		for(Group g:BungeePerms.getInstance().getPermissionsManager().getGroups())
		{
			if(inheritances.contains(g.getName()))
			{
				List<String> gperms=g.getPerms();
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
				List<String> gperms=g.getPerms();
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
            srv=new Server(server.getName(),new ArrayList<String>(),"","","");
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
	
	/**
	 * Checks for.
	 *
	 * @param perm the perm
	 * @return true, if successful
	 */
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
	
	/**
	 * Checks for on server.
	 *
	 * @param perm the perm
	 * @param server the server
	 * @return true, if successful
	 */
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
    
    @Override
    public int compareTo(Group g)
    {
        return -Integer.compare(rank, g.getRank());
    }

}
