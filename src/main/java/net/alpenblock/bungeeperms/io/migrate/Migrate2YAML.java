package net.alpenblock.bungeeperms.io.migrate;

import java.util.List;
import net.alpenblock.bungeeperms.BungeePerms;
import net.alpenblock.bungeeperms.Config;
import net.alpenblock.bungeeperms.Group;
import net.alpenblock.bungeeperms.User;
import net.alpenblock.bungeeperms.io.BackEnd;
import net.alpenblock.bungeeperms.io.BackEndType;
import net.alpenblock.bungeeperms.io.YAMLBackEnd;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.plugin.Plugin;

public class Migrate2YAML implements Migrator
{
    private BungeeCord bc;
    private Plugin plugin;
    private Config config;
    
    public Migrate2YAML(Plugin plugin,Config conf)
    {
        bc=BungeeCord.getInstance();
        this.plugin = plugin;
        config=conf;
    }
    
    @Override
    public void migrate(final List<Group> groups, final List<User> users, final int permsversion)
    {
        BackEnd be=new YAMLBackEnd(bc,plugin,true,false);
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
        
        
        config.setEnumValue("backendtype",BackEndType.YAML);
        config.save();
        
        BungeePerms.getInstance().getPermissionsManager().setBackEnd(be);
    }
}
