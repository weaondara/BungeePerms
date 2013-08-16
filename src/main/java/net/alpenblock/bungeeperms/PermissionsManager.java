package net.alpenblock.bungeeperms;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.command.ConsoleCommandSender;
import net.md_5.bungee.event.EventBus;
import net.md_5.bungee.event.EventHandler;

public class PermissionsManager implements Listener
{
	private BungeeCord bc;
	private Plugin plugin;
    private Config config;
    private Debug debug;
    private boolean enabled;
    
    
    private boolean saveAllUsers;
    private boolean deleteUsersOnCleanup;
    
    private BackEnd backend;
	
	public PermissionsManager(Plugin p,Config conf,Debug d)
	{
		bc=BungeeCord.getInstance();
		plugin=p;
        config=conf;
        debug=d;
		
        //config
        loadConfig();
        
		//perms
		loadPerms();
        
        enabled=false;
	}
	
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
	public final void loadPerms()
	{
		bc.getLogger().info("[BungeePerms] loading permissions ...");
		
        backend.load();
		
		bc.getLogger().info("[BungeePerms] permissions loaded");
	}
    
    public void enable()
    {
        if(!enabled)
        {
            bc.getPluginManager().registerListener(plugin,this);
            enabled=true;
        }
    }
    public void disable()
    {
        if(!enabled)
        {
            //since md-5 doesn't provide an unregister function we have to do this via reflections
            try
            {
                Field f=bc.getPluginManager().getClass().getDeclaredField("eventBus");
                f.setAccessible(true);
                EventBus bus=(EventBus) f.get(bc.getPluginManager());
                bus.unregister(this);
            } catch (Exception ex) {ex.printStackTrace();}
            enabled=false;
        }
    }
	
	public synchronized void validateUserGroups() 
	{
        //user check
        List<User> users=backend.getUsers();
		for(int i=0;i<users.size();i++)
		{
			User u=users.get(i);
			List<Group> pgroups=u.getGroups();
			for(int j=0;j<pgroups.size();j++)
			{
				if(getGroup(pgroups.get(j).getName())==null)
				{
                    backend.removeUserGroup(u,pgroups.get(j));
					j--;
				}
			}
		}
        
        //group check
        List<Group> groups=backend.getGroups();
		for(int i=0;i<groups.size();i++)
		{
			Group group=groups.get(i);
			List<String> inheritances=group.getInheritances();
			for(int j=0;j<inheritances.size();j++)
			{
				if(getGroup(inheritances.get(j))==null)
				{
					backend.removeGroupInheritance(group,inheritances.get(j));
					j--;
				}
			}
		}
	}

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
    public synchronized List<Group> getLadderGroups(String ladder)
    {
        List<Group> ret=new ArrayList<>();
        
        for(Group g:backend.getGroups())
        {
            if(g.getLadder().equalsIgnoreCase(ladder))
            {
                ret.add(g);
            }
        }
        
        Collections.sort(ret);
        
        return ret;
    }
	
	public synchronized Group getGroup(String groupname)
	{
		return backend.getGroup(groupname);
	}
	public synchronized User getUser(String username)
	{
		return backend.getUser(username);
	}
	
	public synchronized List<Group> getDefaultGroups()
	{
		List<Group> ret=new ArrayList<>();
		for(Group g:backend.getGroups())
		{
			if(g.isDefault())
			{
				ret.add(g);
			}
		}
		return ret;
	}
	
	public List<Group> getGroups()
	{
		return Collections.unmodifiableList(backend.getGroups());
	}
	public List<User> getUsers()
	{
		return Collections.unmodifiableList(backend.getUsers());
	}
	
	public synchronized void deleteUser(User user) 
	{
		backend.deleteUser(user);
	}
	public synchronized void deleteGroup(Group group) 
	{
		backend.deleteGroup(group);
	}
	
	public synchronized void addUser(User user) 
	{
		backend.addUser(user);
	}
	public synchronized void addGroup(Group group) 
	{
		backend.addGroup(group);
	}
	
