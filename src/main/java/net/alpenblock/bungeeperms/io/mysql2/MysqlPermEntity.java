package net.alpenblock.bungeeperms.io.mysql2;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MysqlPermEntity
{

    private String name;
    private EntityType type;
    private Map<String, List<ValueEntry>> data;

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
            if (res.wasNull())
            {
                server = null;
            }
            else
            {
                world = res.getString("world");
                if (res.wasNull())
                {
                    world = null;
                }
            }

            //add entry
            ValueEntry ve = new ValueEntry(value, server, world);

            List<ValueEntry> e = data.get(key);
            if (e == null)
            {
                e = new ArrayList<>();
                data.put(key, e);
            }

            e.add(ve);
        }
    }

    public String getName()
    {
        return name;
    }

    public EntityType getType()
    {
        return type;
    }

    public Map<String, List<ValueEntry>> getAllData()
    {
        return data;
    }

    public List<ValueEntry> getData(String type)
    {
        return data.get(type);
    }
}
