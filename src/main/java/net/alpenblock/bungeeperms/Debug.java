package net.alpenblock.bungeeperms;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.md_5.bungee.api.plugin.Plugin;

/**
 * The Class Debug.
 */
public class Debug {
	
	/** The ps. */
	PrintStream ps;
	
	/** The logger. */
	Logger logger;
	
	/** The path. */
	String path;
	
	/** The config. */
	Config config;
	
	/** The plugin. */
	Plugin plugin;
	
	/** The showexceptions. */
	boolean showexceptions;
	
	/** The showlogs. */
	boolean showlogs;
	
	/**
	 * Instantiates a new debug.
	 *
	 * @param p the p
	 * @param conf the conf
	 * @param loggername the loggername
	 */
	public Debug(Plugin p,Config conf, String loggername)
	{
		plugin=p;
		config=conf;
		loadconfig();
		File file=new File(path);
		try 
		{
			if(!file.isFile()|!file.exists())
			{
				file.getParentFile().mkdirs();
				file.createNewFile();
			}
			logger=Logger.getLogger(loggername+"Debug");
			logger.setUseParentHandlers(false);
			FileHandler fh=new FileHandler(path,true);
			fh.setFormatter(new DebugFormatter());
			logger.addHandler(fh);
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Loadconfig.
	 */
	private void loadconfig()
	{
		path=config.getString("debug.path",plugin.getDataFolder()+"/debug.log");
		showexceptions=config.getBoolean("debug.showexceptions", true);
		showlogs=config.getBoolean("debug.showlogs", false);
	}
	
	/**
	 * Log.
	 *
	 * @param str the str
	 */
	public void log(String str)
	{
		File file=new File(path);
		if(!file.isFile()|!file.exists())
		{
			try 
			{
				file.getParentFile().mkdirs();
				file.createNewFile();
			}
			catch (Exception ex) 
			{
				ex.printStackTrace();
			}
		}
		if(str==null){str="null";}
		logger.info(str);
		if(showlogs)
		{
			
			plugin.getProxy().getLogger().info("["+plugin.getDescription().getName()+"] [Debug] "+str);
		}
	}
	
	/**
	 * Log.
	 *
	 * @param o the o
	 */
	public void log(Object o)
	{
		if(o==null)
		{
			log("null");
		}
		else
		{
			log(o.toString());
		}
	}
	
	/**
	 * Log.
	 *
	 * @param e the e
	 */
	public void log(Exception e)
	{
		File file=new File(path);
		if(!file.isFile()|!file.exists())
		{
			try 
			{
				file.getParentFile().mkdirs();
				file.createNewFile();
			}
			catch (IOException ex) 
			{
				ex.printStackTrace();
			}
		}
		logger.log(Level.SEVERE, e.getMessage(), e);
		if(showexceptions)
		{
			e.printStackTrace();
		}
	}
}
