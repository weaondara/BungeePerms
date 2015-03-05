package net.alpenblock.bungeeperms.io.mysql2;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import net.alpenblock.bungeeperms.Mysql;

@Getter
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
            name = Mysql.unescape(res.getString("name"));
            type = EntityType.getByCode(res.getInt("type"));
        }

        res.beforeFirst();

        while (res.next())
        {
            String key = Mysql.unescape(res.getString("key"));
            String value = Mysql.unescape(res.getString("value"));
            String server = Mysql.unescape(res.getString("server"));
            String world = null;
            if (server != null)
            {
                world = Mysql.unescape(res.getString("world"));
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

        //todo: close res?
    }

    public List<ValueEntry> getData(String type)
    {
        return data.get(type);
    }
}
