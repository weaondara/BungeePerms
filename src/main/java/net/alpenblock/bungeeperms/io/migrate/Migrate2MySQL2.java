package net.alpenblock.bungeeperms.io.migrate;

import java.util.List;
import net.alpenblock.bungeeperms.BPConfig;
import net.alpenblock.bungeeperms.BungeePerms;
import net.alpenblock.bungeeperms.Debug;
import net.alpenblock.bungeeperms.Group;
import net.alpenblock.bungeeperms.User;
import net.alpenblock.bungeeperms.io.BackEnd;
import net.alpenblock.bungeeperms.io.BackEndType;
import net.alpenblock.bungeeperms.io.MySQL2BackEnd;

public class Migrate2MySQL2 implements Migrator
{

    private final BPConfig config;
    private final Debug debug;

    public Migrate2MySQL2(BPConfig config, Debug debug)
    {
        this.config = config;
        this.debug = debug;
    }

    @Override
    public void migrate(final List<Group> groups, final List<User> users, final int permsversion)
    {
        BackEnd be = new MySQL2BackEnd();
        be.clearDatabase();
        for (Group group : groups)
        {
            be.saveGroup(group, false);
        }
        for (User user : users)
        {
            be.saveUser(user, false);
        }
        be.saveVersion(permsversion, true);

        config.setBackendType(BackEndType.MySQL2);

        BungeePerms.getInstance().getPermissionsManager().setBackEnd(be);
    }
}
