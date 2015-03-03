package net.alpenblock.bungeeperms;

import net.alpenblock.bungeeperms.platform.Sender;

public class Messages
{

    public static String Error = Color.Error + "An error occured! Please report this error on https://github.com/weaondara/BungeePerms/issues . Please include exceptions from console.";
    public static String TooLessArgs = Color.Error + "Too less arguments!";
    public static String TooManyArgs = Color.Error + "Too many arguments!";
    public static String NoRights = Color.Error + "You don't have permission to do that!";

    public static void sendErrorMessage(Sender sender)
    {
        sender.sendMessage(Error);
    }

    public static void sendTooLessArgsMessage(Sender sender)
    {
        sender.sendMessage(TooLessArgs);
    }

    public static void sendTooManyArgsMessage(Sender sender)
    {
        sender.sendMessage(TooManyArgs);
    }

    public static void sendNoRightsMessage(Sender sender)
    {
        sender.sendMessage(NoRights);
    }
}
