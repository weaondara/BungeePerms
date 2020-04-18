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
import net.alpenblock.bungeeperms.BPConfig;
import net.alpenblock.bungeeperms.BungeePerms;
import net.alpenblock.bungeeperms.Group;
import net.alpenblock.bungeeperms.User;
import net.alpenblock.bungeeperms.io.BackEnd;
import net.alpenblock.bungeeperms.io.BackEndType;
import net.alpenblock.bungeeperms.io.MySQLUUIDPlayerDB;
import net.alpenblock.bungeeperms.io.YAMLBackEnd;
import net.alpenblock.bungeeperms.io.YAMLUUIDPlayerDB;

public class Migrate2YAML implements Migrator
{

    private final BPConfig config;

    public Migrate2YAML(BPConfig conf)
    {
        config = conf;
    }

    @Override
    public void migrate(final List<Group> groups, final List<User> users, final Map<UUID, String> uuidplayer, final int permsversion)
    {
        YAMLBackEnd be = new YAMLBackEnd();
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
        BungeePerms.getInstance().getPermissionsManager().setBackEnd(be);

        if (config.isUseUUIDs())
        {
            YAMLUUIDPlayerDB updb = new YAMLUUIDPlayerDB();
            updb.clear();
            updb.setAutosave(false);
            for (Map.Entry<UUID, String> entry : uuidplayer.entrySet())
            {
                updb.update(entry.getKey(), entry.getValue());
            }
            updb.setAutosave(true);
            updb.save();
            BungeePerms.getInstance().getPermissionsManager().setUUIDPlayerDB(updb);
        }

        config.setBackendType(BackEndType.YAML);
    }
}
