package net.alpenblock.bungeeperms;

import net.md_5.bungee.api.CommandSender;

public class Messages 
{
	public static String Error=Color.Error+"An error occured! Please report this error on https://github.com/weaondara/BungeePerms/issues . Please include exceptions from console.";
	public static String TooLessArgs=Color.Error+"Too less arguments!";
	public static String TooManyArgs=Color.Error+"Too many arguments!";
	public static String NoRights=Color.Error+"You don't have permission to do that!";
	
	public static void sendErrorMessage(CommandSender sender)
	{
		sender.sendMessage(Error);
	}
	public static void sendTooLessArgsMessage(CommandSender sender)
	{
		sender.sendMessage(TooLessArgs);
	}
	public static void sendTooManyArgsMessage(CommandSender sender)
	{
		sender.sendMessage(TooManyArgs);
	}
	public static void sendNoRightsMessage(CommandSender sender)
	{
		sender.sendMessage(NoRights);
	}
}
