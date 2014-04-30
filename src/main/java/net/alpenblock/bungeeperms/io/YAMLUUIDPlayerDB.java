package net.alpenblock.bungeeperms.io;

import java.util.UUID;
import net.alpenblock.bungeeperms.BungeePerms;
import net.alpenblock.bungeeperms.Config;

public class YAMLUUIDPlayerDB implements UUIDPlayerDB
{
    private Config uuidconf;
    
    public YAMLUUIDPlayerDB()
    {
        uuidconf=new Config(BungeePerms.getInstance(), "uuidplayerdb.yml");
        uuidconf.load();
    }
    
    @Override
    public UUID getUUID(String player)
    {
        UUID ret=null;
        
        for(String uuid:uuidconf.getSubNodes(""))
        {
            String p=uuidconf.getString(uuid, "");
            if(p.equalsIgnoreCase(player))
            {
                ret=UUID.fromString(uuid);
            }
        }
        
        return ret;
    }
    @Override
    public String getPlayerName(UUID uuid)
    {
        String ret=null;
        
        for(String suuid:uuidconf.getSubNodes(""))
        {
            if(suuid.equalsIgnoreCase(uuid.toString()))
            {
                ret=uuidconf.getString(suuid, "");
            }
        }
        
        return ret;
    }
    @Override
    public void update(UUID uuid, String player)
    {
        for(String suuid:uuidconf.getSubNodes(""))
        {
            if(suuid.equalsIgnoreCase(uuid.toString()) || uuidconf.getString(suuid, "").equalsIgnoreCase(player))
            {
                uuidconf.deleteNode(suuid);
            }
        }
        uuidconf.setString(uuid.toString(), player);
    }
}
