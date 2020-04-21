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
package net.alpenblock.bungeeperms.io.mysql2;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MysqlPermEntity
{

    private String name;
    private EntityType type;
    private final Map<String, List<ValueEntry>> data;

    public MysqlPermEntity(ResultSet res) throws SQLException
    {
        data = new HashMap<>();
        load(res);
    }

    private void load(ResultSet res) throws SQLException
    {
        if (res.first())
        {
            name = res.getString("name");
            type = EntityType.getByCode(res.getInt("type"));
        }

        res.beforeFirst();

        while (res.next())
        {
            String key = res.getString("key");
            String value = res.getString("value");
            String server = res.getString("server");
            String world = null;
            if (server != null)
                world = res.getString("world");
            Timestamp start = res.getTimestamp("timedstart");
            Integer dur = res.getInt("timedduration");
            if (res.wasNull())
                dur = null;
            if (start == null || dur == null)
            {
                start = null;
                dur = null;
            }

            //add entry
            ValueEntry ve = new ValueEntry(value, server, world, start, dur);

            List<ValueEntry> e = data.get(key);
            if (e == null)
            {
                e = new ArrayList<>();
                data.put(key, e);
            }

            e.add(ve);
        }

        //close res? -> no will be done
    }

    public List<ValueEntry> getData(String type)
    {
        return data.get(type);
    }
}
