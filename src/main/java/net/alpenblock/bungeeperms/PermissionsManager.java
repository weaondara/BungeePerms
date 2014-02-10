package net.alpenblock.bungeeperms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import net.alpenblock.bungeeperms.io.BackEnd;
import net.alpenblock.bungeeperms.io.BackEndType;
import net.alpenblock.bungeeperms.io.MySQLBackEnd;
import net.alpenblock.bungeeperms.io.YAMLBackEnd;
import net.alpenblock.bungeeperms.io.migrate.Migrate2MySQL;
import net.alpenblock.bungeeperms.io.migrate.Migrate2YAML;
import net.alpenblock.bungeeperms.io.migrate.Migrator;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PermissionCheckEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.command.ConsoleCommandSender;
import net.md_5.bungee.event.EventHandler;

public class PermissionsManager implements Listener
{
	private BungeeCord bc;
	private Plugin plugin;
    private Config config;
    private Debug debug;
    private boolean enabled;
    
    private String channel;
    private Map<String,String> playerWorlds;
    
    private List<Group> groups;
    private List<User> users;
    private int permsversion;
    
    private boolean saveAllUsers;
    private boolean deleteUsersOnCleanup;
    
    private BackEnd backend;
	
	public PermissionsManager(Plugin p,Config conf,Debug d)
	{
		bc=BungeeCord.getInstance();
		plugin=p;
        config=conf;
        debug=d;
		
        channel="bungeeperms";
        playerWorlds=new HashMap<>();
        
        //config
        loadConfig();
        
		//perms
		loadPerms();
        
        enabled=false;
	}
	
    /**
     * Loads the configuration of the plugin from the config.yml file.
     */
    public final void loadConfig()
    {
        config.load();
        
        saveAllUsers=config.getBoolean("saveAllUsers", true);
        deleteUsersOnCleanup=config.getBoolean("deleteUsersOnCleanup", false);
        
        BackEndType bet=config.getEnumValue("backendtype",BackEndType.YAML);
        if(bet==BackEndType.YAML)
        {
            backend=new YAMLBackEnd(bc,plugin,saveAllUsers,deleteUsersOnCleanup);
        }
        else if(bet==BackEndType.MySQL)
        {
            backend=new MySQLBackEnd(bc,config,debug,saveAllUsers,deleteUsersOnCleanup);
        }
    }
    
	/**
     * (Re)loads the all groups and online players from file/table.
     */
    public final void loadPerms()
	{
		bc.getLogger().info("[BungeePerms] loading permissions ...");
		
        //load database
        backend.load();
        
        //load all groups
        groups=backend.loadGroups();
        
        //load online players; allows reload
        users=new ArrayList<>();
        for(ProxiedPlayer pp:BungeeCord.getInstance().getPlayers())
        {
            getUser(pp.getName());
        }
        //users=backend.loadUsers();
        
        //load permsversion
        permsversion=backend.loadVersion();
		
		bc.getLogger().info("[BungeePerms] permissions loaded");
	}
    
    /**
     * Enables the permissions manager.
     */
    public void enable()
    {
        if(!enabled)
        {
            bc.getPluginManager().registerListener(plugin,this);
            bc.registerChannel(channel);
            enabled=true;
        }
    }
    
    /**
     * Disables the permissions manager.
     */
    public void disable()
    {
        if(enabled)
        {
            bc.getPluginManager().unregisterListener(this);
            bc.unregisterChannel(channel);
            enabled=false;
        }
    }
	
    /**
     * Validates all loaded groups and users and fixes invalid objects.
     */
	public synchronized void validateUserGroups() 
	{
        //user check
		for(int i=0;i<users.size();i++)
		{
			User u=users.get(i);
			for(int j=0;j<u.getGroups().size();j++)
			{
				if(getGroup(u.getGroups().get(j).getName())==null)
				{
                    u.getGroups().remove(j);
					j--;
				}
			}
            backend.saveUserGroups(u);
            
            //send bukkit update info
            sendPM(u.getName(),"reloadUser;"+u.getName());
		}
        
        //group check
		for(int i=0;i<groups.size();i++)
		{
			Group group=groups.get(i);
			List<String> inheritances=group.getInheritances();
			for(int j=0;j<inheritances.size();j++)
			{
				if(getGroup(inheritances.get(j))==null)
				{
					inheritances.remove(j);
					j--;
				}
			}
            backend.saveGroupInheritances(group);
            
            //send bukkit update info
            sendPM(group.getName(),"reloadGroup;"+group.getName());
		}
	}

    
	/**
     * Get the group of the player with the highesst rank. Do not to be confused with the rank property.
     * The higher the rank the smaller the rank property. (1 is highest rank; 1000 is a low rank)
     * @param player the user to get the main group of
     * @return the main group of the user (highest rank)
     * @throws NullPointerException if player is null
     */
    public synchronized Group getMainGroup(User player) 
	{
		if(player==null)
		{
			throw new NullPointerException("player is null");
		}
		if(player.getGroups().isEmpty())
		{
			return null;
		}
		Group ret=player.getGroups().get(0);
		for(int i=1;i<player.getGroups().size();i++)
		{
			if(player.getGroups().get(i).getRank()<ret.getRank())
			{
				ret=player.getGroups().get(i);
			}
		}
		return ret;
	}
    
