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
