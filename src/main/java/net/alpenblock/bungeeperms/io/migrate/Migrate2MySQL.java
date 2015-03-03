package net.alpenblock.bungeeperms.io.migrate;

import java.util.List;
import net.alpenblock.bungeeperms.BPConfig;
import net.alpenblock.bungeeperms.BungeePerms;
import net.alpenblock.bungeeperms.Debug;
import net.alpenblock.bungeeperms.Group;
import net.alpenblock.bungeeperms.User;
import net.alpenblock.bungeeperms.io.BackEnd;
import net.alpenblock.bungeeperms.io.BackEndType;
import net.alpenblock.bungeeperms.io.MySQLBackEnd;

public class Migrate2MySQL implements Migrator
{

    private final BPConfig config;
    private final Debug debug;

    public Migrate2MySQL(BPConfig config, Debug debug)
    {
        this.config = config;
        this.debug = debug;
    }

    @Override
    public void migrate(final List<Group> groups, final List<User> users, final int permsversion)
    {
        BackEnd be = new MySQLBackEnd();
        be.clearDatabase();
        for (Group group : groups)
        {
            be.saveGroup(group, false);
        }
        for (User user : users)
        {
            be.saveUser(user, false);
        }
        be.saveVersion(permsversion, false);

        config.setBackendType(BackEndType.MySQL);

        BungeePerms.getInstance().getPermissionsManager().setBackEnd(be);
    }
}