	/**
     * Gets the next (higher) group in the same ladder.
     * @param group the group to get the next group of
     * @return the next group in the same ladder or null if the group has no next group
     * @throws IllegalArgumentException if the group ladder does not exist (anymore)
     */
    public synchronized Group getNextGroup(Group group)
	{
        List<Group> laddergroups=getLadderGroups(group.getLadder());
        
		for(int i=0;i<laddergroups.size();i++)
		{
			if(laddergroups.get(i).getRank()==group.getRank())
			{
				if(i+1<laddergroups.size())
				{
					return laddergroups.get(i+1);
				}
				else
				{
					return null;
				}
			}
		}
		throw new IllegalArgumentException("group ladder does not exist (anymore)");
	}
    
    /**
     * Gets the previous (lower) group in the same ladder.
     * @param group the group to get the previous group of
     * @return the previous group in the same ladder or null if the group has no previous group
     * @throws IllegalArgumentException if the group ladder does not exist (anymore)
     */
	public synchronized Group getPreviousGroup(Group group)
	{
        List<Group> laddergroups=getLadderGroups(group.getLadder());
        
		for(int i=0;i<laddergroups.size();i++)
		{
			if(laddergroups.get(i).getRank()==group.getRank())
			{
				if(i>0)
				{
					return laddergroups.get(i-1);
				}
				else
				{
					return null;
				}
			}
		}
		throw new IllegalArgumentException("group ladder does not exist (anymore)");
	}
    
    /**
     * Gets all groups of the given ladder.
     * @param ladder the ladder of the groups to get
     * @return a sorted list of all matched groups
     */
    public synchronized List<Group> getLadderGroups(String ladder)
    {
        List<Group> ret=new ArrayList<>();
        
        for(Group g:groups)
        {
            if(g.getLadder().equalsIgnoreCase(ladder))
            {
                ret.add(g);
            }
        }
        
        Collections.sort(ret);
        
        return ret;
    }
    
    /**
     * Gets a list of all existing ladders.
     * @return a list of all ladders
     */
    public synchronized List<String> getLadders()
    {
        List<String> ret=new ArrayList<>();
        
        for(Group g:groups)
        {
            if(!ret.contains(g.getLadder()))
            {
                ret.add(g.getLadder());
            }
        }
        
        return ret;
    }
    
	/**
     * Gets a list of all groups that are marked as default and given to all users by default.
     * @return a list of default groups
     */
    public synchronized List<Group> getDefaultGroups()
	{
		List<Group> ret=new ArrayList<>();
		for(Group g:groups)
		{
			if(g.isDefault())
			{
				ret.add(g);
			}
		}
		return ret;
	}
	
	/**
     * Gets a group by its name.
     * @param groupname the name of the group to get
     * @return the found group if any or null
     */
    public synchronized Group getGroup(String groupname)
	{
		for(Group g:groups)
		{
			if(g.getName().equalsIgnoreCase(groupname))
			{
				return g;
			}
		}
		return null;
	}
    
	/**
     * Gets a user by its name. If the user is not loaded it will be loaded.
     * @param username the name of the user to get
     * @return the found user or null if it does not exist
     */
    public synchronized User getUser(String username)
	{
		for(User u:users)
		{
			if(u.getName().equalsIgnoreCase(username))
			{
				return u;
			}
		}
        
        //load user from database
        User u=backend.loadUser(username);
        if(u!=null)
        {
            users.add(u);
            return u;
        }
        
		return null;
	}
	
	/**
     * Gets an unmodifiable list of all groups
     * @return an unmodifiable list of all groups
     */
    public List<Group> getGroups()
	{
		return Collections.unmodifiableList(groups);
	}
    
	/**
     * Gets an unmodifiable list of all loaded users
     * @return an unmodifiable list of all loaded users
     */
    public List<User> getUsers()
	{
		return Collections.unmodifiableList(users);
	}
    
    /**
     * Gets a list of all users
     * @return a list of all users
     */
    public List<String> getRegisteredUsers()
    {
        return backend.getRegisteredUsers();
    }
	
	/**
     * Deletes a user from cache and database.
     * @param user the user to delete
     */
    public synchronized void deleteUser(User user) 
	{
        //cache
        users.remove(user);
        
        //database
		backend.deleteUser(user);
        
        //send bukkit update info
        sendPM(user.getName(),"deleteUser;"+user.getName());
	}
    
