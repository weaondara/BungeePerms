/* 
 * Copyright (C) 2020 wea_ondara
 *
 * BungeePerms is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BungeePerms is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.alpenblock.bungeeperms.io;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import net.alpenblock.bungeeperms.BungeePerms;
import net.alpenblock.bungeeperms.Config;

public class YAMLUUIDPlayerDB implements UUIDPlayerDB
{

    private Config uuidconf;
    @Getter
    @Setter
    private boolean autosave = true;

    public YAMLUUIDPlayerDB()
    {
        uuidconf = new Config(BungeePerms.getInstance().getPlugin(), "/uuidplayerdb.yml");
        uuidconf.load();
    }

    @Override
    public BackEndType getType()
    {
        return BackEndType.YAML;
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
        if (autosave)
            uuidconf.setStringAndSave(uuid.toString(), player);
        else
            uuidconf.setString(uuid.toString(), player);
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

    public void save()
    {
        uuidconf.save();
    }
}
