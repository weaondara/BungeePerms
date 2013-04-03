package net.alpenblock.bungeeperms;

import java.util.ArrayList;
import java.util.List;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class Statics {
	public static int countSequences(String s, String seq)
	{
		int count=0;
		for(int i=0;i<s.length()-seq.length()+1;i++)
		{
			if(s.substring(i, i+seq.length()).equalsIgnoreCase(seq))
			{
				count++;
			}
		}
		return count;
	}
	public static String getFullPlayerName(BungeeCord s,String player)
	{
		ProxiedPlayer p = s.getPlayer(player);
		if(p!=null) 
		{
			for(ProxiedPlayer pp:s.getPlayers())
			{
				if(pp.getName().startsWith(player))
				{
					return pp.getName();
				}
			}
			return p.getName();
		} 
		else 
		{
			return player;
		}
	}
	public static List<String> ToList(String s,String seperator)
	{
		List<String> l=new ArrayList<String>();
		String ls="";
		for(int i=0;i<(s.length()-seperator.length())+1;i++)
		{
			if(s.substring(i, i+seperator.length()).equalsIgnoreCase(seperator))
			{
				l.add(ls);
				ls="";
				i=i+seperator.length()-1;
			}
			else
			{
				ls+=s.substring(i,i+1);
			}
		}
		if(ls.length()>0)
		{		
			l.add(ls);
		}
		return l;
	}

}