	/**
     * Deletes a user from cache and database and validates all groups and users.
     * @param group the group the remove
     */
    public synchronized void deleteGroup(Group group) 
	{
        //cache
        groups.remove(group);
        
        //database
		backend.deleteGroup(group);
        
        //group validation
        BungeePerms.getInstance().getPermissionsManager().validateUserGroups();
        
        //send bukkit update info
        sendPM(group.getName(),"deleteGroup;"+group.getName());
	}
	
	/**
     * Adds a user to cache and database.
     * @param user the user to add
     */
    public synchronized void addUser(User user) 
	{
        //cache
        users.add(user);
        
        //database
		backend.saveUser(user,true);
        
        //send bukkit update info
        sendPM(user.getName(),"reloadUser;"+user.getName());
	}
    
    /**
     * Adds a group to cache and database.
     * @param group the group to add
     */
	public synchronized void addGroup(Group group) 
	{
        //cache
        groups.add(group);
		Collections.sort(groups);
        
        //database
		backend.saveGroup(group,true);
        
        //send bukkit update info
        sendPM(group.getName(),"reloadGroup;"+group.getName());
	}
	
    
	/**
     * Do NOT call this function.
     * @param e
     */
    @EventHandler(priority=-128)
	public void onLogin(LoginEvent e)
	{
        String playername=e.getConnection().getName();
		bc.getLogger().log(Level.INFO, "[BungeePerms] Login by {0}", playername);
        
        User u=getUser(playername);
        if(u==null)
        {
			bc.getLogger().log(Level.INFO, "[BungeePerms] Adding default groups to {0}", playername);
            
			List<Group> groups=getDefaultGroups();
			u=new User(playername, groups, new ArrayList<String>(),new HashMap<String, List<String>>(),new HashMap<String, Map<String, List<String>>>());
            users.add(u);
            
			backend.saveUser(u,true);
        }
	}
    
    /**
     * Do NOT call this function.
     * @param e
     */
    @EventHandler(priority=127)
	public void onDisconnect(PlayerDisconnectEvent e)
	{
        String playername=e.getPlayer().getName();
        
        User u=getUser(playername);
        users.remove(u);
	}
    
    /**
     * Do NOT call this function.
     * @param e
     */
	@EventHandler
	public void onPermissionCheck(PermissionCheckEvent e)
	{
		e.setHasPermission(hasPermOrConsoleOnServerInWorld(e.getSender(),e.getPermission()));
	}
	
    //possible permission checks
	/**
     * Checks if a user (no console) has a specific permission (globally).
     * @param sender the command sender to check a permission for
     * @param permission the permission to check
     * @return the result of the permission check
     */
    public boolean hasPerm(CommandSender sender, String permission)
	{
		if(sender instanceof ProxiedPlayer)
		{
			return getUser(sender.getName()).hasPerm(permission);
		}
		return false;
	}
    
    /**
     * Checks if a user (or console) has a specific permission (globally). If sender is console this function return true.
     * @param sender the command sender to check a permission for
     * @param permission the permission to check
     * @return the result of the permission check
     */
	public boolean hasPermOrConsole(CommandSender sender, String permission)
	{
		if(sender instanceof ProxiedPlayer)
		{
			return getUser(sender.getName()).hasPerm(permission);
		}
		else if(sender instanceof ConsoleCommandSender)
		{
			return true;
		}
		return false;
	}
    
    /**
     * Checks if a user (no console) has a specific permission (globally).
     * @param sender the command sender to check a permission for
     * @param permission the permission to check
     * @return the result of the permission check
     */
	public boolean hasPerm(String sender, String permission)
	{
		if(!sender.equalsIgnoreCase("CONSOLE"))
		{
			User u=getUser(sender);
			if(u==null)
			{
				return false;
			}
			return u.hasPerm(permission);
		}
		return false;
	}
    
    /**
     * Checks if a user (or console) has a specific permission (globally). If sender is console this function return true.
     * @param sender the command sender to check a permission for
     * @param permission the permission to check
     * @return the result of the permission check
     */
	public boolean hasPermOrConsole(String sender, String permission)
	{
		if(sender.equalsIgnoreCase("CONSOLE"))
		{
			return true;
		}
		else
		{
			User u=getUser(sender);
			if(u==null)
			{
				return false;
			}
			return u.hasPerm(permission);
		}
	}
    
	/**
     * Checks if a user (no console) has a specific permission (globally).
     * @param sender the command sender to check a permission for
     * @param perm the permission to check
     * @param msg if a no-permission message is send to the sender
     * @return the result of the permission check
     */
    public boolean has(CommandSender sender, String perm, boolean msg)
	{
		if(sender instanceof ProxiedPlayer)
		{
			boolean isperm=(hasPerm(sender, perm));
			if(!isperm & msg){sender.sendMessage(Color.Error+"You don't have permission to do that!"+ChatColor.RESET);}
			return isperm;
		}
		else
		{
			sender.sendMessage(Color.Error+"You don't have permission to do that!"+ChatColor.RESET);
			return false;
		}
	}
    
