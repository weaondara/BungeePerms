package net.alpenblock.bungeeperms;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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
			String url = "jdbc:mysql://"+config.getString(configsection+".general.mysqlhost", "localhost")+":"+config.getString(configsection+".general.mysqlport","3306")+"/"+config.getString(configsection+".general.mysqldb", configsection)+"?autoReconnect=true&dontTrackOpenResources=true";
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
        boolean connected=false;
        
        ResultSet rs=null;
        try 
        {
            rs = this.returnQuery("SELECT 1;",false);
            if (rs == null)
            {
                connected=false;
            }
            if (rs.next()) 
            {
                connected=true;
            }
        }
        catch (Exception e) 
        {
            connected=false;
        }
        finally
        {
            try
            {
                rs.close();
            }
            catch(Exception e){}
        }
        return connected;
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
        
        ResultSet res=null;
    	try 
    	{
			res = this.returnQuery("SHOW TABLES");
			while(res.next()) 
			{
				if(res.getString(1).equalsIgnoreCase(table)) 
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
            try
            {
                res.close();
            }
            catch(Exception e){}
        }
    	return tableexists;
    }
    public boolean addColumn(String table,String column,String type,String after,String value) 
    {
        boolean success=false;
        
        ResultSet res=null;
    	try 
    	{
    		String getc="SHOW COLUMNS FROM "+table;
			res=returnQuery(getc);
			
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
			success=true;
    	} 
    	catch (Exception e)
    	{
    		debug.log(e);
    		success=false;
    	}
        finally
        {
            try
            {
                res.close();
            }
            catch(Exception e){}
        }
        return success;
    }
    public int columnExists(String table,String column) 
    {
        //0: error
        //1: coulumn found
        //2: cloumn not found
        int fsuccess=0;
        
        ResultSet res=null;
    	try 
    	{
    		String getc="SHOW COLUMNS FROM "+table;
			res=returnQuery(getc);
			
			while(res.next())
			{
				if(res.getString("Field").equalsIgnoreCase(column))
				{
					fsuccess=1;
				}
			}
			fsuccess=2;
    	} 
    	catch (Exception e)
    	{
    		debug.log(e);
    		fsuccess=0;
    	}
        finally
        {
            try
            {
                res.close();
            }
            catch(Exception e){}
        }
        return fsuccess;
    }
    
    
    private ResultSet returnQuery(String query,boolean checkconnection) 
    {
        Statement stmt=null;
        ResultSet rs=null;
        try 
        {
        	if(checkconnection)checkConnection();
            stmt = this.connection.createStatement();
            rs = stmt.executeQuery(query);
        } 
        catch (SQLException e) 
        {
            try
            {
                rs.close();
            }
            catch(Exception ex){}
            try
            {
                stmt.close();
            }
            catch(Exception ex){}
            throw new RuntimeException(e);
        }
        finally
        {
            try
            {
                //stmt.closeOnCompletion();
            }
            catch(Exception e){}
        }
        return rs;
    }
    private boolean runQuery(String query,boolean checkconnection) 
    {
        try 
        {
        	if(checkconnection)checkConnection();
            Statement stmt = this.connection.createStatement();
            boolean success=stmt.execute(query);
            stmt.close();
            return success;
        }
        catch (Exception e) 
        {
        	throw new RuntimeException(e);
        }
    }
    private long runQueryGetId(String query,boolean checkconnection) 
    {
        long id=0;
        
        Statement stmt=null;
        ResultSet rs=null;
        try 
        {
        	if(checkconnection)checkConnection();
        	stmt = this.connection.createStatement();
            stmt.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
            
            rs = stmt.getGeneratedKeys();
            if(rs.last()) 
            {
                id=rs.getLong(1);
            }
        }
        catch (Exception e) 
        {
        	throw new RuntimeException(e);
        }
        finally
        {
            try
            {
                rs.close();
            }
            catch(Exception e){}
            try
            {
                stmt.close();
            }
            catch(Exception e){}
        }
        return id;
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