/* 
 * Copyright (C) 2020 wea_ondara
 *
 * BungeePerms is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BungeePerms is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.alpenblock.bungeeperms.io.migrate;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.SneakyThrows;
import net.alpenblock.bungeeperms.BPConfig;
import net.alpenblock.bungeeperms.BungeePerms;
import net.alpenblock.bungeeperms.Debug;
import net.alpenblock.bungeeperms.Group;
import net.alpenblock.bungeeperms.User;
import net.alpenblock.bungeeperms.io.BackEndType;
import net.alpenblock.bungeeperms.io.MySQLBackEnd;
import net.alpenblock.bungeeperms.io.MySQLUUIDPlayerDB;

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
    @SneakyThrows
    public void migrate(final List<Group> groups, final List<User> users, final Map<UUID, String> uuidplayer, final int permsversion)
    {
        debug.log("migrate backend: migrating " + groups.size() + " groups and " + users.size() + " users");
        MySQLBackEnd be = new MySQLBackEnd();
        be.clearDatabase();
        be.getMysql().getConnection().setAutoCommit(false);
        try
        {
            for (Group group : groups)
            {
                be.saveGroup(group, false);
            }
            debug.log("migrate backend: " + groups.size() + " groups migrated");

            int um = 0;
            for (User user : users)
            {
                be.saveUser(user, false);
                um++;
                if (um % 1000 == 0)
                    debug.log("migrate backend: " + um + "/" + users.size() + " users migrated");
            }
            debug.log("migrate backend: " + users.size() + " users migrated");

            be.saveVersion(permsversion, true);
            be.getMysql().getConnection().commit();
        }
        finally
        {
            be.getMysql().getConnection().setAutoCommit(true);
        }
        BungeePerms.getInstance().getPermissionsManager().setBackEnd(be);

        if (config.isUseUUIDs())
        {
            MySQLUUIDPlayerDB updb = new MySQLUUIDPlayerDB();
            updb.clear();
            updb.getMysql().getConnection().setAutoCommit(false);
            try
            {
                int um = 0;
                for (Map.Entry<UUID, String> entry : uuidplayer.entrySet())
                {
                    updb.update(entry.getKey(), entry.getValue());
                    um++;
                    if (um % 1000 == 0)
                        debug.log("migrate backend: " + um + "/" + uuidplayer.size() + " uuid/player entries migrated");
                }
                debug.log("migrate backend: " + users.size() + " uuid/player entries migrated");
            }
            finally
            {
                updb.getMysql().getConnection().setAutoCommit(true);
            }
            BungeePerms.getInstance().getPermissionsManager().setUUIDPlayerDB(updb);
        }

        config.setBackendType(BackEndType.MySQL);
    }
}