    /**
     * Checks if a user (or console) has a specific permission (globally).
     * @param sender the command sender to check a permission for
     * @param perm the permission to check
     * @param msg if a no-permission message is send to the sender
     * @return the result of the permission check
     */
	public boolean hasOrConsole(CommandSender sender, String perm, boolean msg)
	{
		boolean isperm=(hasPerm(sender, perm)|(sender instanceof ConsoleCommandSender));
		if(!isperm & msg){sender.sendMessage(Color.Error+"You don't have permission to do that!"+ChatColor.RESET);}
		return isperm;
	}
    
	/**
     * Checks if a user (no console) has a specific permission on the current server.
     * @param sender the command sender to check a permission for
     * @param permission the permission to check
     * @return the result of the permission check
     */
    public boolean hasPermOnServer(CommandSender sender, String permission)
	{
		if(sender instanceof ProxiedPlayer)
		{
			User user=getUser(sender.getName());
			if(((ProxiedPlayer) sender).getServer()==null)
			{
				return user.hasPerm(permission);
			}
			return user.hasPermOnServer(permission,((ProxiedPlayer) sender).getServer().getInfo());
		}
		return false;
	}
    
    /**
     * Checks if a user (or console) has a specific permission on the current server.
     * @param sender the command sender to check a permission for
     * @param permission the permission to check
     * @return the result of the permission check
     */
	public boolean hasPermOrConsoleOnServer(CommandSender sender, String permission)
	{
		if(sender instanceof ProxiedPlayer)
		{
			User user=getUser(sender.getName());
			if(((ProxiedPlayer) sender).getServer()==null)
			{
				return user.hasPerm(permission);
			}
			return user.hasPermOnServer(permission,((ProxiedPlayer) sender).getServer().getInfo());
		}
		else if(sender instanceof ConsoleCommandSender)
		{
			return true;
		}
		return false;
	}
    
    /**
     * Checks if a user (no console) has a specific permission on the given server.
     * @param sender the command sender to check a permission for
     * @param permission the permission to check
     * @param server the server for additional permissions
     * @return the result of the permission check
     */
	public boolean hasPermOnServer(String sender, String permission,ServerInfo server)
	{
		if(!sender.equalsIgnoreCase("CONSOLE"))
		{
			User p=getUser(sender);
			if(p==null)
			{
				return false;
			}
			return p.hasPermOnServer(permission,server);
		}
		return false;
	}
    
    /**
     * Checks if a user (or console) has a specific permission on the given server.
     * @param sender the command sender to check a permission for
     * @param permission the permission to check
     * @param server the server for additional permissions
     * @return the result of the permission check
     */
	public boolean hasPermOrConsoleOnServer(String sender, String permission,ServerInfo server)
	{
		if(sender.equalsIgnoreCase("CONSOLE"))
		{
			return true;
		}
		else
		{
			User u=getUser(sender);
			if(u==null)
			{
				return false;
			}
			return u.hasPermOnServer(permission,server);
		}
	}
    
    /**
     * Checks if a user (no console) has a specific permission on the current server.
     * @param sender the command sender to check a permission for
     * @param perm the permission to check
     * @param msg if a no-permission message is send to the sender
     * @return the result of the permission check
     */
	public boolean hasOnServer(CommandSender sender, String perm, boolean msg)
	{
		if(sender instanceof ProxiedPlayer)
		{
			boolean isperm=hasPermOnServer(sender, perm);
			if(!isperm & msg){sender.sendMessage(Color.Error+"You don't have permission to do that!"+ChatColor.RESET);}
			return isperm;
		}
		else
		{
			sender.sendMessage(Color.Error+"You don't have permission to do that!"+ChatColor.RESET);
			return false;
		}
	}
    
    /**
     * Checks if a user (or console) has a specific permission on the current server.
     * @param sender the command sender to check a permission for
     * @param perm the permission to check
     * @param msg if a no-permission message is send to the sender
     * @return the result of the permission check
     */
	public boolean hasOrConsoleOnServer(CommandSender sender, String perm, boolean msg)
	{
		boolean isperm=(hasPermOnServer(sender, perm)|(sender instanceof ConsoleCommandSender));
		if(!isperm & msg){sender.sendMessage(Color.Error+"You don't have permission to do that!"+ChatColor.RESET);}
		return isperm;
	}

