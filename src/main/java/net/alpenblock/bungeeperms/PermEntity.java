package net.alpenblock.bungeeperms;

import java.util.Map;

public interface PermEntity extends Permable
{
    public String getName();
    public void setName(String name);
    public Map<String, Server> getServers();
    public Server getServer(String server);
}
