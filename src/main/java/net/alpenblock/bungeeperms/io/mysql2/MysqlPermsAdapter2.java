package net.alpenblock.bungeeperms.io.mysql2;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import net.alpenblock.bungeeperms.Mysql;

public class MysqlPermsAdapter2
{

    private Mysql mysql;
    private String table;

    public MysqlPermsAdapter2(Mysql m, String table)
    {
        mysql = m;
        this.table = table;
    }

    public void createTable()
    {
        if (!mysql.tableExists(table))
        {
            String t = "CREATE TABLE `" + table + "` ("
                    + "`id` INT( 64 ) NOT NULL AUTO_INCREMENT PRIMARY KEY ,"
                    + "`name` VARCHAR( 64 ) NOT NULL ,"
                    + "`type` TINYINT( 2 ) NOT NULL ,"
                    + "`key` VARCHAR( 256 ) NOT NULL, "
                    + "`value` VARCHAR( 256 ) NOT NULL, "
                    + "`server` VARCHAR( 64 ), "
                    + "`world` VARCHAR( 64 ) "
                    + ") ENGINE = MYISAM ;";
            mysql.runQuery(t);
        }
    }

    public List<String> getGroups()
    {
        List<String> groups = new ArrayList<>();

        ResultSet res = null;
        try
        {
            res = mysql.returnQuery("SELECT DISTINCT `name` FROM `" + table + "` WHERE `type`=" + EntityType.Group.getCode() + " ORDER BY id ASC");
            while (res.next())
            {
                String name = Mysql.unescape(res.getString("name"));
                groups.add(name);
            }
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

        return groups;
    }

    public List<String> getUsers()
    {
        List<String> groups = new ArrayList<>();

        ResultSet res = null;
        try
        {
            res = mysql.returnQuery("SELECT DISTINCT `name` FROM `" + table + "` WHERE `type`=" + EntityType.User.getCode() + " ORDER BY id ASC");
            while (res.next())
            {
                String name = Mysql.unescape(res.getString("name"));
                groups.add(name);
            }
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

        return groups;
    }

    private MysqlPermEntity getEntity(String name, EntityType type)
    {
        MysqlPermEntity mpe = null;

        ResultSet res = null;
        try
        {
            res = mysql.returnQuery("SELECT `name`,`type`,`key`,`value`,`server`,`world` FROM `" + table + "` "
                    + "WHERE `type`=" + type.getCode() + " AND `name`='" + Mysql.escape(name) + "' ORDER BY id ASC");

            mpe = new MysqlPermEntity(res);
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

        return mpe;
    }

    public MysqlPermEntity getGroup(String name)
    {
        return getEntity(name, EntityType.Group);
    }

    public MysqlPermEntity getUser(String name)
    {
        return getEntity(name, EntityType.User);
    }

    public MysqlPermEntity getVersion()
    {
        return getEntity("version", EntityType.Version);
    }

    public boolean isInBD(String name, EntityType type)
    {
        boolean found = false;

        ResultSet res = null;
        try
        {
            res = mysql.returnQuery("SELECT DISTINCT `name` FROM `" + table + "` WHERE `name`='" + Mysql.escape(name) + "' AND `type`=" + type.getCode() + " ORDER BY id ASC");
            if (res.next())
            {
                found = true;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            found = false;
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

        return found;
    }

    public void deleteEntity(String name, EntityType type)
    {
        mysql.runQuery("DELETE FROM `" + table + "` WHERE `name`='" + Mysql.escape(name) + "' AND `type`=" + type.getCode());
    }

    public void saveData(String name, EntityType type, String key, List<ValueEntry> values)
    {
        //delete entries
        String delq = "DELETE FROM `" + table + "` WHERE `name`='" + Mysql.escape(name) + "' AND `type`=" + type.getCode() + " AND `key`='" + Mysql.escape(key) + "'";
        mysql.runQuery(delq);

        //add values
        doSaveData(name, type, key, values);
    }

    public void saveData(String name, EntityType type, String key, List<ValueEntry> values, String server, String world)
    {
        //delete entries
        String delq = "DELETE FROM `" + table + "` WHERE `name`='" + Mysql.escape(name) + "' AND `type`=" + type.getCode() + " AND `key`='" + Mysql.escape(key) + "' AND ";
        if (server == null)
        {
            delq += "`server` IS NULL";
        }
        else
        {
            delq += "`server`='" + Mysql.escape(server) + "'";
        }
        delq += " AND ";
        if (world == null)
        {
            delq += "`world` IS NULL";
        }
        else
        {
            delq += "`world`='" + Mysql.escape(world) + "'";
        }
        mysql.runQuery(delq);

        //add values
        doSaveData(name, type, key, values);
    }

    private void doSaveData(String name, EntityType type, String key, List<ValueEntry> values)
    {
        for (ValueEntry val : values)
        {
            String insq = "INSERT INTO `" + table + "` (`name`,`type`,`key`,`value`,`server`,`world`) VALUES"
                    + "('" + Mysql.escape(name) + "'," + type.getCode() + ",'" + Mysql.escape(key) + "','" + Mysql.escape(val.getValue()) + "',";
            if (val.getServer() == null)
            {
                insq += "null,null";
            }
            else
            {
                insq += "'" + Mysql.escape(val.getServer()) + "',";
                if (val.getWorld() == null)
                {
                    insq += "null";
                }
                else
                {
                    insq += "'" + Mysql.escape(val.getWorld()) + "'";
                }
            }

            insq += ")";
            mysql.runQuery(insq);
        }
    }

    public List<String> getGroupUsers(String group)
    {
        List<String> groups = new ArrayList<>();

        ResultSet res = null;
        try
        {
            res = mysql.returnQuery("SELECT DISTINCT `name` FROM `" + table + "` WHERE `type`=" + EntityType.User.getCode() + " AND `key`='groups' AND `value`='" + Mysql.escape(group) + "' ORDER BY id ASC");
            while (res.next())
            {
                String name = Mysql.unescape(res.getString("name"));
                groups.add(name);
            }
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

        return groups;
    }

    public void clearTable(String table)
    {
        mysql.runQuery("TRUNCATE `" + table + "`");
    }

}
