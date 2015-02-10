package net.alpenblock.bungeeperms.io.migrate;

import java.util.List;

import net.alpenblock.bungeeperms.BungeePerms;
import net.alpenblock.bungeeperms.Config;
import net.alpenblock.bungeeperms.Debug;
import net.alpenblock.bungeeperms.Group;
import net.alpenblock.bungeeperms.User;
import net.alpenblock.bungeeperms.io.BackEnd;
import net.alpenblock.bungeeperms.io.BackEndType;
import net.alpenblock.bungeeperms.io.MySQLBackEnd;
import net.md_5.bungee.api.plugin.Plugin;

public class Migrate2MySQL implements Migrator
{
	private Plugin p;
    private Config config;
    private Debug debug;
    
    public Migrate2MySQL(Plugin p, Config config, Debug debug)
    {
    	this.p = p;
        this.config = config;
        this.debug = debug;
    }
    
    
    @Override
    public void migrate(final List<Group> groups, final List<User> users, final int permsversion)
    {
        BackEnd be=new MySQLBackEnd(p, config,debug);
        be.clearDatabase();
        for(Group group:groups)
        {
            be.saveGroup(group,false);
        }
        for(User user:users)
        {
            be.saveUser(user,false);
        }
        be.saveVersion(permsversion,false);
        
        config.setEnumValue("backendtype",BackEndType.MySQL);
        config.save();
        
        BungeePerms.getInstance().getPermissionsManager().setBackEnd(be);
    }
}
