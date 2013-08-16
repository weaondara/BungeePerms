package net.alpenblock.bungeeperms.io;

import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.alpenblock.bungeeperms.BungeePerms;
import net.alpenblock.bungeeperms.Config;
import net.alpenblock.bungeeperms.Group;
import net.alpenblock.bungeeperms.Server;
import net.alpenblock.bungeeperms.User;
import net.alpenblock.bungeeperms.config.YamlConfiguration;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.plugin.Plugin;

public class YAMLBackEnd implements BackEnd
{
    private BungeeCord bc;
    private Plugin plugin;
    
    private String permspath;
    private Config permsconf;
    
    private List<Group> groups;
    private List<User> users;
    private int permsversion;
    
    private boolean saveAllUsers;
    private boolean deleteUsersOnCleanup;
    
    public YAMLBackEnd(BungeeCord bc, Plugin p,boolean saveAllUsers, boolean deleteUsersOnCleanup)
    {
        this.bc=bc;
        plugin=p;
        this.saveAllUsers=saveAllUsers;
        this.deleteUsersOnCleanup=deleteUsersOnCleanup;
        
        permspath="/permissions.yml";
        
        //inits
        groups=new ArrayList<>();
        users=new ArrayList<>();
        
        checkPermFile();
        
        permsconf=new Config(plugin,permspath);
    }
    
    @Override
    public BackEndType getType()
    {
        return BackEndType.YAML;
    }
    
    @Override
    public void load()
    {
        this.groups.clear();
		this.users.clear();
		
        //check if perms file exists; if not copy default packed permissions.yml
        checkPermFile();
        
        
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
			String ladder=permsconf.getString("groups."+g+".ladder", "default");
			String display=permsconf.getString("groups."+g+".display", "");
			String prefix=permsconf.getString("groups."+g+".prefix", "");
			String suffix=permsconf.getString("groups."+g+".suffix", "");
			
			//per server perms
			Map<String,Server> servers=new HashMap<>();
			for(String server:permsconf.getSubNodes("groups."+g+".servers"))
			{
				List<String> serverperms=permsconf.getListString("groups."+g+".servers."+server+".permissions", new ArrayList<String>());
                String sdisplay=permsconf.getString("groups."+g+".servers."+server+".display", "");
                String sprefix=permsconf.getString("groups."+g+".servers."+server+".prefix", "");
                String ssuffix=permsconf.getString("groups."+g+".servers."+server+".suffix", "");
                servers.put(server, new Server(server,serverperms,sdisplay,sprefix,ssuffix));
			}
			
			Group group=new Group(g, inheritances, permissions, servers, rank, ladder, isdefault, display, prefix, suffix);
			this.groups.add(group);
		}
        Collections.sort(this.groups);
		
