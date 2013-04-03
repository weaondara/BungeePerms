package net.alpenblock.bungeeperms;

import java.util.ArrayList;
import java.util.List;

public class Group {
	boolean isdefault;
	String name;
	List<String> perms;
	int rank;
	List<String> inheritances;
	public Group(boolean isdefault, String name, List<String> perms, int rank, List<String> inheritances) 
	{
		this.isdefault = isdefault;
		this.name = name;
		this.perms = perms;
		this.rank = rank;
		this.inheritances = inheritances;
	}
	public boolean isDefault() {
		return isdefault;
	}
	public String getName() {
		return name;
	}
	public List<String> getPerms() {
		return perms;
	}
	public int getRank() {
		return rank;
	}
	public List<String> getInheritance() {
		return inheritances;
	}
	//1:has perm
	//0:hasnt perm
	//-1: perm removed
	public int isPermittet(String perm)
	{
		int ret=0;
		for(int i=0;i<perms.size();i++)
		{
			if(perms.get(i).equalsIgnoreCase(perm))
			{
				ret=1;
			}
			else if(perms.get(i).equalsIgnoreCase("-"+perm))
			{
				ret=-1;
			}
			else
			{
				List<String> lpermsi=Statics.ToList(perms.get(i), ".");
				List<String> lperm=Statics.ToList(perm, ".");
				int index=0;
				try
				{
					while(lperm.get(index).equalsIgnoreCase(lpermsi.get(index)))
					{
						index++;
					}
					if(lpermsi.get(index).equalsIgnoreCase("*"))
					{
						ret=1;
					}
				}catch(Exception e){}
			}
		}
		return ret;
	}
	public List<String> getEffectivePerms(List<Group> allgroups)
	{
		List<String> ret=new ArrayList<String>();
		for(Group g:allgroups)
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
	public boolean has(List<Group> allgroups,String perm) 
	{
		List<String> perms=getEffectivePerms(allgroups);
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