    /**
     * Checks if a user (no console) has a specific permission on the current server and in the current world.
     * @param sender the command sender to check a permission for
     * @param permission the permission to check
     * @return the result of the permission check
     */
	public boolean hasPermOnServerInWorld(CommandSender sender, String permission)
	{
		if(sender instanceof ProxiedPlayer)
		{
			User user=getUser(sender.getName());
            
            //per server
			if(((ProxiedPlayer) sender).getServer()==null)
			{
				return user.hasPerm(permission);
			}
            
            //per server and world
            String world=playerWorlds.get(sender.getName());
            if(world==null)
            {
                return user.hasPermOnServer(permission,((ProxiedPlayer) sender).getServer().getInfo());
            }
            
            return user.hasPermOnServerInWorld(permission,((ProxiedPlayer) sender).getServer().getInfo(),world);
		}
		return false;
	}
    
    /**
     * Checks if a user (or console) has a specific permission on the current server and in the current world.
     * @param sender the command sender to check a permission for
     * @param perm the permission to check
     * @return the result of the permission check
     */
	public boolean hasPermOrConsoleOnServerInWorld(CommandSender sender, String permission)
	{
		if(sender instanceof ProxiedPlayer)
		{
			User user=getUser(sender.getName());
			if(((ProxiedPlayer) sender).getServer()==null)
			{
				return user.hasPerm(permission);
			}
			
            //per server and world
            String world=playerWorlds.get(sender.getName());
            if(world==null)
            {
                return user.hasPermOnServer(permission,((ProxiedPlayer) sender).getServer().getInfo());
            }
            
            return user.hasPermOnServerInWorld(permission,((ProxiedPlayer) sender).getServer().getInfo(),world);
		}
		else if(sender instanceof ConsoleCommandSender)
		{
			return true;
		}
		return false;
	}
    
    /**
     * Checks if a user (no console) has a specific permission on the given server and in the given world.
     * @param sender the command sender to check a permission for
     * @param permission the permission to check
     * @param server the server for additional permissions
     * @param world the world for additional permissions
     * @return the result of the permission check
     */
	public boolean hasPermOnServerInWorld(String sender, String permission,ServerInfo server,String world)
	{
		if(!sender.equalsIgnoreCase("CONSOLE"))
		{
			User u=getUser(sender);
			if(u==null)
			{
				return false;
			}
            
            if(world==null)
            {
                return hasPermOnServer(sender,permission,server);
            }
                
			return u.hasPermOnServerInWorld(permission,server,world);
		}
		return false;
	}
    
    /**
     * Checks if a user (or console) has a specific permission on the given server and in the given world.
     * @param sender the command sender to check a permission for
     * @param permission the permission to check
     * @param server the server for additional permissions
     * @param world the world for additional permissions
     * @return the result of the permission check
     */
	public boolean hasPermOrConsoleOnServerInWorld(String sender, String permission,ServerInfo server,String world)
	{
		if(sender.equalsIgnoreCase("CONSOLE"))
		{
			return true;
		}
		else
		{
			User u=getUser(sender);
			if(u==null)
			{
				return false;
			}
            
			if(world==null)
            {
                return hasPermOnServer(sender,permission,server);
            }
                
			return u.hasPermOnServerInWorld(permission,server,world);
		}
	}
    
    /**
     * Checks if a user (no console) has a specific permission on the current server and in the given world.
     * @param sender the command sender to check a permission for
     * @param perm the permission to check
     * @param msg if a no-permission message is send to the sender
     * @return the result of the permission check
     */
    public boolean hasOnServerInWorld(CommandSender sender, String perm, boolean msg)
	{
		if(sender instanceof ProxiedPlayer)
		{
			boolean isperm=hasPermOnServerInWorld(sender, perm);
			if(!isperm & msg){sender.sendMessage(Color.Error+"You don't have permission to do that!"+ChatColor.RESET);}
			return isperm;
		}
		else
		{
			sender.sendMessage(Color.Error+"You don't have permission to do that!"+ChatColor.RESET);
			return false;
		}
	}
    
    /**
     * Checks if a user (or console) has a specific permission on the current server and in the given world.
     * @param sender the command sender to check a permission for
     * @param perm the permission to check
     * @param msg if a no-permission message is send to the sender
     * @return the result of the permission check
     */
	public boolean hasOrConsoleOnServerInWorld(CommandSender sender, String perm, boolean msg)
	{
		boolean isperm=(hasPermOnServerInWorld(sender, perm)|(sender instanceof ConsoleCommandSender));
		if(!isperm & msg){sender.sendMessage(Color.Error+"You don't have permission to do that!"+ChatColor.RESET);}
		return isperm;
	}

    //database and permission operations
    /**
     * Formats the permissions backend.
     */
    public void format() 
    {
        backend.format(backend.loadGroups(), backend.loadUsers(),permsversion);
        backend.load();
        sendPMAll("reload;all");
    }
    
