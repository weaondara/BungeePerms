package net.alpenblock.bungeeperms.io;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import net.alpenblock.bungeeperms.BPConfig;
import net.alpenblock.bungeeperms.BungeePerms;
import net.alpenblock.bungeeperms.Debug;
import net.alpenblock.bungeeperms.Mysql;

public class MySQLUUIDPlayerDB implements UUIDPlayerDB
{

    private final BPConfig config;
    private final Debug debug;
    @Getter
    private final Mysql mysql;

    private final String table;

    public MySQLUUIDPlayerDB()
    {
        this.config = BungeePerms.getInstance().getConfig();
        this.debug = BungeePerms.getInstance().getDebug();
        mysql = new Mysql(config, debug, "bungeeperms");
        mysql.connect();

        table = config.getMysqlTablePrefix() + "uuidplayer";

        createTable();
    }

    private void createTable()
    {
        if (!mysql.tableExists(table))
        {
            PreparedStatement stmt = null;
            try
            {
                String t = "CREATE TABLE `" + table + "` ("
                           + "`id` INT( 64 ) NOT NULL AUTO_INCREMENT PRIMARY KEY ,"
                           + "`uuid` VARCHAR( 40 ) NOT NULL UNIQUE KEY,"
                           + "`player` VARCHAR( 20 ) NOT NULL UNIQUE KEY"
                           + ") ENGINE = MYISAM ;";
                mysql.checkConnection();
                stmt = mysql.stmt(t);
                mysql.runQuery(stmt);
            }
            catch (Exception e)
            {
                debug.log(e);
            }
            finally
            {
                Mysql.close(stmt);
            }
        }
    }

    @Override
    public BackEndType getType()
    {
        return BackEndType.MySQL;
    }

    @Override
    public UUID getUUID(String player)
    {
        UUID ret = null;

        PreparedStatement stmt = null;
        ResultSet res = null;
        try
        {
            mysql.checkConnection();
            stmt = mysql.stmt("SELECT id, uuid FROM " + table + " WHERE player=? ORDER BY id ASC LIMIT 1");
            stmt.setString(1, player);
            res = mysql.returnQuery(stmt);
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
            Mysql.close(res);
            Mysql.close(stmt);
        }

        return ret;
    }

    @Override
    public String getPlayerName(UUID uuid)
    {
        String ret = null;

        PreparedStatement stmt = null;
        ResultSet res = null;
        try
        {
            mysql.checkConnection();
            stmt = mysql.stmt("SELECT player FROM " + table + " WHERE uuid=?");
            stmt.setString(1, uuid.toString());
            res = mysql.returnQuery(stmt);
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
            Mysql.close(res);
            Mysql.close(stmt);
        }

        return ret;
    }

    @Override
    public void update(UUID uuid, String player)
    {
        PreparedStatement stmt = null;
        try
        {
            mysql.checkConnection();
            stmt = mysql.stmt("DELETE FROM " + table + " WHERE uuid=? OR player=?");
            stmt.setString(1, uuid.toString());
            stmt.setString(2, player);
            mysql.runQuery(stmt);
            Mysql.close(stmt);

            mysql.checkConnection();
            stmt = mysql.stmt("INSERT IGNORE INTO " + table + " (uuid, player) VALUES (?, ?)");
            stmt.setString(1, uuid.toString());
            stmt.setString(2, player);
            mysql.runQuery(stmt);
        }
        catch (Exception e)
        {
            debug.log(e);
        }
        finally
        {
            Mysql.close(stmt);
        }
    }

    @Override
    public Map<UUID, String> getAll()
    {
        Map<UUID, String> ret = new HashMap<>();

        PreparedStatement stmt = null;
        ResultSet res = null;
        try
        {
            mysql.checkConnection();
            stmt = mysql.stmt("SELECT uuid, player FROM " + table);
            res = mysql.returnQuery(stmt);
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
            Mysql.close(res);
            Mysql.close(stmt);
        }

        return ret;
    }

    @Override
    public void clear()
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
            debug.log(e);
        }
        finally
        {
            Mysql.close(stmt);
        }
    }
}
