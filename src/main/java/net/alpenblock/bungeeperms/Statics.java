package net.alpenblock.bungeeperms;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

// TODO: Auto-generated Javadoc
/**
 * The Class Statics.
 */
public class Statics {
	
	/**
	 * Count sequences.
	 *
	 * @param s the s
	 * @param seq the seq
	 * @return the int
	 */
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
	
	/**
	 * Gets the full player name.
	 *
	 * @param s the s
	 * @param player the player
	 * @return the full player name
	 */
	public static String getFullPlayerName(ProxyServer s,String player)
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
	
	/**
	 * To list.
	 *
	 * @param s the s
	 * @param seperator the seperator
	 * @return the list
	 */
	public static List<String> toList(String s,String seperator)
	{
		List<String> l=new ArrayList<>();
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
	
	/**
	 * Arg alias.
	 *
	 * @param arg the arg
	 * @param aliases the aliases
	 * @return true, if successful
	 */
	public static boolean ArgAlias(String arg,String[] aliases)
	{
		for(int i=0;i<aliases.length;i++)
		{
			if(aliases[i].equalsIgnoreCase(arg))
			{
				return true;
			}
		}
		return false;
	}
    public static UUID parseUUID(String s)
    {
        try
        {
            return UUID.fromString(s);
        }
        catch(Exception e) {}
        
        if(s.length()==32)
        {
            s=s.substring(0,8)+"-"+
                    s.substring(8,12)+"-"+
                    s.substring(12,16)+"-"+
                    s.substring(16,20)+"-"+
                    s.substring(20,32)+"-";
            try
            {
                return UUID.fromString(s);
            }
            catch(Exception e) {}
        }
        
        return null;
    }
}
