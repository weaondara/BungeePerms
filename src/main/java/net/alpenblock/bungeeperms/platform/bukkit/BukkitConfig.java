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
package net.alpenblock.bungeeperms.platform.bukkit;

import lombok.Getter;
import net.alpenblock.bungeeperms.BPConfig;
import net.alpenblock.bungeeperms.BungeePerms;
import net.alpenblock.bungeeperms.Config;

@Getter
public class BukkitConfig extends BPConfig
{

    private String servername;

    private boolean allowops;
    private boolean superpermscompat;

    private boolean standalone;

    public BukkitConfig(Config config)
    {
        super(config);
    }

    @Override
    public void load()
    {
        super.load();

        //perms
        servername = config.getString("permissions.servername", "servername");
        allowops = config.getBoolean("permissions.allowops", true);
        superpermscompat = config.getBoolean("permissions.superpermscompat", false);

        standalone = config.getBoolean("network.standalone", false);
    }

    public void setServerName(String servername)
    {
        this.servername = servername;
        config.setString("permissions.servername", servername);
        config.save();
        BungeePerms.getInstance().reload(false);
    }

    @Override
    protected void migrate0to1(Config oldconf, Config newconf)
    {
        super.migrate0to1(oldconf, newconf);

        newconf.setString("permissions.servername", oldconf.getString("servername", "servername"));
        newconf.setBool("permissions.allowops", oldconf.getBoolean("allowops", true));
        newconf.setBool("permissions.superpermscompat", oldconf.getBoolean("superpermscompat", false));

        newconf.setBool("network.standalone", oldconf.getBoolean("standalone", false));
    }
}
