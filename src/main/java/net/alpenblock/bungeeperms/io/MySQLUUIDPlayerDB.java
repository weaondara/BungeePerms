package net.alpenblock.bungeeperms.io;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.alpenblock.bungeeperms.BPConfig;
import net.alpenblock.bungeeperms.BungeePerms;
import net.alpenblock.bungeeperms.Debug;
import net.alpenblock.bungeeperms.Mysql;

public class MySQLUUIDPlayerDB implements UUIDPlayerDB
{

    private BPConfig config;
    private Debug debug;
    private Mysql mysql;

    private String table;

    public MySQLUUIDPlayerDB()
    {
        this.config = BungeePerms.getInstance().getConfig();
        this.debug = BungeePerms.getInstance().getDebug();
        mysql = new Mysql(config.getConfig(), debug, "bungeeperms");
        mysql.connect();

        table = config.getTablePrefix() + "uuidplayer";

        createTable();
    }

    private void createTable()
    {
        if (!mysql.tableExists(table))
        {
            String t = "CREATE TABLE `" + table + "` ("
                    + "`id` INT( 64 ) NOT NULL AUTO_INCREMENT PRIMARY KEY ,"
                    + "`uuid` VARCHAR( 40 ) NOT NULL UNIQUE KEY,"
                    + "`player` VARCHAR( 20 ) NOT NULL UNIQUE KEY"
                    + ") ENGINE = MYISAM ;";
            mysql.runQuery(t);
        }
    }

    @Override
    public UUIDPlayerDBType getType()
    {
        return UUIDPlayerDBType.MySQL;
    }

    @Override
    public UUID getUUID(String player)
    {
        UUID ret = null;

        ResultSet res = null;
        try
        {
            String q = "SELECT uuid FROM " + table + " WHERE player='" + player + "' ORDER BY id ASC LIMIT 1";
            res = mysql.returnQuery(q);
            if (res.last())
            {
                ret = UUID.fromString(res.getString("uuid"));
            }
        }
        catch (Exception e)
        {
            debug.log(e);
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

        return ret;
    }

    @Override
    public String getPlayerName(UUID uuid)
    {
        String ret = null;

        ResultSet res = null;
        try
        {
            String q = "SELECT player FROM " + table + " WHERE uuid='" + uuid + "'";
            res = mysql.returnQuery(q);
            if (res.last())
            {
                ret = res.getString("player");
            }
        }
        catch (Exception e)
        {
            debug.log(e);
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

        return ret;
    }

    @Override
    public void update(UUID uuid, String player)
    {
        mysql.runQuery("DELETE FROM " + table + " WHERE uuid='" + uuid + "' OR player='" + player + "'");
        mysql.runQuery("INSERT IGNORE INTO " + table + " (uuid, player) VALUES ('" + uuid + "', '" + player + "')");
    }

    @Override
    public Map<UUID, String> getAll()
    {
        Map<UUID, String> ret = new HashMap<>();

        ResultSet res = null;
        try
        {
            String q = "SELECT uuid, player FROM " + table;
            res = mysql.returnQuery(q);
            while (res.next())
            {
                UUID uuid = UUID.fromString(res.getString("uuid"));
                String name = res.getString("player");

                ret.put(uuid, name);
            }
        }
        catch (Exception e)
        {
            debug.log(e);
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

        return ret;
    }

    @Override
    public void clear()
    {
        mysql.runQuery("TRUNCATE " + table);
    }
}
