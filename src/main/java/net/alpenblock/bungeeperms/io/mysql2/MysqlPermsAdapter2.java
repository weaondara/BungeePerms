package net.alpenblock.bungeeperms.io.mysql2;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import net.alpenblock.bungeeperms.Mysql;

public class MysqlPermsAdapter2 
{
    private Mysql mysql;
    private String table;
    
	public MysqlPermsAdapter2 (Mysql m,String table) 
	{
        mysql=m;
        this.table=table;
	}
	
	public void createTable()
	{
		if(!mysql.tableExists(table))
        {
            String t = "CREATE TABLE `"+table+"` ("
                        +"`id` INT( 64 ) NOT NULL AUTO_INCREMENT PRIMARY KEY ,"
                        +"`name` VARCHAR( 64 ) NOT NULL ,"
                        +"`type` TINYINT( 2 ) NOT NULL ,"
                        +"`key` VARCHAR( 256 ) NOT NULL, "
                        +"`value` VARCHAR( 256 ) NOT NULL, "
                        +"`server` VARCHAR( 64 ), "
                        +"`world` VARCHAR( 64 ) "
                        +") ENGINE = MYISAM ;";
            mysql.runQuery(t);
        }
	}
    
    public List<String> getGroups()
    {
        List<String> groups=new ArrayList<>();
        
        ResultSet res=null;
		try 
        {
            res=mysql.returnQuery("SELECT DISTINCT `name` FROM `"+table+"` WHERE `type`="+EntityType.Group.getCode()+" ORDER BY id ASC");
            while(res.next())
            {
                String name=res.getString("name");
                groups.add(name);
            }
		} 
        catch (Exception e) {e.printStackTrace();}
        finally
        {
            try
            {
                res.close();
            }
            catch(Exception e){}
        }
        
        return groups;
    }
    public List<String> getUsers()
    {
        List<String> groups=new ArrayList<>();
        
        ResultSet res=null;
		try 
        {
            res=mysql.returnQuery("SELECT DISTINCT `name` FROM `"+table+"` WHERE `type`="+EntityType.User.getCode()+" ORDER BY id ASC");
            while(res.next())
            {
                String name=res.getString("name");
                groups.add(name);
            }
		} 
        catch (Exception e) {e.printStackTrace();}
        finally
        {
            try
            {
                res.close();
            }
            catch(Exception e){}
        }
        
        return groups;
    }
    
    private MysqlPermEntity getEntity(String name, EntityType type)
    {
        MysqlPermEntity mpe=null;
        
        ResultSet res=null;
		try 
        {
            res=mysql.returnQuery("SELECT `name`,`type`,`key`,`value`,`server`,`world` FROM `"+table+"` "
                    + "WHERE `type`="+type.getCode()+" AND `name`='"+name+"' ORDER BY id ASC");
            
            mpe=new MysqlPermEntity(res);
		} 
        catch (Exception e) {e.printStackTrace();}
        finally
        {
            try
            {
                res.close();
            }
            catch(Exception e){}
        }
        
        return mpe;
    }
    
    public MysqlPermEntity getGroup(String name)
    {
        return getEntity(name,EntityType.Group);
    }
    public MysqlPermEntity getUser(String name)
    {
        return getEntity(name,EntityType.User);
    }
    public MysqlPermEntity getVersion()
    {
        return getEntity("version",EntityType.Version);
    }
    
    public boolean isInBD(String name, EntityType type)
    {
        boolean found=false;
        
        ResultSet res=null;
		try 
        {
            res=mysql.returnQuery("SELECT DISTINCT `name` FROM `"+table+"` WHERE `name`='"+name+"' AND `type`="+type.getCode()+" ORDER BY id ASC");
            if(res.next())
            {
                found=true;
            }
		} 
        catch (Exception e) {e.printStackTrace();found=false;}
        finally
        {
            try
            {
                res.close();
            }
            catch(Exception e){}
        }
        
        return found;
    }

    public void deleteEntity(String name, EntityType type) 
    {
        mysql.runQuery("DELETE FROM `"+table+"` WHERE `name`='"+name+"' AND `type`="+type.getCode());
    }
    
    public void saveData(String name, EntityType type, String key, List<ValueEntry> values)
    {
        //delete entries
        String delq="DELETE FROM `"+table+"` WHERE `name`='"+name+"' AND `type`="+type.getCode()+" AND `key`='"+key+"'";
        mysql.runQuery(delq);
        
        //add values
        for(ValueEntry val:values)
        {
            String insq="INSERT INTO `"+table+"` (`name`,`type`,`key`,`value`,`server`,`world`) VALUES"
                    + "('"+name+"',"+type.getCode()+",'"+key+"','"+val.getValue()+"',";
            if(val.getServer()==null)
            {
                insq+="null,null";
            }
            else
            {
                insq+="'"+val.getServer()+"',";
                if(val.getWorld()==null)
                {
                    insq+="null";
                }
                else
                {
                    insq+="'"+val.getWorld()+"'";
                }
            }
            
            insq+= ")";
            mysql.runQuery(insq);
        }
    }

    
    public void clearTable(String table)
    {
        mysql.runQuery("TRUNCATE `"+table+"`");
    }
}
