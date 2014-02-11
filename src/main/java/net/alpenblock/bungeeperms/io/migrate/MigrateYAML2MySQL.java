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
import net.md_5.bungee.BungeeCord;

public class MigrateYAML2MySQL implements Migrator
{
    private BungeeCord bc;
    private Config config;
    private Debug debug;
    
    public MigrateYAML2MySQL(Config config, Debug debug)
    {
        bc=BungeeCord.getInstance();
        this.config = config;
        this.debug = debug;
    }
    
    
    @Override
    public void migrate(final List<Group> groups, final List<User> users, final int permsversion) 
    {
        BackEnd be=new MySQLBackEnd(bc,config,debug,true,false);
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
