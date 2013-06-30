package net.alpenblock.bungeeperms;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.md_5.bungee.api.config.ServerInfo;

// TODO: Auto-generated Javadoc
/**
 * The Class Player.
 */
public class Player 
{
	
	/** The name. */
	private String name;
	
	/** The groups. */
	private List<Group> groups;
	
	/** The extraperms. */
	private List<String> extraperms;
	
	/** The serverperms. */
	Map<String, List<String>> serverperms;
	
	/**
	 * Instantiates a new player.
	 *
	 * @param name the name
	 * @param groups the groups
	 * @param extraperms the extraperms
	 * @param serverperms the serverperms
	 */
	public Player(String name, List<Group> groups, List<String> extraperms, Map<String, List<String>> serverperms) 
	{
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
				List<String> lp=Statics.ToList(p, ".");
				List<String> lperm=Statics.ToList(perm, ".");
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
				List<String> lp=Statics.ToList(p, ".");
				List<String> lperm=Statics.ToList(perm, ".");
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
	
	/**
	 * Gets the effective perms.
	 *
	 * @param server the server
	 * @return the effective perms
	 */
	public List<String> getEffectivePerms(ServerInfo server)
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
}
