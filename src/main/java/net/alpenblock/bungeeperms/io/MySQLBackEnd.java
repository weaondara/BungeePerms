package net.alpenblock.bungeeperms.io;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.alpenblock.bungeeperms.BungeePerms;
import net.alpenblock.bungeeperms.Config;
import net.alpenblock.bungeeperms.Debug;
import net.alpenblock.bungeeperms.Group;
import net.alpenblock.bungeeperms.Mysql;
import net.alpenblock.bungeeperms.MysqlConfig;
import net.alpenblock.bungeeperms.PermissionsManager;
import net.alpenblock.bungeeperms.Server;
import net.alpenblock.bungeeperms.User;
import net.alpenblock.bungeeperms.World;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

public class MySQLBackEnd implements BackEnd
{
    private ProxyServer bc;
    private Config config;
    private Debug debug;
    private Plugin p;
    private Mysql mysql;
    
    private MysqlConfig permsconf;
    private String table;
    private String tablePrefix;
    
    public MySQLBackEnd(Plugin plugin, Config conf, Debug d)
    {
    	this.p = plugin;
        bc = plugin.getProxy();
        config=conf;
        debug=d;
        
        loadConfig();
        
        mysql=new Mysql(conf,d,"bungeeperms");
        mysql.connect();
        
        table=tablePrefix+"permissions";
        
        permsconf=new MysqlConfig(mysql,table);
        permsconf.createTable();
    }
    private void loadConfig()
    {
        tablePrefix=config.getString("tablePrefix", "bungeeperms_");
    }
    
    @Override
    public BackEndType getType()
    {
        return BackEndType.MySQL;
    }
    
    @Override
    public void load()
    {
		//load from table
		permsconf.load();
    }
    @Override
    public List<Group> loadGroups()
    {
        List<Group> ret=new ArrayList<>();
        
        List<String> groups=permsconf.getSubNodes("groups");
		for(String g:groups)
		{
			List<String> inheritances=permsconf.getListString("groups."+g+".inheritances", new ArrayList<String>());
			List<String> permissions=permsconf.getListString("groups."+g+".permissions", new ArrayList<String>());
			boolean isdefault=permsconf.getBoolean("groups."+g+".default",false);
			int rank=permsconf.getInt("groups."+g+".rank", 1000);
			int weight=permsconf.getInt("groups."+g+".weight", 1000);
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
                
                //per server world perms
                Map<String,World> worlds=new HashMap<>();
                for(String world:permsconf.getSubNodes("groups."+g+".servers."+server+".worlds"))
                {
                    List<String> worldperms=permsconf.getListString("groups."+g+".servers."+server+".worlds."+world+".permissions", new ArrayList<String>());
                    String wdisplay=permsconf.getString("groups."+g+".servers."+server+".worlds."+world+".display", "");
                    String wprefix=permsconf.getString("groups."+g+".servers."+server+".worlds."+world+".prefix", "");
                    String wsuffix=permsconf.getString("groups."+g+".servers."+server+".worlds."+world+".suffix", "");
                    
                    World w=new World(world,worldperms,wdisplay,wprefix,wsuffix);
                    worlds.put(world, w);
                }
                
                servers.put(server, new Server(server,serverperms,worlds,sdisplay,sprefix,ssuffix));
			}
			
			Group group=new Group(p, g, inheritances, permissions, servers, rank, weight, ladder, isdefault, display, prefix, suffix);
			ret.add(group);
		}
        Collections.sort(ret);
        
