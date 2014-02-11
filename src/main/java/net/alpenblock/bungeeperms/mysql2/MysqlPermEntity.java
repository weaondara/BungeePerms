package net.alpenblock.bungeeperms.mysql2;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MysqlPermEntity 
{
    private String name;
    private boolean isGroup;
    private Map<String,List<ValueEntry>> data;
    
    
    public MysqlPermEntity(ResultSet res) throws SQLException
    {
        data=new HashMap<>();
        load(res);
    }
    
    private void load(ResultSet res) throws SQLException
    {
        if(res.first())
        {
            name=res.getString("name");
            isGroup=res.getBoolean("isgroup");
        }
        
        res.beforeFirst();
        
        while(res.next())
        {
            String key=res.getString("key");
            String value=res.getString("key");
            String server=res.getString("server");
            String world=null;
            if(res.wasNull())
            {
                server=null;
            }
            else
            {
                world=res.getString("world");
                if(res.wasNull())
                {
                    world=null;
                }
            }
            
            //add entry
            ValueEntry ve=new ValueEntry(value,server,world);
            
            List<ValueEntry> e = data.get(key);
            if(e==null)
            {
                e=new ArrayList<>();
                data.put(key, e);
            }
            
            e.add(ve);
        }
    }

    public String getName() {
        return name;
    }

    public boolean isGroup() {
        return isGroup;
    }

    public Map<String,List<ValueEntry>> getAllData() {
        return data;
    }
    
    public List<ValueEntry> getData(String type)
    {
        return data.get(type);
    }
}
