package net.alpenblock.bungeeperms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PermissionCheckEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.command.ConsoleCommandSender;
import net.md_5.bungee.event.EventHandler;

// TODO: Auto-generated Javadoc
/**
 * The Class PermissionsManager.
 */
public class PermissionsManager implements Listener
{
	
	/** The bc. */
	private BungeeCord bc;
	
	/** The plugin. */
	private Plugin plugin;
	
	/** The groups. */
	private List<Group> groups;
	
	/** The players. */
	private List<Player> players;
	
	/** The permsconf. */
	private Config permsconf;
	
	/** The permspath. */
	private String permspath;
	
	/** The permsversion. */
	private int permsversion;
	
	/**
	 * Instantiates a new permissions manager.
	 *
	 * @param p the p
	 */
	public PermissionsManager(Plugin p)
	{
		bc=BungeeCord.getInstance();
		plugin=p;
		
		this.groups=new ArrayList<Group>();
		this.players=new ArrayList<Player>();
		//load perms form file
		permspath="/permissions.yml";
		permsconf=new Config(p, permspath);
		loadPerms();
		bc.getPluginManager().registerListener(plugin,this);
	}
	
	/**
	 * Load perms.
	 */
	public void loadPerms()
	{
		this.groups.clear();
		this.players.clear();
		
		//load from file
		permsconf.load();
		
		//version
		permsversion=permsconf.getInt("version", 1);
		
		//load groups
		List<String> groups=permsconf.getSubNodes("groups");
		for(String g:groups)
		{
			List<String> inheritances=permsconf.getListString("groups."+g+".inheritances", new ArrayList<String>());
			List<String> permissions=permsconf.getListString("groups."+g+".permissions", new ArrayList<String>());
			boolean isdefault=permsconf.getBoolean("groups."+g+".default",false);
			int rank=permsconf.getInt("groups."+g+".rank", 1000);
			String display=permsconf.getString("groups."+g+".display", "");
			String prefix=permsconf.getString("groups."+g+".prefix", "");
			String suffix=permsconf.getString("groups."+g+".suffix", "");
			
			//per server perms
			Map<String,List<String>> serverperms=new HashMap<>();
			for(String server:permsconf.getSubNodes("groups."+g+".servers"))
			{
				serverperms.put(server, permsconf.getListString("groups."+g+".servers."+server+".permissions", new ArrayList<String>()));
			}
			
			Group group=new Group(g, inheritances, permissions, serverperms, rank, isdefault, display, prefix, suffix);
			this.groups.add(group);
		}
		sortGroups();
		
		//load users
		List<String> users=permsconf.getSubNodes("users");
		for(String u:users)
		{
			List<String> sgroups=permsconf.getListString("users."+u+".groups", new ArrayList<String>());
			List<Group> lgroups=new ArrayList<Group>();
			for(String s:sgroups)
			{
				Group g=getGroup(s);
				if(g!=null)
				{
					lgroups.add(g);
				}
			}
			List<String> extrapermissions=permsconf.getListString("users."+u+".permissions", new ArrayList<String>());
			
			Map<String,List<String>> serverperms=new HashMap<>();
			for(String server:permsconf.getSubNodes("users."+u+".servers"))
			{
				serverperms.put(server, permsconf.getListString("users."+u+".servers."+server+".permissions", new ArrayList<String>()));
			}
			
			Player player=new Player(u, lgroups, extrapermissions, serverperms);
			this.players.add(player);
		}
	}
	
	/**
	 * Sort groups.
	 */
	public void sortGroups()
	{
		Collections.sort(this.groups, new Comparator<Group>()
				{
					@Override
					public int compare(Group arg0, Group arg1) 
					{
						return -Integer.compare(arg0.getRank(), arg1.getRank());
					}
				});
	}
	