        return ret;
    }
    @Override
    public List<User> loadUsers()
    {
        List<User> ret=new ArrayList<>();
        
        List<String> users=permsconf.getSubNodes("users");
		for(String u:users)
		{
			User user=BungeePerms.getInstance().getPermissionsManager().isUseUUIDs() ? loadUser(UUID.fromString(u)) : loadUser(u);
			ret.add(user);
		}
        
        return ret;
    }
    @Override
    public User loadUser(String user) 
    {
        if(!permsconf.keyExists("users."+user))
        {
            return null;
        }
        
        //load user from database
        List<String> sgroups=permsconf.getListString("users."+user+".groups", new ArrayList<String>());
        List<Group> lgroups=new ArrayList<>();
        for(String s:sgroups)
        {
            Group g=BungeePerms.getInstance().getPermissionsManager().getGroup(s);
            if(g!=null)
            {
                lgroups.add(g);
            }
        }
        List<String> extrapermissions=permsconf.getListString("users."+user+".permissions", new ArrayList<String>());

        Map<String,List<String>> serverperms=new HashMap<>();
        Map<String,Map<String,List<String>>> serverworldperms=new HashMap<>();
        for(String server:permsconf.getSubNodes("users."+user+".servers"))
        {
            //per server perms
            serverperms.put(server, permsconf.getListString("users."+user+".servers."+server+".permissions", new ArrayList<String>()));

            //per server world perms
            Map<String,List<String>> worldperms=new HashMap<>();
            for(String world:permsconf.getSubNodes("users."+user+".servers."+server+".worlds"))
            {
                worldperms.put(world, permsconf.getListString("users."+user+".servers."+server+".worlds."+world+".permissions", new ArrayList<String>()));
            }
            serverworldperms.put(server, worldperms);
        }
        
        UUID uuid=BungeePerms.getInstance().getPermissionsManager().getUUIDPlayerDB().getUUID(user);
        User u=new User(p, user, uuid, lgroups, extrapermissions, serverperms,serverworldperms);
        return u;
    }
    @Override
    public User loadUser(UUID user) 
    {
        if(!permsconf.keyExists("users."+user))
        {
            return null;
        }
        
        //load user from database
        List<String> sgroups=permsconf.getListString("users."+user+".groups", new ArrayList<String>());
        List<Group> lgroups=new ArrayList<>();
        for(String s:sgroups)
        {
            Group g=BungeePerms.getInstance().getPermissionsManager().getGroup(s);
            if(g!=null)
            {
                lgroups.add(g);
            }
        }
        List<String> extrapermissions=permsconf.getListString("users."+user+".permissions", new ArrayList<String>());

        Map<String,List<String>> serverperms=new HashMap<>();
        Map<String,Map<String,List<String>>> serverworldperms=new HashMap<>();
        for(String server:permsconf.getSubNodes("users."+user+".servers"))
        {
            //per server perms
            serverperms.put(server, permsconf.getListString("users."+user+".servers."+server+".permissions", new ArrayList<String>()));

            //per server world perms
            Map<String,List<String>> worldperms=new HashMap<>();
            for(String world:permsconf.getSubNodes("users."+user+".servers."+server+".worlds"))
            {
                worldperms.put(world, permsconf.getListString("users."+user+".servers."+server+".worlds."+world+".permissions", new ArrayList<String>()));
            }
            serverworldperms.put(server, worldperms);
        }

        String username=BungeePerms.getInstance().getPermissionsManager().getUUIDPlayerDB().getPlayerName(user);
        User u=new User(p, username, user, lgroups, extrapermissions, serverperms,serverworldperms);
        return u;
    }
    @Override
    public int loadVersion()
    {
         return permsconf.getInt("version", 1);
    }
    @Override
    public void saveVersion(int version,boolean savetodisk)
    {
        permsconf.setInt("version", version);
    }

    @Override
    public boolean isUserInDatabase(User user)
    {
        return permsconf.keyExists("users."+(BungeePerms.getInstance().getPermissionsManager().isUseUUIDs() ? user.getUUID().toString() : user.getName()));
    }
    @Override
    public List<String> getRegisteredUsers() 
    {
        return permsconf.getSubNodes("users");
    }
    @Override
    public List<String> getGroupUsers(Group group) 
    {
        List<String> users=new ArrayList<>();
        
        for(String user:permsconf.getSubNodes("users"))
        {
            if(permsconf.getListString("users."+user+".groups", new ArrayList<String>()).contains(group.getName()))
            {
                users.add(user);
            }
        }
        
        return users;
    }
    
    @Override
    public synchronized void saveUser(User user,boolean savetodisk)
    {
        if(BungeePerms.getInstance().getPermissionsManager().isSaveAllUsers()?true:!user.isNothingSpecial())
        {
            List<String> groups=new ArrayList<>();
            for(Group g:user.getGroups())
            {
                groups.add(g.getName());
            }
            permsconf.setListString("users."+(BungeePerms.getInstance().getPermissionsManager().isUseUUIDs() ? user.getUUID().toString() : user.getName())+".groups", groups);
            permsconf.setListString("users."+(BungeePerms.getInstance().getPermissionsManager().isUseUUIDs() ? user.getUUID().toString() : user.getName())+".permissions", user.getExtraperms());

            for(Map.Entry<String, List<String>> se:user.getServerPerms().entrySet())
            {
                permsconf.setListString("users."+(BungeePerms.getInstance().getPermissionsManager().isUseUUIDs() ? user.getUUID().toString() : user.getName())+".servers."+se.getKey()+".permissions", se.getValue());
            }
            for(Map.Entry<String, Map<String, List<String>>> swe:user.getServerWorldPerms().entrySet())
            {
                for(Map.Entry<String, List<String>> we:swe.getValue().entrySet())
                {
                    permsconf.getListString("users."+(BungeePerms.getInstance().getPermissionsManager().isUseUUIDs() ? user.getUUID().toString() : user.getName())+".servers."+swe.getKey()+".worlds."+we.getKey()+".permissions", we.getValue());
                }
            }
        }
    }
    @Override
    public synchronized void saveGroup(Group group,boolean savetodisk)
    {
        permsconf.setListString("groups."+group.getName()+".inheritances", group.getInheritances());
		permsconf.setListString("groups."+group.getName()+".permissions", group.getPerms());
		permsconf.setInt("groups."+group.getName()+".rank", group.getRank());
		permsconf.setString("groups."+group.getName()+".ladder", group.getLadder());
		permsconf.setBool("groups."+group.getName()+".default", group.isDefault());
		permsconf.setString("groups."+group.getName()+".display", group.getDisplay());
		permsconf.setString("groups."+group.getName()+".prefix", group.getPrefix());
		permsconf.setString("groups."+group.getName()+".suffix", group.getSuffix());

		for(Map.Entry<String, Server> se:group.getServers().entrySet())
		{
			permsconf.setListString("groups."+group.getName()+".servers."+se.getKey()+".permissions", se.getValue().getPerms());
            permsconf.setString("groups."+group.getName()+".servers."+se.getKey()+".display", se.getValue().getDisplay());
            permsconf.setString("groups."+group.getName()+".servers."+se.getKey()+".prefix", se.getValue().getPrefix());
            permsconf.setString("groups."+group.getName()+".servers."+se.getKey()+".suffix", se.getValue().getSuffix());
            
            for(Map.Entry<String,World> we:se.getValue().getWorlds().entrySet())
            {
                permsconf.setListString("groups."+group.getName()+".servers."+se.getKey()+".worlds."+we.getKey()+".permissions", we.getValue().getPerms());
                permsconf.setString("groups."+group.getName()+".servers."+se.getKey()+".worlds."+we.getKey()+".display", we.getValue().getDisplay());
                permsconf.setString("groups."+group.getName()+".servers."+se.getKey()+".worlds."+we.getKey()+".prefix", we.getValue().getPrefix());
                permsconf.setString("groups."+group.getName()+".servers."+se.getKey()+".worlds."+we.getKey()+".suffix", we.getValue().getSuffix());
            }
		}
    }
    @Override
    public synchronized void deleteUser(User user)
    {
        permsconf.deleteNode("users."+(BungeePerms.getInstance().getPermissionsManager().isUseUUIDs() ? user.getUUID().toString() : user.getName()));
    }
    @Override
    public synchronized void deleteGroup(Group group)
    {
        permsconf.deleteNode("groups."+group.getName());
    }

    @Override
    public synchronized void saveUserGroups(User user)
    {
        List<String> savegroups=new ArrayList<>();
        for(Group g:user.getGroups())
        {
            savegroups.add(g.getName());
        }
        
        permsconf.setListString("users."+(BungeePerms.getInstance().getPermissionsManager().isUseUUIDs() ? user.getUUID().toString() : user.getName())+".groups", savegroups);
    }
    @Override
    public synchronized void saveUserPerms(User user)
    {
        permsconf.setListString("users."+(BungeePerms.getInstance().getPermissionsManager().isUseUUIDs() ? user.getUUID().toString() : user.getName())+".permissions", user.getExtraperms());
    }
    @Override
    public synchronized void saveUserPerServerPerms(User user, String server) 
    {
        permsconf.setListString("users."+(BungeePerms.getInstance().getPermissionsManager().isUseUUIDs() ? user.getUUID().toString() : user.getName())+".servers."+server+".permissions", user.getServerPerms().get(server));
    }
    @Override
    public synchronized void saveUserPerServerWorldPerms(User user, String server, String world) 
    {
        permsconf.setListString("users."+(BungeePerms.getInstance().getPermissionsManager().isUseUUIDs() ? user.getUUID().toString() : user.getName())+".servers."+server+".worlds."+world+".permissions", user.getServerWorldPerms().get(server).get(world));
    }

    @Override
    public synchronized void saveGroupPerms(Group group)
    {
        permsconf.setListString("groups."+group.getName()+".permissions", group.getPerms());
    }
    @Override
    public synchronized void saveGroupPerServerPerms(Group group, String server) 
    {
        permsconf.setListString("groups."+group.getName()+".servers."+server+".permissions", group.getServers().get(server).getPerms());
    }
    @Override
    public synchronized void saveGroupPerServerWorldPerms(Group group, String server, String world)
    {
        permsconf.setListString("groups."+group.getName()+".servers."+server+".worlds."+world+".permissions", group.getServers().get(server).getWorlds().get(world).getPerms());
    }
    @Override
    public synchronized void saveGroupInheritances(Group group)
    {
        permsconf.setListString("groups."+group.getName()+".inheritances", group.getInheritances());
    }
    @Override
    public synchronized void saveGroupLadder(Group group)
    {
        permsconf.setString("groups."+group.getName()+".ladder", group.getLadder());
    }
    @Override
    public synchronized void saveGroupRank(Group group)
    {
        permsconf.setInt("groups."+group.getName()+".rank", group.getRank());
    }
    @Override
    public synchronized void saveGroupWeight(Group group)
    {
        permsconf.setInt("groups."+group.getName()+".weight", group.getWeight());
    }
    @Override
    public synchronized void saveGroupDefault(Group group)
    {
        permsconf.setBool("groups."+group.getName()+".default", group.isDefault());
    }
    @Override
    public synchronized void saveGroupDisplay(Group group, String server, String world)
    {
        permsconf.setString("groups."+group.getName()+(server!=null?".servers."+server+(world!=null?".worlds."+world:""):"")+".display", group.getDisplay());
    }
    @Override
    public synchronized void saveGroupPrefix(Group group, String server, String world)
    {
        permsconf.setString("groups."+group.getName()+(server!=null?".servers."+server+(world!=null?".worlds."+world:""):"")+".prefix", group.getPrefix());
    }
    @Override
    public synchronized void saveGroupSuffix(Group group, String server, String world)
    {
        permsconf.setString("groups."+group.getName()+(server!=null?".servers."+server+(world!=null?".worlds."+world:""):"")+".suffix", group.getSuffix());
    }
    
    @Override
    public synchronized void format(List<Group> groups, List<User> users,int version) 
    {
        clearDatabase();
        for(int i=0;i<groups.size();i++)
        {
            saveGroup(groups.get(i),false);
        }
        for(int i=0;i<users.size();i++)
        {
            saveUser(users.get(i),false);
        }
        saveVersion(version,false);
    }
    @Override
    public synchronized int cleanup(List<Group> groups, List<User> users,int version) 
    {
        int deleted=0;
        
        clearDatabase() ;
        for(int i=0;i<groups.size();i++)
        {
            saveGroup(groups.get(i),false);
        }
        for(int i=0;i<users.size();i++)
        {
            User u=users.get(i);
            if(BungeePerms.getInstance().getPermissionsManager().isDeleteUsersOnCleanup())
            {
                //check for additional permissions and non-default groups AND onlinecheck
                if(u.isNothingSpecial() && 
                        bc.getPlayer(u.getName())==null && 
                        bc.getPlayer(u.getUUID())==null)
                {
                    deleted++;
                    continue;
                }
            }
            
            //player has to be saved
            saveUser(users.get(i),false);
        }
        saveVersion(version,false);
        
        return deleted;
    }

    @Override
    public void clearDatabase() 
    {
        permsconf.clearTable(table);
        permsconf=new MysqlConfig(mysql,table);
        load();
    }
}
