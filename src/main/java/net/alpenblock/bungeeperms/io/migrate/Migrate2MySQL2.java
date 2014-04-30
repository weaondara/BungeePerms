package net.alpenblock.bungeeperms.io.migrate;

import java.util.List;
import net.alpenblock.bungeeperms.BungeePerms;
import net.alpenblock.bungeeperms.Config;
import net.alpenblock.bungeeperms.Debug;
import net.alpenblock.bungeeperms.Group;
import net.alpenblock.bungeeperms.User;
import net.alpenblock.bungeeperms.io.BackEnd;
import net.alpenblock.bungeeperms.io.BackEndType;
import net.alpenblock.bungeeperms.io.MySQL2BackEnd;
import net.md_5.bungee.BungeeCord;

public class Migrate2MySQL2  implements Migrator
{
    private BungeeCord bc;
    private Config config;
    private Debug debug;
    
    public Migrate2MySQL2(Config config, Debug debug)
    {
        bc=BungeeCord.getInstance();
        this.config = config;
        this.debug = debug;
    }
    
    @Override
    public void migrate(final List<Group> groups, final List<User> users, final int permsversion) 
    {
        BackEnd be=new MySQL2BackEnd(config,debug);
        be.clearDatabase();
        for(Group group:groups)
        {
            be.saveGroup(group,false);
        }
        for(User user:users)
        {
            be.saveUser(user,false);
        }
        be.saveVersion(permsversion,true);
        
        
        config.setEnumValue("backendtype",BackEndType.MySQL2);
        config.save();
        
        BungeePerms.getInstance().getPermissionsManager().setBackEnd(be);
    }
}
