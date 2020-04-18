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
package net.alpenblock.bungeeperms;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import lombok.Getter;
import lombok.SneakyThrows;

public class Mysql
{

    public static void close(AutoCloseable res)
    {
        if (res == null)
        {
            return;
        }
        try
        {
            res.close();
        }
        catch (Exception e)
        {
        }
    }

    private final BPConfig config;
    private final Debug debug;
    private final String configsection;
    @Getter
    private Connection connection;

    public Mysql(BPConfig c, Debug d, String configsection)
    {
        config = c;
        debug = d;
        this.configsection = configsection;
    }

    public void connect()
    {
        BungeePerms.getInstance().getPlugin().getLogger().info("Connecting to database");
        try
        {
            this.connection = DriverManager.getConnection(config.getMysqlURL(), config.getMysqlUser(), config.getMysqlPassword());
        }
        catch (Exception e)
        {
            RuntimeException t;
            if (e.getCause() != null && e.getCause().getMessage().startsWith("Access denied for user"))
                t = new RuntimeException("Failed to connect to database: " + e.getCause().getMessage());
            else
                t = new RuntimeException(e);
            throw t;
        }
    }

    public void close()
    {
        if (this.connection != null)
        {
            BungeePerms.getInstance().getPlugin().getLogger().info("Disconnecting from database");
            try
            {
                if (isConnected())
                {
                    this.connection.close();
                }
            }
            catch (Exception e)
            {
                debug.log(e);
            }
        }
    }

    public boolean isConnected()
    {
        boolean connected = false;

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try
        {
            stmt = stmt("SELECT 1;");
            rs = this.returnQuery(stmt, false);
            if (rs == null)
                connected = false;
            else if (rs.next())
                connected = true;
        }
        catch (Exception e)
        {
            connected = false;
        }
        finally
        {
            close(rs);
            close(stmt);
        }
        return connected;
    }

    @SneakyThrows
    public PreparedStatement stmt(String template)
    {
        return connection.prepareStatement(template);
    }

    public ResultSet returnQuery(PreparedStatement stmt)
    {
        return returnQuery(stmt, true);
    }

    public boolean runQuery(PreparedStatement stmt)
    {
        return runQuery(stmt, true);
    }

    public long runQueryGetId(PreparedStatement stmt)
    {
        return runQueryGetId(stmt, true);
    }

