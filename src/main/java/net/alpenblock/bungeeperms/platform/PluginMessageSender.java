package net.alpenblock.bungeeperms.platform;

public interface PluginMessageSender 
{
    public void sendPluginMessage(String target, String channel, String msg);
}
