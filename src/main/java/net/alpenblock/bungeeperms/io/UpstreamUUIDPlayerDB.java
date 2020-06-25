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

import java.io.DataInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.SneakyThrows;
import net.alpenblock.bungeeperms.io.upstream.UpstreamIO;
import net.alpenblock.bungeeperms.platform.bukkit.BukkitPlugin;

public class UpstreamUUIDPlayerDB implements UUIDPlayerDB
{

    public UpstreamUUIDPlayerDB()
    {
    }

    @Override
    public BackEndType getType()
    {
        return BackEndType.UPSTREAM;
    }

    @Override
    public UUID getUUID(String player)
    {
        DataInputStream is = BukkitPlugin.getInstance().getUpstreamio().request("uuid:" + player);
        String suuid = UpstreamIO.readStringNull(is);
        if (suuid == null)
            return null;
        else
            return UUID.fromString(suuid);
    }

    @Override
    public String getPlayerName(UUID uuid)
    {
        DataInputStream is = BukkitPlugin.getInstance().getUpstreamio().request("name:" + uuid);
        return UpstreamIO.readStringNull(is);
    }

    @Override
    public void update(UUID uuid, String player)
    {
        //ignore
    }

    @Override
    @SneakyThrows
    public Map<UUID, String> getAll()
    {
        Map<UUID, String> ret = new HashMap<>();
        DataInputStream is = BukkitPlugin.getInstance().getUpstreamio().request("uuidplayerall");
        int len = is.readInt();
        for (int i = 0; i < len; i++)
        {
            UUID uuid = UUID.fromString(is.readUTF());
            String name = is.readUTF();
            ret.put(uuid, name);
        }
        return ret;
    }

    @Override
    public void clear()
    {
    }

    public void save()
    {
    }
}