    public boolean tableExists(String table)
    {
        boolean tableexists = false;

        PreparedStatement stmt = null;
        ResultSet res = null;
        try
        {
            checkConnection();
            stmt = stmt("SHOW TABLES");
            res = this.returnQuery(stmt);
            while (res.next())
            {
                if (res.getString(1).equalsIgnoreCase(table))
                {
                    tableexists = true;
                    break;
                }
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
        return tableexists;
    }

    public boolean addColumn(String table, String column, String type, String after, String value)
    {
        boolean success = false;

        PreparedStatement stmt = null;
        ResultSet res = null;
        try
        {
            checkConnection();
            stmt = stmt("SHOW COLUMNS FROM " + table);
            res = returnQuery(stmt);

            boolean found = false;
            while (res.next())
            {
                if (res.getString("Field").equalsIgnoreCase(column))
                {
                    found = true;
                    break;
                }
            }
            stmt.close();
            if (!found)
            {
                checkConnection();
                stmt = stmt("ALTER TABLE `" + table + "` ADD COLUMN `" + column + "` " + type + " AFTER `" + after + "`");
                runQuery(stmt);
                stmt.close();

                checkConnection();
                stmt = stmt("UPDATE " + table + " SET " + column + "=?");
                stmt.setString(1, value);
                runQuery(stmt);
            }
            success = true;
        }
        catch (Exception e)
        {
            debug.log(e);
            success = false;
        }
        finally
        {
            Mysql.close(res);
            Mysql.close(stmt);
        }
        return success;
    }

    public int columnExists(String table, String column)
    {
        //0: error
        //1: column found
        //2: column not found
        int fsuccess = 2;

        PreparedStatement stmt = null;
        ResultSet res = null;
        try
        {
            checkConnection();
            stmt = stmt("SHOW COLUMNS FROM " + table);
            res = returnQuery(stmt);

            while (res.next())
            {
                if (res.getString("Field").equalsIgnoreCase(column))
                {
                    fsuccess = 1;
                }
            }
        }
        catch (Exception e)
        {
            debug.log(e);
            fsuccess = 0;
        }
        finally
        {
            close(res);
            close(stmt);
        }
        return fsuccess;
    }

    private ResultSet returnQuery(PreparedStatement stmt, boolean checkconnection)
    {
        ResultSet rs = null;
        try
        {
            if (checkconnection)
                checkConnection();
            rs = stmt.executeQuery();
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
        }
        return rs;
    }

    private boolean runQuery(PreparedStatement stmt, boolean checkconnection)
    {
        try
        {
            if (checkconnection)
                checkConnection();
            boolean success = stmt.execute();
            return success;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private long runQueryGetId(PreparedStatement stmt, boolean checkconnection)
    {
        long id = 0;

        ResultSet rs = null;
        try
        {
            if (checkconnection)
                checkConnection();
            stmt.executeUpdate();

            rs = stmt.getGeneratedKeys();
            if (rs.last())
            {
                id = rs.getLong(1);
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            close(rs);
        }
        return id;
    }

    public void checkConnection()
    {
        if (!isConnected())
        {
            reconnect();
        }
    }

    private void reconnect()
    {
        BungeePerms.getInstance().getPlugin().getLogger().info("Reconnecting to database");
        close();
        connect();
    }

    //maybe for later use
//    //transaction stuff
//    private ReentrantLock transactionlock = new ReentrantLock();
//    private Condition transactioncond = transactionlock.newCondition();
//    private Thread transactionthread;
//    private int nestedtranactions;
//    
//    @SneakyThrows
//    public void startTransaction()
//    {
//        transactionlock.lock();
//        try
//        {
//            while (transactionthread != null && transactionthread != Thread.currentThread())
//            {
//                System.out.println("st: wait on cond; thread = " + Thread.currentThread().getName());
//                transactioncond.await();
//            }
//            System.out.println("start transaction " + Thread.currentThread().getName());
//
//            transactionthread = Thread.currentThread();
//            nestedtranactions++;
//
//            connection.setAutoCommit(false);
//        }
//        catch (Throwable t)
//        {
//            nestedtranactions = 0;
//            connection.setAutoCommit(true);
//            transactionthread = null;
//            transactioncond.signal();
//        }
//        finally
//        {
//            transactionlock.unlock();
//        }
//    }
//
//    @SneakyThrows
//    public void commit()
//    {
//        transactionlock.lock();
//        try
//        {
//            System.out.println("commit " + Thread.currentThread().getName());
//            if (transactionthread != Thread.currentThread())
//                throw new IllegalStateException("transactionthread != Thread.currentThread()");
//
//            nestedtranactions--;
//            if (nestedtranactions < 0)
//                throw new IllegalStateException("nestedtranactions < 0");
//            if (nestedtranactions == 0)
//                connection.commit();
//        }
//        catch (Throwable t)
//        {
//            nestedtranactions = 0;
//        }
//        finally
//        {
//            if (nestedtranactions == 0)
//            {
//                connection.setAutoCommit(true);
//                transactionthread = null;
//                transactioncond.signal();
//            }
//            transactionlock.unlock();
//        }
//    }
//
//    @SneakyThrows
//    public void rollback()
//    {
//        transactionlock.lock();
//        try
//        {
//            System.out.println("rollback " + Thread.currentThread().getName());
//            if (transactionthread != Thread.currentThread())
//                throw new IllegalStateException("transactionthread != Thread.currentThread()");
//
//            nestedtranactions--;
//            if (nestedtranactions < 0)
//                throw new IllegalStateException("nestedtranactions < 0");
//            if (nestedtranactions == 0)
//                connection.rollback();
//        }
//        catch (Throwable t)
//        {
//            nestedtranactions = 0;
//        }
//        finally
//        {
//            if (nestedtranactions == 0)
//            {
//                connection.setAutoCommit(true);
//                transactionthread = null;
//                transactioncond.signal();
//            }
//            transactionlock.unlock();
//        }
//    }
}
