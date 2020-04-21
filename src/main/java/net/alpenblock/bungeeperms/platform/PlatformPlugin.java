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
package net.alpenblock.bungeeperms.platform;

import java.io.File;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public interface PlatformPlugin
{

    public String getPluginName();

    public String getVersion();

    public String getAuthor();

    public String getPluginFolderPath();

    public File getPluginFolder();

    public Sender getPlayer(String name);

    public Sender getPlayer(UUID uuid);

    public Sender getConsole();

    public List<Sender> getPlayers();

    public Logger getLogger();

    public PlatformType getPlatformType();

    public boolean isChatApiPresent();

    public MessageEncoder newMessageEncoder();

    public int registerRepeatingTask(Runnable r, long delay, long interval);

    public int runTaskLater(Runnable r, long delay);

    public int runTaskLaterAsync(Runnable r, long delay);

    public void cancelTask(int id);

    public Integer getBuild();
}
