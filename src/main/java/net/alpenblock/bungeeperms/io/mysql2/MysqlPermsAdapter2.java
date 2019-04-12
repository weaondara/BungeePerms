package net.alpenblock.bungeeperms.io.mysql2;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import net.alpenblock.bungeeperms.BungeePerms;
import net.alpenblock.bungeeperms.Mysql;

public class MysqlPermsAdapter2
{

    private final Mysql mysql;
    private final String table;

    public MysqlPermsAdapter2(Mysql m, String table)
    {
        mysql = m;
        this.table = table;
    }

    public void createTable()
    {
        if (!mysql.tableExists(table))
        {
            PreparedStatement stmt = null;
            try
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
                mysql.checkConnection();
                stmt = mysql.stmt(t);
                mysql.runQuery(stmt);
            }
            catch (Exception e)
            {
                BungeePerms.getInstance().getDebug().log(e);
            }
            finally
            {
                Mysql.close(stmt);
            }
        }
    }

    public List<String> getGroups()
    {
        List<String> groups = new ArrayList<>();

        PreparedStatement stmt = null;
        ResultSet res = null;
        try
        {
            mysql.checkConnection();
            stmt = mysql.stmt("SELECT DISTINCT `name` FROM `" + table + "` WHERE `type`=" + EntityType.Group.getCode() + " ORDER BY id ASC");
            res = mysql.returnQuery(stmt);
            while (res.next())
            {
                String name = res.getString("name");
                groups.add(name);
            }
        }
        catch (Exception e)
        {
            BungeePerms.getInstance().getDebug().log(e);
        }
        finally
        {
            Mysql.close(res);
            Mysql.close(stmt);
        }

        return groups;
    }

    public List<String> getUsers()
    {
        List<String> groups = new ArrayList<>();

        PreparedStatement stmt = null;
        ResultSet res = null;
        try
        {
            mysql.checkConnection();
            stmt = mysql.stmt("SELECT DISTINCT `name` FROM `" + table + "` WHERE `type`=" + EntityType.User.getCode() + " ORDER BY id ASC");
            res = mysql.returnQuery(stmt);
            while (res.next())
            {
                String name = res.getString("name");
                groups.add(name);
            }
        }
        catch (Exception e)
        {
            BungeePerms.getInstance().getDebug().log(e);
        }
        finally
        {
            Mysql.close(res);
            Mysql.close(stmt);
        }

        return groups;
    }

    private MysqlPermEntity getEntity(String name, EntityType type)
    {
        MysqlPermEntity mpe = null;

        PreparedStatement stmt = null;
        ResultSet res = null;
        try
        {
            mysql.checkConnection();
            stmt = mysql.stmt("SELECT `name`,`type`,`key`,`value`,`server`,`world` FROM `" + table + "` "
                              + "WHERE `type`=" + type.getCode() + " AND `name`=? ORDER BY id ASC");
            stmt.setString(1, name);
            res = mysql.returnQuery(stmt);

            mpe = new MysqlPermEntity(res);
        }
        catch (Exception e)
        {
            BungeePerms.getInstance().getDebug().log(e);
        }
        finally
        {
            Mysql.close(res);
            Mysql.close(stmt);
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

        PreparedStatement stmt = null;
        ResultSet res = null;
        try
        {
            mysql.checkConnection();
            stmt = mysql.stmt("SELECT DISTINCT `name` FROM `" + table + "` WHERE `name`=? AND `type`=" + type.getCode() + " ORDER BY id ASC");
            stmt.setString(1, name);
            res = mysql.returnQuery(stmt);

            if (res.next())
            {
                found = true;
            }
        }
        catch (Exception e)
        {
            BungeePerms.getInstance().getDebug().log(e);
            found = false;
        }
        finally
        {
            Mysql.close(res);
            Mysql.close(stmt);
        }

        return found;
    }

    public void deleteEntity(String name, EntityType type)
    {
        PreparedStatement stmt = null;
        try
        {
            mysql.checkConnection();
            stmt = mysql.stmt("DELETE FROM `" + table + "` WHERE `name`=? AND `type`=" + type.getCode());
            stmt.setString(1, name);
            mysql.runQuery(stmt);
        }
        catch (Exception e)
        {
            BungeePerms.getInstance().getDebug().log(e);
        }
        finally
        {
            Mysql.close(stmt);
        }
    }

    public void saveData(String name, EntityType type, String key, List<ValueEntry> values)
    {
        PreparedStatement stmt = null;
        try
        {
            //delete entries
            mysql.checkConnection();
            stmt = mysql.stmt("DELETE FROM `" + table + "` WHERE `name`=? AND `type`=" + type.getCode() + " AND `key`=?");
            stmt.setString(1, name);
            stmt.setString(2, key);
            mysql.runQuery(stmt);
        }
        catch (Exception e)
        {
            BungeePerms.getInstance().getDebug().log(e);
            return;
        }
        finally
        {
            Mysql.close(stmt);
        }

        //add values
        doSaveData(name, type, key, values);
    }

    public void saveData(String name, EntityType type, String key, List<ValueEntry> values, String server, String world)
    {
        PreparedStatement stmt = null;
        try
        {
            //delete entries
            String delq = "DELETE FROM `" + table + "` WHERE `name`=? AND `type`=" + type.getCode() + " AND `key`=? AND ";
            if (server == null)
                delq += "`server` IS NULL";
            else
                delq += "`server`=?";
            delq += " AND ";
            if (world == null)
                delq += "`world` IS NULL";
            else
                delq += "`world`=?";

            mysql.checkConnection();
            stmt = mysql.stmt(delq);
            stmt.setString(1, name);
            stmt.setString(2, key);
            if (server != null)
                stmt.setString(3, server);
            if (world != null)
                stmt.setString(4, world);

            mysql.runQuery(stmt);
        }
        catch (Exception e)
        {
            BungeePerms.getInstance().getDebug().log(e);
            return;
        }
        finally
        {
            Mysql.close(stmt);
        }

        //add values
        doSaveData(name, type, key, values);
    }

    private void doSaveData(String name, EntityType type, String key, List<ValueEntry> values)
    {
        for (ValueEntry val : values)
        {
            PreparedStatement stmt = null;
            try
            {
                if (val.getValue() == null)
                {
                    continue;
                }
                String insq = "INSERT INTO `" + table + "` (`name`,`type`,`key`,`value`,`server`,`world`) VALUES (?," + type.getCode() + ",?,?,";
                if (val.getServer() == null)
                    insq += "null,null";
                else
                {
                    insq += "?,";
                    if (val.getWorld() == null)
                        insq += "null";
                    else
                        insq += "?";
                }
                insq += ")";

                mysql.checkConnection();
                stmt = mysql.stmt(insq);
                stmt.setString(1, name);
                stmt.setString(2, key);
                stmt.setString(3, val.getValue());
                if (val.getServer() != null)
                {
                    stmt.setString(4, val.getServer());
                    if (val.getWorld() != null)
                        stmt.setString(5, val.getWorld());
                }

                mysql.runQuery(stmt);
            }
            catch (Exception e)
            {
                BungeePerms.getInstance().getDebug().log(e);
            }
            finally
            {
                Mysql.close(stmt);
            }
        }
    }

    public List<String> getGroupUsers(String group)
    {
        List<String> groups = new ArrayList<>();

        PreparedStatement stmt = null;
        ResultSet res = null;
        try
        {
            mysql.checkConnection();
            stmt = mysql.stmt("SELECT DISTINCT `name` FROM `" + table + "` WHERE `type`=" + EntityType.User.getCode() + " AND `key`='groups' AND `value`=? ORDER BY id ASC");
            stmt.setString(1, group);
            res = mysql.returnQuery(stmt);
            while (res.next())
            {
                String name = res.getString("name");
                groups.add(name);
            }
        }
        catch (Exception e)
        {
            BungeePerms.getInstance().getDebug().log(e);
        }
        finally
        {
            Mysql.close(res);
            Mysql.close(stmt);
        }

        return groups;
    }

    public void clearTable(String table)
    {
        PreparedStatement stmt = null;
        try
        {
            mysql.checkConnection();
            stmt = mysql.stmt("TRUNCATE `" + table + "`");
            mysql.runQuery(stmt);
        }
        catch (Exception e)
        {
            BungeePerms.getInstance().getDebug().log(e);
        }
        finally
        {
            Mysql.close(stmt);
        }
    }
}
