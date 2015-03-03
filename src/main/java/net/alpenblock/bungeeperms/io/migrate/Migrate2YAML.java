package net.alpenblock.bungeeperms.io.migrate;

import java.util.List;
import net.alpenblock.bungeeperms.BPConfig;
import net.alpenblock.bungeeperms.BungeePerms;
import net.alpenblock.bungeeperms.Group;
import net.alpenblock.bungeeperms.User;
import net.alpenblock.bungeeperms.io.BackEnd;
import net.alpenblock.bungeeperms.io.BackEndType;
import net.alpenblock.bungeeperms.io.YAMLBackEnd;

public class Migrate2YAML implements Migrator
{

    private final BPConfig config;

    public Migrate2YAML(BPConfig conf)
    {
        config = conf;
    }

    @Override
    public void migrate(final List<Group> groups, final List<User> users, final int permsversion)
    {
        BackEnd be = new YAMLBackEnd();
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

        config.setBackendType(BackEndType.YAML);

        BungeePerms.getInstance().getPermissionsManager().setBackEnd(be);
    }
}
