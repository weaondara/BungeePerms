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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import net.alpenblock.bungeeperms.BPPermission;
import net.alpenblock.bungeeperms.BungeePerms;
import net.alpenblock.bungeeperms.Mysql;

public class MysqlPermsAdapter
{

    private final Mysql mysql;
    private final String table;

    public MysqlPermsAdapter(Mysql m, String table)
    {
        mysql = m;
        this.table = table;
    }

    public void createTable()
    {

        PreparedStatement stmt = null;
        ResultSet res = null;
        try
        {
            if (!mysql.tableExists(table))
            {
                String t = "CREATE TABLE `" + table + "` ("
                           + "`id` INT( 64 ) NOT NULL AUTO_INCREMENT PRIMARY KEY ,"
                           + "`name` VARCHAR( 64 ) NOT NULL ,"
                           + "`type` TINYINT( 2 ) NOT NULL ,"
                           + "`key` VARCHAR( 255 ) NOT NULL, "
                           + "`value` VARCHAR( 255 ) NOT NULL, "
                           + "`server` VARCHAR( 64 ) DEFAULT NULL, "
                           + "`world` VARCHAR( 64 ) DEFAULT NULL, "
                           + "`timedstart` TIMESTAMP NULL DEFAULT NULL, "
                           + "`timedduration` INT(11) DEFAULT NULL "
                           + ") ENGINE = MYISAM ;";
                mysql.checkConnection();
                stmt = mysql.stmt(t);
                mysql.runQuery(stmt);
                stmt.close();
            }

            stmt = mysql.stmt("SHOW FIELDS FROM `" + table + "`");
            res = mysql.returnQuery(stmt);
            boolean found = false;
            while (res.next())
                if (res.getString("Field").equals("timedstart"))
                    found = true;
            res.close();
            stmt.close();

            if (!found)
            {
                stmt = mysql.stmt("ALTER TABLE `" + table + "` ADD COLUMN `timedstart` TIMESTAMP NULL DEFAULT NULL, ADD COLUMN `timedduration` INT(11) DEFAULT NULL");
                mysql.runQuery(stmt);
            }
            
            //check indexes
            try 
            {
                stmt = mysql.stmt("ALTER TABLE `" + table + "` ADD INDEX `bp_type_name_key` (`type`, `name`, `key`)");
                mysql.runQuery(stmt);
            } 
            catch (Exception e) {}
            try 
            {
                stmt = mysql.stmt("ALTER TABLE `" + table + "` ADD INDEX `bp_type_key_value` (`type`, `key`, `value`)");
                mysql.runQuery(stmt);
            } 
            catch (Exception e) {}
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
    }

    public List<String> getGroups()
    {
        List<String> groups = new ArrayList<>();

        PreparedStatement stmt = null;
        ResultSet res = null;
        try
        {
            mysql.checkConnection();
            stmt = mysql.stmt("SELECT DISTINCT `name` FROM `" + table + "` WHERE `type`=" + EntityType.Group.getCode());
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
            stmt = mysql.stmt("SELECT DISTINCT `name` FROM `" + table + "` WHERE `type`=" + EntityType.User.getCode());
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
            stmt = mysql.stmt("SELECT id,`name`,`type`,`key`,`value`,`server`,`world`,`timedstart`,`timedduration` FROM `" + table + "` "
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
            stmt = mysql.stmt("SELECT DISTINCT `name` FROM `" + table + "` WHERE `name`=? AND `type`=" + type.getCode());
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

    public void saveData(String name, EntityType type, String key, List<ValueEntry> values, String server, String world)
    {
        String delq = "DELETE FROM `" + table + "` WHERE `name`=? AND `type`=" + type.getCode() + " AND `key`=? AND ";
        if (server == null)
            delq += "`server` IS NULL";
        else
            delq += "`server`=?";
        delq += " AND ";
        if (server == null || world == null)
            delq += "`world` IS NULL";
        else
            delq += "`world`=?";

        mysql.checkConnection();
        mysql.startTransaction();
        PreparedStatement stmt = null;
        try
        {
            //delete entries
            stmt = mysql.stmt(delq);
            stmt.setString(1, name);
            stmt.setString(2, key);
            if (server != null)
                stmt.setString(3, server);
            if (server != null && world != null)
                stmt.setString(4, world);

            mysql.runQuery(stmt);

            //add values
            doSaveData(name, type, key, values);

            mysql.commit();
        }
        catch (Throwable t)
        {
            mysql.rollback();
            BungeePerms.getInstance().getDebug().log(t);
        }
        finally
        {
            Mysql.close(stmt);
        }
    }

    private void doSaveData(String name, EntityType type, String key, List<ValueEntry> values)
    {
        mysql.checkConnection();
        mysql.startTransaction();
        PreparedStatement stmt = null;
        try
        {
            String insq = "INSERT INTO `" + table + "` (`name`,`type`,`key`,`value`,`server`,`world`,`timedstart`,`timedduration`) "
                          + "VALUES (?," + type.getCode() + ",?,?,?,?,?,?)";
            stmt = mysql.stmt(insq);
            for (ValueEntry val : values)
            {
                if (val.getValue() == null)
                    continue;

                try
                {
                    stmt.setString(1, name);
                    stmt.setString(2, key);
                    stmt.setString(3, val.getValue());
                    stmt.setString(4, val.getServer());
                    stmt.setString(5, val.getWorld());
                    stmt.setTimestamp(6, val.getStart());
                    if (val.getDuration() != null)
                        stmt.setInt(7, val.getDuration());
                    else
                        stmt.setNull(7, Types.INTEGER);

                    mysql.runQuery(stmt);
                }
                catch (Exception e)
                {
                    BungeePerms.getInstance().getDebug().log(e);
                }
            }

            mysql.commit();
        }
        catch (Throwable t)
        {
            mysql.rollback();
            throw t;
        }
        finally
        {
            Mysql.close(stmt);
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
            stmt = mysql.stmt("SELECT DISTINCT `name` FROM `" + table + "` WHERE `type`=" + EntityType.User.getCode() + " AND `key`='groups' AND `value`=?");
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

    public void removeGroupReferences(String group)
    {
        PreparedStatement stmt = null;
        try
        {
            mysql.checkConnection();
            stmt = mysql.stmt("DELETE FROM `" + table + "` WHERE `value` = ? AND `key` IN ('groups', 'timedgroups', 'inheritances', 'timedinheritances')");
            stmt.setString(1, group);
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

    public List<BPPermission> getUsersWithPerm(String perm) {
        List<BPPermission> users = new ArrayList<>();

        PreparedStatement stmt = null;
        ResultSet res = null;
        try {
            mysql.checkConnection();
            stmt = mysql.stmt("SELECT * FROM `" + table + "` WHERE `key` LIKE 'permissions' AND `type`=" + EntityType.User.getCode() + " AND `value` LIKE '%" + perm + "%'");
            res = mysql.returnQuery(stmt);
            while (res.next()) {
                String name = res.getString("name");
                String value = res.getString("value");
                String server = res.getString("server");
                String world = res.getString("world");
                BPPermission bpperm = new BPPermission(value, name, true, server, world, null, null);
                users.add(bpperm);
            }
        } catch (Exception e) {
            BungeePerms.getInstance().getDebug().log(e);
        } finally {
            Mysql.close(res);
            Mysql.close(stmt);
        }

        return users;
    }

    public List<BPPermission> getGroupsWithPerm(String perm) {
        List<BPPermission> groups = new ArrayList<>();

        PreparedStatement stmt = null;
        ResultSet res = null;
        try {
            mysql.checkConnection();
            stmt = mysql.stmt("SELECT * FROM `" + table + "` WHERE `key` LIKE 'permissions' AND `type`=" + EntityType.Group.getCode() + " AND `value` LIKE '%" + perm + "%'");
            res = mysql.returnQuery(stmt);
            while (res.next()) {
                String name = res.getString("name");
                String value = res.getString("value");
                String server = res.getString("server");
                String world = res.getString("world");
                BPPermission bpperm = new BPPermission(value, name, true, server, world, null, null);
                groups.add(bpperm);
            }
        } catch (Exception e) {
            BungeePerms.getInstance().getDebug().log(e);
        } finally {
            Mysql.close(res);
            Mysql.close(stmt);
        }

        return groups;
    }
}