    /**
     * Cleans the permissions backend and wipes 0815 users.
     * @return the number of deleted users
     */
    public int cleanup() 
    {
        int res=backend.cleanup(backend.loadGroups(), backend.loadUsers(),permsversion);
        backend.load();
        sendPMAll("reload;all");
        return res;
    }

    /**
     * Adds the given group to the user.
     * @param user the user to add the group to
     * @param group the group to add to the user
     */
    public void addUserGroup(User user, Group group)
    {
        //cache
        user.getGroups().add(group);
        Collections.sort(user.getGroups());
        
        //database
        backend.saveUserGroups(user);
        
        //recalc perms
        user.recalcPerms();
        
        //send bukkit update info
        sendPM(user.getName(),"reloadUser;"+user.getName());
    }
    
    /**
     * Removes the given group from the user.
     * @param user the user to remove the group from
     * @param group the group to remove from the user
     */
    public void removeUserGroup(User user, Group group)
    {
        //cache
        user.getGroups().remove(group);
        Collections.sort(user.getGroups());
        
        //database
        backend.saveUserGroups(user);
        
        //recalc perms
        user.recalcPerms();
        
        //send bukkit update info
        sendPM(user.getName(),"reloadUser;"+user.getName());
    }
    
    /**
     * Adds a permission to the user.
     * @param user the user to add the permission to
     * @param perm the permission to add to the user
     */
    public void addUserPerm(User user, String perm)
    {
        //cache
        user.getExtraperms().add(perm);
        
        //database
        backend.saveUserPerms(user);
        
        //recalc perms
        user.recalcPerms();
        
        //send bukkit update info
        sendPM(user.getName(),"reloadUser;"+user.getName());
    }
    
    /**
     * Removes a permission from the user.
     * @param user the user to remove the permission from
     * @param perm the permission to remove from the user
     */
    public void removeUserPerm(User user, String perm) 
    {
        //cache
        user.getExtraperms().remove(perm);
        
        //database
        backend.saveUserPerms(user);
        
        //recalc perms
        user.recalcPerms();
        
        //send bukkit update info
        sendPM(user.getName(),"reloadUser;"+user.getName());
    }

    /**
     * Adds a permission to the user on the given server.
     * @param user the user to add the permission to
     * @param server the server to add the permission on
     * @param perm the permission to add to the user
     */
    public void addUserPerServerPerm(User user, String server, String perm) 
    {
        //cache
        List<String> perserverperms=user.getServerPerms().get(server);
        if(perserverperms==null)
        {
            perserverperms=new ArrayList<>();
        }
        
        perserverperms.add(perm);
        user.getServerPerms().put(server, perserverperms);
        
        //database
        backend.saveUserPerServerPerms(user, server);
        
        //recalc perms
        user.recalcPerms(server);
      
        //send bukkit update info
        sendPM(user.getName(),"reloadUser;"+user.getName());
    }
    
    /**
     * Removes a permission from the user on the given server.
     * @param user the user to remove the permission from
     * @param server the server to remove the permission from
     * @param perm the permission to remove from the user
     */
    public void removeUserPerServerPerm(User user, String server, String perm) 
    {
        //cache
        List<String> perserverperms=user.getServerPerms().get(server);
        if(perserverperms==null)
        {
            perserverperms=new ArrayList<>();
        }
        
        perserverperms.remove(perm);
        user.getServerPerms().put(server, perserverperms);
        
        //database
        backend.saveUserPerServerPerms(user, server);
        
        //recalc perms
        user.recalcPerms(server);
      
        //send bukkit update info
        sendPM(user.getName(),"reloadUser;"+user.getName());
    }
    
    /**
     * Adds a permission to the user on the given server in the given world.
     * @param user the user to add the permission to
     * @param server the server to add the permission on
     * @param world the world to add the permission in
     * @param perm the permission to add to the user
     */
    public void addUserPerServerWorldPerm(User user, String server, String world, String perm) 
    {
        //cache
        Map<String, List<String>> perserverperms=user.getServerWorldPerms().get(server);
        if(perserverperms==null)
        {
            perserverperms=new HashMap<>();
        }
        
        List<String> perserverworldperms=perserverperms.get(world);
        if(perserverworldperms==null)
        {
            perserverworldperms=new ArrayList<>();
        }
        
        perserverworldperms.add(perm);
        perserverperms.put(world, perserverworldperms);
        user.getServerWorldPerms().put(server, perserverperms);
        
        //database
        backend.saveUserPerServerWorldPerms(user, server, world);
        
        //recalc perms
        user.recalcPerms(server,world);
        
        //send bukkit update info
        sendPM(user.getName(),"reloadUser;"+user.getName());
    }

