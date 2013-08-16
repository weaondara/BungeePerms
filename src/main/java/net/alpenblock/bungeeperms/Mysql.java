package net.alpenblock.bungeeperms;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * The Class Mysql.
 */
public class Mysql 
{
	private Config config;
	private Debug debug;
	private Connection connection;
	private String configsection;
	
	public Mysql (Config c,Debug d,String configsection) 
	{	
		config = c;
		debug=d;
		this.configsection=configsection;
	}
	
	public void connect() 
	{
		try 
		{
			//URL zusammenbasteln
			String url = "jdbc:mysql://"+config.getString(configsection+".general.mysqlhost", "localhost")+":"+config.getString(configsection+".general.mysqlport","3306")+"/"+config.getString(configsection+".general.mysqldb", configsection)+"?autoReconnect=true";
			this.connection = DriverManager.getConnection(url,config.getString(configsection+".general.mysqluser",configsection),config.getString(configsection+".general.mysqlpw","password"));
		}
		catch (Exception e) 
		{
			debug.log(e);
		}
	}
	public void close() 
	{
		if(this.connection!=null) 
		{
			try 
			{
				if(isConnected())
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
        try 
        {
            ResultSet rs = this.returnQuery("SELECT 1;",false);
            if (rs == null)
            {
                return false;
            }
            if (rs.next()) 
            {
                return true;
            }
            return false;
        }
        catch (Exception e) 
        {
            return false;
        }
	}

    public ResultSet returnQuery(String query) 
    {
    	return returnQuery(query, true);
    }
    public boolean runQuery(String query) 
    {
        return runQuery(query, true);
    }
    public long runQueryGetId(String query) 
    {
        return runQueryGetId(query,true);
    }
    
    public boolean tableExists(String table) 
    {
    	boolean tableexists = false;
    	try 
    	{
			ResultSet res = this.returnQuery("SHOW TABLES");
			while(res.next()) 
			{
				if(res.getString(1).equalsIgnoreCase(table)) 
				{
					tableexists = true;
				}
			}
    	} 
    	catch (Exception e)
    	{
    		debug.log(e);
    	}
    	return tableexists;
    }
    public boolean addColumn(String table,String column,String type,String after,String value) 
    {
    	try 
    	{
    		String getc="SHOW COLUMNS FROM "+table;
			ResultSet res=returnQuery(getc);
			
			boolean found=false;
			while(res.next())
			{
				if(res.getString("Field").equalsIgnoreCase(column))
				{
					found=true;
					break;
				}
			}
			if(!found)
			{
				String c="ALTER TABLE `"+table+"` ADD COLUMN `"+column+"` "+type+" AFTER `"+after+"`";
				runQuery(c);
				c="UPDATE "+table+" SET "+column+"="+value;
				runQuery(c);
			}
			return true;
    	} 
    	catch (Exception e)
    	{
    		debug.log(e);
    		return false;
    	}
    }
    public int columnExists(String table,String column) 
    {
    	try 
    	{
    		String getc="SHOW COLUMNS FROM "+table;
			ResultSet res=returnQuery(getc);
			
			while(res.next())
			{
				if(res.getString("Field").equalsIgnoreCase(column))
				{
					return 1;
				}
			}
			return 2;
    	} 
    	catch (Exception e)
    	{
    		debug.log(e);
    		return 0;
    	}
    }

	
    private ResultSet returnQuery(String query,boolean checkconnection) 
    {
        try 
        {
        	if(checkconnection)checkConnection();
            Statement stmt = this.connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            return rs;
        } 
        catch (SQLException e) 
        {
        	debug.log(e);
            return null;
        }
    }
    private boolean runQuery(String query,boolean checkconnection) 
    {
        try 
        {
        	if(checkconnection)checkConnection();
            Statement stmt = this.connection.createStatement();
            return stmt.execute(query);
        }
        catch (Exception e) 
        {
        	debug.log(e);
            return false;
        }
    }
    private long runQueryGetId(String query,boolean checkconnection) 
    {
        try 
        {
        	if(checkconnection)checkConnection();
        	Statement stmt = this.connection.createStatement();
            stmt.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
            
            //ResultSet rs = stmt.getGeneratedKeys();
            ResultSet rs = stmt.getGeneratedKeys();
            while(rs.next()) 
            {
            	return rs.getLong(1);
            }
        }
        catch (Exception e) 
        {
        	debug.log(e);
        }
        return 0;
    }
    
    private void checkConnection()
    {
    	if(!isConnected())
    	{
    		reconnect();
    	}
    }
    private void reconnect()
    {
    	close();
    	connect();
    }

    public static String escape(String s)
    {
        if(s==null)
        {
            return null;
        }
    	String ret=s;
    	ret = ret.replaceAll("\\\\", "\\\\\\\\");
    	ret = ret.replaceAll("\\n","\\\\n");
    	ret = ret.replaceAll("\\r", "\\\\r");
    	ret = ret.replaceAll("\\t", "\\\\t");
    	ret = ret.replaceAll("\\00", "\\\\0");
    	ret = ret.replaceAll("'", "\\\\'");
    	ret = ret.replaceAll("\\\"", "\\\\\"");
    	return ret;
    }
    public static String unescape(String s)
    {
        if(s==null)
        {
            return null;
        }
    	String ret=s;
    	ret = ret.replaceAll("\\\\n","\\n");
    	ret = ret.replaceAll("\\\\r", "\\r");
    	ret = ret.replaceAll("\\\\t", "\\t");
    	ret = ret.replaceAll("\\\\0", "\\00");
    	ret = ret.replaceAll("\\\\'", "'");
    	ret = ret.replaceAll("\\\\\"", "\\\"");
    	ret = ret.replaceAll("\\\\\\\\", "\\\\");
    	return ret;
    }
}