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
package net.alpenblock.bungeeperms.platform.bungee;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import net.alpenblock.bungeeperms.BPConfig;
import net.alpenblock.bungeeperms.Config;

@Getter
@Deprecated
public class BungeeConfig extends BPConfig
{

    private NetworkType networkType;
    private List<String> networkServers;

    public BungeeConfig(Config config)
    {
        super(config);
    }

    @Override
    public void load()
    {
        super.load();

        networkType = config.getEnumValue("network.type", NetworkType.Global);
        if (networkType == NetworkType.ServerDependend || networkType == NetworkType.ServerDependendBlacklist)
        {
            networkServers = config.getListString("network.servers", Arrays.asList("lobby"));
        }
        else
        {
            //create option if not server dependend
            if (!config.keyExists("network.servers"))
            {
                config.setListString("network.servers", new ArrayList<String>());
            }
        }
    }

    @Override
    protected void migrate0to1(Config oldconf, Config newconf)
    {
        super.migrate0to1(oldconf, newconf);

        newconf.setEnumValue("network.type", oldconf.getEnumValue("networktype", NetworkType.Global));
        newconf.setListString("network.servers", oldconf.getListString("networkservers", new ArrayList()));
    }
}
