package net.alpenblock.bungeeperms.io;

import java.util.UUID;

public class NoneUUIDPlayerDB implements UUIDPlayerDB
{
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
}
