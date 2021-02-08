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
package net.alpenblock.bungeeperms.io;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import lombok.Getter;
import lombok.Setter;
import net.alpenblock.bungeeperms.BungeePerms;
import net.alpenblock.bungeeperms.Config;

public class YAMLUUIDPlayerDB implements UUIDPlayerDB {

    private Config uuidconf;
    @Getter
    @Setter
    private boolean autosave = true;

    private final ReentrantLock lock = new ReentrantLock();

    public YAMLUUIDPlayerDB() {
        uuidconf = new Config(BungeePerms.getInstance().getPlugin(), "/uuidplayerdb.yml");
        uuidconf.load();
    }

    @Override
    public BackEndType getType() {
        return BackEndType.YAML;
    }

    @Override
    public UUID getUUID(String player) {
        UUID ret = null;

        lock.lock();
        try {
            for (String uuid : uuidconf.getSubNodes("")) {
                String p = uuidconf.getString(uuid, "");
                if (p.equalsIgnoreCase(player)) {
                    ret = UUID.fromString(uuid);
                }
            }
        } finally {
            lock.unlock();
        }

        return ret;
    }

    @Override
    public String getPlayerName(UUID uuid) {
        String ret = null;

        lock.lock();
        try {
            for (String suuid : uuidconf.getSubNodes("")) {
                if (suuid.equalsIgnoreCase(uuid.toString())) {
                    ret = uuidconf.getString(suuid, "");
                }
            }
        } finally {
            lock.unlock();
        }

        return ret;
    }

    @Override
    public void update(UUID uuid, String player) {
        lock.lock();
        try {
            for (String suuid : uuidconf.getSubNodes("")) {
                if (suuid.equalsIgnoreCase(uuid.toString()) || uuidconf.getString(suuid, "").equalsIgnoreCase(player)) {
                    uuidconf.deleteNode(suuid);
                }
            }
            if (autosave)
                uuidconf.setStringAndSave(uuid.toString(), player);
            else
                uuidconf.setString(uuid.toString(), player);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Map<UUID, String> getAll() {
        Map<UUID, String> ret = new HashMap<>();

        lock.lock();
        try {
            for (String suuid : uuidconf.getSubNodes("")) {
                ret.put(UUID.fromString(suuid), uuidconf.getString(suuid, ""));
            }
        } finally {
            lock.unlock();
        }

        return ret;
    }

    @Override
    public void clear() {
        lock.lock();
        try {
            new File(BungeePerms.getInstance().getPlugin().getPluginFolder(), "/uuidplayerdb.yml").delete();
            uuidconf = new Config(BungeePerms.getInstance().getPlugin(), "/uuidplayerdb.yml");
            uuidconf.load();
        } finally {
            lock.unlock();
        }
    }

    public void save() {
        lock.lock();
        try {
            uuidconf.save();
        } finally {
            lock.unlock();
        }
    }
}
