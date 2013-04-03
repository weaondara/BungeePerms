package net.alpenblock.bungeeperms;

import net.md_5.bungee.api.ChatColor;

public class Color {
	public static final String ABT = ChatColor.RED+"Alpen"+ChatColor.WHITE+"block"+ChatColor.RED+"Tools"+ChatColor.RESET;
	public static ChatColor Text=ChatColor.GRAY;
	public static ChatColor Tool=ChatColor.RED;
	public static ChatColor User=ChatColor.DARK_AQUA;
	public static ChatColor Success=ChatColor.GREEN;
	public static ChatColor Error=ChatColor.DARK_RED;
	public static ChatColor Activated=ChatColor.GREEN;
	public static ChatColor Deactivated=ChatColor.RED;
	public static ChatColor Value=ChatColor.GOLD;
	public static ChatColor Message=ChatColor.DARK_GREEN;
	public static ChatColor Severe=ChatColor.RED;
	public static ChatColor Fatal=ChatColor.DARK_RED;
	public static String Link=ChatColor.DARK_BLUE+""+ChatColor.UNDERLINE;
	public static String Text(String text,boolean reset)
	{
		return Text+text+(reset?ChatColor.RESET:"");
	}
	public static String Tool(String tool,boolean reset)
	{
		return Tool+tool+(reset?ChatColor.RESET:"");
	}
	public static String User(String user,boolean reset)
	{
		return User+user+(reset?ChatColor.RESET:"");
	}
	public static String Success(String success,boolean reset)
	{
		return Success+success+(reset?ChatColor.RESET:"");
	}
	public static String Error(String error,boolean reset)
	{
		return Error+error+(reset?ChatColor.RESET:"");
	}
	public static String Activated(String activated,boolean reset)
	{
		return Activated+activated+(reset?ChatColor.RESET:"");
	}
	public static String Deactivated(String deactivated,boolean reset)
	{
		return Deactivated+deactivated+(reset?ChatColor.RESET:"");
	}
	public static String Value(String value,boolean reset)
	{
		return Value+value+(reset?ChatColor.RESET:"");
	}
	public static String ColorString(String s,ChatColor cc,boolean reset)
	{
		return cc+s+(reset?ChatColor.RESET:"");
	}
	public static String ColorString(int s,ChatColor cc,boolean reset)
	{
		return cc+String.valueOf(s)+(reset?ChatColor.RESET:"");
	}
}