    /**
     * Removes a permission from the user on the given server.
     * @param user the user to remove the permission from
     * @param server the server to remove the permission from
     * @param world the world to remove the permission from
     * @param perm the permission to remove from the user
     */
    public void removeUserPerServerWorldPerm(User user, String server, String world, String perm) 
    {
        //cache
        Map<String, List<String>> perserverperms=user.getServerWorldPerms().get(server);
        if(perserverperms==null)
        {
            perserverperms=new HashMap<>();
        }
        
        List<String> perserverworldperms=perserverperms.get(world);
        if(perserverworldperms==null)
        {
            perserverworldperms=new ArrayList<>();
        }
        
        perserverworldperms.remove(perm);
        perserverperms.put(world, perserverworldperms);
        user.getServerWorldPerms().put(server, perserverperms);
        
        //database
        backend.saveUserPerServerWorldPerms(user, server, world);
        
        //recalc perms
        user.recalcPerms(server,world);
        
        //send bukkit update info
        sendPM(user.getName(),"reloadUser;"+user.getName());
    }

    public void addGroupPerm(Group group, String perm)
    {
        //cache
        group.getPerms().add(perm);
        
        //database
        backend.saveGroupPerms(group);
        
        //recalc perms
        group.recalcAllPerms();
        for(User u:users)
        {
            u.recalcPerms();
        }
        
        //send bukkit update info
        sendPMAll("reloadGroup;"+group.getName());
    }
    public void removeGroupPerm(Group group, String perm) 
    {
        //cache
        group.getPerms().remove(perm);
        
        //database
        backend.saveGroupPerms(group);
        
        //recalc perms
        group.recalcAllPerms();
        for(User u:users)
        {
            u.recalcPerms();
        }
        
        //send bukkit update info
        sendPMAll("reloadGroup;"+group.getName());
    }

    public void addGroupPerServerPerm(Group group, String server, String perm)
    {
        //cache
        Server srv=group.getServers().get(server);
        if(srv==null)
        {
            srv=new Server(server,new ArrayList<String>(),new HashMap<String,World>(),"","","");
        }
        
        srv.getPerms().add(perm);
        
        group.getServers().put(server, srv);
        
        //database
        backend.saveGroupPerServerPerms(group, server);
        
        //recalc perms
        group.recalcPerms(server);
        for(User u:users)
        {
            u.recalcPerms();
        }
        
        //send bukkit update info
        sendPMAll("reloadGroup;"+group.getName());
    }
    public void removeGroupPerServerPerm(Group group, String server, String perm)
    {
        //cache
        Server srv=group.getServers().get(server);
        if(srv==null)
        {
            srv=new Server(server,new ArrayList<String>(),new HashMap<String,World>(),"","","");
        }
        
        srv.getPerms().remove(perm);
        
        group.getServers().put(server, srv);
        
        //database
        backend.saveGroupPerServerPerms(group, server);
        
        //recalc perms
        group.recalcPerms(server);
        for(User u:users)
        {
            u.recalcPerms();
        }
        
        //send bukkit update info
        sendPMAll("reloadGroup;"+group.getName());
    }

    public void addGroupPerServerWorldPerm(Group group, String server, String world, String perm) 
    {
        //cache
        Server srv=group.getServers().get(server);
        if(srv==null)
        {
            srv=new Server(server,new ArrayList<String>(),new HashMap<String,World>(),"","","");
        }
        
        World w=srv.getWorlds().get(world);
        if(w==null)
        {
            w=new World(world,new ArrayList<String>(),"","","");
        }
        
        w.getPerms().add(perm);
        srv.getWorlds().put(world, w);
        group.getServers().put(server, srv);
        
        //database
        backend.saveGroupPerServerWorldPerms(group, server, world);
        
        //recalc perms
        group.recalcPerms(server,world);
        for(User u:users)
        {
            u.recalcPerms();
        }
        
        //send bukkit update info
        sendPMAll("reloadGroup;"+group.getName());
    }
    public void removeGroupPerServerWorldPerm(Group group, String server, String world, String perm) 
    {
        //cache
        Server srv=group.getServers().get(server);
        if(srv==null)
        {
            srv=new Server(server,new ArrayList<String>(),new HashMap<String,World>(),"","","");
        }
        
        World w=srv.getWorlds().get(world);
        if(w==null)
        {
            w=new World(world,new ArrayList<String>(),"","","");
        }
        
        w.getPerms().remove(perm);
        srv.getWorlds().put(world, w);
        group.getServers().put(server, srv);
        
        //database
        backend.saveGroupPerServerWorldPerms(group, server, world);
        
        //recalc perms
        group.recalcPerms(server,world);
        for(User u:users)
        {
            u.recalcPerms();
        }
        
        //send bukkit update info
        sendPMAll("reloadGroup;"+group.getName());
    }
    