		//load users
		List<String> users=permsconf.getSubNodes("users");
		for(String u:users)
		{
			List<String> sgroups=permsconf.getListString("users."+u+".groups", new ArrayList<String>());
			List<Group> lgroups=new ArrayList<>();
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
			
			User user=new User(u, lgroups, extrapermissions, serverperms);
			this.users.add(user);
		}
    }
    
    @Override
    public List<Group> getGroups() 
    {
        return groups;
    }
    @Override
    public List<User> getUsers()
    {
        return users;
    }
    @Override
    public int getVersion()
    {
        return permsversion;
    }
    @Override
    public void setVersion(int version)
    {
        permsconf.setInt("version", version);
    }

    @Override
    public boolean isUserInDatabase(User user)
    {
        return permsconf.keyExists("users."+user.getName());
    }
    
    @Override
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
    @Override
    public synchronized User getUser(String username)
	{
		for(User u:users)
		{
			if(u.getName().equalsIgnoreCase(username))
			{
				return u;
			}
		}
		return null;
	}

    @Override
    public synchronized void addUser(User user)
    {
        users.add(user);
		
        if(saveAllUsers?true:!user.isNothingSpecial())
        {
            List<String> groups=new ArrayList<>();
            for(Group g:user.getGroups())
            {
                groups.add(g.getName());
            }
            permsconf.setListString("users."+user.getName()+".groups", groups);
            permsconf.setListString("users."+user.getName()+".permissions", user.getExtraperms());

            for(String server:user.getServerPerms().keySet())
            {
                permsconf.setListString("users."+user.getName()+".servers."+server+".permissions", user.getServerPerms().get(server));
            }

            permsconf.save();
        }
    }
    @Override
    public synchronized void addGroup(Group group)
    {
        groups.add(group);
		Collections.sort(groups);
		
        //save
        permsconf.setListString("groups."+group.getName()+".inheritances", group.getInheritances());
		permsconf.setListString("groups."+group.getName()+".permissions", group.getPerms());
		permsconf.setInt("groups."+group.getName()+".rank", group.getRank());
		permsconf.setString("groups."+group.getName()+".ladder", group.getLadder());
        permsconf.setBool("groups."+group.getName()+".default", group.isDefault());
		permsconf.setString("groups."+group.getName()+".display", group.getDisplay());
		permsconf.setString("groups."+group.getName()+".prefix", group.getPrefix());
		permsconf.setString("groups."+group.getName()+".suffix", group.getSuffix());

		for(String server:group.getServers().keySet())
		{
            Server s=group.getServers().get(server);
			permsconf.setListString("groups."+group.getName()+".servers."+server+".permissions", s.getPerms());
            permsconf.setString("groups."+group.getName()+".servers."+server+".display", s.getDisplay());
            permsconf.setString("groups."+group.getName()+".servers."+server+".prefix", s.getPrefix());
            permsconf.setString("groups."+group.getName()+".servers."+server+".suffix", s.getSuffix());
		}
        
        permsconf.save();
    }

    @Override
    public synchronized void deleteUser(User user)
    {
        for(int i=0;i<users.size();i++)
		{
			if(users.get(i).getName().equalsIgnoreCase(user.getName()))
			{
				users.remove(i);
                permsconf.deleteNode("users."+user.getName());
				return;
			}
		}
    }
    @Override
    public synchronized void deleteGroup(Group group)
    {
        for(int i=0;i<groups.size();i++)
		{
			if(groups.get(i).getName().equalsIgnoreCase(group.getName()))
			{
				groups.remove(i);
				permsconf.deleteNode("groups."+group.getName());
				BungeePerms.getInstance().getPermissionsManager().validateUserGroups();
                permsconf.save();
				return;
			}
		}
    }

    @Override
    public synchronized void addUserGroup(User user, Group group)
    {
        user.getGroups().add(group);
        Collections.sort(user.getGroups());
        
        List<String> savegroups=new ArrayList<>();
        for(Group g:user.getGroups())
        {
            savegroups.add(g.getName());
        }
        
        permsconf.setListStringAndSave("users."+user.getName()+".groups", savegroups);
        
        user.recalcAllPerms();
    }
    @Override
    public synchronized void removeUserGroup(User user, Group group)
    {
        user.getGroups().remove(group);
        
        List<String> savegroups=new ArrayList<>();
        for(Group g:user.getGroups())
        {
            savegroups.add(g.getName());
        }
        
        permsconf.setListStringAndSave("users."+user.getName()+".groups", savegroups);
        
        user.recalcAllPerms();
    }

    @Override
    public synchronized void addUserPerm(User user, String perm)
    {
        user.getExtraperms().add(perm);
        permsconf.setListStringAndSave("users."+user.getName()+".permissions", user.getExtraperms());
        user.recalcPerms();
    }
    @Override
    public synchronized void removeUserPerm(User user, String perm)
    {
        user.getExtraperms().remove(perm);
        permsconf.setListStringAndSave("users."+user.getName()+".permissions", user.getExtraperms());
        user.recalcPerms();
    }

    @Override
    public synchronized void addUserPerServerPerm(User user, String server, String perm) 
    {
        List<String> perserverperms=user.getServerPerms().get(server);
        if(perserverperms==null)
        {
            perserverperms=new ArrayList<>();
        }
        
        perserverperms.add(perm);
        user.getServerPerms().put(server, perserverperms);
        
        permsconf.setListStringAndSave("users."+user.getName()+".servers."+server+".permissions", perserverperms);
        
        user.recalcPerms(server);
    }
    @Override
    public synchronized void removeUserPerServerPerm(User user, String server, String perm) 
    {
        List<String> perserverperms=user.getServerPerms().get(server);
        if(perserverperms==null)
        {
            return;
        }
        
        perserverperms.remove(perm);
        user.getServerPerms().put(server, perserverperms);
        
        permsconf.setListStringAndSave("users."+user.getName()+".servers."+server+".permissions", perserverperms);
        
        user.recalcPerms(server);
    }
    
    @Override
    public synchronized void addGroupPerm(Group group, String perm)
    {
        group.getPerms().add(perm);
        permsconf.setListStringAndSave("groups."+group.getName()+".permissions", group.getPerms());
        group.recalcPerms();
    }
    @Override
    public synchronized void removeGroupPerm(Group group, String perm)
    {
        group.getPerms().remove(perm);
        permsconf.setListStringAndSave("groups."+group.getName()+".permissions", group.getPerms());
        group.recalcPerms();
    }

    @Override
    public synchronized void addGroupPerServerPerm(Group group, String server, String perm) 
    {
        Server srv=group.getServers().get(server);
        if(srv==null)
        {
            srv=new Server(server,new ArrayList<String>(),"","","");
        }
        
        srv.getPerms().add(perm);
        
        group.getServers().put(server, srv);
        
        permsconf.setListStringAndSave("groups."+group.getName()+".servers."+server+".permissions", srv.getPerms());
        
        group.recalcPerms(server);
    }
    @Override
    public synchronized void removeGroupPerServerPerm(Group group, String server, String perm) 
    {
        Server srv=group.getServers().get(server);
        if(srv==null)
        {
            return;
        }
        
        srv.getPerms().remove(perm);
        
        group.getServers().put(server, srv);
        
        permsconf.setListStringAndSave("groups."+group.getName()+".servers."+server+".permissions", srv.getPerms());
        
        group.recalcPerms(server);
    }

    @Override
    public synchronized void addGroupInheritance(Group group, String toadd)
    {
        group.getInheritances().add(toadd);
        Collections.sort(group.getInheritances());
        permsconf.setListStringAndSave("groups."+group.getName()+".inheritances", group.getInheritances());
        group.recalcAllPerms();
    }
    @Override
    public synchronized void removeGroupInheritance(Group group, String toremove)
    {
        group.getInheritances().remove(toremove);
        permsconf.setListStringAndSave("groups."+group.getName()+".inheritances", group.getInheritances());
        group.recalcAllPerms();
    }

    @Override
    public synchronized void ladderGroup(Group group, String ladder)
    {
        group.setLadder(ladder);
        permsconf.setStringAndSave("groups."+group.getName()+".ladder", group.getLadder());
    }
    @Override
    public synchronized void rankGroup(Group group, int rank)
    {
        group.setRank(rank);
        Collections.sort(groups);
        permsconf.setIntAndSave("groups."+group.getName()+".rank", group.getRank());
    }
    @Override
    public synchronized void setGroupDefault(Group group, boolean isdefault)
    {
        group.setIsdefault(isdefault);
        permsconf.setBoolAndSave("groups."+group.getName()+".default", group.isDefault());
    }
    @Override
    public synchronized void setGroupDisplay(Group group, String display)
    {
        group.setDisplay(display);
        permsconf.setStringAndSave("groups."+group.getName()+".display", group.getDisplay());
    }
    @Override
    public synchronized void setGroupPrefix(Group group, String prefix)
    {
        group.setPrefix(prefix);
        permsconf.setStringAndSave("groups."+group.getName()+".prefix", group.getPrefix());
    }
    @Override
    public synchronized void setGroupSuffix(Group group, String suffix)
    {
        group.setSuffix(suffix);
        permsconf.setStringAndSave("groups."+group.getName()+".suffix", group.getSuffix());
    }
    
    @Override
    public synchronized void format() 
    {
        new File(plugin.getDataFolder()+"/permissions.yml").delete();
        Config newperms=new Config(plugin,"/permissions.yml");
        for(int i=0;i<groups.size();i++)
        {
            Group g=groups.get(i);
            newperms.setInt("groups."+g.getName()+".rank", g.getRank());
            newperms.setString("groups."+g.getName()+".ladder", g.getLadder());
            newperms.setBool("groups."+g.getName()+".default", g.isDefault());
            newperms.setString("groups."+g.getName()+".display",g.getDisplay());
            newperms.setString("groups."+g.getName()+".prefix",g.getPrefix());
            newperms.setString("groups."+g.getName()+".suffix",g.getSuffix());
            newperms.setListString("groups."+g.getName()+".inheritances", g.getInheritances());
            newperms.setListString("groups."+g.getName()+".permissions", g.getPerms());
            for(String server:g.getServers().keySet())
            {
                Server s=g.getServers().get(server);
                newperms.setListString("groups."+g.getName()+".servers."+server+".permissions", s.getPerms());
                newperms.setString("groups."+g.getName()+".servers."+server+".display", s.getDisplay());
                newperms.setString("groups."+g.getName()+".servers."+server+".prefix", s.getPrefix());
                newperms.setString("groups."+g.getName()+".servers."+server+".suffix", s.getSuffix());
            }
        }
        for(User p:users)
        {
            List<String> groups=new ArrayList<>();
            for(Group g:p.getGroups())
            {
                groups.add(g.getName());
            }
            newperms.setListString("users."+p.getName()+".groups", groups);
            
            newperms.setListString("users."+p.getName()+".permissions", p.getExtraperms());
            for(String server:p.getServerPerms().keySet())
            {
                newperms.setListString("users."+p.getName()+".servers."+server+".permissions", p.getServerPerms().get(server));
            }
        }
        newperms.setInt("version", 1);
        
        newperms.save();
        
        permsconf=newperms;
        
        permsconf.load();
    }
    @Override
    public synchronized int cleanup() 
    {
        int deleted=0;
        
        new File(plugin.getDataFolder()+"/permissions.yml").delete();
        Config newperms=new Config(plugin,"/permissions.yml");
        for(Group g:groups)
        {
            newperms.setInt("groups."+g.getName()+".rank", g.getRank());
            newperms.setString("groups."+g.getName()+".ladder", g.getLadder());
            newperms.setBool("groups."+g.getName()+".default", g.isDefault());
            newperms.setString("groups."+g.getName()+".display",g.getDisplay());
            newperms.setString("groups."+g.getName()+".prefix",g.getPrefix());
            newperms.setString("groups."+g.getName()+".suffix",g.getSuffix());
            newperms.setListString("groups."+g.getName()+".inheritances", g.getInheritances());
            newperms.setListString("groups."+g.getName()+".permissions", g.getPerms());
            for(String server:g.getServers().keySet())
            {
                Server s=g.getServers().get(server);
                newperms.setListString("groups."+g.getName()+".servers."+server+".permissions", s.getPerms());
                newperms.setString("groups."+g.getName()+".servers."+server+".display", s.getDisplay());
                newperms.setString("groups."+g.getName()+".servers."+server+".prefix", s.getPrefix());
                newperms.setString("groups."+g.getName()+".servers."+server+".suffix", s.getSuffix());
            }
        }
        for(User p:users)
        {
            if(deleteUsersOnCleanup)
            {
                //check for additional permissions and non-default groups AND onlinecheck
                if(p.isNothingSpecial()&BungeeCord.getInstance().getPlayer(p.getName())==null)
                {
                    deleted++;
                    continue;
                }
            }
            
            //player has to be saved
            List<String> groups=new ArrayList<>();
            for(Group g:p.getGroups())
            {
                groups.add(g.getName());
            }
            newperms.setListString("users."+p.getName()+".groups", groups);
            
            newperms.setListString("users."+p.getName()+".permissions", p.getExtraperms());
            for(String server:p.getServerPerms().keySet())
            {
                newperms.setListString("users."+p.getName()+".servers."+server+".permissions", p.getServerPerms().get(server));
            }
        }
        newperms.setInt("version", 1);
        
        newperms.save();
        
        permsconf=newperms;
        
        load();
        
        return deleted;
    }

    @Override
    public void clearDatabase() 
    {
        new File(plugin.getDataFolder()+"/permissions.yml").delete();
        permsconf=new Config(plugin,"/permissions.yml");
        load();
    }

    private void checkPermFile()
    {
        File f=new File(plugin.getDataFolder(),permspath);
        if(!f.exists()|!f.isFile())
        {
            bc.getLogger().info("[BungeePerms] no permissions file found -> copy packed default permissions.yml to data folder ...");
            f.getParentFile().mkdirs();
            try 
			{
				//file öffnen
				ClassLoader cl=this.getClass().getClassLoader();
	            URL url = cl.getResource("permissions.yml");
	            if(url!=null)
	            {
		            URLConnection connection = url.openConnection();
		            connection.setUseCaches(false);
		            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(connection.getInputStream());
		            defConfig.save(f);
	            }
	        } 
			catch (Exception e) 
	        {
				e.printStackTrace();
	        }
            bc.getLogger().info("[BungeePerms] copied default permissions.yml to data folder");
        }
    }
}
