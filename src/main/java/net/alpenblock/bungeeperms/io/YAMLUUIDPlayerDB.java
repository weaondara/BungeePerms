package net.alpenblock.bungeeperms.io;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.alpenblock.bungeeperms.BungeePerms;
import net.alpenblock.bungeeperms.Config;

public class YAMLUUIDPlayerDB implements UUIDPlayerDB
{

    private Config uuidconf;

    public YAMLUUIDPlayerDB()
    {
        uuidconf = new Config(BungeePerms.getInstance().getPlugin(), "/uuidplayerdb.yml");
        uuidconf.load();
    }

    @Override
    public UUIDPlayerDBType getType()
    {
        return UUIDPlayerDBType.YAML;
    }

    @Override
    public UUID getUUID(String player)
    {
        UUID ret = null;

        for (String uuid : uuidconf.getSubNodes(""))
        {
            String p = uuidconf.getString(uuid, "");
            if (p.equalsIgnoreCase(player))
            {
                ret = UUID.fromString(uuid);
            }
        }

        return ret;
    }

    @Override
    public String getPlayerName(UUID uuid)
    {
        String ret = null;

        for (String suuid : uuidconf.getSubNodes(""))
        {
            if (suuid.equalsIgnoreCase(uuid.toString()))
            {
                ret = uuidconf.getString(suuid, "");
            }
        }

        return ret;
    }

    @Override
    public void update(UUID uuid, String player)
    {
        for (String suuid : uuidconf.getSubNodes(""))
        {
            if (suuid.equalsIgnoreCase(uuid.toString()) || uuidconf.getString(suuid, "").equalsIgnoreCase(player))
            {
                uuidconf.deleteNode(suuid);
            }
        }
        uuidconf.setStringAndSave(uuid.toString(), player);
    }

    @Override
    public Map<UUID, String> getAll()
    {
        Map<UUID, String> ret = new HashMap<>();

        for (String suuid : uuidconf.getSubNodes(""))
        {
            ret.put(UUID.fromString(suuid), uuidconf.getString(suuid, ""));
        }

        return ret;
    }

    @Override
    public void clear()
    {
        new File(BungeePerms.getInstance().getPlugin().getPluginFolder(), "/uuidplayerdb.yml").delete();
        uuidconf = new Config(BungeePerms.getInstance().getPlugin(), "/uuidplayerdb.yml");
        uuidconf.load();
    }
}
