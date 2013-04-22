package net.alpenblock.bungeeperms;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.alpenblock.bungeeperms.config.FileConfiguration;
import net.alpenblock.bungeeperms.config.YamlConfiguration;

public class NewConfig {
	
	private FileConfiguration fconfig;
	private String path;
	
	
	public NewConfig (String path) 
	{
		this.path=System.getProperty("user.dir")+path;
		fconfig = YamlConfiguration.loadConfiguration(new File(System.getProperty("user.dir")+path));
	}
	
	public void load() {
		createFile();
		try {fconfig.load(path);
		} catch (Exception e) {e.printStackTrace();}
	}
	public void save() {
		createFile();
		try {fconfig.save(path);
		} catch (Exception e) {e.printStackTrace();}
	}
	public void createFile()
	{
		File file=new File(path);
		if(!file.exists())
		{
			file.getParentFile().mkdirs();
			try 
			{
				file.createNewFile();
			} 
			catch (Exception e) 
			{
				//dobug.log(e,true);
				e.printStackTrace();
			}
		}
	}
	public String getString(String key, String def) {
		
		if(fconfig.contains(key)) {
			return fconfig.getString(key);
		}
		else {		
			fconfig.set(key, def);
			try { fconfig.save(path); } catch (IOException e) { e.printStackTrace(); }
			return def;
		}
	}
	
	public int getInt(String key, int def) {
		
		if(fconfig.contains(key)) {
			return fconfig.getInt(key);
		}
		else {
			fconfig.set(key, def);
			try { fconfig.save(path); } catch (IOException e) { e.printStackTrace(); }
			return def;
		}
			
	}
	
	public boolean getBoolean(String key, boolean def) {
		
		if(fconfig.contains(key)) {
			return fconfig.getBoolean(key);
		}
		else {
			fconfig.set(key, def);
			try { fconfig.save(path); } catch (IOException e) { e.printStackTrace(); }
			return def;
		}
			
	}
	
	public List<String> getListString(String key, List<String> def) {
		
		if(fconfig.contains(key)) {
			return fconfig.getStringList(key);
		}
		else {
			fconfig.set(key, def);
			try { fconfig.save(path); } catch (IOException e) { e.printStackTrace(); }
			return def;
		}
			
	}
	
	public double getDouble(String key, double def)
	{
		if(fconfig.contains(key)) {
			return fconfig.getDouble(key);
		}
		else {
			fconfig.set(key, def);
			try { fconfig.save(path); } catch (IOException e) { e.printStackTrace(); }
			return def;
		}
	}
	
	public void setString(String key, String val) {
		fconfig.set(key, val);
		try { fconfig.save(path); } catch (IOException e) { e.printStackTrace(); }
	}
	
	public void setInt(String key, int val) {
		fconfig.set(key, val);
		try { fconfig.save(path); } catch (IOException e) { e.printStackTrace(); }
	}
	
	public void setBool(String key, boolean val) {
		fconfig.set(key, val);
		try { fconfig.save(path); } catch (IOException e) { e.printStackTrace(); }
	}
	
	public void setListString(String key, List<String> val) {
		fconfig.set(key, val);
		try { fconfig.save(path); } catch (IOException e) { e.printStackTrace(); }
	}
	
	public List<String> getSubNodes(String node)
	{
		List<String> ret=new ArrayList<String>();
		try
		{
			for(Object o:fconfig.getConfigurationSection(node).getKeys(false).toArray())
			{
				ret.add((String) o);
			}
		}
		catch(Exception e){}
		return ret;
	}

}