	@EventHandler
	public void onLogin(LoginEvent e)
	{
        String playername=e.getConnection().getName();
		bc.getLogger().log(Level.INFO, "[BungeePerms] Login by {0}", playername);
        
        User u=backend.getUser(playername);
        if(u==null)
        {
			bc.getLogger().log(Level.INFO, "[BungeePerms] Adding default groups to {0}", playername);
            
			List<Group> groups=getDefaultGroups();
			u=new User(playername, groups, new ArrayList<String>(),new HashMap<String, List<String>>());
            
			backend.addUser(u);
        }
	}
	@EventHandler
	public void onPermissionCheck(PermissionCheckEvent e)
	{
		e.setHasPermission(hasPermOrConsoleOnServer(e.getSender(),e.getPermission()));
	}
	
	public boolean hasPerm(CommandSender sender, String permission)
	{
		if(sender instanceof ProxiedPlayer)
		{
			return getUser(sender.getName()).hasPerm(permission);
		}
		return false;
	}
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
	public boolean hasOrConsole(CommandSender sender, String perm, boolean msg)
	{
		boolean isperm=(hasPerm(sender, perm)|(sender instanceof ConsoleCommandSender));
		if(!isperm & msg){sender.sendMessage(Color.Error+"You don't have permission to do that!"+ChatColor.RESET);}
		return isperm;
	}
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
	public boolean hasOrConsoleOnServer(CommandSender sender, String perm, boolean msg)
	{
		boolean isperm=(hasPermOnServer(sender, perm)|(sender instanceof ConsoleCommandSender));
		if(!isperm & msg){sender.sendMessage(Color.Error+"You don't have permission to do that!"+ChatColor.RESET);}
		return isperm;
	}

    public void format() 
    {
        backend.format();
    }
    public int cleanup() 
    {
        return backend.cleanup();
    }

    public void addUserGroup(User user, Group group)
    {
        backend.addUserGroup(user, group);
    }
    public void removeUserGroup(User user, Group group)
    {
        backend.removeUserGroup(user, group);
    }
    
    public void addUserPerm(User user, String perm)
    {
        backend.addUserPerm(user, perm);
    }
    public void removeUserPerm(User user, String perm) 
    {
        backend.removeUserPerm(user, perm);
    }

    public void addUserPerServerPerm(User user, String server, String perm) 
    {
        backend.addUserPerServerPerm(user, server, perm);
    }
    public void removeUserPerServerPerm(User user, String server, String perm) 
    {
        backend.removeUserPerServerPerm(user, server, perm);
    }

    public void addGroupPerm(Group group, String perm)
    {
        backend.addGroupPerm(group, perm);
    }
    public void removeGroupPerm(Group group, String perm) 
    {
        backend.removeGroupPerm(group, perm);
    }

    public void addGroupPerServerPerm(Group group, String server, String perm)
    {
        backend.addGroupPerServerPerm(group, server, perm);
    }
    public void removeGroupPerServerPerm(Group group, String server, String perm)
    {
        backend.removeGroupPerServerPerm(group, server, perm);
    }

    public void addGroupInheritance(Group group, Group toadd)
    {
        backend.addGroupInheritance(group, toadd.getName());
    }
    public void removeGroupInheritance(Group group, Group toremove) 
    {
         backend.removeGroupInheritance(group, toremove.getName());
    }

    public void rankGroup(Group group, int rank) 
    {
        backend.rankGroup(group, rank);
    }
    public void ladderGroup(Group group, String ladder) 
    {
        backend.ladderGroup(group, ladder);
    }
    public void setGroupDefault(Group group, boolean isdefault) 
    {
        backend.setGroupDefault(group, isdefault);
    }
    public void setGroupDisplay(Group group, String display) 
    {
        backend.setGroupDisplay(group, display);
    }
    public void setGroupPrefix(Group group, String prefix)
    {
        backend.setGroupPrefix(group, prefix);
    }
    public void setGroupSuffix(Group group, String suffix)
    {
        backend.setGroupSuffix(group, suffix);
    }

    public BackEnd getBackEnd() {
        return backend;
    }
    public void setBackEnd(BackEnd backend) {
        this.backend = backend;
    }
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
        
        migrator.migrate(backend.getGroups(), backend.getUsers(), backend.getVersion());
    }

}