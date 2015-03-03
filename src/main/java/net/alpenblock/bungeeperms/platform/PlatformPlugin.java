package net.alpenblock.bungeeperms.platform;

import java.io.File;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public interface PlatformPlugin 
{
    public String getPluginName();
    public String getVersion();
    public String getAuthor();
    public String getPluginFolderPath();
    public File getPluginFolder();
    public Sender getPlayer(String name);
    public Sender getPlayer(UUID uuid);
    public Sender getConsole();
    public List<Sender> getPlayers();
    public Logger getLogger();
}
