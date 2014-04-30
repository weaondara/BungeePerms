package net.alpenblock.bungeeperms.io;

import java.util.Map;
import java.util.UUID;

public interface UUIDPlayerDB 
{
    public UUID getUUID(String player);
    public String getPlayerName(UUID uuid);
    public void update(UUID uuid, String player);
    
    public Map<UUID, String> getAll();
    public void clear();

    public UUIDPlayerDBType getType();
}
