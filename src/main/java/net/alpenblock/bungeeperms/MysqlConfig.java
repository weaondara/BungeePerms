package net.alpenblock.bungeeperms;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MysqlConfig
{

    private final Mysql mysql;
    private final String table;

    private final Map<String, List<String>> data;

    public MysqlConfig(Mysql m, String table)
    {
        mysql = m;
        this.table = table;
        data = new HashMap<>();
    }

    public void load()
    {
        createTable();

        ResultSet res = null;
        try
        {
            res = mysql.returnQuery("SELECT `key`,`value` FROM `" + table + "` ORDER BY id ASC");
            fromResult(res);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                res.close();
            }
            catch (Exception e)
            {
            }
        }
    }

    public void fromResult(ResultSet res)
    {
        try
        {
            while (res.next())
            {
                String key = Mysql.unescape(res.getString("key"));
                String val = Mysql.unescape(res.getString("value"));
                List<String> values = data.get(key);
                if (values == null)
                {
                    values = new ArrayList<>();
                    data.put(key, values);
                }
                values.add(val);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void createTable()
    {
        if (!mysql.tableExists(table))
        {
            String t = "CREATE TABLE `" + table + "` ("
                    + "`id` INT( 64 ) NOT NULL AUTO_INCREMENT PRIMARY KEY ,"
                    + "`key` VARCHAR( 256 ) NOT NULL ,"
                    + "`value` VARCHAR( 256 ) NOT NULL "
                    + ") ENGINE = MYISAM ;";
            mysql.runQuery(t);
        }
    }

    public String getString(String key, String def)
    {

        if (data.containsKey(key))
        {
            return data.get(key).get(0);
        }
        else
        {
            List<String> list = new ArrayList<>();
            list.add(def);
            data.put(key, list);
            save(key, list);
            return def;
        }
    }

    public int getInt(String key, int def)
    {

        if (data.containsKey(key))
        {
            return Integer.parseInt(data.get(key).get(0));
        }
        else
        {
            List<String> list = new ArrayList<>();
            list.add(String.valueOf(def));
            data.put(key, list);
            save(key, list);
            return def;
        }

    }

    public boolean getBoolean(String key, boolean def)
    {

        if (data.containsKey(key))
        {
            return Boolean.parseBoolean(data.get(key).get(0));
        }
        else
        {
            List<String> list = new ArrayList<>();
            list.add(String.valueOf(def));
            data.put(key, list);
            save(key, list);
            return def;
        }

    }

    public <T extends Enum> T getEnumValue(String key, T def)
    {
        if (data.containsKey(key))
        {
            String s = getString(key, def.name());
            T[] constants = (T[]) def.getDeclaringClass().getEnumConstants();
            for (T constant : constants)
            {
                if (constant.name().equals(s))
                {
                    return constant;
                }
            }
            return def;
        }
        else
        {
            List<String> list = new ArrayList<>();
            list.add(def.name());
            data.put(key, list);
            save(key, list);
            return def;
        }
    }

    public List<String> getListString(String key, List<String> def)
    {

        if (data.containsKey(key))
        {
            return data.get(key);
        }
        else
        {
            data.put(key, def);
            save(key, def);
            return def;
        }

    }

    public double getDouble(String key, double def)
    {
        if (data.containsKey(key))
        {
            return Double.parseDouble(data.get(key).get(0));
        }
        else
        {
            List<String> list = new ArrayList<>();
            list.add(String.valueOf(def));
            data.put(key, list);
            save(key, list);
            return def;
        }
    }

    public void setString(String key, String val)
    {
        List<String> list = new ArrayList<>();
        list.add(val);
        save(key, list);
    }

    public void setInt(String key, int val)
    {
        List<String> list = new ArrayList<>();
        list.add(String.valueOf(val));
        save(key, list);
    }

    public void setBool(String key, boolean val)
    {
        List<String> list = new ArrayList<>();
        list.add(String.valueOf(val));
        save(key, list);
    }

    public void setListString(String key, List<String> val)
    {
        save(key, val);
    }

    public List<String> getSubNodes(String node)
    {
        List<String> ret = new ArrayList<>();
        for (String key : data.keySet())
        {
            if (isSubNode(node, key))
            {
                String rest = key.substring(node.length() + 1);
                List<String> list = Statics.toList(rest, ".");
                if (list.size() > 0)
                {
                    String subnode = list.get(0);
                    if (!ret.contains(subnode))
                    {
                        ret.add(subnode);
                    }
                }
            }
        }
        return ret;
    }

    private boolean isSubNode(String node, String subnode)
    {
        //is same node?
        if (subnode.equals(node))
        {
            return false;
        }

        //same beginning?
        if (!subnode.startsWith(node))
        {
            return false;
        }

        //starts with same part -> check for dot
        String dotkeyrest = subnode.substring(node.length());
        if (dotkeyrest.charAt(0) != ".".charAt(0))
        {
            return false;
        }
        return true;
    }

    public void deleteNode(String node)
    {
        for (String key : data.keySet())
        {
            if (isSubNode(key, node))
            {
                data.remove(key);
                String delq = "DELETE FROM " + table + " WHERE `key`='" + key + "'";
                mysql.runQuery(delq);
            }
        }
    }

    public boolean keyExists(String node)
    {
        return !getSubNodes(node).isEmpty();
    }

    private void save(String key, List<String> values)
    {
        //delete all entries with the given key
        String delq = "DELETE FROM `" + table + "` WHERE `key`='" + key + "'";
        mysql.runQuery(delq);

        //add values
        for (String val : values)
        {
            String insq = "INSERT INTO `" + table + "` (`key`,`value`) VALUES('" + Mysql.escape(key) + "','" + Mysql.escape(val) + "')";
            mysql.runQuery(insq);
        }
    }

    public void clearTable(String table)
    {
        String q = "TRUNCATE `" + table + "`";
        mysql.runQuery(q);
    }
}
