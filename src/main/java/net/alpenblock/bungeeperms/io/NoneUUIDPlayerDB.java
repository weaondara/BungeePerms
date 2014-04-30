package net.alpenblock.bungeeperms.io;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NoneUUIDPlayerDB implements UUIDPlayerDB
{
    @Override
    public UUIDPlayerDBType getType()
    {
        return UUIDPlayerDBType.None;
    }
    
    @Override
    public UUID getUUID(String player)
    {
        return null;
    }
    @Override
    public String getPlayerName(UUID uuid)
    {
        return null;
    }
    @Override
    public void update(UUID uuid, String player)
    {
    }

    @Override
    public Map<UUID, String> getAll()
    {
        return new HashMap<>();
    }

    @Override
    public void clear()
    {
    }
}