	/**
	 * Validate user groups.
	 */
	public void validateUserGroups() 
	{
		for(int i=0;i<players.size();i++)
		{
			Player p=players.get(i);
			List<Group> pgroups=p.getGroups();
			for(int j=0;j<pgroups.size();j++)
			{
				if(getGroup(pgroups.get(j).getName())==null)
				{
					pgroups.remove(j);
					j--;
				}
			}
			p.setGroups(pgroups);
			players.set(i, p);
			saveUser(p);
		}
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
			group.setInheritances(inheritances);
			groups.set(i, group);
			saveGroup(group);
		}
	}

	/**
	 * Gets the main group.
	 *
	 * @param player the player
	 * @return the main group
	 */
	public Group getMainGroup(Player player) 
	{
		if(player==null)
		{
			throw new NullPointerException("player is null");
		}
		if(player.getGroups().size()==0)
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
	 * Gets the next group.
	 *
	 * @param group the group
	 * @return the next group
	 */
	public Group getNextGroup(Group group)
	{
		for(int i=0;i<groups.size();i++)
		{
			if(groups.get(i).getRank()==group.getRank())
			{
				if(i+1<groups.size())
				{
					return groups.get(i+1);
				}
				else
				{
					return null;
				}
			}
		}
		throw new IllegalArgumentException("group does not exist (anymore)");
	}
	
	/**
	 * Gets the previous group.
	 *
	 * @param group the group
	 * @return the previous group
	 */
	public Group getPreviousGroup(Group group)
	{
		for(int i=0;i<groups.size();i++)
		{
			if(groups.get(i).getRank()==group.getRank())
			{
				if(i>0)
				{
					return groups.get(i-1);
				}
				else
				{
					return null;
				}
			}
		}
		throw new IllegalArgumentException("group does not exist (anymore)");
	}
	
	/**
	 * Gets the group.
	 *
	 * @param groupname the groupname
	 * @return the group
	 */
	public Group getGroup(String groupname)
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
	 * Gets the user.
	 *
	 * @param username the username
	 * @return the user
	 */
	public Player getUser(String username)
	{
		for(Player p:players)
		{
			if(p.getName().equalsIgnoreCase(username))
			{
				return p;
			}
		}
		return null;
	}
	
	/**
	 * Gets the default groups.
	 *
	 * @return the default groups
	 */
	public List<Group> getDefaultGroups()
	{
		List<Group> ret=new ArrayList<Group>();
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
	 * Gets the groups.
	 *
	 * @return the groups
	 */
	public List<Group> getGroups()
	{
		return Collections.unmodifiableList(groups);
	}
	
	/**
	 * Gets the users.
	 *
	 * @return the users
	 */
	public List<Player> getUsers()
	{
		return Collections.unmodifiableList(players);
	}
	
	/**
	 * Save user.
	 *
	 * @param p the p
	 */
	public synchronized void saveUser(Player p) 
	{
		List<String> groups=new ArrayList<>();
		for(Group g:p.getGroups())
		{
			groups.add(g.getName());
		}
		permsconf.setListString("users."+p.getName()+".groups", groups);
		permsconf.setListString("users."+p.getName()+".permissions", p.getExtraperms());

		for(String server:p.getServerPerms().keySet())
		{
			permsconf.setListString("users."+p.getName()+".servers."+server+".permissions", p.getServerPerms().get(server));
		}
	}	
	
	/**
	 * Save group.
	 *
	 * @param g the g
	 */
	public synchronized void saveGroup(Group g)
	{
		permsconf.setListString("groups."+g.getName()+".inheritances", g.getInheritances());
		permsconf.setListString("groups."+g.getName()+".permissions", g.getPerms());
		permsconf.setInt("groups."+g.getName()+".rank", g.getRank());
		permsconf.setBool("groups."+g.getName()+".default", g.isDefault());
		permsconf.setString("groups."+g.getName()+".display", g.getDisplay());
		permsconf.setString("groups."+g.getName()+".prefix", g.getPrefix());
		permsconf.setString("groups."+g.getName()+".suffix", g.getSuffix());

		for(String server:g.getServerPerms().keySet())
		{
			permsconf.setListString("groups."+g.getName()+".servers."+server+".permissions", g.getServerPerms().get(server));
		}
	}
	
	/**
	 * Delete user.
	 *
	 * @param user the user
	 */
	public synchronized void deleteUser(Player user) 
	{
		for(int i=0;i<players.size();i++)
		{
			if(players.get(i).getName().equalsIgnoreCase(user.getName()))
			{
				players.remove(i);
				return;
			}
		}
		permsconf.deleteNode("users."+user.getName());
	}
	
	/**
	 * Delete group.
	 *
	 * @param group the group
	 */
	public synchronized void deleteGroup(Group group) 
	{
		for(int i=0;i<groups.size();i++)
		{
			if(groups.get(i).getName().equalsIgnoreCase(group.getName()))
			{
				groups.remove(i);
				permsconf.deleteNode("groups."+group.getName());
				validateUserGroups();
				return;
			}
		}
	}
	
	/**
	 * Update user.
	 *
	 * @param user the user
	 */
	public synchronized void updateUser(Player user) 
	{
		for(int i=0;i<players.size();i++)
		{
			if(players.get(i).getName().equalsIgnoreCase(user.getName()))
			{
				players.set(i,user);
				saveUser(user);
				return;
			}
		}
	}
	
	/**
	 * Update group.
	 *
	 * @param group the group
	 */
	public synchronized void updateGroup(Group group) 
	{
		for(int i=0;i<groups.size();i++)
		{
			if(groups.get(i).getName().equalsIgnoreCase(group.getName()))
			{
				groups.set(i,group);
				saveGroup(group);
				sortGroups();
				return;
			}
		}
	}
	
	/**
	 * Adds the user.
	 *
	 * @param user the user
	 */
	public synchronized void addUser(Player user) 
	{
		players.add(user);
		saveUser(user);
	}
	
	/**
	 * Adds the group.
	 *
	 * @param group the group
	 */
	public synchronized void addGroup(Group group) 
	{
		groups.add(group);
		sortGroups();
		saveGroup(group);
	}
	
	/**
	 * On login.
	 *
	 * @param e the e
	 */
	@EventHandler
	public void onLogin(LoginEvent e)
	{
		bc.getLogger().info("[BungeePerms] Login by "+e.getConnection().getName());
		boolean found=false;
		for(int i=0;i<players.size();i++)
		{
			if(e.getConnection().getName().equalsIgnoreCase(players.get(i).getName()))
			{
				found=true;
				break;
			}
		}
		if(!found)
		{
			String playername=e.getConnection().getName();
			bc.getLogger().info("[BungeePerms] Adding default groups to "+playername);
			List<Group> groups=getDefaultGroups();
			Player p=new Player(playername, groups, new ArrayList<String>(),new HashMap<String, List<String>>());
			//save to cache
			players.add(p);
			saveUser(p);
		}
	}
	
	/**
	 * On permission check.
	 *
	 * @param e the e
	 */
	@EventHandler
	public void onPermissionCheck(PermissionCheckEvent e)
	{
		e.setHasPermission(hasPermOrConsoleOnServer(e.getSender(),e.getPermission()));
	}
	
	/**
	 * Checks for perm.
	 *
	 * @param sender the sender
	 * @param permission the permission
	 * @return true, if successful
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
	 * Checks for perm or console.
	 *
	 * @param sender the sender
	 * @param permission the permission
	 * @return true, if successful
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
	 * Checks for perm.
	 *
	 * @param sender the sender
	 * @param permission the permission
	 * @return true, if successful
	 */
	public boolean hasPerm(String sender, String permission)
	{
		if(!sender.equalsIgnoreCase("CONSOLE"))
		{
			Player p=getUser(sender);
			if(p==null)
			{
				return false;
			}
			return p.hasPerm(permission);
		}
		return false;
	}
	
	/**
	 * Checks for perm or console.
	 *
	 * @param sender the sender
	 * @param permission the permission
	 * @return true, if successful
	 */
	public boolean hasPermOrConsole(String sender, String permission)
	{
		if(sender.equalsIgnoreCase("CONSOLE"))
		{
			return true;
		}
		else
		{
			Player p=getUser(sender);
			if(p==null)
			{
				return false;
			}
			return p.hasPerm(permission);
		}
	}
	
	/**
	 * Checks for.
	 *
	 * @param sender the sender
	 * @param perm the perm
	 * @param msg the msg
	 * @return true, if successful
	 */
	public boolean has(CommandSender sender, String perm, boolean msg)
	{
		if(sender instanceof Player)
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
	 * Checks for or console.
	 *
	 * @param sender the sender
	 * @param perm the perm
	 * @param msg the msg
	 * @return true, if successful
	 */
	public boolean hasOrConsole(CommandSender sender, String perm, boolean msg)
	{
		boolean isperm=(hasPerm(sender, perm)|(sender instanceof ConsoleCommandSender));
		if(!isperm & msg){sender.sendMessage(Color.Error+"You don't have permission to do that!"+ChatColor.RESET);}
		return isperm;
	}

	/**
	 * Checks for perm on server.
	 *
	 * @param sender the sender
	 * @param permission the permission
	 * @return true, if successful
	 */
	public boolean hasPermOnServer(CommandSender sender, String permission)
	{
		if(sender instanceof ProxiedPlayer)
		{
			Player user=getUser(sender.getName());
			if(((ProxiedPlayer) sender).getServer()==null)
			{
				return user.hasPerm(permission);
			}
			return user.hasPermOnServer(permission,((ProxiedPlayer) sender).getServer().getInfo());
		}
		return false;
	}
	
	/**
	 * Checks for perm or console on server.
	 *
	 * @param sender the sender
	 * @param permission the permission
	 * @return true, if successful
	 */
	public boolean hasPermOrConsoleOnServer(CommandSender sender, String permission)
	{
		if(sender instanceof ProxiedPlayer)
		{
			Player user=getUser(sender.getName());
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
	 * Checks for perm on server.
	 *
	 * @param sender the sender
	 * @param permission the permission
	 * @param server the server
	 * @return true, if successful
	 */
	public boolean hasPermOnServer(String sender, String permission,ServerInfo server)
	{
		if(!sender.equalsIgnoreCase("CONSOLE"))
		{
			Player p=getUser(sender);
			if(p==null)
			{
				return false;
			}
			return p.hasPermOnServer(permission,server);
		}
		return false;
	}
	
	/**
	 * Checks for perm or console on server.
	 *
	 * @param sender the sender
	 * @param permission the permission
	 * @param server the server
	 * @return true, if successful
	 */
	public boolean hasPermOrConsoleOnServer(String sender, String permission,ServerInfo server)
	{
		if(sender.equalsIgnoreCase("CONSOLE"))
		{
			return true;
		}
		else
		{
			Player p=getUser(sender);
			if(p==null)
			{
				return false;
			}
			return p.hasPermOnServer(permission,server);
		}
	}
	
	/**
	 * Checks for on server.
	 *
	 * @param sender the sender
	 * @param perm the perm
	 * @param msg the msg
	 * @return true, if successful
	 */
	public boolean hasOnServer(CommandSender sender, String perm, boolean msg)
	{
		if(sender instanceof Player)
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
	 * Checks for or console on server.
	 *
	 * @param sender the sender
	 * @param perm the perm
	 * @param msg the msg
	 * @return true, if successful
	 */
	public boolean hasOrConsoleOnServer(CommandSender sender, String perm, boolean msg)
	{
		boolean isperm=(hasPermOnServer(sender, perm)|(sender instanceof ConsoleCommandSender));
		if(!isperm & msg){sender.sendMessage(Color.Error+"You don't have permission to do that!"+ChatColor.RESET);}
		return isperm;
	}
}