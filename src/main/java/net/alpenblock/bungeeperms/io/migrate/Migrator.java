package net.alpenblock.bungeeperms.io.migrate;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.alpenblock.bungeeperms.Group;
import net.alpenblock.bungeeperms.User;

public interface Migrator
{

    public void migrate(final List<Group> groups, final List<User> users, final Map<UUID, String> uuidplayer, final int permsversion);
}