    public void addGroupInheritance(Group group, Group toadd)
    {
        //cache
        group.getInheritances().add(toadd.getName());
        Collections.sort(group.getInheritances());

        //database
        backend.saveGroupInheritances(group);
        
        //recalc perms
        group.recalcPerms();
        for(User u:users)
        {
            u.recalcPerms();
        }
        
        //send bukkit update info
        sendPMAll("reloadGroup;"+group.getName());
    }
    public void removeGroupInheritance(Group group, Group toremove) 
    {
        //cache
        group.getInheritances().remove(toremove.getName());
        Collections.sort(group.getInheritances());

        //database
        backend.saveGroupInheritances(group);
        
        //recalc perms
        group.recalcPerms();
        for(User u:users)
        {
            u.recalcPerms();
        }
        
        //send bukkit update info
        sendPMAll("reloadGroup;"+group.getName());
    }

    /**
     * Set the ladder for the group.
     * @param group
     * @param ladder
     */
    public void ladderGroup(Group group, String ladder) 
    {
        //cache
        group.setLadder(ladder);
        
        //database
        backend.saveGroupLadder(group);
        
        //send bukkit update info
        sendPMAll("reloadGroup;"+group.getName());
    }
    
    /**
     * Sets the rank for the group.
     * @param group
     * @param rank
     */
    public void rankGroup(Group group, int rank) 
    {
        //cache
        group.setRank(rank);
        Collections.sort(groups);
        
        //database
        backend.saveGroupRank(group);
        
        //send bukkit update info
        sendPMAll("reloadGroup;"+group.getName());
    }
    /**
     * Sets if the the group is a default group.
     * @param group
     * @param isdefault
     */
    public void setGroupDefault(Group group, boolean isdefault) 
    {
        //cache
        group.setIsdefault(isdefault);
        
        //database
        backend.saveGroupDefault(group);
        
        //send bukkit update info
        sendPMAll("reloadGroup;"+group.getName());
    }
    /**
     * Sets the displayname of the group
     * @param group
     * @param display
     */
    public void setGroupDisplay(Group group, String display) 
    {
        //cache
        group.setDisplay(display);
        
        //database
        backend.saveGroupDisplay(group);
        
        //send bukkit update info
        sendPMAll("reloadGroup;"+group.getName());
    }
    /**
     * Sets the prefix for the group.
     * @param group
     * @param prefix
     */
    public void setGroupPrefix(Group group, String prefix)
    {
        //cache
        group.setPrefix(prefix);
        
        //database
        backend.saveGroupPrefix(group);
        
        //send bukkit update info
        sendPMAll("reloadGroup;"+group.getName());
    }
    /**
     * Sets the suffix for the group.
     * @param group
     * @param suffix
     */
    public void setGroupSuffix(Group group, String suffix)
    {
        //cache
        group.setSuffix(suffix);
        
        //database
        backend.saveGroupSuffix(group);
        
        //send bukkit update info
        sendPMAll("reloadGroup;"+group.getName());
    }

    //backend things
    /**
     * Gets the currently use backend.
     * @return the backend instance used
     */
    public BackEnd getBackEnd()
    {
        return backend;
    }
    
    /**
     * Sets the backend to use
     * @param backend the new backend instance to use
     */
    public void setBackEnd(BackEnd backend)
    {
        this.backend = backend;
    }
    
    /**
     * Migrates the permissions to the given backnd type.
     * @param bet the backend type to migrate to
     */
    public synchronized void migrateBackEnd(BackEndType bet)
    {
        if(bet==null)
        {
            throw new NullPointerException("bet must not be null");
        }
        Migrator migrator = null;
        if(bet==BackEndType.MySQL)
        {
            migrator=new Migrate2MySQL(config,debug);
        }
        else if(bet==BackEndType.YAML)
        {
            migrator=new Migrate2YAML(plugin,config);
        }
        
        if(migrator==null)
        {
            throw new UnsupportedOperationException("bet=="+bet.name());
        }
        
        migrator.migrate(backend.loadGroups(), backend.loadUsers(), permsversion);
        
        backend.load();
    }
    
    //perms per world
    @EventHandler
    public void onMessage(PluginMessageEvent e)
    {
        if(!e.getTag().equalsIgnoreCase(channel))
        {
            return;
        }
        
        String msg=new String(e.getData());
        List<String> data=Statics.toList(msg, ";");
        if(data.get(0).equalsIgnoreCase("updateplayerworld"))
        {
            String player=data.get(1);
            String world=data.get(2);
            
            playerWorlds.put(player, world);
        }
        
        e.setCancelled(true);
    }

    //bukkit-bungeeperms reload information functions
    public void sendPM(String player,String msg)
    {
        ProxiedPlayer pp=BungeeCord.getInstance().getPlayer(player);
        if(pp!=null)
        {
            pp.getServer().getInfo().sendData(channel, msg.getBytes());
        }
    }
    public void sendPMAll(String msg) 
    {
        for(ServerInfo si:BungeeCord.getInstance().config.getServers().values())
        {
            si.sendData(channel, msg.getBytes());
        }
    }
}