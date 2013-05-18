package net.alpenblock.bungeeperms;

import java.util.ArrayList;
import java.util.List;

public class Group {
	String name;
	List<String> inheritances;
	List<String> perms;
	int rank;
	boolean isdefault;
	String display;
	String prefix;
	String suffix;
	public Group(String name, List<String> inheritances, List<String> perms, int rank, boolean isdefault, String display, String prefix, String suffix) 
	{
		this.isdefault = isdefault;
		this.name = name;
		this.perms = perms;
		this.rank = rank;
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
	public int getRank() {
		return rank;
	}
	public void setRank(int rank) {
		this.rank = rank;
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
		List<String> ret=new ArrayList<String>();
		for(Group g:BungeePerms.getInstance().getGroups())
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
}
